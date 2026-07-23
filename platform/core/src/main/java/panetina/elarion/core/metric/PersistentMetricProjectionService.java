package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;
import panetina.elarion.core.storage.MetricJournalStorage;
import panetina.elarion.core.storage.MetricProjectionStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Core persistence owner. Durable commits are intentionally synchronous and
 * must be invoked by Core's bounded persistence worker, never a gameplay tick.
 */
public final class PersistentMetricProjectionService {
    private final Map<Identifier, MetricDescriptor> descriptors = new LinkedHashMap<>();
    private final MetricJournalStorage journal;
    private final MetricProjectionStorage snapshots;
    private MetricProjectionService projections;
    private Path elarionRoot;
    private boolean descriptorsFrozen;

    public PersistentMetricProjectionService(MetricJournalStorage journal, MetricProjectionStorage snapshots) {
        this.journal = Objects.requireNonNull(journal, "journal");
        this.snapshots = Objects.requireNonNull(snapshots, "snapshots");
    }

    public synchronized void registerDescriptors(Collection<MetricDescriptor> descriptors) {
        if (descriptorsFrozen) throw new IllegalStateException("metric descriptors are frozen after first bind");
        Objects.requireNonNull(descriptors, "descriptors").forEach(descriptor -> {
            Objects.requireNonNull(descriptor, "descriptor");
            if (this.descriptors.putIfAbsent(descriptor.metricId(), descriptor) != null) {
                throw new IllegalArgumentException("Duplicate metric " + descriptor.metricId());
            }
        });
    }

    /** Startup-only, fail-closed restoration and crash-window replay. */
    public synchronized long bind(Path root) {
        if (projections != null) throw new IllegalStateException("metric persistence is already bound");
        Objects.requireNonNull(root, "root");
        MetricDescriptorRegistry.Builder registry = MetricDescriptorRegistry.builder();
        descriptors.values().forEach(registry::register);
        MetricProjectionService candidate = new MetricProjectionService(registry.build());
        try {
            MetricProjectionState restored = snapshots.load(root);
            candidate.restoreState(restored);
            long recovered = journal.replay(root, restored, candidate);
            if (recovered > 0) snapshots.save(root, candidate.snapshotState());
            projections = candidate;
            elarionRoot = root;
            descriptorsFrozen = true;
            return recovered;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to restore Core metric projections", exception);
        }
    }

    /** Append first, project second. Atomic snapshots are periodic/shutdown work, never per-update. */
    public synchronized boolean commitDurably(MetricUpdateBatch batch) {
        MetricProjectionService service = requireBound();
        if (!service.validateBatch(batch)) return false;
        try {
            journal.append(elarionRoot, batch);
            if (!service.apply(batch)) {
                throw new IllegalStateException("validated metric batch became an exact retry before commit");
            }
            return true;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to commit Core metric batch " + batch.eventId(), exception);
        }
    }

    public synchronized MetricRankEntry player(MetricQuery query, UUID actorId) {
        return requireBound().player(query, actorId);
    }

    public synchronized MetricPage top(MetricQuery query, int limit) {
        return requireBound().top(query, limit);
    }

    public synchronized MetricPage pageAfter(MetricQuery query, MetricCursor cursor, int limit) {
        return requireBound().pageAfter(query, cursor, limit);
    }

    public synchronized MetricPage around(MetricQuery query, UUID actorId, int radius) {
        return requireBound().around(query, actorId, radius);
    }

    public synchronized long revision(MetricQuery query) {
        return requireBound().revision(query);
    }

    public synchronized long nextSourceSequence(Identifier sourceSystem, String sourcePartition) {
        return requireBound().nextSourceSequence(sourceSystem, sourcePartition);
    }

    /** Bounded persistence-worker checkpoint; never call from a gameplay action. */
    public synchronized void saveSnapshot() {
        try {
            snapshots.save(elarionRoot, requireBound().snapshotState());
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to save Core metric projections", exception);
        }
    }

    /**
     * Persistence-worker-only checkpoint. The snapshot is durable before old
     * journal segments are removed, so a crash at any point remains replay-safe.
     */
    public synchronized void checkpointAndCompact() {
        try {
            snapshots.save(elarionRoot, requireBound().snapshotState());
            journal.compact(elarionRoot);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to checkpoint Core metric projections", exception);
        }
    }

    public synchronized void shutdown() {
        if (projections == null) return;
        try {
            snapshots.save(elarionRoot, projections.snapshotState());
            journal.compact(elarionRoot);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to save Core metric projections during shutdown", exception);
        } finally {
            projections = null;
            elarionRoot = null;
        }
    }

    private MetricProjectionService requireBound() {
        if (projections == null) throw new IllegalStateException("metric persistence is not bound");
        return projections;
    }
}
