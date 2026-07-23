package panetina.elarion.addons.angling.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;

/** Server-safe particle identities; factories are isolated behind the client entrypoint. */
public final class AnglingParticles {
    public static final SimpleParticleType VALLEY_NOTIFICATION = register("valley_notification");
    public static final SimpleParticleType FISHING_BITING = register("fishing_biting");
    public static final SimpleParticleType FISHING_BITING_LAVA = register("fishing_biting_lava");

    private AnglingParticles() {
    }

    public static void initialize() {
        // Class initialization performs registry bootstrap.
    }

    private static SimpleParticleType register(String path) {
        return Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(ElarionAnglingAddon.MOD_ID, path),
                FabricParticleTypes.simple(true)
        );
    }
}
