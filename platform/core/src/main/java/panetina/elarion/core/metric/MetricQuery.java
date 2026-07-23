package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Objects;

public record MetricQuery(Identifier metricId, MetricScope scope, Map<String, Identifier> dimensions) {
    public MetricQuery {
        Objects.requireNonNull(metricId, "metricId");
        Objects.requireNonNull(scope, "scope");
        dimensions = Map.copyOf(Objects.requireNonNull(dimensions, "dimensions"));
        if (dimensions.size() > MetricDescriptor.MAX_INDEXED_DIMENSIONS) {
            throw new IllegalArgumentException("metric query dimensions are unbounded");
        }
    }
}
