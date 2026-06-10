package panetina.elarion.core.registry;

import java.util.Map;

public record MilestoneContext(
        RegistryExecutionContext execution,
        String milestoneId,
        Map<String, String> parameters
) {
    public MilestoneContext {
        if (execution == null) throw new IllegalArgumentException("execution cannot be null");
        milestoneId = milestoneId == null ? "" : milestoneId;
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
