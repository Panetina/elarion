package panetina.elarion.core.registry;

@FunctionalInterface
public interface ConditionHandler {
    RegistryExecutionResult evaluate(ConditionContext context);
}
