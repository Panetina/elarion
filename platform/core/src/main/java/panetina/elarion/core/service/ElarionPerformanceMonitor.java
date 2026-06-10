package panetina.elarion.core.service;

import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class ElarionPerformanceMonitor {
    private static final long WARNING_COOLDOWN_NANOS = TimeUnit.SECONDS.toNanos(30);
    private static final ConcurrentHashMap<String, Counter> COUNTERS = new ConcurrentHashMap<>();
    private static volatile long warningThresholdNanos = TimeUnit.MILLISECONDS.toNanos(50);
    private static volatile Logger logger;

    private ElarionPerformanceMonitor() {
    }

    public static void configure(Logger configuredLogger, long slowOperationWarningNanos) {
        logger = configuredLogger;
        warningThresholdNanos = Math.max(1L, slowOperationWarningNanos);
    }

    public static void record(String operation, long nanos) {
        if (operation == null || operation.isBlank()) {
            operation = "unnamed";
        }
        Counter counter = COUNTERS.computeIfAbsent(operation, ignored -> new Counter());
        counter.total.incrementAndGet();
        counter.totalNanos.addAndGet(Math.max(0L, nanos));
        counter.maxNanos.accumulateAndGet(Math.max(0L, nanos), Math::max);
        if (nanos > warningThresholdNanos) {
            counter.slow.incrementAndGet();
            long now = System.nanoTime();
            long previous = counter.lastWarningNanos.get();
            if (now - previous >= WARNING_COOLDOWN_NANOS
                    && counter.lastWarningNanos.compareAndSet(previous, now)) {
                Logger activeLogger = logger;
                if (activeLogger != null) {
                    activeLogger.warn("Slow Elarion operation {} took {} ms",
                            operation, String.format(java.util.Locale.ROOT, "%.3f", nanos / 1_000_000.0D));
                }
            }
        }
    }

    public static Map<String, Snapshot> snapshot() {
        Map<String, Snapshot> values = new LinkedHashMap<>();
        COUNTERS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Counter counter = entry.getValue();
                    values.put(entry.getKey(), new Snapshot(
                            counter.total.get(),
                            counter.slow.get(),
                            counter.totalNanos.get(),
                            counter.maxNanos.get()
                    ));
                });
        return values;
    }

    private static final class Counter {
        private final AtomicLong total = new AtomicLong();
        private final AtomicLong slow = new AtomicLong();
        private final AtomicLong totalNanos = new AtomicLong();
        private final AtomicLong maxNanos = new AtomicLong();
        private final AtomicLong lastWarningNanos = new AtomicLong();
    }

    public record Snapshot(long total, long slow, long totalNanos, long maxNanos) {
        public double averageMillis() {
            return total == 0L ? 0.0D : (totalNanos / (double) total) / 1_000_000.0D;
        }

        public double maxMillis() {
            return maxNanos / 1_000_000.0D;
        }
    }
}
