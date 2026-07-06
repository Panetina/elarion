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

    @Test
    void clampsLongRouteStatusText() {
        String longText = "x".repeat(500);
        PortalRouteStatusSyncPayload payload = new PortalRouteStatusSyncPayload(List.of(
                new PortalRouteStatusSyncPayload.Entry(
                        longText, longText, longText,
                        true, true, false, 100L, 200L,
                        longText, longText, 0xFFA82929)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        PortalRouteStatusSyncPayload.CODEC.encode(buffer, payload);
        PortalRouteStatusSyncPayload decoded = PortalRouteStatusSyncPayload.CODEC.decode(buffer);

        PortalRouteStatusSyncPayload.Entry entry = decoded.routes().getFirst();
        assertEquals(128, entry.routeId().length());
        assertEquals(256, entry.displayName().length());
        assertEquals(64, entry.mode().length());
        assertEquals(256, entry.iconItem().length());
    }
}
