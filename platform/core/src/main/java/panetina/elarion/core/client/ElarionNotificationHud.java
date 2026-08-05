package panetina.elarion.core.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiTypography;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionHudOverlayRegistry;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionEmptyStateLayout;
import panetina.elarion.core.client.ui.ElarionItemSlotLayout;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.ElarionNotificationRewardPreview;
import panetina.elarion.core.model.ElarionNotificationSnapshot;
import panetina.elarion.core.network.NotificationClaimPayload;
import panetina.elarion.core.network.NotificationDismissPayload;
import panetina.elarion.core.network.NotificationActionPayload;

import java.util.ArrayList;
import java.util.List;

public final class ElarionNotificationHud {
    private static final int SCREEN_MARGIN = ElarionNotificationHudLayout.SCREEN_MARGIN;
    private static final int RAIL_WIDTH = ElarionNotificationHudLayout.RAIL_WIDTH;
    private static final int PANEL_X = ElarionNotificationHudLayout.PANEL_X;
    private static final int PANEL_WIDTH = ElarionNotificationHudLayout.PANEL_WIDTH;
    private static final int LOGICAL_WIDTH = ElarionNotificationHudLayout.LOGICAL_WIDTH;
    private static final int LOGICAL_HEIGHT = ElarionNotificationHudLayout.LOGICAL_HEIGHT;
    private static final int MAX_PANEL_HEIGHT = ElarionNotificationHudLayout.MAX_PANEL_HEIGHT;
    private static final int DRAWER_HEADER_HEIGHT = ElarionNotificationHudLayout.DRAWER_HEADER_HEIGHT;
    private static final int CLOSE_SIZE = ElarionNotificationHudLayout.CLOSE_SIZE;
    private static final int LIST_MARGIN = ElarionNotificationHudLayout.LIST_MARGIN;
    private static final int LIST_TOP = ElarionNotificationHudLayout.LIST_TOP;
    private static final int LIST_BOTTOM_MARGIN = ElarionNotificationHudLayout.LIST_BOTTOM_MARGIN;
    private static final int EMPTY_CARD_HEIGHT = ElarionNotificationHudLayout.EMPTY_CARD_HEIGHT;
    private static final int PERSONAL_Y = 4;
    private static final int REALM_Y = 31;
    private static final int QUEST_Y = 58;
    private static final int WORLD_Y = 85;
    private static final int DISPLAY_SIZE = 24;
    private static final int ACCESSORY_Y = WORLD_Y + DISPLAY_SIZE + 9;
    private static final int ICON_DRAW_SIZE = 16;
    private static final int CARD_GAP = 4;
    private static final int REWARD_SLOT_SIZE = 18;
    private static final int REWARD_SLOT_GAP = 3;
    private static final String LOCAL_VIEW_ACTION = "elarion_core:view_notification";
    private static final int CIVIC_GREEN = ElarionCivicColors.ACTIVE_GREEN;
    private static final int CIVIC_PURPLE = ElarionCivicColors.QUEST_PURPLE;
    private static final int CIVIC_BLUE = ElarionCivicColors.INFO_BLUE;
    private static final int CIVIC_RED = ElarionCivicColors.REJECT_RED;
    private static final int CIVIC_RED_DARK = ElarionCivicColors.REJECT_RED_SHADOW;
    private static final int CIVIC_GOLD = ElarionCivicColors.GOLD_BORDER;
    private static final int CIVIC_GOLD_DARK = ElarionCivicColors.GOLD_SHADOW;
    private static final int CIVIC_GOLD_LIGHT = ElarionCivicColors.GOLD_HIGHLIGHT;

    private static ElarionNotificationSnapshot snapshot = ElarionNotificationSnapshot.EMPTY;
    private static String expandedNotificationId = "";
    private static String selectedNotificationId = "";
    private static String activeFilter = "";
    private static int scrollOffsetPixels;
    private static int detailScrollPixels;
    private static ElarionNotificationRewardPreview hoveredReward;
    private static ItemStack hoveredRewardStack = ItemStack.EMPTY;

    private ElarionNotificationHud() {}

    public static void initialize() {
        // Rendering is injected at the end of InGameHud so notifications stay above chat.
    }

    public static void update(ElarionNotificationSnapshot next) {
        snapshot = next == null ? ElarionNotificationSnapshot.EMPTY : next;
        if (!snapshot.worldVisible() && "world".equals(activeFilter)) close();
        if (!containsEntry(selectedNotificationId)) selectedNotificationId = "";
        if (!containsEntry(expandedNotificationId)) expandedNotificationId = "";
        clampScroll();
    }

