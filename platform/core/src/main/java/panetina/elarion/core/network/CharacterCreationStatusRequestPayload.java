package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CharacterCreationStatusRequestPayload() implements CustomPayload {
    public static final CharacterCreationStatusRequestPayload INSTANCE = new CharacterCreationStatusRequestPayload();
    public static final Id<CharacterCreationStatusRequestPayload> ID =
            new Id<>(Identifier.of("elarion_core", "character_creation_status_request"));
    public static final PacketCodec<PacketByteBuf, CharacterCreationStatusRequestPayload> CODEC =
            PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
