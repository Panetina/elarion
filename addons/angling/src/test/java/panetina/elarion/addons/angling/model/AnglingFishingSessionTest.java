package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingFishingSessionTest {
    @Test
    void beginCompletionIsImmutableAndIdempotent() {
        AnglingFishingSession session = session();

        AnglingFishingSession completing = session.beginCompletion(150);

        assertEquals(0, session.completionStartedAt());
        assertEquals(150, completing.completionStartedAt());
        assertTrue(completing.completionPending());
        assertEquals(completing, completing.beginCompletion(175));
    }

    @Test
    void rejectsInvalidTimeBounds() {
        AnglingFishingSession session = session();
        assertThrows(IllegalArgumentException.class, () -> new AnglingFishingSession(
                session.sessionId(),
                session.eventId(),
                session.actorId(),
                session.fishDefinitionId(),
                session.rarityId(),
                session.worldId(),
                session.dimensionId(),
                session.biomeId(),
                100,
                100,
                0));
        assertThrows(IllegalArgumentException.class, () -> session.beginCompletion(99));
    }

    private static AnglingFishingSession session() {
        return new AnglingFishingSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                100,
                200,
                0);
    }
}
