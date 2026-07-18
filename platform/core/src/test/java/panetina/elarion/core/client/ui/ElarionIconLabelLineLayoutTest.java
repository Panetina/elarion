package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionIconLabelLineLayoutTest {
    @Test
    void compactCurrencyMatchesBankFeeLineGeometry() {
        ElarionIconLabelLineLayout.CompactLine line =
                ElarionIconLabelLineLayout.compactCurrency(24, 223, 18, 12);

        assertEquals(24, line.labelX());
        assertEquals(225, line.labelY());
        assertEquals(new ElarionSemanticRowLayout.Rect(52, 223, 12, 12), line.icon());
        assertEquals(68, line.valueX());
        assertEquals(225, line.valueY());
    }

    @Test
    void compactCurrencySupportsCustomGaps() {
        ElarionIconLabelLineLayout.CompactLine line =
                ElarionIconLabelLineLayout.compactCurrency(10, 20, 40, 14, 6, 3);

        assertEquals(new ElarionSemanticRowLayout.Rect(56, 20, 14, 14), line.icon());
        assertEquals(73, line.valueX());
    }

    @Test
    void compactCurrencyClampsInvalidSizes() {
        ElarionIconLabelLineLayout.CompactLine line =
                ElarionIconLabelLineLayout.compactCurrency(10, 20, -4, 0, -8, -2);

        assertEquals(new ElarionSemanticRowLayout.Rect(10, 20, 1, 1), line.icon());
        assertEquals(11, line.valueX());
    }
}
