package panetina.elarion.addons.angling.minigame;

import panetina.elarion.addons.angling.network.AnglingMinigameInputAction;
import panetina.elarion.addons.angling.network.AnglingMinigameInputPayload;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Bounded server-side replay, ownership, transition, lifetime, and rate gate.
 * One instance belongs to exactly one active fishing session.
 */
public final class AnglingMinigameInputGate {
    public static final int WINDOW_TICKS = 20;
    public static final int MAX_EDGES_PER_TICK = 4;
    public static final int MAX_EDGES_PER_WINDOW = 40;
    public static final long MAX_LIFETIME_TICKS = 72_000L;

    private final UUID sessionId;
    private final UUID actorId;
    private final int bobberEntityId;
    private final long expiresAtTick;
    private final long[] bucketTicks = new long[WINDOW_TICKS];
    private final int[] bucketCounts = new int[WINDOW_TICKS];

    private int windowCount;
    private int lastSequence = -1;
    private long lastServerTick;
    private boolean pressed;
    private boolean closed;

    public AnglingMinigameInputGate(
            UUID sessionId,
            UUID actorId,
            int bobberEntityId,
            long openedAtTick,
            long lifetimeTicks
    ) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        if (bobberEntityId < 0) throw new IllegalArgumentException("bobberEntityId must be non-negative");
        if (openedAtTick < 0) throw new IllegalArgumentException("openedAtTick must be non-negative");
        if (lifetimeTicks < 1 || lifetimeTicks > MAX_LIFETIME_TICKS) {
            throw new IllegalArgumentException("lifetimeTicks outside bounded range");
        }
        this.bobberEntityId = bobberEntityId;
        this.lastServerTick = openedAtTick;
        this.expiresAtTick = Math.addExact(openedAtTick, lifetimeTicks);
        Arrays.fill(bucketTicks, Long.MIN_VALUE);
    }

    public Result accept(UUID senderId, AnglingMinigameInputPayload payload, long serverTick) {
        Objects.requireNonNull(senderId, "senderId");
        Objects.requireNonNull(payload, "payload");
        if (closed) return Result.CLOSED;
        if (!actorId.equals(senderId)) return Result.WRONG_ACTOR;
        if (!sessionId.equals(payload.sessionId())) return Result.WRONG_SESSION;
        if (bobberEntityId != payload.bobberEntityId()) return Result.WRONG_BOBBER;
        if (serverTick < lastServerTick) return Result.STALE_SERVER_TICK;
        if (serverTick > expiresAtTick) {
            closed = true;
            pressed = false;
            return Result.EXPIRED;
        }
        if (payload.sequence() < 0) return Result.INVALID_SEQUENCE;
        if (payload.sequence() <= lastSequence) return Result.REPLAYED;
        if (payload.sequence() != lastSequence + 1) return Result.OUT_OF_ORDER;
        if (payload.action() == AnglingMinigameInputAction.INVALID) return Result.INVALID_ACTION;
        if ((payload.action() == AnglingMinigameInputAction.PRESS && pressed)
                || (payload.action() == AnglingMinigameInputAction.RELEASE && !pressed)) {
            return Result.IMPOSSIBLE_TRANSITION;
        }

        lastServerTick = serverTick;
        pruneWindow(serverTick);
        int bucket = (int) (serverTick % WINDOW_TICKS);
        if (bucketTicks[bucket] != serverTick) {
            windowCount -= bucketCounts[bucket];
            bucketTicks[bucket] = serverTick;
            bucketCounts[bucket] = 0;
        }
        if (bucketCounts[bucket] >= MAX_EDGES_PER_TICK || windowCount >= MAX_EDGES_PER_WINDOW) {
            return Result.RATE_LIMITED;
        }

        bucketCounts[bucket]++;
        windowCount++;
        lastSequence = payload.sequence();
        switch (payload.action()) {
            case PRESS -> pressed = true;
            case RELEASE -> pressed = false;
            case LAYER_PREVIOUS, LAYER_NEXT -> {
                // Discrete server-validated selection edges do not alter hit-button state.
            }
            case ABANDON -> {
                pressed = false;
                closed = true;
            }
            case INVALID -> throw new IllegalStateException("invalid action passed validation");
        }
        return Result.ACCEPTED;
    }

    private void pruneWindow(long serverTick) {
        long oldestRetainedTick = serverTick - WINDOW_TICKS + 1;
        for (int index = 0; index < WINDOW_TICKS; index++) {
            if (bucketTicks[index] < oldestRetainedTick) {
                windowCount -= bucketCounts[index];
                bucketCounts[index] = 0;
                bucketTicks[index] = Long.MIN_VALUE;
            }
        }
    }

    public void close() {
        closed = true;
        pressed = false;
    }

    public boolean isPressed() {
        return pressed;
    }

    public boolean isClosed() {
        return closed;
    }

    public int lastSequence() {
        return lastSequence;
    }

    public enum Result {
        ACCEPTED,
        CLOSED,
        WRONG_ACTOR,
        WRONG_SESSION,
        WRONG_BOBBER,
        STALE_SERVER_TICK,
        EXPIRED,
        INVALID_SEQUENCE,
        REPLAYED,
        OUT_OF_ORDER,
        INVALID_ACTION,
        IMPOSSIBLE_TRANSITION,
        RATE_LIMITED
    }
}
