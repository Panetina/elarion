package panetina.elarion.core.client.ui;

public final class ElarionStatusLineLayout {
    private ElarionStatusLineLayout() {
    }

    public static SingleLine singleLine(int x, int y, int width, int height, int lineHeight) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int safeLineHeight = Math.max(1, lineHeight);
        int textY = y + Math.max(0, (safeHeight - safeLineHeight) / 2);
        return new SingleLine(
                new ElarionSemanticRowLayout.Rect(x, y, safeWidth, safeHeight),
                x,
                textY,
                safeWidth
        );
    }

    public record SingleLine(
            ElarionSemanticRowLayout.Rect bounds,
            int textX,
            int textY,
            int textMaxWidth
    ) {
    }
}
