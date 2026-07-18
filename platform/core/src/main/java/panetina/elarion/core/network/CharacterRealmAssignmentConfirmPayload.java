package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CharacterRealmAssignmentConfirmPayload() implements CustomPayload {
    public static final CharacterRealmAssignmentConfirmPayload INSTANCE = new CharacterRealmAssignmentConfirmPayload();
    public static final Id<CharacterRealmAssignmentConfirmPayload> ID =
            new Id<>(Identifier.of("elarion_core", "character_realm_assignment_confirm"));
    public static final PacketCodec<PacketByteBuf, CharacterRealmAssignmentConfirmPayload> CODEC =
            PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
