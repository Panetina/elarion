package panetina.elarion.core.model;

import java.util.LinkedHashSet;
import java.util.Locale;
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
        LinkedHashSet<String> normalizedFlags = new LinkedHashSet<>();
        if (flags != null) {
            flags.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(value -> value.trim().toLowerCase(Locale.ROOT))
                    .forEach(normalizedFlags::add);
        }
        flags = Set.copyOf(normalizedFlags);
    }

    public boolean hasFlag(String flag) {
        return flag != null && flags.contains(flag.trim().toLowerCase(Locale.ROOT));
    }
}
