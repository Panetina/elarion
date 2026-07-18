package panetina.elarion.addons.npcs.model;

import java.util.List;

public record DialogueOption(
        String id,
        String buttonText,
        String playerText,
        String sound,
        String voice,
        String presentationRole,
        String next,
        List<DialogueCondition> conditions,
        List<DialogueAction> actions,
        DialoguePrompt prompt,
        boolean close,
        boolean oneTime
) {
    public DialogueOption(
            String id, String buttonText, String playerText, String sound, String voice,
            String presentationRole, String next, List<DialogueCondition> conditions,
            List<DialogueAction> actions, DialoguePrompt prompt, boolean close
    ) {
        this(id, buttonText, playerText, sound, voice, presentationRole, next,
                conditions, actions, prompt, close, false);
    }

    public DialogueOption(
            String id, String buttonText, String playerText, String sound, String voice, String next,
            List<DialogueCondition> conditions, List<DialogueAction> actions, DialoguePrompt prompt, boolean close
    ) {
        this(id, buttonText, playerText, sound, voice, "", next, conditions, actions, prompt, close, false);
    }

    public DialogueOption(
            String id, String buttonText, String playerText, String sound, String voice, String next,
            List<DialogueCondition> conditions, List<DialogueAction> actions, DialoguePrompt prompt,
            boolean close, boolean oneTime
    ) {
        this(id, buttonText, playerText, sound, voice, "", next, conditions, actions, prompt, close, oneTime);
    }

    public DialogueOption {
        buttonText = buttonText == null || buttonText.isBlank() ? id : buttonText;
        playerText = playerText == null || playerText.isBlank() ? buttonText : playerText;
        sound = sound == null ? "" : sound;
        voice = voice == null ? "" : voice;
        presentationRole = presentationRole == null ? "" : presentationRole.trim();
        conditions = conditions == null ? List.of() : List.copyOf(conditions);
        actions = actions == null ? List.of() : List.copyOf(actions);
        prompt = prompt == null ? DialoguePrompt.NONE : prompt;
    }
}
