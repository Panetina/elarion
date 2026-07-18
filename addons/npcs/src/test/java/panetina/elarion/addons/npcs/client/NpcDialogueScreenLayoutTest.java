package panetina.elarion.addons.npcs.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcDialogueScreenLayoutTest {
    @Test
    void capsTheCompactDialogueAtThreeVisibleChoices() {
        assertEquals(3, NpcDialogueScreen.visibleOptionRows(8, 6, 120, 24));
    }

    @Test
    void retainsFewerServerAuthoredChoicesWithoutBlankRows() {
        assertEquals(2, NpcDialogueScreen.visibleOptionRows(2, 6, 120, 24));
    }

    @Test
    void reducesVisibleChoicesWhenScaledControlsNeedMoreHeight() {
        assertEquals(2, NpcDialogueScreen.visibleOptionRows(8, 6, 55, 24));
        assertEquals(1, NpcDialogueScreen.visibleOptionRows(8, 6, 23, 24));
    }

    @Test
    void emptyDialogueStillReservesOneStableChoiceRow() {
        assertEquals(1, NpcDialogueScreen.visibleOptionRows(0, 6, 120, 24));
    }
}
