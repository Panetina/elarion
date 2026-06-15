package panetina.elarion.core.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.client.ElarionNotificationHud;

@Mixin(InGameHud.class)
public abstract class InGameHudNotificationMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void elarion$renderNotificationsAboveChat(
            DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci
    ) {
        ElarionNotificationHud.renderAboveChat(context);
    }
}
