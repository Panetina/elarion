package panetina.elarion.addons.government.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GovernmentUiActionPayload(
        String screenType,
        String action,
        String realmId,
        String targetId,
        String value,
        String secondaryValue
) implements CustomPayload {
    public static final Id<GovernmentUiActionPayload> ID =
            new Id<>(Identifier.of("elarion_government", "government_ui_action"));

    public static final PacketCodec<PacketByteBuf, GovernmentUiActionPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeString(payload.screenType());
                buffer.writeString(payload.action());
                buffer.writeString(payload.realmId());
                buffer.writeString(payload.targetId());
                buffer.writeString(payload.value());
                buffer.writeString(payload.secondaryValue());
            },
            buffer -> new GovernmentUiActionPayload(
                    buffer.readString(64),
                    buffer.readString(64),
                    buffer.readString(128),
                    buffer.readString(256),
                    buffer.readString(512),
                    buffer.readString(256)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
