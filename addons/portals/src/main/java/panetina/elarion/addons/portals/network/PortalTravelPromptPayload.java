package panetina.elarion.addons.portals.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.model.PortalTravelDirection;

public record PortalTravelPromptPayload(
        String routeId,
        String gateName,
        PortalTravelDirection direction,
        long closesAt,
        String iconItem,
        String requirement,
        int requirementColor,
        boolean allowed,
        String message,
        String themeVariant,
        int logicalWidth,
        int logicalHeight,
        int minimumScalePercent,
        int confirmButtonWidth,
        int closeButtonWidth
) implements CustomPayload {
    public static final Id<PortalTravelPromptPayload> ID =
            new Id<>(Identifier.of("elarion_portals", "travel_prompt"));
    public static final PacketCodec<PacketByteBuf, PortalTravelPromptPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeString(payload.routeId());
                buffer.writeString(payload.gateName());
                buffer.writeEnumConstant(payload.direction());
                buffer.writeLong(payload.closesAt());
                buffer.writeString(payload.iconItem());
                buffer.writeString(payload.requirement());
                buffer.writeInt(payload.requirementColor());
                buffer.writeBoolean(payload.allowed());
                buffer.writeString(payload.message());
                buffer.writeString(payload.themeVariant());
                buffer.writeVarInt(payload.logicalWidth());
                buffer.writeVarInt(payload.logicalHeight());
                buffer.writeVarInt(payload.minimumScalePercent());
                buffer.writeVarInt(payload.confirmButtonWidth());
                buffer.writeVarInt(payload.closeButtonWidth());
            },
            buffer -> new PortalTravelPromptPayload(
                    buffer.readString(128), buffer.readString(256),
                    buffer.readEnumConstant(PortalTravelDirection.class), buffer.readLong(),
                    buffer.readString(256), buffer.readString(1024),
                    buffer.readInt(),
                    buffer.readBoolean(), buffer.readString(1024),
                    buffer.readString(64), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
