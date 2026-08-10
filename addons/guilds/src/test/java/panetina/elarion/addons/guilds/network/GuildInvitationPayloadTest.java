package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GuildInvitationPayloadTest {
    @Test void promptRoundTripsBoundedServerAuthoredText() {
        GuildInvitationPromptPayload payload = new GuildInvitationPromptPayload("silver-dawn", "Silver Dawn", "DAWN", "Panyel");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GuildInvitationPromptPayload.CODEC.encode(buffer, payload);
        assertEquals(payload, GuildInvitationPromptPayload.CODEC.decode(buffer));
    }

    @Test void decisionRoundTripsWithoutClientAuthority() {
        GuildInvitationDecisionPayload payload = new GuildInvitationDecisionPayload("silver-dawn", true);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GuildInvitationDecisionPayload.CODEC.encode(buffer, payload);
        assertEquals(payload, GuildInvitationDecisionPayload.CODEC.decode(buffer));
    }
}
