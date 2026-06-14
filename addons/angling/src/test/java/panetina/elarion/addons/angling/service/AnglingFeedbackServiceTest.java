package panetina.elarion.addons.angling.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingFeedbackServiceTest {
    @Test
    void limitsEachFeedbackTypeIndependently() {
        AtomicLong clock = new AtomicLong(100);
        AnglingFeedbackService feedback = new AnglingFeedbackService(clock::get);
        UUID actorId = UUID.randomUUID();

        assertTrue(feedback.shouldNotify(
                actorId,
                AnglingFeedbackService.FeedbackType.ACCEPTED));
        assertFalse(feedback.shouldNotify(
                actorId,
                AnglingFeedbackService.FeedbackType.ACCEPTED));
        assertTrue(feedback.shouldNotify(
                actorId,
                AnglingFeedbackService.FeedbackType.UNAVAILABLE));

        clock.addAndGet(AnglingFeedbackService.ACCEPTED_COOLDOWN_MILLIS);
        assertTrue(feedback.shouldNotify(
                actorId,
                AnglingFeedbackService.FeedbackType.ACCEPTED));
        assertFalse(feedback.shouldNotify(
                actorId,
                AnglingFeedbackService.FeedbackType.UNAVAILABLE));
    }

    @Test
    void clearRemovesOnlyTheRequestedPlayer() {
        AtomicLong clock = new AtomicLong(100);
        AnglingFeedbackService feedback = new AnglingFeedbackService(clock::get);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        feedback.shouldNotify(first, AnglingFeedbackService.FeedbackType.ACCEPTED);
        feedback.shouldNotify(first, AnglingFeedbackService.FeedbackType.UNAVAILABLE);
        feedback.shouldNotify(second, AnglingFeedbackService.FeedbackType.ACCEPTED);

        feedback.clear(first);

        assertEquals(1, feedback.trackedEntryCount());
        assertTrue(feedback.shouldNotify(
                first,
                AnglingFeedbackService.FeedbackType.ACCEPTED));
        assertFalse(feedback.shouldNotify(
                second,
                AnglingFeedbackService.FeedbackType.ACCEPTED));
    }

    @Test
    void clearAllDropsBoundedEphemeralState() {
        AnglingFeedbackService feedback = new AnglingFeedbackService(() -> 100);
        feedback.shouldNotify(
                UUID.randomUUID(),
                AnglingFeedbackService.FeedbackType.ACCEPTED);

        feedback.clear();

        assertEquals(0, feedback.trackedEntryCount());
    }
}
