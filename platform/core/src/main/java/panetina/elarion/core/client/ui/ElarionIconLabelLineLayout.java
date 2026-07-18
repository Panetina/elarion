package panetina.elarion.core.client.ui;

public final class ElarionIconLabelLineLayout {
    private static final int DEFAULT_LABEL_ICON_GAP = 10;
    private static final int DEFAULT_ICON_VALUE_GAP = 4;
    private static final int DEFAULT_TEXT_Y_OFFSET = 2;

    private ElarionIconLabelLineLayout() {
    }

    public static CompactLine compactCurrency(int x, int y, int labelWidth, int iconSize) {
        return compactCurrency(x, y, labelWidth, iconSize, DEFAULT_LABEL_ICON_GAP, DEFAULT_ICON_VALUE_GAP);
    }

    public static CompactLine compactCurrency(
            int x,
            int y,
            int labelWidth,
            int iconSize,
            int labelIconGap,
            int iconValueGap
    ) {
        int safeIconSize = Math.max(1, iconSize);
        int iconX = x + Math.max(0, labelWidth) + Math.max(0, labelIconGap);
        return new CompactLine(
                x,
                y + DEFAULT_TEXT_Y_OFFSET,
                new ElarionSemanticRowLayout.Rect(iconX, y, safeIconSize, safeIconSize),
                iconX + safeIconSize + Math.max(0, iconValueGap),
                y + DEFAULT_TEXT_Y_OFFSET
        );
    }

    public record CompactLine(
            int labelX,
            int labelY,
            ElarionSemanticRowLayout.Rect icon,
            int valueX,
            int valueY
    ) {
    }
}
