package panetina.elarion.addons.offerings.model;

import java.util.List;
import java.util.Optional;

public record OfferingProjectDefinition(
        String id,
        String displayName,
        String description,
        boolean enabled,
        OfferingScope scope,
        boolean repeatable,
        boolean allowMultipleInstances,
        List<OfferingRequirement> requirements,
        List<OfferingMilestone> milestones,
        OfferingPresentation presentation,
        List<OfferingProjectLevel> levels
) {
    public OfferingProjectDefinition(
            String id,
            String displayName,
            String description,
            boolean enabled,
            OfferingScope scope,
            boolean repeatable,
            boolean allowMultipleInstances,
            List<OfferingRequirement> requirements,
            List<OfferingMilestone> milestones,
            OfferingPresentation presentation
    ) {
        this(id, displayName, description, enabled, scope, repeatable, allowMultipleInstances,
                requirements, milestones, presentation, List.of());
    }

    public OfferingProjectDefinition {
        id = id == null ? "" : id;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        scope = scope == null ? OfferingScope.REALM : scope;
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
        milestones = milestones == null ? List.of() : List.copyOf(milestones);
        presentation = presentation == null ? OfferingPresentation.defaults() : presentation;
        levels = levels == null || levels.isEmpty()
                ? List.of(new OfferingProjectLevel("foundation_i", displayName, description,
                requirements, milestones, presentation))
                : List.copyOf(levels);
    }

    public OfferingProjectLevel firstLevel() {
        return levels.getFirst();
    }

    public Optional<OfferingProjectLevel> level(String id) {
        if (id == null || id.isBlank()) return Optional.of(firstLevel());
        return levels.stream().filter(level -> level.id().equals(id)).findFirst();
    }

    public Optional<OfferingProjectLevel> nextLevel(String id) {
        String current = id == null || id.isBlank() ? firstLevel().id() : id;
        for (int index = 0; index < levels.size(); index++) {
            if (levels.get(index).id().equals(current) && index + 1 < levels.size()) {
                return Optional.of(levels.get(index + 1));
            }
        }
        return Optional.empty();
    }
}
