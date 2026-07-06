package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CharacterCreationRequirementPayload(
        boolean required,
        String nonce,
        String status,
        long eligibleAt,
        String prefilledName,
        String prefilledBiography,
        String feedback
) implements CustomPayload {
    public static final Id<CharacterCreationRequirementPayload> ID =
            new Id<>(Identifier.of("elarion_core", "character_creation_requirement"));
    public static final PacketCodec<PacketByteBuf, CharacterCreationRequirementPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeBoolean(payload.required());
                ElarionPacketCodecs.writeString(buffer, payload.nonce(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.status(), 48);
                buffer.writeLong(payload.eligibleAt());
                ElarionPacketCodecs.writeString(buffer, payload.prefilledName(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.prefilledBiography(), 500);
                ElarionPacketCodecs.writeString(buffer, payload.feedback(), 512);
            },
            buffer -> new CharacterCreationRequirementPayload(
                    buffer.readBoolean(), ElarionPacketCodecs.readString(buffer, 64),
                    ElarionPacketCodecs.readString(buffer, 48), buffer.readLong(),
                    ElarionPacketCodecs.readString(buffer, 64), ElarionPacketCodecs.readString(buffer, 500),
                    ElarionPacketCodecs.readString(buffer, 512)));

    public CharacterCreationRequirementPayload {
        nonce = clean(nonce);
        status = clean(status);
        prefilledName = clean(prefilledName);
        prefilledBiography = clean(prefilledBiography);
        feedback = clean(feedback);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
