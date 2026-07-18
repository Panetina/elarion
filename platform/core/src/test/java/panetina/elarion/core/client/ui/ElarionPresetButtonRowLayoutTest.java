package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionPresetButtonRowLayoutTest {
    @Test
    void rowMatchesNpcBankPresetGeometry() {
        ElarionPresetButtonRowLayout.Row row =
                ElarionPresetButtonRowLayout.row(24, 184, 90, 22, 10, 3);

        assertEquals(3, row.buttons().size());
        assertRect(row.button(0), 24, 184, 90, 22);
        assertRect(row.button(1), 124, 184, 90, 22);
        assertRect(row.button(2), 224, 184, 90, 22);
        assertRect(row.bounds(), 24, 184, 290, 22);
        assertEquals(0, row.hitIndex(24, 184));
        assertEquals(1, row.hitIndex(124, 184));
        assertEquals(2, row.hitIndex(313, 205));
        assertEquals(-1, row.hitIndex(314, 205));
    }

    @Test
    void presetConfirmRowMatchesNpcBankAmountActions() {
        ElarionPresetButtonRowLayout.PresetConfirmRow row =
                ElarionPresetButtonRowLayout.presetConfirmRow(24, 184, 90, 22, 10, 3, 12, 160);

        assertRect(row.presets().button(0), 24, 184, 90, 22);
        assertRect(row.presets().button(1), 124, 184, 90, 22);
        assertRect(row.presets().button(2), 224, 184, 90, 22);
        assertRect(row.confirm(), 326, 184, 160, 22);
        assertRect(row.bounds(), 24, 184, 462, 22);
    }

    @Test
    void rowHandlesEmptyCount() {
        ElarionPresetButtonRowLayout.Row row =
                ElarionPresetButtonRowLayout.row(5, 6, 20, 10, 4, 0);

        assertEquals(0, row.buttons().size());
        assertRect(row.bounds(), 5, 6, 0, 10);
        assertEquals(-1, row.hitIndex(5, 6));
    }

    @Test
    void rowClampsInvalidSizes() {
        ElarionPresetButtonRowLayout.Row row =
                ElarionPresetButtonRowLayout.row(1, 2, -3, 0, -4, 2);

        assertRect(row.button(0), 1, 2, 1, 1);
        assertRect(row.button(1), 2, 2, 1, 1);
        assertRect(row.bounds(), 1, 2, 2, 1);
    }

    private static void assertRect(ElarionSemanticRowLayout.Rect actual, int x, int y, int width, int height) {
        assertEquals(x, actual.x());
        assertEquals(y, actual.y());
        assertEquals(width, actual.width());
        assertEquals(height, actual.height());
    }
}
