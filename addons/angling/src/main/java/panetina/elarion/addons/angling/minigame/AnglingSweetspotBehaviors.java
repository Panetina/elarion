package panetina.elarion.addons.angling.minigame;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.compile.AnglingIdentifierRegistry;

/** Immutable identity registry; behavior implementations attach in later minigame slices. */
public final class AnglingSweetspotBehaviors {
    private AnglingSweetspotBehaviors() {
    }

    public static AnglingIdentifierRegistry<AnglingSweetspotBehaviorType> create() {
        return AnglingIdentifierRegistry.<AnglingSweetspotBehaviorType>builder()
                .register(id("normal"), AnglingSweetspotBehaviorType.NORMAL)
                .register(id("freeze"), AnglingSweetspotBehaviorType.FREEZE)
                .register(id("treasure"), AnglingSweetspotBehaviorType.TREASURE)
                .register(id("tnt"), AnglingSweetspotBehaviorType.TNT)
                .register(id("aqua"), AnglingSweetspotBehaviorType.AQUA)
                .register(id("leaf"), AnglingSweetspotBehaviorType.LEAF)
                .register(id("deep_ocean"), AnglingSweetspotBehaviorType.DEEP_OCEAN)
                .register(id("cloud"), AnglingSweetspotBehaviorType.CLOUD)
                .register(id("glowing"), AnglingSweetspotBehaviorType.GLOWING)
                .build();
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
