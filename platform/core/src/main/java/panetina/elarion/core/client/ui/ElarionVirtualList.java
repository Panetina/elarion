package panetina.elarion.core.client.ui;

public final class ElarionVirtualList {
    private int itemCount;
    private int visibleRows;
    private int firstVisible;
    private int selectedIndex = -1;

    public ElarionVirtualList(int itemCount, int visibleRows, int firstVisible) {
        update(itemCount, visibleRows, firstVisible);
        selectedIndex = itemCount == 0 ? -1 : this.firstVisible;
    }

    public void update(int itemCount, int visibleRows, int preferredFirstVisible) {
        this.itemCount = Math.max(0, itemCount);
        this.visibleRows = Math.max(1, visibleRows);
        this.firstVisible = clamp(preferredFirstVisible, 0, maximumFirstVisible());
        selectedIndex = itemCount == 0 ? -1 : clamp(selectedIndex < 0 ? firstVisible : selectedIndex, 0, itemCount - 1);
    }

    public boolean scroll(int rows) {
        int previous = firstVisible;
        firstVisible = clamp(firstVisible + rows, 0, maximumFirstVisible());
        return previous != firstVisible;
    }

    public boolean page(int direction) { return scroll(Integer.signum(direction) * visibleRows); }
    public int firstVisible() { return firstVisible; }
    public int lastVisibleExclusive() { return Math.min(itemCount, firstVisible + visibleRows); }
    public int visibleRows() { return visibleRows; }
    public int maximumFirstVisible() { return Math.max(0, itemCount - visibleRows); }
    public int selectedIndex() { return selectedIndex; }
    public boolean canScrollUp() { return firstVisible > 0; }
    public boolean canScrollDown() { return firstVisible < maximumFirstVisible(); }

    public boolean setFirstVisible(int value) {
        int previous = firstVisible;
        firstVisible = clamp(value, 0, maximumFirstVisible());
        return previous != firstVisible;
    }

    public boolean moveSelection(int rows) {
        if (itemCount == 0) return false;
        int previous = selectedIndex;
        selectedIndex = clamp((selectedIndex < 0 ? 0 : selectedIndex) + rows, 0, itemCount - 1);
        ensureSelectedVisible();
        return previous != selectedIndex;
    }

    public void select(int index) {
        if (index < 0 || index >= itemCount) return;
        selectedIndex = index;
        ensureSelectedVisible();
    }

    public int itemAt(double mouseY, int listY, int rowHeight) {
        if (mouseY < listY) return -1;
        int row = (int) ((mouseY - listY) / Math.max(1, rowHeight));
        int index = firstVisible + row;
        return row >= 0 && row < visibleRows && index < itemCount ? index : -1;
    }

    private void ensureSelectedVisible() {
        if (selectedIndex < 0) return;
        if (selectedIndex < firstVisible) firstVisible = selectedIndex;
        else if (selectedIndex >= firstVisible + visibleRows) firstVisible = selectedIndex - visibleRows + 1;
        firstVisible = clamp(firstVisible, 0, maximumFirstVisible());
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
