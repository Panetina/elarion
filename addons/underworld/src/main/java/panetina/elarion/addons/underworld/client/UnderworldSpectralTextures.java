package panetina.elarion.addons.underworld.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

/** Texture-independent flat colors for dead and banished player models. */
public final class UnderworldSpectralTextures {
    private static final Identifier WHITE = Identifier.ofVanilla("textures/misc/white.png");
    private static final Identifier RED = Identifier.of("elarion_underworld", "dynamic/banished_red");
    private static boolean initialized;

    private UnderworldSpectralTextures() {
    }

    public static Identifier modelTexture(boolean banished) {
        if (!banished) return WHITE;
        initializeRedTexture();
        return RED;
    }

    private static void initializeRedTexture() {
        if (initialized) return;
        NativeImage image = new NativeImage(2, 2, true);
        int opaqueRedAbgr = 0xFF1200D0;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) image.setColor(x, y, opaqueRedAbgr);
        }
        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
        texture.setFilter(false, false);
        texture.upload();
        MinecraftClient.getInstance().getTextureManager().registerTexture(RED, texture);
        initialized = true;
    }
}
