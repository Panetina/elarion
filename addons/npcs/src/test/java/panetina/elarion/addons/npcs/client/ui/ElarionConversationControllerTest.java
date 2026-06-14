package panetina.elarion.addons.npcs.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConversationControllerTest {
    @Test
    void responseTypesPlayerBeforeNpcAndThenEnablesInput() {
        ElarionConversationController controller =
                new ElarionConversationController("My question", "The answer", true, 1);

        assertEquals(ElarionConversationController.Phase.PLAYER_TYPING, controller.phase());
        assertFalse(controller.canSubmit());
        assertTrue(controller.completeCurrentPhase());
        assertEquals(ElarionConversationController.Phase.NPC_TYPING, controller.phase());
        assertFalse(controller.canSubmit());
        assertTrue(controller.completeCurrentPhase());
        assertEquals(ElarionConversationController.Phase.AWAITING_INPUT, controller.phase());
        assertTrue(controller.canSubmit());
    }

    @Test
    void initialDialogueStartsWithNpc() {
        ElarionConversationController controller =
                new ElarionConversationController("", "Welcome", true, 1);

        assertEquals(ElarionConversationController.Phase.NPC_TYPING, controller.phase());
        controller.completeCurrentPhase();
        assertEquals(ElarionConversationController.Phase.AWAITING_INPUT, controller.phase());
    }

    @Test
    void submissionCanOnlyBeClaimedOnce() {
        ElarionConversationController controller =
                new ElarionConversationController("", "", false, 45);

        assertTrue(controller.markSubmitted());
        assertFalse(controller.markSubmitted());
    }
}
