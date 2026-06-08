package panetina.elarion.core.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record TitleDefinition(
        String id,
        String displayName,
        String prefix,
        String suffix,
        int priority,
        boolean visibleUnderUsername,
        Set<String> abilities
) {
    public TitleDefinition {
        abilities = abilities == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(abilities));
    }
}
