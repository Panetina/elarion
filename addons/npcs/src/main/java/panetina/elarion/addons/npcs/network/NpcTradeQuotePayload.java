package panetina.elarion.addons.npcs.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.UUID;

public record NpcTradeQuotePayload(
        UUID npcId,
        String nodeId,
        String catalogId,
        long catalogRevision,
        String offerId,
        int quantity,
        int maxQuantity,
        long subtotal,
        int taxBasisPoints,
        long tax,
        long total,
        long policyRevision,
        String taxAuthorityLabel,
        boolean valid,
        String message
) implements CustomPayload {
    public static final Id<NpcTradeQuotePayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "trade_quote"));
    public static final PacketCodec<RegistryByteBuf, NpcTradeQuotePayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.catalogId(), 128);
                buffer.writeLong(payload.catalogRevision());
                ElarionPacketCodecs.writeString(buffer, payload.offerId(), 128);
                buffer.writeVarInt(payload.quantity());
                buffer.writeVarInt(payload.maxQuantity());
                buffer.writeVarLong(payload.subtotal());
                buffer.writeVarInt(payload.taxBasisPoints());
                buffer.writeVarLong(payload.tax());
                buffer.writeVarLong(payload.total());
                buffer.writeVarLong(payload.policyRevision());
                ElarionPacketCodecs.writeString(buffer, payload.taxAuthorityLabel(), 64);
                buffer.writeBoolean(payload.valid());
                ElarionPacketCodecs.writeString(buffer, payload.message(), 256);
            },
            buffer -> new NpcTradeQuotePayload(
                    buffer.readUuid(), ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128), buffer.readLong(),
                    ElarionPacketCodecs.readString(buffer, 128), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarLong(), buffer.readVarInt(), buffer.readVarLong(), buffer.readVarLong(),
                    buffer.readVarLong(), ElarionPacketCodecs.readString(buffer, 64), buffer.readBoolean(),
                    ElarionPacketCodecs.readString(buffer, 256)));

    public NpcTradeQuotePayload {
        npcId = npcId == null ? new UUID(0L, 0L) : npcId;
        nodeId = nodeId == null ? "" : nodeId;
        catalogId = catalogId == null ? "" : catalogId;
        offerId = offerId == null ? "" : offerId;
        quantity = Math.max(1, Math.min(64, quantity));
        maxQuantity = Math.max(1, Math.min(64, maxQuantity));
        taxBasisPoints = Math.max(0, Math.min(10_000, taxBasisPoints));
        taxAuthorityLabel = taxAuthorityLabel == null ? "" : taxAuthorityLabel;
        message = message == null ? "" : message;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
