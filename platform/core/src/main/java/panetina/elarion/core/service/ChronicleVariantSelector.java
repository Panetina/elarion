package panetina.elarion.core.service;

import panetina.elarion.core.model.ChronicleTemplate;
import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ChronicleVariantSelector {
    public String selectVariantId(PublicHistoryEntry entry, ChronicleTemplateFamily family) {
        String persisted = entry == null
                ? ""
                : entry.metadata().getOrDefault(ChronicleRendererRegistry.VARIANT_METADATA_KEY, "").trim();
        if (!persisted.isBlank()) return persisted;
        if (family == null) return ChronicleRendererRegistry.selectedVariantId(entry, "core.default");
        return selectTemplate(entry, family).variantId();
    }

    public ChronicleTemplate selectTemplate(PublicHistoryEntry entry, ChronicleTemplateFamily family) {
        if (family == null) return new ChronicleTemplate("core.default", "");
        if (family.templates().isEmpty()) return family.fallbackTemplate();
        UUID eventId = entry == null ? UUID.nameUUIDFromBytes(family.familyId().getBytes(StandardCharsets.UTF_8)) : entry.eventId();
        int index = Math.floorMod((eventId + ":" + family.familyId()).hashCode(), family.templates().size());
        return family.templates().get(index);
    }
}
