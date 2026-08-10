package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionChatChannel;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class ChatChannelSendPayloadTest {
    @Test void privateChannelRoundTripsRecipientAndTrimsMessage() {
        UUID recipient = UUID.randomUUID();
        ChatChannelSendPayload payload = new ChatChannelSendPayload(ElarionChatChannel.PRIVATE, recipient, " hello ");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        ChatChannelSendPayload.CODEC.encode(buffer, payload);
        ChatChannelSendPayload decoded = ChatChannelSendPayload.CODEC.decode(buffer);
        assertEquals(ElarionChatChannel.PRIVATE, decoded.channel());
        assertEquals(recipient, decoded.recipientId());
        assertEquals("hello", decoded.message());
    }

    @Test void localChannelDoesNotNeedARecipient() {
        ChatChannelSendPayload payload = new ChatChannelSendPayload(ElarionChatChannel.LOCAL, null, "nearby");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        ChatChannelSendPayload.CODEC.encode(buffer, payload);
        assertNull(ChatChannelSendPayload.CODEC.decode(buffer).recipientId());
    }
}
