package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElarionEmptyStateLayoutTest {
    @Test
    void compactEmptyStateMatchesNotificationOffsets() {
        ElarionEmptyStateLayout.EmptyState layout =
                ElarionEmptyStateLayout.compact(70, 28, 256, 74, 10);

        assertEquals(70, layout.panel().x());
        assertEquals(28, layout.panel().y());
        assertEquals(256, layout.panel().width());
        assertEquals(74, layout.panel().height());
        assertEquals(77, layout.titleX());
        assertEquals(35, layout.titleY());
        assertEquals(77, layout.body().x());
        assertEquals(47, layout.body().y());
        assertEquals(242, layout.body().width());
        assertEquals(48, layout.body().height());
    }

    @Test
    void compactEmptyStateClampsInvalidBounds() {
        ElarionEmptyStateLayout.EmptyState layout =
                ElarionEmptyStateLayout.compact(1, 2, 0, -3, 0);

        assertEquals(1, layout.panel().width());
        assertEquals(1, layout.panel().height());
        assertEquals(1, layout.body().width());
        assertEquals(1, layout.body().height());
    }
}
