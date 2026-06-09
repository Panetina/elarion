package panetina.elarion.core.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record RealmDefinition(
        String id,
        String displayName,
        String shortName,
        String prefix,
        String color,
        SpawnPoint spawn,
        VisibilityScope visibilityScope,
        Set<String> flags
) {
    public RealmDefinition {
        flags = flags == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(flags));
    }
}
