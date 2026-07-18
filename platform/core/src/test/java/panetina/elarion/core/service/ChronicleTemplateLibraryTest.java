package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ChronicleTemplateLibraryTest {
    @Test
    void deterministicSelectorReturnsSameVariantForSameEventAndFamily() {
        ChronicleVariantSelector selector = new ChronicleVariantSelector();
        ChronicleTemplateFamily family = family("government.proposal-passed", List.of(
                new ChronicleTemplate("government.proposal-passed.01", "First"),
                new ChronicleTemplate("government.proposal-passed.02", "Second"),
                new ChronicleTemplate("government.proposal-passed.03", "Third")));
        PublicHistoryEntry entry = entry(UUID.fromString("11111111-1111-1111-1111-111111111111"), Map.of());

        String first = selector.selectVariantId(entry, family);
        String second = selector.selectVariantId(entry, family);

        assertEquals(first, second);
    }

    @Test
    void persistedVariantMetadataWinsOverDeterministicSelection() {
        ChronicleVariantSelector selector = new ChronicleVariantSelector();
        ChronicleTemplateFamily family = family("government.proposal-passed", List.of(
                new ChronicleTemplate("government.proposal-passed.01", "First"),
                new ChronicleTemplate("government.proposal-passed.02", "Second")));
        PublicHistoryEntry entry = entry(UUID.randomUUID(), Map.of(
                ChronicleRendererRegistry.VARIANT_METADATA_KEY, "government.proposal-passed.02"));

        assertEquals("government.proposal-passed.02", selector.selectVariantId(entry, family));
    }

    @Test
    void familyReadinessRequiresTenAuthoredVariants() {
        ChronicleTemplateFamily draft = family("government.proposal-passed", List.of(
                new ChronicleTemplate("government.proposal-passed.01", "First")));
        ChronicleTemplateFamily ready = family("government.proposal-passed", List.of(
                new ChronicleTemplate("government.proposal-passed.01", "1"),
                new ChronicleTemplate("government.proposal-passed.02", "2"),
                new ChronicleTemplate("government.proposal-passed.03", "3"),
                new ChronicleTemplate("government.proposal-passed.04", "4"),
                new ChronicleTemplate("government.proposal-passed.05", "5"),
                new ChronicleTemplate("government.proposal-passed.06", "6"),
                new ChronicleTemplate("government.proposal-passed.07", "7"),
                new ChronicleTemplate("government.proposal-passed.08", "8"),
                new ChronicleTemplate("government.proposal-passed.09", "9"),
                new ChronicleTemplate("government.proposal-passed.10", "10")));

        assertFalse(draft.isLibraryReady());
        assertTrue(ready.isLibraryReady());
    }

    @Test
    void templateRendererUsesMissingContextFallbackWhenRequiredMetadataIsAbsent() {
        ChronicleTemplateLibrary library = new ChronicleTemplateLibrary();
        library.register(new ChronicleTemplateFamily(
                "government.proposal-passed",
                "government",
                Set.of("proposal-passed"),
                "Proposal Passed",
                "Government",
                "Civic Chronicle",
                "A proposal passed, but its archived details are incomplete.",
                List.of(new ChronicleTemplate("government.proposal-passed.01", "{proposalName} passed in {realmName}.")),
                Set.of("proposalName", "realmName"),
                Set.of()));
        ChronicleTemplateRenderer renderer = new ChronicleTemplateRenderer(library, new ChronicleVariantSelector());

        ChronicleProjection projection = renderer.render(entry(UUID.randomUUID(), Map.of()), ChronicleRenderContext.EMPTY);

        assertEquals("Proposal Passed", projection.title());
        assertEquals("A proposal passed, but its archived details are incomplete.", projection.body());
        assertTrue(projection.variantId().startsWith("government.proposal-passed."));
    }

    @Test
    void templateRendererReplacesMetadataAndContextTokens() {
        ChronicleTemplateLibrary library = new ChronicleTemplateLibrary();
        library.register(new ChronicleTemplateFamily(
                "government.proposal-passed",
                "government",
                Set.of("proposal-passed"),
                "Proposal Passed",
                "Government",
                "Civic Chronicle",
                "Missing details.",
                List.of(new ChronicleTemplate("government.proposal-passed.01", "{actor} witnessed {proposalName} pass in {realmName}.")),
                Set.of("proposalName", "realmName"),
                Set.of()));
        ChronicleTemplateRenderer renderer = new ChronicleTemplateRenderer(library, new ChronicleVariantSelector());
        PublicHistoryEntry entry = entry(UUID.randomUUID(), Map.of(
                ChronicleRendererRegistry.VARIANT_METADATA_KEY, "government.proposal-passed.01",
                "proposalName", "Harbor Tax Reform",
                "realmName", "Valoria"));

        ChronicleProjection projection = renderer.render(entry, new ChronicleRenderContext("Mara"));

        assertEquals("Mara witnessed Harbor Tax Reform pass in Valoria.", projection.body());
        assertEquals("government.proposal-passed.01", projection.variantId());
    }

    private static ChronicleTemplateFamily family(String familyId, List<ChronicleTemplate> templates) {
        return new ChronicleTemplateFamily(
                familyId,
                "government",
                Set.of("proposal-passed"),
                "Proposal Passed",
                "Government",
                "Civic Chronicle",
                "Missing details.",
                templates,
                Set.of(),
                Set.of());
    }

    private static PublicHistoryEntry entry(UUID eventId, Map<String, String> metadata) {
        return new PublicHistoryEntry(
                eventId,
                1L,
                "live-index",
                "government",
                "proposal-passed",
                null,
                "proposal",
                "proposal-1",
                "realm1",
                metadata,
                "Fallback text.");
    }
}
