package panetina.elarion.core.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record TitleDefinition(
        String id,
        String description,
        String displayName,
        String prefix,
        String suffix,
        int priority,
        boolean visibleUnderUsername,
        TitleAcquisitionMode acquisitionMode,
        TitleOwnershipMode ownershipMode,
        boolean hiddenFromDiscovery,
        Set<String> abilities,
        Set<TitleActiveEffect> activeEffects
) {
    public TitleDefinition {
        description = description == null ? "" : description;
        acquisitionMode = acquisitionMode == null ? TitleAcquisitionMode.ADMIN_ONLY : acquisitionMode;
        ownershipMode = ownershipMode == null ? TitleOwnershipMode.UNLIMITED : ownershipMode;
        abilities = abilities == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(abilities));
        activeEffects = activeEffects == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(activeEffects));
    }
}
