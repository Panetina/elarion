package panetina.elarion.addons.portals.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalRouteModeTest {
    @Test
    void scheduledRoutesRequireTheirProgressionControls() {
        PortalRouteMode mode = PortalRouteMode.parse("scheduled_ticketed");

        assertTrue(mode.usesSchedule());
        assertTrue(mode.requiresUnlock());
        assertTrue(mode.requiresTicket());
    }

    @Test
    void alwaysOpenRoutesRequireNoWindowLockOrTicket() {
        PortalRouteMode mode = PortalRouteMode.parse("always-open");

        assertFalse(mode.usesSchedule());
        assertFalse(mode.requiresUnlock());
        assertFalse(mode.requiresTicket());
        assertEquals("always_open", mode.configId());
    }

    @Test
    void feePassageUsesEconomyWithoutTicketsOrSchedule() {
        PortalRouteMode mode = PortalRouteMode.parse("fee_passage");

        assertFalse(mode.usesSchedule());
        assertFalse(mode.requiresUnlock());
        assertFalse(mode.requiresTicket());
        assertTrue(mode.chargesPassage());
    }

    @Test
    void rejectsUnknownModes() {
        assertThrows(IllegalArgumentException.class, () -> PortalRouteMode.parse("sometimes"));
    }
}
