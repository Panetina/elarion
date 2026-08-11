package panetina.elarion.core.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ElarionChatChannelClientState;
import panetina.elarion.core.client.ElarionChatRecipientClientState;
import panetina.elarion.core.client.ElarionNotificationHud;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;
import panetina.elarion.core.model.ElarionChatChannel;
import panetina.elarion.core.network.ChatChannelSendPayload;
import panetina.elarion.core.network.ChatRecipientRequestPayload;
import panetina.elarion.core.network.ChatRecipientSnapshotPayload;

@Mixin(ChatScreen.class)
public abstract class ChatScreenNotificationMixin {
    private static final int BUTTON_WIDTH = 116;
    private static final int BUTTON_HEIGHT = 16;
    private static final int ROW_HEIGHT = 15;
    private static final int RECIPIENT_WIDTH = 142;
    private static final int MAX_RECIPIENTS = 8;

    @Shadow protected TextFieldWidget chatField;
    private boolean elarion$menuOpen;

    @Inject(method = "init", at = @At("TAIL"))
    private void elarion$alignComposer(CallbackInfo ci) {
        chatField.setX(4);
        chatField.setWidth(MinecraftClient.getInstance().getWindow().getScaledWidth() - 8);
        if (ElarionChatChannelClientState.selected() == ElarionChatChannel.PRIVATE) {
            ClientPlayNetworking.send(ChatRecipientRequestPayload.INSTANCE);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void elarion$renderComposer(
            DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci
    ) {
        // ChatHud is rendered in the same GUI pass. Give the interactive
        // selector a foreground layer so its expanded choices never disappear
        // behind recent messages.
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 500.0F);
        ElarionNotificationHud.renderOverChatScreen(context);
        MinecraftClient client = MinecraftClient.getInstance();
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        int x = chatField.getX();
        int y = chatField.getY() - 19;
        String current = elarion$selectedLabel();
        ElarionCivicUi.compactActionButton(context, client.textRenderer, x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                current + (elarion$menuOpen ? "  ^" : "  v"),
                elarion$inside(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT), false, true,
                ElarionCivicUi.Tone.PRIMARY, style);
        if (elarion$menuOpen) {
            int rowY = y - ROW_HEIGHT;
            for (ElarionChatChannel channel : ElarionChatChannelClientState.available()) {
                boolean selected = channel == ElarionChatChannelClientState.selected();
                ElarionCivicUi.compactActionButton(context, client.textRenderer, x, rowY,
                        BUTTON_WIDTH, ROW_HEIGHT - 1, ElarionChatChannelClientState.label(channel),
                        elarion$inside(mouseX, mouseY, x, rowY, BUTTON_WIDTH, ROW_HEIGHT - 1),
                        selected, true, selected ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.NORMAL, style);
                rowY -= ROW_HEIGHT;
            }
            if (ElarionChatChannelClientState.selected() == ElarionChatChannel.PRIVATE) {
                elarion$renderRecipients(context, client, style, x + BUTTON_WIDTH + 4, y - ROW_HEIGHT,
                        mouseX, mouseY);
            }
        }
        context.getMatrices().pop();
    }

    private void elarion$renderRecipients(
            DrawContext context, MinecraftClient client, ElarionUiStyle style,
            int x, int y, int mouseX, int mouseY
    ) {
        var recipients = ElarionChatRecipientClientState.recipients();
        if (recipients.isEmpty()) {
            ElarionCivicUi.thinBox(context, x, y, RECIPIENT_WIDTH, ROW_HEIGHT,
                    style.insetColor(), style.borderColor());
            ElarionUiTypography.draw(context, client.textRenderer, "No eligible player online",
                    x + 5, y + 4, style.mutedColor(), false);
            return;
        }
        int shown = 0;
        int rowY = y;
        for (ChatRecipientSnapshotPayload.Entry recipient : recipients) {
            if (shown++ >= MAX_RECIPIENTS) break;
            boolean selected = recipient.id().equals(ElarionChatRecipientClientState.selected());
            String name = ElarionUiRenderer.ellipsize(client.textRenderer, recipient.nickname(), RECIPIENT_WIDTH - 12);
            ElarionCivicUi.compactActionButton(context, client.textRenderer, x, rowY,
                    RECIPIENT_WIDTH, ROW_HEIGHT - 1, name,
                    elarion$inside(mouseX, mouseY, x, rowY, RECIPIENT_WIDTH, ROW_HEIGHT - 1),
                    selected, true, selected ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.NORMAL, style);
            rowY -= ROW_HEIGHT;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void elarion$selectChannel(
            double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir
    ) {
        if (button != 0) return;
        int x = chatField.getX();
        int y = chatField.getY() - 19;
        if (elarion$inside(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            elarion$menuOpen = !elarion$menuOpen;
            if (elarion$menuOpen && ElarionChatChannelClientState.selected() == ElarionChatChannel.PRIVATE) {
                ClientPlayNetworking.send(ChatRecipientRequestPayload.INSTANCE);
            }
            cir.setReturnValue(true);
            return;
        }
        if (!elarion$menuOpen) return;

        int rowY = y - ROW_HEIGHT;
        for (ElarionChatChannel channel : ElarionChatChannelClientState.available()) {
            if (elarion$inside(mouseX, mouseY, x, rowY, BUTTON_WIDTH, ROW_HEIGHT - 1)) {
                ElarionChatChannelClientState.select(channel);
                if (channel == ElarionChatChannel.PRIVATE) {
                    ClientPlayNetworking.send(ChatRecipientRequestPayload.INSTANCE);
                } else {
                    elarion$menuOpen = false;
                }
                cir.setReturnValue(true);
                return;
            }
            rowY -= ROW_HEIGHT;
        }
        if (ElarionChatChannelClientState.selected() == ElarionChatChannel.PRIVATE) {
            int recipientX = x + BUTTON_WIDTH + 4;
            int recipientY = y - ROW_HEIGHT;
            int shown = 0;
            for (ChatRecipientSnapshotPayload.Entry recipient : ElarionChatRecipientClientState.recipients()) {
                if (shown++ >= MAX_RECIPIENTS) break;
                if (elarion$inside(mouseX, mouseY, recipientX, recipientY, RECIPIENT_WIDTH, ROW_HEIGHT - 1)) {
                    ElarionChatRecipientClientState.select(recipient.id());
                    elarion$menuOpen = false;
                    cir.setReturnValue(true);
                    return;
                }
                recipientY -= ROW_HEIGHT;
            }
        }
    }

    @Inject(method = "sendMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void elarion$sendSelected(String message, boolean addToHistory, CallbackInfo ci) {
        if (message.isBlank() || message.startsWith("/")) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ElarionChatChannel channel = ElarionChatChannelClientState.selected();
        java.util.UUID recipient = channel == ElarionChatChannel.PRIVATE
                ? ElarionChatRecipientClientState.selected() : null;
        if (channel == ElarionChatChannel.PRIVATE && recipient == null) {
            ClientPlayNetworking.send(ChatRecipientRequestPayload.INSTANCE);
            elarion$menuOpen = true;
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Choose a PM recipient before sending."), false);
            }
            ci.cancel();
            return;
        }
        if (addToHistory) client.inGameHud.getChatHud().addToMessageHistory(message);
        ClientPlayNetworking.send(new ChatChannelSendPayload(channel, recipient, message));
        client.setScreen(null);
        ci.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void elarion$cycleChannel(int keyCode, int scanCode, int modifiers,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (keyCode != GLFW.GLFW_KEY_TAB || chatField.getText().startsWith("/")) return;
        ElarionChatChannelClientState.cycle((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 ? -1 : 1);
        if (ElarionChatChannelClientState.selected() == ElarionChatChannel.PRIVATE) {
            ClientPlayNetworking.send(ChatRecipientRequestPayload.INSTANCE);
            elarion$menuOpen = true;
        } else {
            elarion$menuOpen = false;
        }
        cir.setReturnValue(true);
    }

    private String elarion$selectedLabel() {
        ElarionChatChannel selected = ElarionChatChannelClientState.selected();
        if (selected != ElarionChatChannel.PRIVATE) return ElarionChatChannelClientState.label(selected);
        java.util.UUID recipientId = ElarionChatRecipientClientState.selected();
        String recipient = ElarionChatRecipientClientState.recipients().stream()
                .filter(entry -> entry.id().equals(recipientId))
                .map(ChatRecipientSnapshotPayload.Entry::nickname)
                .findFirst().orElse("choose player");
        return "PM: " + recipient;
    }

    private static boolean elarion$inside(
            double x, double y, int left, int top, int width, int height
    ) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}
