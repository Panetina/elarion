package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionUiIconsTest {
    @Test
    void reputationFactionIconsResolveToCoreTextures() {
        assertTrue(ElarionUiIcons.has("reputation"));
        assertTrue(ElarionUiIcons.has("faction"));
        assertTrue(ElarionUiIcons.has("worldheart"));
        assertTrue(ElarionUiIcons.has("underworld"));
        assertTrue(ElarionUiIcons.has("underworld_faction"));
    }
}
