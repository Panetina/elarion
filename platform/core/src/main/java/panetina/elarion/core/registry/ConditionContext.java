package panetina.elarion.core.registry;

import java.util.Map;

public record ConditionContext(
        RegistryExecutionContext execution,
        String conditionId,
        Map<String, String> parameters
) {
    public ConditionContext {
        if (execution == null) throw new IllegalArgumentException("execution cannot be null");
        conditionId = conditionId == null ? "" : conditionId;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
