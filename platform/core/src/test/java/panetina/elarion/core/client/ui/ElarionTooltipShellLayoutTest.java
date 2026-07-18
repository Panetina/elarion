package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionTooltipShellLayoutTest {
    @Test
    void tooltipOpensDownAndRightWhenSpaceAllows() {
        ElarionTooltipShellLayout.Tooltip tooltip =
                ElarionTooltipShellLayout.tooltip(100, 80, 320, 240, 90, 20, 5, 8);

        assertEquals(new ElarionSemanticRowLayout.Rect(108, 88, 100, 30), tooltip.shell());
        assertEquals(new ElarionSemanticRowLayout.Rect(113, 93, 90, 20), tooltip.content());
    }

    @Test
    void tooltipFlipsLeftAndUpNearScreenEdge() {
        ElarionTooltipShellLayout.Tooltip tooltip =
                ElarionTooltipShellLayout.tooltip(310, 230, 320, 240, 90, 20, 5, 8);

        assertEquals(new ElarionSemanticRowLayout.Rect(202, 192, 100, 30), tooltip.shell());
        assertEquals(new ElarionSemanticRowLayout.Rect(207, 197, 90, 20), tooltip.content());
    }

    @Test
    void tooltipClampsWhenContentIsLargerThanAvailableSide() {
        ElarionTooltipShellLayout.Tooltip tooltip =
                ElarionTooltipShellLayout.tooltip(4, 4, 80, 40, 120, 30, 4, 8);

        assertEquals(new ElarionSemanticRowLayout.Rect(0, 0, 128, 38), tooltip.shell());
        assertEquals(new ElarionSemanticRowLayout.Rect(4, 4, 120, 30), tooltip.content());
    }

    @Test
    void invalidSizesClampToSafeGeometry() {
        ElarionTooltipShellLayout.Tooltip tooltip =
                ElarionTooltipShellLayout.tooltip(0, 0, 0, 0, 0, -5, -2, -4);

        assertEquals(new ElarionSemanticRowLayout.Rect(0, 0, 1, 1), tooltip.shell());
        assertEquals(new ElarionSemanticRowLayout.Rect(0, 0, 1, 1), tooltip.content());
    }
}
