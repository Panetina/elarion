package panetina.elarion.addons.angling.condition;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.model.AnglingConditionId;

import java.util.Objects;

public final class AnglingBuiltinConditions {
    public static final AnglingConditionId PLACEHOLDER_ALWAYS =
            AnglingConditionId.of("placeholder_condition_001");
    public static final AnglingConditionId FLUID_WATER =
            AnglingConditionId.of("condition/fluid_water");
    public static final AnglingConditionId FLUID_LAVA =
            AnglingConditionId.of("condition/fluid_lava");
    public static final AnglingConditionId DIMENSION_OVERWORLD =
            AnglingConditionId.of("condition/dimension_overworld");
    public static final AnglingConditionId DIMENSION_NETHER =
            AnglingConditionId.of("condition/dimension_nether");
    public static final AnglingConditionId DIMENSION_END =
            AnglingConditionId.of("condition/dimension_end");
    public static final AnglingConditionId WEATHER_CLEAR =
            AnglingConditionId.of("condition/weather_clear");
    public static final AnglingConditionId WEATHER_RAINING =
            AnglingConditionId.of("condition/weather_raining");
    public static final AnglingConditionId WEATHER_THUNDERING =
            AnglingConditionId.of("condition/weather_thundering");
    public static final AnglingConditionId TIME_DAY =
            AnglingConditionId.of("condition/time_day");
    public static final AnglingConditionId TIME_NIGHT =
            AnglingConditionId.of("condition/time_night");
    public static final AnglingConditionId ELEVATION_SURFACE =
            AnglingConditionId.of("condition/elevation_surface");
    public static final AnglingConditionId ELEVATION_UNDERGROUND =
            AnglingConditionId.of("condition/elevation_underground");
    public static final AnglingConditionId ELEVATION_DEEPSLATE =
            AnglingConditionId.of("condition/elevation_deepslate");
    public static final AnglingConditionId BIOME_RIVER =
            AnglingConditionId.of("condition/biome_river");
    public static final AnglingConditionId BAIT_NONE =
            AnglingConditionId.of("condition/bait_none");
    public static final AnglingConditionId BAIT_PRESENT =
            AnglingConditionId.of("condition/bait_present");

    private static final Identifier MINECRAFT_WATER = Identifier.of("minecraft", "water");
    private static final Identifier MINECRAFT_LAVA = Identifier.of("minecraft", "lava");
    private static final Identifier MINECRAFT_OVERWORLD = Identifier.of("minecraft", "overworld");
    private static final Identifier MINECRAFT_NETHER = Identifier.of("minecraft", "the_nether");
    private static final Identifier MINECRAFT_END = Identifier.of("minecraft", "the_end");
    private static final Identifier MINECRAFT_RIVER = Identifier.of("minecraft", "river");
    private static final long DAY_END_EXCLUSIVE = 12_000L;
    private static final int SURFACE_MIN_Y = 50;
    private static final int DEEPSLATE_MAX_Y_EXCLUSIVE = 0;

    private AnglingBuiltinConditions() {
    }

    public static void register(AnglingConditionRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.register(PLACEHOLDER_ALWAYS, (definition, context) -> true);
        registry.register(FLUID_WATER, (definition, context) -> MINECRAFT_WATER.equals(context.fluidId()));
        registry.register(FLUID_LAVA, (definition, context) -> MINECRAFT_LAVA.equals(context.fluidId()));
        registry.register(
                DIMENSION_OVERWORLD,
                (definition, context) -> MINECRAFT_OVERWORLD.equals(context.dimensionId()));
        registry.register(
                DIMENSION_NETHER,
                (definition, context) -> MINECRAFT_NETHER.equals(context.dimensionId()));
        registry.register(
                DIMENSION_END,
                (definition, context) -> MINECRAFT_END.equals(context.dimensionId()));
        registry.register(
                WEATHER_CLEAR,
                (definition, context) -> !context.raining() && !context.thundering());
        registry.register(WEATHER_RAINING, (definition, context) -> context.raining());
        registry.register(WEATHER_THUNDERING, (definition, context) -> context.thundering());
        registry.register(TIME_DAY, (definition, context) -> context.timeOfDay() < DAY_END_EXCLUSIVE);
        registry.register(TIME_NIGHT, (definition, context) -> context.timeOfDay() >= DAY_END_EXCLUSIVE);
        registry.register(ELEVATION_SURFACE, (definition, context) -> context.blockY() >= SURFACE_MIN_Y);
        registry.register(ELEVATION_UNDERGROUND, (definition, context) -> context.blockY() < SURFACE_MIN_Y);
        registry.register(
                ELEVATION_DEEPSLATE,
                (definition, context) -> context.blockY() < DEEPSLATE_MAX_Y_EXCLUSIVE);
        registry.register(BIOME_RIVER, (definition, context) -> MINECRAFT_RIVER.equals(context.biomeId()));
        registry.register(BAIT_NONE, (definition, context) -> context.baitId() == null);
        registry.register(BAIT_PRESENT, (definition, context) -> context.baitId() != null);
    }
}
