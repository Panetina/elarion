package panetina.elarion.core.client.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class ElarionListRangeMarker {
    private static final int ARROW_WIDTH = 3;
    private static final int ARROW_GAP = 5;

    private ElarionListRangeMarker() {
    }

    public static Range range(int firstVisible, int visibleCount, int total) {
        int safeTotal = Math.max(0, total);
        if (safeTotal <= 0) return new Range(0, 0, 0, false, false);
        int safeVisible = Math.max(1, visibleCount);
        int safeFirst = Math.max(0, Math.min(firstVisible, Math.max(0, safeTotal - safeVisible)));
        int first = safeFirst + 1;
        int last = Math.min(safeTotal, safeFirst + safeVisible);
        return new Range(first, last, safeTotal, safeFirst > 0, last < safeTotal);
    }

    public static boolean shouldDraw(Range range) {
        return range != null && range.total() > 0 && (range.hasPrevious() || range.hasNext());
    }

    public static String label(Range range) {
        if (range == null || range.total() <= 0) return "";
        return "Rows " + range.first() + "-" + range.last() + " / " + range.total();
    }

    public static void draw(
            DrawContext context,
            TextRenderer renderer,
            int centerX,
            int y,
            Range range,
            int color
    ) {
        if (!shouldDraw(range)) return;
        String label = label(range);
        int labelWidth = ElarionUiTypography.width(renderer, label);
        int totalWidth = labelWidth + (ARROW_WIDTH + ARROW_GAP) * 2;
        int left = centerX - totalWidth / 2;
        drawRangeArrow(context, left, y + 4, false, color);
        ElarionUiTypography.draw(context, renderer, label, left + ARROW_WIDTH + ARROW_GAP, y, color, false);
        drawRangeArrow(context, left + ARROW_WIDTH + ARROW_GAP + labelWidth + ARROW_GAP,
                y + 4, true, color);
    }

    private static void drawRangeArrow(DrawContext context, int x, int y, boolean up, int color) {
        if (up) {
            context.fill(x + 1, y, x + 2, y + 1, color);
            context.fill(x, y + 1, x + 3, y + 2, color);
            return;
        }
        context.fill(x, y, x + 3, y + 1, color);
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
    }

    public record Range(int first, int last, int total, boolean hasPrevious, boolean hasNext) {
    }
}
