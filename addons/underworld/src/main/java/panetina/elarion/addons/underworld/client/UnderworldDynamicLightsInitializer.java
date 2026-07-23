package panetina.elarion.addons.underworld.client;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

/** Registers the spectral player state with LambDynamicLights' entity-light engine. */
public final class UnderworldDynamicLightsInitializer implements DynamicLightsInitializer {
    private static final SpectralPlayerLuminance PLAYER_LUMINANCE = new SpectralPlayerLuminance();
    private static final EntityLuminance.Type PLAYER_LUMINANCE_TYPE = EntityLuminance.Type.registerSimple(
            Identifier.of("elarion_underworld", "spectral_player"), PLAYER_LUMINANCE);

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        context.entityLightSourceManager().onRegisterEvent().register(registration ->
                registration.register(EntityType.PLAYER, PLAYER_LUMINANCE));
    }

    @Override
    @SuppressWarnings("removal")
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {
        // The 4.x context entrypoint above owns registration.
    }

    private static final class SpectralPlayerLuminance implements EntityLuminance {
        @Override
        public Type type() {
            return PLAYER_LUMINANCE_TYPE;
        }

        @Override
        public int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
            if (!(entity instanceof AbstractClientPlayerEntity player)) return 0;
            MinecraftClient client = MinecraftClient.getInstance();
            boolean localPlayer = client.player != null && client.player.getUuid().equals(player.getUuid());
            return UnderworldSpectralLight.luminance(
                    localPlayer, UnderworldSoulSight.active(), UnderworldSoulSight.appearance(player),
                    UnderworldBanishmentStatus.isBanished(player.getUuid()));
        }
    }
}
