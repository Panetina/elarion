package panetina.elarion.addons.angling.fishing;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.core.metric.MetricOperation;
import panetina.elarion.core.metric.MetricScope;
import panetina.elarion.core.metric.MetricUpdate;
import panetina.elarion.core.metric.MetricUpdateBatch;
import panetina.elarion.core.model.CatchTelemetryDetails;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Builds the fixed, bounded Core telemetry and metric projection batch for one server outcome. */
public final class AnglingCatchCommitFactory {
    public static final Identifier SOURCE = id("fishing");

    public AnglingCatchCommit create(AnglingCatchOutcome outcome, Facts facts, long sourceSequence) {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(facts, "facts");
        if (sourceSequence <= 0) throw new IllegalArgumentException("sourceSequence must be positive");
        Identifier fishId = outcome.catchDefinition().id();
        Identifier rarityId = id(outcome.catchDefinition().rarity().serializedName());
        Identifier catchTypeId = id(outcome.catchDefinition().type().serializedName());
        Identifier outputItemId = outcome.item().isEmpty()
                ? outcome.catchDefinition().definition().source().catchInfo().item().id()
                : Registries.ITEM.getId(outcome.item().getItem());
        long quantity = outcome.item().isEmpty() ? 1L : outcome.item().getCount();
        CatchTelemetryDetails details = new CatchTelemetryDetails(
                outputItemId, catchTypeId, outcome.sizeMillimetres(), outcome.weightGrams(),
                outcome.percentileBasisPoints(), outcome.minigameDurationTicks(), outcome.perfect(),
                outcome.golden(), outcome.treasureCompleted(), outcome.minigameHits(), facts.baitId(),
                facts.rodId(), facts.bobberId(), facts.hookId(), facts.fluidId(), facts.realmId(),
                facts.tournamentId());
        CatchTelemetryEvent telemetry = new CatchTelemetryEvent(
                facts.eventId(), facts.occurredAt(), facts.actorId(), SOURCE, fishId, rarityId, quantity,
                facts.worldId(), facts.dimensionId(), facts.biomeId(), Map.of(), details);

        Set<MetricScope> scopes = new LinkedHashSet<>();
        scopes.add(MetricScope.global());
        if (facts.realmId() != null) scopes.add(MetricScope.realm(facts.realmId()));
        List<MetricUpdate> updates = new ArrayList<>();
        addCountDimensions(updates, "catch/count", MetricOperation.ADD, quantity, scopes, fishId, rarityId);
        if (outcome.perfect()) addPerformance(updates, "catch/perfect_count", MetricOperation.ADD, 1, scopes, fishId);
        if (outcome.golden()) addPerformance(updates, "catch/golden_count", MetricOperation.ADD, 1, scopes, fishId);
        if (outcome.treasureAwarded()) {
            addPerformance(updates, "catch/treasure_count", MetricOperation.ADD, 1, scopes, fishId);
        }
        if (outcome.minigameDurationTicks() > 0) {
            addPerformance(updates, "catch/fastest_ticks", MetricOperation.MIN,
                    outcome.minigameDurationTicks(), scopes, fishId);
        }
        addPerformance(updates, "catch/largest_size_mm", MetricOperation.MAX,
                outcome.sizeMillimetres(), scopes, fishId);
        addPerformance(updates, "catch/heaviest_weight_g", MetricOperation.MAX,
                outcome.weightGrams(), scopes, fishId);
        addPerformance(updates, "catch/best_percentile_bps", MetricOperation.MIN,
                outcome.percentileBasisPoints(), scopes, fishId);
        MetricUpdateBatch metrics = new MetricUpdateBatch(
                SOURCE, sourcePartition(facts.actorId()), sourceSequence, facts.eventId(), facts.actorId(),
                facts.occurredAt(), facts.realmId(), updates);
        AnglingCatchReward reward = facts.reward().orElseGet(
                () -> AnglingCatchReward.from(outcome, facts.rewardPosition()));
        return new AnglingCatchCommit(telemetry, metrics, reward);
    }

    public static String sourcePartition(UUID actorId) {
        return "catch:" + Objects.requireNonNull(actorId, "actorId");
    }

    private static void addCountDimensions(
            List<MetricUpdate> updates,
            String path,
            MetricOperation operation,
            long value,
            Set<MetricScope> scopes,
            Identifier fishId,
            Identifier rarityId
    ) {
        updates.add(update(path, operation, value, scopes, Map.of()));
        updates.add(update(path, operation, value, scopes, Map.of("fish_id", fishId)));
        updates.add(update(path, operation, value, scopes, Map.of("rarity_id", rarityId)));
    }

    private static void addPerformance(
            List<MetricUpdate> updates,
            String path,
            MetricOperation operation,
            long value,
            Set<MetricScope> scopes,
            Identifier fishId
    ) {
        updates.add(update(path, operation, value, scopes, Map.of()));
        updates.add(update(path, operation, value, scopes, Map.of("fish_id", fishId)));
    }

    private static MetricUpdate update(
            String path,
            MetricOperation operation,
            long value,
            Set<MetricScope> scopes,
            Map<String, Identifier> dimensions
    ) {
        return new MetricUpdate(id(path), operation, value, scopes, dimensions);
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }

    public record Facts(
            UUID eventId,
            long occurredAt,
            UUID actorId,
            Identifier worldId,
            Identifier dimensionId,
            Identifier biomeId,
            Identifier baitId,
            Identifier rodId,
            Identifier bobberId,
            Identifier hookId,
            Identifier fluidId,
            Identifier realmId,
            Identifier tournamentId,
            AnglingCatchReward.RewardPosition rewardPosition,
            java.util.Optional<AnglingCatchReward> reward
    ) {
        public Facts(
                UUID eventId,
                long occurredAt,
                UUID actorId,
                Identifier worldId,
                Identifier dimensionId,
                Identifier biomeId,
                Identifier baitId,
                Identifier rodId,
                Identifier bobberId,
                Identifier hookId,
                Identifier fluidId,
                Identifier realmId,
                Identifier tournamentId,
                AnglingCatchReward.RewardPosition rewardPosition
        ) {
            this(eventId, occurredAt, actorId, worldId, dimensionId, biomeId, baitId, rodId, bobberId,
                    hookId, fluidId, realmId, tournamentId, rewardPosition, java.util.Optional.empty());
        }

        public Facts {
            Objects.requireNonNull(eventId, "eventId");
            Objects.requireNonNull(actorId, "actorId");
            Objects.requireNonNull(worldId, "worldId");
            Objects.requireNonNull(dimensionId, "dimensionId");
            Objects.requireNonNull(biomeId, "biomeId");
            Objects.requireNonNull(rewardPosition, "rewardPosition");
            reward = java.util.Objects.requireNonNull(reward, "reward");
            if (occurredAt <= 0) {
                throw new IllegalArgumentException("catch commit timestamp must be positive");
            }
        }
    }
}
