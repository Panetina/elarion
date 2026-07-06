package panetina.elarion.core.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionVirtualList;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.ElarionCollectionTab;
import panetina.elarion.core.network.CollectionActionPayload;

import java.util.List;

public final class ElarionCollectionScreen extends ElarionScreen {
    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 380;
    static final int ROW_HEIGHT = 36;
    static final int DETAIL_WIDTH = 172;
    private static final int BUTTON_HEIGHT = 22;
    private static final float MAX_SCALE = 0.94F;
    private static final int ACTIVE_BORDER = 0xFF62C987;
    private static final int ACTIVE_HIGHLIGHT = 0xFF9BE1B2;
    private static final int ACTIVE_SHADOW = 0xFF1F5A38;

    private ElarionCollectionSnapshot snapshot;
    private ElarionScaledLayout layout;
    private ElarionVirtualList list = new ElarionVirtualList(0, 1, 0);
    private String selectedTabId;
    private String selectedEntryId = "";

    public ElarionCollectionScreen(ElarionCollectionSnapshot snapshot) {
        super(Text.literal("Collection"));
        this.snapshot = snapshot;
        this.selectedTabId = snapshot.selectedTabId();
        if (this.selectedTabId.isBlank()) {
            this.selectedTabId = tabs().getFirst().id();
        }
        selectFirstEntryIfNeeded();
    }

    public void update(ElarionCollectionSnapshot snapshot) {
        this.snapshot = snapshot;
        this.selectedTabId = snapshot.selectedTabId().isBlank() ? selectedTabId : snapshot.selectedTabId();
        if (selectedTab() == null) {
            this.selectedTabId = tabs().getFirst().id();
        }
        selectFirstEntryIfNeeded();
    }

