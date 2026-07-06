package panetina.elarion.addons.quests.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record QuestEndingDefinition(
        String id,
        String displayName,
        String description,
        Map<String, String> shrineDisplayNames
) {
    public QuestEndingDefinition {
        id = id == null ? "" : id;
        displayName = displayName == null || displayName.isBlank() ? id : displayName;
        description = description == null ? "" : description;
        shrineDisplayNames = shrineDisplayNames == null ? Map.of() : new LinkedHashMap<>(shrineDisplayNames);
    }
}
