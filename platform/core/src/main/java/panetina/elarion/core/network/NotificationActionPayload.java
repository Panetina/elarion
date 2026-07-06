package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NotificationActionPayload(String notificationId, String actionId) implements CustomPayload {
    public static final Id<NotificationActionPayload> ID =
            new Id<>(Identifier.of("elarion_core", "notification_action"));
    public static final PacketCodec<PacketByteBuf, NotificationActionPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.notificationId(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.actionId(), 128);
            },
            buffer -> new NotificationActionPayload(
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 128)));

    public NotificationActionPayload {
        notificationId = notificationId == null ? "" : notificationId;
        actionId = actionId == null ? "" : actionId;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
