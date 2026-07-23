package panetina.elarion.addons.angling.domainmap;

import java.util.Arrays;

/** Frozen aquarium action identities; behavior remains owned by the aquarium block slice. */
public enum AnglingAquariumInteraction {
    BUILD_CASTLE("build_castle"),
    BUILD_CAVE("build_cave"),
    PLACE_CLAM("place_clam"),
    PLACE_CONCH("place_conch"),
    PLACE_FISH("place_fish"),
    PLACE_FISH_CREATIVE("place_fish_creative"),
    PLACE_GRAVEL("place_gravel"),
    PLACE_KELP("place_kelp"),
    PLACE_RED_SAND("place_red_sand"),
    PLACE_SAND("place_sand"),
    PLACE_SEAGRASS("place_seagrass"),
    PLACE_STONE("place_stone"),
    REMOVE_FISH("remove_fish");

    private final String id;

    AnglingAquariumInteraction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static AnglingAquariumInteraction parse(String id) {
        return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown aquarium interaction " + id));
    }
}
