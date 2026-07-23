package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Immutable persistence boundary for current indexes and source cursors. */
public record MetricProjectionState(
        int schemaVersion,
        List<IndexState> indexes,
        List<PartitionState> partitions
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final int MAX_INDEXES = 100_000;
    public static final int MAX_PARTITIONS = 100_000;
    public static final int MAX_ACTORS_PER_INDEX = 1_000_000;

    public MetricProjectionState {
        indexes = List.copyOf(Objects.requireNonNull(indexes, "indexes"));
        partitions = List.copyOf(Objects.requireNonNull(partitions, "partitions"));
        if (schemaVersion != CURRENT_SCHEMA_VERSION || indexes.size() > MAX_INDEXES
                || partitions.size() > MAX_PARTITIONS
                || indexes.stream().anyMatch(Objects::isNull)
                || partitions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Invalid or unbounded metric projection state");
        }
    }

    public static MetricProjectionState empty() {
        return new MetricProjectionState(CURRENT_SCHEMA_VERSION, List.of(), List.of());
    }

    public record IndexState(
            Identifier metricId,
            MetricScope scope,
            Map<String, Identifier> dimensions,
            long revision,
            Map<UUID, Long> actorValues
    ) {
        public IndexState {
            Objects.requireNonNull(metricId, "metricId");
            Objects.requireNonNull(scope, "scope");
            dimensions = Map.copyOf(Objects.requireNonNull(dimensions, "dimensions"));
            actorValues = Map.copyOf(Objects.requireNonNull(actorValues, "actorValues"));
            if (revision < 0 || dimensions.size() > MetricDescriptor.MAX_INDEXED_DIMENSIONS
                    || actorValues.size() > MAX_ACTORS_PER_INDEX
                    || dimensions.entrySet().stream().anyMatch(e -> e.getKey() == null || e.getValue() == null)
                    || actorValues.entrySet().stream().anyMatch(e -> e.getKey() == null || e.getValue() == null)) {
                throw new IllegalArgumentException("Invalid or unbounded metric index state");
            }
        }
    }

    public record PartitionState(
            Identifier sourceSystem,
            String sourcePartition,
            long sequence,
            UUID eventId,
            MetricUpdateBatch latestBatch
    ) {
        public PartitionState {
            Objects.requireNonNull(sourceSystem, "sourceSystem");
            Objects.requireNonNull(sourcePartition, "sourcePartition");
            Objects.requireNonNull(eventId, "eventId");
            Objects.requireNonNull(latestBatch, "latestBatch");
            if (sourcePartition.isBlank() || sourcePartition.length() > MetricUpdateBatch.MAX_PARTITION_LENGTH
                    || sequence <= 0 || !sourceSystem.equals(latestBatch.sourceSystem())
                    || !sourcePartition.equals(latestBatch.sourcePartition())
                    || sequence != latestBatch.sequence() || !eventId.equals(latestBatch.eventId())) {
                throw new IllegalArgumentException("Metric partition cursor conflicts with its latest batch");
            }
        }
    }
}
