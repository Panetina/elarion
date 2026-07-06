package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public enum CollectionOpenRequestPayload implements CustomPayload {
    INSTANCE;

    public static final Id<CollectionOpenRequestPayload> ID =
            new Id<>(Identifier.of("elarion_core", "collection_open_request"));
    public static final PacketCodec<PacketByteBuf, CollectionOpenRequestPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
