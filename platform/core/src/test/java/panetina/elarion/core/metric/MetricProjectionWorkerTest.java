package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.storage.MetricJournalStorage;
import panetina.elarion.core.storage.MetricProjectionStorage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MetricProjectionWorkerTest {
    private static final Identifier METRIC = Identifier.of("elarion_angling", "catch/count");
    private static final Identifier SOURCE = Identifier.of("elarion_angling", "fishing");

    @TempDir
    Path root;

    @Test
    void serializesCommitsCheckpointsOffThreadAndRebinds() throws Exception {
        UUID actor = UUID.randomUUID();
        MetricProjectionWorker worker = worker(2);
        assertEquals(0, worker.bind(root));

        assertEquals(MetricProjectionWorker.CommitResult.APPLIED,
                worker.submit(batch(actor, 1, 2)).get(10, TimeUnit.SECONDS));
        assertEquals(MetricProjectionWorker.CommitResult.APPLIED,
                worker.submit(batch(actor, 2, 3)).get(10, TimeUnit.SECONDS));
        assertEquals(5, worker.player(query(), actor).fixedPointValue());
        assertEquals(0, journalLineCount(root));

        assertEquals(MetricProjectionWorker.CommitResult.APPLIED,
                worker.submit(batch(actor, 3, 4)).get(10, TimeUnit.SECONDS));
        assertEquals(1, journalLineCount(root));
        MetricProjectionWorker.Snapshot live = worker.snapshot();
        assertTrue(live.accepting());
        assertEquals(3, live.appliedBatches());
        worker.shutdown();
        assertFalse(worker.snapshot().accepting());
        assertEquals(0, journalLineCount(root));

        assertEquals(0, worker.bind(root));
        assertEquals(9, worker.player(query(), actor).fixedPointValue());
        assertEquals(MetricProjectionWorker.CommitResult.EXACT_RETRY,
                worker.submit(batch(actor, 3, 4)).get(10, TimeUnit.SECONDS));
        worker.shutdown();
    }

    @Test
    void emitsVersionedMetricEventOnlyForTheFirstDurableApplication() throws Exception {
        UUID actor = UUID.randomUUID();
        java.util.concurrent.atomic.AtomicReference<MetricUpdatedEvent> event =
                new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicInteger emissions = new java.util.concurrent.atomic.AtomicInteger();
        PersistentMetricProjectionService persistence = new PersistentMetricProjectionService(
                new MetricJournalStorage(), new MetricProjectionStorage());
        persistence.registerDescriptors(List.of(new MetricDescriptor(
                METRIC, MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                Set.of(MetricScopeType.GLOBAL), Set.of(), MetricRetentionPolicy.INDEFINITE)));
        MetricProjectionWorker worker = new MetricProjectionWorker(persistence, value -> {
            event.set(value);
            emissions.incrementAndGet();
        }, 4, 16, Duration.ofSeconds(10));
        worker.bind(root);
        MetricUpdateBatch batch = batch(actor, 1, 2);

        assertEquals(MetricProjectionWorker.CommitResult.APPLIED,
                worker.submit(batch).get(10, TimeUnit.SECONDS));
        assertEquals(MetricProjectionWorker.CommitResult.EXACT_RETRY,
                worker.submit(batch).get(10, TimeUnit.SECONDS));
        assertEquals(1, emissions.get());
        assertEquals(MetricUpdatedEvent.CURRENT_SCHEMA_VERSION, event.get().schemaVersion());
        assertEquals(batch, event.get().batch());
        worker.shutdown();
    }

    private static MetricProjectionWorker worker(int checkpointInterval) {
        PersistentMetricProjectionService persistence = new PersistentMetricProjectionService(
                new MetricJournalStorage(), new MetricProjectionStorage());
        persistence.registerDescriptors(List.of(new MetricDescriptor(
                METRIC, MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                Set.of(MetricScopeType.GLOBAL), Set.of(), MetricRetentionPolicy.INDEFINITE)));
        return new MetricProjectionWorker(persistence, 4, checkpointInterval, Duration.ofSeconds(10));
    }

    private static MetricQuery query() {
        return new MetricQuery(METRIC, MetricScope.global(), Map.of());
    }

    private static MetricUpdateBatch batch(UUID actor, long sequence, long amount) {
        return new MetricUpdateBatch(
                SOURCE, "player:" + actor, sequence,
                UUID.nameUUIDFromBytes((sequence + ":" + amount).getBytes(StandardCharsets.UTF_8)), actor,
                1000 + sequence, null,
                List.of(new MetricUpdate(
                        METRIC, MetricOperation.ADD, amount, Set.of(MetricScope.global()), Map.of())));
    }

    private static long journalLineCount(Path root) throws Exception {
        Path journal = root.resolve("metrics").resolve("journal");
        if (Files.notExists(journal)) return 0;
        try (var paths = Files.walk(journal)) {
            long total = 0;
            for (Path file : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jsonl")).toList()) {
                try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
                    total += lines.filter(line -> !line.isBlank()).count();
                }
            }
            return total;
        }
    }
}
