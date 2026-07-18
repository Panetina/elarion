package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionPairedButtonLayoutTest {
    @Test
    void pairMatchesNpcBankModeButtonGeometry() {
        ElarionPairedButtonLayout.Pair pair =
                ElarionPairedButtonLayout.pair(14, 90, 226, 236, 10, 24);

        assertRect(pair.left(), 14, 90, 226, 24);
        assertRect(pair.right(), 250, 90, 236, 24);
        assertRect(pair.bounds(), 14, 90, 472, 24);
        assertEquals(10, pair.gap());
        assertTrue(pair.left().contains(14, 90));
        assertFalse(pair.left().contains(250, 90));
        assertTrue(pair.right().contains(250, 90));
    }

    @Test
    void pairMatchesNpcTradeModeButtonGeometry() {
        ElarionPairedButtonLayout.Pair pair =
                ElarionPairedButtonLayout.pair(14, 90, 238, 238, 16, 24);

        assertRect(pair.left(), 14, 90, 238, 24);
        assertRect(pair.right(), 268, 90, 238, 24);
        assertRect(pair.bounds(), 14, 90, 492, 24);
        assertEquals(16, pair.gap());
    }

    @Test
    void pairClampsInvalidSizes() {
        ElarionPairedButtonLayout.Pair pair =
                ElarionPairedButtonLayout.pair(3, 4, -1, 0, -8, -9);

        assertRect(pair.left(), 3, 4, 1, 1);
        assertRect(pair.right(), 4, 4, 1, 1);
        assertRect(pair.bounds(), 3, 4, 2, 1);
        assertEquals(0, pair.gap());
    }

    private static void assertRect(ElarionSemanticRowLayout.Rect actual, int x, int y, int width, int height) {
        assertEquals(x, actual.x());
        assertEquals(y, actual.y());
        assertEquals(width, actual.width());
        assertEquals(height, actual.height());
    }
}
