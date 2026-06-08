package panetina.elarion.core.model;

import java.util.Map;

public record RewardAction(String type, Map<String, String> parameters) {
    public RewardAction {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
