package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ElarionActionBandLayoutTest {
    @Test
    void quantityConfirmBandMatchesCompactTradeGeometry() {
        ElarionActionBandLayout.QuantityConfirmBand band = ElarionActionBandLayout.quantityConfirmBand(
                24, 272, 472, 64, 36, 22, 34, 46, 110, 26);

        assertEquals(new ElarionActionBandLayout.Rect(24, 272, 472, 64), band.panel());
        assertEquals(36, band.labelX());
        assertEquals(282, band.labelY());
        assertEquals(new ElarionActionBandLayout.Rect(82, 282, 22, 22), band.minus());
        assertEquals(new ElarionActionBandLayout.Rect(110, 282, 34, 22), band.value());
        assertEquals(new ElarionActionBandLayout.Rect(150, 282, 22, 22), band.plus());
        assertEquals(new ElarionActionBandLayout.Rect(182, 282, 46, 22), band.max());
        assertEquals(new ElarionActionBandLayout.Rect(376, 280, 110, 26), band.confirm());
        assertEquals(236, band.statusX());
        assertEquals(286, band.statusY());
        assertEquals(132, band.statusWidth());
        assertEquals(314, band.summaryY());
    }

    @Test
    void rectContainsUsesRenderBounds() {
        ElarionActionBandLayout.Rect rect = new ElarionActionBandLayout.Rect(10, 20, 30, 12);

        assertTrue(rect.contains(10, 20));
        assertTrue(rect.contains(39.9, 31.9));
        assertFalse(rect.contains(40, 20));
        assertFalse(rect.contains(10, 32));
        assertFalse(rect.contains(9.9, 20));
    }

    @Test
    void quantityConfirmBandClampsInvalidSizes() {
        ElarionActionBandLayout.QuantityConfirmBand band = ElarionActionBandLayout.quantityConfirmBand(
                0, 0, -20, -10, -1, 0, 0, 0, 0, 0);

        assertEquals(1, band.panel().width());
        assertEquals(1, band.panel().height());
        assertEquals(1, band.minus().width());
        assertEquals(1, band.value().width());
        assertEquals(1, band.max().width());
        assertEquals(1, band.confirm().height());
        assertTrue(band.statusWidth() >= 0);
    }
}
