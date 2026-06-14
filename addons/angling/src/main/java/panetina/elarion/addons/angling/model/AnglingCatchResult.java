package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.Map;
import java.util.UUID;

public record AnglingCatchResult(
        UUID eventId,
        long occurredAt,
        UUID actorId,
        Identifier sourceId,
        Identifier fishDefinitionId,
        Identifier rarityId,
        long quantity,
        Identifier worldId,
        Identifier dimensionId,
        Identifier biomeId
) {
    public AnglingCatchResult {
        new CatchTelemetryEvent(
                eventId,
                occurredAt,
                actorId,
                sourceId,
                fishDefinitionId,
                rarityId,
                quantity,
                worldId,
                dimensionId,
                biomeId,
                Map.of());
    }

    public CatchTelemetryEvent toTelemetryEvent() {
        return new CatchTelemetryEvent(
                eventId,
                occurredAt,
                actorId,
                sourceId,
                fishDefinitionId,
                rarityId,
                quantity,
                worldId,
                dimensionId,
                biomeId,
                Map.of());
    }
}
