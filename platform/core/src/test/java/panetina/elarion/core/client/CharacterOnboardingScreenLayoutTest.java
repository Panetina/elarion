package panetina.elarion.core.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CharacterOnboardingScreenLayoutTest {
    @Test
    void characterCreationInputsStayInsidePanel() {
        assertEquals(680, CharacterCreationScreen.PANEL_WIDTH);
        assertEquals(398, CharacterCreationScreen.PANEL_HEIGHT);

        assertInside(CharacterCreationScreen.NAME_X, CharacterCreationScreen.NAME_Y,
                CharacterCreationScreen.NAME_WIDTH, CharacterCreationScreen.NAME_HEIGHT,
                CharacterCreationScreen.PANEL_WIDTH, CharacterCreationScreen.PANEL_HEIGHT);
        assertInside(CharacterCreationScreen.BIO_X, CharacterCreationScreen.BIO_Y,
                CharacterCreationScreen.BIO_WIDTH, CharacterCreationScreen.BIO_HEIGHT,
                CharacterCreationScreen.PANEL_WIDTH, CharacterCreationScreen.PANEL_HEIGHT);
        assertTrue(CharacterCreationScreen.BIO_HEIGHT > CharacterCreationScreen.NAME_HEIGHT);
        assertTrue(CharacterCreationScreen.BIO_X > CharacterCreationScreen.NAME_X + CharacterCreationScreen.NAME_WIDTH);
        assertVerticallyCentered(CharacterCreationScreen.PRIMARY_BUTTON_Y, CharacterCreationScreen.BUTTON_HEIGHT,
                CharacterCreationScreen.FOOTER_Y, CharacterCreationScreen.FOOTER_HEIGHT);
    }

    @Test
    void realmPlacementRowsFitThreeVerticalChoicesAndFooter() {
        assertEquals(680, CharacterRealmAssignmentScreen.PANEL_WIDTH);
        assertEquals(360, CharacterRealmAssignmentScreen.PANEL_HEIGHT);

        int lastRowBottom = CharacterRealmAssignmentScreen.OPTION_Y
                + 3 * CharacterRealmAssignmentScreen.OPTION_HEIGHT
                + 2 * CharacterRealmAssignmentScreen.OPTION_GAP;
        assertTrue(lastRowBottom <= CharacterRealmAssignmentScreen.PANEL_HEIGHT - 68);
        assertInside(CharacterRealmAssignmentScreen.OPTION_X, CharacterRealmAssignmentScreen.OPTION_Y,
                CharacterRealmAssignmentScreen.OPTION_WIDTH, CharacterRealmAssignmentScreen.OPTION_HEIGHT,
                CharacterRealmAssignmentScreen.PANEL_WIDTH, CharacterRealmAssignmentScreen.PANEL_HEIGHT);
        assertVerticallyCentered(CharacterRealmAssignmentScreen.CONFIRM_BUTTON_Y,
                CharacterRealmAssignmentScreen.BUTTON_HEIGHT, CharacterRealmAssignmentScreen.FOOTER_Y,
                CharacterRealmAssignmentScreen.FOOTER_HEIGHT);
    }

    private static void assertInside(int x, int y, int width, int height, int panelWidth, int panelHeight) {
        assertTrue(x >= 0);
        assertTrue(y >= 0);
        assertTrue(width > 0);
        assertTrue(height > 0);
        assertTrue(x + width <= panelWidth);
        assertTrue(y + height <= panelHeight);
    }

    private static void assertVerticallyCentered(int y, int height, int parentY, int parentHeight) {
        int topGap = y - parentY;
        int bottomGap = parentY + parentHeight - (y + height);
        assertTrue(topGap >= 0);
        assertTrue(bottomGap >= 0);
        assertTrue(Math.abs(topGap - bottomGap) <= 1);
    }
}
