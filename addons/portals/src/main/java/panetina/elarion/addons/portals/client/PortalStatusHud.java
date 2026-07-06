package panetina.elarion.addons.portals.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayload;
import panetina.elarion.core.client.ElarionNotificationHud;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;

import java.time.Duration;
import java.util.List;

public final class PortalStatusHud {
    private static final Identifier NETHER_GATE_TEXTURE =
            Identifier.of("elarion_portals", "textures/gui/portal_status/nether_gate.png");
    private static final Identifier END_GATE_TEXTURE =
            Identifier.of("elarion_portals", "textures/gui/portal_status/end_gate.png");
    private static final int X = 3;
    private static final int SLOT_SIZE = 24;
    private static final int TEXTURE_SOURCE_SIZE = 32;
    private static final int SLOT_GAP = 3;
    private static final int CIVIC_ROOT = 0xFF090503;
    private static final int CIVIC_CARD = 0xFF171008;
    private static final int CIVIC_GOLD_DARK = 0xFF5B3513;

    private PortalStatusHud() {
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (client.player == null || client.options.hudHidden) return;
        List<PortalRouteStatusSyncPayload.Entry> visible = PortalClientRouteStatus.all().stream()
                .filter(PortalRouteStatusSyncPayload.Entry::unlocked)
                .filter(route -> "scheduled_ticketed".equals(route.mode()))
                .toList();
        if (visible.isEmpty()) return;

        ElarionNotificationHud.RailAnchor anchor = ElarionNotificationHud.accessoryAnchor(client);
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        context.getMatrices().push();
        context.getMatrices().translate(anchor.screenX(), anchor.screenY(), 0.0F);
        context.getMatrices().scale(anchor.scale(), anchor.scale(), 1.0F);
        int separatorY = anchor.nextLogicalY() - 6;
        context.fill(X, separatorY, X + SLOT_SIZE, separatorY + 1, CIVIC_GOLD_DARK);
        int index = 0;
        long now = System.currentTimeMillis();
        for (PortalRouteStatusSyncPayload.Entry route : visible) {
            boolean open = isOpen(route, now);
            int accent = routeColor(route);
            int y = anchor.nextLogicalY() + index * (SLOT_SIZE + SLOT_GAP);
            if (open) {
                renderOpenSlot(context, X, y, accent);
            } else {
                renderClosedSlot(context, X, y, dim(accent));
            }
            drawGateTexture(context, route, X + 3, y + 3, 18, open);
            if (open) {
                int trackX = X + 3;
                int trackWidth = SLOT_SIZE - 6;
                context.fill(trackX, y + SLOT_SIZE - 4, trackX + trackWidth, y + SLOT_SIZE - 2,
                        style.insetColor());
                int progressWidth = Math.round(trackWidth * PortalWindowProgress.remaining(
                        now, route.opensAt(), route.closesAt(), true));
                if (progressWidth > 0) {
                    context.fill(trackX, y + SLOT_SIZE - 4, trackX + progressWidth,
                            y + SLOT_SIZE - 2, accent);
                }
            }
            index++;
        }
        context.getMatrices().pop();
    }

    public static void renderTooltip(DrawContext context, MinecraftClient client) {
        if (client.player == null || client.options.hudHidden) return;
        if (client.currentScreen == null) return;
        HoveredRoute hovered = hoveredRoute(client);
        if (hovered == null) return;
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 200.0F);
        context.drawTooltip(client.textRenderer, tooltip(hovered.route()),
                hovered.screenMouseX(), hovered.screenMouseY());
        context.getMatrices().pop();
    }

    private static HoveredRoute hoveredRoute(MinecraftClient client) {
        ElarionNotificationHud.RailAnchor anchor = ElarionNotificationHud.accessoryAnchor(client);
        int screenMouseX = (int) (client.mouse.getX() * client.getWindow().getScaledWidth()
                / client.getWindow().getWidth());
        int screenMouseY = (int) (client.mouse.getY() * client.getWindow().getScaledHeight()
                / client.getWindow().getHeight());
        double mouseX = anchor.logicalX(screenMouseX);
        double mouseY = anchor.logicalY(screenMouseY);
        int index = 0;
        for (PortalRouteStatusSyncPayload.Entry route : visibleRoutes()) {
            int y = anchor.nextLogicalY() + index * (SLOT_SIZE + SLOT_GAP);
            if (inside(mouseX, mouseY, X, y, SLOT_SIZE, SLOT_SIZE)) {
                return new HoveredRoute(route, screenMouseX, screenMouseY);
            }
            index++;
        }
        return null;
    }

    private static List<PortalRouteStatusSyncPayload.Entry> visibleRoutes() {
        return PortalClientRouteStatus.all().stream()
                .filter(PortalRouteStatusSyncPayload.Entry::unlocked)
                .filter(route -> "scheduled_ticketed".equals(route.mode()))
                .toList();
    }

    private static int routeColor(PortalRouteStatusSyncPayload.Entry route) {
        return 0xFF000000 | (route.argb() & 0x00FFFFFF);
    }

    private static void renderOpenSlot(DrawContext context, int x, int y, int accent) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, accent);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, CIVIC_CARD);
        context.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + 3, brighten(accent, 34));
    }

    private static void renderClosedSlot(DrawContext context, int x, int y, int accent) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, accent);
        context.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, CIVIC_ROOT);
        context.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + 3, 0x225B3513);
    }

    private static void drawGateTexture(
            DrawContext context, PortalRouteStatusSyncPayload.Entry route, int x, int y, int size, boolean open
    ) {
        Identifier texture = routeTexture(route);
        context.drawTexture(texture, x, y, 0.0F, 0.0F, size, size, TEXTURE_SOURCE_SIZE, TEXTURE_SOURCE_SIZE);
        if (!open) {
            context.fill(x, y, x + size, y + size, 0xAA090503);
        }
    }

    private static Identifier routeTexture(PortalRouteStatusSyncPayload.Entry route) {
        String key = (route.routeId() + " " + route.displayName()).toLowerCase(java.util.Locale.ROOT);
        return key.contains("end") ? END_GATE_TEXTURE : NETHER_GATE_TEXTURE;
    }

    private static int brighten(int argb, int amount) {
        int r = Math.min(255, ((argb >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + amount);
        int b = Math.min(255, (argb & 0xFF) + amount);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int dim(int argb) {
        int r = ((argb >> 16) & 0xFF) * 2 / 5;
        int g = ((argb >> 8) & 0xFF) * 2 / 5;
        int b = (argb & 0xFF) * 2 / 5;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static List<Text> tooltip(PortalRouteStatusSyncPayload.Entry route) {
        long now = System.currentTimeMillis();
        boolean open = isOpen(route, now);
        long target = open ? route.closesAt() : route.opensAt();
        String state = open ? "Open" : "Closed";
        String timer = target <= now
                ? (open ? "Closing now" : "Opening time pending")
                : formatDuration(target - now);
        String timerLabel = open ? "Closes in: " : "Next opening: ";
        return target <= now
                ? List.of(
                Text.literal(route.displayName()),
                Text.literal("Status: " + state),
                Text.literal(timer))
                : List.of(
                Text.literal(route.displayName()),
                Text.literal("Status: " + state),
                Text.literal(timerLabel + timer));
    }

    private static boolean isOpen(PortalRouteStatusSyncPayload.Entry route, long now) {
        return route.active() && route.opensAt() <= now && now < route.closesAt();
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

    private record HoveredRoute(
            PortalRouteStatusSyncPayload.Entry route,
            int screenMouseX,
            int screenMouseY
    ) {
    }
}
