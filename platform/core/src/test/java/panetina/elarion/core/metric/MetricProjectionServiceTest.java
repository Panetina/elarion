package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MetricProjectionServiceTest {
    private static final Identifier COUNT = Identifier.of("elarion_angling", "catch/count");
    private static final Identifier REALM = Identifier.of("elarion", "realm/one");

    @Test
    void competitionRanksUseValueTiesAndStableUuidDisplayOrder() {
        MetricProjectionService service = service();
        UUID first = new UUID(0, 1);
        UUID second = new UUID(0, 2);
        UUID third = new UUID(0, 3);
        assertTrue(service.apply(batch(first, "first", 1, 10)));
        service.apply(batch(second, "second", 1, 10));
        service.apply(batch(third, "third", 1, 5));

        MetricQuery query = query();
        MetricPage page = service.top(query, 100);
        assertEquals(List.of(first, second, third), page.entries().stream().map(MetricRankEntry::actorId).toList());
        assertEquals(List.of(1L, 1L, 3L), page.entries().stream().map(MetricRankEntry::rank).toList());
        assertEquals(1, service.player(query, second).rank());
        assertFalse(service.apply(batch(first, "first", 1, 10)));
        assertThrows(IllegalArgumentException.class, () -> service.apply(batch(first, "first", 1, 11)));
    }

    @Test
    void updatesRanksInLogarithmicIndexesAndSupportsBoundedPaging() {
        MetricProjectionService service = service();
        List<UUID> actors = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            UUID actor = new UUID(0, index + 1);
            actors.add(actor);
            service.apply(batch(actor, "p" + index, 1, index));
        }
        MetricPage firstPage = service.top(query(), 5);
        assertEquals(5, firstPage.entries().size());
        assertEquals(11, firstPage.entries().get(0).fixedPointValue());
        MetricPage secondPage = service.pageAfter(query(), firstPage.nextCursor(), 5);
        assertEquals(6, secondPage.entries().get(0).fixedPointValue());

        MetricPage around = service.around(query(), actors.get(5), 2);
        assertEquals(5, around.entries().size());
        MetricCursor stale = firstPage.nextCursor();
        service.apply(batch(actors.get(0), "p0", 2, 100));
        assertThrows(IllegalArgumentException.class, () -> service.pageAfter(query(), stale, 5));
        assertEquals(1, service.player(query(), actors.get(0)).rank());
    }

    @Test
    void validatesScopeDimensionsOperationAndAppliesBatchAtomically() {
        MetricProjectionService service = service();
        UUID actor = UUID.randomUUID();
        MetricUpdate wrongRealm = new MetricUpdate(COUNT, MetricOperation.ADD, 1,
                Set.of(MetricScope.realm(Identifier.of("elarion", "realm/two"))), Map.of());
        assertThrows(IllegalArgumentException.class, () -> service.apply(rawBatch(actor, "bad-realm", 1, List.of(wrongRealm))));

        MetricUpdate unindexed = new MetricUpdate(COUNT, MetricOperation.ADD, 1,
                Set.of(MetricScope.global()), Map.of("biome", Identifier.ofVanilla("plains")));
        assertThrows(IllegalArgumentException.class, () -> service.apply(rawBatch(actor, "bad-dimension", 1, List.of(unindexed))));

        MetricUpdate wrongOperation = new MetricUpdate(COUNT, MetricOperation.MAX, 1,
                Set.of(MetricScope.global()), Map.of());
        assertThrows(IllegalArgumentException.class, () -> service.apply(rawBatch(actor, "bad-operation", 1, List.of(wrongOperation))));

        service.apply(batch(actor, "overflow", 1, Long.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> service.apply(batch(actor, "overflow", 2, 1)));
        assertEquals(Long.MAX_VALUE, service.player(query(), actor).fixedPointValue());
        assertTrue(service.apply(batch(actor, "overflow", 2, 0)));
    }

    @Test
    void realmAndDimensionIndexesAreMaterializedOnlyWhenExplicitlyUpdated() {
        MetricProjectionService service = service();
        UUID actor = UUID.randomUUID();
        MetricUpdate update = new MetricUpdate(COUNT, MetricOperation.ADD, 4,
                Set.of(MetricScope.global(), MetricScope.realm(REALM)),
                Map.of("fish_id", Identifier.of("elarion_angling", "aloe_bream")));
        service.apply(rawBatch(actor, "scoped", 1, List.of(update)));

        MetricQuery globalFish = new MetricQuery(COUNT, MetricScope.global(), update.dimensions());
        MetricQuery realmFish = new MetricQuery(COUNT, MetricScope.realm(REALM), update.dimensions());
        assertEquals(4, service.player(globalFish, actor).fixedPointValue());
        assertEquals(4, service.player(realmFish, actor).fixedPointValue());
        assertNull(service.player(query(), actor));
    }

    @Test
    void snapshotRestorePreservesRanksRevisionsAndIdempotency() {
        MetricProjectionService source = service();
        UUID first = new UUID(0, 1);
        UUID second = new UUID(0, 2);
        MetricUpdateBatch firstBatch = batch(first, "first", 1, 10);
        source.apply(firstBatch);
        source.apply(batch(second, "second", 1, 5));
        source.apply(batch(first, "first", 2, 2));
        MetricProjectionState saved = source.snapshotState();

        MetricProjectionService restored = service();
        restored.restoreState(saved);
        assertEquals(source.top(query(), 100), restored.top(query(), 100));
        assertEquals(source.revision(query()), restored.revision(query()));
        assertFalse(restored.apply(batch(first, "first", 2, 2)));
        assertThrows(IllegalArgumentException.class,
                () -> restored.apply(batch(first, "first", 2, 3)));
    }

    @Test
    void invalidRestoreDoesNotMutateLiveProjection() {
        MetricProjectionService service = service();
        UUID actor = UUID.randomUUID();
        service.apply(batch(actor, "valid", 1, 5));
        MetricPage before = service.top(query(), 100);
        MetricProjectionState invalid = new MetricProjectionState(
                MetricProjectionState.CURRENT_SCHEMA_VERSION,
                List.of(new MetricProjectionState.IndexState(
                        Identifier.of("missing", "metric"), MetricScope.global(), Map.of(), 1,
                        Map.of(actor, 99L))),
                List.of());
        assertThrows(IllegalArgumentException.class, () -> service.restoreState(invalid));
        assertEquals(before, service.top(query(), 100));
    }

    private static MetricProjectionService service() {
        MetricDescriptor descriptor = new MetricDescriptor(
                COUNT, MetricOperation.ADD, MetricSortDirection.DESCENDING, "count",
                Set.of(MetricScopeType.GLOBAL, MetricScopeType.REALM, MetricScopeType.EVENT),
                Set.of("fish_id", "rarity_id"), MetricRetentionPolicy.INDEFINITE);
        return new MetricProjectionService(MetricDescriptorRegistry.builder().register(descriptor).build());
    }

    private static MetricQuery query() {
        return new MetricQuery(COUNT, MetricScope.global(), Map.of());
    }

    private static MetricUpdateBatch batch(UUID actor, String partition, long sequence, long value) {
        MetricUpdate update = new MetricUpdate(COUNT, MetricOperation.ADD, value,
                Set.of(MetricScope.global()), Map.of());
        return rawBatch(actor, partition, sequence, List.of(update));
    }

    private static MetricUpdateBatch rawBatch(
            UUID actor,
            String partition,
            long sequence,
            List<MetricUpdate> updates
    ) {
        return new MetricUpdateBatch(
                Identifier.of("elarion_angling", "fishing"), partition, sequence,
                UUID.nameUUIDFromBytes((partition + sequence).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                actor, 1, REALM, updates);
    }
}
