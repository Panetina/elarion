package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ElarionScrollViewportLayoutTest {
    @Test
    void rowViewportMatchesTradeCatalogGeometry() {
        ElarionScrollViewportLayout.RowViewport viewport =
                ElarionScrollViewportLayout.rows(24, 166, 472, 92, 20, 4, 5, 0);

        assertEquals(new ElarionSemanticRowLayout.Rect(24, 166, 472, 92), viewport.bounds());
        assertEquals(4, viewport.visibleCapacity());
        assertEquals(4, viewport.visibleCount());
        assertEquals(1, viewport.maximumFirstVisible());
        assertEquals(166, viewport.rowY(0));
        assertEquals(238, viewport.rowY(3));
    }

    @Test
    void preferredFirstIsClampedToAvailableRows() {
        ElarionScrollViewportLayout.RowViewport viewport =
                ElarionScrollViewportLayout.rows(24, 166, 472, 92, 20, 4, 5, 20);

        assertEquals(1, viewport.firstVisible());
        assertEquals(4, viewport.visibleCount());
        assertEquals(5, viewport.lastVisibleExclusive());
        assertTrue(viewport.scrollable());
    }

    @Test
    void hitTestIgnoresGapsAndOutsideBounds() {
        ElarionScrollViewportLayout.RowViewport viewport =
                ElarionScrollViewportLayout.rows(24, 166, 472, 92, 20, 4, 5, 1);

        assertEquals(1, viewport.itemIndexAt(24, 166));
        assertEquals(1, viewport.itemIndexAt(495.9, 185.9));
        assertEquals(-1, viewport.itemIndexAt(24, 186));
        assertEquals(2, viewport.itemIndexAt(24, 190));
        assertEquals(-1, viewport.itemIndexAt(500, 190));
    }

    @Test
    void emptyViewportIsSafeAndNotScrollable() {
        ElarionScrollViewportLayout.RowViewport viewport =
                ElarionScrollViewportLayout.rows(10, 20, 0, 0, 0, -2, 0, 5);

        assertEquals(0, viewport.firstVisible());
        assertEquals(0, viewport.visibleCount());
        assertEquals(0, viewport.maximumFirstVisible());
        assertFalse(viewport.scrollable());
        assertEquals(-1, viewport.itemIndexAt(10, 20));
        assertEquals(0, viewport.scrolledFirstVisible(1));
    }
}
