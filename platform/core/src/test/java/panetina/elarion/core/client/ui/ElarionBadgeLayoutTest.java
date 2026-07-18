package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElarionBadgeLayoutTest {
    @Test
    void textMaxWidthLeavesHorizontalPadding() {
        assertEquals(106, ElarionBadgeLayout.textMaxWidth(116));
        assertEquals(8, ElarionBadgeLayout.textMaxWidth(0));
    }

    @Test
    void badgeUsesMinimumWidthAndFixedInsets() {
        ElarionBadgeLayout.Badge layout = ElarionBadgeLayout.badge(12, 34, 116, 4);

        assertEquals(12, layout.bounds().x());
        assertEquals(34, layout.bounds().y());
        assertEquals(24, layout.bounds().width());
        assertEquals(10, layout.bounds().height());
        assertEquals(12, layout.accent().x());
        assertEquals(2, layout.accent().width());
        assertEquals(17, layout.textX());
        assertEquals(35, layout.textY());
    }

    @Test
    void badgeClampsToMaximumWidth() {
        ElarionBadgeLayout.Badge layout = ElarionBadgeLayout.badge(12, 34, 42, 80);

        assertEquals(42, layout.bounds().width());
        assertEquals(40, layout.topLine().width());
    }
}
