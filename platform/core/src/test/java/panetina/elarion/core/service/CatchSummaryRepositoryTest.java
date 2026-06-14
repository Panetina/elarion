package panetina.elarion.core.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalCheckpoint;
import panetina.elarion.core.model.CatchJournalReplay;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.CatchTelemetryJournalStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchSummaryRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void projectionMarksDirtyAtomicSaveClearsDirtyAndRestartLoadsSnapshot() throws IOException {
        UUID actorId = UUID.randomUUID();
        AcceptedCatchRecord record = record(actorId);
        CatchSummaryRepository repository = new CatchSummaryRepository(
                tempDir, new CatchSummaryStorage());

        CatchSummary updated = repository.apply(
                actorId,
                new CatchJournalReplay(
                        List.of(record),
                        new CatchJournalCheckpoint("2026-06", 1),
                        1,
                        false));

        assertEquals(1, updated.totalQuantity());
        assertTrue(repository.dirtyActors().contains(actorId));

        repository.saveDirty();

        assertFalse(repository.dirtyActors().contains(actorId));
        CatchSummary restarted = new CatchSummaryRepository(
                tempDir, new CatchSummaryStorage()).get(actorId);
        assertEquals(updated, restarted);
    }

    @Test
    void duplicateOnlyCheckpointProgressIsDirtyAndPersistent() throws IOException {
        UUID actorId = UUID.randomUUID();
        CatchSummaryRepository repository = new CatchSummaryRepository(
                tempDir, new CatchSummaryStorage());
        CatchJournalCheckpoint checkpoint = new CatchJournalCheckpoint("2026-06", 4);

        CatchSummary updated = repository.apply(
                actorId,
                new CatchJournalReplay(List.of(), checkpoint, 4, false));
        repository.save(actorId);

        assertEquals(checkpoint, updated.checkpoint());
        assertEquals(checkpoint, new CatchSummaryStorage().load(tempDir, actorId).checkpoint());
    }

    @Test
    void failedSaveLeavesPlayerDirtyForRetry() throws IOException {
        Path invalidRoot = tempDir.resolve("occupied");
        Files.writeString(invalidRoot, "not a directory");
        UUID actorId = UUID.randomUUID();
        CatchSummaryRepository repository = new CatchSummaryRepository(
                invalidRoot, new CatchSummaryStorage());
        repository.apply(
                actorId,
                new CatchJournalReplay(
                        List.of(record(actorId)),
                        new CatchJournalCheckpoint("2026-06", 1),
                        1,
                        false));

        assertThrows(IOException.class, repository::saveDirty);
        assertTrue(repository.dirtyActors().contains(actorId));
    }

    @Test
    void loadingOnePlayerDoesNotScanOrParseOtherPlayerSnapshots() throws IOException {
        UUID requestedActor = UUID.randomUUID();
        UUID corruptActor = UUID.randomUUID();
        Path corrupt = CatchSummaryStorage.summaryPath(tempDir, corruptActor);
        Files.createDirectories(corrupt.getParent());
        Files.writeString(corrupt, "{broken");

        CatchSummary loaded = new CatchSummaryRepository(
                tempDir, new CatchSummaryStorage()).get(requestedActor);

        assertEquals(CatchSummary.empty(requestedActor), loaded);
    }

    @Test
    void restartReplaysOnlyAfterPersistedCheckpoint() throws IOException {
        UUID actorId = UUID.randomUUID();
        AcceptedCatchRecord first = record(actorId);
        AcceptedCatchRecord second = new AcceptedCatchRecord(
                first.schemaVersion(),
                UUID.randomUUID(),
                first.occurredAt() + 1,
                actorId,
                first.sourceId(),
                first.fishDefinitionId(),
                first.rarityId(),
                2,
                null,
                null,
                null,
                Map.of());
        CatchTelemetryJournalStorage journal = new CatchTelemetryJournalStorage();
        journal.append(tempDir, first);

        CatchSummaryRepository beforeRestart = new CatchSummaryRepository(
                tempDir, new CatchSummaryStorage());
        Map<UUID, AcceptedCatchRecord> seen = new HashMap<>();
        var firstReplay = journal.replay(
                tempDir, actorId, CatchJournalCheckpoint.START, seen, 10);
        beforeRestart.apply(actorId, firstReplay);
        beforeRestart.saveDirty();

        journal.append(tempDir, second);

        CatchSummaryRepository afterRestart = new CatchSummaryRepository(
                tempDir, new CatchSummaryStorage());
        CatchSummary persisted = afterRestart.get(actorId);
        var secondReplay = new CatchTelemetryJournalStorage().replay(
                tempDir, actorId, persisted.checkpoint(), new HashMap<>(), 10);
        CatchSummary completed = afterRestart.apply(actorId, secondReplay);

        assertEquals(3, completed.totalQuantity());
        assertEquals(3, completed.quantityForRarity(first.rarityId()));
        assertEquals(new CatchJournalCheckpoint("2026-06", 2), completed.checkpoint());
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
                1,
                null,
                null,
                null,
                Map.of());
    }
}
