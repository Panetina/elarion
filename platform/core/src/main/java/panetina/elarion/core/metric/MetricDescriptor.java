package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public record MetricDescriptor(
        Identifier metricId,
        MetricOperation operation,
        MetricSortDirection sortDirection,
        String unit,
        Set<MetricScopeType> legalScopes,
        Set<String> indexedDimensions,
        MetricRetentionPolicy retentionPolicy
) {
    public static final int MAX_INDEXED_DIMENSIONS = 8;
    private static final Pattern DIMENSION = Pattern.compile("[a-z0-9_.-]{1,64}");

    public MetricDescriptor {
        Objects.requireNonNull(metricId, "metricId");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(sortDirection, "sortDirection");
        Objects.requireNonNull(unit, "unit");
        if (unit.isBlank() || unit.length() > 32) throw new IllegalArgumentException("metric unit is invalid");
        legalScopes = Set.copyOf(Objects.requireNonNull(legalScopes, "legalScopes"));
        indexedDimensions = Set.copyOf(Objects.requireNonNull(indexedDimensions, "indexedDimensions"));
        Objects.requireNonNull(retentionPolicy, "retentionPolicy");
        if (legalScopes.isEmpty()) throw new IllegalArgumentException("metric requires at least one legal scope");
        if (indexedDimensions.size() > MAX_INDEXED_DIMENSIONS
                || indexedDimensions.stream().anyMatch(value -> !DIMENSION.matcher(value).matches())) {
            throw new IllegalArgumentException("metric indexed dimensions are invalid or unbounded");
        }
    }
}
