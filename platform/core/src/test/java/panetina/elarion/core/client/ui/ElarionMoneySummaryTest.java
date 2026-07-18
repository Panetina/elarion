package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionMoneySummaryTest {
    @Test
    void layoutKeepsIconAndValueGroupedAfterLabel() {
        ElarionMoneySummary.CellLayout layout = ElarionMoneySummary.layout(36, 314, 48, 14);

        assertEquals(36, layout.labelX());
        assertEquals(92, layout.iconX());
        assertEquals(110, layout.valueX());
        assertEquals(314, layout.y());
        assertEquals(14, layout.iconSize());
    }

    @Test
    void layoutClampsInvalidIconSizeAndGap() {
        ElarionMoneySummary.CellLayout layout = ElarionMoneySummary.layout(10, 20, -5, 0, -1);

        assertEquals(10, layout.iconX());
        assertEquals(15, layout.valueX());
        assertEquals(1, layout.iconSize());
    }

    @Test
    void cellNormalizesNullLabelAndKeepsEmphasis() {
        ElarionMoneySummary.Cell cell = ElarionMoneySummary.cell(null, 25L, true);

        assertEquals("", cell.label());
        assertEquals(25L, cell.amount());
        assertTrue(cell.emphasis());
        assertFalse(ElarionMoneySummary.cell("Tax", 0L, false).emphasis());
    }
}
