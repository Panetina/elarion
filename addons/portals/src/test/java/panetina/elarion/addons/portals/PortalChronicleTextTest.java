package panetina.elarion.addons.portals;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PortalChronicleTextTest {
    @Test
    void routeUnlockedFamilyIsLibraryReady() {
        assertTrue(PortalChronicleText.routeUnlockedFamily().isLibraryReady());
    }

    @Test
    void routeUnlockedUsesPersistedVariantAndMetadata() {
        PublicHistoryEntry entry = entry(Map.of(
                "routeId", "realm1_worldheart",
                "chronicle.variant", "portal.route-unlocked.01"));

        ChronicleProjection projection = PortalChronicleText.project(entry);

        assertEquals("Portal Route Opened", projection.title());
        assertEquals("The route realm1_worldheart opened to travelers.", projection.body());
        assertEquals("Portal", projection.category());
        assertEquals("Route unlocked", projection.detailLabel());
        assertEquals("portal.route-unlocked.01", projection.variantId());
    }

    @Test
    void routeUnlockedVariantSelectionIsStable() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                1L,
                "live-index",
                "portal",
                "route-unlocked",
                null,
                "portal_route",
                "realm1_worldheart",
                "",
                Map.of("routeId", "realm1_worldheart"),
                "The portal route realm1_worldheart recorded route unlocked.");

        ChronicleProjection first = PortalChronicleText.project(entry);
        ChronicleProjection second = PortalChronicleText.project(entry);

        assertEquals(first.variantId(), second.variantId());
        assertEquals(first.body(), second.body());
    }

    @Test
    void routeUnlockedFallsBackWithoutRouteId() {
        ChronicleProjection projection = PortalChronicleText.project(entry(Map.of()));

        assertEquals("A portal route opened.", projection.body());
    }

    private static PublicHistoryEntry entry(Map<String, String> metadata) {
        return new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "portal",
                "route-unlocked",
                null,
                "portal_route",
                "realm1_worldheart",
                "",
                metadata,
                "The portal route realm1_worldheart recorded route unlocked.");
    }
}
