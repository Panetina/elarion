package panetina.elarion.core.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.CatchTelemetryJournalStorage;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchTelemetryWorkerTest {
    @TempDir
    Path root;

    @Test
    void acceptsProjectsDrainsAndRebindsWithoutGameplayThreadReplay() throws Exception {
        CatchTelemetryWorker worker = new CatchTelemetryWorker(
                new CatchTelemetryService(new CatchTelemetryJournalStorage(), new CatchSummaryStorage(),
                        LoggerFactory.getLogger("catch-worker-test")),
                4, Duration.ofSeconds(10));
        UUID actor = UUID.randomUUID();
        CatchTelemetryEvent event = event(actor, UUID.randomUUID(), 3);
        worker.bind(root);

        assertEquals(event.eventId(), worker.submit(event).get(10, TimeUnit.SECONDS).eventId());
        assertEquals(3, worker.cachedSummary(actor).totalQuantity());
        assertTrue(worker.snapshot().accepting());
        worker.shutdown();
        assertFalse(worker.snapshot().accepting());

        worker.bind(root);
        worker.activate(actor);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (worker.cachedSummary(actor).totalQuantity() != 3 && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertEquals(3, worker.cachedSummary(actor).totalQuantity());
        worker.shutdown();
        assertThrows(ExecutionException.class,
                () -> worker.submit(event(actor, UUID.randomUUID(), 1)).get(10, TimeUnit.SECONDS));
    }

    private static CatchTelemetryEvent event(UUID actor, UUID event, long quantity) {
        return new CatchTelemetryEvent(
                event, 1_780_000_000_000L, actor,
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "test_fish"),
                Identifier.of("elarion_angling", "common"), quantity,
                Identifier.ofVanilla("overworld"), Identifier.ofVanilla("overworld"),
                Identifier.ofVanilla("plains"), Map.of());
    }
}
