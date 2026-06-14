package panetina.elarion.core.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class CatchSummaryTest {
    @Test
    void emptySummaryHasImmutableDirectLookupIndexes() {
        UUID actorId = UUID.randomUUID();
        CatchSummary summary = CatchSummary.empty(actorId);

        assertEquals(actorId, summary.actorId());
        assertEquals(0, summary.totalQuantity());
        assertEquals(0, summary.quantityForSource(Identifier.of("elarion_angling", "fishing")));
        assertThrows(UnsupportedOperationException.class, () ->
                summary.quantitiesBySource().put(Identifier.of("elarion", "test"), 1L));
        assertThrows(UnsupportedOperationException.class, () -> summary.recentCatches().add(record(actorId)));
    }

    @Test
    void rejectsInconsistentTotalsAndRecentActors() {
        UUID actorId = UUID.randomUUID();
        Identifier source = Identifier.of("elarion_angling", "fishing");

        assertThrows(IllegalArgumentException.class, () -> new CatchSummary(
                CatchSummary.CURRENT_SCHEMA_VERSION,
                actorId,
                2,
                Map.of(source, 1L),
                Map.of(Identifier.of("elarion_angling", "placeholder_fish_001"), 2L),
                Map.of(Identifier.of("elarion_angling", "placeholder_common"), 2L),
                1,
                1,
                CatchJournalCheckpoint.START,
                List.of()));
        assertThrows(IllegalArgumentException.class, () -> new CatchSummary(
                CatchSummary.CURRENT_SCHEMA_VERSION,
                actorId,
                1,
                Map.of(source, 1L),
                Map.of(Identifier.of("elarion_angling", "placeholder_fish_001"), 1L),
                Map.of(Identifier.of("elarion_angling", "placeholder_common"), 1L),
                1,
                1,
                CatchJournalCheckpoint.START,
                List.of(record(UUID.randomUUID()))));
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
