package panetina.elarion.addons.angling.fishing;

import panetina.elarion.core.metric.MetricUpdateBatch;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.Objects;

/** The two Core-owned projections and restart-safe delivery payload for one accepted catch UUID. */
public record AnglingCatchCommit(
        CatchTelemetryEvent telemetry,
        MetricUpdateBatch metrics,
        AnglingCatchReward reward
) {
    public AnglingCatchCommit {
        Objects.requireNonNull(telemetry, "telemetry");
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(reward, "reward");
        if (!telemetry.eventId().equals(metrics.eventId())
                || !telemetry.actorId().equals(metrics.actorId())
                || telemetry.occurredAt() != metrics.occurredAt()) {
            throw new IllegalArgumentException("catch telemetry and metric batch identities do not match");
        }
    }
}
