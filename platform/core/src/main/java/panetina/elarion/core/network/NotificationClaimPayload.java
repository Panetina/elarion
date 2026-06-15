package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NotificationClaimPayload(String grantId) implements CustomPayload {
    public static final Id<NotificationClaimPayload> ID =
            new Id<>(Identifier.of("elarion_core", "notification_claim"));

    public static final PacketCodec<PacketByteBuf, NotificationClaimPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> buffer.writeString(payload.grantId()),
            buffer -> new NotificationClaimPayload(buffer.readString(256)));

    public NotificationClaimPayload {
        grantId = grantId == null ? "" : grantId;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
