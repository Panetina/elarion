package panetina.elarion.core.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.ElarionNotificationRewardPreview;
import panetina.elarion.core.model.ElarionNotificationSnapshot;
import panetina.elarion.core.network.NotificationClaimPayload;
import panetina.elarion.core.network.NotificationDismissPayload;
import panetina.elarion.core.network.NotificationActionPayload;

import java.util.List;

public final class ElarionNotificationHud {
    private static final Identifier MAIL_NONEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/mail_nonew.png");
    private static final Identifier MAIL_NEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/mail_new.png");
    private static final Identifier REALM_NONEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/realm_nonew.png");
    private static final Identifier REALM_NEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/realm_new.png");
    private static final Identifier QUEST_NONEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/quest_nonew.png");
    private static final Identifier QUEST_NEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/quest_new.png");
    private static final Identifier WORLD_NONEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/world_nonew.png");
    private static final Identifier WORLD_NEW_TEXTURE =
            Identifier.of("elarion_core", "textures/gui/notifications/world_new.png");

    private static final int SCREEN_MARGIN = 8;
    private static final int RAIL_WIDTH = 36;
    private static final int RAIL_PANEL_GAP = 4;
    private static final int PANEL_X = RAIL_WIDTH + RAIL_PANEL_GAP;
    private static final int PANEL_WIDTH = 224;
    private static final int LOGICAL_WIDTH = RAIL_WIDTH + RAIL_PANEL_GAP + PANEL_WIDTH;
    private static final int LOGICAL_HEIGHT = 320;
    private static final int PERSONAL_Y = 0;
    private static final int REALM_Y = 36;
    private static final int QUEST_Y = 72;
    private static final int WORLD_Y = 108;
    private static final int SOURCE_SIZE = 16;
    private static final int DISPLAY_SIZE = 32;
    private static final int ICON_DRAW_SIZE = 28;
    private static final int PANEL_HEIGHT = LOGICAL_HEIGHT;
    private static final int CARD_GAP = 6;
    private static final int COLLAPSED_CARD_HEIGHT = 54;
    private static final int EXPANDED_CARD_BASE_HEIGHT = 118;
    private static final int REWARD_SLOT_SIZE = 20;
    private static final int REWARD_SLOT_GAP = 4;
    private static final int CLAIM_WIDTH = 50;
    private static final int CLAIM_HEIGHT = 16;

    private static ElarionNotificationSnapshot snapshot = ElarionNotificationSnapshot.EMPTY;
    private static String expandedNotificationId = "";
    private static String activeFilter = "";
    private static int scrollOffsetPixels;
    private static ElarionNotificationRewardPreview hoveredReward;
    private static ItemStack hoveredRewardStack = ItemStack.EMPTY;

    private ElarionNotificationHud() {}

    public static void initialize() {
        // Rendering is injected at the end of InGameHud so notifications stay above chat.
    }

    public static void update(ElarionNotificationSnapshot next) {
        snapshot = next == null ? ElarionNotificationSnapshot.EMPTY : next;
        if (!snapshot.worldVisible() && "world".equals(activeFilter)) close();
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
        int previous = scrollOffsetPixels;
        scrollOffsetPixels += vertical < 0 ? 18 : -18;
        clampScroll();
        return previous != scrollOffsetPixels;
    }

    public static boolean handleKey(int key, int scancode, int action) {
        if (!drawerOpen() || action != GLFW.GLFW_PRESS) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (client.options.inventoryKey.matchesKey(key, scancode)) {
            close();
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            scrollOffsetPixels += 18;
            clampScroll();
            return true;
        }
        if (key == GLFW.GLFW_KEY_UP) {
            scrollOffsetPixels -= 18;
            clampScroll();
            return true;
        }
        return false;
    }

    private static boolean open(String filter) {
        String next = filter == null ? "" : filter;
        if (!next.equals(activeFilter)) {
            scrollOffsetPixels = 0;
            expandedNotificationId = "";
        }
        activeFilter = next;
        clampScroll();
        return true;
    }

    private static void close() {
        activeFilter = "";
        expandedNotificationId = "";
    }

    private static boolean drawerOpen() {
        return !activeFilter.isBlank();
    }

