package panetina.elarion.addons.mounts.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.mounts.entity.ElarionMountEntities;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.network.MountInputPayload;
import panetina.elarion.addons.mounts.network.MountToggleActivePayload;
import panetina.elarion.core.client.ElarionCollectionPreviewRegistry;

public final class ElarionMountsClient implements ClientModInitializer {
    private static KeyBinding dismountKey;
    private static int allowClientDismountTicks;
    private static boolean wasRidingMount;
    private static float lastLookYaw;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ElarionMountEntities.MOUNT, ElarionMountEntityRenderer::new);
        ElarionCollectionPreviewRegistry.register(new ElarionMountCollectionPreviewRenderer());
        dismountKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elarion_mounts.dismount",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.elarion_mounts.mounts"));
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null
                    && client.player.getVehicle() instanceof ElarionMountEntity
                    && client.player.input != null) {
                client.player.input.jumping = false;
                client.player.input.sneaking = false;
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !(client.player.getVehicle() instanceof ElarionMountEntity mount)) {
                ElarionMountCamera.tick(client, null);
                if (allowClientDismountTicks > 0) {
                    allowClientDismountTicks--;
                }
                if (client.player != null && dismountKey != null) {
                    while (dismountKey.wasPressed()) {
                        ClientPlayNetworking.send(MountToggleActivePayload.INSTANCE);
                    }
                }
                wasRidingMount = false;
                return;
            }
            float lookYaw = client.player.getYaw();
            float turnIntent = wasRidingMount ? MathHelper.wrapDegrees(lookYaw - lastLookYaw) : 0.0F;
            lastLookYaw = lookYaw;
            wasRidingMount = true;
            boolean dismount = dismountKey.wasPressed();
            if (dismount) {
                allowClientDismountTicks = 20;
            } else if (allowClientDismountTicks > 0) {
                allowClientDismountTicks--;
            }
            float forward = 0.0F;
            if (client.options.forwardKey.isPressed()) forward += 1.0F;
            if (client.options.backKey.isPressed()) forward -= 1.0F;
            float sideways = 0.0F;
            if (client.options.leftKey.isPressed()) sideways += 1.0F;
            if (client.options.rightKey.isPressed()) sideways -= 1.0F;
            boolean boost = client.options.sprintKey.isPressed();
            MountInputPayload payload = new MountInputPayload(
                    mount.getId(),
                    forward,
                    sideways,
                    lookYaw,
                    turnIntent,
                    client.options.jumpKey.isPressed(),
                    client.options.sneakKey.isPressed(),
                    boost,
                    dismount);
            mount.applyClientPrediction(payload);
            ElarionMountCamera.tick(client, mount, boost && forward > 0.05F);
            ClientPlayNetworking.send(payload);
            client.player.input.jumping = false;
            client.player.input.sneaking = false;
        });
    }

    public static boolean allowClientDismount() {
        return allowClientDismountTicks > 0;
    }
}
