package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class GuildScreenActionPayloadTest {
    @Test void roundTripsBoundedIconRedrawRequest() {
        byte[] pixels = new byte[1024];
        pixels[100] = 7;
        GuildScreenActionPayload payload = new GuildScreenActionPayload("redraw_icon", UUID.randomUUID(), "", pixels);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GuildScreenActionPayload.CODEC.encode(buffer, payload);
        GuildScreenActionPayload decoded = GuildScreenActionPayload.CODEC.decode(buffer);
        assertEquals(payload.action(), decoded.action());
        assertEquals(payload.target(), decoded.target());
        assertArrayEquals(pixels, decoded.iconPixels());
    }

    @Test void rejectsNon32By32IconPayload() {
        assertThrows(IllegalArgumentException.class,
                () -> new GuildScreenActionPayload("redraw_icon", null, "", new byte[12]));
    }
}
