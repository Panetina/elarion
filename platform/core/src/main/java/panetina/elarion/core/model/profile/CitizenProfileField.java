package panetina.elarion.core.model.profile;

import java.util.Locale;

public record CitizenProfileField(
        String id,
        String label,
        String value,
        ProfileVisibility visibility
) {
    public CitizenProfileField {
        id = normalizeId(id);
        if (id.isBlank()) throw new IllegalArgumentException("Profile field id cannot be blank");
        label = safe(label);
        value = safe(value);
        visibility = visibility == null ? ProfileVisibility.PUBLIC : visibility;
    }

    private static String normalizeId(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
