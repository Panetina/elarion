package panetina.elarion.addons.npcs.model;

import java.util.Map;

public record DialogueCondition(String type, Map<String, String> parameters) {
    public DialogueCondition {
        type = type == null ? "" : type;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
