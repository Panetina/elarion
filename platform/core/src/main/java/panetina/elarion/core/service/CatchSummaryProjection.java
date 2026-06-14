package panetina.elarion.core.service;

import net.minecraft.util.Identifier;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalCheckpoint;
import panetina.elarion.core.model.CatchJournalReplay;
import panetina.elarion.core.model.CatchSummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CatchSummaryProjection {
    private CatchSummaryProjection() {
    }

    public static CatchSummary apply(CatchSummary current, CatchJournalReplay replay) {
        if (compare(replay.nextCheckpoint(), current.checkpoint()) < 0) {
            throw new IllegalArgumentException("replay checkpoint must not move backwards");
        }

        long total = current.totalQuantity();
        Map<Identifier, Long> bySource = new LinkedHashMap<>(current.quantitiesBySource());
        Map<Identifier, Long> byFish = new LinkedHashMap<>(current.quantitiesByFishDefinition());
        Map<Identifier, Long> byRarity = new LinkedHashMap<>(current.quantitiesByRarity());
        long firstCatchAt = current.firstCatchAt();
        long latestCatchAt = current.latestCatchAt();
        List<AcceptedCatchRecord> recent = new ArrayList<>(current.recentCatches());

        for (AcceptedCatchRecord record : replay.records()) {
            if (!current.actorId().equals(record.actorId())) {
                throw new IllegalArgumentException("replay record actor does not match summary actor");
            }
            total = Math.addExact(total, record.quantity());
            increment(bySource, record.sourceId(), record.quantity());
            increment(byFish, record.fishDefinitionId(), record.quantity());
            increment(byRarity, record.rarityId(), record.quantity());
            firstCatchAt = firstCatchAt == 0
                    ? record.occurredAt()
                    : Math.min(firstCatchAt, record.occurredAt());
            latestCatchAt = Math.max(latestCatchAt, record.occurredAt());
            recent.add(record);
        }

        recent.sort(Comparator.comparingLong(AcceptedCatchRecord::occurredAt).reversed()
                .thenComparing(AcceptedCatchRecord::eventId));
        if (recent.size() > CatchSummary.MAX_RECENT_CATCHES) {
            recent = new ArrayList<>(recent.subList(0, CatchSummary.MAX_RECENT_CATCHES));
        }

        return new CatchSummary(
                CatchSummary.CURRENT_SCHEMA_VERSION,
                current.actorId(),
                total,
                bySource,
                byFish,
                byRarity,
                firstCatchAt,
                latestCatchAt,
                replay.nextCheckpoint(),
                recent);
    }

    private static void increment(Map<Identifier, Long> counts, Identifier id, long quantity) {
        counts.put(id, Math.addExact(counts.getOrDefault(id, 0L), quantity));
    }

    private static int compare(CatchJournalCheckpoint left, CatchJournalCheckpoint right) {
        if (left.isStart()) return right.isStart() ? 0 : -1;
        if (right.isStart()) return 1;
        int month = left.month().compareTo(right.month());
        return month != 0 ? month : Long.compare(left.processedLines(), right.processedLines());
    }
}
