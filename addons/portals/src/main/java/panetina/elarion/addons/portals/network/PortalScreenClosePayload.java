package panetina.elarion.addons.portals.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PortalScreenClosePayload() implements CustomPayload {
    public static final PortalScreenClosePayload INSTANCE = new PortalScreenClosePayload();
    public static final Id<PortalScreenClosePayload> ID =
            new Id<>(Identifier.of("elarion_portals", "screen_close"));
    public static final PacketCodec<RegistryByteBuf, PortalScreenClosePayload> CODEC =
            PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
