package panetina.elarion.addons.underworld.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record UnderworldStatusSyncPayload(
        boolean active,
        long remainingMillis,
        String deathType,
        int fractures,
        int maxFractures,
        boolean trueDeath
) implements CustomPayload {
    public static final Id<UnderworldStatusSyncPayload> ID =
            new Id<>(Identifier.of("elarion_underworld", "status_sync"));

    public static final PacketCodec<PacketByteBuf, UnderworldStatusSyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeBoolean(payload.active);
                buffer.writeLong(payload.remainingMillis);
                ElarionPacketCodecs.writeString(buffer, payload.deathType, 32);
                buffer.writeVarInt(payload.fractures);
                buffer.writeVarInt(payload.maxFractures);
                buffer.writeBoolean(payload.trueDeath);
            },
            buffer -> new UnderworldStatusSyncPayload(
                    buffer.readBoolean(),
                    buffer.readLong(),
                    ElarionPacketCodecs.readString(buffer, 32),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean())
    );

    public static UnderworldStatusSyncPayload clear() {
        return new UnderworldStatusSyncPayload(false, 0L, "", 0, 3, false);
    }

    public UnderworldStatusSyncPayload {
        remainingMillis = Math.max(0L, remainingMillis);
        deathType = deathType == null ? "" : deathType;
        maxFractures = Math.max(1, maxFractures);
        fractures = Math.max(0, fractures);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
