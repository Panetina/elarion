package panetina.elarion.core.client;

import net.minecraft.client.gui.DrawContext;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.model.ElarionCollectionEntry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ElarionCollectionPreviewRegistry {
    private static final List<PreviewRenderer> RENDERERS = new CopyOnWriteArrayList<>();

    private ElarionCollectionPreviewRegistry() {
    }

    public static void register(PreviewRenderer renderer) {
        if (renderer != null) RENDERERS.add(renderer);
    }

    static boolean render(
            DrawContext context,
            ElarionScaledLayout layout,
            String tabId,
            ElarionCollectionEntry entry,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY,
            float delta,
            ElarionUiStyle style
    ) {
        for (PreviewRenderer renderer : RENDERERS) {
            if (renderer.render(context, layout, tabId, entry, x, y, width, height, mouseX, mouseY, delta, style)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface PreviewRenderer {
        boolean render(
                DrawContext context,
                ElarionScaledLayout layout,
                String tabId,
                ElarionCollectionEntry entry,
                int x,
                int y,
                int width,
                int height,
                int mouseX,
                int mouseY,
                float delta,
                ElarionUiStyle style
        );
    }
}
