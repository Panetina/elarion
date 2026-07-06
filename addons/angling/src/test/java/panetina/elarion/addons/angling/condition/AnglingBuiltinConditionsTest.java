package panetina.elarion.addons.angling.condition;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.model.AnglingConditionId;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinition;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingBuiltinConditionsTest {
    @Test
    void registersBoundedElarionOwnedConditionIds() {
        AnglingConditionRegistry registry = registered();

        Set<AnglingConditionId> ids = registry.registeredIds();
        assertTrue(ids.size() < AnglingConditionRegistry.MAX_EVALUATORS);
        assertTrue(ids.contains(AnglingBuiltinConditions.PLACEHOLDER_ALWAYS));
        assertTrue(ids.contains(AnglingBuiltinConditions.FLUID_WATER));
        assertTrue(ids.contains(AnglingBuiltinConditions.DIMENSION_OVERWORLD));
        assertTrue(ids.stream()
                .allMatch(id -> "elarion_angling".equals(id.value().getNamespace())));
    }

    @Test
    void placeholderAlwaysConditionMaintainsExistingPlaceholderCompatibility() {
        AnglingConditionRegistry registry = registered();

        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.PLACEHOLDER_ALWAYS),
                context(Identifier.of("minecraft", "lava"),
                        Identifier.of("minecraft", "the_end"),
                        Identifier.of("minecraft", "plains"),
                        null,
                        10,
                        18_000,
                        true,
                        true)));
    }

    @Test
    void fluidConditionsMatchCurrentFluidOnly() {
        AnglingConditionRegistry registry = registered();

        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.FLUID_WATER),
                contextWithFluid(Identifier.of("minecraft", "water"))));
        assertFalse(registry.matches(
                fish(AnglingBuiltinConditions.FLUID_WATER),
                contextWithFluid(Identifier.of("minecraft", "lava"))));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.FLUID_LAVA),
                contextWithFluid(Identifier.of("minecraft", "lava"))));
    }

    @Test
    void dimensionConditionsMatchCurrentDimensionOnly() {
        AnglingConditionRegistry registry = registered();

        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.DIMENSION_OVERWORLD),
                contextWithDimension(Identifier.of("minecraft", "overworld"))));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.DIMENSION_NETHER),
                contextWithDimension(Identifier.of("minecraft", "the_nether"))));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.DIMENSION_END),
                contextWithDimension(Identifier.of("minecraft", "the_end"))));
        assertFalse(registry.matches(
                fish(AnglingBuiltinConditions.DIMENSION_OVERWORLD),
                contextWithDimension(Identifier.of("minecraft", "the_nether"))));
    }

    @Test
    void weatherConditionsMatchCurrentWeather() {
        AnglingConditionRegistry registry = registered();

        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.WEATHER_CLEAR),
                contextWithWeather(false, false)));
        assertFalse(registry.matches(
                fish(AnglingBuiltinConditions.WEATHER_CLEAR),
                contextWithWeather(true, false)));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.WEATHER_RAINING),
                contextWithWeather(true, false)));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.WEATHER_RAINING),
                contextWithWeather(true, true)));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.WEATHER_THUNDERING),
                contextWithWeather(true, true)));
        assertFalse(registry.matches(
                fish(AnglingBuiltinConditions.WEATHER_THUNDERING),
                contextWithWeather(true, false)));
    }

    @Test
    void timeConditionsSplitOneValidatedMinecraftDay() {
        AnglingConditionRegistry registry = registered();

        assertTrue(registry.matches(fish(AnglingBuiltinConditions.TIME_DAY), contextWithTime(0)));
        assertTrue(registry.matches(fish(AnglingBuiltinConditions.TIME_DAY), contextWithTime(11_999)));
        assertFalse(registry.matches(fish(AnglingBuiltinConditions.TIME_DAY), contextWithTime(12_000)));
        assertTrue(registry.matches(fish(AnglingBuiltinConditions.TIME_NIGHT), contextWithTime(12_000)));
        assertTrue(registry.matches(fish(AnglingBuiltinConditions.TIME_NIGHT), contextWithTime(23_999)));
    }

    @Test
    void elevationConditionsMatchCurrentBlockY() {
        AnglingConditionRegistry registry = registered();

        assertTrue(registry.matches(fish(AnglingBuiltinConditions.ELEVATION_SURFACE), contextWithY(64)));
        assertFalse(registry.matches(fish(AnglingBuiltinConditions.ELEVATION_SURFACE), contextWithY(49)));
        assertTrue(registry.matches(fish(AnglingBuiltinConditions.ELEVATION_UNDERGROUND), contextWithY(49)));
        assertTrue(registry.matches(fish(AnglingBuiltinConditions.ELEVATION_DEEPSLATE), contextWithY(-1)));
        assertFalse(registry.matches(fish(AnglingBuiltinConditions.ELEVATION_DEEPSLATE), contextWithY(0)));
    }

    @Test
    void biomeAndBaitConditionsUseTechnicalContextOnly() {
        AnglingConditionRegistry registry = registered();

        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.BIOME_RIVER),
                contextWithBiome(Identifier.of("minecraft", "river"))));
        assertFalse(registry.matches(
                fish(AnglingBuiltinConditions.BIOME_RIVER),
                contextWithBiome(Identifier.of("minecraft", "plains"))));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.BAIT_NONE),
                contextWithBait(null)));
        assertFalse(registry.matches(
                fish(AnglingBuiltinConditions.BAIT_NONE),
                contextWithBait(Identifier.of("elarion_angling", "placeholder_bait_001"))));
        assertTrue(registry.matches(
                fish(AnglingBuiltinConditions.BAIT_PRESENT),
                contextWithBait(Identifier.of("elarion_angling", "placeholder_bait_001"))));
    }

    private static AnglingConditionRegistry registered() {
        AnglingConditionRegistry registry = new AnglingConditionRegistry();
        AnglingBuiltinConditions.register(registry);
        return registry;
    }

    private static FishDefinition fish(AnglingConditionId... conditions) {
        return new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                1,
                List.of(conditions));
    }

    private static AnglingConditionContext contextWithFluid(Identifier fluidId) {
        return context(
                fluidId,
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                null,
                64,
                6_000,
                false,
                false);
    }

    private static AnglingConditionContext contextWithDimension(Identifier dimensionId) {
        return context(
                Identifier.of("minecraft", "water"),
                dimensionId,
                Identifier.of("minecraft", "river"),
                null,
                64,
                6_000,
                false,
                false);
    }

    private static AnglingConditionContext contextWithWeather(boolean raining, boolean thundering) {
        return context(
                Identifier.of("minecraft", "water"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                null,
                64,
                6_000,
                raining,
                thundering);
    }

    private static AnglingConditionContext contextWithTime(long timeOfDay) {
        return context(
                Identifier.of("minecraft", "water"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                null,
                64,
                timeOfDay,
                false,
                false);
    }

    private static AnglingConditionContext contextWithY(int blockY) {
        return context(
                Identifier.of("minecraft", "water"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                null,
                blockY,
                6_000,
                false,
                false);
    }

    private static AnglingConditionContext contextWithBiome(Identifier biomeId) {
        return context(
                Identifier.of("minecraft", "water"),
                Identifier.of("minecraft", "overworld"),
                biomeId,
                null,
                64,
                6_000,
                false,
                false);
    }

    private static AnglingConditionContext contextWithBait(Identifier baitId) {
        return context(
                Identifier.of("minecraft", "water"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                baitId,
                64,
                6_000,
                false,
                false);
    }

    private static AnglingConditionContext context(
            Identifier fluidId,
            Identifier dimensionId,
            Identifier biomeId,
            Identifier baitId,
            int blockY,
            long timeOfDay,
            boolean raining,
            boolean thundering
    ) {
        return new AnglingConditionContext(
                UUID.randomUUID(),
                Identifier.of("minecraft", "overworld"),
                dimensionId,
                biomeId,
                fluidId,
                baitId,
                blockY,
                timeOfDay,
                raining,
                thundering);
    }
}
