package panetina.elarion.core.placeholder;

import java.util.Set;

public record PlaceholderDescriptor(
        String id,
        String owner,
        String description,
        PlaceholderValueType valueType,
        Set<PlaceholderRenderContext> contexts,
        Set<String> requiredContextKeys,
        PlaceholderVisibility visibility,
        PlaceholderFailureBehavior missingBehavior,
        PlaceholderFailureBehavior unauthorizedBehavior
) {
    public PlaceholderDescriptor {
        id = normalize(id);
        owner = clean(owner, "unknown");
        description = clean(description, id);
        valueType = valueType == null ? PlaceholderValueType.STRING : valueType;
        contexts = contexts == null || contexts.isEmpty()
                ? Set.of(PlaceholderRenderContext.values()) : Set.copyOf(contexts);
        requiredContextKeys = requiredContextKeys == null ? Set.of() : Set.copyOf(requiredContextKeys);
        visibility = visibility == null ? PlaceholderVisibility.PUBLIC : visibility;
        missingBehavior = missingBehavior == null ? PlaceholderFailureBehavior.PRESERVE_TOKEN : missingBehavior;
        unauthorizedBehavior = unauthorizedBehavior == null ? PlaceholderFailureBehavior.EMPTY : unauthorizedBehavior;
    }

    public static PlaceholderDescriptor publicString(String id, String owner, String description,
                                                     Set<PlaceholderRenderContext> contexts) {
        return new PlaceholderDescriptor(id, owner, description, PlaceholderValueType.STRING, contexts, Set.of(),
                PlaceholderVisibility.PUBLIC, PlaceholderFailureBehavior.PRESERVE_TOKEN,
                PlaceholderFailureBehavior.EMPTY);
    }

    static String normalize(String id) {
        String clean = id == null ? "" : id.trim().toLowerCase(java.util.Locale.ROOT);
        if (!clean.matches("[a-z0-9][a-z0-9_.:-]*")) {
            throw new IllegalArgumentException("Invalid placeholder id: " + id);
        }
        return clean;
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
