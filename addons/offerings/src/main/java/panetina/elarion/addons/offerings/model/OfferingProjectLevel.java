package panetina.elarion.addons.offerings.model;

import java.util.List;

public record OfferingProjectLevel(
        String id,
        String displayName,
        String description,
        List<OfferingRequirement> requirements,
        List<OfferingMilestone> milestones,
        OfferingPresentation presentation
) {
    public OfferingProjectLevel {
        id = id == null || id.isBlank() ? "level_1" : id.trim().toLowerCase(java.util.Locale.ROOT);
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
        milestones = milestones == null ? List.of() : List.copyOf(milestones);
        presentation = presentation == null ? OfferingPresentation.defaults() : presentation;
    }
}
