package panetina.elarion.core.model.profile;

import java.util.Locale;

public record CitizenProfileCard(
        String id,
        String title,
        String body,
        ProfileVisibility visibility
) {
    public CitizenProfileCard {
        id = normalizeId(id);
        if (id.isBlank()) throw new IllegalArgumentException("Profile card id cannot be blank");
        title = safe(title);
        body = safe(body);
        visibility = visibility == null ? ProfileVisibility.PUBLIC : visibility;
    }

    private static String normalizeId(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
