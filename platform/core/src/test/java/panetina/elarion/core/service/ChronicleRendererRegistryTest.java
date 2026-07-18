package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ChronicleRendererRegistryTest {
    @Test
    void fallbackProjectionUsesEventTextAndDeterministicDefaultVariant() {
        ChronicleRendererRegistry registry = new ChronicleRendererRegistry();
        PublicHistoryEntry entry = entry(Map.of(), "world", "portal-route-opened", "A Nether route opened.");

        ChronicleProjection projection = registry.project(entry, ChronicleRenderContext.EMPTY);

        assertEquals("Portal Route Opened", projection.title());
        assertEquals("A Nether route opened.", projection.body());
        assertEquals("World", projection.category());
        assertEquals("Chronicle record", projection.detailLabel());
        assertEquals("world.portal-route-opened.default", projection.variantId());
    }

    @Test
    void persistedVariantIdOverridesDerivedDefault() {
        ChronicleRendererRegistry registry = new ChronicleRendererRegistry();
        PublicHistoryEntry entry = entry(
                Map.of(ChronicleRendererRegistry.VARIANT_METADATA_KEY, "world.portal-route-opened.03"),
                "world",
                "portal-route-opened",
                "A Nether route opened.");

        ChronicleProjection projection = registry.project(entry, ChronicleRenderContext.EMPTY);

        assertEquals("world.portal-route-opened.03", projection.variantId());
    }

    @Test
    void registeredRendererCanProjectSupportedEntries() {
        ChronicleRendererRegistry registry = new ChronicleRendererRegistry();
        registry.register(new panetina.elarion.core.model.ChronicleRenderer() {
            @Override
            public boolean supports(PublicHistoryEntry entry) {
                return entry != null && entry.category().equals("test");
            }

            @Override
            public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
                return new ChronicleProjection("Rendered", context.actorName(), "Test", "Projected", "test.01");
            }
        });

        ChronicleProjection projection = registry.project(entry(Map.of(), "test", "event", "fallback"),
                new ChronicleRenderContext("Mara"));

        assertEquals("Rendered", projection.title());
        assertEquals("Mara", projection.body());
        assertEquals("test.01", projection.variantId());
    }

    private static PublicHistoryEntry entry(
            Map<String, String> metadata,
            String category,
            String type,
            String text
    ) {
        return new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                category,
                type,
                null,
                "subject",
                "subject-1",
                "realm1",
                metadata,
                text);
    }
}
