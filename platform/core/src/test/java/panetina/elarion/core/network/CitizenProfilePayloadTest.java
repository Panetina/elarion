package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.profile.CitizenProfileCard;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;
import panetina.elarion.core.model.profile.ProfileVisibility;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.ElarionCollectionTab;
import panetina.elarion.core.service.CitizenProfileService;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CitizenProfilePayloadTest {
    private static final UUID TARGET = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void requestPayloadRoundTripsTargetAndSection() {
        CitizenProfileRequestPayload payload = new CitizenProfileRequestPayload(TARGET, " Core.Identity ");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CitizenProfileRequestPayload.CODEC.encode(buffer, payload);
        CitizenProfileRequestPayload decoded = CitizenProfileRequestPayload.CODEC.decode(buffer);

        assertEquals(TARGET, decoded.targetId());
        assertEquals("core.identity", decoded.sectionId());
    }

    @Test
    void snapshotPayloadRoundTripsProfileData() {
        CitizenProfileSnapshot snapshot = new CitizenProfileSnapshot(
                TARGET,
                "Mara",
                List.of(new CitizenProfileSection(
                        "core.identity",
                        "Identity",
                        "elarion_core",
                        ProfileVisibility.PUBLIC,
                        List.of(
                                new CitizenProfileField("display-name", "Name", "Mara", ProfileVisibility.PUBLIC),
                                new CitizenProfileField("citizen-id", "Citizen ID", TARGET.toString(), ProfileVisibility.SELF)),
                        List.of(new CitizenProfileCard("summary", "Summary", "Realm citizen.", ProfileVisibility.PUBLIC)))));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CitizenProfileSnapshotPayload.CODEC.encode(buffer, new CitizenProfileSnapshotPayload(snapshot));
        CitizenProfileSnapshot decoded = CitizenProfileSnapshotPayload.CODEC.decode(buffer).snapshot();

        assertEquals(snapshot, decoded);
    }

    @Test
    void combinedOpenPayloadRoundTripsCollectionAndFilteredProfile() {
        ElarionCollectionSnapshot collection = new ElarionCollectionSnapshot(
                "Character Menu", "Profile", "profile", "Viewing Mara",
                List.of(new ElarionCollectionTab("profile", "Profile", "", List.of())));
        CitizenProfileSnapshot profile = new CitizenProfileSnapshot(TARGET, "Mara", List.of());
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CitizenProfileOpenPayload.CODEC.encode(buffer, new CitizenProfileOpenPayload(collection, profile));
        CitizenProfileOpenPayload decoded = CitizenProfileOpenPayload.CODEC.decode(buffer);

        assertEquals(collection, decoded.collection());
        assertEquals(profile, decoded.profile());
    }

    @Test
    void snapshotPayloadCapsSectionsFieldsAndCardsBeforeEncoding() {
        List<CitizenProfileField> fields = IntStream.range(0, CitizenProfileService.MAX_FIELDS_PER_SECTION + 8)
                .mapToObj(index -> new CitizenProfileField(
                        "field-" + index,
                        "Field " + index,
                        "Value " + index,
                        ProfileVisibility.PUBLIC))
                .toList();
        List<CitizenProfileCard> cards = IntStream.range(0, CitizenProfileService.MAX_CARDS_PER_SECTION + 8)
                .mapToObj(index -> new CitizenProfileCard(
                        "card-" + index,
                        "Card " + index,
                        "Body " + index,
                        ProfileVisibility.PUBLIC))
                .toList();
        List<CitizenProfileSection> sections = IntStream.range(0, CitizenProfileService.MAX_SECTIONS + 4)
                .mapToObj(index -> new CitizenProfileSection(
                        "section-" + index,
                        "Section " + index,
                        "addon",
                        ProfileVisibility.PUBLIC,
                        index == 0 ? fields : List.of(),
                        index == 0 ? cards : List.of()))
                .toList();
        CitizenProfileSnapshot snapshot = new CitizenProfileSnapshot(TARGET, "Mara", sections);

        CitizenProfileSnapshot decoded = roundTrip(snapshot);

        assertEquals(CitizenProfileService.MAX_SECTIONS, decoded.sections().size());
        assertEquals(CitizenProfileService.MAX_FIELDS_PER_SECTION, decoded.sections().getFirst().fields().size());
        assertEquals(CitizenProfileService.MAX_CARDS_PER_SECTION, decoded.sections().getFirst().cards().size());
    }

    @Test
    void snapshotPayloadBoundsDisplayStrings() {
        CitizenProfileSnapshot snapshot = new CitizenProfileSnapshot(
                TARGET,
                "T".repeat(CitizenProfileSnapshotPayload.MAX_TITLE_LENGTH + 20),
                List.of(new CitizenProfileSection(
                        "core.identity",
                        "S".repeat(CitizenProfileSnapshotPayload.MAX_TITLE_LENGTH + 20),
                        "source".repeat(20),
                        ProfileVisibility.PUBLIC,
                        List.of(new CitizenProfileField(
                                "display-name",
                                "L".repeat(CitizenProfileSnapshotPayload.MAX_LABEL_LENGTH + 20),
                                "V".repeat(CitizenProfileSnapshotPayload.MAX_VALUE_LENGTH + 20),
                                ProfileVisibility.PUBLIC)),
                        List.of(new CitizenProfileCard(
                                "summary",
                                "C".repeat(CitizenProfileSnapshotPayload.MAX_TITLE_LENGTH + 20),
                                "B".repeat(CitizenProfileSnapshotPayload.MAX_BODY_LENGTH + 20),
                                ProfileVisibility.PUBLIC)))));

        CitizenProfileSnapshot decoded = roundTrip(snapshot);

        assertEquals(CitizenProfileSnapshotPayload.MAX_TITLE_LENGTH, decoded.title().length());
        CitizenProfileSection section = decoded.sections().getFirst();
        assertEquals(CitizenProfileSnapshotPayload.MAX_TITLE_LENGTH, section.title().length());
        assertEquals(CitizenProfileSnapshotPayload.MAX_SOURCE_LENGTH, section.sourceSystem().length());
        assertEquals(CitizenProfileSnapshotPayload.MAX_LABEL_LENGTH, section.fields().getFirst().label().length());
        assertEquals(CitizenProfileSnapshotPayload.MAX_VALUE_LENGTH, section.fields().getFirst().value().length());
        assertEquals(CitizenProfileSnapshotPayload.MAX_TITLE_LENGTH, section.cards().getFirst().title().length());
        assertEquals(CitizenProfileSnapshotPayload.MAX_BODY_LENGTH, section.cards().getFirst().body().length());
    }

    private static CitizenProfileSnapshot roundTrip(CitizenProfileSnapshot snapshot) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        CitizenProfileSnapshotPayload.CODEC.encode(buffer, new CitizenProfileSnapshotPayload(snapshot));
        return CitizenProfileSnapshotPayload.CODEC.decode(buffer).snapshot();
    }
}
