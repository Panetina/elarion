package panetina.elarion.core.client.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class ElarionMoneySummary {
    private static final int DEFAULT_GAP = 8;
    private static final int VALUE_GAP = 4;

    private ElarionMoneySummary() {
    }

    public static Cell cell(String label, long amount, boolean emphasis) {
        return new Cell(label, amount, emphasis);
    }

    public static CellLayout layout(int x, int y, int labelWidth, int iconSize) {
        return layout(x, y, labelWidth, iconSize, DEFAULT_GAP);
    }

    public static CellLayout layout(int x, int y, int labelWidth, int iconSize, int labelIconGap) {
        int safeIconSize = Math.max(1, iconSize);
        int safeGap = Math.max(0, labelIconGap);
        int iconX = x + Math.max(0, labelWidth) + safeGap;
        int valueX = iconX + safeIconSize + VALUE_GAP;
        return new CellLayout(x, iconX, valueX, y, safeIconSize);
    }

    public static void drawCell(
            DrawContext context,
            TextRenderer renderer,
            Cell cell,
            int x,
            int y,
            int iconSize,
            ElarionUiStyle style
    ) {
        if (cell == null) return;
        int labelColor = cell.emphasis() ? style.titleColor() : style.mutedColor();
        int valueColor = cell.emphasis() ? ElarionCivicColors.ACTIVE_GREEN : style.titleColor();
        String label = cell.label();
        CellLayout layout = layout(x, y, ElarionUiTypography.width(renderer, label), iconSize);
        ElarionUiTypography.draw(context, renderer, label, layout.labelX(), y + 4, labelColor, false);
        drawAmount(context, renderer, Long.toString(cell.amount()), layout, valueColor);
    }

    public static void drawAmount(
            DrawContext context,
            TextRenderer renderer,
            String value,
            CellLayout layout,
            int valueColor
    ) {
        if (layout == null) return;
        ElarionUiRenderer.currencyIcon(context, layout.iconX(), layout.y(), layout.iconSize());
        ElarionUiTypography.draw(context, renderer, value == null ? "" : value,
                layout.valueX(), layout.y() + Math.max(1,
                        (layout.iconSize() - ElarionUiTypography.lineHeight()) / 2) + 1,
                valueColor, false);
    }

    public record Cell(String label, long amount, boolean emphasis) {
        public Cell {
            label = label == null ? "" : label;
        }
    }

    public record CellLayout(int labelX, int iconX, int valueX, int y, int iconSize) {
    }
}
