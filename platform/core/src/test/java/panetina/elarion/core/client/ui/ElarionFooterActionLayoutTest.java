package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionFooterActionLayoutTest {
    @Test
    void actionMatchesNpcBankFooterGeometry() {
        ElarionFooterActionLayout.Action action =
                ElarionFooterActionLayout.action(14, 264, 156, 22);

        assertRect(action.button(), 14, 264, 156, 22);
        assertTrue(action.button().contains(14, 264));
        assertFalse(action.button().contains(170, 264));
    }

    @Test
    void actionMatchesNpcTradeFooterGeometry() {
        ElarionFooterActionLayout.Action action =
                ElarionFooterActionLayout.action(14, 356, 170, 22);

        assertRect(action.button(), 14, 356, 170, 22);
        assertTrue(action.button().contains(183, 377));
        assertFalse(action.button().contains(184, 377));
    }

    @Test
    void actionClampsInvalidSize() {
        ElarionFooterActionLayout.Action action =
                ElarionFooterActionLayout.action(3, 4, -1, 0);

        assertRect(action.button(), 3, 4, 1, 1);
    }

    private static void assertRect(ElarionSemanticRowLayout.Rect actual, int x, int y, int width, int height) {
        assertEquals(x, actual.x());
        assertEquals(y, actual.y());
        assertEquals(width, actual.width());
        assertEquals(height, actual.height());
    }
}
