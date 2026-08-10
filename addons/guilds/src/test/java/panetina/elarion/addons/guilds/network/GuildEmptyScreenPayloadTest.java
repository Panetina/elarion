package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

final class GuildEmptyScreenPayloadTest {
    @Test void usesTheStatelessSingletonCodec() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GuildEmptyScreenPayload.CODEC.encode(buffer, GuildEmptyScreenPayload.INSTANCE);
        assertSame(GuildEmptyScreenPayload.INSTANCE, GuildEmptyScreenPayload.CODEC.decode(buffer));
    }
}
