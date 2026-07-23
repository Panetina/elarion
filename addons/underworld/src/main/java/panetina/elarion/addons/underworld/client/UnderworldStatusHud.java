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
    private static final int NORMAL_HEIGHT = 30;
    private static final int BANISHMENT_HEIGHT = 42;

    private UnderworldStatusHud() {
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (client.player == null || client.options.hudHidden) return;
        UnderworldStatusSyncPayload status = UnderworldClientStatus.current();
        if (!status.active()) return;

        TextRenderer renderer = client.textRenderer;
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        int height = status.banished() ? BANISHMENT_HEIGHT : NORMAL_HEIGHT;
        int x = (context.getScaledWindowWidth() - WIDTH) / 2;
        int y = 8;
        context.fill(x, y, x + WIDTH, y + height, status.banished() ? 0xFFA01820 : style.bevelHighlightColor());
        context.fill(x + 1, y + 1, x + WIDTH - 1, y + height - 1, style.cardColor());

        String title = status.banished() ? "Banished To The Underworld" : "Soul Bound To The Underworld";
        String timer = status.banished()
                ? status.banishmentExpiresAt() <= 0L
                    ? "Sentence: permanent"
                    : "Release in " + formatTime(status.remainingMillis())
                : "Return in " + formatTime(status.remainingMillis());
        ElarionUiTypography.draw(context, renderer, title, x + (WIDTH - ElarionUiTypography.width(renderer, title)) / 2, y + 6,
                status.banished() ? 0xFFE75A62 : style.titleColor(), false);
        ElarionUiTypography.draw(context, renderer, timer, x + (WIDTH - ElarionUiTypography.width(renderer, timer)) / 2,
                y + 17, style.textColor(), false);
        if (status.banished()) {
            String reason = renderer.trimToWidth("Reason: " + status.banishmentReason(), WIDTH - 12);
            ElarionUiTypography.draw(context, renderer, reason,
                    x + (WIDTH - ElarionUiTypography.width(renderer, reason)) / 2,
                    y + 28, 0xFFD8A0A4, false);
        }
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
