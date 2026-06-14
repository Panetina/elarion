package panetina.elarion.addons.npcs.client.ui;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.client.ui.ElarionVirtualList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionVirtualListTest {
    @Test
    void clampsAndScrollsWithinBounds() {
        ElarionVirtualList list = new ElarionVirtualList(20, 5, 99);

        assertEquals(15, list.firstVisible());
        assertFalse(list.canScrollDown());
        assertTrue(list.scroll(-5));
        assertEquals(10, list.firstVisible());
        assertEquals(12, list.itemAt(25, 0, 10));
    }

    @Test
    void ignoresRowsOutsideVisibleRange() {
        ElarionVirtualList list = new ElarionVirtualList(3, 2, 0);

        assertEquals(-1, list.itemAt(-1, 0, 10));
        assertEquals(-1, list.itemAt(25, 0, 10));
    }

    @Test
    void selectionAndPagingStayBoundedAndVisible() {
        ElarionVirtualList list = new ElarionVirtualList(12, 4, 0);

        assertTrue(list.moveSelection(7));
        assertEquals(7, list.selectedIndex());
        assertEquals(4, list.firstVisible());
        assertTrue(list.page(1));
        assertEquals(8, list.firstVisible());
        assertTrue(list.moveSelection(99));
        assertEquals(11, list.selectedIndex());
        assertFalse(list.moveSelection(1));
    }
}
