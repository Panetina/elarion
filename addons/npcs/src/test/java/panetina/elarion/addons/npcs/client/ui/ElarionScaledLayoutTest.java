package panetina.elarion.addons.npcs.client.ui;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.client.ui.ElarionScaledLayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionScaledLayoutTest {
    @Test
    void scalesWholeCanvasToFitSmallWindow() {
        ElarionScaledLayout layout = ElarionScaledLayout.fit(210, 170, 420, 340, 0, 60);

        assertEquals(0.5F, layout.scale(), 0.001F);
        assertEquals(100.0D, layout.logicalX(50), 0.001D);
        assertTrue(layout.belowPreferredScale());
    }
}
