package panetina.elarion.addons.portals.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalCommandRoleTest {
    @Test
    void canonicalInternalNamesMapToExistingRoles() {
        assertEquals(PortalEndpointRole.SOURCE, PortalEndpointRole.parse("source"));
        assertEquals(PortalEndpointRole.RETURN, PortalEndpointRole.parse("return"));
        assertEquals(PortalArrivalRole.OUTBOUND, PortalArrivalRole.parse("outbound"));
        assertEquals(PortalArrivalRole.RETURN, PortalArrivalRole.parse("return"));
    }

    @Test
    void legacySourceDestinationNamesRemainAcceptedInternally() {
        assertEquals(PortalEndpointRole.RETURN, PortalEndpointRole.parse("destination"));
        assertEquals(PortalArrivalRole.OUTBOUND, PortalArrivalRole.parse("destination"));
        assertEquals(PortalArrivalRole.RETURN, PortalArrivalRole.parse("source"));
    }
}
