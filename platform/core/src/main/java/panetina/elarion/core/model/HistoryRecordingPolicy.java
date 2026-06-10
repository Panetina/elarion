package panetina.elarion.core.model;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record HistoryRecordingPolicy(
        boolean enabled,
        boolean defaultCategoryEnabled,
        Set<String> enabledCategories,
        Set<String> disabledCategories,
        boolean defaultTypeEnabled,
        Set<String> enabledTypes,
        Set<String> disabledTypes
) {
    public HistoryRecordingPolicy {
        enabledCategories = normalizeSet(enabledCategories);
        disabledCategories = normalizeSet(disabledCategories);
        enabledTypes = normalizeSet(enabledTypes);
        disabledTypes = normalizeSet(disabledTypes);
    }

    public boolean allows(String category, String type) {
        if (!enabled) return false;
        String categoryKey = normalize(category);
        String typeKey = normalize(type);
        String scopedTypeKey = categoryKey + ":" + typeKey;
        boolean categoryAllowed = enabledCategories.contains(categoryKey)
                || defaultCategoryEnabled && !disabledCategories.contains(categoryKey);
        boolean typeAllowed = enabledTypes.contains(typeKey)
                || enabledTypes.contains(scopedTypeKey)
                || defaultTypeEnabled && !disabledTypes.contains(typeKey)
                && !disabledTypes.contains(scopedTypeKey);
        return categoryAllowed && typeAllowed;
    }

    public static HistoryRecordingPolicy defaults() {
        return new HistoryRecordingPolicy(true, true, Set.of(), Set.of(), true, Set.of(), Set.of());
    }

    private static Set<String> normalizeSet(Set<String> values) {
        return values == null ? Set.of() : values.stream()
                .map(HistoryRecordingPolicy::normalize)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
