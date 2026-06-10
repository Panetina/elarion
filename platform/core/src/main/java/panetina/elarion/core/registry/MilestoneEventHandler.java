package panetina.elarion.core.registry;

@FunctionalInterface
public interface MilestoneEventHandler {
    RegistryExecutionResult execute(MilestoneContext context);
}
