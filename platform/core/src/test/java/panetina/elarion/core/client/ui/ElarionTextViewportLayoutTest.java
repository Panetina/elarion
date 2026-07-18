package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ElarionTextViewportLayoutTest {
    @Test
    void textViewportMatchesBiographyGeometry() {
        ElarionTextViewportLayout.TextViewport viewport =
                ElarionTextViewportLayout.lines(304, 160, 312, 108, 10, 14, 0);

        assertEquals(new ElarionSemanticRowLayout.Rect(304, 160, 312, 108), viewport.bounds());
        assertEquals(10, viewport.visibleLineCapacity());
        assertEquals(10, viewport.visibleLineCount());
        assertEquals(4, viewport.maximumFirstLine());
        assertEquals(160, viewport.lineY(0));
        assertEquals(250, viewport.lineY(9));
        assertTrue(viewport.canScrollDown());
        assertFalse(viewport.canScrollUp());
    }

    @Test
    void firstLineIsClampedAndLastExclusiveIsVisibleRangeEnd() {
        ElarionTextViewportLayout.TextViewport viewport =
                ElarionTextViewportLayout.lines(304, 160, 312, 108, 10, 14, 99);

        assertEquals(4, viewport.firstLine());
        assertEquals(10, viewport.visibleLineCount());
        assertEquals(14, viewport.lastVisibleExclusive());
        assertTrue(viewport.canScrollUp());
        assertFalse(viewport.canScrollDown());
    }

    @Test
    void visibleLineMappingRejectsOffscreenCaretLines() {
        ElarionTextViewportLayout.TextViewport viewport =
                ElarionTextViewportLayout.lines(304, 160, 312, 108, 10, 14, 4);

        assertEquals(-1, viewport.visibleLineForAbsolute(3));
        assertEquals(0, viewport.visibleLineForAbsolute(4));
        assertEquals(9, viewport.visibleLineForAbsolute(13));
        assertEquals(-1, viewport.visibleLineForAbsolute(14));
        assertEquals(4, viewport.absoluteLineForVisible(0));
        assertEquals(13, viewport.absoluteLineForVisible(9));
        assertEquals(-1, viewport.absoluteLineForVisible(10));
    }

    @Test
    void emptyAndInvalidInputClampsSafely() {
        ElarionTextViewportLayout.TextViewport viewport =
                ElarionTextViewportLayout.lines(10, 20, 0, 0, 0, -3, 8);

        assertEquals(1, viewport.bounds().width());
        assertEquals(1, viewport.bounds().height());
        assertEquals(1, viewport.lineHeight());
        assertEquals(0, viewport.firstLine());
        assertEquals(0, viewport.visibleLineCount());
        assertEquals(0, viewport.maximumFirstLine());
        assertFalse(viewport.canScrollUp());
        assertFalse(viewport.canScrollDown());
    }
}
