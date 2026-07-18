package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.profile.CitizenProfileCard;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;
import panetina.elarion.core.model.profile.ProfileVisibility;
import panetina.elarion.core.service.CitizenProfileService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CitizenProfileSnapshotPayload(CitizenProfileSnapshot snapshot) implements CustomPayload {
    public static final Id<CitizenProfileSnapshotPayload> ID =
            new Id<>(Identifier.of("elarion_core", "citizen_profile_snapshot"));
    public static final int MAX_SECTION_ID_LENGTH = 96;
    public static final int MAX_FIELD_ID_LENGTH = 96;
    public static final int MAX_CARD_ID_LENGTH = 96;
    public static final int MAX_TITLE_LENGTH = 128;
    public static final int MAX_LABEL_LENGTH = 128;
    public static final int MAX_VALUE_LENGTH = 512;
    public static final int MAX_BODY_LENGTH = 1024;
    public static final int MAX_SOURCE_LENGTH = 64;
    public static final PacketCodec<PacketByteBuf, CitizenProfileSnapshotPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> writeSnapshot(payload.snapshot(), buffer),
            buffer -> new CitizenProfileSnapshotPayload(readSnapshot(buffer)));

    static void writeSnapshot(CitizenProfileSnapshot snapshot, PacketByteBuf buffer) {
        CitizenProfileSnapshot wire = wireSnapshot(snapshot);
        buffer.writeUuid(wire.targetId());
        ElarionPacketCodecs.writeString(buffer, wire.title(), MAX_TITLE_LENGTH);
        buffer.writeVarInt(wire.sections().size());
        wire.sections().forEach(section -> writeSection(buffer, section));
    }

    static CitizenProfileSnapshot readSnapshot(PacketByteBuf buffer) {
        UUID targetId = buffer.readUuid();
        String title = ElarionPacketCodecs.readString(buffer, MAX_TITLE_LENGTH);
        int count = ElarionPacketCodecs.readBoundedCount(buffer, CitizenProfileService.MAX_SECTIONS);
        List<CitizenProfileSection> sections = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            sections.add(readSection(buffer));
        }
        return new CitizenProfileSnapshot(targetId, title, sections);
    }

    private static void writeSection(PacketByteBuf buffer, CitizenProfileSection section) {
        ElarionPacketCodecs.writeString(buffer, section.id(), MAX_SECTION_ID_LENGTH);
        ElarionPacketCodecs.writeString(buffer, section.title(), MAX_TITLE_LENGTH);
        ElarionPacketCodecs.writeString(buffer, section.sourceSystem(), MAX_SOURCE_LENGTH);
        buffer.writeEnumConstant(section.visibility());
        buffer.writeVarInt(section.fields().size());
        section.fields().forEach(field -> writeField(buffer, field));
        buffer.writeVarInt(section.cards().size());
        section.cards().forEach(card -> writeCard(buffer, card));
    }

    private static CitizenProfileSection readSection(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, MAX_SECTION_ID_LENGTH);
        String title = ElarionPacketCodecs.readString(buffer, MAX_TITLE_LENGTH);
        String source = ElarionPacketCodecs.readString(buffer, MAX_SOURCE_LENGTH);
        ProfileVisibility visibility = ElarionPacketCodecs.readEnumOrDefault(
                buffer, ProfileVisibility.class, ProfileVisibility.PUBLIC);
        int fieldCount = ElarionPacketCodecs.readBoundedCount(buffer, CitizenProfileService.MAX_FIELDS_PER_SECTION);
        List<CitizenProfileField> fields = new ArrayList<>(fieldCount);
        for (int index = 0; index < fieldCount; index++) {
            fields.add(readField(buffer));
        }
        int cardCount = ElarionPacketCodecs.readBoundedCount(buffer, CitizenProfileService.MAX_CARDS_PER_SECTION);
        List<CitizenProfileCard> cards = new ArrayList<>(cardCount);
        for (int index = 0; index < cardCount; index++) {
            cards.add(readCard(buffer));
        }
        return new CitizenProfileSection(id, title, source, visibility, fields, cards);
    }

    private static void writeField(PacketByteBuf buffer, CitizenProfileField field) {
        ElarionPacketCodecs.writeString(buffer, field.id(), MAX_FIELD_ID_LENGTH);
        ElarionPacketCodecs.writeString(buffer, field.label(), MAX_LABEL_LENGTH);
        ElarionPacketCodecs.writeString(buffer, field.value(), MAX_VALUE_LENGTH);
        buffer.writeEnumConstant(field.visibility());
    }

    private static CitizenProfileField readField(PacketByteBuf buffer) {
        return new CitizenProfileField(
                ElarionPacketCodecs.readString(buffer, MAX_FIELD_ID_LENGTH),
                ElarionPacketCodecs.readString(buffer, MAX_LABEL_LENGTH),
                ElarionPacketCodecs.readString(buffer, MAX_VALUE_LENGTH),
                ElarionPacketCodecs.readEnumOrDefault(buffer, ProfileVisibility.class, ProfileVisibility.PUBLIC));
    }

    private static void writeCard(PacketByteBuf buffer, CitizenProfileCard card) {
        ElarionPacketCodecs.writeString(buffer, card.id(), MAX_CARD_ID_LENGTH);
        ElarionPacketCodecs.writeString(buffer, card.title(), MAX_TITLE_LENGTH);
        ElarionPacketCodecs.writeString(buffer, card.body(), MAX_BODY_LENGTH);
        buffer.writeEnumConstant(card.visibility());
    }

    private static CitizenProfileCard readCard(PacketByteBuf buffer) {
        return new CitizenProfileCard(
                ElarionPacketCodecs.readString(buffer, MAX_CARD_ID_LENGTH),
                ElarionPacketCodecs.readString(buffer, MAX_TITLE_LENGTH),
                ElarionPacketCodecs.readString(buffer, MAX_BODY_LENGTH),
                ElarionPacketCodecs.readEnumOrDefault(buffer, ProfileVisibility.class, ProfileVisibility.PUBLIC));
    }

    static CitizenProfileSnapshot wireSnapshot(CitizenProfileSnapshot snapshot) {
        if (snapshot == null) return new CitizenProfileSnapshot(new UUID(0L, 0L), "", List.of());
        List<CitizenProfileSection> sections = new ArrayList<>();
        for (CitizenProfileSection section : snapshot.sections()) {
            if (sections.size() >= CitizenProfileService.MAX_SECTIONS) break;
            if (section == null) continue;
            sections.add(new CitizenProfileSection(
                    section.id(),
                    section.title(),
                    section.sourceSystem(),
                    section.visibility(),
                    bounded(section.fields(), CitizenProfileService.MAX_FIELDS_PER_SECTION),
                    bounded(section.cards(), CitizenProfileService.MAX_CARDS_PER_SECTION)));
        }
        return new CitizenProfileSnapshot(snapshot.targetId(), snapshot.title(), sections);
    }

    private static <T> List<T> bounded(List<T> values, int max) {
        if (values == null || values.isEmpty()) return List.of();
        return List.copyOf(values.stream()
                .filter(value -> value != null)
                .limit(max)
                .toList());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
