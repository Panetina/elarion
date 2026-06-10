package panetina.elarion.core.registry;

@FunctionalInterface
public interface RequirementHandler {
    RegistryExecutionResult evaluate(RequirementContext context);
}
