package panetina.elarion.addons.underworld.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import panetina.elarion.addons.underworld.model.StoredItemStack;
import panetina.elarion.addons.underworld.network.GraveOpenPayload;
import panetina.elarion.addons.underworld.network.GraveRecoverPayload;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;

import java.util.ArrayList;
import java.util.List;

public final class GraveRecoveryScreen extends ElarionScreen {
    private static final int MAX_PANEL_WIDTH = 448;
    private static final int MAX_PANEL_HEIGHT = 318;
    private static final int MIN_PANEL_WIDTH = 330;
    private static final int MIN_PANEL_HEIGHT = 238;
    private static final int SLOT_SIZE = 26;
    private static final int SLOT_GAP = 5;

    private GraveOpenPayload payload;
    private final List<DisplayEntry> displayEntries = new ArrayList<>();
    private boolean decoded;
    private int firstRow;

    public GraveRecoveryScreen(GraveOpenPayload payload) {
        super(Text.literal(payload.title()));
        update(payload);
    }

    public void update(GraveOpenPayload payload) {
        this.payload = payload;
        this.displayEntries.clear();
        this.decoded = false;
        this.firstRow = 0;
    }

    @Override
    protected void init() {
        decodeEntriesIfReady();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        decodeEntriesIfReady();
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        context.fill(0, 0, width, height, style.backgroundOverlayColor());

        int panelWidth = Math.max(MIN_PANEL_WIDTH, Math.min(MAX_PANEL_WIDTH, width - 32));
        int panelHeight = Math.max(MIN_PANEL_HEIGHT, Math.min(MAX_PANEL_HEIGHT, height - 32));
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;
        int innerX = x + 16;
        int innerWidth = panelWidth - 32;
        int headerHeight = 42;
        int footerHeight = 36;
        int gridY = y + headerHeight + 74;
        int gridBottom = y + panelHeight - footerHeight - 8;
        int gridHeight = Math.max(SLOT_SIZE, gridBottom - gridY);
        int columns = Math.max(1, Math.min(9, (innerWidth + SLOT_GAP) / (SLOT_SIZE + SLOT_GAP)));
        int visibleRows = Math.max(1, gridHeight / (SLOT_SIZE + SLOT_GAP));
        int maxFirstRow = Math.max(0, (displayEntries.size() + columns - 1) / columns - visibleRows);
        firstRow = Math.max(0, Math.min(firstRow, maxFirstRow));

        ElarionCivicUi.attachedShell(context, x, y, panelWidth, panelHeight, headerHeight);
        ElarionCivicUi.headerOrnament(context, x + panelWidth / 2 - 104, y + 22, true);
        ElarionCivicUi.headerOrnament(context, x + panelWidth / 2 + 104, y + 22, false);
        context.drawCenteredTextWithShadow(textRenderer, payload.title(), width / 2, y + 15, style.titleColor());

        renderStatus(context, style, innerX, y + headerHeight + 8, innerWidth);
        renderGrid(context, style, innerX, gridY, columns, visibleRows, mouseX, mouseY);
        renderFooter(context, style, x, y, panelWidth, panelHeight, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelWidth = Math.max(MIN_PANEL_WIDTH, Math.min(MAX_PANEL_WIDTH, width - 32));
        int innerWidth = panelWidth - 32;
        int columns = Math.max(1, Math.min(9, (innerWidth + SLOT_GAP) / (SLOT_SIZE + SLOT_GAP)));
        int panelHeight = Math.max(MIN_PANEL_HEIGHT, Math.min(MAX_PANEL_HEIGHT, height - 32));
        int gridHeight = Math.max(SLOT_SIZE, panelHeight - 42 - 74 - 36 - 8);
        int visibleRows = Math.max(1, gridHeight / (SLOT_SIZE + SLOT_GAP));
        int maxFirstRow = Math.max(0, (displayEntries.size() + columns - 1) / columns - visibleRows);
        int direction = verticalAmount > 0.0D ? -1 : verticalAmount < 0.0D ? 1 : 0;
        if (direction == 0 || maxFirstRow <= 0) return false;
        firstRow = Math.max(0, Math.min(maxFirstRow, firstRow + direction));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelWidth = Math.max(MIN_PANEL_WIDTH, Math.min(MAX_PANEL_WIDTH, width - 32));
        int panelHeight = Math.max(MIN_PANEL_HEIGHT, Math.min(MAX_PANEL_HEIGHT, height - 32));
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;
        int recoverX = x + panelWidth / 2 - 124;
        int closeX = x + panelWidth / 2 + 22;
        int buttonY = y + panelHeight - 30;
        if (button == 0 && recoverEnabled() && inside(mouseX, mouseY, recoverX, buttonY, 124, 20)) {
            ClientPlayNetworking.send(new GraveRecoverPayload(payload.corpseId()));
            return true;
        }
        if (button == 0 && inside(mouseX, mouseY, closeX, buttonY, 92, 20)) {
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderStatus(DrawContext context, ElarionUiStyle style, int x, int y, int width) {
        int chipWidth = Math.min(158, width);
        ElarionCivicUi.statusChip(context, textRenderer, x, y + 6, statusText(), chipWidth,
                statusTone(), style);

        String count = payload.totalItemCount() + (payload.totalItemCount() == 1 ? " item" : " items");
        ElarionUiTypography.draw(context, textRenderer, count, x + width - ElarionUiTypography.width(textRenderer, count), y + 7,
                style.mutedColor(), false);

        String owner = payload.ownerName().isBlank() ? "" : "Belongs to " + payload.ownerName();
        if (!owner.isBlank()) {
            ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, owner, width),
                    x, y + 30, style.mutedColor(), false);
        }

        String body = payload.body();
        if (!body.isBlank()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, body, width),
                    x + width / 2, y + 42, payload.error() ? style.errorColor() : style.textColor());
        }
    }

    private void renderGrid(
            DrawContext context,
            ElarionUiStyle style,
            int x,
            int y,
            int columns,
            int visibleRows,
            int mouseX,
            int mouseY
    ) {
        int start = firstRow * columns;
        int end = Math.min(displayEntries.size(), start + columns * visibleRows);
        if (displayEntries.isEmpty()) {
            String empty = decoded ? "No visible items." : "Loading grave items...";
            context.drawCenteredTextWithShadow(textRenderer, empty, width / 2, y + 16, style.mutedColor());
            return;
        }
        for (int index = start; index < end; index++) {
            int local = index - start;
            int slotX = x + (local % columns) * (SLOT_SIZE + SLOT_GAP);
            int slotY = y + (local / columns) * (SLOT_SIZE + SLOT_GAP);
            boolean hovered = inside(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            ElarionCivicUi.thinBox(context, slotX, slotY, SLOT_SIZE, SLOT_SIZE,
                    hovered ? ElarionCivicColors.CARD_HOVER : ElarionCivicColors.MESSAGE_BODY,
                    ElarionCivicColors.GOLD_BORDER);
            ItemStack stack = displayEntries.get(index).stack();
            context.drawItem(stack, slotX + 5, slotY + 5);
            context.drawItemInSlot(textRenderer, stack, slotX + 5, slotY + 5);
            if (hovered) {
                context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
            }
        }
        int totalRows = (displayEntries.size() + columns - 1) / columns;
        if (totalRows > visibleRows) {
            String scroll = (firstRow + 1) + " / " + Math.max(1, totalRows - visibleRows + 1);
            ElarionUiTypography.draw(context, textRenderer, scroll, x + columns * (SLOT_SIZE + SLOT_GAP) + 4, y,
                    style.mutedColor(), false);
        }
    }

    private void renderFooter(
            DrawContext context,
            ElarionUiStyle style,
            int x,
            int y,
            int panelWidth,
            int panelHeight,
            int mouseX,
            int mouseY
    ) {
        int recoverX = x + panelWidth / 2 - 124;
        int closeX = x + panelWidth / 2 + 22;
        int buttonY = y + panelHeight - 30;
        ElarionCivicUi.compactActionButton(context, textRenderer, recoverX, buttonY, 124, 20,
                "Recover All", inside(mouseX, mouseY, recoverX, buttonY, 124, 20), false,
                recoverEnabled(), recoverEnabled() ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.MUTED,
                style);
        ElarionCivicUi.compactActionButton(context, textRenderer, closeX, buttonY, 92, 20,
                "Close", inside(mouseX, mouseY, closeX, buttonY, 92, 20), false, true,
                ElarionCivicUi.Tone.NORMAL, style);
    }

    private void decodeEntriesIfReady() {
        if (decoded || client == null || client.world == null) return;
        displayEntries.clear();
        for (GraveOpenPayload.Entry entry : payload.items()) {
            StoredItemStack stored = new StoredItemStack(entry.itemId(), entry.count());
            stored.stackNbt = entry.stackNbt();
            stored.sourceType = entry.sourceType();
            stored.sourceId = entry.sourceId();
            stored.sourceLabel = entry.sourceLabel();
            stored.slotIndex = entry.slotIndex();
            stored.equipmentSlot = entry.equipmentSlot();
            ItemStack stack = stored.toStack(client.world.getRegistryManager());
            if (!stack.isEmpty()) displayEntries.add(new DisplayEntry(entry, stack));
        }
        decoded = true;
    }

    private boolean recoverEnabled() {
        return !payload.error() && payload.totalItemCount() > 0;
    }

    private String statusText() {
        long now = System.currentTimeMillis();
        return switch (payload.accessState()) {
            case "protected" -> "Protected " + formatTime(Math.max(0L, payload.protectedUntil() - now));
            case "lootable" -> "Lootable " + formatTime(Math.max(0L, payload.decaysAt() - now));
            case "killer" -> "PvP loot";
            case "owner" -> "Owner recovery";
            default -> payload.error() ? "Unavailable" : "Grave";
        };
    }

    private ElarionCivicUi.Tone statusTone() {
        return switch (payload.accessState()) {
            case "protected" -> ElarionCivicUi.Tone.MUTED;
            case "lootable", "owner" -> ElarionCivicUi.Tone.PRIMARY;
            case "killer" -> ElarionCivicUi.Tone.DESTRUCTIVE;
            default -> payload.error() ? ElarionCivicUi.Tone.DESTRUCTIVE : ElarionCivicUi.Tone.NORMAL;
        };
    }

    private static String formatTime(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;
        return hours > 0L
                ? hours + "h " + minutes + "m"
                : minutes + ":" + (remainingSeconds < 10L ? "0" : "") + remainingSeconds;
    }

    private static boolean inside(double mx, double my, int x, int y, int width, int height) {
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }

    private record DisplayEntry(GraveOpenPayload.Entry entry, ItemStack stack) {
    }
}
