package panetina.elarion.addons.npcs.model;

import java.util.List;

public record DialogueNode(
        String id,
        String text,
        String sound,
        String voice,
        List<DialogueCondition> conditions,
        List<DialogueTextVariant> variants,
        List<DialogueOption> options
) {
    public DialogueNode(
            String id,
            String text,
            String sound,
            String voice,
            List<DialogueCondition> conditions,
            List<DialogueOption> options
    ) {
        this(id, text, sound, voice, conditions, List.of(), options);
    }

    public DialogueNode {
        id = id == null ? "" : id;
        text = text == null ? "" : text;
        sound = sound == null ? "" : sound;
        voice = voice == null ? "" : voice;
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        variants = variants == null ? List.of() : List.copyOf(variants);
        options = options == null ? List.of() : List.copyOf(options);
    }
}
