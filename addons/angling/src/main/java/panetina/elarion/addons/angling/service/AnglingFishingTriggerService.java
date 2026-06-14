package panetina.elarion.addons.angling.service;

import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.model.AnglingCatchResult;
import panetina.elarion.addons.angling.model.AnglingFishingSession;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class AnglingFishingTriggerService {
    private final AnglingFishingSessionService sessions;

    public AnglingFishingTriggerService(AnglingFishingSessionService sessions) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    public Optional<AnglingFishingSession> begin(
            AnglingConditionContext context,
            long selectionRoll
    ) {
        Objects.requireNonNull(context, "context");
        sessions.cancel(context.actorId());
        return sessions.start(context, selectionRoll);
    }

    public Optional<AnglingFishingSession> beginIfAbsent(
            AnglingConditionContext context,
            long selectionRoll
    ) {
        Objects.requireNonNull(context, "context");
        Optional<AnglingFishingSession> active = sessions.active(context.actorId());
        if (active.isPresent()) return active;
        return sessions.start(context, selectionRoll);
    }

    public Optional<AnglingCatchResult> complete(UUID actorId) {
        Objects.requireNonNull(actorId, "actorId");
        return sessions.active(actorId)
                .map(session -> sessions.complete(actorId, session.sessionId()));
    }

    public boolean cancel(UUID actorId) {
        return sessions.cancel(Objects.requireNonNull(actorId, "actorId"));
    }
}
