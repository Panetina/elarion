package panetina.elarion.core.model;

import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * Optional authoritative outcome details supplied by a server-owned fishing system.
 * Legacy and non-fishing telemetry may omit this object entirely.
 */
public record CatchTelemetryDetails(
        Identifier outputItemId,
        Identifier catchTypeId,
        int sizeMillimetres,
        long weightGrams,
        int percentileBasisPoints,
        int minigameDurationTicks,
        boolean perfectCatch,
        boolean goldenCatch,
        boolean treasureCompleted,
        int minigameHits,
        Identifier baitId,
        Identifier rodId,
        Identifier bobberId,
        Identifier hookId,
        Identifier fluidId,
        Identifier realmId,
        Identifier tournamentId
) {
    public CatchTelemetryDetails {
        Objects.requireNonNull(outputItemId, "outputItemId");
        Objects.requireNonNull(catchTypeId, "catchTypeId");
        if (sizeMillimetres < 0 || weightGrams < 0 || percentileBasisPoints < 0
                || percentileBasisPoints > 10_000 || minigameDurationTicks < 0 || minigameHits < 0) {
            throw new IllegalArgumentException("catch outcome values must be nonnegative and percentile must be 0-10000");
        }
    }
}
