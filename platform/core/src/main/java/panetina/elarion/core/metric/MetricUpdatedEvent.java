package panetina.elarion.core.metric;

import java.util.Objects;

/** Stable event emitted once after a metric batch is durably applied to current projections. */
public record MetricUpdatedEvent(int schemaVersion, MetricUpdateBatch batch) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public MetricUpdatedEvent {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException("unsupported metric event schema version");
        }
        Objects.requireNonNull(batch, "batch");
    }

    public static MetricUpdatedEvent applied(MetricUpdateBatch batch) {
        return new MetricUpdatedEvent(CURRENT_SCHEMA_VERSION, batch);
    }
}
