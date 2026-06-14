package panetina.elarion.core.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AcceptedCatchRecordTest {
    @Test
    void convertsTelemetryAndDefensivelyCopiesMetadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("method", "placeholder");
        CatchTelemetryEvent event = new CatchTelemetryEvent(
                UUID.randomUUID(),
                1_718_452_800_000L,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                2,
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "plains"),
                metadata);

        AcceptedCatchRecord record = AcceptedCatchRecord.from(event);
        metadata.put("changed", "later");

        assertEquals(AcceptedCatchRecord.CURRENT_SCHEMA_VERSION, record.schemaVersion());
        assertEquals(event.eventId(), record.eventId());
        assertEquals(event.occurredAt(), record.occurredAt());
        assertEquals(event.actorId(), record.actorId());
        assertEquals(Map.of("method", "placeholder"), record.metadata());
        assertThrows(UnsupportedOperationException.class, () -> record.metadata().put("new", "value"));
    }

    @Test
    void rejectsUnsupportedSchemaAndInvalidTelemetryFields() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Identifier sourceId = Identifier.of("elarion_angling", "fishing");
        Identifier fishId = Identifier.of("elarion_angling", "placeholder_fish_001");
        Identifier rarityId = Identifier.of("elarion_angling", "placeholder_common");

        assertThrows(IllegalArgumentException.class, () -> new AcceptedCatchRecord(
                2, eventId, 1, actorId, sourceId, fishId, rarityId, 1,
                null, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AcceptedCatchRecord(
                AcceptedCatchRecord.CURRENT_SCHEMA_VERSION, eventId, 0, actorId,
                sourceId, fishId, rarityId, 1, null, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AcceptedCatchRecord(
                AcceptedCatchRecord.CURRENT_SCHEMA_VERSION, eventId, 1, actorId,
                sourceId, fishId, rarityId, 0, null, null, null, Map.of()));
    }
}
