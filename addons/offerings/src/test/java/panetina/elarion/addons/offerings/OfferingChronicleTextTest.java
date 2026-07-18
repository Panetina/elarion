package panetina.elarion.addons.offerings;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OfferingChronicleTextTest {
    @Test
    void projectCompletedFamilyIsLibraryReady() {
        assertTrue(OfferingChronicleText.projectCompletedFamily().isLibraryReady());
        assertTrue(OfferingChronicleText.realmGlobalAccessFamily().isLibraryReady());
    }

    @Test
    void projectCompletedUsesPersistedVariantAndMetadata() {
        PublicHistoryEntry entry = entry(Map.of(
                "project", "foundation_iii",
                "chronicle.variant", "offering.project-completed.01"));

        ChronicleProjection projection = OfferingChronicleText.project(entry);

        assertEquals("Offering Project Completed", projection.title());
        assertEquals("The Offering project foundation_iii was completed.", projection.body());
        assertEquals("Offering", projection.category());
        assertEquals("Project completed", projection.detailLabel());
        assertEquals("offering.project-completed.01", projection.variantId());
    }

    @Test
    void projectCompletedVariantSelectionIsStable() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                1L,
                "live-index",
                "offering",
                "project-completed",
                null,
                "project",
                "offering_realm_oak_1",
                "oak",
                Map.of("project", "foundation_iii"),
                "The project foundation_iii recorded offering event project completed.");

        ChronicleProjection first = OfferingChronicleText.project(entry);
        ChronicleProjection second = OfferingChronicleText.project(entry);

        assertEquals(first.variantId(), second.variantId());
        assertEquals(first.body(), second.body());
    }

    @Test
    void projectCompletedFallsBackWithoutProject() {
        ChronicleProjection projection = OfferingChronicleText.project(entry(Map.of()));

        assertEquals("An Offering project was completed.", projection.body());
    }

    @Test
    void realmGlobalAccessUsesPersistedVariantAndRealm() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "offering",
                "realm-global-access-changed",
                null,
                "realm",
                "oak",
                "Kingdom of Oak",
                Map.of("chronicle.variant", "offering.realm-global-access-changed.01"),
                "The Realm oak recorded offering event realm-global-access-changed.");

        ChronicleProjection projection = OfferingChronicleText.project(entry);

        assertEquals("Realm Reached The World Stage", projection.title());
        assertEquals("Kingdom of Oak stepped onto the global stage.", projection.body());
        assertEquals("Realm global access", projection.detailLabel());
        assertEquals("offering.realm-global-access-changed.01", projection.variantId());
    }

    private static PublicHistoryEntry entry(Map<String, String> metadata) {
        return new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "offering",
                "project-completed",
                null,
                "project",
                "offering_realm_oak_1",
                "oak",
                metadata,
                "The project foundation_iii recorded offering event project completed.");
    }
}
