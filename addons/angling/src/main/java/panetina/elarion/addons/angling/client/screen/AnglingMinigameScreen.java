package panetina.elarion.addons.angling.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSnapshot;
import panetina.elarion.addons.angling.network.AnglingMinigameInputAction;
import panetina.elarion.addons.angling.network.AnglingMinigameInputPayload;
import panetina.elarion.addons.angling.network.AnglingMinigameStartPayload;

import java.util.Objects;

/** Upstream-layout presentation driven exclusively by bounded server snapshots. */
public final class AnglingMinigameScreen extends Screen {
    private static final int TEXTURE_SIZE = 256;

    private final AnglingMinigameStartPayload start;
    private AnglingServerMinigameSnapshot state;
    private int nextSequence;
    private boolean pressed;
    private boolean closedByServer;

    public AnglingMinigameScreen(AnglingMinigameStartPayload start) {
        super(Text.empty());
        this.start = Objects.requireNonNull(start, "start");
    }

    public void accept(AnglingServerMinigameSnapshot next) {
        Objects.requireNonNull(next, "next");
        if (!start.sessionId().equals(next.sessionId())) return;
        if (state != null && next.revision() <= state.revision()) return;
        state = next;
        if (next.status().terminal() && client != null) {
            closedByServer = true;
            client.setScreen(null);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        AnglingServerMinigameSnapshot snapshot = state;
        if (snapshot == null) return;
        int centerX = width / 2;
        int centerY = height / 2;
        Identifier texture = start.surfaceTextureId();

        if (snapshot.treasureProgress() > 0 && !start.treasureItemId().equals(Identifier.ofVanilla("air"))) {
            renderTreasure(context, centerX, centerY, texture, snapshot.treasureProgress());
        }
        context.drawTexture(texture, centerX - 150, centerY - 58,
                96, 112, 0, 112, 96, 112, TEXTURE_SIZE, TEXTURE_SIZE);
        if (snapshot.darkness() > 0) RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        context.drawTexture(texture, centerX - 32, centerY - 32,
                64, 64, 96, 112, 64, 64, TEXTURE_SIZE, TEXTURE_SIZE);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        context.drawTexture(texture, centerX - 24, centerY + 40,
                48, 16, 96, pressed ? 16 : 0, 48, 16, TEXTURE_SIZE, TEXTURE_SIZE);
        Text key = Text.literal("Space");
        context.drawText(textRenderer, key, centerX - textRenderer.getWidth(key) / 2,
                centerY + 42 + (pressed ? 2 : 0), 0xff546170, false);

        for (AnglingServerMinigameSnapshot.Sweetspot spot : snapshot.sweetspots()) {
            if (spot.layer() != snapshot.pointerLayer() || spot.alpha() <= 0) continue;
            context.getMatrices().push();
            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spot.position()));
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, Math.clamp(spot.alpha(), 0, 1));
            context.drawTexture(spot.texturePath(), -48, -48, 96, 96,
                    0, 0, 96, 96, 96, 96);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
            context.getMatrices().pop();
        }

        if (snapshot.darkness() > 0) RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        context.drawTexture(texture, centerX - 32, centerY - 32,
                64, 64, 160, 112, 64, 64, TEXTURE_SIZE, TEXTURE_SIZE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        renderPointer(context, centerX, centerY, texture, snapshot.pointerPosition());
        context.drawTexture(texture, centerX - 16, centerY - 16,
                32, 32, 224, 128, 32, 32, TEXTURE_SIZE, TEXTURE_SIZE);

        boolean flip = start.pullDownPresentation();
        context.drawTexture(texture, centerX - 102, centerY - 81 + (flip ? 130 : 0),
                64, 48, 192, 0, 64, 48, TEXTURE_SIZE, TEXTURE_SIZE);
        float progress = Math.clamp(snapshot.smoothedProgress() / start.hitPoints(), 0, 1);
        int yOffset = (int) (progress * 77);
        if (flip) {
            context.drawTexture(texture, centerX - 108, centerY - 46 + yOffset,
                    16, Math.max(0, 97 - yOffset), 176, yOffset,
                    16, Math.max(0, 97 - yOffset), TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            context.drawTexture(texture, centerX - 108, centerY - 80,
                    16, Math.max(0, 112 - yOffset), 176, yOffset - 10,
                    16, Math.max(0, 112 - yOffset), TEXTURE_SIZE, TEXTURE_SIZE);
        }
        ItemStack caught = item(start.displayItemId());
        context.getMatrices().push();
        context.getMatrices().translate(0, flip ? yOffset - 77 : -yOffset, 0);
        context.drawItem(caught, centerX - 108, centerY + 27);
        context.getMatrices().pop();
    }

    private void renderTreasure(DrawContext context, int centerX, int centerY, Identifier texture, int progress) {
        int barSize = Math.min(64, 64 * progress / 100);
        context.drawTexture(texture, centerX - 158, centerY + 22 - barSize,
                5, barSize, 77, 6, 5, barSize, TEXTURE_SIZE, TEXTURE_SIZE);
        context.drawTexture(texture, centerX - 171, centerY - 48,
                32, 96, 32, 0, 32, 96, TEXTURE_SIZE, TEXTURE_SIZE);
        context.drawItem(item(start.treasureItemId()), centerX - 163, centerY - barSize + 16);
        if (progress >= 100) {
            context.drawTexture(texture, centerX - 171, centerY - 48,
                    32, 96, 0, 0, 32, 96, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    private static void renderPointer(
            DrawContext context,
            int centerX,
            int centerY,
            Identifier texture,
            float position
    ) {
        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(position));
        context.getMatrices().translate(0, -16, 0);
        context.drawTexture(texture, -16, -32, 32, 64,
                144, 0, 32, 64, TEXTURE_SIZE, TEXTURE_SIZE);
        context.getMatrices().pop();
    }

    private static ItemStack item(Identifier id) {
        return Registries.ITEM.containsId(id) ? new ItemStack(Registries.ITEM.get(id)) : ItemStack.EMPTY;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            if (!pressed) send(AnglingMinigameInputAction.PRESS);
            pressed = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            send(AnglingMinigameInputAction.LAYER_PREVIOUS);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            send(AnglingMinigameInputAction.LAYER_NEXT);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE && pressed) {
            pressed = false;
            send(AnglingMinigameInputAction.RELEASE);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!pressed) send(AnglingMinigameInputAction.PRESS);
        pressed = true;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressed) send(AnglingMinigameInputAction.RELEASE);
        pressed = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) send(AnglingMinigameInputAction.LAYER_NEXT);
        else if (verticalAmount < 0) send(AnglingMinigameInputAction.LAYER_PREVIOUS);
        return verticalAmount != 0;
    }

    private void send(AnglingMinigameInputAction action) {
        ClientPlayNetworking.send(new AnglingMinigameInputPayload(
                start.sessionId(), start.bobberEntityId(), nextSequence++, action));
    }

    @Override
    public void close() {
        if (!closedByServer) {
            if (pressed) {
                pressed = false;
                send(AnglingMinigameInputAction.RELEASE);
            }
            send(AnglingMinigameInputAction.ABANDON);
        }
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
