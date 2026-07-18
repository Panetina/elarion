package panetina.elarion.addons.portals.command;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.model.PortalTravelDirection;
import panetina.elarion.addons.portals.model.PortalUiConfig;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PortalCommandsPreviewTest {
    private static final PortalUiConfig UI = new PortalUiConfig("default", 340, 190, 50, 104, 104);

    @Test
    void neutralPreviewUsesFreeCostWithoutSchedule() {
        PortalTravelPromptPayload payload = PortalCommands.previewPayload("neutral", UI);

        assertEquals("neutral", payload.routeId());
        assertEquals(PortalTravelPromptPayload.COST_FREE, payload.costKind());
        assertEquals(0L, payload.closesAt());
        assertTrue(payload.allowed());
    }

    @Test
    void netherAndEndPreviewsUseTicketCost() {
        PortalTravelPromptPayload nether = PortalCommands.previewPayload("nether", UI);
        PortalTravelPromptPayload end = PortalCommands.previewPayload("end", UI);

        assertEquals("nether", nether.routeId());
        assertEquals("end", end.routeId());
        assertEquals(PortalTravelPromptPayload.COST_TICKET, nether.costKind());
        assertEquals(PortalTravelPromptPayload.COST_TICKET, end.costKind());
        assertEquals("elarion:portal_ticket", nether.iconItem());
        assertEquals("elarion:portal_ticket", end.iconItem());
        assertTrue(nether.closesAt() > 0L);
        assertTrue(end.closesAt() > 0L);
    }

    @Test
    void feePreviewUsesSigilCostAndBlockedPreviewDisablesConfirm() {
        PortalTravelPromptPayload fee = PortalCommands.previewPayload("fee", UI);
        PortalTravelPromptPayload blocked = PortalCommands.previewPayload("blocked", UI);

        assertEquals(PortalTravelPromptPayload.COST_FEE, fee.costKind());
        assertEquals("elarion:currency", fee.iconItem());
        assertTrue(fee.allowed());
        assertEquals(PortalTravelPromptPayload.COST_FEE, blocked.costKind());
        assertFalse(blocked.allowed());
    }

    @Test
    void returnPreviewUsesFreeReturnDirection() {
        PortalTravelPromptPayload payload = PortalCommands.previewPayload("return", UI);

        assertEquals(PortalTravelDirection.RETURN, payload.direction());
        assertEquals(PortalTravelPromptPayload.COST_FREE, payload.costKind());
    }

    @Test
    void unknownPreviewStateIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PortalCommands.previewPayload("bad", UI));
    }
}