    @Override
    protected void init() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 62, MAX_SCALE);
        selectFirstEntryIfNeeded();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 62, MAX_SCALE);
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 48);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 - 108, 20, true);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 + 108, 20, false);
        context.drawCenteredTextWithShadow(textRenderer, snapshot.title(), PANEL_WIDTH / 2, 12, style.titleColor());
        context.drawCenteredTextWithShadow(textRenderer, snapshot.subtitle(), PANEL_WIDTH / 2, 29, style.mutedColor());

        renderTabs(context, lx, ly, style);
        renderEntries(context, lx, ly, style);
        renderDetails(context, lx, ly, mouseX, mouseY, delta, style);

        if (!snapshot.message().isBlank()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, snapshot.message(), PANEL_WIDTH - 24),
                    PANEL_WIDTH / 2, PANEL_HEIGHT - 14, style.feedbackColor());
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        ElarionScaledLayout current = currentLayout();
        double lx = current.logicalX(mouseX);
        double ly = current.logicalY(mouseY);
        Layout metrics = layoutMetrics();
        List<ElarionCollectionTab> tabs = tabs();
        for (int index = 0; index < tabs.size(); index++) {
            int x = metrics.tabX(index);
            if (inside(lx, ly, x, Layout.TAB_Y, Layout.TAB_WIDTH, Layout.TAB_HEIGHT)) {
                selectedTabId = tabs.get(index).id();
                selectedEntryId = "";
                selectFirstEntryIfNeeded();
                return true;
            }
        }

        ElarionCollectionTab tab = selectedTab();
        if (tab == null) return false;
        int visibleRows = metrics.visibleRows();
        list.update(tab.entries().size(), visibleRows, list.firstVisible());
        int clicked = metrics.itemAt(ly, list.firstVisible(), tab.entries().size());
        if (clicked >= 0 && inside(lx, ly, metrics.listX(), metrics.rowsY(), metrics.listWidth(), metrics.rowsHeight())) {
            selectedEntryId = tab.entries().get(clicked).id();
            list.select(clicked);
            return true;
        }

        ElarionCollectionEntry entry = selectedEntry();
        if (entry == null) return false;
        int buttonX = metrics.detailX() + 14;
        int buttonY = metrics.detailBottom() - 36;
        for (ElarionCollectionAction action : entry.actions()) {
            if (inside(lx, ly, buttonX, buttonY, DETAIL_WIDTH - 28, BUTTON_HEIGHT)) {
                if (action.enabled()) {
                    ClientPlayNetworking.send(new CollectionActionPayload(tab.id(), entry.id(), action.id()));
                }
                return true;
            }
            buttonY += BUTTON_HEIGHT + 6;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        ElarionCollectionTab tab = selectedTab();
        if (tab == null) return false;
        int visibleRows = layoutMetrics().visibleRows();
        list.update(tab.entries().size(), visibleRows, list.firstVisible());
        list.scroll(verticalAmount > 0.0D ? -1 : 1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ElarionCollectionTab tab = selectedTab();
        if (closesOnKey(keyCode)) {
            close();
            return true;
        }
        if (tab == null) return false;
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) {
            moveSelection(tab, 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) {
            moveSelection(tab, -1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            int visibleRows = layoutMetrics().visibleRows();
            list.page(1);
            moveSelection(tab, visibleRows);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            int visibleRows = layoutMetrics().visibleRows();
            list.page(-1);
            moveSelection(tab, -visibleRows);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderTabs(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        Layout metrics = layoutMetrics();
        List<ElarionCollectionTab> tabs = tabs();
        for (int index = 0; index < tabs.size(); index++) {
            ElarionCollectionTab tab = tabs.get(index);
            int x = metrics.tabX(index);
            boolean selected = tab.id().equals(selectedTabId);
            boolean hovered = inside(mouseX, mouseY, x, Layout.TAB_Y, Layout.TAB_WIDTH, Layout.TAB_HEIGHT);
            ElarionUiRenderer.compactButton(context, textRenderer, x, Layout.TAB_Y, Layout.TAB_WIDTH, Layout.TAB_HEIGHT,
                    tab.title(), hovered, selected, true, style);
        }
    }

    private void renderEntries(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        ElarionCollectionTab tab = selectedTab();
        Layout metrics = layoutMetrics();
        ElarionCivicUi.thinBox(context, metrics.contentHeaderX(), metrics.contentHeaderY(),
                metrics.contentHeaderWidth(), Layout.CONTENT_HEADER_HEIGHT,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        String title = tab == null ? "Nothing registered" : tab.subtitle();
        ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, title, metrics.contentHeaderWidth() - 18),
                metrics.contentHeaderX() + 9, metrics.contentHeaderY() + 6, style.mutedColor(), false);
        ElarionCivicUi.thinBox(context, metrics.listX(), metrics.panelTop(), metrics.listWidth(), metrics.panelHeight(),
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        if (tab == null || tab.entries().isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "No collection entries yet.",
                    metrics.listX() + metrics.listWidth() / 2, metrics.rowsY() + metrics.rowsHeight() / 2 - 4,
                    style.mutedColor());
            return;
        }

        int visibleRows = metrics.visibleRows();
        list.update(tab.entries().size(), visibleRows, list.firstVisible());
        int rowY = metrics.firstRowY();
        for (int index = list.firstVisible(); index < list.lastVisibleExclusive(); index++) {
            ElarionCollectionEntry entry = tab.entries().get(index);
            boolean selected = entry.id().equals(selectedEntryId);
            boolean hovered = inside(mouseX, mouseY, metrics.listX() + 8, rowY, metrics.listWidth() - 16, ROW_HEIGHT);
            int fill = !entry.unlocked() ? style.insetColor()
                    : entry.active() ? style.cardColor()
                    : selected ? style.headerColor()
                    : hovered ? style.buttonHoverColor() : style.cardColor();
            int rowX = metrics.listX() + 8;
            int rowWidth = metrics.listWidth() - 16;
            int rowHeight = ROW_HEIGHT;
            drawStateFrame(context, rowX, rowY, rowWidth, rowHeight, fill, entry.active(), selected, style);
            int iconSize = 28;
            int iconFrameSize = iconSize + 6;
            int iconFrameX = rowX + 8;
            int iconFrameY = rowY + (ROW_HEIGHT - iconFrameSize) / 2;
            drawIconFrame(context, iconFrameX, iconFrameY, iconFrameSize, iconFrameSize,
                    style.insetColor(), entry.active(), style);
            int iconX = iconFrameX + 3;
            int iconY = iconFrameY + 3;
            drawIconTexture(context, iconX, iconY, iconSize, entry.icon());
            if (!entry.unlocked()) {
                context.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xAA111111);
            }
            int textX = rowX + 52;
            int markerRight = rowX + rowWidth - 12;
            int nameColor = entry.unlocked() ? style.titleColor() : style.mutedColor();
            int subtitleColor = entry.unlocked() ? style.textColor() : style.mutedColor();
            ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, entry.title(), markerRight - textX - 8),
                    textX, rowY + 8, nameColor, false);
            ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, entry.subtitle(), markerRight - textX - 8),
                    textX, rowY + 22, subtitleColor, false);
            rowY += metrics.rowStride();
        }
    }

    private void renderDetails(
            DrawContext context,
            double mouseX,
            double mouseY,
            int screenMouseX,
            int screenMouseY,
            float delta,
            ElarionUiStyle style
    ) {
        Layout metrics = layoutMetrics();
        int x = metrics.detailX();
        int y = metrics.panelTop();
        ElarionCivicUi.headerShell(context, x, y, DETAIL_WIDTH, metrics.panelHeight(), 26);
        ElarionCollectionEntry entry = selectedEntry();
        if (entry == null) {
            context.drawCenteredTextWithShadow(textRenderer, "Select an entry", x + DETAIL_WIDTH / 2,
                    y + metrics.panelHeight() / 2 - 4, style.mutedColor());
            return;
        }
        context.drawCenteredTextWithShadow(textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, entry.title(), DETAIL_WIDTH - 22),
                x + DETAIL_WIDTH / 2, y + 18, entry.unlocked() ? style.titleColor() : style.mutedColor());
        int previewSize = 106;
        int previewX = x + (DETAIL_WIDTH - previewSize) / 2;
        int previewY = y + 30;
        ElarionCivicUi.thinBox(context, previewX, previewY, previewSize, previewSize,
                ElarionCivicColors.ROOT_SURFACE, entry.active()
                        ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_BORDER);
        ElarionCivicUi.messageBody(context, previewX + 7, previewY + 7, previewSize - 14, previewSize - 14,
                entry.active() ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW);
        renderPreview(context, entry, previewX, previewY, previewSize, screenMouseX, screenMouseY, delta, style);
        int dividerY = previewY + previewSize + 8;
        int buttonX = x + 14;
        int buttonY = metrics.detailBottom() - 36;
        int labelY = dividerY + 8;
        int bodyY = dividerY + 22;
        int bodyHeight = Math.max(20, buttonY - bodyY - 8);
        context.fill(x + 14, dividerY, x + DETAIL_WIDTH - 14, dividerY + 1,
                entry.active() ? style.feedbackColor() : style.borderColor());
        ElarionUiTypography.draw(context, textRenderer, entry.unlocked() ? "Unlocked" : "How to unlock", x + 12, labelY,
                entry.unlocked() ? style.feedbackColor() : style.titleColor(), false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(entry.body()),
                x + 12, bodyY, DETAIL_WIDTH - 24, bodyHeight,
                entry.unlocked() ? style.textColor() : style.mutedColor(), style.mutedColor());

        for (ElarionCollectionAction action : entry.actions()) {
            boolean hovered = inside(mouseX, mouseY, buttonX, buttonY, DETAIL_WIDTH - 28, BUTTON_HEIGHT);
            ElarionCivicUi.compactActionButton(context, textRenderer, buttonX, buttonY,
                    DETAIL_WIDTH - 28, BUTTON_HEIGHT, action.label(), hovered, false, action.enabled(),
                    action.enabled() ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.MUTED, style);
            buttonY += BUTTON_HEIGHT + 6;
        }
    }

    private void moveSelection(ElarionCollectionTab tab, int direction) {
        if (tab.entries().isEmpty()) return;
        int current = 0;
        for (int index = 0; index < tab.entries().size(); index++) {
            if (tab.entries().get(index).id().equals(selectedEntryId)) {
                current = index;
                break;
            }
        }
        int next = Math.max(0, Math.min(tab.entries().size() - 1, current + direction));
        selectedEntryId = tab.entries().get(next).id();
        list.select(next);
    }

    private void renderPreview(
            DrawContext context,
            ElarionCollectionEntry entry,
            int x,
            int y,
            int size,
            int mouseX,
            int mouseY,
            float delta,
            ElarionUiStyle style
    ) {
        if (!entry.unlocked()) {
            context.drawCenteredTextWithShadow(textRenderer, "Locked", x + size / 2, y + size / 2 - 4, style.mutedColor());
            return;
        }
        if (!ElarionCollectionPreviewRegistry.render(
                context, layout, selectedTabId, entry, x + 8, y + 8, size - 16, size - 20, mouseX, mouseY, delta, style)) {
            int iconSize = Math.min(54, size - 34);
            int iconX = x + (size - iconSize) / 2;
            int iconY = y + (size - iconSize) / 2;
            ElarionUiRenderer.icon(context, iconX, iconY, iconSize, entry.icon(), ElarionUiThemes.variant("default"));
        }
        context.fill(x + 18, y + size - 19, x + size - 18, y + size - 17, 0x554B3320);
    }

    private static void drawStateFrame(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int fill,
            boolean active,
            boolean selected,
            ElarionUiStyle style
    ) {
        int border = active ? ACTIVE_BORDER : selected ? style.titleColor() : style.borderColor();
        int highlight = active ? ACTIVE_HIGHLIGHT : style.bevelHighlightColor();
        int shadow = active ? ACTIVE_SHADOW : style.bevelShadowColor();
        drawBeveledBox(context, x, y, width, height, fill, border, highlight, shadow);
    }

    private static void drawBeveledBox(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int fill,
            int border,
            int highlight,
            int shadow
    ) {
        if (width <= 2 || height <= 2) {
            context.fill(x, y, x + Math.max(0, width), y + Math.max(0, height), fill);
            return;
        }
        context.fill(x + 1, y, x + width - 1, y + height, fill);
        context.fill(x, y + 1, x + width, y + height - 1, fill);
        context.fill(x + 2, y, x + width - 2, y + 1, highlight);
        context.fill(x, y + 2, x + 1, y + height - 2, highlight);
        context.fill(x, y, x + 1, y + 1, shadow);
        context.fill(x + width - 1, y, x + width, y + 1, shadow);
        context.fill(x, y + height - 1, x + 1, y + height, shadow);
        context.fill(x + width - 1, y + height - 1, x + width, y + height, shadow);
        context.fill(x + 1, y + 1, x + width - 1, y + 2, border);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, border);
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, border);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, border);
        context.fill(x + 2, y + height - 1, x + width - 2, y + height, shadow);
        context.fill(x + width - 1, y + 2, x + width, y + height - 2, shadow);
    }

    private static void drawIconTexture(DrawContext context, int x, int y, int size, String raw) {
        Identifier icon = Identifier.tryParse(raw);
        if (icon == null) return;
        int drawSize = Math.max(1, size - 6);
        int drawX = x + (size - drawSize) / 2;
        int drawY = y + (size - drawSize) / 2;
        context.drawTexture(icon, drawX, drawY, 0, 0, drawSize, drawSize, 16, 16);
    }

    private static void drawIconFrame(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            int fill,
            boolean active,
            ElarionUiStyle style
    ) {
        int border = active ? ACTIVE_BORDER : style.borderColor();
        int highlight = active ? ACTIVE_HIGHLIGHT : style.bevelHighlightColor();
        int shadow = active ? ACTIVE_SHADOW : style.bevelShadowColor();
        context.fill(x, y, x + width, y + height, fill);
        context.fill(x, y, x + width, y + 1, highlight);
        context.fill(x, y, x + 1, y + height, highlight);
        context.fill(x, y + height - 1, x + width, y + height, shadow);
        context.fill(x + width - 1, y, x + width, y + height, shadow);
        context.fill(x + 1, y + 1, x + width - 1, y + 2, border);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, border);
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, border);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, border);
    }

    static boolean closesOnKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_C;
    }

    private void selectFirstEntryIfNeeded() {
        ElarionCollectionTab tab = selectedTab();
        if (tab == null || tab.entries().isEmpty()) {
            selectedEntryId = "";
            return;
        }
        boolean exists = tab.entries().stream().anyMatch(entry -> entry.id().equals(selectedEntryId));
        if (!exists) {
            selectedEntryId = tab.entries().getFirst().id();
            list = new ElarionVirtualList(tab.entries().size(), 1, 0);
        }
    }

    private ElarionCollectionTab selectedTab() {
        return tabs().stream()
                .filter(tab -> tab.id().equals(selectedTabId))
                .findFirst()
                .orElse(null);
    }

    private ElarionCollectionEntry selectedEntry() {
        ElarionCollectionTab tab = selectedTab();
        if (tab == null) return null;
        return tab.entries().stream()
                .filter(entry -> entry.id().equals(selectedEntryId))
                .findFirst()
                .orElse(tab.entries().isEmpty() ? null : tab.entries().getFirst());
    }

    private ElarionScaledLayout currentLayout() {
        if (layout == null) {
            layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 62, MAX_SCALE);
        }
        return layout;
    }

    private List<ElarionCollectionTab> tabs() {
        return snapshot.tabs();
    }

    static Layout layoutMetrics() {
        return new Layout();
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    static final class Layout {
        static final int MARGIN = 12;
        static final int GAP = 8;
        static final int TAB_Y = 52;
        static final int TAB_WIDTH = 96;
        static final int TAB_HEIGHT = 22;
        static final int PANEL_TOP = 106;
        static final int PANEL_BOTTOM = 366;
        static final int CONTENT_HEADER_Y = 78;
        static final int CONTENT_HEADER_HEIGHT = 22;
        static final int VISIBLE_ROWS = 6;
        static final int ROW_GAP = 6;

        int tabX(int index) {
            return MARGIN + index * (TAB_WIDTH + 6);
        }

        int panelTop() {
            return PANEL_TOP;
        }

        int panelHeight() {
            return PANEL_BOTTOM - PANEL_TOP;
        }

        int detailX() {
            return PANEL_WIDTH - MARGIN - DETAIL_WIDTH;
        }

        int detailBottom() {
            return PANEL_BOTTOM;
        }

        int listX() {
            return MARGIN;
        }

        int listWidth() {
            return detailX() - GAP - listX();
        }

        int contentHeaderX() {
            return MARGIN;
        }

        int contentHeaderY() {
            return CONTENT_HEADER_Y;
        }

        int contentHeaderWidth() {
            return PANEL_WIDTH - MARGIN * 2;
        }

        int rowsY() {
            return PANEL_TOP;
        }

        int rowsHeight() {
            return PANEL_BOTTOM - rowsY();
        }

        int rowPadding() {
            return Math.max(6, (rowsHeight() - VISIBLE_ROWS * ROW_HEIGHT - (VISIBLE_ROWS - 1) * ROW_GAP) / 2);
        }

        int firstRowY() {
            return rowsY() + rowPadding();
        }

        int rowStride() {
            return ROW_HEIGHT + ROW_GAP;
        }

        int visibleRows() {
            return VISIBLE_ROWS;
        }

        int itemAt(double mouseY, int firstVisible, int itemCount) {
            if (mouseY < firstRowY()) return -1;
            int local = (int) (mouseY - firstRowY());
            int row = local / rowStride();
            if (row < 0 || row >= visibleRows()) return -1;
            if (local % rowStride() >= ROW_HEIGHT) return -1;
            int index = firstVisible + row;
            return index < itemCount ? index : -1;
        }
    }
}
