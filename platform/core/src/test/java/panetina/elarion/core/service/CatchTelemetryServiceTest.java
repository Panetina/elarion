package panetina.elarion.core.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.CatchTelemetryJournalStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchTelemetryServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void eventBusAcceptanceAppendsProjectsAndShutdownPersists() throws IOException {
        ElarionEventBus events = new ElarionEventBus();
        CatchTelemetryService service = service();
        service.registerEvents(events);
        service.bind(tempDir);
        CatchTelemetryEvent event = event(UUID.randomUUID(), UUID.randomUUID(), 2);

        events.emitCatchTelemetry(event);

        CatchSummary summary = service.summary(event.actorId());
        assertEquals(2, summary.totalQuantity());
        assertEquals(2, summary.quantityForFishDefinition(event.fishDefinitionId()));
        service.shutdown();
        assertEquals(summary, new CatchSummaryStorage().load(tempDir, event.actorId()));
    }

    @Test
    void journalAppendFailureDoesNotCreateOrMutateSummary() throws IOException {
        Path invalidRoot = tempDir.resolve("occupied");
        Files.writeString(invalidRoot, "not a directory");
        CatchTelemetryService service = service();
        service.bind(invalidRoot);
        CatchTelemetryEvent event = event(UUID.randomUUID(), UUID.randomUUID(), 1);

        assertThrows(UncheckedIOException.class, () -> service.accept(event));

        assertFalse(Files.exists(CatchSummaryStorage.summaryPath(tempDir, event.actorId())));
    }

    @Test
    void replayWorkRemainsPageBoundedAndContinuesOnLaterCalls() throws IOException {
        UUID actorId = UUID.randomUUID();
        CatchTelemetryJournalStorage journal = new CatchTelemetryJournalStorage();
        journal.append(tempDir, AcceptedCatchRecord.from(event(UUID.randomUUID(), actorId, 1)));
        journal.append(tempDir, AcceptedCatchRecord.from(event(UUID.randomUUID(), actorId, 2)));
        journal.append(tempDir, AcceptedCatchRecord.from(event(UUID.randomUUID(), actorId, 3)));
        CatchTelemetryService service = new CatchTelemetryService(
                journal,
                new CatchSummaryStorage(),
                LoggerFactory.getLogger("catch-telemetry-test"),
                1,
                1,
                60_000);
        service.bind(tempDir);

        assertEquals(1, service.summary(actorId).totalQuantity());
        assertTrue(service.pendingActors().contains(actorId));
        assertEquals(3, service.summary(actorId).totalQuantity());
        assertTrue(service.pendingActors().contains(actorId));
        assertEquals(6, service.summary(actorId).totalQuantity());
        assertFalse(service.pendingActors().contains(actorId));
    }

    @Test
    void exactRetryInsideRecentWindowAdvancesCheckpointWithoutDoubleCounting() {
        CatchTelemetryService service = service();
        service.bind(tempDir);
        CatchTelemetryEvent event = event(UUID.randomUUID(), UUID.randomUUID(), 4);

        service.accept(event);
        service.accept(event);

        CatchSummary summary = service.summary(event.actorId());
        assertEquals(4, summary.totalQuantity());
        assertEquals(2, summary.checkpoint().processedLines());
        assertEquals(1, summary.recentCatches().size());
    }

    @Test
    void restartLoadsSnapshotAndReplaysOnlyNewJournalRecords() throws IOException {
        UUID actorId = UUID.randomUUID();
        CatchTelemetryEvent first = event(UUID.randomUUID(), actorId, 1);
        CatchTelemetryEvent second = event(UUID.randomUUID(), actorId, 2);
        CatchTelemetryService beforeRestart = service();
        beforeRestart.bind(tempDir);
        beforeRestart.accept(first);
        beforeRestart.shutdown();
        new CatchTelemetryJournalStorage().append(tempDir, AcceptedCatchRecord.from(second));

        CatchTelemetryService afterRestart = service();
        afterRestart.bind(tempDir);
        CatchSummary summary = afterRestart.summary(actorId);

        assertEquals(3, summary.totalQuantity());
        assertEquals(2, summary.recentCatches().size());
    }

    @Test
    void conflictingRetryFailsClosed() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        CatchTelemetryService service = service();
        service.bind(tempDir);
        service.accept(event(eventId, actorId, 1));

        assertThrows(IllegalArgumentException.class, () -> service.accept(event(eventId, actorId, 2)));
        assertThrows(IllegalStateException.class, () -> service.summary(actorId));
    }

    private static CatchTelemetryService service() {
        return new CatchTelemetryService(
                new CatchTelemetryJournalStorage(),
                new CatchSummaryStorage(),
                LoggerFactory.getLogger("catch-telemetry-test"));
    }

    private static CatchTelemetryEvent event(UUID eventId, UUID actorId, long quantity) {
        return new CatchTelemetryEvent(
                eventId,
                Instant.parse("2026-06-12T10:15:30Z").toEpochMilli(),
                actorId,
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                quantity,
                null,
                null,
                null,
                Map.of());
    }
}
