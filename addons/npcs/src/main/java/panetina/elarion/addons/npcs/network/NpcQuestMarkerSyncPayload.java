package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A per-viewer, bounded projection of NPCs with an available quest. */
public record NpcQuestMarkerSyncPayload(List<UUID> npcIds) implements CustomPayload {
    public static final Id<NpcQuestMarkerSyncPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "quest_markers"));
    public static final PacketCodec<PacketByteBuf, NpcQuestMarkerSyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeVarInt(payload.npcIds.size());
                payload.npcIds.forEach(buffer::writeUuid);
            },
            buffer -> {
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 512);
                List<UUID> ids = new ArrayList<>(count);
                for (int index = 0; index < count; index++) ids.add(buffer.readUuid());
                return new NpcQuestMarkerSyncPayload(ids);
            });

    public NpcQuestMarkerSyncPayload {
        npcIds = npcIds == null ? List.of() : npcIds.stream().filter(java.util.Objects::nonNull).distinct().limit(512).toList();
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
