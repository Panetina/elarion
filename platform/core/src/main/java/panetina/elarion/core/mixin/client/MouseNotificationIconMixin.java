package panetina.elarion.core.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.client.ElarionNotificationHud;

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
        double scaledX = x * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double scaledY = y * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
        if (ElarionNotificationHud.handleClick(scaledX, scaledY, button, action)) {
            ci.cancel();
        }
    }
}
