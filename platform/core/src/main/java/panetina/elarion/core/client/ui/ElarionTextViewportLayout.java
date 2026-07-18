package panetina.elarion.core.client.ui;

public final class ElarionTextViewportLayout {
    private ElarionTextViewportLayout() {
    }

    public static TextViewport lines(
            int x,
            int y,
            int width,
            int height,
            int lineHeight,
            int lineCount,
            int preferredFirstLine
    ) {
        int safeLineHeight = Math.max(1, lineHeight);
        int visibleLines = Math.max(1, Math.max(0, height) / safeLineHeight);
        int safeLineCount = Math.max(0, lineCount);
        int maxFirst = Math.max(0, safeLineCount - visibleLines);
        int firstLine = clamp(preferredFirstLine, 0, maxFirst);
        int visibleCount = Math.min(visibleLines, Math.max(0, safeLineCount - firstLine));
        return new TextViewport(
                new ElarionSemanticRowLayout.Rect(x, y, Math.max(1, width), Math.max(1, height)),
                safeLineHeight,
                safeLineCount,
                firstLine,
                visibleLines,
                visibleCount,
                maxFirst
        );
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record TextViewport(
            ElarionSemanticRowLayout.Rect bounds,
            int lineHeight,
            int lineCount,
            int firstLine,
            int visibleLineCapacity,
            int visibleLineCount,
            int maximumFirstLine
    ) {
        public int lineY(int visibleLine) {
            return bounds.y() + Math.max(0, visibleLine) * lineHeight;
        }

        public int lastVisibleExclusive() {
            return Math.min(lineCount, firstLine + visibleLineCount);
        }

        public int absoluteLineForVisible(int visibleLine) {
            int line = firstLine + Math.max(0, visibleLine);
            return line < lineCount ? line : -1;
        }

        public int visibleLineForAbsolute(int absoluteLine) {
            int visible = absoluteLine - firstLine;
            return visible >= 0 && visible < visibleLineCapacity ? visible : -1;
        }

        public int clampedFirstLine(int preferredFirstLine) {
            return clamp(preferredFirstLine, 0, maximumFirstLine);
        }

        public boolean canScrollUp() {
            return firstLine > 0;
        }

        public boolean canScrollDown() {
            return firstLine < maximumFirstLine;
        }
    }
}
