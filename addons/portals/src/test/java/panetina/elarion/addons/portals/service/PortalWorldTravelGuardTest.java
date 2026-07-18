package panetina.elarion.addons.portals.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalWorldTravelGuardTest {
    @Test
    void onlyNetherAndEndRequireRegisteredWorldTravel() {
        assertTrue(PortalWorldTravelGuard.isRestrictedDimension("minecraft:the_nether"));
        assertTrue(PortalWorldTravelGuard.isRestrictedDimension("minecraft:the_end"));
        assertFalse(PortalWorldTravelGuard.isRestrictedDimension("minecraft:overworld"));
        assertFalse(PortalWorldTravelGuard.isRestrictedDimension("elarion:worldheart"));
        assertFalse(PortalWorldTravelGuard.isRestrictedDimension("elarion:realm1"));
    }
}
