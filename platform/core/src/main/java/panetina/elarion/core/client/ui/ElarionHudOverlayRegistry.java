package panetina.elarion.core.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public final class ElarionHudOverlayRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_core_hud");
    private static final List<BiConsumer<DrawContext, MinecraftClient>> BEFORE_NOTIFICATIONS =
            new CopyOnWriteArrayList<>();
    private static final List<BiConsumer<DrawContext, MinecraftClient>> AFTER_NOTIFICATIONS =
            new CopyOnWriteArrayList<>();

    private ElarionHudOverlayRegistry() {
    }

    public static void registerBeforeNotifications(BiConsumer<DrawContext, MinecraftClient> renderer) {
        if (renderer != null) BEFORE_NOTIFICATIONS.add(renderer);
    }

    public static void registerAfterNotifications(BiConsumer<DrawContext, MinecraftClient> renderer) {
        if (renderer != null) AFTER_NOTIFICATIONS.add(renderer);
    }

    public static void renderBeforeNotifications(DrawContext context, MinecraftClient client) {
        render(BEFORE_NOTIFICATIONS, context, client, "before-notifications");
    }

    public static void renderAfterNotifications(DrawContext context, MinecraftClient client) {
        render(AFTER_NOTIFICATIONS, context, client, "after-notifications");
    }

    private static void render(
            List<BiConsumer<DrawContext, MinecraftClient>> renderers,
            DrawContext context,
            MinecraftClient client,
            String layer
    ) {
        for (BiConsumer<DrawContext, MinecraftClient> renderer : renderers) {
            try {
                renderer.accept(context, client);
            } catch (RuntimeException exception) {
                LOGGER.error("Elarion HUD renderer failed in layer {}", layer, exception);
            }
        }
    }
}
