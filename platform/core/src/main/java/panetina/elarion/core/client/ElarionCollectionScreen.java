package panetina.elarion.core.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionPixelCanvas32;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionVirtualList;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.ElarionCollectionTab;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.network.CitizenProfileRequestPayload;
import panetina.elarion.core.network.CollectionActionPayload;
import panetina.elarion.core.service.ElarionCollectionService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class ElarionCollectionScreen extends ElarionScreen {
    private static final int PANEL_WIDTH = 760;
    private static final int PANEL_HEIGHT = 460;
    static final int ROW_HEIGHT = 44;
    static final int DETAIL_WIDTH = 350;
    private static final int BUTTON_HEIGHT = 22;
    private static final float MAX_SCALE = 0.94F;
    private static final int ACTIVE_BORDER = 0xFF62C987;
    private static final int ACTIVE_HIGHLIGHT = 0xFF9BE1B2;
    private static final int ACTIVE_SHADOW = 0xFF1F5A38;
    private static final DateTimeFormatter PROFILE_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy")
            .withZone(ZoneOffset.UTC);

    private ElarionCollectionSnapshot snapshot;
    private ElarionScaledLayout layout;
    private ElarionVirtualList list = new ElarionVirtualList(0, 1, 0);
    private String selectedTabId;
    private String selectedEntryId = "";
    private boolean profileRequested;

    public ElarionCollectionScreen(ElarionCollectionSnapshot snapshot) {
        this(snapshot, false);
    }

    public ElarionCollectionScreen(ElarionCollectionSnapshot snapshot, boolean profileProvided) {
        super(Text.literal(ElarionCollectionService.SHELL_TITLE));
        this.snapshot = snapshot;
        this.profileRequested = profileProvided;
        this.selectedTabId = snapshot.selectedTabId();
        if (this.selectedTabId.isBlank()) {
            this.selectedTabId = tabs().getFirst().id();
        }
        selectFirstEntryIfNeeded();
        requestProfileIfSelected();
    }

    public void update(ElarionCollectionSnapshot snapshot) {
        this.snapshot = snapshot;
        this.selectedTabId = snapshot.selectedTabId().isBlank() ? selectedTabId : snapshot.selectedTabId();
        if (selectedTab() == null) {
            this.selectedTabId = tabs().getFirst().id();
        }
        selectFirstEntryIfNeeded();
        requestProfileIfSelected();
    }

    @Override
    protected void init() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 62, MAX_SCALE);
        selectFirstEntryIfNeeded();
        requestProfileIfSelected();
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
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 - 122, 20, true);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 + 122, 20, false);
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
            int x = metrics.tabX(index, tabs.size());
            if (inside(lx, ly, x, Layout.TAB_Y, metrics.tabWidth(tabs.size()), Layout.TAB_HEIGHT)) {
                selectedTabId = tabs.get(index).id();
                selectedEntryId = "";
                if (profileSelected()) {
                    requestProfileIfSelected();
                } else {
                    selectFirstEntryIfNeeded();
                }
                return true;
            }
        }

        if (profileSelected()) {
            return inside(lx, ly, metrics.listX(), metrics.panelTop(),
                    metrics.contentHeaderWidth(), metrics.panelHeight());
        }

        if (reputationSelected()) {
            ElarionCollectionTab tab = selectedTab();
            if (tab == null) return false;
            list.update(tab.entries().size(), layoutMetrics().visibleRows(), list.firstVisible());
            int clicked = layoutMetrics().itemAt(ly, list.firstVisible(), tab.entries().size());
            if (clicked >= 0) {
                selectedEntryId = tab.entries().get(clicked).id();
                list.select(clicked);
                return true;
            }
            return false;
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
        if (profileSelected()) {
            return false;
        }
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
        if (profileSelected()) return super.keyPressed(keyCode, scanCode, modifiers);
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
            int x = metrics.tabX(index, tabs.size());
            boolean selected = tab.id().equals(selectedTabId);
            int tabWidth = metrics.tabWidth(tabs.size());
            boolean hovered = inside(mouseX, mouseY, x, Layout.TAB_Y, tabWidth, Layout.TAB_HEIGHT);
            ElarionUiRenderer.compactButton(context, textRenderer, x, Layout.TAB_Y, tabWidth, Layout.TAB_HEIGHT,
                    "", hovered, selected, true, style);
            drawCollectionTabLabel(context, tab, x, tabWidth, selected, style);
        }
    }

    private void renderEntries(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        ElarionCollectionTab tab = selectedTab();
        Layout metrics = layoutMetrics();
        if (profileSelected()) {
            renderProfileSections(context, mouseX, mouseY, style);
            return;
        }
        if (reputationSelected()) {
            renderReputationEntries(context, mouseX, mouseY, style);
            return;
        }
        ElarionCivicUi.thinBox(context, metrics.contentHeaderX(), metrics.contentHeaderY(),
                metrics.contentHeaderWidth(), Layout.CONTENT_HEADER_HEIGHT,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        if (tab == null) {
            ElarionUiTypography.draw(context, textRenderer, "NOTHING REGISTERED",
                    metrics.contentHeaderX() + 9, metrics.contentHeaderY() + 6, style.mutedColor(), false);
        } else {
            long unlocked = tab.entries().stream().filter(ElarionCollectionEntry::unlocked).count();
            String heading = tab.title().toUpperCase(Locale.ROOT) + " COLLECTION";
            String completion = unlocked + " / " + tab.entries().size() + " UNLOCKED";
            ElarionUiTypography.draw(context, textRenderer, heading,
                    metrics.contentHeaderX() + 9, metrics.contentHeaderY() + 6, style.titleColor(), false);
            ElarionUiTypography.drawRight(context, textRenderer, completion,
                    metrics.contentHeaderX() + metrics.contentHeaderWidth() - 9,
                    metrics.contentHeaderY() + 6, unlocked > 0 ? ACTIVE_BORDER : style.mutedColor(), false);
        }
        ElarionCivicUi.thinBox(context, metrics.listX(), metrics.panelTop(), metrics.listWidth(), metrics.panelHeight(),
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        if (tab == null || tab.entries().isEmpty()) {
            renderEmptyCollection(context, tab, metrics.listX(), metrics.panelTop(), metrics.listWidth(),
                    metrics.panelHeight(), style);
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
            int accent = entryAccent(entry);
            drawStateFrame(context, rowX, rowY, rowWidth, rowHeight, fill,
                    entry.active(), selected, accent, style);
            int iconSize = 30;
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
            int textX = rowX + 54;
            int markerRight = rowX + rowWidth - 12;
            String state = entry.active() ? "ACTIVE" : entry.unlocked() ? "OWNED" : "LOCKED";
            int stateColor = entry.active() ? ACTIVE_BORDER : entry.unlocked() ? style.textColor() : style.mutedColor();
            int stateWidth = ElarionUiTypography.width(textRenderer, state);
            String rank = entry.rankLabel().toUpperCase(Locale.ROOT);
            int rankColor = entryRankColor(entry);
            int rankWidth = rank.isBlank() ? 0 : ElarionUiTypography.width(textRenderer, rank) + 12;
            int nameColor = entry.unlocked() && "titles".equals(selectedTabId)
                    ? accent : entry.unlocked() ? style.titleColor() : style.mutedColor();
            int subtitleColor = entry.unlocked() ? style.textColor() : style.mutedColor();
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, entry.title(),
                            markerRight - textX - stateWidth - rankWidth - 20),
                    textX, rowY + 9, nameColor, false);
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, entry.subtitle(), markerRight - textX - 8),
                    textX, rowY + 26, subtitleColor, false);
            if (!rank.isBlank()) {
                drawRankBadge(context, textRenderer, markerRight - stateWidth - rankWidth - 10, rowY + 7,
                        rankWidth, rank, entry.unlocked() ? rankColor : style.mutedColor());
            }
            ElarionUiTypography.drawRight(context, textRenderer, state, markerRight, rowY + 9, stateColor, false);
            rowY += metrics.rowStride();
        }
    }

    private void renderReputationEntries(
            DrawContext context,
            double mouseX,
            double mouseY,
            ElarionUiStyle style
    ) {
        Layout metrics = layoutMetrics();
        ElarionCollectionTab tab = selectedTab();
        int x = metrics.contentHeaderX();
        int width = metrics.contentHeaderWidth();
        ElarionCivicUi.thinBox(context, x, metrics.contentHeaderY(), width, Layout.CONTENT_HEADER_HEIGHT,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionUiTypography.draw(context, textRenderer, "FACTION REPUTATION",
                x + 9, metrics.contentHeaderY() + 6, style.titleColor(), false);
        int factionCount = tab == null ? 0 : tab.entries().size();
        ElarionUiTypography.drawRight(context, textRenderer,
                factionCount + (factionCount == 1 ? " FACTION" : " FACTIONS"),
                x + width - 9, metrics.contentHeaderY() + 6, style.mutedColor(), false);

        ElarionCivicUi.thinBox(context, x, metrics.panelTop(), width, metrics.panelHeight(),
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        if (tab == null || tab.entries().isEmpty()) {
            renderEmptyCollection(context, tab, x, metrics.panelTop(), width, metrics.panelHeight(), style);
            return;
        }

        list.update(tab.entries().size(), metrics.visibleRows(), list.firstVisible());
        int rowY = metrics.firstRowY();
        for (int index = list.firstVisible(); index < list.lastVisibleExclusive(); index++) {
            ElarionCollectionEntry entry = tab.entries().get(index);
            boolean selected = entry.id().equals(selectedEntryId);
            boolean hovered = inside(mouseX, mouseY, x + 8, rowY, width - 16, ROW_HEIGHT);
            int rowX = x + 8;
            int rowWidth = width - 16;
            int accent = entryRankColor(entry);
            drawStateFrame(context, rowX, rowY, rowWidth, ROW_HEIGHT,
                    selected ? style.headerColor() : hovered ? style.buttonHoverColor() : style.cardColor(),
                    false, selected, accent, style);

            int iconSize = 28;
            int iconX = rowX + 9;
            int iconY = rowY + (ROW_HEIGHT - iconSize) / 2;
            drawIconTexture(context, iconX, iconY, iconSize, entry.icon());

            int nameX = rowX + 48;
            int right = rowX + rowWidth - 12;
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, entry.title(), 190),
                    nameX, rowY + 8, style.titleColor(), false);
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, entry.subtitle(), 190),
                    nameX, rowY + 25, style.mutedColor(), false);

            ReputationProgress progress = reputationProgress(entry.state());
            String standing = entry.rankLabel().toUpperCase(Locale.ROOT);
            ElarionUiTypography.drawRight(context, textRenderer, standing, right, rowY + 7, accent, false);
            String amount = progress.current() + "/" + progress.maximum();
            ElarionUiTypography.drawRight(context, textRenderer, amount, right, rowY + 25,
                    style.textColor(), false);

            int barX = rowX + 244;
            int amountWidth = ElarionUiTypography.width(textRenderer, amount);
            int barRight = right - amountWidth - 12;
            int barY = rowY + 28;
            int barWidth = Math.max(40, barRight - barX);
            context.fill(barX, barY, barX + barWidth, barY + 7, style.insetColor());
            context.fill(barX, barY, barX + barWidth, barY + 1, ElarionCivicColors.GOLD_SHADOW);
            int filled = (int) Math.round(barWidth * progress.ratio());
            if (filled > 0) context.fill(barX + 1, barY + 1, barX + Math.max(1, filled), barY + 6, accent);
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
        if (profileSelected()) {
            renderProfileDetails(context, style);
            return;
        }
        if (reputationSelected()) return;
        int x = metrics.detailX();
        int y = metrics.panelTop();
        ElarionCivicUi.headerShell(context, x, y, DETAIL_WIDTH, metrics.panelHeight(), 34);
        ElarionCollectionEntry entry = selectedEntry();
        if (entry == null) {
            ElarionCollectionTab tab = selectedTab();
            renderEmptyCollection(context, tab, x, y, DETAIL_WIDTH, metrics.panelHeight(), style);
            return;
        }
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, entry.title(), DETAIL_WIDTH - 126),
                x + 16, y + 13, entry.unlocked() ? entryAccent(entry) : style.mutedColor(), false);
        String detailState = entry.active() ? "ACTIVE" : entry.unlocked() ? "OWNED" : "LOCKED";
        ElarionUiTypography.drawRight(context, textRenderer, detailState, x + DETAIL_WIDTH - 16, y + 13,
                entry.active() ? ACTIVE_BORDER : entry.unlocked() ? style.textColor() : style.mutedColor(), false);
        String rank = entry.rankLabel().toUpperCase(Locale.ROOT);
        if (!rank.isBlank()) {
            int rankWidth = ElarionUiTypography.width(textRenderer, rank) + 12;
            drawRankBadge(context, textRenderer, x + 16, y + 26, rankWidth, rank,
                    entry.unlocked() ? entryRankColor(entry) : style.mutedColor());
        }
        int previewX = x + 16;
        int previewY = y + 38;
        int previewWidth = DETAIL_WIDTH - 32;
        int previewHeight = 184;
        ElarionCivicUi.thinBox(context, previewX, previewY, previewWidth, previewHeight,
                ElarionCivicColors.ROOT_SURFACE, entry.active() || entry.accentColor() != 0
                        ? entryAccent(entry) : ElarionCivicColors.GOLD_BORDER);
        ElarionCivicUi.messageBody(context, previewX + 7, previewY + 7, previewWidth - 14, previewHeight - 14,
                entry.active() ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW);
        renderPreview(context, entry, previewX, previewY, previewWidth, previewHeight,
                screenMouseX, screenMouseY, delta, style);
        int dividerY = previewY + previewHeight + 8;
        int buttonX = x + 14;
        int buttonY = metrics.detailBottom() - 36;
        int labelY = dividerY + 7;
        int bodyY = dividerY + 21;
        int bodyHeight = Math.max(20, buttonY - bodyY - 8);
        context.fill(x + 14, dividerY, x + DETAIL_WIDTH - 14, dividerY + 1,
                entry.active() ? style.feedbackColor() : style.borderColor());
        ElarionUiTypography.draw(context, textRenderer, entry.unlocked() ? "COLLECTION RECORD" : "HOW TO UNLOCK", x + 16, labelY,
                entry.unlocked() ? style.feedbackColor() : style.titleColor(), false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(entry.body()),
                x + 16, bodyY, DETAIL_WIDTH - 32, bodyHeight,
                entry.unlocked() ? style.textColor() : style.mutedColor(), style.mutedColor());

        for (ElarionCollectionAction action : entry.actions()) {
            boolean hovered = inside(mouseX, mouseY, buttonX, buttonY, DETAIL_WIDTH - 28, BUTTON_HEIGHT);
            ElarionCivicUi.compactActionButton(context, textRenderer, buttonX, buttonY,
                    DETAIL_WIDTH - 28, BUTTON_HEIGHT, action.label(), hovered, false, action.enabled(),
                    action.enabled() ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.MUTED, style);
            buttonY += BUTTON_HEIGHT + 6;
        }
    }

    private void renderProfileSections(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        requestProfileIfSelected();
        Layout metrics = layoutMetrics();
        ElarionCivicUi.thinBox(context, metrics.contentHeaderX(), metrics.contentHeaderY(),
                metrics.contentHeaderWidth(), Layout.CONTENT_HEADER_HEIGHT,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionUiTypography.draw(context, textRenderer, "EMBER DOSSIER",
                metrics.contentHeaderX() + 9, metrics.contentHeaderY() + 6, style.mutedColor(), false);

        int sheetX = metrics.listX();
        int sheetY = metrics.panelTop();
        int sheetWidth = metrics.contentHeaderWidth();
        int sheetHeight = metrics.panelHeight();
        ElarionCivicUi.thinBox(context, sheetX, sheetY, sheetWidth, sheetHeight,
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        CitizenProfileSnapshot profile = CitizenProfileClientState.latest().orElse(null);
        if (profile == null) {
            context.drawCenteredTextWithShadow(textRenderer, "Loading profile...",
                    sheetX + sheetWidth / 2, sheetY + sheetHeight / 2 - 4,
                    style.mutedColor());
            return;
        }
        if (profile.sections().isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "No profile details visible.",
                    sheetX + sheetWidth / 2, sheetY + sheetHeight / 2 - 4,
                    style.mutedColor());
            return;
        }

        CitizenProfileSection identity = section(profile, "core.identity");
        CitizenProfileSection realm = section(profile, "core.realm");
        CitizenProfileSection title = section(profile, "core.title");
        String citizenName = profile.title().isBlank()
                ? value(identity, "display-name", "Ember")
                : profile.title();
        String civicRole = contributedField(profile, "government", "active-office",
                value(identity, "civic-standing", "Ember"));
        String activeTitle = value(title, "active-title", "None");
        String titleSubtitle = titleSubtitle(activeTitle, civicRole);

        int innerX = sheetX + 12;
        int innerY = sheetY + 10;
        int innerWidth = sheetWidth - 24;
        ElarionCivicUi.thinBox(context, innerX, innerY, innerWidth, 76,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        drawBeveledBox(context, innerX + 10, innerY + 10, 56, 56,
                style.insetColor(), style.borderColor(), style.bevelHighlightColor(), style.bevelShadowColor());
        drawPlayerHead(context, profile.targetId(), innerX + 13, innerY + 13, 50, style);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, citizenName, 190),
                innerX + 78, innerY + 13, style.titleColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                "@" + value(identity, "username", "hidden"),
                innerX + 78, innerY + 30, style.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                compactDateLine("Joined", value(identity, "joined-at", "")),
                innerX + 78, innerY + 49, style.textColor(), false);

        int realmX = innerX + 302;
        context.fill(realmX - 14, innerY + 11, realmX - 13, innerY + 65, ElarionCivicColors.DIVIDER);
        ElarionUiTypography.draw(context, textRenderer, "REALM", realmX, innerY + 13, style.mutedColor(), false);
        var heraldry = ElarionHeraldryClientRegistry.realm(value(realm, "realm-id", ""));
        if (!heraldry.empty()) {
            ElarionPixelCanvas32.renderPreview(context, realmX + 164, innerY + 19, 1, heraldry.paletteIndices());
        }
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, value(realm, "realm-name", "Unassigned"), 155),
                realmX, innerY + 31, ACTIVE_BORDER, false);
        ElarionUiTypography.draw(context, textRenderer,
                value(realm, "realm-prefix", "No realm crest"), realmX, innerY + 49, style.textColor(), false);

        int titleX = innerX + 510;
        context.fill(titleX - 14, innerY + 11, titleX - 13, innerY + 65, ElarionCivicColors.DIVIDER);
        ElarionUiTypography.draw(context, textRenderer, "ACTIVE TITLE", titleX, innerY + 13, style.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, activeTitle, 150),
                titleX, innerY + 31, style.titleColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, titleSubtitle, 150),
                titleX, innerY + 49, style.textColor(), false);

        int bodyY = innerY + 88;
        int columnGap = 8;
        int columnWidth = (innerWidth - columnGap * 2) / 3;
        int middleX = innerX + columnWidth + columnGap;
        int rightX = middleX + columnWidth + columnGap;

        drawProfilePanel(context, innerX, bodyY, columnWidth, 104, "EMBER STANDING", style);
        drawProfileFact(context, innerX + 10, bodyY + 25, columnWidth - 20,
                "Citizenship", value(identity, "citizenship", "Unknown"), style, ACTIVE_BORDER);
        drawProfileFact(context, innerX + 10, bodyY + 49, columnWidth - 20,
                "Realm membership", value(realm, "realm-name", "Unassigned"), style);
        drawProfileFact(context, innerX + 10, bodyY + 73, columnWidth - 20,
                "Civic role", civicRole, style);

        drawProfilePanel(context, middleX, bodyY, columnWidth, 104, "PROGRESSION", style);
        drawProfileFact(context, middleX + 10, bodyY + 25, columnWidth - 20,
                "Offering score", contributedField(profile, CitizenProfileSummaryFields.SOURCE_OFFERINGS,
                        CitizenProfileSummaryFields.FIELD_OFFERING_SCORE, "Not recorded"),
                style, ElarionCivicColors.QUEST_PURPLE);
        drawProfileFact(context, middleX + 10, bodyY + 49, columnWidth - 20,
                "Quests completed", contributedField(profile, CitizenProfileSummaryFields.SOURCE_QUESTS,
                        CitizenProfileSummaryFields.FIELD_QUESTS_COMPLETED, "Not recorded"), style);
        drawProfileFact(context, middleX + 10, bodyY + 73, columnWidth - 20,
                "Milestones", contributedField(profile, CitizenProfileSummaryFields.SOURCE_PROGRESSION,
                        CitizenProfileSummaryFields.FIELD_MILESTONES, "0"), style);

        drawProfilePanel(context, rightX, bodyY, columnWidth, 104, "COLLECTION", style);
        drawProfileFact(context, rightX + 10, bodyY + 25, columnWidth - 20,
                "Active title", activeTitle, style, style.titleColor());
        drawProfileFact(context, rightX + 10, bodyY + 49, columnWidth - 20,
                "Mounts owned", contributedField(profile, CitizenProfileSummaryFields.SOURCE_MOUNTS,
                        CitizenProfileSummaryFields.FIELD_MOUNTS_UNLOCKED, "0"), style, ACTIVE_BORDER);
        drawProfileFact(context, rightX + 10, bodyY + 73, columnWidth - 20,
                "Advancements", contributedField(profile, CitizenProfileSummaryFields.SOURCE_PROGRESSION,
                        CitizenProfileSummaryFields.FIELD_ADVANCEMENTS_COMPLETED, "0"), style);

        int lowerY = bodyY + 112;
        drawProfilePanel(context, innerX, lowerY, columnWidth, 100, "AFFILIATIONS", style);
        drawProfileEmptyLine(context, innerX + 10, lowerY + 28, columnWidth - 20,
                "Guilds", contributedField(profile, CitizenProfileSummaryFields.SOURCE_GUILDS,
                        CitizenProfileSummaryFields.FIELD_MEMBERSHIPS, "No records contributed"), style);
        drawProfileEmptyLine(context, innerX + 10, lowerY + 57, columnWidth - 20,
                "Government offices", contributedField(profile, CitizenProfileSummaryFields.SOURCE_GOVERNMENT,
                        CitizenProfileSummaryFields.FIELD_OFFICE_HISTORY, "No office record contributed"), style);

        drawProfilePanel(context, middleX, lowerY, columnWidth, 100, "LIFETIME RECORD", style);
        drawProfileFact(context, middleX + 10, lowerY + 25, columnWidth - 20,
                "Deaths", contributedField(profile, CitizenProfileSummaryFields.SOURCE_UNDERWORLD,
                        CitizenProfileSummaryFields.FIELD_DEATHS, "Not recorded"), style);
        drawProfileFact(context, middleX + 10, lowerY + 49, columnWidth - 20,
                "Portal journeys", contributedField(profile, CitizenProfileSummaryFields.SOURCE_PORTALS,
                        CitizenProfileSummaryFields.FIELD_PORTAL_JOURNEYS, "Not recorded"), style);
        drawProfileFact(context, middleX + 10, lowerY + 73, columnWidth - 20,
                "Milestones", contributedField(profile, CitizenProfileSummaryFields.SOURCE_PROGRESSION,
                        CitizenProfileSummaryFields.FIELD_MILESTONES, "Not recorded"), style);

        drawProfilePanel(context, rightX, lowerY, columnWidth, 100, "CHRONICLE", style);
        drawProfileFact(context, rightX + 10, lowerY + 25, columnWidth - 20,
                "First joined", compactDate(value(identity, "joined-at", "")), style);
        drawProfileFact(context, rightX + 10, lowerY + 49, columnWidth - 20,
                "Last seen", compactDate(value(identity, "last-seen-at", "")), style, ACTIVE_BORDER);
        drawProfileEmptyLine(context, rightX + 10, lowerY + 73, columnWidth - 20,
                "Recent history", contributedField(profile, CitizenProfileSummaryFields.SOURCE_HISTORY,
                        CitizenProfileSummaryFields.FIELD_RECENT_SUMMARY, "Chronicle summary coming later"), style);
    }

    private static String titleSubtitle(String activeTitle, String civicRole) {
        if (activeTitle == null || activeTitle.isBlank() || "None".equalsIgnoreCase(activeTitle)) {
            return civicRole == null || civicRole.isBlank() ? "No active title" : civicRole;
        }
        if (civicRole == null || civicRole.isBlank()) {
            return "Display title";
        }
        if (activeTitle.equalsIgnoreCase(civicRole)) {
            return "Civic office";
        }
        return civicRole;
    }

    private void renderProfileDetails(DrawContext context, ElarionUiStyle style) {
        // Profile renders as one composed sheet in renderProfileSections.
    }

    private void requestProfileIfSelected() {
        if (!profileSelected() || profileRequested || client == null || client.player == null) return;
        profileRequested = true;
        ClientPlayNetworking.send(new CitizenProfileRequestPayload(new UUID(0L, 0L), ""));
    }

    private boolean profileSelected() {
        return ElarionCollectionService.PROFILE_TAB_ID.equals(selectedTabId);
    }

    private boolean reputationSelected() {
        return "reputation".equals(selectedTabId);
    }

    static ReputationProgress reputationProgress(String value) {
        if (value == null) return new ReputationProgress(0, 1);
        String[] parts = value.trim().split("/", 2);
        try {
            int maximum = parts.length == 2 ? Math.max(1, Integer.parseInt(parts[1].trim())) : 1;
            int current = Math.max(0, Math.min(maximum, Integer.parseInt(parts[0].trim())));
            return new ReputationProgress(current, maximum);
        } catch (NumberFormatException ignored) {
            return new ReputationProgress(0, 1);
        }
    }

    record ReputationProgress(int current, int maximum) {
        double ratio() {
            return maximum <= 0 ? 0.0D : (double) current / maximum;
        }
    }

    private CitizenProfileSection section(CitizenProfileSnapshot profile, String id) {
        if (profile == null || id == null) return null;
        return profile.sections().stream()
                .filter(section -> section.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String value(CitizenProfileSection section, String id, String fallback) {
        if (section == null || id == null) return fallback;
        return section.fields().stream()
                .filter(field -> field.id().equals(id))
                .map(CitizenProfileField::value)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(fallback);
    }

    private void drawProfilePanel(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            String title,
            ElarionUiStyle style
    ) {
        ElarionCivicUi.thinBox(context, x, y, width, height,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        context.fill(x + 2, y + 1, x + width - 2, y + 2, profileAccent(title));
        ElarionUiIcons.drawOrDefault(context, profileIcon(title), x + 8, y + 4, 14);
        ElarionUiTypography.draw(context, textRenderer, title, x + 25, y + 8,
                style.titleColor(), false);
        context.fill(x + 10, y + 20, x + width - 10, y + 21, style.borderColor());
    }

    private void drawProfileFact(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            String value,
            ElarionUiStyle style
    ) {
        drawProfileFact(context, x, y, width, label, value, style, style.textColor());
    }

    private void drawProfileFact(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            String value,
            ElarionUiStyle style,
            int valueColor
    ) {
        ElarionUiTypography.draw(context, textRenderer, label, x, y, style.mutedColor(), false);
        ElarionUiTypography.drawRight(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, value == null ? "" : value, Math.max(20, width / 2)),
                x + width, y, valueColor, false);
    }

    private void drawProfileEmptyLine(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            String detail,
            ElarionUiStyle style
    ) {
        ElarionUiTypography.draw(context, textRenderer, label, x, y, style.textColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, detail, width),
                x, y + 12, style.mutedColor(), false);
    }

    private void drawPlayerHead(DrawContext context, UUID citizenId, int x, int y, int size, ElarionUiStyle style) {
        if (client != null && client.getNetworkHandler() != null) {
            var entry = client.getNetworkHandler().getPlayerListEntry(citizenId);
            if (entry != null) {
                PlayerSkinDrawer.draw(context, entry.getSkinTextures(), x, y, size);
                return;
            }
        }
        ElarionUiTypography.drawCentered(context, textRenderer, "?", x + size / 2, y + size / 2 - 4,
                style.titleColor(), false);
    }

    private int unlockedCount(String tabId) {
        return tabs().stream()
                .filter(tab -> tab.id().equals(tabId))
                .findFirst()
                .map(tab -> (int) tab.entries().stream().filter(ElarionCollectionEntry::unlocked).count())
                .orElse(0);
    }

    private String contributedField(
            CitizenProfileSnapshot profile,
            String sourceSystem,
            String fieldId,
            String fallback
    ) {
        if (profile == null) return fallback;
        return profile.sections().stream()
                .filter(section -> section.sourceSystem().equalsIgnoreCase(sourceSystem))
                .flatMap(section -> section.fields().stream())
                .filter(field -> field.id().equals(fieldId))
                .map(CitizenProfileField::value)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(fallback);
    }

    private static String profileIcon(String title) {
        return switch (title) {
            case "EMBER STANDING" -> "profile";
            case "PROGRESSION" -> "progression";
            case "COLLECTION" -> "collection";
            case "AFFILIATIONS" -> "affiliations";
            case "LIFETIME RECORD" -> "lifetime";
            case "CHRONICLE" -> "chronicle";
            default -> "default";
        };
    }

    private static int profileAccent(String title) {
        return switch (title) {
            case "EMBER STANDING", "COLLECTION" -> ElarionCivicColors.ACTIVE_GREEN;
            case "PROGRESSION" -> ElarionCivicColors.QUEST_PURPLE;
            case "AFFILIATIONS" -> ElarionCivicColors.INFO_BLUE;
            case "LIFETIME RECORD" -> ElarionCivicColors.REJECT_RED;
            default -> ElarionCivicColors.GOLD_HIGHLIGHT;
        };
    }

    private static String compactDateLine(String label, String value) {
        String date = compactDate(value);
        return date.isBlank() ? label + ": Unknown" : label + ": " + date;
    }

    private static String compactDate(String value) {
        if (value == null || value.isBlank()) return "Unknown";
        try {
            return PROFILE_DATE.format(Instant.parse(value));
        } catch (RuntimeException ignored) {
            return value;
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
            int width,
            int height,
            int mouseX,
            int mouseY,
            float delta,
            ElarionUiStyle style
    ) {
        if (!entry.unlocked()) {
            context.drawCenteredTextWithShadow(textRenderer, "LOCKED", x + width / 2, y + height / 2 - 4,
                    style.mutedColor());
            return;
        }
        if ("titles".equals(selectedTabId) && client != null && client.player != null) {
            int accent = entryAccent(entry);
            ElarionUiTypography.drawCentered(context, textRenderer, entry.title(), x + width / 2, y + 12,
                    accent, true);
            ElarionUiTypography.drawCentered(context, textRenderer,
                    titlePreviewName(client.player.getUuid(), client.player.getGameProfile().getName()),
                    x + width / 2, y + 27,
                    titlePreviewNameColor(client.player.getUuid(), style.textColor()), false);
            renderTitlePlayerPreview(context, client.player, x, y, width, height);
            return;
        }
        if (!ElarionCollectionPreviewRegistry.render(
                context, layout, selectedTabId, entry, x + 8, y + 8, width - 16, height - 20,
                mouseX, mouseY, delta, style)) {
            int iconSize = Math.min(72, Math.min(width, height) - 34);
            int iconX = x + (width - iconSize) / 2;
            int iconY = y + (height - iconSize) / 2;
            if (ElarionUiIcons.has(entry.icon())) {
                ElarionUiIcons.drawOrDefault(context, entry.icon(), iconX, iconY, iconSize);
            } else {
                Identifier icon = Identifier.tryParse(entry.icon());
                if (icon != null) drawScaledIcon(context, icon, iconX, iconY, iconSize);
            }
        }
        context.fill(x + 28, y + height - 19, x + width - 28, y + height - 17, 0x554B3320);
    }

    static String titlePreviewName(java.util.UUID playerId, String fallbackUsername) {
        return ClientIdentityCache.find(playerId)
                .map(ClientIdentity::baseName)
                .filter(name -> !name.isBlank())
                .orElse(fallbackUsername == null ? "" : fallbackUsername);
    }

    static int titlePreviewNameColor(java.util.UUID playerId, int fallbackColor) {
        return ClientIdentityCache.find(playerId)
                .map(ClientIdentity::nameColorArgb)
                .orElse(fallbackColor);
    }

    private void renderTitlePlayerPreview(
            DrawContext context,
            net.minecraft.client.network.ClientPlayerEntity player,
            int x,
            int y,
            int width,
            int height
    ) {
        float yaw = player.getYaw();
        float previousYaw = player.prevYaw;
        float bodyYaw = player.bodyYaw;
        float previousBodyYaw = player.prevBodyYaw;
        float headYaw = player.headYaw;
        float previousHeadYaw = player.prevHeadYaw;
        float pitch = player.getPitch();
        float previousPitch = player.prevPitch;
        try {
            player.setYaw(180.0F);
            player.prevYaw = 180.0F;
            player.bodyYaw = 180.0F;
            player.prevBodyYaw = 180.0F;
            player.headYaw = 180.0F;
            player.prevHeadYaw = 180.0F;
            player.setPitch(0.0F);
            player.prevPitch = 0.0F;
            ElarionMenuEntityPreviewRenderer.render(context, layout, player,
                    x + 18, y + 38, width - 36, height - 46, 68, 0.0F, 0.0F, 58);
        } finally {
            player.setYaw(yaw);
            player.prevYaw = previousYaw;
            player.bodyYaw = bodyYaw;
            player.prevBodyYaw = previousBodyYaw;
            player.headYaw = headYaw;
            player.prevHeadYaw = previousHeadYaw;
            player.setPitch(pitch);
            player.prevPitch = previousPitch;
        }
    }

    private void drawCollectionTabLabel(
            DrawContext context,
            ElarionCollectionTab tab,
            int x,
            int width,
            boolean selected,
            ElarionUiStyle style
    ) {
        String icon = collectionTabIcon(tab.id());
        int iconSize = 14;
        int gap = 5;
        int textWidth = ElarionUiTypography.width(textRenderer, tab.title());
        int groupWidth = iconSize + gap + textWidth;
        int startX = x + (width - groupWidth) / 2;
        ElarionUiIcons.drawOrDefault(context, icon, startX, Layout.TAB_Y + 3, iconSize + 2);
        ElarionUiTypography.draw(context, textRenderer, tab.title(),
                startX + iconSize + gap, Layout.TAB_Y + 7,
                selected ? ACTIVE_BORDER : style.textColor(), false);
    }

    private static String collectionTabIcon(String tabId) {
        return switch (tabId) {
            case ElarionCollectionService.PROFILE_TAB_ID -> "profile";
            case "reputation" -> "realm";
            case "mounts" -> "mounts";
            case "pets" -> "pets";
            case "titles" -> "titles";
            default -> "collection";
        };
    }

    private void renderEmptyCollection(
            DrawContext context,
            ElarionCollectionTab tab,
            int x,
            int y,
            int width,
            int height,
            ElarionUiStyle style
    ) {
        String icon = collectionTabIcon(tab == null ? "" : tab.id());
        int iconSize = 40;
        int centerX = x + width / 2;
        int top = y + height / 2 - 48;
        ElarionUiIcons.drawOrDefault(context, icon, centerX - iconSize / 2, top, iconSize);
        String title = tab == null ? "Nothing registered" : "No " + tab.title().toLowerCase(Locale.ROOT) + " recorded";
        ElarionUiTypography.drawCentered(context, textRenderer, title, centerX, top + 49, style.titleColor(), false);
        String detail = tab == null || tab.subtitle().isBlank() ? "This ledger section is empty." : tab.subtitle();
        ElarionUiTypography.drawCentered(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, detail, width - 32),
                centerX, top + 66, style.mutedColor(), false);
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
            int accent,
            ElarionUiStyle style
    ) {
        int border = active || selected ? accent : style.borderColor();
        int highlight = active ? brighten(accent) : style.bevelHighlightColor();
        int shadow = active ? darken(accent) : style.bevelShadowColor();
        drawBeveledBox(context, x, y, width, height, fill, border, highlight, shadow);
    }

    private static int entryAccent(ElarionCollectionEntry entry) {
        if (entry == null) return ACTIVE_BORDER;
        if (entry.accentColor() != 0) return entry.accentColor();
        if (entry.rankColor() != 0) return entry.rankColor();
        return ACTIVE_BORDER;
    }

    private static int entryRankColor(ElarionCollectionEntry entry) {
        return entry != null && entry.rankColor() != 0 ? entry.rankColor() : entryAccent(entry);
    }

    private static void drawRankBadge(
            DrawContext context,
            net.minecraft.client.font.TextRenderer textRenderer,
            int x,
            int y,
            int width,
            String label,
            int color
    ) {
        int fill = darken(color);
        context.fill(x + 1, y, x + width - 1, y + 12, fill);
        context.fill(x, y + 1, x + width, y + 11, fill);
        context.fill(x + 1, y, x + width - 1, y + 1, brighten(color));
        context.fill(x + 1, y + 11, x + width - 1, y + 12, darken(color));
        context.fill(x, y + 1, x + 1, y + 11, color);
        context.fill(x + width - 1, y + 1, x + width, y + 11, darken(color));
        ElarionUiTypography.draw(context, textRenderer, label, x + 6, y + 2, 0xFFFFFFFF, false);
    }

    private static int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + 45);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 45);
        int b = Math.min(255, (color & 0xFF) + 45);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static int darken(int color) {
        int r = ((color >> 16) & 0xFF) / 2;
        int g = ((color >> 8) & 0xFF) / 2;
        int b = (color & 0xFF) / 2;
        return 0xFF000000 | r << 16 | g << 8 | b;
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
        if (ElarionUiIcons.has(raw)) {
            int drawSize = Math.max(1, size - 4);
            int drawX = x + (size - drawSize) / 2;
            int drawY = y + (size - drawSize) / 2;
            ElarionUiIcons.drawOrDefault(context, raw, drawX, drawY, drawSize);
            return;
        }
        Identifier icon = Identifier.tryParse(raw);
        if (icon == null) return;
        int drawSize = Math.max(1, size - 6);
        int drawX = x + (size - drawSize) / 2;
        int drawY = y + (size - drawSize) / 2;
        drawScaledIcon(context, icon, drawX, drawY, drawSize);
    }

    private static void drawScaledIcon(DrawContext context, Identifier icon, int x, int y, int size) {
        if (icon == null || size <= 0) return;
        float scale = size / 16.0F;
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawTexture(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        context.getMatrices().pop();
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
        static final int TAB_WIDTH = 136;
        static final int TAB_HEIGHT = 22;
        static final int PANEL_TOP = 106;
        static final int PANEL_BOTTOM = 446;
        static final int CONTENT_HEADER_Y = 78;
        static final int CONTENT_HEADER_HEIGHT = 22;
        static final int VISIBLE_ROWS = 6;
        static final int ROW_GAP = 4;

        int tabX(int index) {
            return tabX(index, 5);
        }

        int tabWidth(int tabCount) {
            if (tabCount <= 0) return TAB_WIDTH;
            return (PANEL_WIDTH - MARGIN * 2 - GAP * (tabCount - 1)) / tabCount;
        }

        int tabX(int index, int tabCount) {
            return MARGIN + index * (tabWidth(tabCount) + GAP);
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