    private static void handleDrawerClick(double mouseX, double mouseY) {
        List<ElarionNotificationEntry> entries = entries();
        int listX = PANEL_X + 8;
        int listY = 10;
        int listWidth = PANEL_WIDTH - 16;
        int cursorY = listY - scrollOffsetPixels;
        for (ElarionNotificationEntry entry : entries) {
            int height = cardHeight(entry, listWidth);
            if (mouseY >= cursorY && mouseY < cursorY + height) {
                ElarionNotificationAction clicked =
                        actionAt(mouseX, mouseY, entry, listX, cursorY, listWidth, height);
                if (clicked != null) {
                    runAction(entry, clicked);
                } else {
                    toggleExpanded(entry.id());
                }
                return;
            }
            cursorY += height + CARD_GAP;
        }
    }

    public static void renderAboveChat(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        Layout layout = layout(client);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);
        if (drawerOpen()) drawDrawer(context, client.textRenderer, layout);
        drawIcon(context, PERSONAL_Y, snapshot.hasUnread("personal") ? MAIL_NEW_TEXTURE : MAIL_NONEW_TEXTURE,
                "personal".equals(activeFilter), style);
        drawIcon(context, REALM_Y, snapshot.hasUnread("realm") ? REALM_NEW_TEXTURE : REALM_NONEW_TEXTURE,
                "realm".equals(activeFilter), style);
        drawIcon(context, QUEST_Y, snapshot.hasUnread("quest") ? QUEST_NEW_TEXTURE : QUEST_NONEW_TEXTURE,
                "quest".equals(activeFilter), style);
        if (snapshot.worldVisible()) {
            drawIcon(context, WORLD_Y, snapshot.hasUnread("world") ? WORLD_NEW_TEXTURE : WORLD_NONEW_TEXTURE,
                    "world".equals(activeFilter), style);
        }
        context.getMatrices().pop();
    }

    private static void drawIcon(DrawContext context, int y, Identifier texture, boolean selected, ElarionUiStyle style) {
        if (selected) {
            texturedBox(context, 0, y, PANEL_X + 3, DISPLAY_SIZE + 4,
                    style.panelColor(), style, true);
        }
        int iconX = (RAIL_WIDTH - ICON_DRAW_SIZE) / 2;
        int iconY = y + (DISPLAY_SIZE + 4 - ICON_DRAW_SIZE) / 2;
        drawScaledTexture(context, texture, iconX, iconY, ICON_DRAW_SIZE);
    }

    private static void drawDrawer(DrawContext context, TextRenderer renderer, Layout layout) {
        MinecraftClient client = MinecraftClient.getInstance();
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        ElarionUiRenderer.panel(context, PANEL_X, 0, PANEL_WIDTH, PANEL_HEIGHT, style);
        double screenMouseX = client.mouse.getX() * client.getWindow().getScaledWidth()
                / client.getWindow().getWidth();
        double screenMouseY = client.mouse.getY() * client.getWindow().getScaledHeight()
                / client.getWindow().getHeight();
        double mouseX = layout.logicalX(screenMouseX);
        double mouseY = layout.logicalY(screenMouseY);
        hoveredReward = null;
        hoveredRewardStack = ItemStack.EMPTY;

        List<ElarionNotificationEntry> entries = entries();
        int listX = PANEL_X + 8;
        int listY = 10;
        int listWidth = PANEL_WIDTH - 16;
        int listBottom = PANEL_HEIGHT - 10;
        if (entries.isEmpty()) {
            ElarionUiRenderer.borderedBox(context, listX, listY, listWidth, 92, style);
            String emptyTitle = "quest".equals(activeFilter) ? "No active quests." : "No notifications.";
            String emptyBody = "quest".equals(activeFilter)
                    ? "Assigned and accepted quests will appear here."
                    : "New messages, votes, and rewards will appear here.";
            context.drawText(renderer, emptyTitle, listX + 8, listY + 10, style.titleColor(), false);
            ElarionUiRenderer.wrappedClipped(context, renderer, Text.literal(emptyBody),
                    listX + 8, listY + 26, listWidth - 16, 40, style.textColor(), style.mutedColor());
            return;
        }

        int sx1 = layout.screenX() + Math.round(listX * layout.scale());
        int sy1 = layout.screenY() + Math.round(listY * layout.scale());
        int sx2 = layout.screenX() + Math.round((listX + listWidth) * layout.scale());
        int sy2 = layout.screenY() + Math.round(listBottom * layout.scale());
        context.enableScissor(sx1, sy1, sx2, sy2);
        try {
            int cursorY = listY - scrollOffsetPixels;
            for (ElarionNotificationEntry entry : entries) {
                int height = cardHeight(entry, listWidth);
                if (cursorY + height >= listY && cursorY <= listBottom) {
                    drawEntry(context, renderer, entry, listX, cursorY, listWidth, height,
                            mouseX, mouseY, style);
                }
                cursorY += height + CARD_GAP;
            }
        } finally {
            context.disableScissor();
        }
        if (scrollOffsetPixels < maxScroll()) {
            drawDownArrow(context, PANEL_X + PANEL_WIDTH / 2, PANEL_HEIGHT - 12, style);
        }
        drawRewardTooltip(context, renderer, (int) mouseX, (int) mouseY);
    }

    private static void drawEntry(
            DrawContext context, TextRenderer renderer, ElarionNotificationEntry entry,
            int x, int y, int width, int height, double mouseX, double mouseY, ElarionUiStyle style
    ) {
        boolean open = entry.id().equals(expandedNotificationId);
        int fill = entry.unread() ? style.headerColor() : style.cardColor();
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, style);

        int padding = 8;
        int iconSize = 24;
        int iconX = x + padding;
        int iconY = y + 7;
        drawCardIcon(context, renderer, entry, iconX, iconY, iconSize, style);

        List<ElarionNotificationAction> actions = visibleActions(entry);
        int claimX = x + width - CLAIM_WIDTH - padding;
        int textX = x + 40;
        int titleWidth = open
                ? x + width - padding - textX
                : !actions.isEmpty() ? claimX - textX - 8 : width - 48;
        context.drawText(renderer, ElarionUiRenderer.ellipsize(renderer, entry.title(), titleWidth),
                textX, y + 8, style.titleColor(), false);

        if (open) {
            if (!entry.rewards().isEmpty()) {
                int bodyY = y + 24;
                int bodyWidth = width - 56;
                ElarionUiRenderer.wrappedClipped(context, renderer, Text.literal(entry.body()),
                        textX, bodyY, bodyWidth, 24, style.textColor(), style.mutedColor());
                int gridX = x + padding;
                int gridY = y + 55;
                int gridWidth = width - padding * 2;
                int gridBottom = y + height - CLAIM_HEIGHT - 16;
                int gridHeight = Math.max(0, gridBottom - gridY);
                if (gridHeight > 0) {
                    texturedBox(context, gridX, gridY, gridWidth, gridHeight, style.insetColor(), style, false);
                    renderRewardPreviews(context, renderer, entry, gridX + 6, gridY + 6,
                            gridWidth - 12, Math.max(0, gridHeight - 12), mouseX, mouseY, style);
                }
            } else {
                int bodyX = x + padding;
                int bodyY = y + 38;
                int bodyWidth = width - padding * 2;
                int bodyHeight = Math.max(24, height - 38 - CLAIM_HEIGHT - 18);
                ElarionUiRenderer.beveledBox(context, bodyX, bodyY, bodyWidth, bodyHeight,
                        style.insetColor(), style);
                if (!entry.status().isBlank()) {
                    context.drawText(renderer, entry.status(), bodyX + 7, bodyY + 6,
                            style.mutedColor(), false);
                }
                ElarionUiRenderer.wrappedClipped(context, renderer, Text.literal(entry.body()),
                        bodyX + 7, bodyY + (entry.status().isBlank() ? 7 : 20),
                        bodyWidth - 14, bodyHeight - (entry.status().isBlank() ? 14 : 27),
                        style.textColor(), style.mutedColor());
            }
        } else {
            int collapsedTextWidth = !actions.isEmpty() ? claimX - textX - 8 : width - 48;
            context.drawText(renderer, ElarionUiRenderer.ellipsize(renderer, entry.body(), collapsedTextWidth),
                    textX, y + 23, style.textColor(), false);
        }
        if (!actions.isEmpty() && !open) {
            ElarionNotificationAction action = actions.getFirst();
            int claimY = y + (height - CLAIM_HEIGHT) / 2;
            ElarionUiRenderer.compactButton(context, renderer,
                    claimX, claimY, CLAIM_WIDTH, CLAIM_HEIGHT,
                    action.label().isBlank() ? defaultActionLabel(action.id()) : action.label(),
                    false, action.enabled(), style);
        } else if (!actions.isEmpty()) {
            int gap = 6;
            int actionCount = Math.min(3, actions.size());
            int buttonWidth = Math.min(76, (width - padding * 2 - gap * (actionCount - 1)) / actionCount);
            int rowWidth = buttonWidth * actionCount + gap * (actionCount - 1);
            int startX = x + (width - rowWidth) / 2;
            int buttonY = y + height - CLAIM_HEIGHT - 8;
            for (int index = 0; index < actionCount; index++) {
                ElarionNotificationAction action = actions.get(index);
                ElarionUiRenderer.compactButton(context, renderer,
                        startX + index * (buttonWidth + gap), buttonY, buttonWidth, CLAIM_HEIGHT,
                        action.label().isBlank() ? defaultActionLabel(action.id()) : action.label(),
                        false, action.enabled(), style);
            }
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
            int row = index / columns;
            int column = index % columns;
            int slotX = startX + column * (slot + gap);
            int slotY = y + row * (slot + gap);
            ElarionUiRenderer.beveledBox(context, slotX, slotY, slot, slot, style.insetColor(), style);
            drawRewardPreviewIcon(context, renderer, reward.icon(), reward.count(), slotX, slotY, slot);
            if (inside(mouseX, mouseY, slotX, slotY, slot, slot)) {
                hoveredReward = reward;
                hoveredRewardStack = rewardStack(reward);
            }
        }
    }

    private static void drawRewardTooltip(
            DrawContext context, TextRenderer renderer, int mouseX, int mouseY
    ) {
        if (hoveredReward == null) return;
        if (!hoveredRewardStack.isEmpty()) {
            context.drawItemTooltip(renderer, hoveredRewardStack, mouseX, mouseY);
            return;
        }
        String label = hoveredReward.label().isBlank() ? "Reward" : hoveredReward.label();
        if (hoveredReward.count() > 1) label = hoveredReward.count() + " " + label;
        context.drawTooltip(renderer, List.of(Text.literal(label)), mouseX, mouseY);
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
        ClientPlayNetworking.send(new NotificationActionPayload(entry.id(), action.id()));
    }

    private static void toggleExpanded(String id) {
        if (id == null || id.isBlank()) return;
        boolean opening = !id.equals(expandedNotificationId);
        expandedNotificationId = opening ? id : "";
        if (opening) ClientPlayNetworking.send(new NotificationActionPayload(id, "elarion_core:mark_read"));
        clampScroll();
    }

    private static List<ElarionNotificationEntry> entries() {
        return snapshot.filtered(activeFilter, 50);
    }

    private static int cardHeight(ElarionNotificationEntry entry, int width) {
        if (!entry.id().equals(expandedNotificationId)) return COLLAPSED_CARD_HEIGHT;
        int columns = Math.max(1, (width - 16) / (REWARD_SLOT_SIZE + REWARD_SLOT_GAP));
        int rows = entry.rewards().isEmpty() ? 0
                : (int) Math.ceil(entry.rewards().size() / (float) columns);
        return EXPANDED_CARD_BASE_HEIGHT + Math.max(0, rows - 1) * (REWARD_SLOT_SIZE + REWARD_SLOT_GAP);
    }

    private static int totalContentHeight() {
        int total = 0;
        int width = PANEL_WIDTH - 16;
        for (ElarionNotificationEntry entry : entries()) {
            total += cardHeight(entry, width) + CARD_GAP;
        }
        return Math.max(0, total - CARD_GAP);
    }

    private static int maxScroll() {
        return Math.max(0, totalContentHeight() - (PANEL_HEIGHT - 20));
    }

    private static void clampScroll() {
        scrollOffsetPixels = Math.max(0, Math.min(scrollOffsetPixels, maxScroll()));
    }

    private static List<ElarionNotificationAction> visibleActions(ElarionNotificationEntry entry) {
        return entry.actions().stream()
                .filter(action -> !"elarion_core:mark_read".equals(action.id()))
                .toList();
    }

    private static ElarionNotificationAction actionAt(
            double mouseX, double mouseY, ElarionNotificationEntry entry,
            int x, int y, int width, int height
    ) {
        List<ElarionNotificationAction> actions = visibleActions(entry);
        if (actions.isEmpty()) return null;
        boolean open = entry.id().equals(expandedNotificationId);
        if (!open) {
            int buttonX = x + width - CLAIM_WIDTH - 8;
            int buttonY = y + (height - CLAIM_HEIGHT) / 2;
            return inside(mouseX, mouseY, buttonX, buttonY, CLAIM_WIDTH, CLAIM_HEIGHT)
                    ? actions.getFirst() : null;
        }
        int gap = 6;
        int actionCount = Math.min(3, actions.size());
        int buttonWidth = Math.min(76, (width - 16 - gap * (actionCount - 1)) / actionCount);
        int rowWidth = buttonWidth * actionCount + gap * (actionCount - 1);
        int startX = x + (width - rowWidth) / 2;
        int buttonY = y + height - CLAIM_HEIGHT - 8;
        for (int index = 0; index < actionCount; index++) {
            int buttonX = startX + index * (buttonWidth + gap);
            if (inside(mouseX, mouseY, buttonX, buttonY, buttonWidth, CLAIM_HEIGHT)) {
                return actions.get(index);
            }
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
                && mouseY < PANEL_HEIGHT;
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

    private record Layout(int screenX, int screenY, float scale) {
        double logicalX(double screenX) { return (screenX - this.screenX) / scale; }
        double logicalY(double screenY) { return (screenY - this.screenY) / scale; }
    }

    private static void drawScaledTexture16(DrawContext context, Identifier texture, int x, int y) {
        drawScaledTexture(context, texture, x, y, DISPLAY_SIZE);
    }

    private static void drawScaledTexture(DrawContext context, Identifier texture, int x, int y, int size) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0F);
        context.getMatrices().scale(size / (float) SOURCE_SIZE, size / (float) SOURCE_SIZE, 1.0F);
        context.drawTexture(texture, 0, 0, 0.0F, 0.0F, SOURCE_SIZE, SOURCE_SIZE, SOURCE_SIZE, SOURCE_SIZE);
        context.getMatrices().pop();
    }

    private static void drawCardIcon(
            DrawContext context, TextRenderer renderer, ElarionNotificationEntry entry,
            int x, int y, int size, ElarionUiStyle style
    ) {
        ElarionUiRenderer.beveledBox(context, x, y, size, size, style.insetColor(), style);
        if (entry.category() == ElarionNotificationCategory.REWARD) {
            drawCenteredItem(context, new ItemStack(Items.CHEST), x, y, size);
            return;
        }
        String icon = entry.icon() == null || entry.icon().isBlank() ? "item:minecraft:paper" : entry.icon();
        if (icon.startsWith("item:")) {
            Identifier itemId = Identifier.tryParse(icon.substring("item:".length()));
            if (itemId != null && Registries.ITEM.containsId(itemId)) {
                drawCenteredItem(context, new ItemStack(Registries.ITEM.get(itemId)), x, y, size);
                return;
            }
        }
        Identifier texture = Identifier.tryParse(icon);
        if (texture != null) context.drawTexture(texture, x + 4, y + 4, 0, 0, size - 8, size - 8, 16, 16);
    }

    private static void drawCenteredItem(DrawContext context, ItemStack stack, int x, int y, int size) {
        int itemX = x + Math.max(0, (size - 16) / 2);
        int itemY = y + Math.max(0, (size - 16) / 2);
        context.drawItem(stack, itemX, itemY);
    }

    private static void drawRewardPreviewIcon(
            DrawContext context, TextRenderer renderer, String rawIcon, int count, int x, int y, int size
    ) {
        String icon = rawIcon == null ? "" : rawIcon;
        if (icon.startsWith("item:")) {
            Identifier id = Identifier.tryParse(icon.substring("item:".length()));
            if (id != null && Registries.ITEM.containsId(id)) {
                ItemStack stack = new ItemStack(Registries.ITEM.get(id), Math.max(1, count));
                int itemX = x + Math.max(1, (size - 16) / 2);
                int itemY = y + Math.max(1, (size - 16) / 2);
                context.drawItem(stack, itemX, itemY);
                context.drawItemInSlot(renderer, stack, itemX, itemY);
                return;
            }
        }
        Identifier texture = Identifier.tryParse(icon);
        if (texture != null) {
            int drawSize = Math.min(16, Math.max(1, size - 4));
            int drawX = x + (size - drawSize) / 2;
            int drawY = y + (size - drawSize) / 2;
            context.drawTexture(texture, drawX, drawY, 0, 0, drawSize, drawSize, 16, 16);
            if (count > 1) {
                String visibleCount = String.valueOf(count);
                context.drawText(renderer, visibleCount,
                        x + size - 2 - renderer.getWidth(visibleCount), y + size - 10,
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
        if (width <= 0 || height <= 0) return;
        Identifier texture = parseTexture(style.cardTexture());
        if (texture == null) texture = parseTexture(style.panelTexture());
        if (texture == null) texture = Identifier.of("elarion", "textures/gui/shared/panel_parchment.png");
        if (texture == null) return;
        for (int tileY = y; tileY < y + height; tileY += 16) {
            for (int tileX = x; tileX < x + width; tileX += 16) {
                int tileWidth = Math.min(16, x + width - tileX);
                int tileHeight = Math.min(16, y + height - tileY);
                context.drawTexture(texture, tileX, tileY, 0, 0, tileWidth, tileHeight, 16, 16);
            }
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
}
