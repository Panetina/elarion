package panetina.elarion.addons.quests.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record QuestConsequenceDefinition(
        String id,
        String action,
        Map<String, String> parameters
) {
    public QuestConsequenceDefinition {
        id = id == null ? "" : id;
        action = action == null ? "" : action;
        parameters = parameters == null ? Map.of() : new LinkedHashMap<>(parameters);
    }
}
