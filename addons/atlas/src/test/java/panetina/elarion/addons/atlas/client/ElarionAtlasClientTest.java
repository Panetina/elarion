package panetina.elarion.addons.atlas.client;

import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class ElarionAtlasClientTest {
    @Test
    void atlasUsesTheElarionMKeyBinding() {
        assertEquals("key.elarion_atlas.open", ElarionAtlasClient.KEY_TRANSLATION);
        assertEquals("category.elarion_core.ui", ElarionAtlasClient.KEY_CATEGORY);
        assertEquals(GLFW.GLFW_KEY_M, ElarionAtlasClient.DEFAULT_KEY_CODE);
    }

    @Test
    void placeholderRequiresNoWorldDataAndAllFeatureSlotsStayDisabled() {
        AtlasPlaceholderScreen screen = new AtlasPlaceholderScreen();

        assertDoesNotThrow(screen::close);
        assertEquals(6, AtlasPlaceholderScreen.featureSlots().size());
        assertFalse(AtlasPlaceholderScreen.featureSlots().stream()
                .anyMatch(AtlasPlaceholderScreen.FeatureSlot::enabled));
    }
}
