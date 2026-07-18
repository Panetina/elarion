package panetina.elarion.core.model;

import java.util.LinkedHashSet;
import java.util.Set;

public record TitleDefinition(
        String id,
        String description,
        String displayName,
        String prefix,
        String suffix,
        int colorArgb,
        int priority,
        boolean visibleUnderUsername,
        TitleAcquisitionMode acquisitionMode,
        TitleOwnershipMode ownershipMode,
        boolean hiddenFromDiscovery,
        Set<String> abilities,
        Set<TitleActiveEffect> activeEffects
) {
    public static final int DEFAULT_COLOR = ElarionTitlePresentation.SIMPLE_TITLE_COLOR;

    public TitleDefinition {
        description = description == null ? "" : description;
        colorArgb = 0xFF000000 | (colorArgb & 0x00FFFFFF);
        acquisitionMode = acquisitionMode == null ? TitleAcquisitionMode.ADMIN_ONLY : acquisitionMode;
        ownershipMode = ownershipMode == null ? TitleOwnershipMode.UNLIMITED : ownershipMode;
        abilities = abilities == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(abilities));
        activeEffects = activeEffects == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(activeEffects));
    }

    public TitleDefinition(
            String id, String description, String displayName, String prefix, String suffix, int priority,
            boolean visibleUnderUsername, TitleAcquisitionMode acquisitionMode, TitleOwnershipMode ownershipMode,
            boolean hiddenFromDiscovery, Set<String> abilities, Set<TitleActiveEffect> activeEffects
    ) {
        this(id, description, displayName, prefix, suffix, DEFAULT_COLOR, priority, visibleUnderUsername,
                acquisitionMode, ownershipMode, hiddenFromDiscovery, abilities, activeEffects);
    }
}
