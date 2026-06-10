package panetina.elarion.core.registry;

public record ConditionType(String id, String owner, String description) implements ElarionRegistry.Entry {
}
