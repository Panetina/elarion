package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElarionModalLayoutTest {
    @Test
    void twoButtonModalMatchesAdminActionConfirmGeometry() {
        ElarionModalLayout.TwoButtonModal layout = ElarionModalLayout.twoButtonModal(
                660,
                430,
                new ElarionModalLayout.Spec(380, 158, 18, 36, 66, 8, 30, 112, 20, 16)
        );

        assertEquals(140, layout.x());
        assertEquals(136, layout.y());
        assertEquals(158, layout.bodyX());
        assertEquals(172, layout.bodyY());
        assertEquals(344, layout.bodyWidth());
        assertEquals(66, layout.bodyHeight());
        assertEquals(210, layout.cancelX());
        assertEquals(338, layout.submitX());
        assertEquals(264, layout.buttonY());
        assertEquals(284, layout.buttonBottom());
        assertTrue(layout.bottom() < 430);
    }

    @Test
    void twoButtonModalMatchesAdminActionInputGeometry() {
        ElarionModalLayout.TwoButtonModal layout = ElarionModalLayout.twoButtonModal(
                660,
                430,
                new ElarionModalLayout.Spec(380, 176, 18, 36, 48, 8, 30, 112, 20, 16)
        );

        assertEquals(140, layout.x());
        assertEquals(127, layout.y());
        assertEquals(158, layout.bodyX());
        assertEquals(163, layout.bodyY());
        assertEquals(219, layout.inputY());
        assertEquals(344, layout.inputWidth());
        assertEquals(273, layout.buttonY());
    }

    @Test
    void invalidSpecClampsToPositiveGeometry() {
        ElarionModalLayout.TwoButtonModal layout = ElarionModalLayout.twoButtonModal(
                0,
                0,
                new ElarionModalLayout.Spec(0, -1, -2, -3, 0, -4, 0, 0, 0, -5)
        );

        assertEquals(1, layout.width());
        assertEquals(1, layout.height());
        assertEquals(1, layout.bodyWidth());
        assertEquals(1, layout.bodyHeight());
        assertEquals(1, layout.buttonWidth());
        assertEquals(1, layout.buttonHeight());
    }
}
