package panetina.elarion.core.model;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public record TitleActiveEffect(
        String type,
        Map<String, String> parameters
) {
    public TitleActiveEffect {
        type = normalize(type);
        parameters = parameters == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(parameters));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
