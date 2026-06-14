package panetina.elarion.core.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalCheckpoint;
import panetina.elarion.core.model.CatchSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchSummaryStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void missingSnapshotLoadsEmptyAndSavedSnapshotSurvivesRestart() throws IOException {
        UUID actorId = UUID.randomUUID();
        CatchSummaryStorage storage = new CatchSummaryStorage();

        assertEquals(CatchSummary.empty(actorId), storage.load(tempDir, actorId));

        CatchSummary summary = summary(actorId);
        storage.save(tempDir, summary);
        CatchSummary loaded = new CatchSummaryStorage().load(tempDir, actorId);

        assertEquals(summary, loaded);
        Path file = CatchSummaryStorage.summaryPath(tempDir, actorId);
        assertTrue(Files.exists(file));
        assertFalse(Files.exists(file.resolveSibling(file.getFileName() + ".tmp")));
    }

    @Test
    void corruptSnapshotFailsInsteadOfResettingTotals() throws IOException {
        UUID actorId = UUID.randomUUID();
        CatchSummaryStorage storage = new CatchSummaryStorage();
        storage.save(tempDir, summary(actorId));
        Path file = CatchSummaryStorage.summaryPath(tempDir, actorId);
        Files.writeString(file, "{\"schemaVersion\":1}");

        CatchTelemetryFormatException failure = assertThrows(
                CatchTelemetryFormatException.class,
                () -> storage.load(tempDir, actorId));

        assertTrue(failure.getMessage().contains(file.toString()));
    }

    @Test
    void snapshotActorMustMatchPath() throws IOException {
        UUID requestedActor = UUID.randomUUID();
        CatchSummary other = summary(UUID.randomUUID());
        Path file = CatchSummaryStorage.summaryPath(tempDir, requestedActor);
        Files.createDirectories(file.getParent());
        Files.writeString(file, CatchSummaryCodec.encode(other).toString());

        assertThrows(CatchTelemetryFormatException.class, () ->
                new CatchSummaryStorage().load(tempDir, requestedActor));
    }

    @Test
    void codecRejectsInconsistentCountsAndInvalidRecentRecord() {
        CatchSummary summary = summary(UUID.randomUUID());
        String json = CatchSummaryCodec.encode(summary).toString();

        assertThrows(CatchTelemetryFormatException.class, () ->
                CatchSummaryCodec.decode("bad-counts",
                        json.replace("\"totalQuantity\":2", "\"totalQuantity\":3")));
        JsonObject badRecent = JsonParser.parseString(json).getAsJsonObject();
        badRecent.getAsJsonArray("recentCatches").get(0).getAsJsonObject()
                .addProperty("actorId", UUID.randomUUID().toString());
        assertThrows(CatchTelemetryFormatException.class, () ->
                CatchSummaryCodec.decode("bad-recent", badRecent.toString()));
    }

    private static CatchSummary summary(UUID actorId) {
        AcceptedCatchRecord record = record(actorId);
        return new CatchSummary(
                CatchSummary.CURRENT_SCHEMA_VERSION,
                actorId,
                2,
                Map.of(record.sourceId(), 2L),
                Map.of(record.fishDefinitionId(), 2L),
                Map.of(record.rarityId(), 2L),
                record.occurredAt(),
                record.occurredAt(),
                new CatchJournalCheckpoint("2026-06", 1),
                List.of(record));
    }

    private static AcceptedCatchRecord record(UUID actorId) {
        return new AcceptedCatchRecord(
                AcceptedCatchRecord.CURRENT_SCHEMA_VERSION,
                UUID.randomUUID(),
                Instant.parse("2026-06-12T10:15:30Z").toEpochMilli(),
                actorId,
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                2,
                null,
                null,
                null,
                Map.of());
    }
}
