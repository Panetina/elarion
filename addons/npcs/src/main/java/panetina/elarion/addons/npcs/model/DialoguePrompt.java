package panetina.elarion.addons.npcs.model;

public record DialoguePrompt(
        String type,
        String question,
        String action,
        int maxDigits,
        long minimumAmount,
        long maximumAmount
) {
    public static final DialoguePrompt NONE = new DialoguePrompt("", "", "", 0, 0L, 0L);

    public DialoguePrompt {
        type = type == null ? "" : type;
        question = question == null ? "" : question;
        action = action == null ? "" : action;
    }

    public boolean present() {
        return !type.isBlank();
    }
}
