package panetina.elarion.core.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.client.ElarionNotificationHud;

@Mixin(ChatScreen.class)
public abstract class ChatScreenNotificationMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void elarion$renderNotificationsOverChat(
            DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci
    ) {
        ElarionNotificationHud.renderOverChatScreen(context);
    }
}
