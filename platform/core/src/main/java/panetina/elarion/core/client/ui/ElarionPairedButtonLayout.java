package panetina.elarion.core.client.ui;

public final class ElarionPairedButtonLayout {
    private ElarionPairedButtonLayout() {
    }

    public static Pair pair(
            int x,
            int y,
            int leftWidth,
            int rightWidth,
            int gap,
            int height
    ) {
        int safeLeftWidth = Math.max(1, leftWidth);
        int safeRightWidth = Math.max(1, rightWidth);
        int safeGap = Math.max(0, gap);
        int safeHeight = Math.max(1, height);
        return new Pair(
                new ElarionSemanticRowLayout.Rect(x, y, safeLeftWidth, safeHeight),
                new ElarionSemanticRowLayout.Rect(x + safeLeftWidth + safeGap, y, safeRightWidth, safeHeight),
                safeGap,
                new ElarionSemanticRowLayout.Rect(x, y,
                        safeLeftWidth + safeGap + safeRightWidth, safeHeight)
        );
    }

    public record Pair(
            ElarionSemanticRowLayout.Rect left,
            ElarionSemanticRowLayout.Rect right,
            int gap,
            ElarionSemanticRowLayout.Rect bounds
    ) {
        public ElarionSemanticRowLayout.Rect button(int index) {
            return index <= 0 ? left : right;
        }
    }
}
