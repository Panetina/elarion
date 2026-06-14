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
                PlayerSkinDrawer.draw(context, skin, x + 4, y + 4, size - 8);
                return true;
            }
        }
        if ("texture".equalsIgnoreCase(dialogue.portraitType())) {
            Identifier texture = NpcSkinResolver.texture(dialogue.portrait()).orElse(null);
            if (texture != null) {
                ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
                context.drawTexture(texture, x + 4, y + 4, 0, 0,
                        size - 8, size - 8, 64, 64);
                return true;
            }
        }
        if ("texture".equalsIgnoreCase(dialogue.portraitFallbackType())) {
            Identifier fallback = NpcSkinResolver.texture(dialogue.portraitFallbackTexture()).orElse(null);
            if (fallback != null) {
                ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
                context.drawTexture(fallback, x + 4, y + 4, 0, 0,
                        size - 8, size - 8, 64, 64);
                return true;
            }
        }
        return false;
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
                PlayerSkinDrawer.draw(context, skin, x + 4, y + 4, size - 8);
                return true;
            }
        }
        Identifier skinTexture = NpcSkinResolver.texture(visual.skinTexture()).orElse(null);
        if (skinTexture == null && "texture".equalsIgnoreCase(visual.skinFallbackType())) {
            skinTexture = NpcSkinResolver.texture(visual.skinFallbackTexture()).orElse(null);
        }
        if (skinTexture == null) return false;

        ElarionUiRenderer.portraitFrame(context, textRenderer, x, y, size, "", style);
        int inner = size - 8;
        context.drawTexture(skinTexture, x + 4, y + 4, inner, inner,
                8.0F, 8.0F, 8, 8, 64, 64);
        context.drawTexture(skinTexture, x + 4, y + 4, inner, inner,
                40.0F, 8.0F, 8, 8, 64, 64);
        return true;
    }
}
