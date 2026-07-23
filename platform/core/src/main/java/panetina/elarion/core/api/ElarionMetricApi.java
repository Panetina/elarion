package panetina.elarion.core.api;

import panetina.elarion.core.metric.MetricCursor;
import panetina.elarion.core.metric.MetricDescriptor;
import panetina.elarion.core.metric.MetricPage;
import panetina.elarion.core.metric.MetricProjectionWorker;
import panetina.elarion.core.metric.MetricQuery;
import panetina.elarion.core.metric.MetricRankEntry;
import panetina.elarion.core.metric.MetricUpdateBatch;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.Identifier;

/** Core-owned, bounded metric registration, commit, and ranking contract. */
public final class ElarionMetricApi {
    private final MetricProjectionWorker worker;

    ElarionMetricApi(MetricProjectionWorker worker) {
        this.worker = Objects.requireNonNull(worker, "worker");
    }

    /** Addon bootstrap only. Descriptor registration freezes at the first world bind. */
    public void registerDescriptors(Collection<MetricDescriptor> descriptors) {
        worker.registerDescriptors(descriptors);
    }

    /**
     * Admits an immutable batch to Core's bounded persistence lane. The future
     * completes only after its journal append is durable and projections apply.
     */
    public CompletableFuture<MetricProjectionWorker.CommitResult> submit(MetricUpdateBatch batch) {
        return worker.submit(batch);
    }

    public MetricRankEntry player(MetricQuery query, UUID actorId) {
        return worker.player(query, actorId);
    }

    public MetricPage top(MetricQuery query, int limit) {
        return worker.top(query, limit);
    }

    public MetricPage pageAfter(MetricQuery query, MetricCursor cursor, int limit) {
        return worker.pageAfter(query, cursor, limit);
    }

    public MetricPage around(MetricQuery query, UUID actorId, int radius) {
        return worker.around(query, actorId, radius);
    }

    public long revision(MetricQuery query) {
        return worker.revision(query);
    }

    /** Single-writer source coordinators use this immediately before durably journaling their next batch. */
    public long nextSourceSequence(Identifier sourceSystem, String sourcePartition) {
        return worker.nextSourceSequence(sourceSystem, sourcePartition);
    }

    public MetricProjectionWorker.Snapshot diagnostics() {
        return worker.snapshot();
    }
}
