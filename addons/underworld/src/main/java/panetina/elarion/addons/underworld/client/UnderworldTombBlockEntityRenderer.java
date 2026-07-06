package panetina.elarion.addons.underworld.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import panetina.elarion.addons.underworld.block.TombstoneVariant;
import panetina.elarion.addons.underworld.block.UnderworldTombBlock;
import panetina.elarion.addons.underworld.block.UnderworldTombBlockEntity;

public final class UnderworldTombBlockEntityRenderer implements BlockEntityRenderer<UnderworldTombBlockEntity> {
    private static final double MAX_DISTANCE_SQUARED = 32.0D * 32.0D;
    private final TextRenderer textRenderer;

    public UnderworldTombBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.textRenderer = context.getTextRenderer();
    }

    @Override
    public void render(
            UnderworldTombBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || entity.itemCount() <= 0) return;
        if (client.player.squaredDistanceTo(
                entity.getPos().getX() + 0.5D,
                entity.getPos().getY() + 0.5D,
                entity.getPos().getZ() + 0.5D) > MAX_DISTANCE_SQUARED) {
            return;
        }
        String timer = timerLabel(entity);
        if (timer.isBlank()) return;
        String owner = ownerLabel(entity);

        TombstoneVariant variant = TombstoneVariant.byBlockStateId(entity.getCachedState().get(UnderworldTombBlock.VARIANT));
        double y = variant.maxY() / 16.0D + 0.38D;
        matrices.push();
        matrices.translate(0.5D, y, 0.5D);
        matrices.multiply(client.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Text ownerText = Text.literal(owner);
        float ownerX = -textRenderer.getWidth(ownerText) / 2.0F;
        textRenderer.draw(ownerText, ownerX, -10.0F, 0xFFFFFFFF, true, matrix, vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH, 0x66000000, light);
        Text timerText = Text.literal(timer);
        float timerX = -textRenderer.getWidth(timerText) / 2.0F;
        textRenderer.draw(timerText, timerX, 1.0F, color(entity), true, matrix, vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH, 0x66000000, light);
        matrices.pop();
    }

    private static String ownerLabel(UnderworldTombBlockEntity entity) {
        String owner = entity.ownerName();
        return owner == null || owner.isBlank() ? "Unknown's Grave" : owner + "'s Grave";
    }

    private static String timerLabel(UnderworldTombBlockEntity entity) {
        long now = System.currentTimeMillis();
        return switch (entity.accessState()) {
            case "protected" -> "Protected " + formatTime(Math.max(0L, entity.protectedUntil() - now));
            case "lootable" -> "Lootable - Decays in " + formatTime(Math.max(0L, entity.decaysAt() - now));
            default -> "";
        };
    }

    private static int color(UnderworldTombBlockEntity entity) {
        return "lootable".equals(entity.accessState()) ? 0xFFFFD166 : 0xFF9ED0FF;
    }

    private static String formatTime(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;
        if (hours > 0L) {
            return hours + ":" + two(minutes) + ":" + two(remainingSeconds);
        }
        return minutes + ":" + two(remainingSeconds);
    }

    private static String two(long value) {
        return value < 10L ? "0" + value : Long.toString(value);
    }
}
