package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.persistence.AnglingCatchTransactionJournal;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.metric.MetricUpdateBatch;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Single-writer transaction boundary for accepted Angling catches.
 *
 * <p>The request record is forced to disk before either Core projection is
 * changed. Projection retries are identified by the same event UUID and source
 * sequence. Delivery must also be idempotent by event UUID. A failure closes
 * admission until restart/recovery so later sequence numbers cannot overtake an
 * incomplete catch.</p>
 */
public final class AnglingCatchCommitCoordinator {
    public static final int DEFAULT_MAX_QUEUED_CATCHES = 4096;
    public static final int DEFAULT_COMPACTION_INTERVAL = 1024;
    public static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);

    private final AnglingCatchCommitFactory factory;
    private final CatchProjection catches;
    private final MetricProjection metrics;
    private final Delivery delivery;
    private final TransactionStore store;
    private final int maxQueuedCatches;
    private final int compactionInterval;
    private final Duration shutdownTimeout;
    private final Map<UUID, AnglingCatchTransactionJournal.Pending> pending = new LinkedHashMap<>();
    private final AtomicLong accepted = new AtomicLong();
    private final AtomicLong completed = new AtomicLong();
    private final AtomicLong recovered = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();

    private ThreadPoolExecutor executor;
    private Path root;
    private Throwable fatalFailure;
    private int completionsSinceCompaction;

    public AnglingCatchCommitCoordinator(ElarionApi api, Delivery delivery) {
        this(new AnglingCatchCommitFactory(),
                event -> api.catchTelemetry().submit(event),
                new MetricProjection() {
                    @Override
                    public long nextSourceSequence(Identifier sourceSystem, String sourcePartition) {
                        return api.metrics().nextSourceSequence(sourceSystem, sourcePartition);
                    }

                    @Override
                    public CompletionStage<?> submit(MetricUpdateBatch batch) {
                        return api.metrics().submit(batch);
                    }
                },
                delivery,
                new JournalStore(new AnglingCatchTransactionJournal()),
                DEFAULT_MAX_QUEUED_CATCHES,
                DEFAULT_COMPACTION_INTERVAL,
                DEFAULT_SHUTDOWN_TIMEOUT);
    }

    AnglingCatchCommitCoordinator(
            AnglingCatchCommitFactory factory,
            CatchProjection catches,
            MetricProjection metrics,
            Delivery delivery,
            TransactionStore store,
            int maxQueuedCatches,
            int compactionInterval,
            Duration shutdownTimeout
    ) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.catches = Objects.requireNonNull(catches, "catches");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
        this.delivery = Objects.requireNonNull(delivery, "delivery");
        this.store = Objects.requireNonNull(store, "store");
        if (maxQueuedCatches < 1 || compactionInterval < 1
                || shutdownTimeout.isZero() || shutdownTimeout.isNegative()) {
            throw new IllegalArgumentException("catch transaction worker bounds must be positive");
        }
        this.maxQueuedCatches = maxQueuedCatches;
        this.compactionInterval = compactionInterval;
        this.shutdownTimeout = shutdownTimeout;
    }

    /** Loads and finishes every incomplete transaction before accepting gameplay. */
    public void bind(Path root) {
        Objects.requireNonNull(root, "root");
        ThreadPoolExecutor current;
        synchronized (this) {
            if (executor != null) throw new IllegalStateException("Angling catch coordinator is already bound");
            this.root = root;
            pending.clear();
            try {
                pending.putAll(store.loadPending(root));
            } catch (IOException | RuntimeException exception) {
                this.root = null;
                throw new IllegalStateException("Failed to load Angling catch transaction journal", exception);
            }
            fatalFailure = null;
            completionsSinceCompaction = 0;
            current = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(maxQueuedCatches), threadFactory(), new ThreadPoolExecutor.AbortPolicy());
            executor = current;
        }

        CompletableFuture<Void> recovery = new CompletableFuture<>();
        current.execute(() -> {
            try {
                for (AnglingCatchTransactionJournal.Pending transaction : ListCopy.of(pending.values())) {
                    process(transaction);
                    recovered.incrementAndGet();
                }
                compact();
                recovery.complete(null);
            } catch (Throwable exception) {
                markFatal(exception);
                recovery.completeExceptionally(exception);
            }
        });
        try {
            recovery.join();
        } catch (CompletionException exception) {
            current.shutdown();
            await(current, "recovering Angling catches");
            synchronized (this) {
                if (executor == current) executor = null;
            }
            throw new IllegalStateException("Failed to recover Angling catch transactions", exception.getCause());
        }
    }

    /** Admits immutable server outcome facts; source sequencing happens only on this worker. */
    public CompletableFuture<AnglingCatchCommit> submit(
            AnglingCatchOutcome outcome,
            AnglingCatchCommitFactory.Facts facts
    ) {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(facts, "facts");
        CompletableFuture<AnglingCatchCommit> result = new CompletableFuture<>();
        ThreadPoolExecutor current;
        synchronized (this) {
            current = executor;
            if (current == null || current.isShutdown() || fatalFailure != null) {
                rejected.incrementAndGet();
                result.completeExceptionally(unavailable());
                return result;
            }
            try {
                current.execute(() -> submitOnWorker(outcome, facts, result));
                accepted.incrementAndGet();
            } catch (RejectedExecutionException exception) {
                rejected.incrementAndGet();
                result.completeExceptionally(new QueueFullException(maxQueuedCatches, exception));
            }
        }
        return result;
    }

    /** Stops admission, drains admitted catches, and atomically compacts incomplete recovery state. */
    public void shutdown() {
        ThreadPoolExecutor current;
        CompletableFuture<Void> compacted = new CompletableFuture<>();
        synchronized (this) {
            current = executor;
            if (current == null) return;
            try {
                current.getQueue().put(() -> {
                    try {
                        compact();
                        compacted.complete(null);
                    } catch (Throwable exception) {
                        compacted.completeExceptionally(exception);
                    }
                });
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while queuing Angling catch shutdown", exception);
            }
            current.shutdown();
        }
        await(current, "draining Angling catch transactions");
        compacted.join();
        synchronized (this) {
            if (executor == current) {
                executor = null;
                root = null;
            }
        }
    }

    public synchronized Snapshot snapshot() {
        return new Snapshot(executor != null && !executor.isShutdown() && fatalFailure == null,
                executor == null ? 0 : executor.getQueue().size(), maxQueuedCatches, pending.size(),
                accepted.get(), completed.get(), recovered.get(), rejected.get(), failed.get(),
                fatalFailure == null ? "" : fatalFailure.getClass().getSimpleName());
    }

    private void submitOnWorker(
            AnglingCatchOutcome outcome,
            AnglingCatchCommitFactory.Facts facts,
            CompletableFuture<AnglingCatchCommit> result
    ) {
        if (fatalFailure != null) {
            result.completeExceptionally(unavailable());
            return;
        }
        try {
            long sequence = metrics.nextSourceSequence(
                    AnglingCatchCommitFactory.SOURCE, AnglingCatchCommitFactory.sourcePartition(facts.actorId()));
            AnglingCatchCommit commit = factory.create(outcome, facts, sequence);
            store.appendRequest(requireRoot(), commit);
            AnglingCatchTransactionJournal.Pending transaction = new AnglingCatchTransactionJournal.Pending(
                    commit, AnglingCatchTransactionJournal.Stage.REQUESTED);
            pending.put(commit.telemetry().eventId(), transaction);
            process(transaction);
            result.complete(commit);
        } catch (Throwable exception) {
            failed.incrementAndGet();
            markFatal(exception);
            result.completeExceptionally(exception);
        }
    }

    private void process(AnglingCatchTransactionJournal.Pending transaction) throws Exception {
        AnglingCatchCommit commit = transaction.commit();
        AnglingCatchTransactionJournal.Stage stage = transaction.stage();
        if (stage == AnglingCatchTransactionJournal.Stage.REQUESTED) {
            join(catches.submit(commit.telemetry()));
            join(metrics.submit(commit.metrics()));
            store.appendStage(requireRoot(), commit.telemetry().eventId(),
                    AnglingCatchTransactionJournal.Stage.PROJECTED);
            stage = AnglingCatchTransactionJournal.Stage.PROJECTED;
            pending.put(commit.telemetry().eventId(), new AnglingCatchTransactionJournal.Pending(commit, stage));
        }
        if (stage == AnglingCatchTransactionJournal.Stage.PROJECTED) {
            join(delivery.deliver(commit));
            store.appendStage(requireRoot(), commit.telemetry().eventId(),
                    AnglingCatchTransactionJournal.Stage.DELIVERED);
            pending.remove(commit.telemetry().eventId());
            completed.incrementAndGet();
            completionsSinceCompaction++;
            if (completionsSinceCompaction >= compactionInterval) compact();
        }
    }

    private void compact() throws IOException {
        store.compact(requireRoot(), Map.copyOf(pending));
        completionsSinceCompaction = 0;
    }

    private Path requireRoot() {
        Path current = root;
        if (current == null) throw new IllegalStateException("Angling catch coordinator is not bound");
        return current;
    }

    private synchronized void markFatal(Throwable exception) {
        if (fatalFailure == null) fatalFailure = exception;
    }

    private synchronized IllegalStateException unavailable() {
        return fatalFailure == null
                ? new IllegalStateException("Angling catch coordinator is not accepting catches")
                : new IllegalStateException("Angling catch coordinator is fail-closed after a transaction failure",
                fatalFailure);
    }

    private static void join(CompletionStage<?> stage) throws Exception {
        try {
            Objects.requireNonNull(stage, "completion stage").toCompletableFuture().join();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception checked) throw checked;
            if (cause instanceof Error error) throw error;
            throw exception;
        }
    }

    private void await(ThreadPoolExecutor current, String operation) {
        try {
            if (!current.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Timed out " + operation + "; admitted catches were not discarded");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while " + operation, exception);
        }
    }

    private static ThreadFactory threadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "elarion-angling-catch-transactions");
            thread.setDaemon(true);
            return thread;
        };
    }

    @FunctionalInterface
    public interface Delivery {
        CompletionStage<Void> deliver(AnglingCatchCommit commit);
    }

    @FunctionalInterface
    interface CatchProjection {
        CompletionStage<?> submit(CatchTelemetryEvent event);
    }

    interface MetricProjection {
        long nextSourceSequence(Identifier sourceSystem, String sourcePartition);

        CompletionStage<?> submit(MetricUpdateBatch batch);
    }

    interface TransactionStore {
        void appendRequest(Path root, AnglingCatchCommit commit) throws IOException;

        void appendStage(Path root, UUID eventId, AnglingCatchTransactionJournal.Stage stage) throws IOException;

        Map<UUID, AnglingCatchTransactionJournal.Pending> loadPending(Path root) throws IOException;

        void compact(Path root, Map<UUID, AnglingCatchTransactionJournal.Pending> pending) throws IOException;
    }

    private record JournalStore(AnglingCatchTransactionJournal journal) implements TransactionStore {
        private JournalStore {
            Objects.requireNonNull(journal, "journal");
        }

        @Override
        public void appendRequest(Path root, AnglingCatchCommit commit) throws IOException {
            journal.appendRequest(root, commit);
        }

        @Override
        public void appendStage(Path root, UUID eventId, AnglingCatchTransactionJournal.Stage stage)
                throws IOException {
            journal.appendStage(root, eventId, stage);
        }

        @Override
        public Map<UUID, AnglingCatchTransactionJournal.Pending> loadPending(Path root) throws IOException {
            return journal.loadPending(root);
        }

        @Override
        public void compact(Path root, Map<UUID, AnglingCatchTransactionJournal.Pending> pending) throws IOException {
            journal.compact(root, pending);
        }
    }

    public record Snapshot(
            boolean accepting,
            int queuedCatches,
            int maxQueuedCatches,
            int pendingTransactions,
            long acceptedCatches,
            long completedCatches,
            long recoveredCatches,
            long rejectedCatches,
            long failedCatches,
            String fatalFailureType
    ) {
    }

    public static final class QueueFullException extends RejectedExecutionException {
        private QueueFullException(int maximum, Throwable cause) {
            super("Angling catch transaction queue is full (maximum " + maximum + ")", cause);
        }
    }

    /** Small immutable list copy without exposing a mutable pending-map iterator to recovery. */
    private static final class ListCopy {
        private ListCopy() {
        }

        private static <T> java.util.List<T> of(java.util.Collection<T> values) {
            return java.util.List.copyOf(values);
        }
    }
}
