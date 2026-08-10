package panetina.elarion.addons.npcs.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcQuestMarkerSyncPayloadTest {
    @Test void roundTripsBoundedDistinctNpcIds() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        NpcQuestMarkerSyncPayload payload = new NpcQuestMarkerSyncPayload(ids);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        NpcQuestMarkerSyncPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, NpcQuestMarkerSyncPayload.CODEC.decode(buffer));
    }

    @Test void dropsDuplicateAndNullIds() {
        UUID id = UUID.randomUUID();
        assertEquals(List.of(id), new NpcQuestMarkerSyncPayload(java.util.Arrays.asList(id, null, id)).npcIds());
    }
}
