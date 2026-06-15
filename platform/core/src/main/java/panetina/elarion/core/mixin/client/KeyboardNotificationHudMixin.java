package panetina.elarion.core.mixin.client;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.client.ElarionNotificationHud;

@Mixin(Keyboard.class)
public abstract class KeyboardNotificationHudMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void elarion$handleNotificationDrawerKey(
            long window,
            int key,
            int scancode,
            int action,
            int modifiers,
            CallbackInfo ci
    ) {
        if (ElarionNotificationHud.handleKey(key, scancode, action)) {
            ci.cancel();
        }
    }
}
