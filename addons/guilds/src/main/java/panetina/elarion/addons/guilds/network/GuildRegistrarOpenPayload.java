package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

/** Server-authored, bounded creation terms for the Guild Registrar surface. */
public record GuildRegistrarOpenPayload(
        boolean enabled,
        long creationFee,
        long walletBalance,
        String currencyPlural,
        int minTagLength,
        int maxTagLength,
        int maxNameLength
) implements CustomPayload {
    public static final Id<GuildRegistrarOpenPayload> ID = new Id<>(Identifier.of("elarion_guilds", "registrar_open"));
    public static final PacketCodec<PacketByteBuf, GuildRegistrarOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeBoolean(payload.enabled);
                buffer.writeVarLong(payload.creationFee);
                buffer.writeVarLong(payload.walletBalance);
                ElarionPacketCodecs.writeString(buffer, payload.currencyPlural, 32);
                buffer.writeVarInt(payload.minTagLength);
                buffer.writeVarInt(payload.maxTagLength);
                buffer.writeVarInt(payload.maxNameLength);
            },
            buffer -> new GuildRegistrarOpenPayload(
                    buffer.readBoolean(),
                    buffer.readVarLong(),
                    buffer.readVarLong(),
                    ElarionPacketCodecs.readString(buffer, 32),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt()));

    public GuildRegistrarOpenPayload {
        if (creationFee < 0L || walletBalance < 0L) {
            throw new IllegalArgumentException("Guild Registrar currency values must not be negative.");
        }
        currencyPlural = currencyPlural == null || currencyPlural.isBlank() ? "Sigils" : currencyPlural;
        if (minTagLength < 1 || maxTagLength < minTagLength || maxTagLength > 16) {
            throw new IllegalArgumentException("Guild Registrar tag bounds are invalid.");
        }
        if (maxNameLength < 3 || maxNameLength > 128) {
            throw new IllegalArgumentException("Guild Registrar name bound is invalid.");
        }
    }

    public boolean affordable() {
        return walletBalance >= creationFee;
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
