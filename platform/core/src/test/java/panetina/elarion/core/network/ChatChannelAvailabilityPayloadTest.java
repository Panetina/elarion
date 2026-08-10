package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionChatChannel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ChatChannelAvailabilityPayloadTest {
    @Test void preservesServerOrderedEligibleChannels() {
        ChatChannelAvailabilityPayload payload = new ChatChannelAvailabilityPayload(
                List.of(ElarionChatChannel.LOCAL, ElarionChatChannel.REALM, ElarionChatChannel.GUILD));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        ChatChannelAvailabilityPayload.CODEC.encode(buffer, payload);

        assertEquals(payload.channels(), ChatChannelAvailabilityPayload.CODEC.decode(buffer).channels());
    }
}
