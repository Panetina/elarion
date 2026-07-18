package panetina.elarion.core.model;

public record ChronicleProjection(
        String title,
        String body,
        String category,
        String detailLabel,
        String variantId
) {
    public ChronicleProjection {
        title = clean(title, "Chronicle Event");
        body = clean(body, "");
        category = clean(category, "Chronicle");
        detailLabel = clean(detailLabel, "Chronicle record");
        variantId = clean(variantId, "core.default");
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
