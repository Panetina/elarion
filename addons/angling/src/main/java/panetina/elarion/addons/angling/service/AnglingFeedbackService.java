package panetina.elarion.addons.angling.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class AnglingFeedbackService {
    public static final long ACCEPTED_COOLDOWN_MILLIS = 1_000L;
    public static final long UNAVAILABLE_COOLDOWN_MILLIS = 5_000L;

    private final LongSupplier clock;
    private final Map<UUID, FeedbackTimes> feedbackByActor = new HashMap<>();

    public AnglingFeedbackService() {
        this(System::currentTimeMillis);
    }

    AnglingFeedbackService(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void accepted(ServerPlayerEntity player) {
        notify(player, FeedbackType.ACCEPTED);
    }

    public void unavailable(ServerPlayerEntity player) {
        notify(player, FeedbackType.UNAVAILABLE);
    }

    public synchronized void clear(UUID actorId) {
        feedbackByActor.remove(Objects.requireNonNull(actorId, "actorId"));
    }

    public synchronized void clear() {
        feedbackByActor.clear();
    }

    synchronized boolean shouldNotify(UUID actorId, FeedbackType type) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(type, "type");
        long now = clock.getAsLong();
        if (now < 0) throw new IllegalStateException("Feedback clock must not be negative");

        FeedbackTimes current = feedbackByActor.getOrDefault(actorId, FeedbackTimes.EMPTY);
        long previous = current.lastSentAt(type);
        if (previous >= 0 && now - previous < type.cooldownMillis()) return false;
        feedbackByActor.put(actorId, current.with(type, now));
        return true;
    }

    synchronized int trackedEntryCount() {
        return feedbackByActor.size();
    }

    private void notify(ServerPlayerEntity player, FeedbackType type) {
        Objects.requireNonNull(player, "player");
        if (!shouldNotify(player.getUuid(), type)) return;
        player.sendMessage(
                Text.translatable(type.translationKey()).formatted(type.formatting()),
                true);
    }

    enum FeedbackType {
        ACCEPTED(
                "feedback.elarion_angling.catch_accepted",
                ACCEPTED_COOLDOWN_MILLIS,
                Formatting.GREEN),
        UNAVAILABLE(
                "feedback.elarion_angling.catch_unavailable",
                UNAVAILABLE_COOLDOWN_MILLIS,
                Formatting.YELLOW);

        private final String translationKey;
        private final long cooldownMillis;
        private final Formatting formatting;

        FeedbackType(String translationKey, long cooldownMillis, Formatting formatting) {
            this.translationKey = translationKey;
            this.cooldownMillis = cooldownMillis;
            this.formatting = formatting;
        }

        String translationKey() {
            return translationKey;
        }

        long cooldownMillis() {
            return cooldownMillis;
        }

        Formatting formatting() {
            return formatting;
        }
    }

    private record FeedbackTimes(long acceptedAt, long unavailableAt) {
        private static final FeedbackTimes EMPTY = new FeedbackTimes(-1, -1);

        long lastSentAt(FeedbackType type) {
            return type == FeedbackType.ACCEPTED ? acceptedAt : unavailableAt;
        }

        FeedbackTimes with(FeedbackType type, long sentAt) {
            return type == FeedbackType.ACCEPTED
                    ? new FeedbackTimes(sentAt, unavailableAt)
                    : new FeedbackTimes(acceptedAt, sentAt);
        }
    }
}
