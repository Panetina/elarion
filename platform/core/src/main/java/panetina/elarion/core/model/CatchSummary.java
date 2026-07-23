package panetina.elarion.core.model;

import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record CatchSummary(
        int schemaVersion,
        UUID actorId,
        long totalQuantity,
        Map<Identifier, Long> quantitiesBySource,
        Map<Identifier, Long> quantitiesByFishDefinition,
        Map<Identifier, Long> quantitiesByRarity,
        Map<Identifier, CatchSpeciesSummary> speciesSummaries,
        long firstCatchAt,
        long latestCatchAt,
        CatchJournalCheckpoint checkpoint,
        List<AcceptedCatchRecord> recentCatches
) {
    public static final int CURRENT_SCHEMA_VERSION = 2;
    public static final int MAX_RECENT_CATCHES = 32;

    public CatchSummary {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException("unsupported catch summary schema version: " + schemaVersion);
        }
        Objects.requireNonNull(actorId, "actorId");
        if (totalQuantity < 0) throw new IllegalArgumentException("totalQuantity must not be negative");
        quantitiesBySource = validatedCounts("quantitiesBySource", quantitiesBySource);
        quantitiesByFishDefinition = validatedCounts(
                "quantitiesByFishDefinition", quantitiesByFishDefinition);
        quantitiesByRarity = validatedCounts("quantitiesByRarity", quantitiesByRarity);
        speciesSummaries = validatedSpeciesSummaries(speciesSummaries, quantitiesByFishDefinition);
        validateTotal("quantitiesBySource", totalQuantity, quantitiesBySource);
        validateTotal("quantitiesByFishDefinition", totalQuantity, quantitiesByFishDefinition);
        validateTotal("quantitiesByRarity", totalQuantity, quantitiesByRarity);
        checkpoint = Objects.requireNonNull(checkpoint, "checkpoint");
        recentCatches = List.copyOf(recentCatches == null ? List.of() : recentCatches);
        if (recentCatches.size() > MAX_RECENT_CATCHES) {
            throw new IllegalArgumentException(
                    "recentCatches must contain at most " + MAX_RECENT_CATCHES + " records");
        }
        Set<UUID> recentEventIds = new HashSet<>();
        long previousTimestamp = Long.MAX_VALUE;
        for (AcceptedCatchRecord record : recentCatches) {
            if (!actorId.equals(record.actorId())) {
                throw new IllegalArgumentException("recent catch actor does not match summary actor");
            }
            if (!recentEventIds.add(record.eventId())) {
                throw new IllegalArgumentException("recent catches must not contain duplicate event IDs");
            }
            if (record.occurredAt() > previousTimestamp) {
                throw new IllegalArgumentException("recent catches must be ordered newest first");
            }
            previousTimestamp = record.occurredAt();
        }
        if (totalQuantity == 0) {
            if (firstCatchAt != 0 || latestCatchAt != 0 || !recentCatches.isEmpty()) {
                throw new IllegalArgumentException("empty catch summary must not contain catch timestamps or records");
            }
        } else if (firstCatchAt <= 0 || latestCatchAt < firstCatchAt) {
            throw new IllegalArgumentException("catch timestamps are inconsistent");
        } else {
            for (AcceptedCatchRecord record : recentCatches) {
                if (record.occurredAt() < firstCatchAt || record.occurredAt() > latestCatchAt) {
                    throw new IllegalArgumentException("recent catch timestamp is outside summary bounds");
                }
            }
        }
    }

    /** Backward-compatible source constructor; count-only species projections are derived immediately. */
    public CatchSummary(
            int schemaVersion,
            UUID actorId,
            long totalQuantity,
            Map<Identifier, Long> quantitiesBySource,
            Map<Identifier, Long> quantitiesByFishDefinition,
            Map<Identifier, Long> quantitiesByRarity,
            long firstCatchAt,
            long latestCatchAt,
            CatchJournalCheckpoint checkpoint,
            List<AcceptedCatchRecord> recentCatches
    ) {
        this(schemaVersion, actorId, totalQuantity, quantitiesBySource, quantitiesByFishDefinition,
                quantitiesByRarity, countOnlySpecies(quantitiesByFishDefinition, firstCatchAt),
                firstCatchAt, latestCatchAt, checkpoint, recentCatches);
    }

    public static CatchSummary empty(UUID actorId) {
        return new CatchSummary(
                CURRENT_SCHEMA_VERSION,
                actorId,
                0,
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                0,
                0,
                CatchJournalCheckpoint.START,
                List.of());
    }

    public long quantityForSource(Identifier sourceId) {
        return quantitiesBySource.getOrDefault(sourceId, 0L);
    }

    public long quantityForFishDefinition(Identifier fishDefinitionId) {
        return quantitiesByFishDefinition.getOrDefault(fishDefinitionId, 0L);
    }

    public long quantityForRarity(Identifier rarityId) {
        return quantitiesByRarity.getOrDefault(rarityId, 0L);
    }

    public CatchSpeciesSummary speciesSummary(Identifier fishDefinitionId) {
        return speciesSummaries.get(fishDefinitionId);
    }

    private static Map<Identifier, Long> validatedCounts(
            String field,
            Map<Identifier, Long> counts
    ) {
        Map<Identifier, Long> copy = new LinkedHashMap<>();
        if (counts != null) copy.putAll(counts);
        for (Map.Entry<Identifier, Long> entry : copy.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException(field + " must contain nonnull IDs and positive quantities");
            }
        }
        return Map.copyOf(copy);
    }

    private static void validateTotal(
            String field,
            long expected,
            Map<Identifier, Long> counts
    ) {
        long total = 0;
        try {
            for (long value : counts.values()) total = Math.addExact(total, value);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(field + " quantity total overflowed", exception);
        }
        if (total != expected) {
            throw new IllegalArgumentException(field + " quantities must total " + expected);
        }
    }

    private static Map<Identifier, CatchSpeciesSummary> validatedSpeciesSummaries(
            Map<Identifier, CatchSpeciesSummary> summaries,
            Map<Identifier, Long> counts
    ) {
        Map<Identifier, CatchSpeciesSummary> copy = new LinkedHashMap<>();
        if (summaries != null) copy.putAll(summaries);
        if (!copy.keySet().equals(counts.keySet())) {
            throw new IllegalArgumentException("speciesSummaries must contain exactly the counted fish definitions");
        }
        for (Map.Entry<Identifier, CatchSpeciesSummary> entry : copy.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null
                    || entry.getValue().totalCount() != counts.get(entry.getKey())) {
                throw new IllegalArgumentException("species summary count does not match fish quantity");
            }
        }
        return Map.copyOf(copy);
    }

    private static Map<Identifier, CatchSpeciesSummary> countOnlySpecies(
            Map<Identifier, Long> counts,
            long firstCatchAt
    ) {
        Map<Identifier, CatchSpeciesSummary> summaries = new LinkedHashMap<>();
        if (counts != null) {
            counts.forEach((id, count) -> summaries.put(id, CatchSpeciesSummary.countOnly(count, firstCatchAt)));
        }
        return summaries;
    }
}
