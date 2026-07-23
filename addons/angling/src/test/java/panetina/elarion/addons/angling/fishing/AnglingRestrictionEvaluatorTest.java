package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnglingRestrictionEvaluatorTest {
    private static final Identifier FISH = id("fish");
    private static final Identifier PLAINS = Identifier.ofVanilla("plains");

    @Test
    void evaluatesWhitelistsBlacklistsFluidWeatherAndStrictTimeBounds() {
        var context = context(Map.of(), Map.of(), 64, 0.5F, 0.0F, 6_000, () -> 0.0D);
        assertEquals(0, evaluate(new AnglingRestriction.Dimension(
                List.of(Identifier.ofVanilla("overworld")), List.of(), "", ""), context));
        assertEquals(AnglingRestrictionEvaluator.UNAVAILABLE, evaluate(new AnglingRestriction.Biome(
                List.of(PLAINS), List.of(), List.of(PLAINS), List.of(), "", ""), context));
        assertEquals(0, evaluate(new AnglingRestriction.Fluid(List.of(Identifier.ofVanilla("water")), ""), context));
        assertEquals(0, evaluate(new AnglingRestriction.WeatherRule(AnglingRestriction.Weather.RAIN, ""), context));
        assertEquals(AnglingRestrictionEvaluator.UNAVAILABLE, evaluate(new AnglingRestriction.Daytime(
                List.of(new AnglingRestriction.TimeRange(6_000, 12_000)), ""), context));
    }

    @Test
    void correctsBiasMathAndCaughtLimitToUseActualCanonicalCounts() {
        var progress = Map.of(FISH, new AnglingCatchEvaluationContext.SpeciesProgress(2, false));
        var context = context(progress, Map.of(FISH, AnglingRarity.COMMON), 95, 0, 0, 23_900, () -> 0.0D);
        assertEquals(50, evaluate(new AnglingRestriction.ElevationBias(100, 10, 100, ""), context));
        assertEquals(90, evaluate(new AnglingRestriction.DaytimeBias(0, 1_000, 100, ""), context));
        assertEquals(0, evaluate(new AnglingRestriction.CaughtLimit(3, ""), context));
        assertEquals(AnglingRestrictionEvaluator.UNAVAILABLE,
                evaluate(new AnglingRestriction.CaughtLimit(2, ""), context));
    }

    @Test
    void rarityRequirementsUseBoundedSpeciesProjectionsWithoutDoubleCounting() {
        Identifier rare = id("rare");
        Map<Identifier, AnglingCatchEvaluationContext.SpeciesProgress> progress = Map.of(
                FISH, new AnglingCatchEvaluationContext.SpeciesProgress(4, true),
                rare, new AnglingCatchEvaluationContext.SpeciesProgress(1, false));
        Map<Identifier, AnglingRarity> catalogue = Map.of(FISH, AnglingRarity.COMMON, rare, AnglingRarity.RARE);
        var context = context(progress, catalogue, 64, 0, 0, 0, () -> 0.0D);

        assertEquals(0, evaluate(rarity(AnglingRarity.NONE, 5, AnglingRestriction.CountType.TOTAL), context));
        assertEquals(0, evaluate(rarity(AnglingRarity.NONE, 2, AnglingRestriction.CountType.UNIQUE), context));
        assertEquals(0, evaluate(rarity(AnglingRarity.GOLDEN, 1, AnglingRestriction.CountType.UNIQUE), context));
        assertEquals(0, evaluate(rarity(AnglingRarity.NONE, 0, AnglingRestriction.CountType.ALL), context));
        assertEquals(AnglingRestrictionEvaluator.UNAVAILABLE,
                evaluate(rarity(AnglingRarity.GOLDEN, 0, AnglingRestriction.CountType.ALL), context));
    }

    @Test
    void percentageChanceUsesOnlyServerRandomness() {
        assertEquals(0, evaluate(new AnglingRestriction.PercentageChance(0.25F, ""),
                context(Map.of(), Map.of(), 0, 0, 0, 0, () -> 0.25D)));
        assertEquals(AnglingRestrictionEvaluator.UNAVAILABLE,
                evaluate(new AnglingRestriction.PercentageChance(0.25F, ""),
                        context(Map.of(), Map.of(), 0, 0, 0, 0, () -> 0.25001D)));
    }

    private static AnglingRestriction.RarityCount rarity(
            AnglingRarity rarity,
            int count,
            AnglingRestriction.CountType type
    ) {
        return new AnglingRestriction.RarityCount(
                List.of(new AnglingRestriction.RarityRequirement(rarity, count, type)), "");
    }

    private static int evaluate(AnglingRestriction restriction, AnglingCatchEvaluationContext context) {
        return AnglingRestrictionEvaluator.adjustment(restriction, FISH, context);
    }

    private static AnglingCatchEvaluationContext context(
            Map<Identifier, AnglingCatchEvaluationContext.SpeciesProgress> progress,
            Map<Identifier, AnglingRarity> catalogue,
            int y,
            float rain,
            float thunder,
            long daytime,
            java.util.function.DoubleSupplier random
    ) {
        return new AnglingCatchEvaluationContext(
                Identifier.ofVanilla("overworld"), Set.of(), PLAINS, Set.of(), Identifier.ofVanilla("air"),
                Set.of(Identifier.ofVanilla("water")), y, rain, thunder, daytime,
                AnglingRestriction.Season.ALL, Set.of(), progress, catalogue, random);
    }

    private static Identifier id(String path) {
        return Identifier.of("elarion_angling", path);
    }
}
