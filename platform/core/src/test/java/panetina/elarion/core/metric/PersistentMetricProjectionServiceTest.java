package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.storage.MetricJournalStorage;
import panetina.elarion.core.storage.MetricProjectionStorage;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PersistentMetricProjectionServiceTest {
    private static final Identifier METRIC = Identifier.of("elarion_angling", "catch/count");
    private static final Identifier SOURCE = Identifier.of("elarion_angling", "fishing");

    @TempDir
    Path root;

    @Test
    void durableCommitRestoresValueRankRevisionAndRetryCursor() {
        UUID actor = UUID.randomUUID();
        MetricUpdateBatch batch = batch(actor, 1, 5);
        PersistentMetricProjectionService first = service();
        assertEquals(0, first.bind(root));
        assertTrue(first.commitDurably(batch));
        long revision = first.revision(query());
        first.shutdown();

        PersistentMetricProjectionService restored = service();
        assertEquals(0, restored.bind(root));
        assertEquals(5, restored.player(query(), actor).fixedPointValue());
        assertEquals(revision, restored.revision(query()));
        assertFalse(restored.commitDurably(batch));
        assertThrows(IllegalArgumentException.class,
                () -> restored.commitDurably(batch(actor, 1, 6)));
    }

    @Test
    void descriptorsFreezeAtBind() {
        PersistentMetricProjectionService service = service();
        service.bind(root);
        assertThrows(IllegalStateException.class, () -> service.registerDescriptors(List.of(descriptor())));
    }

    @Test
    void sameServiceCanRebindAcrossIntegratedServerLifecycles() {
        UUID actor = UUID.randomUUID();
        PersistentMetricProjectionService service = service();
        service.bind(root);
        assertTrue(service.commitDurably(batch(actor, 1, 7)));
        service.shutdown();

        assertEquals(0, service.bind(root));
        assertEquals(7, service.player(query(), actor).fixedPointValue());
        assertFalse(service.commitDurably(batch(actor, 1, 7)));
        service.shutdown();
    }

    private static PersistentMetricProjectionService service() {
        PersistentMetricProjectionService service = new PersistentMetricProjectionService(
                new MetricJournalStorage(), new MetricProjectionStorage());
        service.registerDescriptors(List.of(descriptor()));
        return service;
    }

    private static MetricDescriptor descriptor() {
        return new MetricDescriptor(
                METRIC, MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                Set.of(MetricScopeType.GLOBAL), Set.of(), MetricRetentionPolicy.INDEFINITE);
    }

    private static MetricQuery query() {
        return new MetricQuery(METRIC, MetricScope.global(), Map.of());
    }

    private static MetricUpdateBatch batch(UUID actor, long sequence, long amount) {
        return new MetricUpdateBatch(
                SOURCE, "player:" + actor, sequence,
                UUID.nameUUIDFromBytes((sequence + ":" + amount).getBytes()), actor,
                1000 + sequence, null,
                List.of(new MetricUpdate(
                        METRIC, MetricOperation.ADD, amount, Set.of(MetricScope.global()), Map.of())));
    }
}
