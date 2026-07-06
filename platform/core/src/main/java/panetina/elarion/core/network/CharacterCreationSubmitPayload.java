package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CharacterCreationSubmitPayload(String nonce, String displayName, String biography)
        implements CustomPayload {
    public static final Id<CharacterCreationSubmitPayload> ID =
            new Id<>(Identifier.of("elarion_core", "character_creation_submit"));
    public static final PacketCodec<PacketByteBuf, CharacterCreationSubmitPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.nonce(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.displayName(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.biography(), 500);
            },
            buffer -> new CharacterCreationSubmitPayload(
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 500)));

    public CharacterCreationSubmitPayload {
        nonce = nonce == null ? "" : nonce;
        displayName = displayName == null ? "" : displayName;
        biography = biography == null ? "" : biography;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
