package panetina.elarion.core.client.ui;

public final class ElarionScrollViewportLayout {
    private ElarionScrollViewportLayout() {
    }

    public static RowViewport rows(
            int x,
            int y,
            int width,
            int availableHeight,
            int rowHeight,
            int rowGap,
            int itemCount,
            int preferredFirst
    ) {
        int safeRowHeight = Math.max(1, rowHeight);
        int safeRowGap = Math.max(0, rowGap);
        int visibleRows = ElarionUiMetrics.visibleRows(availableHeight, safeRowHeight, safeRowGap);
        int safeItemCount = Math.max(0, itemCount);
        int maxFirst = Math.max(0, safeItemCount - visibleRows);
        int first = clamp(preferredFirst, 0, maxFirst);
        int visible = Math.min(visibleRows, Math.max(0, safeItemCount - first));
        int height = ElarionUiMetrics.rowsHeight(visibleRows, safeRowHeight, safeRowGap);
        return new RowViewport(
                new ElarionSemanticRowLayout.Rect(x, y, Math.max(1, width), height),
                safeRowHeight,
                safeRowGap,
                safeItemCount,
                first,
                visibleRows,
                visible,
                maxFirst
        );
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record RowViewport(
            ElarionSemanticRowLayout.Rect bounds,
            int rowHeight,
            int rowGap,
            int itemCount,
            int firstVisible,
            int visibleCapacity,
            int visibleCount,
            int maximumFirstVisible
    ) {
        public int lastVisibleExclusive() {
            return Math.min(itemCount, firstVisible + visibleCount);
        }

        public int rowY(int visibleIndex) {
            return bounds.y() + Math.max(0, visibleIndex) * (rowHeight + rowGap);
        }

        public int itemIndexAt(double mouseX, double mouseY) {
            if (!bounds.contains(mouseX, mouseY)) return -1;
            int row = (int) ((mouseY - bounds.y()) / Math.max(1, rowHeight + rowGap));
            int rowLocalY = (int) (mouseY - rowY(row));
            int index = firstVisible + row;
            return row >= 0 && row < visibleCount && rowLocalY >= 0 && rowLocalY < rowHeight && index < itemCount
                    ? index
                    : -1;
        }

        public int clampedFirstVisible(int preferredFirst) {
            return clamp(preferredFirst, 0, maximumFirstVisible);
        }

        public int scrolledFirstVisible(int direction) {
            return clampedFirstVisible(firstVisible + Integer.signum(direction));
        }

        public boolean scrollable() {
            return maximumFirstVisible > 0;
        }
    }
}
