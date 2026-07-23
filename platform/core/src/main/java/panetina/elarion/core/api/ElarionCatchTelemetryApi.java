package panetina.elarion.core.api;

import net.minecraft.util.Identifier;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.model.CatchSpeciesSummary;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.service.CatchTelemetryService;
import panetina.elarion.core.service.CatchTelemetryWorker;
import java.util.concurrent.CompletableFuture;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ElarionCatchTelemetryApi {
    private final CatchTelemetryService telemetry;
    private final CatchTelemetryWorker worker;

    ElarionCatchTelemetryApi(CatchTelemetryService telemetry) {
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry");
        this.worker = null;
    }

    ElarionCatchTelemetryApi(CatchTelemetryWorker worker) {
        this.telemetry = null;
        this.worker = Objects.requireNonNull(worker, "worker");
    }

    public CatchSummary summary(UUID actorId) {
        return worker == null ? telemetry.cachedSummary(actorId) : worker.cachedSummary(actorId);
    }

    /** Completes only after the canonical catch journal append and summary projection succeed. */
    public CompletableFuture<AcceptedCatchRecord> submit(CatchTelemetryEvent event) {
        if (worker == null) throw new IllegalStateException("catch telemetry worker is unavailable");
        return worker.submit(event);
    }

    public long totalQuantity(UUID actorId) {
        return summary(actorId).totalQuantity();
    }

    public long quantityForSource(UUID actorId, Identifier sourceId) {
        return summary(actorId).quantityForSource(Objects.requireNonNull(sourceId, "sourceId"));
    }

    public long quantityForFishDefinition(UUID actorId, Identifier fishDefinitionId) {
        return summary(actorId).quantityForFishDefinition(
                Objects.requireNonNull(fishDefinitionId, "fishDefinitionId"));
    }

    public long quantityForRarity(UUID actorId, Identifier rarityId) {
        return summary(actorId).quantityForRarity(Objects.requireNonNull(rarityId, "rarityId"));
    }

    public List<AcceptedCatchRecord> recentCatches(UUID actorId) {
        return summary(actorId).recentCatches();
    }

    public CatchSpeciesSummary speciesSummary(UUID actorId, Identifier fishDefinitionId) {
        return summary(actorId).speciesSummary(Objects.requireNonNull(fishDefinitionId, "fishDefinitionId"));
    }
}
