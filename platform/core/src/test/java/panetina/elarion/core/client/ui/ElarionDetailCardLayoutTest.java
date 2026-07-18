package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionDetailCardLayoutTest {
    @Test
    void identityHeaderMatchesGovernmentDetailHeaderGeometry() {
        ElarionDetailCardLayout.IdentityHeader layout = ElarionDetailCardLayout.identityHeader(
                520, 150, 196, 48, 14, 4, 22, 40);

        assertEquals(new ElarionSemanticRowLayout.Rect(520, 150, 196, 50), layout.bounds());
        assertEquals(new ElarionSemanticRowLayout.Rect(520, 150, 48, 48), layout.icon());
        assertEquals(582, layout.textX());
        assertEquals(154, layout.titleY());
        assertEquals(134, layout.textWidth());
        assertEquals(172, layout.tagY());
        assertEquals(190, layout.subtitleY());
    }

    @Test
    void identityHeaderClampsInvalidSizes() {
        ElarionDetailCardLayout.IdentityHeader layout = ElarionDetailCardLayout.identityHeader(
                10, 20, -4, 0, -1, 4, 22, 40);

        assertEquals(1, layout.bounds().width());
        assertEquals(1, layout.icon().width());
        assertEquals(11, layout.textX());
        assertEquals(1, layout.textWidth());
        assertTrue(layout.bounds().height() >= layout.icon().height());
    }
}
