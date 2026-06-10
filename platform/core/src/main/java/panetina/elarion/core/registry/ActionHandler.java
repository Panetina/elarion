package panetina.elarion.core.registry;

@FunctionalInterface
public interface ActionHandler {
    RegistryExecutionResult execute(ActionContext context);
}
