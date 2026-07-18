package panetina.elarion.addons.government.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;

public final class GovernmentScreenChrome {
    private GovernmentScreenChrome() {
    }

    public record Tab(String id, String label, String iconId) {
    }

    public static int tabWidth(int areaWidth, int gap, int count, int index) {
        int safeCount = Math.max(1, count);
        int available = Math.max(safeCount, areaWidth - gap * (safeCount - 1));
        int base = available / safeCount;
        int remainder = available % safeCount;
        return base + (index == safeCount - 1 ? remainder : 0);
    }

    public static int tabX(int areaX, int areaWidth, int gap, int count, int index) {
        int x = areaX;
        for (int i = 0; i < index; i++) {
            x += tabWidth(areaWidth, gap, count, i) + gap;
        }
        return x;
    }

    public static int tabRightEdge(int areaX, int areaWidth) {
        return areaX + areaWidth;
    }

    public static int metadataSegmentWidth(int startX, int endX, int count) {
        return Math.max(1, (endX - startX) / Math.max(1, count));
    }

    public static int metadataSegmentX(int startX, int endX, int count, int index) {
        return startX + metadataSegmentWidth(startX, endX, count) * index;
    }

    public static int metadataSegmentEnd(int startX, int endX, int count, int index) {
        return index == count - 1 ? endX : metadataSegmentX(startX, endX, count, index + 1);
    }

    public static void drawHeaderSegment(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            String iconId,
            String label,
            int textColor,
            boolean divider,
            ElarionUiStyle style
    ) {
        if (divider) {
            context.fill(x, y - 3, x + 1, y + 14, GovernmentUiGlyphs.GOLD_SHADOW);
            context.fill(x + 1, y - 3, x + 2, y + 14, 0x44D19B42);
            x += 10;
            width -= 10;
        }
        GovernmentUiGlyphs.icon(context, x, y - 3, 14, iconId, style);
        String visible = ElarionUiRenderer.ellipsize(renderer, label == null ? "" : label,
                Math.max(1, width - 21));
        ElarionUiTypography.draw(context, renderer, visible, x + 21, y + 1, textColor, false);
    }

    public static void drawColorSegment(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            String colorId,
            String label,
            boolean divider,
            ElarionUiStyle style
    ) {
        if (divider) {
            context.fill(x, y - 3, x + 1, y + 14, GovernmentUiGlyphs.GOLD_SHADOW);
            context.fill(x + 1, y - 3, x + 2, y + 14, 0x44D19B42);
            x += 10;
            width -= 10;
        }
        GovernmentUiGlyphs.icon(context, x, y - 3, 14, colorId, style);
        String visible = ElarionUiRenderer.ellipsize(renderer, label == null ? "" : label,
                Math.max(1, width - 21));
        ElarionUiTypography.draw(context, renderer, visible, x + 21, y + 1, style.textColor(), false);
    }

    public static void drawTab(
            DrawContext context,
            TextRenderer renderer,
            int x,
            int y,
            int width,
            int height,
            Tab tab,
            boolean selected,
            boolean hover,
            boolean enabled,
            ElarionUiStyle style
    ) {
        GovernmentUiGlyphs.rowBox(context, x, y, width, height, selected, hover, !enabled, style);
        GovernmentUiGlyphs.icon(context, x + 9, y + Math.max(3, (height - 16) / 2), 16, tab.iconId(), style);
        int color = !enabled && !selected ? style.mutedColor()
                : selected ? style.titleColor() : style.textColor();
        ElarionUiTypography.draw(context, renderer, ElarionUiRenderer.ellipsize(renderer, tab.label(), Math.max(1, width - 38)),
                x + 34, ElarionCivicUi.centeredTextY(renderer, y, height), color, false);
    }
}
