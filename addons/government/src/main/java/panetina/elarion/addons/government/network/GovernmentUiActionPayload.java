package panetina.elarion.addons.government.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record GovernmentUiActionPayload(
        String screenType,
        String action,
        String realmId,
        String targetId,
        String value,
        String secondaryValue,
        String sessionId
) implements CustomPayload {
    public static final Id<GovernmentUiActionPayload> ID =
            new Id<>(Identifier.of("elarion_government", "government_ui_action"));

    public static final PacketCodec<PacketByteBuf, GovernmentUiActionPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.screenType(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.action(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.realmId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.targetId(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.value(), 512);
                ElarionPacketCodecs.writeString(buffer, payload.secondaryValue(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.sessionId(), 64);
            },
            buffer -> new GovernmentUiActionPayload(
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 512),
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 64)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
