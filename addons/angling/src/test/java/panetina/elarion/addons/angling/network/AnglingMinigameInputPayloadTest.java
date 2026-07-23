package panetina.elarion.addons.angling.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnglingMinigameInputPayloadTest {
    @Test
    void boundedInputEdgeRoundTripsWithoutOutcomeFields() {
        AnglingMinigameInputPayload source = new AnglingMinigameInputPayload(
                UUID.randomUUID(), 42, 7, AnglingMinigameInputAction.RELEASE);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        AnglingMinigameInputPayload.CODEC.encode(buffer, source);
        assertEquals(source, AnglingMinigameInputPayload.CODEC.decode(buffer));
    }

    @Test
    void invalidWireActionDoesNotBecomeAValidInput() {
        UUID session = UUID.randomUUID();
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeUuid(session);
        buffer.writeVarInt(1);
        buffer.writeVarInt(0);
        buffer.writeByte(255);
        assertEquals(AnglingMinigameInputAction.INVALID,
                AnglingMinigameInputPayload.CODEC.decode(buffer).action());
    }
}
