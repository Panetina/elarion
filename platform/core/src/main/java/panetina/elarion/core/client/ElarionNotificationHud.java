package panetina.elarion.core.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ElarionNotificationHud {
    private static final Identifier MAIL_NONEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/mail_nonew.png");
    private static final Identifier MAIL_NEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/mail_new.png");
    private static final Identifier REALM_NONEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/realm_nonew.png");
    private static final Identifier REALM_NEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/realm_new.png");
    private static final int X = 8;
    private static final int PERSONAL_Y = 8;
    private static final int REALM_Y = 44;
    private static final int SOURCE_SIZE = 16;
    private static final int DISPLAY_SIZE = 32;
    private static boolean personalUnread = true;
    private static boolean realmUnread = true;

    private ElarionNotificationHud() {}

    public static void initialize() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> render(drawContext));
    }

    public static boolean handleClick(double mouseX, double mouseY, int button, int action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null
                || client.currentScreen != null && !(client.currentScreen instanceof ChatScreen)) {
            return false;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || action != GLFW.GLFW_PRESS) return false;
        if (inside(mouseX, mouseY, PERSONAL_Y)) {
            personalUnread = false;
            client.inGameHud.getChatHud().addMessage(Text.literal("Personal notifications placeholder works."));
            return true;
        }
        if (inside(mouseX, mouseY, REALM_Y)) {
            realmUnread = false;
            client.inGameHud.getChatHud().addMessage(Text.literal("Realm notifications placeholder works."));
            return true;
        }
        return false;
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        drawIcon(context, PERSONAL_Y, personalUnread ? MAIL_NEW_TEXTURE : MAIL_NONEW_TEXTURE);
        drawIcon(context, REALM_Y, realmUnread ? REALM_NEW_TEXTURE : REALM_NONEW_TEXTURE);
    }

    private static void drawIcon(DrawContext context, int y, Identifier texture) {
        context.getMatrices().push();
        context.getMatrices().scale(2.0F, 2.0F, 1.0F);
        context.drawTexture(
                texture,
                X / 2,
                y / 2,
                0.0F,
                0.0F,
                SOURCE_SIZE,
                SOURCE_SIZE,
                SOURCE_SIZE,
                SOURCE_SIZE
        );
        context.getMatrices().pop();
    }

    private static boolean inside(double mouseX, double mouseY, int y) {
        return mouseX >= X
                && mouseX < X + DISPLAY_SIZE
                && mouseY >= y
                && mouseY < y + DISPLAY_SIZE;
    }
}
