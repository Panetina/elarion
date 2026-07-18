package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CharacterCreationPayloadTest {
    @Test
    void requirementCodecRoundTrips() {
        CharacterCreationRequirementPayload payload = new CharacterCreationRequirementPayload(
                true, "nonce", "CREATION_REQUIRED", 123L, "Matie", "A short history.", "");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        CharacterCreationRequirementPayload.CODEC.encode(buffer, payload);
        assertEquals(payload, CharacterCreationRequirementPayload.CODEC.decode(buffer));
    }

    @Test
    void requirementCodecClampsLongStrings() {
        String longText = "x".repeat(800);
        CharacterCreationRequirementPayload payload = new CharacterCreationRequirementPayload(
                true, longText, longText, 123L, longText, longText, longText);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CharacterCreationRequirementPayload.CODEC.encode(buffer, payload);
        CharacterCreationRequirementPayload decoded = CharacterCreationRequirementPayload.CODEC.decode(buffer);

        assertEquals(64, decoded.nonce().length());
        assertEquals(48, decoded.status().length());
        assertEquals(64, decoded.prefilledName().length());
        assertEquals(500, decoded.prefilledBiography().length());
        assertEquals(512, decoded.feedback().length());
    }

    @Test
    void statusRequestCodecIsUnitPayload() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CharacterCreationStatusRequestPayload.CODEC.encode(buffer, CharacterCreationStatusRequestPayload.INSTANCE);

        assertEquals(CharacterCreationStatusRequestPayload.INSTANCE,
                CharacterCreationStatusRequestPayload.CODEC.decode(buffer));
    }

    @Test
    void realmAssignmentConfirmCodecIsUnitPayload() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CharacterRealmAssignmentConfirmPayload.CODEC.encode(buffer, CharacterRealmAssignmentConfirmPayload.INSTANCE);

        assertEquals(CharacterRealmAssignmentConfirmPayload.INSTANCE,
                CharacterRealmAssignmentConfirmPayload.CODEC.decode(buffer));
    }

    @Test
    void submissionCodecRoundTrips() {
        CharacterCreationSubmitPayload payload = new CharacterCreationSubmitPayload(
                "nonce", "Matie", "A short history.");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        CharacterCreationSubmitPayload.CODEC.encode(buffer, payload);
        assertEquals(payload, CharacterCreationSubmitPayload.CODEC.decode(buffer));
    }

    @Test
    void realmAssignmentCodecRoundTrips() {
        CharacterRealmAssignmentPayload payload = new CharacterRealmAssignmentPayload(
                "realm3", "Wilderness III",
                List.of(
                        new CharacterRealmAssignmentPayload.Option("realm1", "Wilderness I", 4, false),
                        new CharacterRealmAssignmentPayload.Option("realm2", "Wilderness II", 5, false),
                        new CharacterRealmAssignmentPayload.Option("realm3", "Wilderness III", 4, true)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        CharacterRealmAssignmentPayload.CODEC.encode(buffer, payload);
        assertEquals(payload, CharacterRealmAssignmentPayload.CODEC.decode(buffer));
    }
}
