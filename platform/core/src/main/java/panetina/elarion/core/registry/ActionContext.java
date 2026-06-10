package panetina.elarion.core.registry;

import java.util.Map;

public record ActionContext(
        RegistryExecutionContext execution,
        String actionId,
        Map<String, String> parameters
) {
    public ActionContext {
        if (execution == null) throw new IllegalArgumentException("execution cannot be null");
        actionId = actionId == null ? "" : actionId;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
