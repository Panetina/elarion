package panetina.elarion.addons.underworld.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import panetina.elarion.addons.underworld.network.UnderworldStatusSyncPayload;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;

public final class UnderworldStatusHud {
    private static final int WIDTH = 158;
    private static final int HEIGHT = 30;

    private UnderworldStatusHud() {
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (client.player == null || client.options.hudHidden) return;
        UnderworldStatusSyncPayload status = UnderworldClientStatus.current();
        if (!status.active()) return;

        TextRenderer renderer = client.textRenderer;
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        int x = (context.getScaledWindowWidth() - WIDTH) / 2;
        int y = 8;
        context.fill(x, y, x + WIDTH, y + HEIGHT, style.bevelHighlightColor());
        context.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, style.cardColor());

        String title = "Soul Bound To The Underworld";
        String timer = "Return in " + formatTime(status.remainingMillis());
        ElarionUiTypography.draw(context, renderer, title, x + (WIDTH - ElarionUiTypography.width(renderer, title)) / 2, y + 6,
                style.titleColor(), false);
        ElarionUiTypography.draw(context, renderer, timer, x + (WIDTH - ElarionUiTypography.width(renderer, timer)) / 2,
                y + 17, style.textColor(), false);
    }

    private static String formatTime(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;
        if (minutes >= 60L) {
            long hours = minutes / 60L;
            long remainingMinutes = minutes % 60L;
            return hours + "h " + remainingMinutes + "m";
        }
        return minutes + "m " + remainingSeconds + "s";
    }
}
