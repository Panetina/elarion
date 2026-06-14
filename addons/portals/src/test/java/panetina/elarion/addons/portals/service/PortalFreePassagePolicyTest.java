package panetina.elarion.addons.portals.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.model.PortalFreePassageState;
import panetina.elarion.addons.portals.model.PortalTravelDirection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalFreePassagePolicyTest {
    @Test
    void grantsExactlyOneFreeRoundTrip() {
        assertTrue(PortalFreePassagePolicy.isFree(true, null, PortalTravelDirection.OUTBOUND));
        PortalFreePassageState state = PortalFreePassagePolicy.afterSuccessfulTravel(
                null, PortalTravelDirection.OUTBOUND);
        assertEquals(PortalFreePassageState.RETURN_AVAILABLE, state);
        assertTrue(PortalFreePassagePolicy.isFree(true, state, PortalTravelDirection.RETURN));
        state = PortalFreePassagePolicy.afterSuccessfulTravel(state, PortalTravelDirection.RETURN);
        assertEquals(PortalFreePassageState.COMPLETED, state);
        assertFalse(PortalFreePassagePolicy.isFree(true, state, PortalTravelDirection.OUTBOUND));
        assertFalse(PortalFreePassagePolicy.isFree(true, state, PortalTravelDirection.RETURN));
    }
}
