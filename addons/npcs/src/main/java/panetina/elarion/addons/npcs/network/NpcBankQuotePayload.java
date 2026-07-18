package panetina.elarion.addons.npcs.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.Locale;
import java.util.UUID;

public record NpcBankQuotePayload(
        UUID npcId,
        String nodeId,
        String mode,
        int amount,
        long balance,
        int physicalCurrency,
        int taxBasisPoints,
        long fee,
        long total,
        boolean valid,
        String message
) implements CustomPayload {
    public static final Id<NpcBankQuotePayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "bank_quote"));
    public static final PacketCodec<RegistryByteBuf, NpcBankQuotePayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.mode(), 32);
                buffer.writeVarInt(payload.amount());
                buffer.writeVarLong(payload.balance());
                buffer.writeVarInt(payload.physicalCurrency());
                buffer.writeVarInt(payload.taxBasisPoints());
                buffer.writeVarLong(payload.fee());
                buffer.writeVarLong(payload.total());
                buffer.writeBoolean(payload.valid());
                ElarionPacketCodecs.writeString(buffer, payload.message(), 256);
            },
            buffer -> new NpcBankQuotePayload(
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 32),
                    buffer.readVarInt(),
                    buffer.readVarLong(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarLong(),
                    buffer.readVarLong(),
                    buffer.readBoolean(),
                    ElarionPacketCodecs.readString(buffer, 256)));

    public NpcBankQuotePayload {
        npcId = npcId == null ? new UUID(0L, 0L) : npcId;
        nodeId = nodeId == null ? "" : nodeId;
        mode = normalizeMode(mode);
        amount = Math.max(0, amount);
        physicalCurrency = Math.max(0, physicalCurrency);
        taxBasisPoints = Math.max(0, Math.min(10_000, taxBasisPoints));
        fee = Math.max(0L, fee);
        total = Math.max(0L, total);
        message = message == null ? "" : message;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static String normalizeMode(String mode) {
        String value = mode == null ? "" : mode.trim().toLowerCase(Locale.ROOT);
        return "withdraw".equals(value) ? "withdraw" : "deposit";
    }
}
