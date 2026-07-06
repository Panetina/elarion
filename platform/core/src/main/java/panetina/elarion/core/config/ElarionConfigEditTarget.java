package panetina.elarion.core.config;

public record ElarionConfigEditTarget(
        String domainId,
        String categoryId,
        String entryId
) {
    public ElarionConfigEditTarget {
        domainId = ElarionConfigEntry.normalizeId(domainId, "Config edit domain id");
        categoryId = ElarionConfigEntry.normalizeId(categoryId, "Config edit category id");
        entryId = ElarionConfigEntry.normalizeId(entryId, "Config edit entry id");
    }

    public String targetKey() {
        return domainId + ":" + categoryId + ":" + entryId;
    }
}
