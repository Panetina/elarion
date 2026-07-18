package panetina.elarion.addons.underworld;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnderworldChronicleTextTest {
    @Test
    void deathFamiliesAreLibraryReady() {
        assertTrue(UnderworldChronicleText.pveDeathFamily().isLibraryReady());
        assertTrue(UnderworldChronicleText.pvpDeathFamily().isLibraryReady());
        assertTrue(UnderworldChronicleText.suicideDeathFamily().isLibraryReady());
        assertTrue(UnderworldChronicleText.trueDeathFamily().isLibraryReady());
    }

    @Test
    void pvpDeathUsesPersistedVariantAndContext() {
        PublicHistoryEntry entry = entry("death-pvp", Map.of(
                "deathType", "PVP",
                "chronicle.variant", "underworld.death-pvp.03"));

        ChronicleProjection projection = UnderworldChronicleText.project(entry, new ChronicleRenderContext("Mara"));

        assertEquals("Death Recorded", projection.title());
        assertEquals("Mara fell by another player's hand.", projection.body());
        assertEquals("Underworld", projection.category());
        assertEquals("PVP death", projection.detailLabel());
        assertEquals("underworld.death-pvp.03", projection.variantId());
    }

    @Test
    void pveDeathVariantSelectionIsStable() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.fromString("77777777-7777-7777-7777-777777777777"),
                1L,
                "live-index",
                "underworld",
                "death-pve",
                UUID.randomUUID(),
                "corpse",
                "corpse-1",
                "realm1",
                Map.of("deathType", "PVE"),
                "Mara died and was sent to the Underworld.");

        ChronicleProjection first = UnderworldChronicleText.project(entry, new ChronicleRenderContext("Mara"));
        ChronicleProjection second = UnderworldChronicleText.project(entry, new ChronicleRenderContext("Mara"));

        assertEquals(first.variantId(), second.variantId());
        assertEquals(first.body(), second.body());
    }

    @Test
    void suicideDeathUsesOwnFamily() {
        ChronicleProjection projection = UnderworldChronicleText.project(
                entry("death-suicide", Map.of("chronicle.variant", "underworld.death-suicide.01")),
                new ChronicleRenderContext("Mara"));

        assertEquals("Mara's own action ended their life.", projection.body());
        assertEquals("Self-inflicted death", projection.detailLabel());
    }

    @Test
    void trueDeathUsesOwnFamily() {
        ChronicleProjection projection = UnderworldChronicleText.project(
                entry("true-death", Map.of("chronicle.variant", "underworld.true-death.02")),
                new ChronicleRenderContext("Mara"));

        assertEquals("True Death claimed Mara.", projection.body());
        assertEquals("True Death", projection.detailLabel());
    }

    private static PublicHistoryEntry entry(String type, Map<String, String> metadata) {
        return new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "underworld",
                type,
                UUID.randomUUID(),
                "corpse",
                "corpse-1",
                "realm1",
                metadata,
                "Mara died and was sent to the Underworld.");
    }
}
