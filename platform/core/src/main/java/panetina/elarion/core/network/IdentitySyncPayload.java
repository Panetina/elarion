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
        String realmId,
        boolean visible
) implements CustomPayload {
    public static final Id<IdentitySyncPayload> ID =
            new Id<>(Identifier.of("elarion_core", "identity_sync"));

    public static final PacketCodec<PacketByteBuf, IdentitySyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.uuid());
                buffer.writeString(payload.username());
                buffer.writeString(payload.nickname());
                buffer.writeString(payload.prefix());
                buffer.writeString(payload.suffix());
                buffer.writeString(payload.title());
                buffer.writeString(payload.leaderLabel());
                buffer.writeString(payload.color());
                buffer.writeString(payload.realmId());
                buffer.writeBoolean(payload.visible());
            },
            buffer -> new IdentitySyncPayload(
                    buffer.readUuid(),
                    buffer.readString(64),
                    buffer.readString(128),
                    buffer.readString(128),
                    buffer.readString(128),
                    buffer.readString(128),
                    buffer.readString(128),
                    buffer.readString(32),
                    buffer.readString(128),
                    buffer.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
