package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NotificationDismissPayload(String notificationId) implements CustomPayload {
    public static final Id<NotificationDismissPayload> ID =
            new Id<>(Identifier.of("elarion_core", "notification_dismiss"));

    public static final PacketCodec<PacketByteBuf, NotificationDismissPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> buffer.writeString(payload.notificationId()),
            buffer -> new NotificationDismissPayload(buffer.readString(256)));

    public NotificationDismissPayload {
        notificationId = notificationId == null ? "" : notificationId;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
