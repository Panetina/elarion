package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionStatusLineLayoutTest {
    @Test
    void singleLineKeepsBankFeedbackGeometry() {
        ElarionStatusLineLayout.SingleLine line =
                ElarionStatusLineLayout.singleLine(178, 270, 300, 10, 10);

        assertEquals(new ElarionSemanticRowLayout.Rect(178, 270, 300, 10), line.bounds());
        assertEquals(178, line.textX());
        assertEquals(270, line.textY());
        assertEquals(300, line.textMaxWidth());
    }

    @Test
    void singleLineCentersTextInsideTallerStatusArea() {
        ElarionStatusLineLayout.SingleLine line =
                ElarionStatusLineLayout.singleLine(20, 30, 120, 20, 10);

        assertEquals(20, line.textX());
        assertEquals(35, line.textY());
        assertEquals(120, line.textMaxWidth());
    }

    @Test
    void singleLineClampsInvalidSizes() {
        ElarionStatusLineLayout.SingleLine line =
                ElarionStatusLineLayout.singleLine(5, 6, 0, -2, 0);

        assertEquals(new ElarionSemanticRowLayout.Rect(5, 6, 1, 1), line.bounds());
        assertEquals(5, line.textX());
        assertEquals(6, line.textY());
        assertEquals(1, line.textMaxWidth());
    }
}
