package panetina.elarion.core.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.client.ElarionNotificationHud;
import panetina.elarion.core.network.PlayerContextActionRequestPayload;

@Mixin(Mouse.class)
public abstract class MouseNotificationIconMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private double x;
    @Shadow private double y;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void elarion$handleNotificationIconClick(
            long window,
            int button,
            int action,
            int mods,
            CallbackInfo ci
    ) {
        if (window != client.getWindow().getHandle()) return;
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                && client.currentScreen == null && client.options.sneakKey.isPressed()
                && client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof PlayerEntity target
                && client.player != null && !target.getUuid().equals(client.player.getUuid())) {
            ClientPlayNetworking.send(new PlayerContextActionRequestPayload(target.getUuid()));
            ci.cancel();
            return;
        }
        double scaledX = x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double scaledY = y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
        if (ElarionNotificationHud.handleClick(scaledX, scaledY, button, action)) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void elarion$handleNotificationDrawerScroll(
            long window,
            double horizontal,
            double vertical,
            CallbackInfo ci
    ) {
        if (window != client.getWindow().getHandle()) return;
        double scaledX = x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double scaledY = y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
        if (ElarionNotificationHud.handleScroll(scaledX, scaledY, vertical)) {
            ci.cancel();
        }
    }
}
