package panetina.elarion.addons.underworld.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GraveRecoveryScreenLayoutTest {
    @Test
    void layoutKeepsStatusGridAndFooterSeparated() {
        GraveRecoveryScreen.Layout layout = GraveRecoveryScreen.calculateLayout(960, 540);

        assertEquals(480, layout.panelWidth());
        assertEquals(342, layout.panelHeight());
        assertTrue(layout.statusY() > layout.y());
        assertTrue(layout.gridFrameY() > layout.statusY());
        assertTrue(layout.gridFrameY() + layout.gridFrameHeight() < layout.buttonY());
        assertTrue(layout.closeX() > layout.recoverX());
    }

    @Test
    void smallScreensStillReserveAtLeastOneVisibleItemRow() {
        GraveRecoveryScreen.Layout layout = GraveRecoveryScreen.calculateLayout(360, 260);

        assertTrue(layout.panelWidth() >= 350);
        assertTrue(layout.panelHeight() >= 258);
        assertTrue(layout.columns() >= 1);
        assertTrue(layout.visibleRows() >= 1);
        assertTrue(layout.slotY() + 26 <= layout.gridFrameY() + layout.gridFrameHeight());
    }

    @Test
    void itemSlotsKeepTooltipHitboxOnNativeItemArea() {
        GraveRecoveryScreen.Layout layout = GraveRecoveryScreen.calculateLayout(960, 540);

        var slot = GraveRecoveryScreen.itemSlot(layout, 0);

        assertEquals(26, slot.bounds().width());
        assertEquals(26, slot.bounds().height());
        assertEquals(16, slot.item().width());
        assertEquals(16, slot.item().height());
        assertTrue(slot.item().contains(slot.itemDrawX(), slot.itemDrawY()));
        assertTrue(slot.item().contains(slot.itemDrawX() + 15.9, slot.itemDrawY() + 15.9));
        assertTrue(slot.bounds().contains(slot.bounds().x(), slot.bounds().y()));
        assertTrue(slot.bounds().contains(slot.bounds().x() + 25.9, slot.bounds().y() + 25.9));
    }

    @Test
    void laterVisibleSlotsUseGridColumnsAndGap() {
        GraveRecoveryScreen.Layout layout = GraveRecoveryScreen.calculateLayout(960, 540);

        var first = GraveRecoveryScreen.itemSlot(layout, 0);
        var second = GraveRecoveryScreen.itemSlot(layout, 1);
        var nextRow = GraveRecoveryScreen.itemSlot(layout, layout.columns());

        assertEquals(first.bounds().x() + 31, second.bounds().x());
        assertEquals(first.bounds().y(), second.bounds().y());
        assertEquals(first.bounds().x(), nextRow.bounds().x());
        assertEquals(first.bounds().y() + 31, nextRow.bounds().y());
    }
}
