package panetina.elarion.core.storage;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.metric.MetricDescriptor;
import panetina.elarion.core.metric.MetricDescriptorRegistry;
import panetina.elarion.core.metric.MetricOperation;
import panetina.elarion.core.metric.MetricProjectionService;
import panetina.elarion.core.metric.MetricQuery;
import panetina.elarion.core.metric.MetricRetentionPolicy;
import panetina.elarion.core.metric.MetricScope;
import panetina.elarion.core.metric.MetricScopeType;
import panetina.elarion.core.metric.MetricSortDirection;
import panetina.elarion.core.metric.MetricUpdate;
import panetina.elarion.core.metric.MetricUpdateBatch;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class MetricPersistenceStorageTest {
    private static final Identifier METRIC = Identifier.of("elarion_angling", "catch/count");
    private static final Identifier SOURCE = Identifier.of("elarion_angling", "fishing");
    private static final Identifier REALM = Identifier.of("elarion", "realm/one");

    @TempDir
    Path temporary;

    @Test
    void atomicSnapshotAndAppendJournalRecoverCrashWindow() throws Exception {
        MetricJournalStorage journal = new MetricJournalStorage();
        MetricProjectionStorage snapshots = new MetricProjectionStorage();
        MetricProjectionService live = service();
        UUID actor = UUID.randomUUID();
        MetricUpdateBatch first = batch(actor, "unsafe/partition:one", 1, 4);
        MetricUpdateBatch second = batch(actor, "unsafe/partition:one", 2, 3);

        assertTrueBeforeAppend(live, first);
        journal.append(temporary, first);
        live.apply(first);
        snapshots.save(temporary, live.snapshotState());
        journal.append(temporary, second); // crash before current.json replacement

        MetricProjectionService restored = service();
        var state = snapshots.load(temporary);
        restored.restoreState(state);
        assertEquals(1, journal.replay(temporary, state, restored));
        assertEquals(7, restored.player(query(), actor).fixedPointValue());
        assertFalse(MetricJournalStorage.journalPath(temporary, first).toString()
                .contains(first.sourcePartition()));
    }

    @Test
    void conflictingJournalRetryFailsClosed() throws Exception {
        MetricJournalStorage journal = new MetricJournalStorage();
        MetricProjectionService live = service();
        UUID actor = UUID.randomUUID();
        MetricUpdateBatch first = batch(actor, "player", 1, 4);
        journal.append(temporary, first);
        journal.append(temporary, batch(actor, "player", 1, 5));
        assertThrows(IllegalArgumentException.class,
                () -> journal.replay(temporary, live.snapshotState(), live));
    }

    @Test
    void malformedCurrentStateDoesNotSilentlyResetRankings() throws Exception {
        Path state = MetricProjectionStorage.statePath(temporary);
        Files.createDirectories(state.getParent());
        Files.writeString(state, "{not-json", StandardCharsets.UTF_8);
        assertThrows(MetricPersistenceFormatException.class,
                () -> new MetricProjectionStorage().load(temporary));
    }

    @Test
    void compactionRemovesOnlyJournalSegmentsAfterSnapshot() throws Exception {
        MetricJournalStorage journal = new MetricJournalStorage();
        MetricProjectionStorage snapshots = new MetricProjectionStorage();
        MetricProjectionService live = service();
        UUID actor = UUID.randomUUID();
        MetricUpdateBatch batch = batch(actor, "player", 1, 4);
        journal.append(temporary, batch);
        live.apply(batch);
        Path unrelated = temporary.resolve("metrics").resolve("journal").resolve("keep.txt");
        Files.createDirectories(unrelated.getParent());
        Files.writeString(unrelated, "keep", StandardCharsets.UTF_8);

        snapshots.save(temporary, live.snapshotState());
        assertEquals(1, journal.compact(temporary));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(unrelated));

        MetricProjectionService restored = service();
        var state = snapshots.load(temporary);
        restored.restoreState(state);
        assertEquals(0, journal.replay(temporary, state, restored));
        assertEquals(4, restored.player(query(), actor).fixedPointValue());
    }

    private static void assertTrueBeforeAppend(MetricProjectionService service, MetricUpdateBatch batch) {
        org.junit.jupiter.api.Assertions.assertTrue(service.validateBatch(batch));
    }

    private static MetricProjectionService service() {
        MetricDescriptor descriptor = new MetricDescriptor(
                METRIC, MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                Set.of(MetricScopeType.GLOBAL), Set.of(), MetricRetentionPolicy.INDEFINITE);
        return new MetricProjectionService(MetricDescriptorRegistry.builder().register(descriptor).build());
    }

    private static MetricQuery query() {
        return new MetricQuery(METRIC, MetricScope.global(), Map.of());
    }

    private static MetricUpdateBatch batch(UUID actor, String partition, long sequence, long amount) {
        return new MetricUpdateBatch(
                SOURCE, partition, sequence,
                UUID.nameUUIDFromBytes((partition + sequence + amount).getBytes(StandardCharsets.UTF_8)),
                actor, 1234 + sequence, REALM,
                List.of(new MetricUpdate(
                        METRIC, MetricOperation.ADD, amount, Set.of(MetricScope.global()), Map.of())));
    }
}
