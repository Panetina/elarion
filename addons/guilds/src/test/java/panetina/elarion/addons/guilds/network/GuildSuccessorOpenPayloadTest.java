package panetina.elarion.addons.guilds.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GuildSuccessorOpenPayloadTest {
    @Test
    void roundTripsTheBoundedServerAuthorizedCandidateList() {
        GuildSuccessorOpenPayload payload = new GuildSuccessorOpenPayload("Ember Court", List.of(
                new GuildSuccessorOpenPayload.Candidate(UUID.randomUUID(), "Aster"),
                new GuildSuccessorOpenPayload.Candidate(UUID.randomUUID(), "Rowan")));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GuildSuccessorOpenPayload.CODEC.encode(buffer, payload);
        assertEquals(payload, GuildSuccessorOpenPayload.CODEC.decode(buffer));
    }
}
