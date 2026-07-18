package panetina.elarion.addons.npcs.client.ui;

import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.npcs.client.NpcClientVisuals;
import panetina.elarion.addons.npcs.client.NpcSkinResolver;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;

public final class ElarionNpcPortraitRenderer {
    private static final int IMAGE_INSET = 3;

    private ElarionNpcPortraitRenderer() {
    }

    public static void render(
            DrawContext context,
            TextRenderer textRenderer,
            NpcDialogueOpenPayload dialogue,
            int x,
            int y,
            int size,
            ElarionUiStyle style
    ) {
        if (renderConfiguredPortrait(context, dialogue, x, y, size, style, textRenderer)) return;
        NpcVisualSyncPayload.Entry visual = NpcClientVisuals.findByNpc(dialogue.npcId()).orElse(null);
        if (visual != null && renderSkinHead(context, visual, x, y, size, style, textRenderer)) return;
        ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "NPC", style);
    }

    private static boolean renderConfiguredPortrait(
            DrawContext context,
            NpcDialogueOpenPayload dialogue,
            int x,
            int y,
            int size,
            ElarionUiStyle style,
            TextRenderer textRenderer
    ) {
        if ("player_head".equalsIgnoreCase(dialogue.portraitType())
                && !dialogue.portraitPlayerName().isBlank()) {
            var skin = NpcSkinResolver.playerSkin(dialogue.portraitPlayerName()).orElse(null);
            if (skin != null) {
                ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
                PlayerSkinDrawer.draw(context, skin, imageX(x), imageY(y), imageSize(size));
                return true;
            }
        }
        if ("texture".equalsIgnoreCase(dialogue.portraitType())) {
            Identifier texture = NpcSkinResolver.texture(dialogue.portrait()).orElse(null);
            if (texture != null) {
                ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
                int textureSize = textureSize(dialogue.portrait());
                drawFullTexture(context, texture, imageX(x), imageY(y), imageSize(size), textureSize);
                return true;
            }
        }
        if ("texture".equalsIgnoreCase(dialogue.portraitFallbackType())) {
            Identifier fallback = NpcSkinResolver.texture(dialogue.portraitFallbackTexture()).orElse(null);
            if (fallback != null) {
                ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
                int textureSize = textureSize(dialogue.portraitFallbackTexture());
                drawFullTexture(context, fallback, imageX(x), imageY(y), imageSize(size), textureSize);
                return true;
            }
        }
        return false;
    }

    private static void drawFullTexture(DrawContext context, Identifier texture, int x, int y, int size, int textureSize) {
        context.drawTexture(texture, x, y, size, size, 0.0F, 0.0F,
                textureSize, textureSize, textureSize, textureSize);
    }

    private static int textureSize(String raw) {
        return raw != null && raw.contains("/32x32/") ? 32 : 64;
    }

    private static boolean renderSkinHead(
            DrawContext context,
            NpcVisualSyncPayload.Entry visual,
            int x,
            int y,
            int size,
            ElarionUiStyle style,
            TextRenderer textRenderer
    ) {
        if ("player_body".equalsIgnoreCase(visual.skinType()) && !visual.skinPlayerName().isBlank()) {
            var skin = NpcSkinResolver.playerSkin(visual.skinPlayerName()).orElse(null);
            if (skin != null) {
                ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
                PlayerSkinDrawer.draw(context, skin, imageX(x), imageY(y), imageSize(size));
                return true;
            }
        }
        Identifier skinTexture = NpcSkinResolver.texture(visual.skinTexture()).orElse(null);
        if (skinTexture == null && "texture".equalsIgnoreCase(visual.skinFallbackType())) {
            skinTexture = NpcSkinResolver.texture(visual.skinFallbackTexture()).orElse(null);
        }
        if (skinTexture == null) return false;

        ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
        int inner = imageSize(size);
        context.drawTexture(skinTexture, imageX(x), imageY(y), inner, inner,
                8.0F, 8.0F, 8, 8, 64, 64);
        context.drawTexture(skinTexture, imageX(x), imageY(y), inner, inner,
                40.0F, 8.0F, 8, 8, 64, 64);
        return true;
    }

    private static int imageX(int frameX) {
        return frameX + IMAGE_INSET;
    }

    private static int imageY(int frameY) {
        return frameY + IMAGE_INSET + 1;
    }

    private static int imageSize(int frameSize) {
        return Math.max(1, frameSize - IMAGE_INSET * 2);
    }
}
