package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentitySyncPayloadTest {
    @Test
    void codecRoundTripsTabAndOverheadVisibilitySeparately() {
        IdentitySyncPayload payload = new IdentitySyncPayload(
                UUID.randomUUID(),
                "Matie",
                "Matie",
                "",
                "",
                "Citizen",
                "",
                "gold",
                "Kingdom of Oak",
                "realm1",
                false,
                true);

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        IdentitySyncPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, IdentitySyncPayload.CODEC.decode(buffer));
    }

    @Test
    void clampsLongStringsBeforeEncoding() {
        String longText = "x".repeat(300);
        IdentitySyncPayload payload = new IdentitySyncPayload(
                UUID.randomUUID(),
                longText,
                longText,
                longText,
                longText,
                longText,
                longText,
                longText,
                longText,
                longText,
                true,
                true);

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        IdentitySyncPayload.CODEC.encode(buffer, payload);
        IdentitySyncPayload decoded = IdentitySyncPayload.CODEC.decode(buffer);

        assertEquals(64, decoded.username().length());
        assertEquals(128, decoded.nickname().length());
        assertEquals(128, decoded.prefix().length());
        assertEquals(128, decoded.suffix().length());
        assertEquals(128, decoded.title().length());
        assertEquals(128, decoded.leaderLabel().length());
        assertEquals(32, decoded.color().length());
        assertEquals(128, decoded.realmName().length());
        assertEquals(128, decoded.realmId().length());
    }
}
