package panetina.elarion.addons.npcs.model;

import java.util.List;

public record DialogueTextVariant(
        String id,
        String text,
        String sound,
        String voice,
        List<DialogueCondition> conditions
) {
    public DialogueTextVariant {
        id = id == null ? "" : id;
        text = text == null ? "" : text;
        sound = sound == null ? "" : sound;
        voice = voice == null ? "" : voice;
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
    }
}
