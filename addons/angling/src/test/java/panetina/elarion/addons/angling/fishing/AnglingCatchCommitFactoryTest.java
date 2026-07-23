package panetina.elarion.addons.angling.fishing;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;
import panetina.elarion.addons.angling.definition.AnglingCatchOutput;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshotRepository;
import panetina.elarion.addons.angling.definition.AnglingCatchType;
import panetina.elarion.addons.angling.definition.AnglingDifficultyDefinition;
import panetina.elarion.addons.angling.definition.AnglingItemReference;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.definition.AnglingSizeWeightDefinition;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;
import panetina.elarion.addons.angling.metric.AnglingMetricDescriptors;
import panetina.elarion.core.metric.MetricProjectionService;
import panetina.elarion.core.metric.MetricQuery;
import panetina.elarion.core.metric.MetricScope;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCatchCommitFactoryTest {
    private static final Identifier FISH = Identifier.of("elarion_angling", "test_fish");
    private static final Identifier REALM = Identifier.of("elarion", "realm/one");

    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void oneCatchMaterializesOnlyContractedOverallFishAndRarityIndexes() {
        UUID actor = UUID.randomUUID();
        UUID event = UUID.randomUUID();
        AnglingCatchOutcome outcome = new AnglingCatchOutcome(
                definition(), new ItemStack(Items.COD, 2), Optional.empty(),
                420, 1_250, 125, true, true, true, 60, 8);
        AnglingCatchCommit commit = new AnglingCatchCommitFactory().create(outcome,
                new AnglingCatchCommitFactory.Facts(
                        event, 1_780_000_000_000L, actor,
                        Identifier.ofVanilla("overworld"), Identifier.ofVanilla("overworld"),
                        Identifier.ofVanilla("plains"), Identifier.of("elarion_angling", "worm"),
                        Identifier.of("elarion_angling", "elarion_angling_rod"),
                        Identifier.of("elarion_angling", "bobber"),
                        Identifier.of("elarion_angling", "hook"), Identifier.ofVanilla("water"), REALM, null,
                        new AnglingCatchReward.RewardPosition(10.5, 64, -2.5)), 1);

        assertEquals(event, commit.telemetry().eventId());
        assertEquals(2, commit.telemetry().quantity());
        assertEquals(420, commit.telemetry().details().sizeMillimetres());
        assertEquals(17, commit.metrics().updates().size());

        MetricProjectionService projections = new MetricProjectionService(AnglingMetricDescriptors.registry());
        assertTrue(projections.apply(commit.metrics()));
        assertFalse(projections.apply(commit.metrics()));
        assertEquals(2, value(projections, "catch/count", actor, MetricScope.global(), Map.of()));
        assertEquals(2, value(projections, "catch/count", actor, MetricScope.global(), Map.of("fish_id", FISH)));
        assertEquals(2, value(projections, "catch/count", actor, MetricScope.realm(REALM),
                Map.of("rarity_id", Identifier.of("elarion_angling", "common"))));
        assertEquals(420, value(projections, "catch/largest_size_mm", actor, MetricScope.global(), Map.of()));
        assertEquals(125, value(projections, "catch/best_percentile_bps", actor, MetricScope.global(),
                Map.of("fish_id", FISH)));
    }

    private static long value(
            MetricProjectionService service,
            String path,
            UUID actor,
            MetricScope scope,
            Map<String, Identifier> dimensions
    ) {
        return service.player(new MetricQuery(Identifier.of("elarion_angling", path), scope, dimensions), actor)
                .fixedPointValue();
    }

    private static AnglingCatchSnapshot.NativeCatch definition() {
        AnglingCatchDefinition source = new AnglingCatchDefinition(
                1,
                new AnglingCatchOutput(new AnglingItemReference(Identifier.ofVanilla("cod"), 2),
                        Optional.empty(), Optional.empty(), false, Optional.empty(), AnglingCatchType.FISH),
                1, new AnglingSizeWeightDefinition(1, 0, 1, 0, 0), AnglingRarity.COMMON,
                List.of(new AnglingTypedNode(Identifier.of("elarion_angling", "empty"),
                        "{\"type\":\"elarion_angling:empty\"}")),
                new AnglingDifficultyDefinition(1, 1, 0, 0, List.of(), List.of()),
                false, true, Identifier.of("elarion_angling", "texture"));
        return new AnglingCatchSnapshotRepository().compileAndPublish(Map.of(FISH, source))
                .find(FISH).orElseThrow();
    }
}
