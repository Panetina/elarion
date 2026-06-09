package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record IdentitySyncRequestPayload(boolean requested) implements CustomPayload {
    public static final IdentitySyncRequestPayload INSTANCE = new IdentitySyncRequestPayload(true);
    public static final Id<IdentitySyncRequestPayload> ID =
            new Id<>(Identifier.of("elarion_core", "identity_sync_request"));

    public static final PacketCodec<PacketByteBuf, IdentitySyncRequestPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> buffer.writeBoolean(payload.requested()),
            buffer -> new IdentitySyncRequestPayload(buffer.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
