package panetina.elarion.core.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import panetina.elarion.core.client.ui.ElarionScaledLayout;

public final class ElarionMenuEntityPreviewRenderer {
    private ElarionMenuEntityPreviewRenderer() {
    }

    public static void render(
            DrawContext context,
            ElarionScaledLayout layout,
            LivingEntity entity,
            int x,
            int y,
            int width,
            int height,
            int size,
            float yaw,
            float pitch,
            int verticalOffset
    ) {
        render(context, layout, entity, x, y, width, height, size, yaw, pitch, 0, verticalOffset);
    }

    public static void render(
            DrawContext context,
            ElarionScaledLayout layout,
            LivingEntity entity,
            int x,
            int y,
            int width,
            int height,
            int size,
            float yaw,
            float pitch,
            int horizontalOffset,
            int verticalOffset
    ) {
        if (entity == null || layout == null || width <= 0 || height <= 0) return;
        int sx1 = layout.screenX() + Math.round(x * layout.scale());
        int sy1 = layout.screenY() + Math.round(y * layout.scale());
        int sx2 = layout.screenX() + Math.round((x + width) * layout.scale());
        int sy2 = layout.screenY() + Math.round((y + height) * layout.scale());
        context.draw();
        context.enableScissor(sx1, sy1, sx2, sy2);
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            try {
                matrices.translate(x + width / 2.0F + horizontalOffset, y + height - 6.0F + verticalOffset, 100.0F);
                matrices.scale(size, size, -size);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
                dispatcher.setRenderShadows(false);
                dispatcher.render(
                        entity,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0F,
                        client.getRenderTickCounter().getTickDelta(true),
                        matrices,
                        context.getVertexConsumers(),
                        LightmapTextureManager.MAX_LIGHT_COORDINATE);
                context.draw();
            } finally {
                dispatcher.setRenderShadows(true);
                matrices.pop();
            }
        } finally {
            context.disableScissor();
        }
    }
}
