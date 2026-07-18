package panetina.elarion.addons.npcs.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.Locale;
import java.util.UUID;

public record NpcBankQuoteRequestPayload(
        UUID npcId,
        String nodeId,
        String mode,
        int amount
) implements CustomPayload {
    public static final Id<NpcBankQuoteRequestPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "bank_quote_request"));
    public static final PacketCodec<RegistryByteBuf, NpcBankQuoteRequestPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.npcId());
                ElarionPacketCodecs.writeString(buffer, payload.nodeId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.mode(), 32);
                buffer.writeVarInt(payload.amount());
            },
            buffer -> new NpcBankQuoteRequestPayload(
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 32),
                    buffer.readVarInt()));

    public NpcBankQuoteRequestPayload {
        npcId = npcId == null ? new UUID(0L, 0L) : npcId;
        nodeId = nodeId == null ? "" : nodeId;
        mode = normalizeMode(mode);
        amount = Math.max(0, amount);
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
