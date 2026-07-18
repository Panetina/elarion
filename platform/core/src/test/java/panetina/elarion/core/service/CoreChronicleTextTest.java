package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CoreChronicleTextTest {
    @Test
    void titleProgressionUnlockedFamilyIsLibraryReady() {
        assertTrue(CoreChronicleText.titleProgressionUnlockedFamily().isLibraryReady());
    }

    @Test
    void titleProgressionUnlockedUsesPersistedVariantAndContext() {
        PublicHistoryEntry entry = entry(UUID.randomUUID(), Map.of(
                "title", "monarch",
                "chronicle.variant", "title.progression-unlocked.01"));

        ChronicleProjection projection = CoreChronicleText.project(entry, new ChronicleRenderContext("Mara"));

        assertEquals("Title Unlocked", projection.title());
        assertEquals("Mara unlocked the title monarch.", projection.body());
        assertEquals("Title", projection.category());
        assertEquals("Progression reward", projection.detailLabel());
        assertEquals("title.progression-unlocked.01", projection.variantId());
    }

    @Test
    void titleProgressionUnlockedVariantSelectionIsStable() {
        PublicHistoryEntry entry = entry(UUID.fromString("66666666-6666-6666-6666-666666666666"),
                Map.of("title", "cleric"));

        ChronicleProjection first = CoreChronicleText.project(entry, new ChronicleRenderContext("Mara"));
        ChronicleProjection second = CoreChronicleText.project(entry, new ChronicleRenderContext("Mara"));

        assertEquals(first.variantId(), second.variantId());
        assertEquals(first.body(), second.body());
    }

    @Test
    void titleProgressionUnlockedFallsBackWithoutTitle() {
        ChronicleProjection projection = CoreChronicleText.project(
                entry(UUID.randomUUID(), Map.of()), new ChronicleRenderContext("Mara"));

        assertEquals("A title was unlocked through progression.", projection.body());
    }

    private static PublicHistoryEntry entry(UUID eventId, Map<String, String> metadata) {
        return new PublicHistoryEntry(
                eventId,
                1L,
                "live-index",
                "title",
                "progression-unlocked",
                null,
                "player",
                "player-1",
                "realm1",
                metadata,
                "Mara unlocked a title through their deeds.");
    }
}
