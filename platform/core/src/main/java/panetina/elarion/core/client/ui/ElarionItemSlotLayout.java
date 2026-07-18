package panetina.elarion.core.client.ui;

public final class ElarionItemSlotLayout {
    private ElarionItemSlotLayout() {
    }

    public static Slot square(int x, int y, int size, int itemInset) {
        int safeSize = Math.max(1, size);
        int safeInset = Math.max(0, Math.min(itemInset, safeSize / 2));
        int itemSize = Math.max(1, safeSize - safeInset * 2);
        return new Slot(
                new ElarionSemanticRowLayout.Rect(x, y, safeSize, safeSize),
                new ElarionSemanticRowLayout.Rect(x + safeInset, y + safeInset, itemSize, itemSize)
        );
    }

    public static Slot gridSlot(
            int x,
            int y,
            int index,
            int columns,
            int slotSize,
            int gap,
            int itemInset
    ) {
        int safeIndex = Math.max(0, index);
        int safeColumns = Math.max(1, columns);
        int safeGap = Math.max(0, gap);
        int safeSlotSize = Math.max(1, slotSize);
        int column = safeIndex % safeColumns;
        int row = safeIndex / safeColumns;
        return square(
                x + column * (safeSlotSize + safeGap),
                y + row * (safeSlotSize + safeGap),
                safeSlotSize,
                itemInset
        );
    }

    public record Slot(
            ElarionSemanticRowLayout.Rect bounds,
            ElarionSemanticRowLayout.Rect item
    ) {
        public boolean contains(double mouseX, double mouseY) {
            return bounds.contains(mouseX, mouseY);
        }

        public int itemDrawX() {
            return item.x();
        }

        public int itemDrawY() {
            return item.y();
        }
    }
}
