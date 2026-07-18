package panetina.elarion.core.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionRequestLimiterTest {
    private static final UUID PLAYER = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void rejectsRequestsBeyondWindowLimitAndResetsAtBoundary() {
        ElarionRequestLimiter limiter = new ElarionRequestLimiter();

        assertTrue(limiter.allow(PLAYER, "profile", 1_000L, 2, 1_000L));
        assertTrue(limiter.allow(PLAYER, "profile", 1_500L, 2, 1_000L));
        assertFalse(limiter.allow(PLAYER, "profile", 1_999L, 2, 1_000L));
        assertTrue(limiter.allow(PLAYER, "profile", 2_000L, 2, 1_000L));
    }

    @Test
    void isolatesChannelsAndClearsDisconnectedPlayers() {
        ElarionRequestLimiter limiter = new ElarionRequestLimiter();

        assertTrue(limiter.allow(PLAYER, "profile", 1_000L, 1, 1_000L));
        assertTrue(limiter.allow(PLAYER, "config", 1_000L, 1, 1_000L));
        assertEquals(2, limiter.trackedWindowCount());

        limiter.clear(PLAYER);
        assertEquals(0, limiter.trackedWindowCount());
        assertTrue(limiter.allow(PLAYER, "profile", 1_100L, 1, 1_000L));
    }
}
