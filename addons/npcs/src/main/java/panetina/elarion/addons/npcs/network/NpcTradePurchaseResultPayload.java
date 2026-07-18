package panetina.elarion.addons.npcs.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.UUID;

public record NpcTradePurchaseResultPayload(
        UUID purchaseId,
        UUID npcId,
        String nodeId,
        String offerId,
        int quantity,
        long subtotal,
        long tax,
        long total,
        boolean successful,
        String message
) implements CustomPayload {
    public static final Id<NpcTradePurchaseResultPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "trade_purchase_result"));
    public static final PacketCodec<RegistryByteBuf, NpcTradePurchaseResultPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.purchaseId());
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.offerId(), 128);
                buffer.writeVarInt(payload.quantity());
                buffer.writeVarLong(payload.subtotal());
                buffer.writeVarLong(payload.tax());
                buffer.writeVarLong(payload.total());
                buffer.writeBoolean(payload.successful());
                ElarionPacketCodecs.writeString(buffer, payload.message(), 256);
            },
            buffer -> new NpcTradePurchaseResultPayload(
                    buffer.readUuid(),
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    buffer.readVarInt(),
                    buffer.readVarLong(),
                    buffer.readVarLong(),
                    buffer.readVarLong(),
                    buffer.readBoolean(),
                    ElarionPacketCodecs.readString(buffer, 256)));

    public NpcTradePurchaseResultPayload {
        purchaseId = purchaseId == null ? new UUID(0L, 0L) : purchaseId;
        npcId = npcId == null ? new UUID(0L, 0L) : npcId;
        nodeId = nodeId == null ? "" : nodeId;
        offerId = offerId == null ? "" : offerId;
        quantity = Math.max(1, Math.min(64, quantity));
        subtotal = Math.max(0L, subtotal);
        tax = Math.max(0L, tax);
        total = Math.max(0L, total);
        message = message == null ? "" : message;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
