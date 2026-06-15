package panetina.elarion.addons.portals.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayload;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;

import java.time.Duration;
import java.util.List;

public final class PortalStatusHud {
    private static final int X = 8;
    private static final int Y = 160;
    private static final int SLOT_SIZE = 32;
    private static final int SLOT_GAP = 4;

    private PortalStatusHud() {
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        List<PortalRouteStatusSyncPayload.Entry> visible = PortalClientRouteStatus.all().stream()
                .filter(PortalRouteStatusSyncPayload.Entry::unlocked)
                .filter(route -> "scheduled_ticketed".equals(route.mode()))
                .toList();
        if (visible.isEmpty()) return;

        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        double mouseX = client.mouse.getX() * client.getWindow().getScaledWidth()
                / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * client.getWindow().getScaledHeight()
                / client.getWindow().getHeight();
        int index = 0;
        for (PortalRouteStatusSyncPayload.Entry route : visible) {
            int y = Y + index * (SLOT_SIZE + SLOT_GAP);
            ElarionUiRenderer.beveledBox(context, X, y, SLOT_SIZE, SLOT_SIZE, style.cardColor(), style);
            drawItem(context, route.statusIconItem(), X + 8, y + 8);
            if (!route.active()) {
                context.fill(X + 3, y + 3, X + SLOT_SIZE - 3, y + SLOT_SIZE - 3, 0x990F0F0F);
            }
            int accent = route.active() ? 0xFF000000 | route.argb() & 0x00FFFFFF : style.mutedColor();
            context.fill(X + 3, y + SLOT_SIZE - 4, X + SLOT_SIZE - 3, y + SLOT_SIZE - 2, accent);
            if (inside(mouseX, mouseY, X, y, SLOT_SIZE, SLOT_SIZE)) {
                context.drawTooltip(client.textRenderer, tooltip(route), (int) mouseX, (int) mouseY);
            }
            index++;
        }
    }

    private static void drawItem(DrawContext context, String rawId, int x, int y) {
        Identifier id = Identifier.tryParse(rawId);
        if (id == null || !Registries.ITEM.containsId(id)) return;
        context.drawItem(new ItemStack(Registries.ITEM.get(id)), x, y);
    }

    private static List<Text> tooltip(PortalRouteStatusSyncPayload.Entry route) {
        long now = System.currentTimeMillis();
        long target = route.active() ? route.closesAt() : route.opensAt();
        String state = route.active() ? "Open" : "Closed";
        String timer = target <= now
                ? (route.active() ? "Closing now" : "Opening time pending")
                : (route.active() ? "Closes in " : "Opens in ") + formatDuration(target - now);
        return List.of(Text.literal(route.displayName()), Text.literal(state + " - " + timer));
    }

    private static String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(Math.max(0L, millis));
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return Math.max(1L, duration.toMinutes()) + "m";
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
