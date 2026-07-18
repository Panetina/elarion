package panetina.elarion.core.client.ui;

public final class ElarionEmptyStateLayout {
    private static final int HORIZONTAL_PADDING = 7;
    private static final int TITLE_TOP_INSET = 7;
    private static final int BODY_TOP_INSET = 9;
    private static final int BODY_BOTTOM_INSET = 16;

    private ElarionEmptyStateLayout() {
    }

    public static EmptyState compact(int x, int y, int width, int height, int lineHeight) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeLineHeight = Math.max(1, lineHeight);
        int bodyY = y + BODY_TOP_INSET + safeLineHeight;
        int bodyHeight = Math.max(1, safeHeight - BODY_BOTTOM_INSET - safeLineHeight);
        return new EmptyState(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, safeHeight),
                x + HORIZONTAL_PADDING,
                y + TITLE_TOP_INSET,
                new ElarionSemanticRowLayout.Rect(
                        x + HORIZONTAL_PADDING,
                        bodyY,
                        Math.max(1, safeWidth - HORIZONTAL_PADDING * 2),
                        bodyHeight
                )
        );
    }

    public record EmptyState(
            ElarionSemanticRowLayout.Rect panel,
            int titleX,
            int titleY,
            ElarionSemanticRowLayout.Rect body
    ) {
    }
}
