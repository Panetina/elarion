package panetina.elarion.core.service;

import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.ChronicleRenderContext;
import panetina.elarion.core.model.ChronicleRenderer;
import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;

public final class ChronicleTemplateRenderer implements ChronicleRenderer {
    private final ChronicleTemplateLibrary library;
    private final ChronicleVariantSelector selector;

    public ChronicleTemplateRenderer(ChronicleTemplateLibrary library, ChronicleVariantSelector selector) {
        this.library = library == null ? new ChronicleTemplateLibrary() : library;
        this.selector = selector == null ? new ChronicleVariantSelector() : selector;
    }

    @Override
    public boolean supports(PublicHistoryEntry entry) {
        return library.find(entry).isPresent();
    }

    @Override
    public ChronicleProjection render(PublicHistoryEntry entry, ChronicleRenderContext context) {
        ChronicleTemplateFamily family = library.find(entry).orElse(null);
        if (family == null) return null;
        String variantId = selector.selectVariantId(entry, family);
        ChronicleTemplate template = family.templateByVariantId(variantId);
        String body = family.hasRequiredMetadata(entry)
                ? template.render(entry, context == null ? ChronicleRenderContext.EMPTY : context, entry.text())
                : family.missingContextBody();
        return new ChronicleProjection(
                family.title(),
                body.isBlank() ? entry.text() : body,
                family.categoryLabel(),
                family.detailLabel(),
                variantId);
    }
}
