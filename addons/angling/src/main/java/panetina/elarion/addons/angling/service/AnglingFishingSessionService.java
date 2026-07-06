package panetina.elarion.addons.angling.service;

import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.model.AnglingCatchResult;
import panetina.elarion.addons.angling.model.AnglingFishingSession;
import panetina.elarion.addons.angling.model.FishDefinition;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class AnglingFishingSessionService {
    public static final long DEFAULT_SESSION_DURATION_MILLIS = 60_000L;
    public static final int DEFAULT_EXPIRATIONS_PER_TICK = 16;

    private final FishCandidateSelector selector;
    private final AnglingCatchResolutionService catchResolution;
    private final AnglingRewardDeliveryService rewardDelivery;
    private final Supplier<UUID> sessionIds;
    private final Supplier<UUID> eventIds;
    private final LongSupplier clock;
    private final Map<UUID, AnglingFishingSession> sessionsByActor = new HashMap<>();
    private final PriorityQueue<Expiry> expirations = new PriorityQueue<>(
            Comparator.comparingLong(Expiry::expiresAt)
                    .thenComparing(Expiry::sessionId));

    public AnglingFishingSessionService(
            FishCandidateSelector selector,
            AnglingCatchResolutionService catchResolution,
            AnglingRewardDeliveryService rewardDelivery
    ) {
        this(
                selector,
                catchResolution,
                rewardDelivery,
                UUID::randomUUID,
                UUID::randomUUID,
                System::currentTimeMillis);
    }

    AnglingFishingSessionService(
            FishCandidateSelector selector,
            AnglingCatchResolutionService catchResolution
    ) {
        this(selector, catchResolution, AnglingRewardDeliveryService.noop());
    }

    AnglingFishingSessionService(
            FishCandidateSelector selector,
            AnglingCatchResolutionService catchResolution,
            Supplier<UUID> sessionIds,
            Supplier<UUID> eventIds,
            LongSupplier clock
    ) {
        this(
                selector,
                catchResolution,
                AnglingRewardDeliveryService.noop(),
                sessionIds,
                eventIds,
                clock);
    }

    AnglingFishingSessionService(
            FishCandidateSelector selector,
            AnglingCatchResolutionService catchResolution,
            AnglingRewardDeliveryService rewardDelivery,
            Supplier<UUID> sessionIds,
            Supplier<UUID> eventIds,
            LongSupplier clock
    ) {
        this.selector = Objects.requireNonNull(selector, "selector");
        this.catchResolution = Objects.requireNonNull(catchResolution, "catchResolution");
        this.rewardDelivery = Objects.requireNonNull(rewardDelivery, "rewardDelivery");
        this.sessionIds = Objects.requireNonNull(sessionIds, "sessionIds");
        this.eventIds = Objects.requireNonNull(eventIds, "eventIds");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public synchronized Optional<AnglingFishingSession> start(
            AnglingConditionContext context,
            long selectionRoll
    ) {
        return start(context, selectionRoll, DEFAULT_SESSION_DURATION_MILLIS);
    }

    public synchronized Optional<AnglingFishingSession> start(
            AnglingConditionContext context,
            long selectionRoll,
            long durationMillis
    ) {
        Objects.requireNonNull(context, "context");
        if (durationMillis <= 0) {
            throw new IllegalArgumentException("durationMillis must be positive");
        }
        if (sessionsByActor.containsKey(context.actorId())) {
            throw new IllegalStateException(
                    "Player already has an active Angling session: " + context.actorId());
        }
        Optional<FishDefinition> selected = selector.select(context, selectionRoll);
        if (selected.isEmpty()) return Optional.empty();
        FishDefinition definition = selected.orElseThrow();

        long startedAt = clock.getAsLong();
        long expiresAt = Math.addExact(startedAt, durationMillis);
        AnglingFishingSession session = new AnglingFishingSession(
                Objects.requireNonNull(sessionIds.get(), "generated sessionId"),
                Objects.requireNonNull(eventIds.get(), "generated eventId"),
                context.actorId(),
                definition.id(),
                definition.rarity().id(),
                context.worldId(),
                context.dimensionId(),
                context.biomeId(),
                context.baitId(),
                startedAt,
                expiresAt,
                0);
        sessionsByActor.put(session.actorId(), session);
        expirations.add(new Expiry(session.expiresAt(), session.sessionId(), session.actorId()));
        return Optional.of(session);
    }

    public synchronized Optional<AnglingFishingSession> active(UUID actorId) {
        return Optional.ofNullable(sessionsByActor.get(Objects.requireNonNull(actorId, "actorId")));
    }

    public synchronized AnglingCatchResult complete(UUID actorId, UUID sessionId) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(sessionId, "sessionId");
        AnglingFishingSession current = requireSession(actorId, sessionId);
        long now = clock.getAsLong();
        if (!current.completionPending() && now >= current.expiresAt()) {
            sessionsByActor.remove(actorId);
            throw new IllegalStateException("Angling session has expired: " + sessionId);
        }

        AnglingFishingSession completing = current.beginCompletion(now);
        sessionsByActor.put(actorId, completing);
        AnglingCatchResult result = catchResolution.createResolvedResult(
                completing.eventId(),
                completing.completionStartedAt(),
                completing.actorId(),
                completing.fishDefinitionId(),
                completing.rarityId(),
                1,
                completing.worldId(),
                completing.dimensionId(),
                completing.biomeId());
        catchResolution.emit(result);
        rewardDelivery.enqueue(result);
        sessionsByActor.remove(actorId);
        return result;
    }

    public synchronized boolean cancel(UUID actorId, UUID sessionId) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(sessionId, "sessionId");
        AnglingFishingSession current = sessionsByActor.get(actorId);
        if (current == null || !current.sessionId().equals(sessionId)) return false;
        sessionsByActor.remove(actorId);
        return true;
    }

    public synchronized boolean cancel(UUID actorId) {
        return sessionsByActor.remove(Objects.requireNonNull(actorId, "actorId")) != null;
    }

    public synchronized int expireDue(long now, int maxExpirations) {
        if (now <= 0) throw new IllegalArgumentException("now must be positive");
        if (maxExpirations <= 0) {
            throw new IllegalArgumentException("maxExpirations must be positive");
        }
        int removed = 0;
        int processed = 0;
        while (processed < maxExpirations && !expirations.isEmpty()) {
            Expiry expiry = expirations.peek();
            if (expiry.expiresAt() > now) break;
            expirations.remove();
            processed++;
            AnglingFishingSession current = sessionsByActor.get(expiry.actorId());
            if (current == null || !current.sessionId().equals(expiry.sessionId())) continue;
            if (current.completionPending()) continue;
            sessionsByActor.remove(expiry.actorId());
            removed++;
        }
        return removed;
    }

    public synchronized int activeCount() {
        return sessionsByActor.size();
    }

    synchronized int pendingExpirationCount() {
        return expirations.size();
    }

    public synchronized void clear() {
        sessionsByActor.clear();
        expirations.clear();
    }

    private AnglingFishingSession requireSession(UUID actorId, UUID sessionId) {
        AnglingFishingSession session = sessionsByActor.get(actorId);
        if (session == null || !session.sessionId().equals(sessionId)) {
            throw new IllegalArgumentException("Unknown Angling session for player: " + actorId);
        }
        return session;
    }

    private record Expiry(long expiresAt, UUID sessionId, UUID actorId) {
    }
}
