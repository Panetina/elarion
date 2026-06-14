package panetina.elarion.addons.npcs.client.ui;

import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;

public final class ElarionConversationController {
    public enum Phase {
        PLAYER_TYPING,
        NPC_TYPING,
        AWAITING_INPUT
    }

    private String playerText;
    private String npcText;
    private boolean typingEnabled;
    private int typingCharactersPerSecond;
    private ElarionTypingText typing;
    private Phase phase;
    private boolean submitted;

    public ElarionConversationController(NpcDialogueOpenPayload dialogue) {
        update(dialogue);
    }

    public ElarionConversationController(
            String playerText,
            String npcText,
            boolean typingEnabled,
            int typingCharactersPerSecond
    ) {
        update(playerText, npcText, typingEnabled, typingCharactersPerSecond);
    }

    public void update(NpcDialogueOpenPayload dialogue) {
        update(dialogue.playerText(), dialogue.text(),
                dialogue.typingEnabled(), dialogue.typingCharactersPerSecond());
    }

    private void update(
            String playerText,
            String npcText,
            boolean typingEnabled,
            int typingCharactersPerSecond
    ) {
        this.playerText = playerText == null ? "" : playerText;
        this.npcText = npcText == null ? "" : npcText;
        this.typingEnabled = typingEnabled;
        this.typingCharactersPerSecond = typingCharactersPerSecond;
        this.submitted = false;
        this.phase = this.playerText.isBlank() ? Phase.NPC_TYPING : Phase.PLAYER_TYPING;
        this.typing = new ElarionTypingText(typingEnabled, typingCharactersPerSecond);
        advanceCompletedPhase();
    }

    public Phase phase() {
        advanceCompletedPhase();
        return phase;
    }

    public String playerText() {
        advanceCompletedPhase();
        if (playerText.isBlank()) return "Choose a response.";
        if (phase == Phase.PLAYER_TYPING) return typing.visible(playerText);
        return playerText;
    }

    public String npcText() {
        advanceCompletedPhase();
        if (phase == Phase.PLAYER_TYPING) return "";
        if (phase == Phase.NPC_TYPING) return typing.visible(npcText);
        return npcText;
    }

    public boolean completeCurrentPhase() {
        if (phase == Phase.AWAITING_INPUT) return false;
        if (!typing.completeIfTyping(currentText())) {
            advanceCompletedPhase();
            return false;
        }
        advanceFromCurrentPhase();
        return true;
    }

    public boolean canSubmit() {
        return phase() == Phase.AWAITING_INPUT && !submitted;
    }

    public boolean markSubmitted() {
        if (!canSubmit()) return false;
        submitted = true;
        return true;
    }

    public int typedIntervalIndex(int intervalCharacters) {
        return typing.typedIntervalIndex(intervalCharacters);
    }

    private String currentText() {
        return phase == Phase.PLAYER_TYPING ? playerText : npcText;
    }

    private void advanceCompletedPhase() {
        if (phase != Phase.AWAITING_INPUT && typing.complete(currentText())) {
            advanceFromCurrentPhase();
        }
    }

    private void advanceFromCurrentPhase() {
        if (phase == Phase.PLAYER_TYPING) {
            phase = Phase.NPC_TYPING;
            typing = new ElarionTypingText(typingEnabled, typingCharactersPerSecond);
        } else if (phase == Phase.NPC_TYPING) {
            phase = Phase.AWAITING_INPUT;
        }
    }
}
