package panetina.elarion.core.metric;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Single-writer, bounded metric persistence lane. Gameplay threads only admit
 * immutable batches; journal fsync, projection mutation, and checkpoints happen
 * on this worker in source submission order.
 */
public final class MetricProjectionWorker {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("elarion_core_metrics");
    public static final int DEFAULT_MAX_QUEUED_BATCHES = 4096;
    public static final int DEFAULT_CHECKPOINT_INTERVAL = 8192;
    public static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);

    private final PersistentMetricProjectionService persistence;
    private final int maxQueuedBatches;
    private final int checkpointInterval;
    private final Duration shutdownTimeout;
    private final java.util.function.Consumer<MetricUpdatedEvent> appliedListener;
    private final AtomicLong accepted = new AtomicLong();
    private final AtomicLong applied = new AtomicLong();
    private final AtomicLong exactRetries = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();
    private ThreadPoolExecutor executor;
    private int commitsSinceCheckpoint;

    public MetricProjectionWorker(PersistentMetricProjectionService persistence) {
        this(persistence, ignored -> {}, DEFAULT_MAX_QUEUED_BATCHES, DEFAULT_CHECKPOINT_INTERVAL,
                DEFAULT_SHUTDOWN_TIMEOUT);
    }

    public MetricProjectionWorker(
            PersistentMetricProjectionService persistence,
            java.util.function.Consumer<MetricUpdatedEvent> appliedListener
    ) {
        this(persistence, appliedListener, DEFAULT_MAX_QUEUED_BATCHES, DEFAULT_CHECKPOINT_INTERVAL,
                DEFAULT_SHUTDOWN_TIMEOUT);
    }

    MetricProjectionWorker(
            PersistentMetricProjectionService persistence,
            int maxQueuedBatches,
            int checkpointInterval,
            Duration shutdownTimeout
    ) {
        this(persistence, ignored -> {}, maxQueuedBatches, checkpointInterval, shutdownTimeout);
    }

    MetricProjectionWorker(
            PersistentMetricProjectionService persistence,
            java.util.function.Consumer<MetricUpdatedEvent> appliedListener,
            int maxQueuedBatches,
            int checkpointInterval,
            Duration shutdownTimeout
    ) {
        this.persistence = Objects.requireNonNull(persistence, "persistence");
        this.appliedListener = Objects.requireNonNull(appliedListener, "appliedListener");
        if (maxQueuedBatches < 1 || checkpointInterval < 1 || shutdownTimeout.isNegative()
                || shutdownTimeout.isZero()) {
            throw new IllegalArgumentException("metric worker bounds must be positive");
        }
        this.maxQueuedBatches = maxQueuedBatches;
        this.checkpointInterval = checkpointInterval;
        this.shutdownTimeout = shutdownTimeout;
    }

    public synchronized long bind(Path root) {
        if (executor != null) throw new IllegalStateException("metric worker is already bound");
        long recovered = persistence.bind(root);
        commitsSinceCheckpoint = 0;
        executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(maxQueuedBatches), threadFactory(), new ThreadPoolExecutor.AbortPolicy());
        return recovered;
    }

    public synchronized void registerDescriptors(Collection<MetricDescriptor> descriptors) {
        if (executor != null) throw new IllegalStateException("metric descriptors are frozen while bound");
        persistence.registerDescriptors(descriptors);
    }

    public CompletableFuture<CommitResult> submit(MetricUpdateBatch batch) {
        Objects.requireNonNull(batch, "batch");
        CompletableFuture<CommitResult> result = new CompletableFuture<>();
        ThreadPoolExecutor current;
        synchronized (this) {
            current = executor;
            if (current == null || current.isShutdown()) {
                rejected.incrementAndGet();
                result.completeExceptionally(new IllegalStateException("metric worker is not accepting batches"));
                return result;
            }
            try {
                current.execute(() -> commit(batch, result));
                accepted.incrementAndGet();
            } catch (RejectedExecutionException exception) {
                rejected.incrementAndGet();
                result.completeExceptionally(new MetricQueueFullException(maxQueuedBatches, exception));
            }
        }
        return result;
    }

    public MetricRankEntry player(MetricQuery query, java.util.UUID actorId) {
        return persistence.player(query, actorId);
    }

    public MetricPage top(MetricQuery query, int limit) {
        return persistence.top(query, limit);
    }

    public MetricPage pageAfter(MetricQuery query, MetricCursor cursor, int limit) {
        return persistence.pageAfter(query, cursor, limit);
    }

    public MetricPage around(MetricQuery query, java.util.UUID actorId, int radius) {
        return persistence.around(query, actorId, radius);
    }

    public long revision(MetricQuery query) {
        return persistence.revision(query);
    }

    public long nextSourceSequence(net.minecraft.util.Identifier sourceSystem, String sourcePartition) {
        return persistence.nextSourceSequence(sourceSystem, sourcePartition);
    }

    public synchronized Snapshot snapshot() {
        int queued = executor == null ? 0 : executor.getQueue().size();
        boolean accepting = executor != null && !executor.isShutdown();
        return new Snapshot(accepting, queued, maxQueuedBatches, accepted.get(), applied.get(),
                exactRetries.get(), rejected.get(), failed.get());
    }

    /** Stops admission, drains accepted work, then atomically checkpoints and compacts. */
    public void shutdown() {
        ThreadPoolExecutor current;
        synchronized (this) {
            current = executor;
            if (current == null) return;
            current.shutdown();
        }
        boolean terminated;
        try {
            terminated = current.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while draining Core metric persistence", exception);
        }
        if (!terminated) {
            throw new IllegalStateException("Timed out draining Core metric persistence; accepted batches were not discarded");
        }
        persistence.shutdown();
        synchronized (this) {
            if (executor == current) executor = null;
        }
    }

    private void commit(MetricUpdateBatch batch, CompletableFuture<CommitResult> result) {
        try {
            boolean changed = persistence.commitDurably(batch);
            if (!changed) {
                exactRetries.incrementAndGet();
                result.complete(CommitResult.EXACT_RETRY);
                return;
            }
            applied.incrementAndGet();
            try {
                appliedListener.accept(MetricUpdatedEvent.applied(batch));
            } catch (RuntimeException exception) {
                LOGGER.error("Metric updated listener failed after durable batch {}", batch.eventId(), exception);
            }
            commitsSinceCheckpoint++;
            if (commitsSinceCheckpoint >= checkpointInterval) {
                persistence.checkpointAndCompact();
                commitsSinceCheckpoint = 0;
            }
            result.complete(CommitResult.APPLIED);
        } catch (RuntimeException exception) {
            failed.incrementAndGet();
            result.completeExceptionally(exception);
        }
    }

    private static ThreadFactory threadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "elarion-metric-persistence");
            thread.setDaemon(true);
            return thread;
        };
    }

    public enum CommitResult {
        APPLIED,
        EXACT_RETRY
    }

    public record Snapshot(
            boolean accepting,
            int queuedBatches,
            int maxQueuedBatches,
            long acceptedBatches,
            long appliedBatches,
            long exactRetries,
            long rejectedBatches,
            long failedBatches
    ) {
        public double queueUsage() {
            return queuedBatches / (double) maxQueuedBatches;
        }
    }

    public static final class MetricQueueFullException extends RejectedExecutionException {
        MetricQueueFullException(int maximum, Throwable cause) {
            super("Core metric persistence queue is full (maximum " + maximum + ")", cause);
        }
    }
}
