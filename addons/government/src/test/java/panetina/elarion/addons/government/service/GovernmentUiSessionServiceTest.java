package panetina.elarion.addons.government.service;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentUiSessionServiceTest {
    private final GovernmentUiSessionService sessions = new GovernmentUiSessionService(100L, 64.0D);
    private final UUID player = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void validatesMatchingSessionNearOriginalBlock() {
        GovernmentUiSessionService.Session session = sessions.create(
                player, "civic_forum", "realm1", "elarion:realm1", new BlockPos(10, 64, 10), 1000L);

        assertTrue(sessions.validate(
                player, session.id(), "realm1", "civic_forum", "elarion:realm1",
                10.5D, 64.5D, 10.5D, 1010L).isPresent());
    }

    @Test
    void rejectsWrongPlayerRealmBlockWorldAndRange() {
        GovernmentUiSessionService.Session session = sessions.create(
                player, "seat_of_rule", "realm1", "elarion:realm1", new BlockPos(10, 64, 10), 1000L);

        assertTrue(sessions.validate(
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                session.id(), "realm1", "seat_of_rule", "elarion:realm1",
                10.5D, 64.5D, 10.5D, 1010L).isEmpty());
        assertTrue(sessions.validate(
                player, session.id(), "realm2", "seat_of_rule", "elarion:realm1",
                10.5D, 64.5D, 10.5D, 1010L).isEmpty());
        assertTrue(sessions.validate(
                player, session.id(), "realm1", "civic_forum", "elarion:realm1",
                10.5D, 64.5D, 10.5D, 1010L).isEmpty());
        assertTrue(sessions.validate(
                player, session.id(), "realm1", "seat_of_rule", "elarion:realm2",
                10.5D, 64.5D, 10.5D, 1010L).isEmpty());
        assertTrue(sessions.validate(
                player, session.id(), "realm1", "seat_of_rule", "elarion:realm1",
                25.5D, 64.5D, 10.5D, 1010L).isEmpty());
    }

    @Test
    void rejectsExpiredSessionAndCleansIt() {
        GovernmentUiSessionService.Session session = sessions.create(
                player, "civic_forum", "realm1", "elarion:realm1", new BlockPos(10, 64, 10), 1000L);

        assertTrue(sessions.validate(
                player, session.id(), "realm1", "civic_forum", "elarion:realm1",
                10.5D, 64.5D, 10.5D, 1200L).isEmpty());
        assertEquals(0, sessions.size());
    }
}
