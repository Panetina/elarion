package panetina.elarion.addons.npcs.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcDialoguePromptSubmitPayloadTest {
    @Test
    void roundTripsPromptSubmission() {
        UUID npcId = UUID.randomUUID();
        NpcDialoguePromptSubmitPayload payload =
                new NpcDialoguePromptSubmitPayload(npcId, "intro", "deposit", "12345");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        NpcDialoguePromptSubmitPayload.CODEC.encode(buffer, payload);
        NpcDialoguePromptSubmitPayload decoded = NpcDialoguePromptSubmitPayload.CODEC.decode(buffer);

        assertEquals(npcId, decoded.npcId());
        assertEquals("intro", decoded.nodeId());
        assertEquals("deposit", decoded.optionId());
        assertEquals("12345", decoded.value());
    }
}
