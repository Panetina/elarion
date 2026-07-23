package panetina.elarion.core.service;

import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded single-writer lane for catch journals, replay, and summary checkpoints. */
public final class CatchTelemetryWorker {
    public static final int DEFAULT_MAX_QUEUED_TASKS = 4096;
    public static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);

    private final CatchTelemetryService telemetry;
    private final int maxQueuedTasks;
    private final Duration shutdownTimeout;
    private final AtomicBoolean maintenanceQueued = new AtomicBoolean();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private ThreadPoolExecutor executor;

    public CatchTelemetryWorker(CatchTelemetryService telemetry) {
        this(telemetry, DEFAULT_MAX_QUEUED_TASKS, DEFAULT_SHUTDOWN_TIMEOUT);
    }

    CatchTelemetryWorker(CatchTelemetryService telemetry, int maxQueuedTasks, Duration shutdownTimeout) {
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry");
        if (maxQueuedTasks < 1 || shutdownTimeout.isZero() || shutdownTimeout.isNegative()) {
            throw new IllegalArgumentException("catch telemetry worker bounds must be positive");
        }
        this.maxQueuedTasks = maxQueuedTasks;
        this.shutdownTimeout = shutdownTimeout;
    }

    public synchronized void bind(Path root) {
        if (executor != null) throw new IllegalStateException("catch telemetry worker is already bound");
        telemetry.bind(root);
        maintenanceQueued.set(false);
        executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueuedTasks), threadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    public CompletableFuture<AcceptedCatchRecord> submit(CatchTelemetryEvent event) {
        Objects.requireNonNull(event, "event");
        CompletableFuture<AcceptedCatchRecord> result = new CompletableFuture<>();
        execute(() -> {
            try {
                result.complete(telemetry.accept(event));
            } catch (RuntimeException exception) {
                failed.incrementAndGet();
                result.completeExceptionally(exception);
            }
        }, result);
        return result;
    }

    public void activate(UUID actorId) {
        Objects.requireNonNull(actorId, "actorId");
        execute(() -> telemetry.activate(actorId), null);
    }

    public void save(UUID actorId) {
        Objects.requireNonNull(actorId, "actorId");
        execute(() -> telemetry.save(actorId), null);
    }

    /** Coalesces the server tick signal so maintenance can never flood its own queue. */
    public void tick() {
        if (!maintenanceQueued.compareAndSet(false, true)) return;
        try {
            execute(() -> {
                try {
                    telemetry.tick();
                } finally {
                    maintenanceQueued.set(false);
                }
            }, null);
        } catch (RuntimeException exception) {
            maintenanceQueued.set(false);
            throw exception;
        }
    }

    public CatchSummary cachedSummary(UUID actorId) {
        return telemetry.cachedSummary(actorId);
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(executor != null && !executor.isShutdown(),
                executor == null ? 0 : executor.getQueue().size(), maxQueuedTasks,
                rejected.get(), failed.get(), maintenanceQueued.get());
    }

    public void shutdown() {
        ThreadPoolExecutor current;
        CompletableFuture<Void> finalSave = new CompletableFuture<>();
        synchronized (this) {
            current = executor;
            if (current == null) return;
            try {
                current.getQueue().put(() -> {
                    try {
                        telemetry.shutdown();
                        finalSave.complete(null);
                    } catch (RuntimeException exception) {
                        finalSave.completeExceptionally(exception);
                    }
                });
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while queuing the catch telemetry shutdown checkpoint", exception);
            }
            current.shutdown();
        }
        try {
            if (!current.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Timed out draining catch telemetry; accepted tasks were not discarded");
            }
            finalSave.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while draining catch telemetry", exception);
        }
        synchronized (this) {
            if (executor == current) executor = null;
        }
    }

    private void execute(Runnable task, CompletableFuture<?> result) {
        ThreadPoolExecutor current;
        synchronized (this) {
            current = executor;
            if (current == null || current.isShutdown()) {
                rejected.incrementAndGet();
                IllegalStateException exception = new IllegalStateException("catch telemetry worker is not accepting tasks");
                if (result != null) result.completeExceptionally(exception);
                else throw exception;
                return;
            }
            try {
                current.execute(() -> {
                    try {
                        task.run();
                    } catch (RuntimeException exception) {
                        failed.incrementAndGet();
                        if (result != null) result.completeExceptionally(exception);
                    }
                });
            } catch (RejectedExecutionException exception) {
                rejected.incrementAndGet();
                CatchQueueFullException failure = new CatchQueueFullException(maxQueuedTasks, exception);
                if (result != null) result.completeExceptionally(failure);
                else throw failure;
            }
        }
    }

    private static ThreadFactory threadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "elarion-catch-telemetry");
            thread.setDaemon(true);
            return thread;
        };
    }

    public record Snapshot(
            boolean accepting,
            int queuedTasks,
            int maxQueuedTasks,
            long rejectedTasks,
            long failedTasks,
            boolean maintenanceQueued
    ) {
    }

    public static final class CatchQueueFullException extends RejectedExecutionException {
        CatchQueueFullException(int maximum, Throwable cause) {
            super("Core catch telemetry queue is full (maximum " + maximum + ")", cause);
        }
    }
}
