package panetina.elarion.addons.portals.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PortalWindowProgressTest {
    @Test
    void activeWindowDecreasesFromFullToEmpty() {
        assertEquals(1.0F, PortalWindowProgress.remaining(100L, 100L, 200L, true));
        assertEquals(0.5F, PortalWindowProgress.remaining(150L, 100L, 200L, true));
        assertEquals(0.0F, PortalWindowProgress.remaining(200L, 100L, 200L, true));
    }

    @Test
    void closedOrInvalidWindowHasNoProgress() {
        assertEquals(0.0F, PortalWindowProgress.remaining(150L, 100L, 200L, false));
        assertEquals(0.0F, PortalWindowProgress.remaining(150L, 200L, 100L, true));
    }
}
