package panetina.elarion.addons.angling.minigame;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.compile.AnglingTypedCompilerRegistry;

/** Codec dispatch for every modifier currently present in the 148 native catches. */
public final class AnglingNativeModifierCompilers {
    private AnglingNativeModifierCompilers() {
    }

    public static AnglingTypedCompilerRegistry<AnglingNativeModifier> create() {
        return AnglingTypedCompilerRegistry.<AnglingNativeModifier>builder()
                .register(id("burn_on_miss"), AnglingNativeModifier.BurnOnMiss.CODEC)
                .register(id("deep_dark"), AnglingNativeModifier.DeepDark.CODEC)
                .register(id("disable_hit_sounds"), AnglingNativeModifier.DisableHitSounds.CODEC)
                .register(id("disable_miss_sounds"), AnglingNativeModifier.DisableMissSounds.CODEC)
                .register(id("flip_sweetspots_on_miss"), AnglingNativeModifier.FlipSweetspotsOnMiss.CODEC)
                .register(id("freeze_on_miss"), AnglingNativeModifier.FreezeOnMiss.CODEC)
                .register(id("multi_layer_modifier"), AnglingNativeModifier.MultiLayer.CODEC)
                .register(id("pull_down"), AnglingNativeModifier.PullDown.CODEC)
                .register(id("teleport"), AnglingNativeModifier.Teleport.CODEC)
                .build();
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
