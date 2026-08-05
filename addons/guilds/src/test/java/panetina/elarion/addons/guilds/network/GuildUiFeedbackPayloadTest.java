package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GuildUiFeedbackPayloadTest {
    @Test void roundTripsResult() {
        GuildUiFeedbackPayload payload = new GuildUiFeedbackPayload(false, "That tag is already in use.");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GuildUiFeedbackPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, GuildUiFeedbackPayload.CODEC.decode(buffer));
    }
}
