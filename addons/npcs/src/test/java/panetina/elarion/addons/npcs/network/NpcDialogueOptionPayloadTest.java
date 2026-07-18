package panetina.elarion.addons.npcs.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcDialogueOptionPayloadTest {
    @Test
    void roundTripsPresentationRole() {
        NpcDialogueOptionPayload option = new NpcDialogueOptionPayload(
                "deposit", "Deposit", "Deposit funds", "deposit", "number", "Amount?", 10);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        NpcDialogueOptionPayload.write(option, buffer);
        NpcDialogueOptionPayload decoded = NpcDialogueOptionPayload.read(buffer);

        assertEquals("deposit", decoded.presentationRole());
        assertEquals("number", decoded.promptType());
    }
}
