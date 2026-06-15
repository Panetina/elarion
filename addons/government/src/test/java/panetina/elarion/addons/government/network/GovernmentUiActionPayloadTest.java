package panetina.elarion.addons.government.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GovernmentUiActionPayloadTest {
    @Test
    void codecRoundTripsGovernmentUiActionWithSession() {
        GovernmentUiActionPayload payload = new GovernmentUiActionPayload(
                "civic_name",
                "vote",
                "realm1",
                "name_oak",
                "",
                "",
                "session-1");

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GovernmentUiActionPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, GovernmentUiActionPayload.CODEC.decode(buffer));
    }
}