    public static boolean handleClick(double mouseX, double mouseY, int button, int action) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null
                || client.currentScreen != null && !(client.currentScreen instanceof ChatScreen)) {
            return false;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || action != GLFW.GLFW_PRESS) return false;
        Layout layout = layout(client);
        double logicalX = layout.logicalX(mouseX);
        double logicalY = layout.logicalY(mouseY);
        if (insideIcon(logicalX, logicalY, PERSONAL_Y)) return open("personal");
        if (insideIcon(logicalX, logicalY, REALM_Y)) return open("realm");
        if (insideIcon(logicalX, logicalY, QUEST_Y)) return open("quest");
        if (snapshot.worldVisible() && insideIcon(logicalX, logicalY, WORLD_Y)) return open("world");
        if (!drawerOpen()) return false;
        if (inside(logicalX, logicalY, closeX(), 6, CLOSE_SIZE, CLOSE_SIZE)) {
            close();
            return true;
        }
        if (insideDrawer(logicalX, logicalY)) {
            handleDrawerClick(logicalX, logicalY);
            return true;
        }
        return false;
    }

    public static boolean handleScroll(double mouseX, double mouseY, double vertical) {
        if (!drawerOpen() || vertical == 0.0D) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        Layout layout = layout(client);
        if (!insideDrawer(layout.logicalX(mouseX), layout.logicalY(mouseY))) return false;
        int previous = detailOpen() ? detailScrollPixels : scrollOffsetPixels;
        if (detailOpen()) detailScrollPixels += vertical < 0 ? 18 : -18;
        else scrollOffsetPixels += vertical < 0 ? 18 : -18;
        clampScroll();
        return previous != (detailOpen() ? detailScrollPixels : scrollOffsetPixels);
    }

    public static boolean handleKey(int key, int scancode, int action) {
        if (!drawerOpen() || action != GLFW.GLFW_PRESS) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (detailOpen()) {
                expandedNotificationId = "";
                detailScrollPixels = 0;
                return true;
            }
            close();
            return true;
        }
        if (client.options.inventoryKey.matchesKey(key, scancode)) {
            close();
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            if (detailOpen()) detailScrollPixels += 18;
            else scrollOffsetPixels += 18;
            clampScroll();
            return true;
        }
        if (key == GLFW.GLFW_KEY_UP) {
            if (detailOpen()) detailScrollPixels -= 18;
            else scrollOffsetPixels -= 18;
            clampScroll();
            return true;
        }
        return false;
    }

    private static boolean open(String filter) {
        String next = filter == null ? "" : filter;
        if (!next.equals(activeFilter)) {
            scrollOffsetPixels = 0;
            detailScrollPixels = 0;
            expandedNotificationId = "";
            selectedNotificationId = "";
        }
        activeFilter = next;
        clampScroll();
        return true;
    }

    private static void close() {
        activeFilter = "";
        expandedNotificationId = "";
        selectedNotificationId = "";
        detailScrollPixels = 0;
    }

    private static boolean drawerOpen() {
        return !activeFilter.isBlank();
    }

    private static void handleDrawerClick(double mouseX, double mouseY) {
        ElarionNotificationEntry selected = selectedEntry();
        int panelHeight = drawerHeight();
        if (detailOpen()) {
            if (inside(mouseX, mouseY, listX(), LIST_TOP, 44, detailBackHeight())) {
                expandedNotificationId = "";
                detailScrollPixels = 0;
                clampScroll();
                return;
            }
            ElarionNotificationAction action = actionBandActionAt(mouseX, mouseY, selected, panelHeight);
            if (action != null) runAction(selected, action);
            return;
        }
        ElarionNotificationAction action = actionBandActionAt(mouseX, mouseY, selected, panelHeight);
        if (action != null) {
            runAction(selected, action);
            return;
        }
        int listX = listX();
        int listY = LIST_TOP;
        int listWidth = listWidth();
        int cursorY = listY - scrollOffsetPixels;
        int rowHeight = rowHeight();
        for (ElarionNotificationEntry entry : entries()) {
            if (inside(mouseX, mouseY, listX, cursorY, listWidth, rowHeight)) {
                selectEntry(entry);
                return;
            }
            cursorY += rowHeight + CARD_GAP;
        }
    }

    public static void renderAboveChat(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        if (client.currentScreen instanceof ChatScreen) return;
        render(context, client);
    }

    public static void renderOverChatScreen(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden
                || !(client.currentScreen instanceof ChatScreen)) {
            return;
        }
        render(context, client);
    }

    private static void render(DrawContext context, MinecraftClient client) {
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 1000.0F);
        ElarionHudOverlayRegistry.renderBeforeNotifications(context, client);
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        Layout layout = layout(client);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);
        drawRailShell(context);
        if (drawerOpen()) drawDrawer(context, client.textRenderer, layout);
        drawIcon(context, PERSONAL_Y, RailIcon.MAIL, snapshot.hasUnread("personal"),
                "personal".equals(activeFilter), style);
        drawIcon(context, REALM_Y, RailIcon.REALM, snapshot.hasUnread("realm"),
                "realm".equals(activeFilter), style);
        drawIcon(context, QUEST_Y, RailIcon.QUEST, snapshot.hasUnread("quest"),
                "quest".equals(activeFilter), style);
        if (snapshot.worldVisible()) {
            drawIcon(context, WORLD_Y, RailIcon.WORLD, snapshot.hasUnread("world"),
                    "world".equals(activeFilter), style);
        }
        context.getMatrices().pop();
        ElarionHudOverlayRegistry.renderAfterNotifications(context, client);
        context.getMatrices().pop();
    }

    private static void drawIcon(
            DrawContext context, int y, RailIcon icon, boolean unread, boolean selected, ElarionUiStyle style
    ) {
        boolean pointerVisible = drawerOpen() && ElarionNotificationHudLayout.railPointerVisible(
                selected, y + DISPLAY_SIZE / 2, drawerHeight());
        drawRailSlot(context, y, selected, pointerVisible);
        int iconX = (RAIL_WIDTH - ICON_DRAW_SIZE) / 2;
        int iconY = y + (DISPLAY_SIZE - ICON_DRAW_SIZE) / 2;
        drawRailGlyph(context, icon, iconX, iconY);
        if (unread) {
            drawUnreadMarker(context, iconX + ICON_DRAW_SIZE - 5, iconY - 1);
        }
    }

    private static void drawRailSlot(DrawContext context, int y, boolean selected, boolean pointerVisible) {
        int x = 3;
        int width = RAIL_WIDTH - 6;
        ElarionCivicUi.rowSurface(context, x, y, width, DISPLAY_SIZE, selected, false, false);
        if (pointerVisible) {
            int centerY = y + DISPLAY_SIZE / 2;
            context.fill(x + width, centerY - 4, x + width + 1, centerY + 5, CIVIC_GOLD_DARK);
            context.fill(x + width + 1, centerY - 3, x + width + 2, centerY + 4, CIVIC_GOLD);
            context.fill(x + width + 2, centerY - 2, x + width + 3, centerY + 3, CIVIC_GOLD_LIGHT);
            context.fill(x + width + 3, centerY - 1, x + width + 4, centerY + 2, CIVIC_GOLD);
        }
    }

    private static void drawRailShell(DrawContext context) {
        int height = WORLD_Y + DISPLAY_SIZE + 4;
        ElarionCivicUi.railShell(context, 0, 0, RAIL_WIDTH, height);
    }

    private static void drawRailGlyph(DrawContext context, RailIcon icon, int x, int y) {
        switch (icon) {
            case MAIL -> ElarionUiIcons.drawOrDefault(context, "mail", x, y, ICON_DRAW_SIZE);
            case REALM -> ElarionUiIcons.drawOrDefault(context, "realm", x, y, ICON_DRAW_SIZE);
            case QUEST -> ElarionUiIcons.drawOrDefault(context, "quest", x - 1, y - 1, ICON_DRAW_SIZE + 2);
            case WORLD -> ElarionUiIcons.drawOrDefault(context, "world", x - 1, y - 1, ICON_DRAW_SIZE + 2);
        }
    }

    private static void drawUnreadMarker(DrawContext context, int x, int y) {
        context.fill(x, y + 1, x + 6, y + 6, CIVIC_GOLD_DARK);
        context.fill(x + 1, y, x + 5, y + 7, CIVIC_GOLD_DARK);
        context.fill(x + 1, y + 1, x + 5, y + 6, CIVIC_RED);
        context.fill(x + 2, y + 2, x + 4, y + 4, 0xFFFFFFFF);
        context.fill(x + 2, y + 5, x + 4, y + 6, 0xFFFFFFFF);
    }

    private static void drawDrawer(DrawContext context, TextRenderer renderer, Layout layout) {
        MinecraftClient client = MinecraftClient.getInstance();
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        int panelHeight = drawerHeight();
        drawDrawerShell(context, renderer, panelHeight);
        double screenMouseX = client.mouse.getX() * client.getWindow().getScaledWidth()
                / client.getWindow().getWidth();
        double screenMouseY = client.mouse.getY() * client.getWindow().getScaledHeight()
                / client.getWindow().getHeight();
        double mouseX = layout.logicalX(screenMouseX);
        double mouseY = layout.logicalY(screenMouseY);
        hoveredReward = null;
        hoveredRewardStack = ItemStack.EMPTY;

        List<ElarionNotificationEntry> entries = entries();
        int listX = listX();
        int listWidth = listWidth();
        if (entries.isEmpty()) {
            ElarionEmptyStateLayout.EmptyState emptyLayout = ElarionEmptyStateLayout.compact(
                    listX, LIST_TOP, listWidth, EMPTY_CARD_HEIGHT, ElarionUiTypography.lineHeight());
            ElarionCivicUi.rowSurface(context, emptyLayout.panel().x(), emptyLayout.panel().y(),
                    emptyLayout.panel().width(), emptyLayout.panel().height(), false, false, true);
            String emptyTitle = emptyTitle();
            String emptyBody = emptyBody();
            ElarionUiTypography.draw(context, renderer, emptyTitle,
                    emptyLayout.titleX(), emptyLayout.titleY(), CIVIC_GOLD_LIGHT, false);
            ElarionUiRenderer.wrappedClipped(context, renderer, Text.literal(emptyBody),
                    emptyLayout.body().x(), emptyLayout.body().y(), emptyLayout.body().width(),
                    emptyLayout.body().height(),
                    style.textColor(), style.mutedColor());
            return;
        }

        ElarionNotificationEntry selected = selectedEntry();
        if (detailOpen() && selected != null) {
            drawDetail(context, renderer, selected, panelHeight, mouseX, mouseY, layout, style);
            drawActionBand(context, renderer, selected, panelHeight, mouseX, mouseY, style);
            drawRewardTooltip(context, renderer, (int) mouseX, (int) mouseY);
            return;
        }

        int listBottom = listBottom(panelHeight);
        int sx1 = layout.screenX() + Math.round(listX * layout.scale());
        int sy1 = layout.screenY() + Math.round(LIST_TOP * layout.scale());
        int sx2 = layout.screenX() + Math.round((listX + listWidth) * layout.scale());
        int sy2 = layout.screenY() + Math.round(listBottom * layout.scale());
        context.enableScissor(sx1, sy1, sx2, sy2);
        try {
            int cursorY = LIST_TOP - scrollOffsetPixels;
            int rowHeight = rowHeight();
            for (ElarionNotificationEntry entry : entries) {
                if (cursorY + rowHeight >= LIST_TOP && cursorY <= listBottom) {
                    drawEntry(context, renderer, entry, listX, cursorY, listWidth, rowHeight,
                            mouseX, mouseY, style);
                }
                cursorY += rowHeight + CARD_GAP;
            }
        } finally {
            context.disableScissor();
        }
        if (scrollOffsetPixels < maxScroll()) {
            drawDownArrow(context, PANEL_X + PANEL_WIDTH / 2, listBottom(panelHeight) - 6, style);
        }
        drawActionBand(context, renderer, selected, panelHeight, mouseX, mouseY, style);
        drawRewardTooltip(context, renderer, (int) mouseX, (int) mouseY);
    }

    private static void drawEntry(
            DrawContext context, TextRenderer renderer, ElarionNotificationEntry entry,
            int x, int y, int width, int height, double mouseX, double mouseY, ElarionUiStyle style
    ) {
        boolean selected = entry.id().equals(selectedNotificationId);
        boolean hovered = inside(mouseX, mouseY, x, y, width, height);
        int accent = categoryAccent(entry);
        ElarionCivicUi.rowSurface(context, x, y, width, height, selected, hovered, false);
        context.fill(x + 2, y + 3, x + 4, y + height - 3, selected ? CIVIC_GREEN : accent);
        if (entry.unread()) {
            context.fill(x + width - 8, y + 5, x + width - 4, y + 9, CIVIC_RED_DARK);
            context.fill(x + width - 7, y + 4, x + width - 5, y + 10, CIVIC_RED_DARK);
            context.fill(x + width - 7, y + 5, x + width - 5, y + 9, CIVIC_RED);
        }

        int padding = 6;
        int iconSize = Math.min(20, height - 10);
        int iconX = x + padding;
        int iconY = y + (height - iconSize) / 2;
        drawCardIcon(context, renderer, entry, iconX, iconY, iconSize, style);

        int textX = iconX + iconSize + 7;
        String age = ageLabel(entry.createdAt());
        int ageWidth = ElarionUiTypography.width(renderer, age);
        int titleRight = x + width - padding - (entry.unread() ? 9 : 0);
        int titleWidth = Math.max(24, titleRight - textX - ageWidth - 7);
        int titleColor = selected ? CIVIC_GREEN : entry.unread() ? CIVIC_GOLD_LIGHT : style.titleColor();
        ElarionUiTypography.draw(context, renderer,
                ElarionUiRenderer.ellipsize(renderer, entry.title(), titleWidth),
                textX, y + 4, titleColor, false);
        ElarionUiTypography.drawRight(context, renderer, age, titleRight, y + 4, style.mutedColor(), false);
        String summary = entry.status().isBlank() ? entry.body() : entry.status();
        ElarionUiTypography.draw(context, renderer,
                ElarionUiRenderer.ellipsize(renderer, summary, x + width - padding - textX),
                textX, y + height - ElarionUiTypography.fontHeight(renderer) - 4,
                style.mutedColor(), false);
    }

    private static void drawDetail(
            DrawContext context, TextRenderer renderer, ElarionNotificationEntry entry,
            int panelHeight, double mouseX, double mouseY, Layout layout, ElarionUiStyle style
    ) {
        int backHeight = detailBackHeight();
        boolean backHover = inside(mouseX, mouseY, listX(), LIST_TOP, 44, backHeight);
        drawLocalButton(context, renderer, listX(), LIST_TOP, 44, backHeight,
                "elarion_core:back", "Back", backHover, true, style);

        int top = detailContentTop();
        int bottom = listBottom(panelHeight);
        int sx1 = layout.screenX() + Math.round(listX() * layout.scale());
        int sy1 = layout.screenY() + Math.round(top * layout.scale());
        int sx2 = layout.screenX() + Math.round((listX() + listWidth()) * layout.scale());
        int sy2 = layout.screenY() + Math.round(bottom * layout.scale());
        context.enableScissor(sx1, sy1, sx2, sy2);
        try {
            int y = top - detailScrollPixels;
            int accent = categoryAccent(entry);
            drawMessageBody(context, listX(), y, listWidth(), detailContentHeight(renderer, entry), accent);
            int iconSize = 28;
            drawCardIcon(context, renderer, entry, listX() + 7, y + 7, iconSize, style);
            int textX = listX() + 43;
            ElarionUiTypography.draw(context, renderer,
                    ElarionUiRenderer.ellipsize(renderer, entry.title(), listWidth() - 50),
                    textX, y + 8, accent, false);
            if (!entry.status().isBlank()) {
                ElarionUiTypography.draw(context, renderer,
                        ElarionUiRenderer.ellipsize(renderer, entry.status(), listWidth() - 50),
                        textX, y + 10 + ElarionUiTypography.lineHeight(), CIVIC_GOLD, false);
            }
            int bodyY = y + 45;
            List<net.minecraft.text.OrderedText> lines = ElarionUiTypography.wrap(
                    renderer, Text.literal(entry.body()), listWidth() - 14);
            for (int index = 0; index < lines.size(); index++) {
                ElarionUiTypography.draw(context, renderer, lines.get(index), listX() + 7,
                        bodyY + index * ElarionUiTypography.lineHeight(), style.textColor(), false);
            }
            if (!entry.rewards().isEmpty()) {
                int rewardsY = bodyY + lines.size() * ElarionUiTypography.lineHeight() + 8;
                renderRewardPreviews(context, renderer, entry, listX() + 7, rewardsY,
                        listWidth() - 14, rewardGridHeight(entry), mouseX, mouseY, style);
            }
        } finally {
            context.disableScissor();
        }
        if (detailScrollPixels < maxDetailScroll()) {
            drawDownArrow(context, PANEL_X + PANEL_WIDTH / 2, bottom - 6, style);
        }
    }

    private static void drawMessageBody(
            DrawContext context, int x, int y, int width, int height, int accent
    ) {
        ElarionCivicUi.messageBody(context, x, y, width, height, accent);
    }

    private static void drawDrawerShell(DrawContext context, TextRenderer renderer, int panelHeight) {
        ElarionCivicUi.headerShell(context, PANEL_X, 0, PANEL_WIDTH, panelHeight, DRAWER_HEADER_HEIGHT);
        String title = drawerTitle();
        String visibleTitle = ElarionUiRenderer.ellipsize(renderer, title, PANEL_WIDTH - 62);
        ElarionUiTypography.drawCentered(context, renderer, visibleTitle,
                PANEL_X + PANEL_WIDTH / 2, 7, CIVIC_GOLD_LIGHT, false);
        ElarionCivicUi.headerOrnament(context, PANEL_X + 12, 11, false);
        ElarionCivicUi.headerOrnament(context, PANEL_X + PANEL_WIDTH - 24, 11, true);
        ElarionCivicUi.closeButton(context, closeX(), 6, CLOSE_SIZE);
        ElarionCivicUi.divider(context, PANEL_X + 8, 19, PANEL_WIDTH - 16);
    }

    private static void drawActionButton(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            ElarionNotificationAction action, boolean hovered, ElarionUiStyle style
    ) {
        boolean active = action.enabled();
        boolean destructive = isDismissAction(action.id(), action.label());
        boolean primary = isPrimaryAction(action.id(), action.label());
        ElarionCivicUi.Tone tone = destructive ? ElarionCivicUi.Tone.DESTRUCTIVE
                : primary ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.NORMAL;
        ElarionCivicUi.compactActionButtonFrame(context, x, y, width, height, hovered, active, tone, style);
        String label = action.label().isBlank() ? defaultActionLabel(action.id()) : action.label();
        int glyphWidth = width >= 32 ? 8 : 0;
        int glyphGap = glyphWidth > 0 ? 4 : 0;
        String visible = ElarionUiRenderer.ellipsize(renderer, label, Math.max(1, width - 10 - glyphWidth - glyphGap));
        int color = !active ? style.mutedColor()
                : destructive ? ElarionCivicColors.DESTRUCTIVE_TEXT : style.textColor();
        int contentWidth = glyphWidth + glyphGap + ElarionUiTypography.width(renderer, visible);
        int contentX = x + Math.max(4, (width - contentWidth) / 2);
        if (glyphWidth > 0) drawActionGlyph(context, action, contentX, y + (height - 8) / 2, color);
        ElarionUiTypography.draw(context, renderer, visible, contentX + glyphWidth + glyphGap,
                y + Math.max(2, (height - ElarionUiTypography.fontHeight(renderer)) / 2), color, false);
    }

    private static void drawActionGlyph(
            DrawContext context, ElarionNotificationAction action, int x, int y, int color
    ) {
        String normalized = actionText(action.id(), action.label());
        String icon = normalized.contains("dismiss") || normalized.contains("decline")
                || normalized.contains("reject") ? "dismiss"
                : normalized.contains("view") ? "view"
                : normalized.contains("claim") ? "claim"
                : normalized.contains("accept") || normalized.contains("approve") ? "accept"
                : normalized.contains("back") ? "back"
                : "go_to";
        ElarionUiIcons.drawOrDefault(context, icon, x, y, 8);
    }

    private static void drawLocalButton(
            DrawContext context, TextRenderer renderer, int x, int y, int width, int height,
            String id, String label, boolean hovered, boolean active, ElarionUiStyle style
    ) {
        ElarionNotificationAction action = new ElarionNotificationAction(id, label, active);
        drawActionButton(context, renderer, x, y, width, height, action, hovered, style);
    }

    private static void drawActionBand(
            DrawContext context, TextRenderer renderer, ElarionNotificationEntry entry,
            int panelHeight, double mouseX, double mouseY, ElarionUiStyle style
    ) {
        if (entry == null) return;
        List<ElarionNotificationAction> actions = drawerActions(entry);
        if (actions.isEmpty()) return;
        int top = actionBandTop(panelHeight, entry);
        ElarionCivicUi.divider(context, PANEL_X + 2, top, PANEL_WIDTH - 4);
        ElarionUiTypography.drawCentered(context, renderer, "ACTIONS", PANEL_X + PANEL_WIDTH / 2,
                top + 4, CIVIC_GOLD, false);
        int buttonHeight = actionButtonHeight();
        int y = top + actionHeaderHeight();
        int available = PANEL_WIDTH - LIST_MARGIN * 2;
        for (int row = 0; row < actionRowCount(actions); row++) {
            int from = row * 4;
            int count = Math.min(4, actions.size() - from);
            int gap = 3;
            int buttonWidth = (available - gap * (count - 1)) / count;
            for (int column = 0; column < count; column++) {
                ElarionNotificationAction action = actions.get(from + column);
                int x = listX() + column * (buttonWidth + gap);
                boolean hover = inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight);
                drawActionButton(context, renderer, x, y, buttonWidth, buttonHeight, action, hover, style);
            }
            y += buttonHeight + 3;
        }
    }

    private static void renderRewardPreviews(
            DrawContext context, TextRenderer renderer, ElarionNotificationEntry entry,
            int x, int y, int width, int height, double mouseX, double mouseY, ElarionUiStyle style
    ) {
        if (height <= 0 || width <= 0) return;
        int slot = REWARD_SLOT_SIZE;
        int gap = REWARD_SLOT_GAP;
        int columns = Math.max(1, Math.min(6, (width + gap) / (slot + gap)));
        int rows = Math.max(1, (int) Math.ceil(entry.rewards().size() / (float) columns));
        int visibleRows = Math.max(1, Math.min(rows, (height + gap) / (slot + gap)));
        int visible = Math.min(entry.rewards().size(), columns * visibleRows);
        int gridColumns = Math.min(columns, visible);
        int gridWidth = gridColumns * slot + Math.max(0, gridColumns - 1) * gap;
        int startX = x + Math.max(0, (width - gridWidth) / 2);
        for (int index = 0; index < visible; index++) {
            var reward = entry.rewards().get(index);
            ElarionItemSlotLayout.Slot rewardSlot =
                    ElarionItemSlotLayout.gridSlot(startX, y, index, columns, slot, gap, 1);
            ElarionUiRenderer.beveledBox(context, rewardSlot.bounds().x(), rewardSlot.bounds().y(),
                    rewardSlot.bounds().width(), rewardSlot.bounds().height(), style.insetColor(), style);
            drawRewardPreviewIcon(context, renderer, reward.icon(), reward.count(), rewardSlot);
            if (rewardSlot.contains(mouseX, mouseY)) {
                hoveredReward = reward;
                hoveredRewardStack = rewardStack(reward);
            }
        }
    }

    private static void drawRewardTooltip(
            DrawContext context, TextRenderer renderer, int mouseX, int mouseY
    ) {
        if (hoveredReward == null) return;
        List<Text> customTooltip = rewardTooltipLines(hoveredReward);
        if (!customTooltip.isEmpty()) {
            context.drawTooltip(renderer, customTooltip, mouseX, mouseY);
            return;
        }
        if (!hoveredRewardStack.isEmpty()) {
            context.drawItemTooltip(renderer, hoveredRewardStack, mouseX, mouseY);
            return;
        }
        String label = hoveredReward.label().isBlank() ? "Reward" : hoveredReward.label();
        if (hoveredReward.count() > 1) label = hoveredReward.count() + " " + label;
        context.drawTooltip(renderer, List.of(Text.literal(label)), mouseX, mouseY);
    }

    private static List<Text> rewardTooltipLines(ElarionNotificationRewardPreview reward) {
        if (reward == null || reward.tooltipLines().isEmpty()) return List.of();
        List<Text> lines = new ArrayList<>();
        String label = reward.label().isBlank() ? "Reward" : reward.label();
        if (reward.count() > 1) label = reward.count() + " " + label;
        lines.add(Text.literal(label));
        String title = label;
        reward.tooltipLines().stream()
                .filter(line -> !line.equalsIgnoreCase(title))
                .map(Text::literal)
                .forEach(lines::add);
        return List.copyOf(lines);
    }

    private static ItemStack rewardStack(ElarionNotificationRewardPreview reward) {
        if (reward == null || !reward.icon().startsWith("item:")) return ItemStack.EMPTY;
        Identifier id = Identifier.tryParse(reward.icon().substring("item:".length()));
        if (id == null || !Registries.ITEM.containsId(id)) return ItemStack.EMPTY;
        return new ItemStack(Registries.ITEM.get(id), Math.max(1, reward.count()));
    }

    private static void drawDownArrow(DrawContext context, int centerX, int y, ElarionUiStyle style) {
        context.fill(centerX - 4, y, centerX + 5, y + 1, style.titleColor());
        context.fill(centerX - 3, y + 1, centerX + 4, y + 2, style.titleColor());
        context.fill(centerX - 2, y + 2, centerX + 3, y + 3, style.titleColor());
        context.fill(centerX - 1, y + 3, centerX + 2, y + 4, style.titleColor());
        context.fill(centerX, y + 4, centerX + 1, y + 5, style.titleColor());
    }

    private static void runAction(ElarionNotificationEntry entry, ElarionNotificationAction action) {
        if (action == null || !action.enabled()) return;
        if (LOCAL_VIEW_ACTION.equals(action.id())) {
            expandedNotificationId = entry.id();
            detailScrollPixels = 0;
            clampScroll();
            return;
        }
        ClientPlayNetworking.send(new NotificationActionPayload(entry.id(), action.id()));
    }

    private static void selectEntry(ElarionNotificationEntry entry) {
        if (entry == null || entry.id().isBlank()) return;
        selectedNotificationId = entry.id();
        expandedNotificationId = "";
        detailScrollPixels = 0;
        if (entry.unread()) {
            ClientPlayNetworking.send(new NotificationActionPayload(entry.id(), "elarion_core:mark_read"));
        }
        clampScroll();
    }

    private static List<ElarionNotificationEntry> entries() {
        return snapshot.filtered(activeFilter, 50);
    }

    private static boolean containsEntry(String id) {
        return id != null && !id.isBlank() && entries().stream().anyMatch(entry -> id.equals(entry.id()));
    }

    private static boolean detailOpen() {
        return !expandedNotificationId.isBlank() && expandedNotificationId.equals(selectedNotificationId);
    }

    private static int totalContentHeight() {
        return Math.max(0, entries().size() * (rowHeight() + CARD_GAP) - CARD_GAP);
    }

    private static int maxScroll() {
        if (detailOpen()) return maxDetailScroll();
        return maxListScroll();
    }

    private static int maxListScroll() {
        return Math.max(0, totalContentHeight() - listViewportHeight());
    }

    private static void clampScroll() {
        scrollOffsetPixels = Math.max(0, Math.min(scrollOffsetPixels, maxListScroll()));
        detailScrollPixels = Math.max(0, Math.min(detailScrollPixels, maxDetailScroll()));
    }

    private static List<ElarionNotificationAction> visibleActions(ElarionNotificationEntry entry) {
        return entry.actions().stream()
                .filter(action -> !"elarion_core:mark_read".equals(action.id()))
                .toList();
    }

    private static List<ElarionNotificationAction> drawerActions(ElarionNotificationEntry entry) {
        if (entry == null) return List.of();
        List<ElarionNotificationAction> actions = new ArrayList<>();
        if (!detailOpen()) actions.add(new ElarionNotificationAction(LOCAL_VIEW_ACTION, "View", true));
        actions.addAll(visibleActions(entry));
        return List.copyOf(actions);
    }

    private static ElarionNotificationAction actionBandActionAt(
            double mouseX, double mouseY, ElarionNotificationEntry entry, int panelHeight
    ) {
        List<ElarionNotificationAction> actions = drawerActions(entry);
        if (actions.isEmpty()) return null;
        int available = PANEL_WIDTH - LIST_MARGIN * 2;
        int buttonHeight = actionButtonHeight();
        int y = actionBandTop(panelHeight, entry) + actionHeaderHeight();
        for (int row = 0; row < actionRowCount(actions); row++) {
            int from = row * 4;
            int count = Math.min(4, actions.size() - from);
            int gap = 3;
            int buttonWidth = (available - gap * (count - 1)) / count;
            for (int column = 0; column < count; column++) {
                int x = listX() + column * (buttonWidth + gap);
                if (inside(mouseX, mouseY, x, y, buttonWidth, buttonHeight)) {
                    return actions.get(from + column);
                }
            }
            y += buttonHeight + 3;
        }
        return null;
    }

    private static boolean insideIcon(double mouseX, double mouseY, int y) {
        return mouseX >= 0
                && mouseX < RAIL_WIDTH
                && mouseY >= y
                && mouseY < y + DISPLAY_SIZE + 4;
    }

    private static boolean insideDrawer(double mouseX, double mouseY) {
        return mouseX >= PANEL_X
                && mouseX < PANEL_X + PANEL_WIDTH
                && mouseY >= 0
                && mouseY < drawerHeight();
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static Layout layout(MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        float safeWidth = Math.max(1, screenWidth - SCREEN_MARGIN);
        float safeHeight = Math.max(1, screenHeight - SCREEN_MARGIN);
        float scale = Math.min(1.0F, Math.min(safeWidth / LOGICAL_WIDTH, safeHeight / LOGICAL_HEIGHT));
        return new Layout(SCREEN_MARGIN, SCREEN_MARGIN, Math.max(0.35F, scale));
    }

    public static RailAnchor railAnchor(MinecraftClient client) {
        return accessoryAnchor(client);
    }

    public static RailAnchor accessoryAnchor(MinecraftClient client) {
        Layout layout = layout(client);
        return new RailAnchor(layout.screenX(), layout.screenY(), layout.scale(),
                ACCESSORY_Y);
    }

    public record RailAnchor(int screenX, int screenY, float scale, int nextLogicalY) {
        public double logicalX(double screenX) {
            return (screenX - this.screenX) / scale;
        }

        public double logicalY(double screenY) {
            return (screenY - this.screenY) / scale;
        }
    }

    private record Layout(int screenX, int screenY, float scale) {
        double logicalX(double screenX) { return (screenX - this.screenX) / scale; }
        double logicalY(double screenY) { return (screenY - this.screenY) / scale; }
    }


    private enum RailIcon {
        MAIL,
        REALM,
        QUEST,
        WORLD
    }

    private static void drawCardIcon(
            DrawContext context, TextRenderer renderer, ElarionNotificationEntry entry,
            int x, int y, int size, ElarionUiStyle style
    ) {
        if (entry.category() == ElarionNotificationCategory.REWARD) {
            ElarionUiIcons.drawOrDefault(context, "reward", x, y, size);
            return;
        }
        String icon = entry.icon() == null || entry.icon().isBlank()
                || "item:minecraft:paper".equals(entry.icon())
                ? semanticNotificationIcon(entry)
                : entry.icon();
        if (ElarionUiIcons.has(icon)) {
            ElarionUiIcons.drawOrDefault(context, icon, x, y, size);
            return;
        }
        if (icon.startsWith("item:")) {
            Identifier itemId = Identifier.tryParse(icon.substring("item:".length()));
            if (itemId != null && Registries.ITEM.containsId(itemId)) {
                drawCenteredItem(context, new ItemStack(Registries.ITEM.get(itemId)), x, y, size);
                return;
            }
        }
        Identifier texture = Identifier.tryParse(icon);
        if (texture != null) context.drawTexture(texture, x + 2, y + 2, 0, 0, size - 4, size - 4, 16, 16);
    }

    private static String semanticNotificationIcon(ElarionNotificationEntry entry) {
        return switch (entry.category()) {
            case PERSONAL, MAIL -> "mail";
            case REALM, GOVERNMENT -> "realm";
            case QUEST -> "quest";
            case WORLD -> "world";
            case REWARD -> "reward";
        };
    }

    private static void drawCenteredItem(DrawContext context, ItemStack stack, int x, int y, int size) {
        int itemX = x + Math.max(0, (size - 16) / 2);
        int itemY = y + Math.max(0, (size - 16) / 2);
        context.drawItem(stack, itemX, itemY);
    }

    private static void drawRewardPreviewIcon(
            DrawContext context,
            TextRenderer renderer,
            String rawIcon,
            int count,
            ElarionItemSlotLayout.Slot slot
    ) {
        String icon = rawIcon == null ? "" : rawIcon;
        if (icon.startsWith("item:")) {
            Identifier id = Identifier.tryParse(icon.substring("item:".length()));
            if (id != null && Registries.ITEM.containsId(id)) {
                ItemStack stack = new ItemStack(Registries.ITEM.get(id), Math.max(1, count));
                context.drawItem(stack, slot.itemDrawX(), slot.itemDrawY());
                context.drawItemInSlot(renderer, stack, slot.itemDrawX(), slot.itemDrawY());
                return;
            }
        }
        if (ElarionUiIcons.has(icon)) {
            int drawSize = Math.min(16, Math.max(1, slot.bounds().width() - 4));
            int drawX = slot.bounds().x() + (slot.bounds().width() - drawSize) / 2;
            int drawY = slot.bounds().y() + (slot.bounds().height() - drawSize) / 2;
            ElarionUiIcons.drawOrDefault(context, icon, drawX, drawY, drawSize);
            if (count > 1) {
                String visibleCount = String.valueOf(count);
                ElarionUiTypography.draw(context, renderer, visibleCount,
                        slot.bounds().x() + slot.bounds().width() - 2 - ElarionUiTypography.width(renderer, visibleCount),
                        slot.bounds().y() + slot.bounds().height() - 10,
                        0xFFFFFFFF, true);
            }
            return;
        }
        if (icon.isBlank()) {
            ElarionUiIcons.drawOrDefault(context, "reward", slot.itemDrawX(), slot.itemDrawY(),
                    Math.max(1, slot.item().width()));
            return;
        }
        Identifier texture = Identifier.tryParse(icon);
        if (texture != null) {
            int drawSize = Math.min(16, Math.max(1, slot.bounds().width() - 4));
            int drawX = slot.bounds().x() + (slot.bounds().width() - drawSize) / 2;
            int drawY = slot.bounds().y() + (slot.bounds().height() - drawSize) / 2;
            context.drawTexture(texture, drawX, drawY, 0, 0, drawSize, drawSize, 16, 16);
            if (count > 1) {
                String visibleCount = String.valueOf(count);
                ElarionUiTypography.draw(context, renderer, visibleCount,
                        slot.bounds().x() + slot.bounds().width() - 2 - ElarionUiTypography.width(renderer, visibleCount),
                        slot.bounds().y() + slot.bounds().height() - 10,
                        0xFFFFFFFF, true);
            }
        }
    }

    private static void texturedBox(
            DrawContext context, int x, int y, int width, int height,
            int fill, ElarionUiStyle style, boolean useCardTexture
    ) {
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, style);
        if (useCardTexture) textureFill(context, x + 2, y + 2,
                Math.max(0, width - 4), Math.max(0, height - 4), style);
    }

    private static void textureFill(DrawContext context, int x, int y, int width, int height, ElarionUiStyle style) {
        textureFillAligned(context, x, y, width, height, style, x, y);
    }

    private static void textureFillAligned(
            DrawContext context, int x, int y, int width, int height,
            ElarionUiStyle style, int originX, int originY
    ) {
        if (width <= 0 || height <= 0) return;
        Identifier texture = parseTexture(style.cardTexture());
        if (texture == null) texture = parseTexture(style.panelTexture());
        if (texture == null) texture = Identifier.of("elarion", "textures/gui/shared/panel_parchment.png");
        if (texture == null) return;
        int endX = x + width;
        int endY = y + height;
        for (int drawY = y; drawY < endY; ) {
            int sourceY = Math.floorMod(drawY - originY, 16);
            int tileHeight = Math.min(16 - sourceY, endY - drawY);
            for (int drawX = x; drawX < endX; ) {
                int sourceX = Math.floorMod(drawX - originX, 16);
                int tileWidth = Math.min(16 - sourceX, endX - drawX);
                context.drawTexture(texture, drawX, drawY, sourceX, sourceY,
                        tileWidth, tileHeight, 16, 16);
                drawX += tileWidth;
            }
            drawY += tileHeight;
        }
        if ((style.panelTextureTint() >>> 24) != 0 && style.panelTextureTint() != 0xFFFFFFFF) {
            context.fill(x, y, x + width, y + height, style.panelTextureTint());
        }
    }

    private static Identifier parseTexture(String raw) {
        return raw == null || raw.isBlank() ? null : Identifier.tryParse(raw);
    }

    private static String defaultActionLabel(String actionId) {
        return "dismiss".equals(actionId) ? "Dismiss" : "Claim";
    }

    private static int listX() {
        return ElarionNotificationHudLayout.listX();
    }

    private static int closeX() {
        return ElarionNotificationHudLayout.closeX();
    }

    private static int listWidth() {
        return ElarionNotificationHudLayout.listWidth();
    }

    private static int listBottom(int panelHeight) {
        ElarionNotificationEntry selected = selectedEntry();
        if (selected == null || actionBandHeight(selected) == 0) return panelHeight - LIST_BOTTOM_MARGIN;
        return actionBandTop(panelHeight, selected) - 4;
    }

    private static int listViewportHeight() {
        return Math.max(1, listBottom(drawerHeight()) - LIST_TOP);
    }

    private static int drawerHeight() {
        if (entries().isEmpty()) {
            return LIST_TOP + EMPTY_CARD_HEIGHT + LIST_BOTTOM_MARGIN;
        }
        if (detailOpen()) {
            ElarionNotificationEntry selected = selectedEntry();
            if (selected == null) return MAX_PANEL_HEIGHT;
            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
            return ElarionNotificationHudLayout.boundedDetailDrawerHeight(
                    detailContentTop(),
                    detailContentHeight(renderer, selected),
                    actionBandHeight(selected),
                    selectedRailPointerBottom());
        }
        ElarionNotificationEntry selected = selectedEntry();
        int footer = selected == null ? 0 : actionBandHeight(selected);
        int separation = selected == null ? 0 : 4;
        int available = MAX_PANEL_HEIGHT - LIST_TOP - LIST_BOTTOM_MARGIN - footer - separation;
        int content = Math.min(totalContentHeight(), Math.max(rowHeight(), available));
        return LIST_TOP + content + separation + footer + LIST_BOTTOM_MARGIN;
    }

    private static String drawerTitle() {
        return "NOTIFICATIONS";
    }

    private static int selectedRailPointerBottom() {
        int y = switch (activeFilter) {
            case "personal" -> PERSONAL_Y;
            case "realm" -> REALM_Y;
            case "quest" -> QUEST_Y;
            case "world" -> WORLD_Y;
            default -> PERSONAL_Y;
        };
        return y + DISPLAY_SIZE + LIST_BOTTOM_MARGIN;
    }

    private static ElarionNotificationEntry selectedEntry() {
        if (selectedNotificationId.isBlank()) return null;
        return entries().stream().filter(entry -> selectedNotificationId.equals(entry.id())).findFirst().orElse(null);
    }

    private static int rowHeight() {
        return ElarionNotificationHudLayout.rowHeight();
    }

    private static int detailBackHeight() {
        return Math.max(16, ElarionUiTypography.lineHeight() + 6);
    }

    private static int detailContentTop() {
        return LIST_TOP + detailBackHeight() + 4;
    }

    private static int actionHeaderHeight() {
        return ElarionNotificationHudLayout.actionHeaderHeight();
    }

    private static int actionButtonHeight() {
        return ElarionNotificationHudLayout.actionButtonHeight();
    }

    private static int actionRowCount(List<ElarionNotificationAction> actions) {
        return Math.max(1, (actions.size() + 3) / 4);
    }

    private static int actionBandHeight(ElarionNotificationEntry entry) {
        if (entry == null) return 0;
        List<ElarionNotificationAction> actions = drawerActions(entry);
        if (actions.isEmpty()) return 0;
        int rows = actionRowCount(actions);
        return actionHeaderHeight()
                + rows * (actionButtonHeight() + 3) + 2;
    }

    private static int actionBandTop(int panelHeight, ElarionNotificationEntry entry) {
        return panelHeight - LIST_BOTTOM_MARGIN - actionBandHeight(entry);
    }

    private static int detailContentHeight(TextRenderer renderer, ElarionNotificationEntry entry) {
        int lines = Math.max(1, ElarionUiTypography.wrap(renderer, Text.literal(entry.body()), listWidth() - 14).size());
        int rewards = entry.rewards().isEmpty() ? 0 : rewardGridHeight(entry) + 10;
        return 53 + lines * ElarionUiTypography.lineHeight() + rewards;
    }

    private static int rewardGridHeight(ElarionNotificationEntry entry) {
        int columns = Math.max(1, (listWidth() - 14 + REWARD_SLOT_GAP) / (REWARD_SLOT_SIZE + REWARD_SLOT_GAP));
        int rows = Math.max(1, (int) Math.ceil(entry.rewards().size() / (float) columns));
        return rows * REWARD_SLOT_SIZE + Math.max(0, rows - 1) * REWARD_SLOT_GAP;
    }

    private static int maxDetailScroll() {
        if (!detailOpen()) return 0;
        ElarionNotificationEntry entry = selectedEntry();
        if (entry == null) return 0;
        int viewport = Math.max(1, listBottom(drawerHeight()) - detailContentTop());
        return Math.max(0, detailContentHeight(MinecraftClient.getInstance().textRenderer, entry) - viewport);
    }

    private static String ageLabel(long createdAt) {
        long seconds = Math.max(0L, (System.currentTimeMillis() - createdAt) / 1000L);
        if (seconds < 60L) return "now";
        long minutes = seconds / 60L;
        if (minutes < 60L) return minutes + "m";
        long hours = minutes / 60L;
        if (hours < 24L) return hours + "h";
        return hours / 24L + "d";
    }

    private static String emptyTitle() {
        return switch (activeFilter) {
            case "personal" -> "No personal mail.";
            case "realm" -> "No realm notices.";
            case "quest" -> "No active quests.";
            case "world" -> "No world events.";
            default -> "No notifications.";
        };
    }

    private static String emptyBody() {
        return switch (activeFilter) {
            case "personal" -> "Letters, private outcomes, and claimable rewards will appear here.";
            case "realm" -> "Realm announcements, votes, and civic notices will appear here.";
            case "quest" -> "Assigned and accepted quests will appear here.";
            case "world" -> "Global-stage events will appear here when your Realm can see them.";
            default -> "New messages, votes, and rewards will appear here.";
        };
    }

    private static boolean isPrimaryAction(String id, String label) {
        String normalized = actionText(id, label);
        return normalized.contains("claim") || normalized.contains("accept")
                || normalized.contains("approve");
    }

    private static boolean isDismissAction(String id, String label) {
        String normalized = actionText(id, label);
        return normalized.contains("dismiss") || normalized.contains("reject")
                || normalized.contains("decline") || normalized.contains("deny");
    }

    private static String actionText(String id, String label) {
        return ((id == null ? "" : id) + " " + (label == null ? "" : label)).toLowerCase(java.util.Locale.ROOT);
    }

    private static int categoryAccent(ElarionNotificationEntry entry) {
        return switch (entry.category()) {
            case REALM, GOVERNMENT -> CIVIC_GREEN;
            case QUEST -> CIVIC_PURPLE;
            case WORLD -> CIVIC_BLUE;
            default -> CIVIC_GOLD;
        };
    }

}
