package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record IdentitySyncPayload(
        UUID uuid,
        String username,
        String nickname,
        String prefix,
        String suffix,
        String title,
        String leaderLabel,
        String color,
        String realmName,
        String realmId,
        boolean tabVisible,
        boolean visible
) implements CustomPayload {
    public static final Id<IdentitySyncPayload> ID =
            new Id<>(Identifier.of("elarion_core", "identity_sync"));

    public static final PacketCodec<PacketByteBuf, IdentitySyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.uuid());
                ElarionPacketCodecs.writeString(buffer, payload.username(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.nickname(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.prefix(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.suffix(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.title(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.leaderLabel(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.color(), 32);
                ElarionPacketCodecs.writeString(buffer, payload.realmName(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.realmId(), 128);
                buffer.writeBoolean(payload.tabVisible());
                buffer.writeBoolean(payload.visible());
            },
            buffer -> new IdentitySyncPayload(
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 32),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    buffer.readBoolean(),
                    buffer.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}
