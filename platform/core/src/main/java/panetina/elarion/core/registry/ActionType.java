package panetina.elarion.core.registry;

public record ActionType(String id, String owner, String description) implements ElarionRegistry.Entry {
}
