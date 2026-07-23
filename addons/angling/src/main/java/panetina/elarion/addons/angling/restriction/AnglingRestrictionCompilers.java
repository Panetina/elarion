package panetina.elarion.addons.angling.restriction;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.compile.AnglingTypedCompilerRegistry;

/** Complete codec dispatch for the 16 frozen catch-restriction registry IDs. */
public final class AnglingRestrictionCompilers {
    private AnglingRestrictionCompilers() {
    }

    public static AnglingTypedCompilerRegistry<AnglingRestriction> create() {
        return AnglingTypedCompilerRegistry.<AnglingRestriction>builder()
                .register(id("empty"), AnglingRestriction.Empty.CODEC)
                .register(id("dimension"), AnglingRestriction.Dimension.CODEC)
                .register(id("biome"), AnglingRestriction.Biome.CODEC)
                .register(id("bait"), AnglingRestriction.Bait.CODEC)
                .register(id("fluid"), AnglingRestriction.Fluid.CODEC)
                .register(id("elevation_restriction"), AnglingRestriction.Elevation.CODEC)
                .register(id("elevation_bias"), AnglingRestriction.ElevationBias.CODEC)
                .register(id("weather_restriction"), AnglingRestriction.WeatherRule.CODEC)
                .register(id("daytime_restriction"), AnglingRestriction.Daytime.CODEC)
                .register(id("daytime_bias"), AnglingRestriction.DaytimeBias.CODEC)
                .register(id("moon_phase"), AnglingRestriction.MoonPhase.CODEC)
                .register(id("season"), AnglingRestriction.SeasonRule.CODEC)
                .register(id("caught_limit"), AnglingRestriction.CaughtLimit.CODEC)
                .register(id("rarity_count"), AnglingRestriction.RarityCount.CODEC)
                .register(id("percentage_chance"), AnglingRestriction.PercentageChance.CODEC)
                .register(id("structure_restriction"), AnglingRestriction.Structure.CODEC)
                .build();
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
