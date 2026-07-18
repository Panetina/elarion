package panetina.elarion.addons.npcs.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.UUID;

public record NpcTradePurchaseRequestPayload(
        UUID purchaseId,
        UUID npcId,
        String nodeId,
        String catalogId,
        long catalogRevision,
        String offerId,
        int quantity
) implements CustomPayload {
    public static final Id<NpcTradePurchaseRequestPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "trade_purchase_request"));
    public static final PacketCodec<RegistryByteBuf, NpcTradePurchaseRequestPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.purchaseId());
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.catalogId(), 128);
                buffer.writeLong(payload.catalogRevision());
                ElarionPacketCodecs.writeString(buffer, payload.offerId(), 128);
                buffer.writeVarInt(payload.quantity());
            },
            buffer -> new NpcTradePurchaseRequestPayload(
                    buffer.readUuid(),
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    buffer.readLong(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    buffer.readVarInt()));

    public NpcTradePurchaseRequestPayload {
        purchaseId = purchaseId == null ? new UUID(0L, 0L) : purchaseId;
        npcId = npcId == null ? new UUID(0L, 0L) : npcId;
        nodeId = nodeId == null ? "" : nodeId;
        catalogId = catalogId == null ? "" : catalogId;
        offerId = offerId == null ? "" : offerId;
        quantity = Math.max(1, Math.min(64, quantity));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
