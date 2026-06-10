package panetina.elarion.core.registry;

public record MilestoneEventType(String id, String owner, String description) implements ElarionRegistry.Entry {
}
