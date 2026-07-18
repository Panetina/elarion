package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ElarionSemanticRowLayoutTest {
    @Test
    void compactItemPriceRowMatchesTradeCatalogGeometry() {
        ElarionSemanticRowLayout.CompactItemPriceRow row = ElarionSemanticRowLayout.compactItemPriceRow(
                24, 166, 472, 20, 4, 16, 28, 310, 420, 14);

        assertEquals(new ElarionSemanticRowLayout.Rect(24, 166, 472, 20), row.row());
        assertEquals(new ElarionSemanticRowLayout.Rect(28, 168, 16, 16), row.icon());
        assertEquals(52, row.titleX());
        assertEquals(334, row.metaX());
        assertEquals(new ElarionSemanticRowLayout.Rect(444, 169, 14, 14), row.priceIcon());
        assertEquals(462, row.priceValueX());
    }

    @Test
    void iconTooltipBoundsDoNotCoverWholeRow() {
        ElarionSemanticRowLayout.CompactItemPriceRow row = ElarionSemanticRowLayout.compactItemPriceRow(
                24, 166, 472, 20, 4, 16, 28, 310, 420, 14);

        assertTrue(row.row().contains(200, 176));
        assertFalse(row.icon().contains(200, 176));
        assertTrue(row.icon().contains(28, 168));
        assertTrue(row.icon().contains(43.9, 183.9));
        assertFalse(row.icon().contains(44, 184));
    }

    @Test
    void compactItemPriceRowClampsInvalidSizes() {
        ElarionSemanticRowLayout.CompactItemPriceRow row = ElarionSemanticRowLayout.compactItemPriceRow(
                10, 20, -5, 0, -4, 0, -12, -20, -30, 0);

        assertEquals(new ElarionSemanticRowLayout.Rect(10, 20, 1, 1), row.row());
        assertEquals(new ElarionSemanticRowLayout.Rect(10, 20, 1, 1), row.icon());
        assertEquals(10, row.titleX());
        assertEquals(10, row.metaX());
        assertEquals(new ElarionSemanticRowLayout.Rect(10, 20, 1, 1), row.priceIcon());
        assertEquals(15, row.priceValueX());
    }

    @Test
    void compactRecordRowMatchesGovernmentRowGeometry() {
        ElarionSemanticRowLayout.CompactRecordRow row = ElarionSemanticRowLayout.compactRecordRow(
                92, 150, 310, 40, 10, 20, 34, 126);

        assertEquals(new ElarionSemanticRowLayout.Rect(92, 150, 310, 40), row.row());
        assertEquals(new ElarionSemanticRowLayout.Rect(102, 160, 20, 20), row.icon());
        assertEquals(126, row.titleX());
        assertEquals(159, row.titleY());
        assertEquals(174, row.tagY());
        assertEquals(276, row.metricX());
        assertEquals(159, row.metricY());
        assertEquals(174, row.secondaryMetricY());
        assertEquals(395, row.metricRight());
        assertTrue(row.titleMaxWidth() >= 72);
    }

    @Test
    void compactRecordRowKeepsSmallRowsReadable() {
        ElarionSemanticRowLayout.CompactRecordRow row = ElarionSemanticRowLayout.compactRecordRow(
                24, 166, 472, 20, 10, 16, 34, 126);

        assertEquals(new ElarionSemanticRowLayout.Rect(34, 170, 16, 16), row.icon());
        assertEquals(58, row.titleX());
        assertEquals(169, row.titleY());
        assertEquals(176, row.tagY());
        assertEquals(370, row.metricX());
        assertEquals(169, row.metricY());
        assertEquals(176, row.secondaryMetricY());
    }
}
