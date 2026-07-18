package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record CitizenProfileRequestPayload(
        UUID targetId,
        String sectionId
) implements CustomPayload {
    public static final Id<CitizenProfileRequestPayload> ID =
            new Id<>(Identifier.of("elarion_core", "citizen_profile_request"));
    public static final int MAX_SECTION_ID_LENGTH = 96;
    public static final PacketCodec<PacketByteBuf, CitizenProfileRequestPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.targetId());
                ElarionPacketCodecs.writeString(buffer, payload.sectionId(), MAX_SECTION_ID_LENGTH);
            },
            buffer -> new CitizenProfileRequestPayload(
                    buffer.readUuid(),
                    ElarionPacketCodecs.readString(buffer, MAX_SECTION_ID_LENGTH)));

    public CitizenProfileRequestPayload {
        if (targetId == null) targetId = new UUID(0L, 0L);
        sectionId = sectionId == null ? "" : sectionId.trim().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
