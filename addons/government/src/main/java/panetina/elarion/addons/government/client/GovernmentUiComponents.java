package panetina.elarion.addons.government.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.core.client.ui.ElarionDetailBodyLayout;
import panetina.elarion.core.client.ui.ElarionDetailCardLayout;
import panetina.elarion.core.client.ui.ElarionSemanticRowLayout;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.model.ElarionUiThemeVariant;

public final class GovernmentUiComponents {
    public static final int VOTE_OPTION_HEIGHT = 29;
    public static final int DETAIL_ICON_SIZE = 48;

    private GovernmentUiComponents() {
    }

    public static void recordRow(
            DrawContext context,
            TextRenderer renderer,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int width,
            int height,
            boolean selected,
            boolean hovered,
            boolean muted,
            String iconId,
            String tag,
            String metric,
            String metricIcon,
            String secondaryMetric,
            String secondaryIcon,
            ElarionUiThemeVariant theme,
            ElarionUiStyle style
    ) {
        GovernmentUiGlyphs.rowBox(context, x, y, width, height, selected, hovered, muted, style);

        int iconSize = height <= 38 ? 16 : 20;
        ElarionSemanticRowLayout.CompactRecordRow layout = ElarionSemanticRowLayout.compactRecordRow(
                x, y, width, height, 10, iconSize, 34, 126);
        GovernmentUiGlyphs.icon(context, layout.icon().x(), layout.icon().y(), layout.icon().width(),
                iconId, style);

        int titleColor = selected || row.selectedByViewer() ? GovernmentUiGlyphs.ACTIVE_GREEN : theme.titleColor();
        ElarionUiTypography.draw(context, renderer,
                ElarionUiRenderer.ellipsize(renderer, row.title(), layout.titleMaxWidth()),
                layout.titleX(), layout.titleY(), titleColor, false);

        String visibleTag = tag == null ? "" : tag.trim();
        if (!visibleTag.isBlank()) {
            GovernmentUiGlyphs.tag(context, renderer, layout.titleX(), layout.tagY(), visibleTag,
                    !muted, Math.min(88, Math.max(34, layout.metricX() - layout.titleX() - 12)), style);
        }

        drawMetric(context, renderer, layout.metricX(), layout.metricY(), layout.metricRight(), metricIcon, metric,
                stateColor(row.state(), theme.textColor()), style);
        if (secondaryMetric != null && !secondaryMetric.isBlank()) {
            drawMetric(context, renderer, layout.metricX(), layout.secondaryMetricY(), layout.metricRight(),
                    secondaryIcon, secondaryMetric, theme.titleColor(), style);
        }
    }

    public static void detailHeader(
            DrawContext context,
            TextRenderer renderer,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int width,
            String iconId,
            String tag,
            String actorLabel,
            ElarionUiThemeVariant theme,
            ElarionUiStyle style
    ) {
        ElarionDetailCardLayout.IdentityHeader layout = ElarionDetailCardLayout.identityHeader(
                x, y, width, DETAIL_ICON_SIZE, 14, 4, 22, 40);
        GovernmentUiGlyphs.iconFrame(context, layout.icon().x(), layout.icon().y(), layout.icon().width(),
                iconId, style);
        ElarionUiTypography.draw(context, renderer,
                ElarionUiRenderer.ellipsize(renderer, row.title(), layout.textWidth()),
                layout.textX(), layout.titleY(), GovernmentUiGlyphs.ACTIVE_GREEN, false);
        String visibleTag = tag == null || tag.isBlank() ? row.state() : tag;
        if (visibleTag != null && !visibleTag.isBlank()) {
            GovernmentUiGlyphs.tag(context, renderer, layout.textX(), layout.tagY(), visibleTag, row.unlocked(), 96, style);
        }
        if (actorLabel != null && !actorLabel.isBlank()) {
            ElarionUiTypography.draw(context, renderer,
                    ElarionUiRenderer.ellipsize(renderer, actorLabel, layout.textWidth()),
                    layout.textX(), layout.subtitleY(), theme.mutedColor(), false);
        }
    }

