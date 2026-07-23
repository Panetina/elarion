package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Bounded in-memory projection engine. Persistence is a separate release gate;
 * this service must not be exposed to gameplay until its journal/snapshot owner is bound.
 */
public final class MetricProjectionService {
    public static final int MAX_QUERY_LIMIT = 100;
    public static final int MAX_AROUND_RADIUS = 49;

    private final MetricDescriptorRegistry descriptors;
    private final Map<ProjectionKey, MetricRankingIndex> indexes = new HashMap<>();
    private final Map<SourcePartition, PartitionCursor> sourceCursors = new HashMap<>();

    public MetricProjectionService(MetricDescriptorRegistry descriptors) {
        this.descriptors = Objects.requireNonNull(descriptors, "descriptors");
    }

    /** Returns false only for an exact retry of the latest partition sequence/event. */
    public synchronized boolean apply(MetricUpdateBatch batch) {
        PreparedBatch prepared = prepare(batch);
        if (prepared == null) return false;
        for (Map.Entry<Target, Long> entry : prepared.proposed().entrySet()) {
            MetricDescriptor descriptor = descriptors.require(entry.getKey().key().metricId());
            indexes.computeIfAbsent(entry.getKey().key(), ignored -> new MetricRankingIndex(descriptor))
                    .apply(entry.getKey().actorId(), entry.getValue());
        }
        sourceCursors.put(prepared.partition(), prepared.cursor());
        return true;
    }

    /** Full non-mutating validation used before durable append. */
    public synchronized boolean validateBatch(MetricUpdateBatch batch) {
        return prepare(batch) != null;
    }

    private PreparedBatch prepare(MetricUpdateBatch batch) {
        Objects.requireNonNull(batch, "batch");
        SourcePartition partition = new SourcePartition(batch.sourceSystem(), batch.sourcePartition());
        PartitionCursor cursor = sourceCursors.get(partition);
        if (cursor != null && batch.sequence() == cursor.sequence() && batch.eventId().equals(cursor.eventId())) {
            if (batch.equals(cursor.batch())) return null;
            throw new IllegalArgumentException("metric retry reused its sequence/event ID with different content");
        }
        if (cursor != null && batch.sequence() <= cursor.sequence()) {
            throw new IllegalArgumentException("metric source sequence is stale or conflicting");
        }

        Map<Target, Long> proposed = new LinkedHashMap<>();
        for (MetricUpdate update : batch.updates()) {
            MetricDescriptor descriptor = descriptors.require(update.metricId());
            validate(batch, update, descriptor);
            for (MetricScope scope : update.scopes()) {
                ProjectionKey key = ProjectionKey.of(update.metricId(), scope, update.dimensions());
                MetricRankingIndex index = indexes.get(key);
                Target target = new Target(key, batch.actorId());
                Long current = proposed.get(target);
                long next = calculate(descriptor.operation(),
                        current != null ? current : index == null ? null : index.value(batch.actorId()),
                        update.fixedPointValue());
                proposed.put(target, next);
            }
        }

        return new PreparedBatch(partition, new PartitionCursor(batch.sequence(), batch.eventId(), batch), proposed);
    }

    public synchronized MetricRankEntry player(MetricQuery query, UUID actorId) {
        MetricRankingIndex index = index(query);
        return index == null ? null : index.player(Objects.requireNonNull(actorId, "actorId"));
    }

    public synchronized MetricPage top(MetricQuery query, int limit) {
        validateLimit(limit);
        MetricRankingIndex index = index(query);
        return index == null ? new MetricPage(0, List.of(), null) : index.top(limit);
    }

    public synchronized MetricPage pageAfter(MetricQuery query, MetricCursor cursor, int limit) {
        validateLimit(limit);
        Objects.requireNonNull(cursor, "cursor");
        MetricRankingIndex index = index(query);
        if (index == null) throw new IllegalArgumentException("metric projection does not exist");
        return index.after(cursor, limit);
    }

