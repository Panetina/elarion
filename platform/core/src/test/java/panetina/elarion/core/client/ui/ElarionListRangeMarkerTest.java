package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionListRangeMarkerTest {
    @Test
    void rangeClampsToVisibleBounds() {
        ElarionListRangeMarker.Range range = ElarionListRangeMarker.range(2, 4, 10);

        assertEquals(3, range.first());
        assertEquals(6, range.last());
        assertEquals(10, range.total());
        assertTrue(range.hasPrevious());
        assertTrue(range.hasNext());
        assertEquals("Rows 3-6 / 10", ElarionListRangeMarker.label(range));
    }

    @Test
    void rangeClampsPastEnd() {
        ElarionListRangeMarker.Range range = ElarionListRangeMarker.range(99, 4, 10);

        assertEquals(7, range.first());
        assertEquals(10, range.last());
        assertTrue(range.hasPrevious());
        assertFalse(range.hasNext());
    }

    @Test
    void markerIsHiddenWhenEverythingFits() {
        ElarionListRangeMarker.Range range = ElarionListRangeMarker.range(0, 10, 4);

        assertEquals("Rows 1-4 / 4", ElarionListRangeMarker.label(range));
        assertFalse(ElarionListRangeMarker.shouldDraw(range));
    }

    @Test
    void emptyRangeHasNoLabel() {
        ElarionListRangeMarker.Range range = ElarionListRangeMarker.range(0, 4, 0);

        assertEquals("", ElarionListRangeMarker.label(range));
        assertFalse(ElarionListRangeMarker.shouldDraw(range));
    }
}
