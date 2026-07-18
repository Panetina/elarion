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
import panetina.elarion.core.client.ui.ElarionItemSlotLayout;
import panetina.elarion.core.client.ui.ElarionListRangeMarker;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;

import java.util.ArrayList;
import java.util.List;

public final class GraveRecoveryScreen extends ElarionScreen {
    private static final int MAX_PANEL_WIDTH = 480;
    private static final int MAX_PANEL_HEIGHT = 342;
    private static final int MIN_PANEL_WIDTH = 350;
    private static final int MIN_PANEL_HEIGHT = 258;
    private static final int HEADER_HEIGHT = 48;
    private static final int FOOTER_HEIGHT = 42;
    private static final int STATUS_HEIGHT = 70;
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

        Layout layout = layout();
        int maxFirstRow = Math.max(0, (displayEntries.size() + layout.columns() - 1) / layout.columns()
                - layout.visibleRows());
        firstRow = Math.max(0, Math.min(firstRow, maxFirstRow));

        ElarionCivicUi.attachedShell(context, layout.x(), layout.y(), layout.panelWidth(), layout.panelHeight(),
                HEADER_HEIGHT);
        ElarionCivicUi.headerOrnament(context, layout.x() + layout.panelWidth() / 2 - 116, layout.y() + 24, true);
        ElarionCivicUi.headerOrnament(context, layout.x() + layout.panelWidth() / 2 + 116, layout.y() + 24, false);
        String title = ElarionUiTypography.ellipsize(textRenderer, payload.title(), layout.panelWidth() - 150);
        ElarionUiTypography.drawCentered(context, textRenderer, title, width / 2,
                layout.y() + ElarionCivicUi.centeredTextY(textRenderer, 0, HEADER_HEIGHT) - 1,
                style.titleColor(), false);