    public synchronized MetricPage around(MetricQuery query, UUID actorId, int radius) {
        if (radius < 0 || radius > MAX_AROUND_RADIUS) {
            throw new IllegalArgumentException("metric around radius must be 0-" + MAX_AROUND_RADIUS);
        }
        MetricRankingIndex index = index(query);
        return index == null ? new MetricPage(0, List.of(), null) : index.around(actorId, radius);
    }

    public synchronized long revision(MetricQuery query) {
        MetricRankingIndex index = index(query);
        return index == null ? 0 : index.revision();
    }

    public synchronized long nextSourceSequence(Identifier sourceSystem, String sourcePartition) {
        SourcePartition partition = new SourcePartition(
                Objects.requireNonNull(sourceSystem, "sourceSystem"),
                Objects.requireNonNull(sourcePartition, "sourcePartition"));
        PartitionCursor cursor = sourceCursors.get(partition);
        return cursor == null ? 1L : Math.addExact(cursor.sequence(), 1L);
    }

    public synchronized MetricProjectionState snapshotState() {
        List<MetricProjectionState.IndexState> savedIndexes = indexes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(MetricProjectionService::compareProjectionKeys))
                .map(entry -> new MetricProjectionState.IndexState(
                        entry.getKey().metricId(), entry.getKey().scope(), entry.getKey().dimensions(),
                        entry.getValue().revision(), entry.getValue().valuesSnapshot()))
                .toList();
        List<MetricProjectionState.PartitionState> savedPartitions = sourceCursors.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(MetricProjectionService::comparePartitions))
                .map(entry -> new MetricProjectionState.PartitionState(
                        entry.getKey().sourceSystem(), entry.getKey().sourcePartition(),
                        entry.getValue().sequence(), entry.getValue().eventId(), entry.getValue().batch()))
                .toList();
        return new MetricProjectionState(
                MetricProjectionState.CURRENT_SCHEMA_VERSION, savedIndexes, savedPartitions);
    }

    /** Validates a complete replacement before mutating any live index. */
    public synchronized void restoreState(MetricProjectionState state) {
        Objects.requireNonNull(state, "state");
        Map<ProjectionKey, MetricRankingIndex> restoredIndexes = new HashMap<>();
        for (MetricProjectionState.IndexState saved : state.indexes()) {
            MetricDescriptor descriptor = descriptors.require(saved.metricId());
            MetricQuery query = new MetricQuery(saved.metricId(), saved.scope(), saved.dimensions());
            validateQuery(query, descriptor);
            ProjectionKey key = ProjectionKey.of(saved.metricId(), saved.scope(), saved.dimensions());
            MetricRankingIndex index = new MetricRankingIndex(descriptor);
            index.restore(saved.actorValues(), saved.revision());
            if (restoredIndexes.putIfAbsent(key, index) != null) {
                throw new IllegalArgumentException("metric projection state contains a duplicate index");
            }
        }
        Map<SourcePartition, PartitionCursor> restoredCursors = new HashMap<>();
        for (MetricProjectionState.PartitionState saved : state.partitions()) {
            MetricUpdateBatch batch = saved.latestBatch();
            for (MetricUpdate update : batch.updates()) {
                validate(batch, update, descriptors.require(update.metricId()));
            }
            SourcePartition partition = new SourcePartition(saved.sourceSystem(), saved.sourcePartition());
            PartitionCursor cursor = new PartitionCursor(saved.sequence(), saved.eventId(), batch);
            if (restoredCursors.putIfAbsent(partition, cursor) != null) {
                throw new IllegalArgumentException("metric projection state contains a duplicate partition");
            }
        }
        indexes.clear();
        indexes.putAll(restoredIndexes);
        sourceCursors.clear();
        sourceCursors.putAll(restoredCursors);
    }

    private MetricRankingIndex index(MetricQuery query) {
        Objects.requireNonNull(query, "query");
        MetricDescriptor descriptor = descriptors.require(query.metricId());
        validateQuery(query, descriptor);
        return indexes.get(ProjectionKey.of(query.metricId(), query.scope(), query.dimensions()));
    }

    private static void validate(MetricUpdateBatch batch, MetricUpdate update, MetricDescriptor descriptor) {
        if (update.operation() != descriptor.operation()) {
            throw new IllegalArgumentException("metric operation does not match descriptor for " + update.metricId());
        }
        if (!descriptor.indexedDimensions().containsAll(update.dimensions().keySet())) {
            throw new IllegalArgumentException("metric update uses an unindexed dimension");
        }
        for (MetricScope scope : update.scopes()) {
            if (!descriptor.legalScopes().contains(scope.type())) {
                throw new IllegalArgumentException("metric update uses an illegal scope");
            }
            if (scope.type() == MetricScopeType.REALM
                    && (batch.realmId() == null || !batch.realmId().equals(scope.id()))) {
                throw new IllegalArgumentException("realm metric scope does not match authoritative batch realm");
            }
        }
    }

    private static void validateQuery(MetricQuery query, MetricDescriptor descriptor) {
        if (!descriptor.legalScopes().contains(query.scope().type())
                || !descriptor.indexedDimensions().containsAll(query.dimensions().keySet())) {
            throw new IllegalArgumentException("metric query uses an illegal scope or unindexed dimension");
        }
    }

    private static long calculate(MetricOperation operation, Long current, long update) {
        return switch (operation) {
            case ADD -> Math.addExact(current == null ? 0 : current, update);
            case MAX -> current == null ? update : Math.max(current, update);
            case MIN -> current == null ? update : Math.min(current, update);
            case SET_ONCE -> current == null ? update : current;
        };
    }

    private static void validateLimit(int limit) {
        if (limit < 1 || limit > MAX_QUERY_LIMIT) {
            throw new IllegalArgumentException("metric query limit must be 1-" + MAX_QUERY_LIMIT);
        }
    }

    private static int compareProjectionKeys(ProjectionKey left, ProjectionKey right) {
        int compared = left.metricId().toString().compareTo(right.metricId().toString());
        if (compared != 0) return compared;
        compared = Integer.compare(left.scope().type().ordinal(), right.scope().type().ordinal());
        if (compared != 0) return compared;
        String leftScope = left.scope().id() == null ? "" : left.scope().id().toString();
        String rightScope = right.scope().id() == null ? "" : right.scope().id().toString();
        if ((compared = leftScope.compareTo(rightScope)) != 0) return compared;
        return canonicalDimensions(left.dimensions()).compareTo(canonicalDimensions(right.dimensions()));
    }

    private static int comparePartitions(SourcePartition left, SourcePartition right) {
        int compared = left.sourceSystem().toString().compareTo(right.sourceSystem().toString());
        return compared != 0 ? compared : left.sourcePartition().compareTo(right.sourcePartition());
    }

    private static String canonicalDimensions(Map<String, Identifier> dimensions) {
        return dimensions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private record SourcePartition(Identifier sourceSystem, String sourcePartition) {
    }

    private record PartitionCursor(long sequence, UUID eventId, MetricUpdateBatch batch) {
    }

    private record Target(ProjectionKey key, UUID actorId) {
    }

    private record PreparedBatch(
            SourcePartition partition,
            PartitionCursor cursor,
            Map<Target, Long> proposed
    ) {
    }

    private record ProjectionKey(Identifier metricId, MetricScope scope, Map<String, Identifier> dimensions) {
        private static ProjectionKey of(
                Identifier metricId,
                MetricScope scope,
                Map<String, Identifier> dimensions
        ) {
            List<Map.Entry<String, Identifier>> sorted = new ArrayList<>(dimensions.entrySet());
            sorted.sort(Map.Entry.comparingByKey(Comparator.naturalOrder()));
            Map<String, Identifier> canonical = new LinkedHashMap<>();
            sorted.forEach(entry -> canonical.put(entry.getKey(), entry.getValue()));
            return new ProjectionKey(metricId, scope, Map.copyOf(canonical));
        }
    }
}
