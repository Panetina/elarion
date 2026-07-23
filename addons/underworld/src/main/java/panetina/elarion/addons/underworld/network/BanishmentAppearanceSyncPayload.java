package panetina.elarion.addons.underworld.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record BanishmentAppearanceSyncPayload(UUID playerId, boolean banished) implements CustomPayload {
    public static final Id<BanishmentAppearanceSyncPayload> ID =
            new Id<>(Identifier.of("elarion_underworld", "banishment_appearance"));
    public static final PacketCodec<PacketByteBuf, BanishmentAppearanceSyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.playerId);
                buffer.writeBoolean(payload.banished);
            },
            buffer -> new BanishmentAppearanceSyncPayload(buffer.readUuid(), buffer.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
