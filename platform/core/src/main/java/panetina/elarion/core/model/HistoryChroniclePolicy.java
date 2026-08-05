package panetina.elarion.core.model;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Configured eligibility shared by Chronicle archives, library reads, and website projections. */
public record HistoryChroniclePolicy(
        Set<String> categories,
        boolean defaultTypeEnabled,
        Set<String> enabledTypes,
        Set<String> disabledTypes
) {
    public HistoryChroniclePolicy {
        categories = normalizeSet(categories);
        enabledTypes = normalizeSet(enabledTypes);
        disabledTypes = normalizeSet(disabledTypes);
    }

    public boolean allows(String category, String type) {
        String normalizedCategory = normalize(category);
        String normalizedType = normalize(type);
        if (!categories.contains(normalizedCategory)) return false;
        String scopedType = normalizedCategory + ":" + normalizedType;
        boolean typeAllowed = defaultTypeEnabled || enabledTypes.contains(normalizedType)
                || enabledTypes.contains(scopedType);
        boolean typeBlocked = disabledTypes.contains(normalizedType) || disabledTypes.contains(scopedType);
        return typeAllowed && !typeBlocked;
    }

    /** Returns false only when the persisted month metadata proves no eligible event can exist. */
    public boolean mayContain(Map<String, Integer> categoryCounts, Map<String, Integer> typeCounts) {
        if (categoryCounts == null || categoryCounts.isEmpty() || typeCounts == null || typeCounts.isEmpty()) return true;
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) continue;
            if (!isScopedType(entry.getKey())) return true;
            if (allowsScopedType(entry.getKey())) return true;
        }
        return false;
    }

    private boolean allowsScopedType(String value) {
        int separator = value.indexOf(':');
        if (separator <= 0 || separator == value.length() - 1) return false;
        return allows(value.substring(0, separator), value.substring(separator + 1));
    }

    private static boolean isScopedType(String value) {
        int separator = value == null ? -1 : value.indexOf(':');
        return separator > 0 && separator < value.length() - 1;
    }

    private static Set<String> normalizeSet(Set<String> values) {
        return values == null ? Set.of() : values.stream()
                .map(HistoryChroniclePolicy::normalize)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
