package panetina.elarion.core.model;

import java.util.Map;
import java.util.LinkedHashMap;
import panetina.elarion.core.placeholder.ElarionPlaceholderService;
import panetina.elarion.core.placeholder.PlaceholderRenderContext;

public record ChronicleTemplate(
        String variantId,
        String body
) {
    public ChronicleTemplate {
        variantId = clean(variantId, "default");
        body = clean(body, "");
    }

    public String render(PublicHistoryEntry entry, ChronicleRenderContext context, String fallbackBody) {
        String rendered = body.isBlank() ? clean(fallbackBody, "") : body;
        Map<String, String> values = new LinkedHashMap<>(entry == null ? Map.of() : entry.metadata());
        values.putIfAbsent("actor", context == null ? "" : context.actorName());
        values.putIfAbsent("eventText", entry == null ? "" : entry.text());
        values.putIfAbsent("category", entry == null ? "" : entry.category());
        values.putIfAbsent("type", entry == null ? "" : entry.type());
        values.putIfAbsent("realm", entry == null ? "" : entry.realmId());
        rendered = ElarionPlaceholderService.resolveSchema(rendered, "chronicle:" + variantId,
                PlaceholderRenderContext.CHRONICLE, values).text();
        return clean(rendered, fallbackBody);
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
