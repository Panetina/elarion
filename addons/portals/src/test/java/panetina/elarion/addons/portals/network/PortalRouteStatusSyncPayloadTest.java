package panetina.elarion.addons.portals.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PortalRouteStatusSyncPayloadTest {
    @Test
    void roundTripsConfiguredStatusIcon() {
        PortalRouteStatusSyncPayload payload = new PortalRouteStatusSyncPayload(List.of(
                new PortalRouteStatusSyncPayload.Entry(
                        "nether", "Nether Gate", "scheduled_ticketed",
                        true, true, false, 100L, 200L,
                        "elarion:portal_ticket", "minecraft:netherrack", 0xFFA82929)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        PortalRouteStatusSyncPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, PortalRouteStatusSyncPayload.CODEC.decode(buffer));
    }
}
