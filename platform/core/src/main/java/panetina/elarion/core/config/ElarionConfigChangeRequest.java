package panetina.elarion.core.config;

import java.util.UUID;

public record ElarionConfigChangeRequest(
        String domainId,
        String categoryId,
        String entryId,
        String proposedValue,
        String expectedCurrentValue,
        UUID actorId,
        String reason
) {
    public ElarionConfigChangeRequest {
        domainId = ElarionConfigEntry.normalizeId(domainId, "Config change domain id");
        categoryId = ElarionConfigEntry.normalizeId(categoryId, "Config change category id");
        entryId = ElarionConfigEntry.normalizeId(entryId, "Config change entry id");
        proposedValue = proposedValue == null ? "" : proposedValue;
        expectedCurrentValue = expectedCurrentValue == null ? "" : expectedCurrentValue;
        reason = reason == null ? "" : reason.trim();
    }

    public String targetKey() {
        return domainId + ":" + categoryId + ":" + entryId;
    }
}
