package panetina.elarion.addons.npcs.model;

import java.util.List;
import java.util.UUID;

public record DialogueView(
        UUID npcId,
        String dialogueId,
        String nodeId,
        String npcName,
        String portrait,
        String portraitType,
        String portraitPlayerName,
        String portraitFallbackType,
        String portraitFallbackTexture,
        String playerText,
        String text,
        String npcSound,
        String npcVoice,
        String playerSound,
        String playerVoice,
        NpcPresentationKind presentation,
        String feedback,
        boolean feedbackError,
        Long currencyBalance,
        String relationLabel,
        Integer relationValue,
        List<CardView> cards,
        List<OptionView> options
) {
    public DialogueView {
        cards = cards == null ? List.of() : List.copyOf(cards);
        options = options == null ? List.of() : List.copyOf(options);
    }

    public record OptionView(
            String id,
            String buttonText,
            String playerText,
            String presentationRole,
            String promptType,
            String promptQuestion,
            int promptMaxDigits
    ) {
    }

    public record CardView(String id, String label, String icon, int count, long currencyAmount, boolean disabled) {
    }
}
