package panetina.elarion.core.config;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record ElarionConfigCategory(
        String id,
        String label,
        String description,
        List<ElarionConfigEntry<?>> entries
) {
    public ElarionConfigCategory {
        id = ElarionConfigEntry.normalizeId(id, "Config category id");
        label = label == null || label.isBlank() ? id : label.trim();
        description = description == null ? "" : description.trim();
        entries = entries == null ? List.of() : List.copyOf(entries);
        Set<String> ids = new HashSet<>();
        for (ElarionConfigEntry<?> entry : entries) {
            if (entry == null) throw new IllegalArgumentException("Config category entries must not contain null");
            if (!ids.add(entry.id())) {
                throw new IllegalArgumentException("Duplicate config entry id in " + id + ": " + entry.id());
            }
        }
    }

    public Optional<ElarionConfigEntry<?>> entry(String entryId) {
        String normalized = entryId == null ? "" : entryId.trim().toLowerCase(java.util.Locale.ROOT);
        return entries.stream().filter(entry -> entry.id().equals(normalized)).findFirst();
    }
}

