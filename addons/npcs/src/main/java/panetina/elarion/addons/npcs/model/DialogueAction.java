package panetina.elarion.addons.npcs.model;

import java.util.Map;

public record DialogueAction(String type, Map<String, String> parameters, boolean historyWorthy) {
    public DialogueAction {
        type = type == null ? "" : type;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
