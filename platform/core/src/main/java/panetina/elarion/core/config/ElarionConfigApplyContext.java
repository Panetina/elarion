package panetina.elarion.core.config;

import java.util.Objects;

public record ElarionConfigApplyContext(
        ElarionConfigRegistry registry,
        ElarionConfigDomain domain,
        ElarionConfigCategory category,
        ElarionConfigEntry<?> entry,
        ElarionConfigChangeRequest request
) {
    public ElarionConfigApplyContext {
        registry = Objects.requireNonNull(registry, "Config registry is required");
        domain = Objects.requireNonNull(domain, "Config apply domain is required");
        category = Objects.requireNonNull(category, "Config apply category is required");
        entry = Objects.requireNonNull(entry, "Config apply entry is required");
        request = Objects.requireNonNull(request, "Config apply request is required");
        if (!request.domainId().equals(domain.id())) {
            throw new IllegalArgumentException("Config apply request domain does not match context");
        }
        if (!request.categoryId().equals(category.id())) {
            throw new IllegalArgumentException("Config apply request category does not match context");
        }
        if (!request.entryId().equals(entry.id())) {
            throw new IllegalArgumentException("Config apply request entry does not match context");
        }
    }
}