    public static void sectionTitle(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            String iconId,
            String label,
            ElarionUiThemeVariant theme,
            ElarionUiStyle style
    ) {
        ElarionDetailBodyLayout.SectionTitle layout = ElarionDetailBodyLayout.sectionTitle(x, y, 16, 6, -3);
        GovernmentUiGlyphs.icon(context, layout.icon().x(), layout.icon().y(), layout.icon().width(), iconId, style);
        ElarionUiTypography.draw(context, renderer, label, layout.textX(), layout.textY(), theme.titleColor(), false);
    }

    public static void divider(DrawContext context, int x, int y, int width) {
        context.fill(x, y, x + width, y + 1, 0xAA5B3513);
    }

    public static void bodyText(
            DrawContext context,
            TextRenderer renderer,
            String text,
            int x,
            int y,
            int width,
            int height,
            ElarionUiThemeVariant theme
    ) {
        ElarionDetailBodyLayout.BodyText layout = ElarionDetailBodyLayout.bodyText(x, y, width, height);
        ElarionUiRenderer.wrappedClipped(context, renderer, Text.literal(text == null ? "" : text),
                layout.body().x(), layout.body().y(), layout.body().width(), layout.body().height(),
                theme.textColor(), theme.mutedColor());
    }

    public static void voteOptionRow(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            String label,
            long value,
            long total,
            int color,
            boolean selected,
            boolean selectable,
            ElarionUiStyle style
    ) {
        GovernmentUiGlyphs.progressRow(context, renderer, x, y, width, label, value, total, color,
                selected, selectable, style);
    }

    public static void timerBlock(
            DrawContext context,
            TextRenderer renderer,
            int centerX,
            int y,
            String value,
            ElarionUiThemeVariant theme,
            ElarionUiStyle style
    ) {
        if (value == null || value.isBlank()) return;
        int iconX = centerX - 48;
        GovernmentUiGlyphs.icon(context, iconX, y - 2, 16, "timer", style);
        ElarionUiTypography.draw(context, renderer, "Time Remaining", iconX + 22, y, theme.titleColor(), false);
        ElarionUiTypography.draw(context, renderer, value, centerX - ElarionUiTypography.width(renderer, value) / 2, y + 13,
                GovernmentUiGlyphs.ACTIVE_GREEN, false);
    }

    public static int metricColumnX(int rowX, int rowWidth) {
        return rowX + rowWidth - 126;
    }

    public static boolean voteOptionLayoutFits(int width) {
        int trackWidth = voteTrackWidth(width, true);
        return width >= 150 && trackWidth >= 72;
    }

    public static int voteTrackWidth(int width, boolean selectable) {
        return Math.max(12, width - (selectable ? 38 : 24));
    }

    private static void drawMetric(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int right,
            String iconId,
            String value,
            int color,
            ElarionUiStyle style
    ) {
        if (value == null || value.isBlank()) return;
        String visible = ElarionUiRenderer.ellipsize(renderer, value, Math.max(1, right - x - 18));
        GovernmentUiGlyphs.icon(context, x, y - 2, 12, iconId == null || iconId.isBlank() ? "people" : iconId, style);
        ElarionUiTypography.draw(context, renderer, visible, x + 17, y, color, false);
    }

    private static int stateColor(String state, int fallback) {
        String normalized = state == null ? "" : state.toLowerCase();
        if (normalized.contains("reject") || normalized.contains("refus") || normalized.contains("oppose")) {
            return GovernmentUiGlyphs.REJECT_RED;
        }
        if (normalized.contains("enacted") || normalized.contains("settled") || normalized.contains("active")
                || normalized.contains("voted") || normalized.contains("approve") || normalized.contains("open")) {
            return GovernmentUiGlyphs.ACTIVE_GREEN;
        }
        return fallback;
    }
}
