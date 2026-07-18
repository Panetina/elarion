package panetina.elarion.core.model.profile;

import java.util.List;
import java.util.Locale;

public record CitizenProfileSection(
        String id,
        String title,
        String sourceSystem,
        ProfileVisibility visibility,
        List<CitizenProfileField> fields,
        List<CitizenProfileCard> cards
) {
    public CitizenProfileSection {
        id = normalizeId(id);
        if (id.isBlank()) throw new IllegalArgumentException("Profile section id cannot be blank");
        title = safe(title);
        sourceSystem = safe(sourceSystem);
        visibility = visibility == null ? ProfileVisibility.PUBLIC : visibility;
        fields = fields == null ? List.of() : List.copyOf(fields.stream()
                .filter(field -> field != null)
                .toList());
        cards = cards == null ? List.of() : List.copyOf(cards.stream()
                .filter(card -> card != null)
                .toList());
    }

    public CitizenProfileSection(
            String id,
            String title,
            String sourceSystem,
            ProfileVisibility visibility,
            List<CitizenProfileField> fields
    ) {
        this(id, title, sourceSystem, visibility, fields, List.of());
    }

    private static String normalizeId(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
