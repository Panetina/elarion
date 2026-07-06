package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public record AnglingFishingSession(
        UUID sessionId,
        UUID eventId,
        UUID actorId,
        Identifier fishDefinitionId,
        Identifier rarityId,
        Identifier worldId,
        Identifier dimensionId,
        Identifier biomeId,
        Identifier baitId,
        long startedAt,
        long expiresAt,
        long completionStartedAt
) {
    public AnglingFishingSession {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(fishDefinitionId, "fishDefinitionId");
        Objects.requireNonNull(rarityId, "rarityId");
        Objects.requireNonNull(worldId, "worldId");
        Objects.requireNonNull(dimensionId, "dimensionId");
        Objects.requireNonNull(biomeId, "biomeId");
        if (startedAt <= 0) throw new IllegalArgumentException("startedAt must be positive");
        if (expiresAt <= startedAt) {
            throw new IllegalArgumentException("expiresAt must be after startedAt");
        }
        if (completionStartedAt < 0) {
            throw new IllegalArgumentException("completionStartedAt must not be negative");
        }
        if (completionStartedAt > 0 && completionStartedAt < startedAt) {
            throw new IllegalArgumentException("completionStartedAt must not precede startedAt");
        }
    }

    public boolean completionPending() {
        return completionStartedAt > 0;
    }

    public AnglingFishingSession beginCompletion(long occurredAt) {
        if (completionPending()) return this;
        return new AnglingFishingSession(
                sessionId,
                eventId,
                actorId,
                fishDefinitionId,
                rarityId,
                worldId,
                dimensionId,
                biomeId,
                baitId,
                startedAt,
                expiresAt,
                occurredAt);
    }
}
