package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElarionProgressTrackLayoutTest {
    @Test
    void emptyTrackHasNoFill() {
        ElarionProgressTrackLayout.ProgressTrack layout =
                ElarionProgressTrackLayout.track(10, 20, 100, 4, 0.0F);

        assertEquals(10, layout.bounds().x());
        assertEquals(20, layout.bounds().y());
        assertEquals(100, layout.bounds().width());
        assertEquals(4, layout.bounds().height());
        assertFalse(layout.hasFill());
    }

    @Test
    void partialTrackFillsInsideBorder() {
        ElarionProgressTrackLayout.ProgressTrack layout =
                ElarionProgressTrackLayout.track(10, 20, 100, 4, 0.5F);

        assertEquals(11, layout.fill().x());
        assertEquals(21, layout.fill().y());
        assertEquals(49, layout.fill().width());
        assertEquals(2, layout.fill().height());
        assertTrue(layout.hasFill());
    }

    @Test
    void fullTrackClampsToInnerWidth() {
        ElarionProgressTrackLayout.ProgressTrack layout =
                ElarionProgressTrackLayout.track(10, 20, 100, 4, 2.0F);

        assertEquals(98, layout.fill().width());
        assertTrue(layout.hasFill());
    }

    @Test
    void invalidDimensionsClampSafely() {
        ElarionProgressTrackLayout.ProgressTrack layout =
                ElarionProgressTrackLayout.track(10, 20, 0, -3, 1.0F);

        assertEquals(1, layout.bounds().width());
        assertEquals(1, layout.bounds().height());
        assertFalse(layout.hasFill());
    }
}
