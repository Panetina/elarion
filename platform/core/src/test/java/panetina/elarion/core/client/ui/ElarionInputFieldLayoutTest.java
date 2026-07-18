package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElarionInputFieldLayoutTest {
    @Test
    void singleLineWithIconMatchesBankAmountGeometry() {
        ElarionInputFieldLayout.SingleLine layout =
                ElarionInputFieldLayout.singleLine(24, 154, 290, 22, 4, 16, 4, true);

        assertEquals(24, layout.bounds().x());
        assertEquals(154, layout.bounds().y());
        assertEquals(290, layout.bounds().width());
        assertEquals(22, layout.bounds().height());
        assertEquals(28, layout.icon().x());
        assertEquals(157, layout.icon().y());
        assertEquals(16, layout.icon().width());
        assertEquals(48, layout.textX());
        assertEquals(260, layout.textMaxWidth());
        assertEquals(308, layout.caretMaxX());
    }

    @Test
    void singleLineWithoutIconStartsTextAtInset() {
        ElarionInputFieldLayout.SingleLine layout =
                ElarionInputFieldLayout.singleLine(10, 20, 120, 18, 6, 16, 4, false);

        assertEquals(16, layout.textX());
        assertEquals(106, layout.textMaxWidth());
        assertEquals(122, layout.caretMaxX());
        assertEquals(0, layout.icon().width());
    }

    @Test
    void singleLineClampsInvalidBounds() {
        ElarionInputFieldLayout.SingleLine layout =
                ElarionInputFieldLayout.singleLine(10, 20, 0, -3, -1, 0, -2, true);

        assertEquals(1, layout.bounds().width());
        assertEquals(1, layout.bounds().height());
        assertEquals(1, layout.icon().width());
        assertEquals(1, layout.textMaxWidth());
    }
}
