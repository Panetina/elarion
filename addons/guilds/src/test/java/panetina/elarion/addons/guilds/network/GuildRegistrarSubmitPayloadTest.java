package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GuildRegistrarSubmitPayloadTest {
    @Test void roundTripsIndependentRegistrarFields() {
        GuildRegistrarSubmitPayload payload = new GuildRegistrarSubmitPayload("Silver Dawn", "DAWN", true);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GuildRegistrarSubmitPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, GuildRegistrarSubmitPayload.CODEC.decode(buffer));
    }

    @Test void normalizesNullTextWithoutPackingFields() {
        assertEquals(new GuildRegistrarSubmitPayload("", "", false),
                new GuildRegistrarSubmitPayload(null, null, false));
    }
}
