package panetina.elarion.addons.angling.service;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Objects;

public final class AnglingFishDisplayNames {
    public static final String UNKNOWN_CATCH_NAME = "Uncatalogued Catch";

    private static final Map<Identifier, String> NAMES = Map.of(
            Identifier.of("elarion_angling", "placeholder_fish_001"), "Pale Brookling",
            Identifier.of("elarion_angling", "placeholder_fish_002"), "Reedglass Darter",
            Identifier.of("elarion_angling", "placeholder_fish_003"), "Rainthread Minnow",
            Identifier.of("elarion_angling", "placeholder_fish_004"), "Moonwell Pike",
            Identifier.of("elarion_angling", "placeholder_fish_005"), "Cavern Siltfin",
            Identifier.of("elarion_angling", "placeholder_fish_006"), "Stormveil Koi",
            Identifier.of("elarion_angling", "placeholder_fish_007"), "Baitbright Perch");

    private AnglingFishDisplayNames() {
    }

    public static String displayName(Identifier fishDefinitionId) {
        return NAMES.getOrDefault(
                Objects.requireNonNull(fishDefinitionId, "fishDefinitionId"),
                UNKNOWN_CATCH_NAME);
    }
}
