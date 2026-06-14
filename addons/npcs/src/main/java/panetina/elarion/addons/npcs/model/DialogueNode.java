package panetina.elarion.addons.npcs.model;

import java.util.List;

public record DialogueNode(
        String id,
        String text,
        String sound,
        String voice,
        List<DialogueCondition> conditions,
        List<DialogueOption> options
) {
    public DialogueNode {
        sound = sound == null ? "" : sound;
        voice = voice == null ? "" : voice;
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        options = options == null ? List.of() : List.copyOf(options);
    }
}
