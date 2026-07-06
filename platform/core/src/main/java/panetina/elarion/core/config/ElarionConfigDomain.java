package panetina.elarion.core.config;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record ElarionConfigDomain(
        String id,
        String ownerModule,
        String label,
        String description,
        List<String> files,
        String reloadCommand,
        List<ElarionConfigCategory> categories
) {
    public ElarionConfigDomain {
        id = ElarionConfigEntry.normalizeId(id, "Config domain id");
        ownerModule = ownerModule == null || ownerModule.isBlank() ? id : ownerModule.trim();
        label = label == null || label.isBlank() ? id : label.trim();
        description = description == null ? "" : description.trim();
        files = files == null ? List.of() : List.copyOf(files);
        reloadCommand = reloadCommand == null ? "" : reloadCommand.trim();
        categories = categories == null ? List.of() : List.copyOf(categories);
        Set<String> ids = new HashSet<>();
        for (ElarionConfigCategory category : categories) {
            if (category == null) throw new IllegalArgumentException("Config domain categories must not contain null");
            if (!ids.add(category.id())) {
                throw new IllegalArgumentException("Duplicate config category id in " + id + ": " + category.id());
            }
        }
    }

    public Optional<ElarionConfigCategory> category(String categoryId) {
        String normalized = categoryId == null ? "" : categoryId.trim().toLowerCase(java.util.Locale.ROOT);
        return categories.stream().filter(category -> category.id().equals(normalized)).findFirst();
    }

    public Optional<ElarionConfigEntry<?>> entry(String categoryId, String entryId) {
        return category(categoryId).flatMap(category -> category.entry(entryId));
    }
}

