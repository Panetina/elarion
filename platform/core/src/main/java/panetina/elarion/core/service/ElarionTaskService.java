package panetina.elarion.core.service;

import org.slf4j.Logger;

import java.util.Objects;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ElarionTaskService {
    private static final int DEFAULT_IO_WORKERS = 1;
    private static final int DEFAULT_COMPUTE_WORKERS = 2;
    private static final int DEFAULT_MAX_QUEUED_SERVER_TASKS = 4096;
    private static final int DEFAULT_MAX_SERVER_APPLIES_PER_TICK = 256;
    private static final long DEFAULT_MAX_SERVER_APPLY_NANOS = TimeUnit.MILLISECONDS.toNanos(2);

    private final Logger logger;
    private final ElarionTaskConfig.Settings settings;
    private final ExecutorService ioExecutor;
    private final ExecutorService computeExecutor;
    private final ConcurrentLinkedQueue<ServerTask> serverTasks = new ConcurrentLinkedQueue<>();
    private final int maxQueuedServerTasks;
    private final int maxServerAppliesPerTick;
    private final long maxServerApplyNanos;
    private final AtomicInteger queuedServerTasks = new AtomicInteger();
    private final AtomicLong rejectedServerTasks = new AtomicLong();
    private final AtomicLong completedServerTasks = new AtomicLong();
    private final AtomicLong failedServerTasks = new AtomicLong();
    private final WorkerCounters ioCounters = new WorkerCounters();
    private final WorkerCounters computeCounters = new WorkerCounters();
    private final ConcurrentHashMap<String, AtomicLong> rejectedByFamily = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> completedByFamily = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> failedByFamily = new ConcurrentHashMap<>();
    private volatile long lastTickApplied;
    private volatile long lastTickNanos;
    private volatile long maxTickNanos;
    private volatile double rollingApplyMillis;
    private final AtomicLong slowServerApplyTicks = new AtomicLong();

    public ElarionTaskService(Logger logger) {
        this(logger, Budget.defaults());
    }

    public ElarionTaskService(Logger logger, Budget budget) {
        this(logger, new ElarionTaskConfig.Settings(
                "unknown_online_host",
                "likely",
                budget,
                ElarionTaskConfig.Monitoring.defaults(),
                false,
                List.of()
        ));
    }

    public ElarionTaskService(Logger logger, ElarionTaskConfig.Settings settings) {
        this(
                logger,
                settings,
                settings.budget().ioWorkers(),
                settings.budget().computeWorkers(),
                settings.budget().maxQueuedServerTasks(),
                settings.budget().maxServerAppliesPerTick(),
                settings.budget().maxServerApplyNanos()
        );
    }

    public ElarionTaskService(
            Logger logger,
            int ioWorkers,
            int computeWorkers,
            int maxQueuedServerTasks,
            int maxServerAppliesPerTick,
            long maxServerApplyNanos
    ) {
        this(logger, new ElarionTaskConfig.Settings(
                "unknown_online_host",
                "likely",
                new Budget(ioWorkers, computeWorkers, maxQueuedServerTasks, maxServerAppliesPerTick, maxServerApplyNanos),
                ElarionTaskConfig.Monitoring.defaults(),
                false,
                List.of()
        ), ioWorkers, computeWorkers, maxQueuedServerTasks, maxServerAppliesPerTick, maxServerApplyNanos);
    }

    private ElarionTaskService(
            Logger logger,
            ElarionTaskConfig.Settings settings,
            int ioWorkers,
            int computeWorkers,
            int maxQueuedServerTasks,
            int maxServerAppliesPerTick,
            long maxServerApplyNanos
    ) {
        this.logger = logger;
        this.settings = settings;
        ElarionPerformanceMonitor.configure(logger, settings.monitoring().slowOperationWarningNanos());
        this.ioExecutor = Executors.newFixedThreadPool(Math.max(1, ioWorkers),
                factory("elarion-io"));
        this.computeExecutor = Executors.newFixedThreadPool(Math.max(1, computeWorkers),
                factory("elarion-compute"));
        this.maxQueuedServerTasks = Math.max(1, maxQueuedServerTasks);
        this.maxServerAppliesPerTick = Math.max(1, maxServerAppliesPerTick);
        this.maxServerApplyNanos = Math.max(1L, maxServerApplyNanos);
    }

    public CompletableFuture<Void> submitIo(String name, Runnable task) {
        Objects.requireNonNull(task, "task");
        return CompletableFuture.runAsync(wrapBackground(name, task, ioCounters), ioExecutor);
    }

    public <T> CompletableFuture<T> submitCompute(String name, Supplier<T> task) {
        Objects.requireNonNull(task, "task");
        String taskName = name == null ? "unnamed" : name;
        computeCounters.submitted(taskName);
        return CompletableFuture.supplyAsync(() -> {
            computeCounters.started();
            try {
                T value = task.get();
                computeCounters.completed(taskName);
                return value;
            } catch (RuntimeException exception) {
                logger.error("Elarion compute task failed: {}", name, exception);
                computeCounters.failed(taskName);
                throw exception;
            } finally {
                computeCounters.finished();
            }
        }, computeExecutor);
    }

    public boolean enqueueServer(String name, Runnable task) {
        Objects.requireNonNull(task, "task");
        while (true) {
            int current = queuedServerTasks.get();
            if (current >= maxQueuedServerTasks) {
                rejectedServerTasks.incrementAndGet();
                increment(rejectedByFamily, family(name));
                return false;
            }
            if (queuedServerTasks.compareAndSet(current, current + 1)) {
                serverTasks.add(new ServerTask(name == null ? "unnamed" : name, task));
                return true;
            }
        }
    }

    public void tickServerQueue() {
        long started = System.nanoTime();
        long appliedNanos = 0L;
        int applied = 0;
        while (applied < maxServerAppliesPerTick && System.nanoTime() - started < maxServerApplyNanos) {
            ServerTask task = serverTasks.poll();
            if (task == null) break;
            queuedServerTasks.decrementAndGet();
            long taskStarted = System.nanoTime();
            try {
                task.runnable().run();
                completedServerTasks.incrementAndGet();
                increment(completedByFamily, task.family());
            } catch (RuntimeException exception) {
                failedServerTasks.incrementAndGet();
                increment(failedByFamily, task.family());
                logger.error("Elarion server-thread task failed: {}", task.name(), exception);
            } finally {
                appliedNanos += System.nanoTime() - taskStarted;
            }
            applied++;
        }
        lastTickApplied = applied;
        // Report queue-owned work only. Measuring the whole callback blamed an
        // empty queue when the server thread was descheduled before polling.
        lastTickNanos = appliedNanos;
        maxTickNanos = Math.max(maxTickNanos, lastTickNanos);
        double millis = lastTickNanos / 1_000_000.0D;
        rollingApplyMillis = rollingApplyMillis == 0.0D
                ? millis
                : rollingApplyMillis * 0.9D + millis * 0.1D;
        if (lastTickNanos > maxServerApplyNanos) {
            slowServerApplyTicks.incrementAndGet();
        }
        if (applied > 0) {
            ElarionPerformanceMonitor.record("server-queue-apply", lastTickNanos);
        }
    }

    public Snapshot snapshot() {
        return new Snapshot(
                queuedServerTasks.get(),
                rejectedServerTasks.get(),
                completedServerTasks.get(),
                failedServerTasks.get(),
                lastTickApplied,
                lastTickNanos,
                maxTickNanos,
                rollingApplyMillis,
                slowServerApplyTicks.get(),
                maxQueuedServerTasks,
                maxServerAppliesPerTick,
                maxServerApplyNanos,
                settings.hardwareProfile(),
                settings.cpuSharingRisk(),
                settings.usingFallback(),
                settings.budget().ioWorkers(),
                settings.budget().computeWorkers(),
                settings.monitoring().tickWarningNanos(),
                settings.monitoring().queueWarningThreshold(),
                settings.monitoring().slowOperationWarningNanos(),
                settings.monitoring().sampleIntervalSeconds(),
                settings.monitoring().worldSamplesEnabled(),
                settings.monitoring().realmSamplesEnabled(),
                settings.monitoring().headroomWarmNanos(),
                settings.monitoring().headroomPressureNanos(),
                settings.monitoring().headroomOverloadedNanos(),
                settings.validationWarnings(),
                copy(rejectedByFamily),
                copy(completedByFamily),
                copy(failedByFamily),
                ioCounters.snapshot(),
                computeCounters.snapshot()
        );
    }

    public void shutdown() {
        shutdown(ioExecutor, "io");
        shutdown(computeExecutor, "compute");
    }

    private Runnable wrapBackground(String name, Runnable task, WorkerCounters counters) {
        String taskName = name == null ? "unnamed" : name;
        counters.submitted(taskName);
        return () -> {
            counters.started();
            try {
                task.run();
                counters.completed(taskName);
            } catch (RuntimeException exception) {
                logger.error("Elarion background task failed: {}", name, exception);
                counters.failed(taskName);
                throw exception;
            } finally {
                counters.finished();
            }
        };
    }

    private void shutdown(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            logger.warn("Interrupted while shutting down Elarion {} executor", name);
        }
    }

    private static ThreadFactory factory(String prefix) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    private static void increment(ConcurrentHashMap<String, AtomicLong> values, String family) {
        values.computeIfAbsent(family, ignored -> new AtomicLong()).incrementAndGet();
    }

    private static Map<String, Long> copy(ConcurrentHashMap<String, AtomicLong> source) {
        Map<String, Long> values = new LinkedHashMap<>();
        source.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> values.put(entry.getKey(), entry.getValue().get()));
        return values;
    }

    private static String family(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed";
        }
        int separator = name.indexOf(':');
        String value = separator < 0 ? name : name.substring(0, separator);
        return value.isBlank() ? "unnamed" : value;
    }

    private record ServerTask(String name, String family, Runnable runnable) {
        private ServerTask(String name, Runnable runnable) {
            this(name, ElarionTaskService.family(name), runnable);
        }
    }

    private static final class WorkerCounters {
        private final AtomicLong submitted = new AtomicLong();
        private final AtomicLong completed = new AtomicLong();
        private final AtomicLong failed = new AtomicLong();
        private final AtomicInteger active = new AtomicInteger();
        private final ConcurrentHashMap<String, AtomicLong> submittedByFamily = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicLong> completedByFamily = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicLong> failedByFamily = new ConcurrentHashMap<>();

        private void submitted(String name) {
            submitted.incrementAndGet();
            increment(submittedByFamily, family(name));
        }

        private void started() {
            active.incrementAndGet();
        }

        private void completed(String name) {
            completed.incrementAndGet();
            increment(completedByFamily, family(name));
        }

        private void failed(String name) {
            failed.incrementAndGet();
            increment(failedByFamily, family(name));
        }

        private void finished() {
            active.decrementAndGet();
        }

        private WorkerSnapshot snapshot() {
            long submittedCount = submitted.get();
            long completedCount = completed.get();
            long failedCount = failed.get();
            int activeCount = active.get();
            long queuedCount = Math.max(0L, submittedCount - completedCount - failedCount - activeCount);
            return new WorkerSnapshot(
                    submittedCount,
                    queuedCount,
                    activeCount,
                    completedCount,
                    failedCount,
                    copy(submittedByFamily),
                    copy(completedByFamily),
                    copy(failedByFamily));
        }
    }

    public record WorkerSnapshot(
            long submittedTasks,
            long queuedTasks,
            int activeTasks,
            long completedTasks,
            long failedTasks,
            Map<String, Long> submittedByFamily,
            Map<String, Long> completedByFamily,
            Map<String, Long> failedByFamily
    ) {
    }

    public record Snapshot(
            int queuedServerTasks,
            long rejectedServerTasks,
            long completedServerTasks,
            long failedServerTasks,
            long lastTickApplied,
            long lastTickNanos,
            long maxTickNanos,
            double rollingApplyMillis,
            long slowServerApplyTicks,
            int maxQueuedServerTasks,
            int maxServerAppliesPerTick,
            long maxServerApplyNanos,
            String hardwareProfile,
            String cpuSharingRisk,
            boolean usingFallbackConfig,
            int ioWorkers,
            int computeWorkers,
            long tickWarningNanos,
            int queueWarningThreshold,
            long slowOperationWarningNanos,
            int sampleIntervalSeconds,
            boolean worldSamplesEnabled,
            boolean realmSamplesEnabled,
            long headroomWarmNanos,
            long headroomPressureNanos,
            long headroomOverloadedNanos,
            List<String> validationWarnings,
            Map<String, Long> rejectedByFamily,
            Map<String, Long> completedByFamily,
            Map<String, Long> failedByFamily,
            WorkerSnapshot io,
            WorkerSnapshot compute
    ) {
        public double lastTickMillis() {
            return lastTickNanos / 1_000_000.0D;
        }

        public double maxServerApplyMillis() {
            return maxServerApplyNanos / 1_000_000.0D;
        }

        public double maxTickMillis() {
            return maxTickNanos / 1_000_000.0D;
        }

        public double tickWarningMillis() {
            return tickWarningNanos / 1_000_000.0D;
        }

        public double slowOperationWarningMillis() {
            return slowOperationWarningNanos / 1_000_000.0D;
        }

        public double headroomWarmMillis() {
            return headroomWarmNanos / 1_000_000.0D;
        }

        public double headroomPressureMillis() {
            return headroomPressureNanos / 1_000_000.0D;
        }

        public double headroomOverloadedMillis() {
            return headroomOverloadedNanos / 1_000_000.0D;
        }

        public double queueUsage() {
            return maxQueuedServerTasks <= 0 ? 0.0D : queuedServerTasks / (double) maxQueuedServerTasks;
        }

        public boolean queuePressure() {
            return queueOverWarningThreshold() || rejectedServerTasks > 0;
        }

        public boolean queueOverWarningThreshold() {
            return queuedServerTasks >= queueWarningThreshold;
        }

        public boolean serverApplyOverBudget() {
            return lastTickNanos > maxServerApplyNanos;
        }
    }

    public record Budget(
            int ioWorkers,
            int computeWorkers,
            int maxQueuedServerTasks,
            int maxServerAppliesPerTick,
            long maxServerApplyNanos
    ) {
        public Budget {
            ioWorkers = Math.max(1, ioWorkers);
            computeWorkers = Math.max(1, computeWorkers);
            maxQueuedServerTasks = Math.max(1, maxQueuedServerTasks);
            maxServerAppliesPerTick = Math.max(1, maxServerAppliesPerTick);
            maxServerApplyNanos = Math.max(1L, maxServerApplyNanos);
        }

        public static Budget defaults() {
            return new Budget(
                    DEFAULT_IO_WORKERS,
                    DEFAULT_COMPUTE_WORKERS,
                    DEFAULT_MAX_QUEUED_SERVER_TASKS,
                    DEFAULT_MAX_SERVER_APPLIES_PER_TICK,
                    DEFAULT_MAX_SERVER_APPLY_NANOS
            );
        }
    }
}
