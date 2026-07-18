package panetina.elarion.addons.portals.client;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PortalConfirmationScreenLayoutTest {
    @Test
    void freePromptsOmitThePaymentSlot() {
        assertFalse(PortalConfirmationScreen.hasPaymentSlot(PortalTravelPromptPayload.COST_FREE));
        assertTrue(PortalConfirmationScreen.hasPaymentSlot(PortalTravelPromptPayload.COST_TICKET));
        assertTrue(PortalConfirmationScreen.hasPaymentSlot(PortalTravelPromptPayload.COST_FEE));
    }

    @Test
    void ticketPromptsResolveRouteSpecificTicketIcons() {
        assertEquals("nether_ticket", PortalConfirmationScreen.semanticGateIcon(
                "nether_gate", PortalTravelPromptPayload.COST_TICKET));
        assertEquals("end_ticket", PortalConfirmationScreen.semanticGateIcon(
                "end_gate", PortalTravelPromptPayload.COST_TICKET));
        assertEquals("portal_ticket", PortalConfirmationScreen.semanticGateIcon(
                "worldheart_gate", PortalTravelPromptPayload.COST_TICKET));
    }

    @Test
    void buttonsStayCenteredAsAPair() {
        PortalConfirmationScreen.ButtonLayout layout = PortalConfirmationScreen.buttonLayout(260, 100, 72, 12);

        assertEquals(38, layout.confirmX());
        assertEquals(150, layout.closeX());
    }
}
