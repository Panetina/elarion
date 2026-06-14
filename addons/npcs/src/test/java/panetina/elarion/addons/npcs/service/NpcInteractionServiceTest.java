package panetina.elarion.addons.npcs.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcInteractionServiceTest {
    @Test
    void actionFeedbackReplacesNodeIntroductionForThatTurn() {
        assertEquals(
                "Wallet: 12 currency. Physical: 4 currency.",
                NpcInteractionService.responseText(
                        "Welcome to the Treasury.",
                        "Wallet: 12 currency. Physical: 4 currency."));
    }

    @Test
    void nodeTextRemainsWhenActionHasNoFeedback() {
        assertEquals(
                "Welcome to the Treasury.",
                NpcInteractionService.responseText("Welcome to the Treasury.", ""));
    }
}
