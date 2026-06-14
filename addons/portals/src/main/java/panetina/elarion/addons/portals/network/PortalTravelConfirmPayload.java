package panetina.elarion.addons.portals.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.model.PortalTravelDirection;

public record PortalTravelConfirmPayload(
        String routeId, PortalTravelDirection direction
) implements CustomPayload {
    public static final Id<PortalTravelConfirmPayload> ID =
            new Id<>(Identifier.of("elarion_portals", "travel_confirm"));
    public static final PacketCodec<PacketByteBuf, PortalTravelConfirmPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeString(payload.routeId());
                buffer.writeEnumConstant(payload.direction());
            },
            buffer -> new PortalTravelConfirmPayload(
                    buffer.readString(128), buffer.readEnumConstant(PortalTravelDirection.class)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