        renderStatus(context, style, layout.innerX(), layout.statusY(), layout.innerWidth());
        renderGrid(context, style, layout, mouseX, mouseY);
        renderFooter(context, style, layout, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout layout = layout();
        if (!inside(mouseX, mouseY, layout.gridFrameX(), layout.gridFrameY(),
                layout.gridFrameWidth(), layout.gridFrameHeight())) {
            return false;
        }
        int maxFirstRow = Math.max(0, (displayEntries.size() + layout.columns() - 1) / layout.columns()
                - layout.visibleRows());
        int direction = verticalAmount > 0.0D ? -1 : verticalAmount < 0.0D ? 1 : 0;
        if (direction == 0 || maxFirstRow <= 0) return false;
        firstRow = Math.max(0, Math.min(maxFirstRow, firstRow + direction));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Layout layout = layout();
        if (button == 0 && recoverEnabled()
                && inside(mouseX, mouseY, layout.recoverX(), layout.buttonY(), layout.recoverWidth(), 20)) {
            ClientPlayNetworking.send(new GraveRecoverPayload(payload.corpseId()));
            return true;
        }
        if (button == 0 && inside(mouseX, mouseY, layout.closeX(), layout.buttonY(), layout.closeWidth(), 20)) {
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderStatus(DrawContext context, ElarionUiStyle style, int x, int y, int width) {
        ElarionCivicUi.thinBox(context, x, y, width, STATUS_HEIGHT,
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionCivicUi.messageBody(context, x + 2, y + 2, width - 4, STATUS_HEIGHT - 4,
                payload.error() ? ElarionCivicColors.REJECT_RED : ElarionCivicColors.ACTIVE_GREEN);
        int pad = 10;
        int chipWidth = Math.min(168, width - pad * 2);
        ElarionCivicUi.statusChip(context, textRenderer, x + pad, y + 9, statusText(), chipWidth,
                statusTone(), style);

        String count = payload.totalItemCount() + (payload.totalItemCount() == 1 ? " item" : " items");
        ElarionUiTypography.drawRight(context, textRenderer, count, x + width - pad, y + 10,
                payload.error() ? style.errorColor() : style.mutedColor(), false);

        String owner = payload.ownerName().isBlank() ? "" : "Belongs to " + payload.ownerName();
        if (!owner.isBlank()) {
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, owner, width - pad * 2),
                    x + pad, y + 28, style.mutedColor(), false);
        }

        String body = payload.body();
        if (!body.isBlank()) {
            ElarionUiTypography.wrappedClipped(context, textRenderer, Text.literal(body),
                    x + pad, y + 44, width - pad * 2, 20,
                    payload.error() ? style.errorColor() : style.textColor(), style.mutedColor());
        }
    }

    private void renderGrid(
            DrawContext context,
            ElarionUiStyle style,
            Layout layout,
            int mouseX,
            int mouseY
    ) {
        ElarionCivicUi.thinBox(context, layout.gridFrameX(), layout.gridFrameY(),
                layout.gridFrameWidth(), layout.gridFrameHeight(),
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionCivicUi.divider(context, layout.gridFrameX() + 10, layout.gridFrameY() + 20,
                layout.gridFrameWidth() - 20);
        ElarionUiTypography.draw(context, textRenderer, "Grave Contents",
                layout.gridFrameX() + 12, layout.gridFrameY() + 8, style.titleColor(), false);

        int start = firstRow * layout.columns();
        int end = Math.min(displayEntries.size(), start + layout.columns() * layout.visibleRows());
        if (displayEntries.isEmpty()) {
            String empty = decoded ? "No visible items." : "Loading grave items...";
            ElarionUiTypography.drawCentered(context, textRenderer, empty, layout.gridFrameX() + layout.gridFrameWidth() / 2,
                    layout.slotY() + 8, style.mutedColor(), false);
            return;
        }
        for (int index = start; index < end; index++) {
            int local = index - start;
            ElarionItemSlotLayout.Slot slot = itemSlot(layout, local);
            boolean hovered = slot.item().contains(mouseX, mouseY);
            ElarionCivicUi.thinBox(context, slot.bounds().x(), slot.bounds().y(),
                    slot.bounds().width(), slot.bounds().height(),
                    hovered ? ElarionCivicColors.CARD_HOVER : ElarionCivicColors.MESSAGE_BODY,
                    ElarionCivicColors.GOLD_BORDER);
            ItemStack stack = displayEntries.get(index).stack();
            context.drawItem(stack, slot.itemDrawX(), slot.itemDrawY());
            context.drawItemInSlot(textRenderer, stack, slot.itemDrawX(), slot.itemDrawY());
            if (hovered) {
                context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
            }
        }
        int totalRows = (displayEntries.size() + layout.columns() - 1) / layout.columns();
        if (totalRows > layout.visibleRows()) {
            ElarionListRangeMarker.draw(context, textRenderer,
                    layout.gridFrameX() + layout.gridFrameWidth() / 2, layout.gridFrameY() + 8,
                    ElarionListRangeMarker.range(firstRow, layout.visibleRows(), totalRows), style.mutedColor());
        }
    }

    private void renderFooter(
            DrawContext context,
            ElarionUiStyle style,
            Layout layout,
            int mouseX,
            int mouseY
    ) {
        ElarionCivicUi.divider(context, layout.x() + 12, layout.buttonY() - 10, layout.panelWidth() - 24);
        ElarionCivicUi.compactActionButton(context, textRenderer, layout.recoverX(), layout.buttonY(),
                layout.recoverWidth(), 20,
                "Recover All", inside(mouseX, mouseY, layout.recoverX(), layout.buttonY(), layout.recoverWidth(), 20),
                false,
                recoverEnabled(), recoverEnabled() ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.MUTED,
                style);
        ElarionCivicUi.compactActionButton(context, textRenderer, layout.closeX(), layout.buttonY(),
                layout.closeWidth(), 20,
                "Close", inside(mouseX, mouseY, layout.closeX(), layout.buttonY(), layout.closeWidth(), 20),
                false, true,
                ElarionCivicUi.Tone.NORMAL, style);
    }

    private Layout layout() {
        return calculateLayout(width, height);
    }

    static Layout calculateLayout(int screenWidth, int screenHeight) {
        int panelWidth = Math.max(MIN_PANEL_WIDTH, Math.min(MAX_PANEL_WIDTH, screenWidth - 32));
        int panelHeight = Math.max(MIN_PANEL_HEIGHT, Math.min(MAX_PANEL_HEIGHT, screenHeight - 32));
        int x = (screenWidth - panelWidth) / 2;
        int y = (screenHeight - panelHeight) / 2;
        int innerX = x + 16;
        int innerWidth = panelWidth - 32;
        int statusY = y + HEADER_HEIGHT + 10;
        int gridFrameX = innerX;
        int gridFrameY = statusY + STATUS_HEIGHT + 10;
        int gridFrameWidth = innerWidth;
        int gridFrameBottom = y + panelHeight - FOOTER_HEIGHT - 10;
        int gridFrameHeight = Math.max(SLOT_SIZE + 34, gridFrameBottom - gridFrameY);
        int slotX = gridFrameX + 10;
        int slotY = gridFrameY + 28;
        int slotAreaWidth = Math.max(SLOT_SIZE, gridFrameWidth - 20);
        int slotAreaHeight = Math.max(SLOT_SIZE, gridFrameHeight - 36);
        int columns = Math.max(1, Math.min(10, (slotAreaWidth + SLOT_GAP) / (SLOT_SIZE + SLOT_GAP)));
        int visibleRows = Math.max(1, slotAreaHeight / (SLOT_SIZE + SLOT_GAP));
        int recoverWidth = 134;
        int closeWidth = 96;
        int gap = 14;
        int totalButtonWidth = recoverWidth + closeWidth + gap;
        int recoverX = x + (panelWidth - totalButtonWidth) / 2;
        int closeX = recoverX + recoverWidth + gap;
        int buttonY = y + panelHeight - 30;
        return new Layout(x, y, panelWidth, panelHeight, innerX, innerWidth,
                statusY, gridFrameX, gridFrameY, gridFrameWidth, gridFrameHeight,
                slotX, slotY, columns, visibleRows, recoverX, closeX, buttonY,
                recoverWidth, closeWidth);
    }

    static ElarionItemSlotLayout.Slot itemSlot(Layout layout, int visibleIndex) {
        return ElarionItemSlotLayout.gridSlot(layout.slotX(), layout.slotY(), visibleIndex,
                layout.columns(), SLOT_SIZE, SLOT_GAP, 5);
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

    record Layout(
            int x,
            int y,
            int panelWidth,
            int panelHeight,
            int innerX,
            int innerWidth,
            int statusY,
            int gridFrameX,
            int gridFrameY,
            int gridFrameWidth,
            int gridFrameHeight,
            int slotX,
            int slotY,
            int columns,
            int visibleRows,
            int recoverX,
            int closeX,
            int buttonY,
            int recoverWidth,
            int closeWidth
    ) {
    }
}
