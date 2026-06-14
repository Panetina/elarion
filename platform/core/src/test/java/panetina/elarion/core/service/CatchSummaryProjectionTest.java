package panetina.elarion.core.service;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalCheckpoint;
import panetina.elarion.core.model.CatchJournalReplay;
import panetina.elarion.core.model.CatchSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CatchSummaryProjectionTest {
    @Test
    void appliesReplayIntoAllBoundedIndexesAndCheckpoint() {
        UUID actorId = UUID.randomUUID();
        AcceptedCatchRecord older = record(actorId, "placeholder_common", 2, "2026-06-10T10:00:00Z");
        AcceptedCatchRecord newer = record(actorId, "placeholder_rare", 3, "2026-06-12T10:00:00Z");
        CatchJournalCheckpoint checkpoint = new CatchJournalCheckpoint("2026-06", 2);

        CatchSummary summary = CatchSummaryProjection.apply(
                CatchSummary.empty(actorId),
                new CatchJournalReplay(List.of(older, newer), checkpoint, 2, false));

        assertEquals(5, summary.totalQuantity());
        assertEquals(5, summary.quantityForSource(older.sourceId()));
        assertEquals(5, summary.quantityForFishDefinition(older.fishDefinitionId()));
        assertEquals(2, summary.quantityForRarity(older.rarityId()));
        assertEquals(3, summary.quantityForRarity(newer.rarityId()));
        assertEquals(older.occurredAt(), summary.firstCatchAt());
        assertEquals(newer.occurredAt(), summary.latestCatchAt());
        assertEquals(List.of(newer, older), summary.recentCatches());
        assertEquals(checkpoint, summary.checkpoint());
    }

    @Test
    void duplicateOnlyPageStillAdvancesCheckpoint() {
        UUID actorId = UUID.randomUUID();
        CatchSummary current = CatchSummary.empty(actorId);
        CatchJournalCheckpoint checkpoint = new CatchJournalCheckpoint("2026-06", 3);

        CatchSummary updated = CatchSummaryProjection.apply(
                current,
                new CatchJournalReplay(List.of(), checkpoint, 3, false));

        assertEquals(0, updated.totalQuantity());
        assertEquals(checkpoint, updated.checkpoint());
    }

    @Test
    void recentRecordsRemainBoundedAndNewestFirst() {
        UUID actorId = UUID.randomUUID();
        List<AcceptedCatchRecord> records = new ArrayList<>();
        for (int index = 0; index < 40; index++) {
            records.add(record(actorId, "placeholder_common", 1,
                    "2026-06-" + String.format("%02d", index % 28 + 1) + "T10:00:00Z"));
        }

        CatchSummary summary = CatchSummaryProjection.apply(
                CatchSummary.empty(actorId),
                new CatchJournalReplay(records, new CatchJournalCheckpoint("2026-06", 40), 40, false));

        assertEquals(CatchSummary.MAX_RECENT_CATCHES, summary.recentCatches().size());
        for (int index = 1; index < summary.recentCatches().size(); index++) {
            assertTrue(summary.recentCatches().get(index - 1).occurredAt()
                    >= summary.recentCatches().get(index).occurredAt());
        }
    }

    @Test
    void overflowAndBackwardsCheckpointFailWithoutMutatingCurrentSummary() {
        UUID actorId = UUID.randomUUID();
        AcceptedCatchRecord existing = record(actorId, "placeholder_common", Long.MAX_VALUE,
                "2026-06-10T10:00:00Z");
        CatchSummary current = new CatchSummary(
                CatchSummary.CURRENT_SCHEMA_VERSION,
                actorId,
                Long.MAX_VALUE,
                Map.of(existing.sourceId(), Long.MAX_VALUE),
                Map.of(existing.fishDefinitionId(), Long.MAX_VALUE),
                Map.of(existing.rarityId(), Long.MAX_VALUE),
                existing.occurredAt(),
                existing.occurredAt(),
                new CatchJournalCheckpoint("2026-06", 1),
                List.of(existing));

        assertThrows(ArithmeticException.class, () -> CatchSummaryProjection.apply(
                current,
                new CatchJournalReplay(
                        List.of(record(actorId, "placeholder_common", 1, "2026-06-11T10:00:00Z")),
                        new CatchJournalCheckpoint("2026-06", 2),
                        1,
                        false)));
        assertThrows(IllegalArgumentException.class, () -> CatchSummaryProjection.apply(
                current,
                new CatchJournalReplay(
                        List.of(),
                        CatchJournalCheckpoint.START,
                        0,
                        false)));
        assertEquals(Long.MAX_VALUE, current.totalQuantity());
    }

    private static AcceptedCatchRecord record(
            UUID actorId,
            String rarity,
            long quantity,
            String occurredAt
    ) {
        return new AcceptedCatchRecord(
                AcceptedCatchRecord.CURRENT_SCHEMA_VERSION,
                UUID.randomUUID(),
                Instant.parse(occurredAt).toEpochMilli(),
                actorId,
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", rarity),
                quantity,
                null,
                null,
                null,
                Map.of());
    }
}
