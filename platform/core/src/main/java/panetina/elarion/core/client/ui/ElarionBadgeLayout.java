package panetina.elarion.core.client.ui;

public final class ElarionBadgeLayout {
    public static final int HEIGHT = 10;
    private static final int MIN_MAX_WIDTH = 18;
    private static final int MIN_WIDTH = 24;
    private static final int HORIZONTAL_PADDING = 10;
    private static final int TEXT_INSET_X = 5;
    private static final int TEXT_INSET_Y = 1;
    private static final int ACCENT_WIDTH = 2;

    private ElarionBadgeLayout() {
    }

    public static int textMaxWidth(int maxWidth) {
        return Math.max(1, Math.max(MIN_MAX_WIDTH, maxWidth) - HORIZONTAL_PADDING);
    }

    public static Badge badge(int x, int y, int maxWidth, int visibleTextWidth) {
        int safeMax = Math.max(MIN_MAX_WIDTH, maxWidth);
        int width = Math.min(safeMax, Math.max(MIN_WIDTH, Math.max(0, visibleTextWidth) + HORIZONTAL_PADDING));
        return new Badge(
                new ElarionSemanticRowLayout.Rect(x, y, width, HEIGHT),
                new ElarionSemanticRowLayout.Rect(x, y, ACCENT_WIDTH, HEIGHT),
                new ElarionSemanticRowLayout.Rect(x + ACCENT_WIDTH, y, Math.max(0, width - ACCENT_WIDTH), 1),
                x + TEXT_INSET_X,
                y + TEXT_INSET_Y
        );
    }

    public record Badge(
            ElarionSemanticRowLayout.Rect bounds,
            ElarionSemanticRowLayout.Rect accent,
            ElarionSemanticRowLayout.Rect topLine,
            int textX,
            int textY
    ) {
    }
}
