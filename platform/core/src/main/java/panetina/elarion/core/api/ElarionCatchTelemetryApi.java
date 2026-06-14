package panetina.elarion.core.api;

import net.minecraft.util.Identifier;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.service.CatchTelemetryService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ElarionCatchTelemetryApi {
    private final CatchTelemetryService telemetry;

    ElarionCatchTelemetryApi(CatchTelemetryService telemetry) {
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry");
    }

    public CatchSummary summary(UUID actorId) {
        return telemetry.summary(actorId);
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
}
