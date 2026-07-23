package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import panetina.elarion.core.metric.MetricOperation;
import panetina.elarion.core.metric.MetricScope;
import panetina.elarion.core.metric.MetricUpdate;
import panetina.elarion.core.metric.MetricUpdateBatch;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.model.CatchTelemetryDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class AnglingCatchCommitTestFixtures {
    private AnglingCatchCommitTestFixtures() {
    }

    static AnglingCatchCommit commit(AnglingCatchReward reward) {
        return commit(reward, UUID.randomUUID(), UUID.randomUUID(), 1);
    }

    static AnglingCatchCommit commit(AnglingCatchReward reward, UUID actorId, UUID eventId, long sequence) {
        long occurredAt = 1_780_000_000_000L;
        Identifier source = AnglingCatchCommitFactory.SOURCE;
        CatchTelemetryEvent telemetry = new CatchTelemetryEvent(
                eventId, occurredAt, actorId, source,
                Identifier.of("elarion_angling", "test_fish"),
                Identifier.of("elarion_angling", "common"), 1,
                Identifier.ofVanilla("overworld"), Identifier.ofVanilla("overworld"),
                Identifier.ofVanilla("plains"), Map.of(),
                new CatchTelemetryDetails(Identifier.ofVanilla("cod"),
                        Identifier.of("elarion_angling", "normal"),
                        100, 200, 5_000, 40, true, false, false, 3,
                        Identifier.ofVanilla("cod"),
                        Identifier.of("elarion_angling", "elarion_angling_rod"),
                        null, null, Identifier.ofVanilla("water"), null, null));
        MetricUpdate update = new MetricUpdate(Identifier.of("elarion_angling", "catch/count"),
                MetricOperation.ADD, 1, Set.of(MetricScope.global()), Map.of());
        MetricUpdateBatch metrics = new MetricUpdateBatch(source,
                AnglingCatchCommitFactory.sourcePartition(actorId), sequence, eventId, actorId,
                occurredAt, null, List.of(update));
        return new AnglingCatchCommit(telemetry, metrics, reward);
    }
}
