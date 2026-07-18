package panetina.elarion.core.placeholder;

import java.util.Map;
import java.util.UUID;

public record PlaceholderResolutionContext(
        PlaceholderRenderContext renderContext,
        UUID viewerId,
        UUID subjectId,
        String viewerRealmId,
        String subjectRealmId,
        boolean admin,
        Map<String, String> values
) {
    public PlaceholderResolutionContext {
        renderContext = renderContext == null ? PlaceholderRenderContext.UI : renderContext;
        viewerRealmId = clean(viewerRealmId);
        subjectRealmId = clean(subjectRealmId);
        values = values == null ? Map.of() : Map.copyOf(values);
    }

    public static PlaceholderResolutionContext publicContext(PlaceholderRenderContext context,
                                                             Map<String, String> values) {
        return new PlaceholderResolutionContext(context, null, null, "", "", false, values);
    }

    public String value(String key) {
        return values.getOrDefault(key, "");
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
