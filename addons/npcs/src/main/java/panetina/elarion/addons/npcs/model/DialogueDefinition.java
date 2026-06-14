package panetina.elarion.addons.npcs.model;

import java.util.Map;

public record DialogueDefinition(
        String id,
        String root,
        Map<String, DialogueNode> nodes
) {
    public DialogueDefinition {
        nodes = nodes == null ? Map.of() : Map.copyOf(nodes);
    }
}
