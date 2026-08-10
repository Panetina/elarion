package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GuildRegistrarOpenPayloadTest {
    @Test void roundTripsServerAuthoredTerms() {
        GuildRegistrarOpenPayload payload = new GuildRegistrarOpenPayload(true, 100L, 75L, "Sigils", 2, 6, 48);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GuildRegistrarOpenPayload.CODEC.encode(buffer, payload);
        GuildRegistrarOpenPayload decoded = GuildRegistrarOpenPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
        assertFalse(decoded.affordable());
    }

    @Test void rejectsInvalidBoundsAndCurrencyValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new GuildRegistrarOpenPayload(true, -1L, 0L, "Sigils", 2, 6, 48));
        assertThrows(IllegalArgumentException.class,
                () -> new GuildRegistrarOpenPayload(true, 1L, 1L, "Sigils", 7, 6, 48));
    }
}
