package panetina.elarion.core.registry;

public record RequirementType(String id, String owner, String description) implements ElarionRegistry.Entry {
}
