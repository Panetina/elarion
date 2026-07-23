package panetina.elarion.addons.angling.metric;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.core.metric.MetricDescriptor;
import panetina.elarion.core.metric.MetricDescriptorRegistry;
import panetina.elarion.core.metric.MetricOperation;
import panetina.elarion.core.metric.MetricRetentionPolicy;
import panetina.elarion.core.metric.MetricScopeType;
import panetina.elarion.core.metric.MetricSortDirection;

import java.util.List;
import java.util.Set;

/** Frozen reusable metric identities registered with Core before world persistence binds. */
public final class AnglingMetricDescriptors {
    private static final Set<MetricScopeType> PERSISTENT_SCOPES = Set.of(
            MetricScopeType.GLOBAL, MetricScopeType.REALM);
    private static final Set<MetricScopeType> TOURNAMENT_SCOPES = Set.of(
            MetricScopeType.GLOBAL, MetricScopeType.REALM, MetricScopeType.EVENT);
    private static final Set<String> FISH = Set.of("fish_id");

    public static final List<MetricDescriptor> ALL = List.of(
            descriptor("catch/count", MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                    PERSISTENT_SCOPES, Set.of("fish_id", "rarity_id"), MetricRetentionPolicy.INDEFINITE),
            descriptor("catch/perfect_count", MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                    PERSISTENT_SCOPES, FISH, MetricRetentionPolicy.INDEFINITE),
            descriptor("catch/golden_count", MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                    PERSISTENT_SCOPES, FISH, MetricRetentionPolicy.INDEFINITE),
            descriptor("catch/treasure_count", MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                    PERSISTENT_SCOPES, FISH, MetricRetentionPolicy.INDEFINITE),
            descriptor("catch/fastest_ticks", MetricOperation.MIN, MetricSortDirection.ASCENDING, "ticks",
                    PERSISTENT_SCOPES, FISH, MetricRetentionPolicy.INDEFINITE),
            descriptor("catch/largest_size_mm", MetricOperation.MAX, MetricSortDirection.DESCENDING, "millimetres",
                    PERSISTENT_SCOPES, FISH, MetricRetentionPolicy.INDEFINITE),
            descriptor("catch/heaviest_weight_g", MetricOperation.MAX, MetricSortDirection.DESCENDING, "grams",
                    PERSISTENT_SCOPES, FISH, MetricRetentionPolicy.INDEFINITE),
            descriptor("catch/best_percentile_bps", MetricOperation.MIN, MetricSortDirection.ASCENDING, "basis_points",
                    PERSISTENT_SCOPES, FISH, MetricRetentionPolicy.INDEFINITE),
            tournament("tournament/entry_count", MetricOperation.ADD, "count"),
            tournament("tournament/completion_count", MetricOperation.ADD, "count"),
            tournament("tournament/win_count", MetricOperation.ADD, "count"),
            tournament("tournament/podium_count", MetricOperation.ADD, "count"),
            tournament("tournament/fish_count", MetricOperation.ADD, "count"),
            tournament("tournament/score_total", MetricOperation.ADD, "score"),
            descriptor("tournament/best_score", MetricOperation.MAX, MetricSortDirection.DESCENDING, "score",
                    TOURNAMENT_SCOPES, Set.of(), MetricRetentionPolicy.COMPLETED_EVENT_TOP_100),
            descriptor("tournament/best_placement", MetricOperation.MIN, MetricSortDirection.ASCENDING, "placement",
                    TOURNAMENT_SCOPES, Set.of(), MetricRetentionPolicy.COMPLETED_EVENT_TOP_100),
            descriptor("milestone/unlocked", MetricOperation.SET_ONCE, MetricSortDirection.ASCENDING, "timestamp_ms",
                    PERSISTENT_SCOPES, Set.of("milestone_id"), MetricRetentionPolicy.INDEFINITE),
            descriptor("milestone/unlocked_count", MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                    PERSISTENT_SCOPES, Set.of(), MetricRetentionPolicy.INDEFINITE)
    );

    private AnglingMetricDescriptors() {
    }

    public static MetricDescriptorRegistry registry() {
        MetricDescriptorRegistry.Builder builder = MetricDescriptorRegistry.builder();
        ALL.forEach(builder::register);
        return builder.build();
    }

    private static MetricDescriptor tournament(String path, MetricOperation operation, String unit) {
        return descriptor(path, operation, MetricSortDirection.DESCENDING, unit,
                TOURNAMENT_SCOPES, Set.of(), MetricRetentionPolicy.COMPLETED_EVENT_TOP_100);
    }

    private static MetricDescriptor descriptor(
            String path,
            MetricOperation operation,
            MetricSortDirection direction,
            String unit,
            Set<MetricScopeType> scopes,
            Set<String> dimensions,
            MetricRetentionPolicy retention
    ) {
        return new MetricDescriptor(id(path), operation, direction, unit, scopes, dimensions, retention);
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
