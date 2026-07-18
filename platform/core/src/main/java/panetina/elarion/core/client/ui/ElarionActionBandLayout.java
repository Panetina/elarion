package panetina.elarion.core.client.ui;

public final class ElarionActionBandLayout {
    private static final int PANEL_PAD = 12;
    private static final int CONFIRM_PAD = 10;
    private static final int CONTROL_GAP = 6;
    private static final int GROUP_GAP = 10;
    private static final int STATUS_GAP = 8;

    private ElarionActionBandLayout() {
    }

    public static QuantityConfirmBand quantityConfirmBand(
            int x,
            int y,
            int width,
            int height,
            int labelWidth,
            int buttonSize,
            int valueWidth,
            int maxWidth,
            int confirmWidth,
            int confirmHeight
    ) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeButton = Math.max(1, buttonSize);
        int safeValue = Math.max(1, valueWidth);
        int safeMax = Math.max(1, maxWidth);
        int safeConfirmWidth = Math.max(1, confirmWidth);
        int safeConfirmHeight = Math.max(1, confirmHeight);
        int safeLabelWidth = Math.max(0, labelWidth);

        int controlY = y + Math.max(4, Math.min(10, safeHeight - safeButton - 2));
        int confirmY = y + Math.max(4, Math.min(8, safeHeight - safeConfirmHeight - 2));
        int labelX = x + PANEL_PAD;
        int minusX = labelX + safeLabelWidth + GROUP_GAP;
        int valueX = minusX + safeButton + CONTROL_GAP;
        int plusX = valueX + safeValue + CONTROL_GAP;
        int maxX = plusX + safeButton + GROUP_GAP;
        int confirmX = x + safeWidth - CONFIRM_PAD - safeConfirmWidth;
        int statusX = maxX + safeMax + STATUS_GAP;
        int statusWidth = Math.max(0, confirmX - statusX - STATUS_GAP);
        int dividerY = y + Math.max(safeButton + 12, Math.min(safeHeight - 20, 38));
        int summaryY = Math.min(y + safeHeight - 20, dividerY + 4);

        return new QuantityConfirmBand(
                new Rect(x, y, safeWidth, safeHeight),
                new Rect(minusX, controlY, safeButton, safeButton),
                new Rect(valueX, controlY, safeValue, safeButton),
                new Rect(plusX, controlY, safeButton, safeButton),
                new Rect(maxX, controlY, safeMax, safeButton),
                new Rect(confirmX, confirmY, safeConfirmWidth, safeConfirmHeight),
                labelX,
                controlY,
                statusX,
                controlY + 4,
                statusWidth,
                dividerY,
                summaryY
        );
    }

    public record QuantityConfirmBand(
            Rect panel,
            Rect minus,
            Rect value,
            Rect plus,
            Rect max,
            Rect confirm,
            int labelX,
            int labelY,
            int statusX,
            int statusY,
            int statusWidth,
            int dividerY,
            int summaryY
    ) {
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean contains(double px, double py) {
            return px >= x && py >= y && px < x + width && py < y + height;
        }
    }
}
