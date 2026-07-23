package panetina.elarion.addons.atlas.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ElarionAtlasClient implements ClientModInitializer {
    static final String KEY_TRANSLATION = "key.elarion_atlas.open";
    static final String KEY_CATEGORY = "category.elarion_core.ui";
    static final int DEFAULT_KEY_CODE = GLFW.GLFW_KEY_M;

    private static KeyBinding atlasKey;

    @Override
    public void onInitializeClient() {
        atlasKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_TRANSLATION,
                InputUtil.Type.KEYSYM,
                DEFAULT_KEY_CODE,
                KEY_CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || atlasKey == null) return;
            while (atlasKey.wasPressed()) {
                if (client.currentScreen instanceof AtlasPlaceholderScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new AtlasPlaceholderScreen());
                }
            }
        });
    }
}
