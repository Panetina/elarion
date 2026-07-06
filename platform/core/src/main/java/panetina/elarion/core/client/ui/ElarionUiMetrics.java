package panetina.elarion.core.client.ui;

public final class ElarionUiMetrics {
    private static final int DEFAULT_ROW_PADDING = 8;
    private static final int DEFAULT_BUTTON_PADDING = 6;

    private ElarionUiMetrics() {
    }

    public static int rowHeight(int baseHeight) {
        return controlHeight(baseHeight, DEFAULT_ROW_PADDING);
    }

    public static int buttonHeight(int baseHeight) {
        return controlHeight(baseHeight, DEFAULT_BUTTON_PADDING);
    }

    public static int themedRowHeight() {
        return rowHeight(ElarionUiThemes.current().rowHeight());
    }

    public static int themedButtonHeight() {
        return buttonHeight(ElarionUiThemes.current().buttonHeight());
    }

    public static int controlHeight(int baseHeight, int verticalPadding) {
        int safeBase = Math.max(1, baseHeight);
        int safePadding = Math.max(0, verticalPadding);
        return Math.max(safeBase, ElarionUiTypography.lineHeight() + safePadding);
    }

    public static int rowsHeight(int rowCount, int rowHeight, int gap) {
        int safeCount = Math.max(0, rowCount);
        if (safeCount == 0) return 0;
        int safeRowHeight = Math.max(1, rowHeight);
        int safeGap = Math.max(0, gap);
        return safeCount * safeRowHeight + (safeCount - 1) * safeGap;
    }

    public static int visibleRows(int availableHeight, int rowHeight, int gap) {
        int safeAvailable = Math.max(0, availableHeight);
        int safeRowHeight = Math.max(1, rowHeight);
        int safeGap = Math.max(0, gap);
        if (safeAvailable < safeRowHeight) return 0;
        return Math.max(1, (safeAvailable + safeGap) / (safeRowHeight + safeGap));
    }
}
