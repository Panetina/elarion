package panetina.elarion.core.model;

/** Immutable per-species projection; all ordinary queries read this instead of scanning telemetry journals. */
public record CatchSpeciesSummary(
        long totalCount,
        long firstCatchAt,
        int fastestTimeTicks,
        long accumulatedTimeTicks,
        long timedSampleCount,
        int largestSizeMillimetres,
        long heaviestWeightGrams,
        int bestPercentileBasisPoints,
        long goldenCount,
        long perfectCount,
        long treasureCount
) {
    /** No percentile sample has been recorded. */
    public static final int NO_PERCENTILE = -1;

    public CatchSpeciesSummary {
        if (totalCount < 0 || firstCatchAt < 0 || fastestTimeTicks < 0 || accumulatedTimeTicks < 0
                || timedSampleCount < 0 || largestSizeMillimetres < 0 || heaviestWeightGrams < 0
                || bestPercentileBasisPoints < NO_PERCENTILE || bestPercentileBasisPoints > 10_000
                || goldenCount < 0 || perfectCount < 0 || treasureCount < 0) {
            throw new IllegalArgumentException("species summary values are outside their legal bounds");
        }
        if (totalCount == 0 && firstCatchAt != 0) {
            throw new IllegalArgumentException("empty species summary cannot have a first-catch timestamp");
        }
        if (totalCount > 0 && firstCatchAt <= 0) {
            throw new IllegalArgumentException("nonempty species summary requires a first-catch timestamp");
        }
        if ((timedSampleCount == 0) != (fastestTimeTicks == 0 && accumulatedTimeTicks == 0)) {
            throw new IllegalArgumentException("timing fields must either all be empty or all contain samples");
        }
        if (goldenCount > totalCount || perfectCount > totalCount || treasureCount > totalCount) {
            throw new IllegalArgumentException("species outcome counts cannot exceed total count");
        }
    }

    public static CatchSpeciesSummary countOnly(long count, long firstCatchAt) {
        return new CatchSpeciesSummary(count, firstCatchAt, 0, 0, 0, 0, 0, NO_PERCENTILE, 0, 0, 0);
    }

    public CatchSpeciesSummary apply(AcceptedCatchRecord record) {
        long newTotal = Math.addExact(totalCount, record.quantity());
        long newFirstCatch = firstCatchAt == 0 ? record.occurredAt() : Math.min(firstCatchAt, record.occurredAt());
        CatchTelemetryDetails details = record.details();
        if (details == null) {
            return new CatchSpeciesSummary(newTotal, newFirstCatch, fastestTimeTicks, accumulatedTimeTicks,
                    timedSampleCount, largestSizeMillimetres, heaviestWeightGrams, bestPercentileBasisPoints,
                    goldenCount, perfectCount, treasureCount);
        }

        int duration = details.minigameDurationTicks();
        int newFastest = duration == 0
                ? fastestTimeTicks
                : timedSampleCount == 0 ? duration : Math.min(fastestTimeTicks, duration);
        long newAccumulated = duration == 0
                ? accumulatedTimeTicks
                : Math.addExact(accumulatedTimeTicks, duration);
        long newSamples = duration == 0 ? timedSampleCount : Math.addExact(timedSampleCount, 1);
        int newBestPercentile = bestPercentileBasisPoints == NO_PERCENTILE
                ? details.percentileBasisPoints()
                : Math.min(bestPercentileBasisPoints, details.percentileBasisPoints());
        return new CatchSpeciesSummary(
                newTotal,
                newFirstCatch,
                newFastest,
                newAccumulated,
                newSamples,
                Math.max(largestSizeMillimetres, details.sizeMillimetres()),
                Math.max(heaviestWeightGrams, details.weightGrams()),
                newBestPercentile,
                Math.addExact(goldenCount, details.goldenCatch() ? 1 : 0),
                Math.addExact(perfectCount, details.perfectCatch() ? 1 : 0),
                Math.addExact(treasureCount, details.treasureCompleted() ? 1 : 0));
    }

    public double averageTimeTicks() {
        return timedSampleCount == 0 ? 0.0D : (double) accumulatedTimeTicks / timedSampleCount;
    }
}
