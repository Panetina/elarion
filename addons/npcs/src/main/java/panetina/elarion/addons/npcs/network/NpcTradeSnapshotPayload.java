package panetina.elarion.addons.npcs.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record NpcTradeSnapshotPayload(
        UUID npcId,
        String nodeId,
        String catalogId,
        long revision,
        List<NpcTradeOfferPayload> offers,
        String emptyMessage
) implements CustomPayload {
    public static final int MAX_OFFERS = 64;
    public static final Id<NpcTradeSnapshotPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "trade_snapshot"));
    public static final PacketCodec<RegistryByteBuf, NpcTradeSnapshotPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.catalogId(), 128);
                buffer.writeLong(payload.revision());
                int count = Math.min(MAX_OFFERS, payload.offers().size());
                buffer.writeVarInt(count);
                for (int index = 0; index < count; index++) {
                    NpcTradeOfferPayload.write(payload.offers().get(index), buffer);
                }
                ElarionPacketCodecs.writeString(buffer, payload.emptyMessage(), 256);
            },
            buffer -> {
                UUID npcId = buffer.readUuid();
                String nodeId = ElarionPacketCodecs.readString(buffer, 128);
                String catalogId = ElarionPacketCodecs.readString(buffer, 128);
                long revision = buffer.readLong();
                int count = ElarionPacketCodecs.readBoundedCount(buffer, MAX_OFFERS);
                List<NpcTradeOfferPayload> offers = new ArrayList<>(count);
                for (int index = 0; index < count; index++) offers.add(NpcTradeOfferPayload.read(buffer));
                String emptyMessage = ElarionPacketCodecs.readString(buffer, 256);
                return new NpcTradeSnapshotPayload(npcId, nodeId, catalogId, revision, offers, emptyMessage);
            });

    public NpcTradeSnapshotPayload {
        npcId = npcId == null ? new UUID(0L, 0L) : npcId;
        nodeId = nodeId == null ? "" : nodeId;
        catalogId = catalogId == null ? "" : catalogId;
        offers = offers == null ? List.of() : offers.stream().limit(MAX_OFFERS).toList();
        emptyMessage = emptyMessage == null ? "" : emptyMessage;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
