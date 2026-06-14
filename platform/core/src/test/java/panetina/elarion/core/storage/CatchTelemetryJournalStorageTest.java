package panetina.elarion.core.storage;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalCheckpoint;
import panetina.elarion.core.model.CatchJournalReplay;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchTelemetryJournalStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void appendSurvivesNewStorageInstanceAndPartitionsByPlayerAndMonth() throws IOException {
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();
        AcceptedCatchRecord january = record(
                UUID.randomUUID(), firstPlayer, "2026-01-31T23:59:59Z");
        AcceptedCatchRecord february = record(
                UUID.randomUUID(), firstPlayer, "2026-02-01T00:00:00Z");
        AcceptedCatchRecord otherPlayer = record(
                UUID.randomUUID(), secondPlayer, "2026-02-01T00:00:00Z");
        CatchTelemetryJournalStorage writer = new CatchTelemetryJournalStorage();

        writer.append(tempDir, january);
        writer.append(tempDir, february);
        writer.append(tempDir, otherPlayer);

        CatchTelemetryJournalStorage restarted = new CatchTelemetryJournalStorage();
        CatchJournalReplay replay = restarted.replay(
                tempDir, firstPlayer, CatchJournalCheckpoint.START, new HashMap<>(), 10);

        assertEquals(java.util.List.of(january, february), replay.records());
        assertEquals(new CatchJournalCheckpoint("2026-02", 1), replay.nextCheckpoint());
        assertEquals(2, replay.linesScanned());
        assertFalse(replay.hasMore());
        assertTrue(Files.exists(CatchTelemetryJournalCodec.journalPath(
                tempDir, secondPlayer, otherPlayer.occurredAt())));
    }

    @Test
    void boundedReplayContinuesFromCheckpointAndDeduplicatesAcrossPages() throws IOException {
        UUID actorId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        AcceptedCatchRecord first = record(duplicateId, actorId, "2026-01-10T00:00:00Z");
        AcceptedCatchRecord duplicate = first;
        AcceptedCatchRecord second = record(UUID.randomUUID(), actorId, "2026-02-10T00:00:00Z");
        CatchTelemetryJournalStorage storage = new CatchTelemetryJournalStorage();
        storage.append(tempDir, first);
        storage.append(tempDir, duplicate);
        storage.append(tempDir, second);
        Map<UUID, AcceptedCatchRecord> seen = new HashMap<>();

        CatchJournalReplay pageOne = storage.replay(
                tempDir, actorId, CatchJournalCheckpoint.START, seen, 1);
        CatchJournalReplay pageTwo = storage.replay(
                tempDir, actorId, pageOne.nextCheckpoint(), seen, 1);
        CatchJournalReplay pageThree = storage.replay(
                tempDir, actorId, pageTwo.nextCheckpoint(), seen, 1);

        assertEquals(java.util.List.of(first), pageOne.records());
        assertTrue(pageOne.hasMore());
        assertEquals(java.util.List.of(), pageTwo.records());
        assertTrue(pageTwo.hasMore());
        assertEquals(java.util.List.of(second), pageThree.records());
        assertFalse(pageThree.hasMore());
        assertEquals(Map.of(duplicateId, first, second.eventId(), second), seen);
    }

    @Test
    void corruptionFailsWithFileAndLineWithoutReturningPartialResults() throws IOException {
        UUID actorId = UUID.randomUUID();
        AcceptedCatchRecord valid = record(UUID.randomUUID(), actorId, "2026-03-10T00:00:00Z");
        CatchTelemetryJournalStorage storage = new CatchTelemetryJournalStorage();
        storage.append(tempDir, valid);
        Path file = CatchTelemetryJournalCodec.journalPath(tempDir, actorId, valid.occurredAt());
        Files.writeString(file, "{broken" + System.lineSeparator(),
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        Map<UUID, AcceptedCatchRecord> seen = new HashMap<>();

        CatchTelemetryFormatException failure = assertThrows(
                CatchTelemetryFormatException.class,
                () -> storage.replay(
                        tempDir, actorId, CatchJournalCheckpoint.START, seen, 10));

        assertTrue(failure.getMessage().contains(file.toString()));
        assertTrue(failure.getMessage().contains(":2"));
        assertTrue(seen.isEmpty());
    }

    @Test
    void conflictingDuplicateEventIdFailsClosed() throws IOException {
        UUID actorId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AcceptedCatchRecord first = record(eventId, actorId, "2026-03-10T00:00:00Z");
        AcceptedCatchRecord conflicting = record(eventId, actorId, "2026-03-11T00:00:00Z");
        CatchTelemetryJournalStorage storage = new CatchTelemetryJournalStorage();
        storage.append(tempDir, first);
        storage.append(tempDir, conflicting);
        Map<UUID, AcceptedCatchRecord> seen = new HashMap<>();

        assertThrows(CatchTelemetryFormatException.class, () ->
                storage.replay(tempDir, actorId, CatchJournalCheckpoint.START, seen, 10));
        assertTrue(seen.isEmpty());
    }

    @Test
    void actorMismatchFailsClosed() throws IOException {
        UUID partitionActor = UUID.randomUUID();
        AcceptedCatchRecord wrongActor = record(
                UUID.randomUUID(), UUID.randomUUID(), "2026-04-10T00:00:00Z");
        Path file = CatchTelemetryJournalCodec.journalPath(
                tempDir, partitionActor, wrongActor.occurredAt());
        Files.createDirectories(file.getParent());
        Files.writeString(file, CatchTelemetryJournalCodec.encode(wrongActor) + System.lineSeparator());

        assertThrows(CatchTelemetryFormatException.class, () ->
                new CatchTelemetryJournalStorage().replay(
                        tempDir, partitionActor, CatchJournalCheckpoint.START, new HashMap<>(), 10));
    }

    @Test
    void appendFailurePropagatesAndCreatesNoJournalRecord() throws IOException {
        Path invalidRoot = tempDir.resolve("not-a-directory");
        Files.writeString(invalidRoot, "occupied");
        AcceptedCatchRecord record = record(
                UUID.randomUUID(), UUID.randomUUID(), "2026-05-10T00:00:00Z");

        assertThrows(IOException.class, () ->
                new CatchTelemetryJournalStorage().append(invalidRoot, record));
        assertFalse(Files.exists(CatchTelemetryJournalCodec.journalPath(
                tempDir, record.actorId(), record.occurredAt())));
    }

    private static AcceptedCatchRecord record(UUID eventId, UUID actorId, String occurredAt) {
        return new AcceptedCatchRecord(
                AcceptedCatchRecord.CURRENT_SCHEMA_VERSION,
                eventId,
                Instant.parse(occurredAt).toEpochMilli(),
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
