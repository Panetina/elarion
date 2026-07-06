package panetina.elarion.addons.quests.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record QuestConditionDefinition(
        String id,
        String type,
        Map<String, String> parameters
) {
    public QuestConditionDefinition {
        id = id == null ? "" : id;
        type = type == null ? "" : type;
        parameters = parameters == null ? Map.of() : new LinkedHashMap<>(parameters);
    }
}
