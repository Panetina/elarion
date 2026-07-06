package panetina.elarion.addons.government.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record GovernmentUiFeedbackPayload(String message) implements CustomPayload {
    private static final int MESSAGE_MAX = 1024;

    public static final Id<GovernmentUiFeedbackPayload> ID =
            new Id<>(Identifier.of("elarion_government", "government_ui_feedback"));

    public static final PacketCodec<PacketByteBuf, GovernmentUiFeedbackPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> ElarionPacketCodecs.writeString(buffer, payload.message(), MESSAGE_MAX),
            buffer -> new GovernmentUiFeedbackPayload(ElarionPacketCodecs.readString(buffer, MESSAGE_MAX)));

    public GovernmentUiFeedbackPayload {
        message = message == null ? "" : message.trim();
        if (message.length() > MESSAGE_MAX) message = message.substring(0, MESSAGE_MAX);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
