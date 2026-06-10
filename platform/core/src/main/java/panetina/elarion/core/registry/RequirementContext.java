package panetina.elarion.core.registry;

import java.util.Map;

public record RequirementContext(
        RegistryExecutionContext execution,
        String requirementId,
        Map<String, String> parameters
) {
    public RequirementContext {
        if (execution == null) throw new IllegalArgumentException("execution cannot be null");
        requirementId = requirementId == null ? "" : requirementId;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
