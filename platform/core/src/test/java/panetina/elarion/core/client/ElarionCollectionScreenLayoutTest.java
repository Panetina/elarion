package panetina.elarion.core.client;

import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionVirtualList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionCollectionScreenLayoutTest {
    @Test
    void topTabsAreAboveAlignedListAndDetails() {
        ElarionCollectionScreen.Layout layout = ElarionCollectionScreen.layoutMetrics();

        assertTrue(ElarionCollectionScreen.Layout.TAB_Y < layout.panelTop());
        assertEquals(12, layout.tabX(0));
        assertEquals(114, layout.tabX(1));
        assertEquals(layout.panelTop(), ElarionCollectionScreen.Layout.PANEL_TOP);
        assertEquals(layout.panelHeight(), layout.detailBottom() - layout.panelTop());
        assertEquals(536, layout.contentHeaderWidth());
    }

    @Test
    void landingRowsUseBalancedPaddingAndHiddenOverflow() {
        ElarionCollectionScreen.Layout layout = ElarionCollectionScreen.layoutMetrics();

        assertEquals(6, layout.visibleRows());
        int used = layout.visibleRows() * ElarionCollectionScreen.ROW_HEIGHT
                + (layout.visibleRows() - 1) * ElarionCollectionScreen.Layout.ROW_GAP
                + layout.rowPadding() * 2;
        assertTrue(Math.abs(layout.rowsHeight() - used) <= 1);
        assertTrue(layout.listWidth() > layout.detailX() / 2);
    }

    @Test
    void hiddenScrollingStillClampsLongerLists() {
        ElarionCollectionScreen.Layout layout = ElarionCollectionScreen.layoutMetrics();
        ElarionVirtualList list = new ElarionVirtualList(16, layout.visibleRows(), 0);

        list.scroll(100);

        assertEquals(16 - layout.visibleRows(), list.firstVisible());
    }

    @Test
    void hiddenScrollingCanMoveAwayFromSelectedRow() {
        ElarionCollectionScreen.Layout layout = ElarionCollectionScreen.layoutMetrics();
        ElarionVirtualList list = new ElarionVirtualList(7, layout.visibleRows(), 0);

        list.select(0);
        list.scroll(1);
        list.update(7, layout.visibleRows(), list.firstVisible());

        assertEquals(1, list.firstVisible());

        list.select(6);
        list.scroll(-1);
        list.update(7, layout.visibleRows(), list.firstVisible());

        assertEquals(0, list.firstVisible());
    }

    @Test
    void collectionClosesOnCollectionKeyButNotDropKey() {
        assertTrue(ElarionCollectionScreen.closesOnKey(GLFW.GLFW_KEY_ESCAPE));
        assertTrue(ElarionCollectionScreen.closesOnKey(GLFW.GLFW_KEY_C));
        assertFalse(ElarionCollectionScreen.closesOnKey(GLFW.GLFW_KEY_Q));
    }
}
