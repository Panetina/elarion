package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionServiceHeaderLayoutTest {
    @Test
    void portraitTitleMatchesNpcBankHeaderGeometry() {
        ElarionServiceHeaderLayout.PortraitTitle header =
                ElarionServiceHeaderLayout.portraitTitle(
                        0, 0, 500, 76,
                        14, 12, 52,
                        78, 18, 40,
                        18, 150, 14, 28
                );

        assertRect(header.bounds(), 0, 0, 500, 76);
        assertRect(header.portrait(), 14, 12, 52, 52);
        assertEquals(78, header.titleX());
        assertEquals(18, header.titleY());
        assertEquals(218, header.titleMaxWidth());
        assertEquals(40, header.subtitleY());
        assertRect(header.badge(), 304, 28, 150, 20);
        assertRect(header.close(), 468, 14, 18, 18);
    }

    @Test
    void portraitTitleMatchesNpcTradeHeaderGeometry() {
        ElarionServiceHeaderLayout.PortraitTitle header =
                ElarionServiceHeaderLayout.portraitTitle(
                        0, 0, 520, 76,
                        14, 12, 52,
                        78, 18, 40,
                        18, 150, 22, 38
                );

        assertRect(header.bounds(), 0, 0, 520, 76);
        assertRect(header.portrait(), 14, 12, 52, 52);
        assertEquals(78, header.titleX());
        assertEquals(18, header.titleY());
        assertEquals(230, header.titleMaxWidth());
        assertEquals(40, header.subtitleY());
        assertRect(header.badge(), 316, 38, 150, 20);
        assertRect(header.close(), 488, 14, 18, 18);
    }

    @Test
    void portraitTitleClampsInvalidDimensions() {
        ElarionServiceHeaderLayout.PortraitTitle header =
                ElarionServiceHeaderLayout.portraitTitle(
                        4, 5, -1, -2,
                        -3, -4, -5,
                        -6, -7, -8,
                        -9, -10, -11, -12
                );

        assertRect(header.bounds(), 4, 5, 1, 1);
        assertRect(header.portrait(), 4, 5, 1, 1);
        assertEquals(4, header.titleX());
        assertEquals(5, header.titleY());
        assertEquals(1, header.titleMaxWidth());
        assertEquals(5, header.subtitleY());
        assertRect(header.close(), 4, 5, 1, 1);
        assertRect(header.badge(), 3, 5, 1, 20);
    }

    private static void assertRect(ElarionSemanticRowLayout.Rect actual, int x, int y, int width, int height) {
        assertEquals(x, actual.x());
        assertEquals(y, actual.y());
        assertEquals(width, actual.width());
        assertEquals(height, actual.height());
    }
}
