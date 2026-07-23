package panetina.elarion.core.storage;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchTelemetryDetails;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchTelemetryJournalCodecTest {
    @TempDir
    Path tempDir;

    @Test
    void recordRoundTripsWithStableIdentifierStrings() {
        AcceptedCatchRecord original = record(
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "plains"));

        String json = CatchTelemetryJournalCodec.encode(original);
        AcceptedCatchRecord decoded = CatchTelemetryJournalCodec.decode("line-1", json);

        assertEquals(original, decoded);
        assertTrue(json.contains("\"sourceId\":\"elarion_angling:fishing\""));
        assertTrue(json.contains("\"fishDefinitionId\":\"elarion_angling:placeholder_fish_001\""));
    }

    @Test
    void richServerOutcomeRoundTrips() {
        AcceptedCatchRecord base = record(null, Identifier.ofVanilla("overworld"), Identifier.ofVanilla("plains"));
        CatchTelemetryDetails details = new CatchTelemetryDetails(
                Identifier.of("elarion_angling", "aloe_bream"),
                Identifier.of("elarion_angling", "fish"),
                420, 2_100, 275, 84, true, true, false, 7,
                Identifier.of("elarion_angling", "worm"),
                Identifier.of("elarion_angling", "fishing_rod"),
                Identifier.of("elarion_angling", "valley_bobber"),
                Identifier.of("elarion_angling", "copper_hook"),
                Identifier.ofVanilla("water"), null, null);
        AcceptedCatchRecord rich = new AcceptedCatchRecord(
                base.schemaVersion(), base.eventId(), base.occurredAt(), base.actorId(), base.sourceId(),
                base.fishDefinitionId(), base.rarityId(), base.quantity(), base.worldId(), base.dimensionId(),
                base.biomeId(), base.metadata(), details);

        assertEquals(rich, CatchTelemetryJournalCodec.decode("rich", CatchTelemetryJournalCodec.encode(rich)));
    }

    @Test
    void absentLocationsRoundTripAsNull() {
        AcceptedCatchRecord decoded = CatchTelemetryJournalCodec.decode(
                "line-2",
                CatchTelemetryJournalCodec.encode(record(null, null, null)));

        assertNull(decoded.worldId());
        assertNull(decoded.dimensionId());
        assertNull(decoded.biomeId());
    }

    @Test
    void schemaOneJournalRecordMigratesToCurrentSchema() {
        AcceptedCatchRecord current = record(null, null, null);
        String legacy = CatchTelemetryJournalCodec.encode(current)
                .replace("\"schemaVersion\":" + AcceptedCatchRecord.CURRENT_SCHEMA_VERSION, "\"schemaVersion\":1");

        assertEquals(current, CatchTelemetryJournalCodec.decode("legacy", legacy));
    }

    @Test
    void metadataEncodingIsKeySorted() {
        AcceptedCatchRecord base = record(null, null, null);
        AcceptedCatchRecord record = new AcceptedCatchRecord(
                base.schemaVersion(),
                base.eventId(),
                base.occurredAt(),
                base.actorId(),
                base.sourceId(),
                base.fishDefinitionId(),
                base.rarityId(),
                base.quantity(),
                null,
                null,
                null,
                Map.of("zeta", "last", "alpha", "first"));

        String json = CatchTelemetryJournalCodec.encode(record);

        assertTrue(json.indexOf("\"alpha\"") < json.indexOf("\"zeta\""));
    }

    @Test
    void rejectsMalformedOrUnsupportedRecordsWithDocumentContext() {
        CatchTelemetryFormatException invalidRoot = assertThrows(
                CatchTelemetryFormatException.class,
                () -> CatchTelemetryJournalCodec.decode("journal.jsonl:4", "[]"));
        assertTrue(invalidRoot.getMessage().contains("journal.jsonl:4"));

        String valid = CatchTelemetryJournalCodec.encode(record(null, null, null));
        assertThrows(CatchTelemetryFormatException.class, () ->
                CatchTelemetryJournalCodec.decode("missing-field", valid.replaceFirst("\"eventId\"", "\"removed\"")));
        assertThrows(CatchTelemetryFormatException.class, () ->
                CatchTelemetryJournalCodec.decode("invalid-id",
                        valid.replace("elarion_angling:fishing", "invalid id")));
        assertThrows(CatchTelemetryFormatException.class, () ->
                CatchTelemetryJournalCodec.decode("unsupported-schema",
                        valid.replace("\"schemaVersion\":" + AcceptedCatchRecord.CURRENT_SCHEMA_VERSION,
                                "\"schemaVersion\":" + (AcceptedCatchRecord.CURRENT_SCHEMA_VERSION + 1))));
        assertThrows(CatchTelemetryFormatException.class, () ->
                CatchTelemetryJournalCodec.decode("fractional-quantity",
                        valid.replace("\"quantity\":2", "\"quantity\":2.5")));
    }

    @Test
    void journalPathUsesActorAndUtcOccurrenceMonth() {
        UUID actorId = UUID.randomUUID();
        long occurredAt = Instant.parse("2026-01-31T23:59:59Z").toEpochMilli();

        Path path = CatchTelemetryJournalCodec.journalPath(tempDir, actorId, occurredAt);

        assertEquals(
                tempDir.resolve("catch-telemetry")
                        .resolve("journal")
                        .resolve(actorId.toString())
                        .resolve("2026-01.jsonl"),
                path);
        assertThrows(IllegalArgumentException.class,
                () -> CatchTelemetryJournalCodec.journalPath(tempDir, actorId, 0));
    }

    private static AcceptedCatchRecord record(
            Identifier worldId,
            Identifier dimensionId,
            Identifier biomeId
    ) {
        return new AcceptedCatchRecord(
                AcceptedCatchRecord.CURRENT_SCHEMA_VERSION,
                UUID.fromString("05cb02bb-12ce-4e90-8c98-f0d02b6feaef"),
                Instant.parse("2026-06-12T10:15:30Z").toEpochMilli(),
                UUID.fromString("2549af1a-2118-42fb-a2d2-165b6e739451"),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                2,
                worldId,
                dimensionId,
                biomeId,
                Map.of("method", "placeholder"));
    }
}
