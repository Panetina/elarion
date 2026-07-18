package panetina.elarion.addons.npcs;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NpcChronicleTextTest {
    @Test
    void storyOutcomeFamilyIsLibraryReady() {
        assertTrue(NpcChronicleText.storyOutcomeFamily().isLibraryReady());
        assertEquals(10, NpcChronicleText.storyOutcomeFamily().templates().size());
    }

    @Test
    void rendersPersistedVariantWithStructuredMetadata() {
        PublicHistoryEntry entry = new PublicHistoryEntry(UUID.randomUUID(), 10L, "live-index",
                "npc", "story-outcome", UUID.randomUUID(), "npc", UUID.randomUUID().toString(), "realm1",
                Map.of("actor", "Mara", "npc", "Tavin", "outcome", "an honest alliance",
                        "chronicle.variant", "npc.story-outcome.01"), "");

        var projection = NpcChronicleText.INSTANCE.render(entry, ChronicleRenderContext.EMPTY);

        assertEquals("npc.story-outcome.01", projection.variantId());
        assertEquals("Mara reached an honest alliance in Tavin's story.", projection.body());
    }

    @Test
    void missingContextUsesSafeFallback() {
        PublicHistoryEntry entry = new PublicHistoryEntry(UUID.randomUUID(), 10L, "live-index",
                "npc", "story-outcome", null, "npc", "npc", "", Map.of(), "");

        var projection = NpcChronicleText.INSTANCE.render(entry, ChronicleRenderContext.EMPTY);

        assertFalse(projection.body().isBlank());
    }
}
