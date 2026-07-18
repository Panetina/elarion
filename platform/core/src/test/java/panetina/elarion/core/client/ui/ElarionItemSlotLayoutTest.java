package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ElarionItemSlotLayoutTest {
    @Test
    void squareSlotCentersNativeItemDrawBounds() {
        ElarionItemSlotLayout.Slot slot = ElarionItemSlotLayout.square(40, 70, 18, 1);

        assertEquals(new ElarionSemanticRowLayout.Rect(40, 70, 18, 18), slot.bounds());
        assertEquals(new ElarionSemanticRowLayout.Rect(41, 71, 16, 16), slot.item());
        assertEquals(41, slot.itemDrawX());
        assertEquals(71, slot.itemDrawY());
    }

    @Test
    void gridSlotUsesColumnsGapAndInset() {
        ElarionItemSlotLayout.Slot slot = ElarionItemSlotLayout.gridSlot(100, 50, 5, 3, 18, 3, 1);

        assertEquals(new ElarionSemanticRowLayout.Rect(142, 71, 18, 18), slot.bounds());
        assertEquals(new ElarionSemanticRowLayout.Rect(143, 72, 16, 16), slot.item());
    }

    @Test
    void hoverBoundsAreSlotBounded() {
        ElarionItemSlotLayout.Slot slot = ElarionItemSlotLayout.square(40, 70, 18, 1);

        assertTrue(slot.contains(40, 70));
        assertTrue(slot.contains(57.9, 87.9));
        assertFalse(slot.contains(58, 88));
        assertFalse(slot.contains(39.9, 70));
    }

    @Test
    void invalidInputClampsToSafeGeometry() {
        ElarionItemSlotLayout.Slot slot = ElarionItemSlotLayout.gridSlot(4, 8, -1, 0, 0, -4, 10);

        assertEquals(new ElarionSemanticRowLayout.Rect(4, 8, 1, 1), slot.bounds());
        assertEquals(new ElarionSemanticRowLayout.Rect(4, 8, 1, 1), slot.item());
    }
}
