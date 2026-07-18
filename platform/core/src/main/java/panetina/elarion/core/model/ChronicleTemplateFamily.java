package panetina.elarion.core.model;

import java.util.List;
import java.util.Set;

public record ChronicleTemplateFamily(
        String familyId,
        String category,
        Set<String> types,
        String title,
        String categoryLabel,
        String detailLabel,
        String missingContextBody,
        List<ChronicleTemplate> templates,
        Set<String> requiredMetadata,
        Set<String> optionalMetadata
) {
    public ChronicleTemplateFamily {
        familyId = clean(familyId, "core.default");
        category = clean(category, "");
        types = types == null ? Set.of() : Set.copyOf(types);
        title = clean(title, "Chronicle Event");
        categoryLabel = clean(categoryLabel, "Chronicle");
        detailLabel = clean(detailLabel, "Chronicle record");
        missingContextBody = clean(missingContextBody, "");
        templates = templates == null ? List.of() : List.copyOf(templates);
        requiredMetadata = requiredMetadata == null ? Set.of() : Set.copyOf(requiredMetadata);
        optionalMetadata = optionalMetadata == null ? Set.of() : Set.copyOf(optionalMetadata);
    }

    public boolean supports(PublicHistoryEntry entry) {
        if (entry == null) return false;
        return category.equals(entry.category()) && (types.isEmpty() || types.contains(entry.type()));
    }

    public boolean isLibraryReady() {
        return templates.size() >= 10;
    }

    public boolean hasRequiredMetadata(PublicHistoryEntry entry) {
        if (entry == null) return requiredMetadata.isEmpty();
        return entry.metadata().keySet().containsAll(requiredMetadata);
    }

    public ChronicleTemplate fallbackTemplate() {
        return templates.isEmpty()
                ? new ChronicleTemplate(familyId + ".default", missingContextBody)
                : templates.getFirst();
    }

    public ChronicleTemplate templateByVariantId(String variantId) {
        if (variantId != null && !variantId.isBlank()) {
            for (ChronicleTemplate template : templates) {
                if (template.variantId().equals(variantId.trim())) return template;
            }
        }
        return fallbackTemplate();
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
