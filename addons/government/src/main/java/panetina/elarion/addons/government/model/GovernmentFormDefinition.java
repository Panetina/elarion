package panetina.elarion.addons.government.model;

import java.util.List;
import java.util.Map;

public record GovernmentFormDefinition(
        String id,
        String displayName,
        String description,
        boolean enabled,
        String officialNameTemplate,
        List<String> authorityOffices,
        boolean confederationDelegatesRepresentGroups,
        List<GovernmentOfficeDefinition> offices,
        Map<String, List<String>> actions,
        Map<String, String> transitions
) {
    public GovernmentFormDefinition {
        id = id == null ? "" : id;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        officialNameTemplate = officialNameTemplate == null || officialNameTemplate.isBlank()
                ? displayName + " of %realm%" : officialNameTemplate;
        authorityOffices = authorityOffices == null ? List.of() : List.copyOf(authorityOffices);
        offices = offices == null ? List.of() : List.copyOf(offices);
        actions = actions == null ? Map.of() : Map.copyOf(actions);
        transitions = transitions == null ? Map.of() : Map.copyOf(transitions);
    }
}
