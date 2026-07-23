package panetina.elarion.core.storage;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.metric.MetricOperation;
import panetina.elarion.core.metric.MetricProjectionService;
import panetina.elarion.core.metric.MetricProjectionState;
import panetina.elarion.core.metric.MetricScope;
import panetina.elarion.core.metric.MetricUpdate;
import panetina.elarion.core.metric.MetricUpdateBatch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class MetricPersistenceCodecTest {
    private static final Identifier METRIC = Identifier.of("elarion_angling", "catch/count");
    private static final Identifier SOURCE = Identifier.of("elarion_angling", "fishing");
    private static final Identifier REALM = Identifier.of("elarion", "realm/one");

    @Test
    void batchRoundTripsAllAuthorityFields() {
        MetricUpdateBatch batch = batch();
        assertEquals(batch, MetricPersistenceCodec.decodeBatch(
                "roundtrip", MetricPersistenceCodec.encodeBatch(batch)));
    }

    @Test
    void projectionStateRoundTripsIndexesRevisionsAndLatestBatch() {
        MetricUpdateBatch batch = batch();
        UUID actor = batch.actorId();
        MetricProjectionState state = new MetricProjectionState(
                MetricProjectionState.CURRENT_SCHEMA_VERSION,
                List.of(new MetricProjectionState.IndexState(
                        METRIC, MetricScope.global(), Map.of("fish_id", Identifier.of("elarion_angling", "aloe_bream")),
                        9, Map.of(actor, 42L))),
                List.of(new MetricProjectionState.PartitionState(
                        SOURCE, "player:" + actor, 1, batch.eventId(), batch)));
        assertEquals(state, MetricPersistenceCodec.decodeState(
                "roundtrip", MetricPersistenceCodec.encodeState(state)));
    }

    @Test
    void rejectsUnboundedArraysBeforeDomainAllocation() {
        String batch = MetricPersistenceCodec.encodeBatch(batch());
        String updates = batch.substring(batch.indexOf("\"updates\":"));
        String repeated = "[" + "{},".repeat(MetricUpdateBatch.MAX_UPDATES) + "{}]";
        String malformed = batch.substring(0, batch.indexOf("[", batch.indexOf("\"updates\":")))
                + repeated + "}";
        assertThrows(MetricPersistenceFormatException.class,
                () -> MetricPersistenceCodec.decodeBatch("oversized", malformed));
    }

    private static MetricUpdateBatch batch() {
        UUID actor = new UUID(0, 1);
        MetricUpdate update = new MetricUpdate(
                METRIC, MetricOperation.ADD, 4,
                Set.of(MetricScope.global(), MetricScope.realm(REALM)),
                Map.of("fish_id", Identifier.of("elarion_angling", "aloe_bream")));
        return new MetricUpdateBatch(
                SOURCE, "player:" + actor, 1, new UUID(0, 2), actor,
                1234, REALM, List.of(update));
    }
}
