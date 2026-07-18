package panetina.elarion.addons.portals.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.portals.model.PortalTravelDirection;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PortalTravelPromptPayloadTest {
    @Test
    void roundTripsCostKind() {
        PortalTravelPromptPayload payload = new PortalTravelPromptPayload(
                "nether_gate", "Nether Gate", PortalTravelDirection.OUTBOUND, 1234L,
                "elarion:portal_ticket", PortalTravelPromptPayload.COST_TICKET,
                "You need a Nether Ticket.", 0xFFFFD37A, true, "",
                "default", 260, 150, 100, 72, 56);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        PortalTravelPromptPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, PortalTravelPromptPayload.CODEC.decode(buffer));
    }

    @Test
    void invalidCostKindFallsBackToFree() {
        PortalTravelPromptPayload payload = new PortalTravelPromptPayload(
                "neutral_gate", "Neutral Gate", PortalTravelDirection.OUTBOUND, 0L,
                "minecraft:air", "unknown", "No ticket or fee required.", 0,
                true, "", "default", 260, 150, 100, 72, 56);

        assertEquals(PortalTravelPromptPayload.COST_FREE, payload.costKind());
    }
}
