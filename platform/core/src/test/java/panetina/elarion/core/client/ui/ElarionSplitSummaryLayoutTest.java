package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionSplitSummaryLayoutTest {
    @Test
    void splitMatchesNpcBankFeeTotalGeometry() {
        ElarionSplitSummaryLayout.Split split =
                ElarionSplitSummaryLayout.split(24, 216, 452, 223, 254);

        assertRect(split.divider(), 24, 216, 452, 1);
        assertEquals(24, split.leftX());
        assertEquals(223, split.leftY());
        assertEquals(254, split.rightX());
        assertEquals(223, split.rightY());
    }

    @Test
    void splitClampsInvalidWidthAndRightX() {
        ElarionSplitSummaryLayout.Split split =
                ElarionSplitSummaryLayout.split(10, 20, -1, 30, 5);

        assertRect(split.divider(), 10, 20, 1, 1);
        assertEquals(10, split.rightX());
    }

    private static void assertRect(ElarionSemanticRowLayout.Rect actual, int x, int y, int width, int height) {
        assertEquals(x, actual.x());
        assertEquals(y, actual.y());
        assertEquals(width, actual.width());
        assertEquals(height, actual.height());
    }
}
