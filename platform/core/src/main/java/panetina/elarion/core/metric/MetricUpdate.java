package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record MetricUpdate(
        Identifier metricId,
        MetricOperation operation,
        long fixedPointValue,
        Set<MetricScope> scopes,
        Map<String, Identifier> dimensions
) {
    public static final int MAX_SCOPES = 3;

    public MetricUpdate {
        Objects.requireNonNull(metricId, "metricId");
        Objects.requireNonNull(operation, "operation");
        scopes = Set.copyOf(Objects.requireNonNull(scopes, "scopes"));
        if (scopes.isEmpty() || scopes.size() > MAX_SCOPES || scopes.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("metric update scopes are empty or unbounded");
        }
        Map<String, Identifier> copied = new LinkedHashMap<>(Objects.requireNonNull(dimensions, "dimensions"));
        if (copied.size() > MetricDescriptor.MAX_INDEXED_DIMENSIONS
                || copied.entrySet().stream().anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)) {
            throw new IllegalArgumentException("metric update dimensions are invalid or unbounded");
        }
        dimensions = Map.copyOf(copied);
    }
}
