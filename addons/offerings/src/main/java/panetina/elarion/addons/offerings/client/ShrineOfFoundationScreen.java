package panetina.elarion.addons.offerings.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.offerings.network.ShrineUiOpenPayload;
import panetina.elarion.addons.offerings.network.ShrineContributionSubmitPayload;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionNumericInput;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionVirtualList;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.util.List;

public final class ShrineOfFoundationScreen extends ElarionScreen {
    private static final int HEADER_PROGRESS_HEIGHT = 18;
    private static final int HEADER_TO_TABS_GAP = 8;
    private static final int TABS_TO_CONTENT_GAP = 8;
    static final int FOOTER_BUTTON_HEIGHT = 20;
    private static final int CONTRIBUTION_MESSAGE_HEIGHT = 22;
    private static final int CONTENT_INSET = 5;
    private static final int LIST_SCROLLBAR_GAP = 3;
    private static final int ROW_HORIZONTAL_PADDING = 8;
    private static final int ROW_ICON_SIZE = 16;
    private static final int ROW_ICON_GAP = 8;
    private static final int REWARD_SLOT_SIZE = 30;
    private static final int REWARD_SLOT_GAP = 6;

    private enum Tab {
        CONTRIBUTE("Contribute"),
        HISTORY("History");

        private final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    private ShrineUiOpenPayload payload;
    private Tab selectedTab = Tab.CONTRIBUTE;
    private ElarionScaledLayout layout;
    private ElarionUiThemeVariant theme;
    private ElarionUiStyle style;
    private ElarionVirtualList list;
    private int padding;
    private int contentTop;
    private int contentBottom;
    private int mainX;
    private int mainWidth;
    private int progressY;
    private int tabsY;
    private int summaryY;
    private ShrineUiOpenPayload.DisplayRow hoveredReward;
    private ItemStack hoveredRewardStack;
    private ShrineUiOpenPayload.RequirementRow activeRequirement;
    private ElarionNumericInput numericInput;
    private boolean submitting;
    private int contributeFirstVisible;
    private int historyFirstVisible;
    private boolean draggingScrollbar;

    static LayoutMetrics calculateLayout(
            int logicalWidth, int logicalHeight, int padding, int summaryWidth, int gap, int tabHeight
    ) {
        int mainX = padding + summaryWidth + gap;
        int progressY = padding + 32;
        int tabsY = progressY + HEADER_PROGRESS_HEIGHT + HEADER_TO_TABS_GAP;
        int contentTop = tabsY + tabHeight + TABS_TO_CONTENT_GAP;
        int closeY = logicalHeight - padding - FOOTER_BUTTON_HEIGHT;
        int contentBottom = closeY - gap;
        return new LayoutMetrics(mainX, logicalWidth - mainX - padding, progressY, tabsY,
                contentTop, contentBottom, closeY);
    }

    public ShrineOfFoundationScreen(ShrineUiOpenPayload payload) {
        super(Text.literal(payload.title()));
        this.payload = payload;
    }

    public boolean belongsTo(ShrineUiOpenPayload update) {
        return payload.instanceId().equals(update.instanceId());
    }

    public void applySnapshot(ShrineUiOpenPayload update) {
        saveListPosition();
        payload = update;
        activeRequirement = null;
        numericInput = null;
        submitting = false;
        if (client != null) init(client, width, height);
    }

    @Override
    protected void init() {
        theme = ElarionUiThemes.variant(payload.themeVariant());
        style = ElarionUiStyle.from(theme);
        padding = ElarionUiThemes.current().padding();
        layout = ElarionScaledLayout.fit(width, height, payload.logicalWidth(), payload.logicalHeight(),
                8, payload.minimumScalePercent());
        LayoutMetrics metrics = calculateLayout(payload.logicalWidth(), payload.logicalHeight(), padding,
                payload.summaryWidth(), ElarionUiThemes.current().gap(), payload.tabHeight());
        mainX = metrics.mainX();
        mainWidth = metrics.mainWidth();
        progressY = metrics.progressY();
        tabsY = metrics.tabsY();
        summaryY = tabsY;
        contentTop = metrics.contentTop();
        contentBottom = metrics.contentBottom();
        rebuildList();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hoveredReward = null;
        hoveredRewardStack = null;
        context.fill(0, 0, width, height, theme.backgroundOverlayColor());
        double logicalMouseX = layout.logicalX(mouseX);
        double logicalMouseY = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, payload.logicalWidth(), payload.logicalHeight(),
                progressY + HEADER_PROGRESS_HEIGHT + 4);
        renderHeader(context);
        renderSummary(context, logicalMouseX, logicalMouseY);
        renderTabs(context, logicalMouseX, logicalMouseY);
        switch (selectedTab) {
            case CONTRIBUTE -> renderContribute(context, logicalMouseX, logicalMouseY);
            case HISTORY -> renderHistory(context);
        }
        renderClose(context, logicalMouseX, logicalMouseY);
        context.getMatrices().pop();
        renderRewardTooltip(context, mouseX, mouseY);
    }

    private void renderHeader(DrawContext context) {
        String level = payload.levelText();
        int levelWidth = ElarionUiTypography.width(textRenderer, level);
        String title = "SHRINE OF FOUNDATION";
        String subtitle = ElarionUiRenderer.ellipsize(textRenderer, payload.subtitle(),
                payload.logicalWidth() - padding * 2 - levelWidth - 18);
        int titleWidth = ElarionUiTypography.width(textRenderer, title);
        int titleX = (payload.logicalWidth() - titleWidth) / 2;
        ElarionCivicUi.headerOrnament(context, titleX - 12, padding + 5, true);
        ElarionCivicUi.headerOrnament(context, titleX + titleWidth + 12, padding + 5, false);
        ElarionUiTypography.draw(context, textRenderer, title, titleX, padding + 1,
                lighten(theme.titleColor(), 0.15F), false);
        ElarionUiTypography.draw(context, textRenderer, subtitle, padding, padding + 15, theme.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer, level,
                payload.logicalWidth() - padding - levelWidth, padding + 15, theme.warningColor(), false);
        ElarionCivicUi.thinBox(context, padding, progressY, payload.logicalWidth() - padding * 2,
                HEADER_PROGRESS_HEIGHT, ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_BORDER);
        ElarionUiRenderer.progressBar(context, textRenderer, padding, progressY,
                payload.logicalWidth() - padding * 2, HEADER_PROGRESS_HEIGHT,
                payload.progressCurrent(), payload.progressRequired(), theme);
    }

    private void renderSummary(DrawContext context, double mouseX, double mouseY) {
        int x = padding;
        int y = summaryY;
        int width = payload.summaryWidth();
        int height = contentBottom - y;
        ElarionCivicUi.thinBox(context, x, y, width, height,
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        int iconX = x + (width - payload.iconSize()) / 2;
        int iconY = y + 10;
        ElarionCivicUi.thinBox(context, iconX - 3, iconY - 3,
                payload.iconSize() + 6, payload.iconSize() + 6,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        ElarionUiIcons.drawOrDefault(context, summaryIcon(), iconX, iconY, payload.iconSize());
        int textY = y + payload.iconSize() + 20;
        int statusY = y + height - 16;
        String projectTitle = ElarionUiRenderer.ellipsize(textRenderer, payload.title(), width - 16);
        ElarionUiTypography.drawCentered(context, textRenderer, projectTitle, x + width / 2, textY,
                lighten(theme.titleColor(), 0.15F), false);
        int descriptionY = textY + 15;
        int desiredRewardsHeight = rewardPanelDesiredHeight(width - 14, payload.rewardRows().size());
        int rewardsY = Math.max(descriptionY + 38, statusY - desiredRewardsHeight - 8);
        int rewardsHeight = Math.max(54, statusY - rewardsY - 8);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(payload.description()),
                x + 8, descriptionY, width - 16, Math.max(30, rewardsY - descriptionY - 8),
                theme.textColor(), theme.mutedColor());
        renderSummaryRewards(context, x + 7, rewardsY, width - 14, rewardsHeight, mouseX, mouseY);
        int statusColor = "Completed".equalsIgnoreCase(payload.status())
                ? theme.successColor() : "Inactive".equalsIgnoreCase(payload.status())
                ? theme.disabledColor() : theme.warningColor();
        String status = payload.status();
        ElarionCivicUi.divider(context, x + 6, statusY - 5, width - 12);
        ElarionUiTypography.draw(context, textRenderer, status,
                x + (width - ElarionUiTypography.width(textRenderer, status)) / 2,
                statusY, statusColor, false);
    }

    private void renderTabs(DrawContext context, double mouseX, double mouseY) {
        int tabWidth = mainWidth / Tab.values().length;
        for (int index = 0; index < Tab.values().length; index++) {
            Tab tab = Tab.values()[index];
            int x = mainX + index * tabWidth;
            int width = index == Tab.values().length - 1 ? mainX + mainWidth - x : tabWidth;
            boolean selected = selectedTab == tab;
            boolean hovered = inside(mouseX, mouseY, x, tabsY, width, payload.tabHeight());
            ElarionCivicUi.rowSurface(context, x, tabsY, width, payload.tabHeight(), selected, hovered, false);
            int iconSize = Math.min(16, payload.tabHeight() - 4);
            int labelWidth = ElarionUiTypography.width(textRenderer, tab.label);
            int groupWidth = iconSize + 5 + labelWidth;
            int groupX = x + Math.max(5, (width - groupWidth) / 2);
            int groupY = tabsY + Math.max(1, (payload.tabHeight() - iconSize) / 2);
            ElarionUiIcons.drawOrDefault(context, tab == Tab.CONTRIBUTE ? "offering" : "history",
                    groupX, groupY, iconSize);
            ElarionUiTypography.draw(context, textRenderer, tab.label, groupX + iconSize + 5,
                    ElarionCivicUi.centeredTextY(textRenderer, tabsY, payload.tabHeight()),
                    selected ? theme.successColor() : theme.titleColor(), false);
        }
    }

    private void renderContribute(DrawContext context, double mouseX, double mouseY) {
        List<ShrineUiOpenPayload.RequirementRow> rows = payload.requirementRows();
        renderListBackground(context);
        boolean scrollable = rows.size() > list.visibleRows();
        for (int index = list.firstVisible(); index < list.lastVisibleExclusive(); index++) {
            int y = contentTop + CONTENT_INSET + (index - list.firstVisible()) * payload.rowHeight();
            renderRequirement(context, rows.get(index), y, scrollable, mouseX, mouseY);
        }
        renderScrollbar(context, rows.size());
        int availableWidth = mainWidth - CONTENT_INSET * 2;
        String message = payload.resultMessage().isBlank()
                ? payload.completed()
                ? "This project is complete."
                : "Select an incomplete item or currency requirement."
                : payload.resultMessage();
        String display = ElarionUiRenderer.ellipsize(textRenderer, message, availableWidth);
        int color = payload.resultError() ? theme.errorColor()
                : payload.resultMessage().isBlank() ? theme.mutedColor() : theme.successColor();
        ElarionUiTypography.draw(context, textRenderer, display,
                mainX + CONTENT_INSET + (availableWidth - ElarionUiTypography.width(textRenderer, display)) / 2,
                contentBottom - 15, color, false);
        renderNumericPrompt(context, mouseX, mouseY);
    }

    private void renderRequirement(
            DrawContext context,
            ShrineUiOpenPayload.RequirementRow row,
            int y,
            boolean scrollable,
            double mouseX,
            double mouseY
    ) {
        int rowX = mainX + CONTENT_INSET;
        int scrollbarWidth = ElarionUiThemes.current().scrollbarWidth();
        int scrollbarReserve = scrollable ? scrollbarWidth + LIST_SCROLLBAR_GAP : 0;
        int rowWidth = mainWidth - CONTENT_INSET * 2 - scrollbarReserve;
        int rowHeight = payload.rowHeight() - 2;
        boolean interactive = !payload.completed() && !row.complete()
                && ("items".equals(row.type()) || "currency".equals(row.type()));
        boolean hovered = interactive && activeRequirement == null
                && inside(mouseX, mouseY, rowX, y, rowWidth, rowHeight);
        ElarionCivicUi.rowSurface(context, rowX, y, rowWidth, rowHeight,
                row.complete(), hovered, !interactive && !row.complete());
        int iconX = rowX + ROW_HORIZONTAL_PADDING;
        int iconY = y + (rowHeight - ROW_ICON_SIZE) / 2;
        int textY = y + (rowHeight - ElarionUiTypography.fontHeight(textRenderer)) / 2;
        int labelX = iconX + ROW_ICON_SIZE + ROW_ICON_GAP;
        renderRowIcon(context, row.icon(), iconX, iconY);
        String amount = row.current() + " / " + row.required();
        int amountX = rowX + rowWidth - ROW_HORIZONTAL_PADDING - ElarionUiTypography.width(textRenderer, amount);
        int labelWidth = Math.max(1, amountX - ROW_ICON_GAP - labelX);
        ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.label(), labelWidth),
                labelX, textY, row.complete() ? theme.successColor() : theme.textColor(), false);
        ElarionUiTypography.draw(context, textRenderer, amount, amountX,
                textY, row.complete() ? theme.successColor() : theme.mutedColor(), false);
    }

    private void renderNumericPrompt(DrawContext context, double mouseX, double mouseY) {
        if (activeRequirement == null || numericInput == null) return;
        int width = Math.min(260, mainWidth - 24);
        int height = 82;
        int x = mainX + (mainWidth - width) / 2;
        int y = contentTop + (contentBottom - contentTop - height) / 2;
        ElarionCivicUi.headerShell(context, x, y, width, height, 22);
        String question = "currency".equals(activeRequirement.type())
                ? "How much " + activeRequirement.label() + "?"
                : "How many " + activeRequirement.label() + "?";
        ElarionUiTypography.drawCentered(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, question, width - 20),
                x + width / 2, y + 7, theme.titleColor(), false);
        ElarionCivicUi.thinBox(context, x + 8, y + 27, width - 16, 18,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        String value = numericInput.value() + (numericInput.caretVisible() ? "_" : "");
        ElarionUiTypography.draw(context, textRenderer, value, x + 13, y + 32, theme.textColor(), false);
        int buttonWidth = 72;
        int buttonHeight = 16;
        int cancelX = x + width - buttonWidth - 8;
        int submitX = cancelX - buttonWidth - 6;
        int buttonY = y + height - buttonHeight - 8;
        ElarionCivicUi.compactActionButton(context, textRenderer, submitX, buttonY,
                buttonWidth, buttonHeight, submitting ? "Sending..." : "Offer",
                inside(mouseX, mouseY, submitX, buttonY, buttonWidth, buttonHeight),
                false, !submitting && !numericInput.empty(), ElarionCivicUi.Tone.PRIMARY, style);
        ElarionCivicUi.compactActionButton(context, textRenderer, cancelX, buttonY,
                buttonWidth, buttonHeight, "Cancel",
                inside(mouseX, mouseY, cancelX, buttonY, buttonWidth, buttonHeight),
                false, !submitting, ElarionCivicUi.Tone.NORMAL, style);
    }

    private void renderHistory(DrawContext context) {
        renderListBackground(context);
        if (payload.historyRows().isEmpty()) {
            ElarionUiTypography.draw(context, textRenderer, payload.historyPlaceholder(), mainX + 8, contentTop + 8,
                    theme.mutedColor(), false);
        } else {
            renderDisplayRows(context, payload.historyRows());
        }
    }

    private void renderDisplayRows(DrawContext context, List<ShrineUiOpenPayload.DonationRow> rows) {
        boolean scrollable = rows.size() > list.visibleRows();
        int scrollbarReserve = scrollable
                ? ElarionUiThemes.current().scrollbarWidth() + LIST_SCROLLBAR_GAP : 0;
        int rowX = mainX + CONTENT_INSET;
        int rowWidth = mainWidth - CONTENT_INSET * 2 - scrollbarReserve;
        for (int index = list.firstVisible(); index < list.lastVisibleExclusive(); index++) {
            int y = contentTop + CONTENT_INSET + (index - list.firstVisible()) * payload.rowHeight();
            ShrineUiOpenPayload.DonationRow row = rows.get(index);
            ElarionCivicUi.rowSurface(
                    context, rowX, y, rowWidth, payload.rowHeight() - 2, false, false, false);
            String offered = row.amount() + " " + row.offeringLabel();
            int nameWidth = ElarionUiTypography.width(textRenderer, row.contributor());
            String verb = " offered ";
            int verbWidth = ElarionUiTypography.width(textRenderer, verb);
            int offeredWidth = ElarionUiTypography.width(textRenderer, offered);
            int totalWidth = nameWidth + verbWidth + offeredWidth;
            int startX = rowX + Math.max(ROW_HORIZONTAL_PADDING, (rowWidth - totalWidth) / 2);
            int textY = y + (payload.rowHeight() - 2 - ElarionUiTypography.fontHeight(textRenderer)) / 2;
            ElarionUiTypography.draw(context, textRenderer, row.contributor(), startX, textY, row.contributorColor(), false);
            ElarionUiTypography.draw(context, textRenderer, verb, startX + nameWidth, textY, theme.mutedColor(), false);
            ElarionUiTypography.draw(context, textRenderer, offered, startX + nameWidth + verbWidth,
                    textY, row.offeringColor(), false);
        }
        renderScrollbar(context, rows.size());
    }

    private void renderListBackground(DrawContext context) {
        ElarionCivicUi.messageBody(
                context, mainX, contentTop, mainWidth, contentBottom - contentTop,
                ElarionCivicColors.GOLD_SHADOW);
    }

    private void renderScrollbar(DrawContext context, int count) {
        if (count <= list.visibleRows()) return;
        int scrollbarWidth = ElarionUiThemes.current().scrollbarWidth();
        int bottom = selectedTab == Tab.CONTRIBUTE ? contributionListBottom() : contentBottom;
        ElarionUiRenderer.scrollbar(context,
                mainX + mainWidth - CONTENT_INSET - scrollbarWidth,
                contentTop + CONTENT_INSET, scrollbarWidth,
                bottom - contentTop - CONTENT_INSET * 2,
                list.firstVisible(), list.visibleRows(), count, theme);
    }

    private void renderRowIcon(DrawContext context, String icon, int x, int y) {
        if (icon.startsWith("item:")) {
            Identifier id = Identifier.tryParse(icon.substring("item:".length()));
            if (id != null && Registries.ITEM.containsId(id)) {
                context.getMatrices().push();
                context.getMatrices().translate(x, y, 0.0F);
                context.getMatrices().scale(ROW_ICON_SIZE / 16.0F, ROW_ICON_SIZE / 16.0F, 1.0F);
                context.drawItem(new ItemStack(Registries.ITEM.get(id)), 0, 0);
                context.getMatrices().pop();
                return;
            }
        }
        if (ElarionUiIcons.has(icon)) {
            ElarionUiIcons.drawOrDefault(context, icon, x, y, ROW_ICON_SIZE);
            return;
        }
        String semantic = icon == null || icon.isBlank() ? "requirement" : switch (icon) {
            case "currency" -> "bank";
            case "items" -> "requirement";
            default -> "";
        };
        if (!semantic.isBlank()) {
            ElarionUiIcons.drawOrDefault(context, semantic, x, y, ROW_ICON_SIZE);
            return;
        }
        String raw = icon.startsWith("texture:") ? icon.substring("texture:".length()) : icon;
        Identifier texture = Identifier.tryParse(raw);
        if (texture != null) {
            context.drawTexture(texture, x, y, 0, 0, ROW_ICON_SIZE, ROW_ICON_SIZE, ROW_ICON_SIZE, ROW_ICON_SIZE);
        }
    }

    private String summaryIcon() {
        return ElarionUiIcons.has(payload.icon()) ? payload.icon() : "shrine";
    }

    private void renderClose(DrawContext context, double mouseX, double mouseY) {
        int x = (payload.logicalWidth() - payload.closeButtonWidth()) / 2;
        int y = payload.logicalHeight() - padding - FOOTER_BUTTON_HEIGHT;
        ElarionCivicUi.divider(context, padding, y - ElarionUiThemes.current().gap() / 2,
                payload.logicalWidth() - padding * 2);
        ElarionCivicUi.compactActionButton(context, textRenderer, x, y,
                payload.closeButtonWidth(), FOOTER_BUTTON_HEIGHT, "Close",
                inside(mouseX, mouseY, x, y, payload.closeButtonWidth(), FOOTER_BUTTON_HEIGHT),
                inside(mouseX, mouseY, x, y, payload.closeButtonWidth(), FOOTER_BUTTON_HEIGHT)
                        && mouseDown(),
                true, ElarionCivicUi.Tone.NORMAL, style);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        int tabWidth = mainWidth / Tab.values().length;
        if (y >= tabsY && y < tabsY + payload.tabHeight() && x >= mainX && x < mainX + mainWidth) {
            saveListPosition();
            int index = Math.min(Tab.values().length - 1, (int) ((x - mainX) / tabWidth));
            selectedTab = Tab.values()[index];
            rebuildList();
            return true;
        }
        int closeX = (payload.logicalWidth() - payload.closeButtonWidth()) / 2;
        int closeY = payload.logicalHeight() - padding - FOOTER_BUTTON_HEIGHT;
        if (inside(x, y, closeX, closeY, payload.closeButtonWidth(), FOOTER_BUTTON_HEIGHT)) {
            close();
            return true;
        }
        if (activeRequirement != null) {
            int promptWidth = Math.min(260, mainWidth - 24);
            int promptHeight = 82;
            int promptX = mainX + (mainWidth - promptWidth) / 2;
            int promptY = contentTop + (contentBottom - contentTop - promptHeight) / 2;
            int buttonWidth = 72;
            int buttonHeight = 16;
            int cancelX = promptX + promptWidth - buttonWidth - 8;
            int submitX = cancelX - buttonWidth - 6;
            int buttonY = promptY + promptHeight - buttonHeight - 8;
            if (!submitting && inside(x, y, cancelX, buttonY, buttonWidth, buttonHeight)) {
                cancelPrompt();
            } else if (!submitting && !numericInput.empty()
                    && inside(x, y, submitX, buttonY, buttonWidth, buttonHeight)) {
                submitPrompt();
            }
            return true;
        }
        if (selectedTab == Tab.CONTRIBUTE && !payload.completed()) {
            int listBottom = contributionListBottom();
            int scrollbarWidth = ElarionUiThemes.current().scrollbarWidth();
            int scrollbarX = mainX + mainWidth - CONTENT_INSET - scrollbarWidth;
            if (inside(x, y, scrollbarX, contentTop + CONTENT_INSET, scrollbarWidth,
                    listBottom - contentTop - CONTENT_INSET * 2)) {
                updateScrollbarFromMouse(y, listBottom);
                draggingScrollbar = true;
                return true;
            }
            if (y >= listBottom) return true;
            boolean scrollable = payload.requirementRows().size() > list.visibleRows();
            int scrollbarReserve = scrollable
                    ? ElarionUiThemes.current().scrollbarWidth() + LIST_SCROLLBAR_GAP : 0;
            int rowX = mainX + CONTENT_INSET;
            int rowWidth = mainWidth - CONTENT_INSET * 2 - scrollbarReserve;
            if (!inside(x, y, rowX, contentTop + CONTENT_INSET,
                    rowWidth, listBottom - contentTop - CONTENT_INSET * 2)) {
                return true;
            }
            int rowIndex = list.itemAt(y, contentTop + CONTENT_INSET, payload.rowHeight());
            if (rowIndex >= 0 && rowIndex < payload.requirementRows().size()) {
                var row = payload.requirementRows().get(rowIndex);
                if (!row.complete() && ("items".equals(row.type()) || "currency".equals(row.type()))) {
                    list.select(rowIndex);
                    saveListPosition();
                    activeRequirement = row;
                    numericInput = new ElarionNumericInput(10);
                    return true;
                }
            }
        }
        return inside(x, y, 0, 0, payload.logicalWidth(), payload.logicalHeight());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!draggingScrollbar) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        updateScrollbarFromMouse(layout.logicalY(mouseY), contributionListBottom());
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        if (!inside(x, y, mainX, contentTop, mainWidth, contentBottom - contentTop)) return false;
        int direction = verticalAmount > 0 ? -1 : verticalAmount < 0 ? 1 : 0;
        boolean changed = direction != 0 && list.scroll(direction);
        if (changed) saveListPosition();
        return changed;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (activeRequirement != null) {
                cancelPrompt();
                return true;
            }
            close();
            return true;
        }
        if (activeRequirement != null) {
            if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
                    && !numericInput.empty() && !submitting) {
                submitPrompt();
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                numericInput.backspace();
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                numericInput.clear();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            saveListPosition();
            int direction = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1;
            int index = Math.max(0, Math.min(Tab.values().length - 1, selectedTab.ordinal() + direction));
            selectedTab = Tab.values()[index];
            rebuildList();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            list.moveSelection(keyCode == GLFW.GLFW_KEY_UP ? -1 : 1);
            saveListPosition();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            list.page(keyCode == GLFW.GLFW_KEY_PAGE_UP ? -1 : 1);
            saveListPosition();
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
                && selectedTab == Tab.CONTRIBUTE && !payload.completed()) {
            openSelectedRequirement();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return activeRequirement != null && numericInput.type(chr);
    }

    private void rebuildList() {
        int count = switch (selectedTab) {
            case CONTRIBUTE -> payload.requirementRows().size();
            case HISTORY -> payload.historyRows().size();
        };
        int reserved = selectedTab == Tab.CONTRIBUTE ? CONTRIBUTION_MESSAGE_HEIGHT : 0;
        int visible = Math.max(1,
                (contentBottom - contentTop - reserved - CONTENT_INSET * 2) / payload.rowHeight());
        int preferred = selectedTab == Tab.CONTRIBUTE ? contributeFirstVisible : historyFirstVisible;
        list = new ElarionVirtualList(count, visible, preferred);
    }

    private int contributionListBottom() {
        return contentBottom - CONTRIBUTION_MESSAGE_HEIGHT;
    }

    private void saveListPosition() {
        if (list == null) return;
        if (selectedTab == Tab.CONTRIBUTE) contributeFirstVisible = list.firstVisible();
        else historyFirstVisible = list.firstVisible();
    }

    private void updateScrollbarFromMouse(double mouseY, int listBottom) {
        if (list.maximumFirstVisible() <= 0) return;
        int trackY = contentTop + CONTENT_INSET;
        int trackHeight = Math.max(1, listBottom - contentTop - CONTENT_INSET * 2);
        double ratio = Math.max(0.0D, Math.min(1.0D, (mouseY - trackY) / trackHeight));
        list.setFirstVisible((int) Math.round(ratio * list.maximumFirstVisible()));
        saveListPosition();
    }

    private void openSelectedRequirement() {
        int index = list.selectedIndex();
        if (index < 0 || index >= payload.requirementRows().size()) return;
        var row = payload.requirementRows().get(index);
        if (row.complete() || (!"items".equals(row.type()) && !"currency".equals(row.type()))) return;
        activeRequirement = row;
        numericInput = new ElarionNumericInput(10);
    }

    private void submitPrompt() {
        if (activeRequirement == null || numericInput == null || numericInput.empty() || submitting) return;
        submitting = true;
        ClientPlayNetworking.send(new ShrineContributionSubmitPayload(
                payload.instanceId(), activeRequirement.key(), numericInput.value()));
    }

    private void cancelPrompt() {
        activeRequirement = null;
        numericInput = null;
        submitting = false;
    }

    private void renderSummaryRewards(
            DrawContext context, int x, int y, int width, int height, double mouseX, double mouseY
    ) {
        ElarionCivicUi.thinBox(context, x, y, width, height,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        String title = "Rewards:";
        int titleWidth = ElarionUiTypography.width(textRenderer, title);
        ElarionCivicUi.headerOrnament(context, x + (width - titleWidth) / 2 - 10, y + 10, true);
        ElarionCivicUi.headerOrnament(context, x + (width + titleWidth) / 2 + 10, y + 10, false);
        ElarionUiTypography.draw(context, textRenderer, title, x + (width - titleWidth) / 2,
                y + 7, theme.titleColor(), false);
        int contentY = y + 24;
        int availableHeight = Math.max(0, height - 34);
        if (payload.rewardRows().isEmpty()) {
            ElarionUiRenderer.wrappedClipped(
                    context, textRenderer, Text.literal(payload.rewardsPlaceholder()),
                    x + 7, contentY, width - 14, availableHeight, theme.mutedColor(), theme.mutedColor());
            return;
        }
        int columns = rewardGridColumns(width);
        int totalRows = Math.max(1, (payload.rewardRows().size() + columns - 1) / columns);
        int slotSize = rewardSlotSizeForRows(availableHeight, totalRows);
        int stride = slotSize + REWARD_SLOT_GAP;
        int maximumRows = Math.max(1, (availableHeight + REWARD_SLOT_GAP) / stride);
        int visible = Math.min(payload.rewardRows().size(), columns * maximumRows);
        int rowCount = Math.max(1, (visible + columns - 1) / columns);
        int gridHeight = rowCount * slotSize + Math.max(0, rowCount - 1) * REWARD_SLOT_GAP;
        int gridY = contentY + Math.max(0, (availableHeight - gridHeight) / 2);
        for (int index = 0; index < visible; index++) {
            var reward = payload.rewardRows().get(index);
            int column = index % columns;
            int row = index / columns;
            int rowStart = row * columns;
            int rowColumns = Math.min(columns, visible - rowStart);
            int rowWidth = rowColumns * slotSize + Math.max(0, rowColumns - 1) * REWARD_SLOT_GAP;
            int startX = x + (width - rowWidth) / 2;
            int slotX = startX + column * stride;
            int slotY = gridY + row * stride;
            ElarionCivicUi.thinBox(context, slotX, slotY, slotSize, slotSize,
                    reward.disabled() ? ElarionCivicColors.BUTTON_DISABLED : ElarionCivicColors.CARD_SURFACE,
                    reward.disabled() ? ElarionCivicColors.GOLD_SHADOW : ElarionCivicColors.GOLD_BORDER);
            int iconOffset = Math.max(3, (slotSize - ROW_ICON_SIZE) / 2);
            renderRewardIcon(context, reward, slotX + iconOffset, slotY + iconOffset);
            if (inside(mouseX, mouseY, slotX, slotY, slotSize, slotSize)) {
                hoveredReward = reward;
                hoveredRewardStack = rewardStack(reward);
            }
        }
    }

    static int rewardGridColumns(int width) {
        int capacity = Math.max(1, (width - 12 + REWARD_SLOT_GAP) / (REWARD_SLOT_SIZE + REWARD_SLOT_GAP));
        return Math.max(1, Math.min(3, capacity));
    }

    static int rewardPanelDesiredHeight(int width, int rewardCount) {
        if (rewardCount <= 0) return 58;
        int columns = rewardGridColumns(width);
        int rows = Math.max(1, (rewardCount + columns - 1) / columns);
        return 28 + rows * REWARD_SLOT_SIZE + Math.max(0, rows - 1) * REWARD_SLOT_GAP + 12;
    }

    static int rewardSlotSizeForRows(int availableHeight, int rowCount) {
        if (rowCount <= 0) return REWARD_SLOT_SIZE;
        int gaps = Math.max(0, rowCount - 1) * REWARD_SLOT_GAP;
        int fit = (availableHeight - gaps) / rowCount;
        return Math.max(22, Math.min(REWARD_SLOT_SIZE, fit));
    }

    private void renderRewardIcon(
            DrawContext context, ShrineUiOpenPayload.DisplayRow reward, int x, int y
    ) {
        ItemStack stack = rewardStack(reward);
        if (!stack.isEmpty()) {
            context.drawItem(stack, x, y);
            context.drawItemInSlot(textRenderer, stack, x, y);
            return;
        }
        if (reward.icon() != null && !reward.icon().isBlank()) {
            renderRowIcon(context, reward.icon(), x, y);
        }
    }

    private void renderRewardTooltip(DrawContext context, int mouseX, int mouseY) {
        if (hoveredReward == null) return;
        if (hoveredRewardStack != null && !hoveredRewardStack.isEmpty()) {
            context.drawItemTooltip(textRenderer, hoveredRewardStack, mouseX, mouseY);
            return;
        }
        java.util.List<Text> lines = new java.util.ArrayList<>();
        lines.add(Text.literal(hoveredReward.label()));
        if (!hoveredReward.body().isBlank()) lines.add(Text.literal(hoveredReward.body()));
        context.drawTooltip(textRenderer, lines, mouseX, mouseY);
    }

    private static ItemStack rewardStack(ShrineUiOpenPayload.DisplayRow reward) {
        if (!"item".equals(reward.kind()) || reward.icon() == null
                || !reward.icon().startsWith("item:")) {
            return ItemStack.EMPTY;
        }
        Identifier id = Identifier.tryParse(reward.icon().substring("item:".length()));
        if (id == null || !Registries.ITEM.containsId(id)) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(Registries.ITEM.get(id));
        stack.setCount(Math.max(1, reward.count()));
        applyEnchantments(stack, reward.enchantments());
        return stack;
    }

    private static void applyEnchantments(ItemStack stack, String raw) {
        if (raw == null || raw.isBlank()) return;
        var world = net.minecraft.client.MinecraftClient.getInstance().world;
        if (world == null) return;
        var enchantments = world.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        for (String entry : raw.split(",")) {
            String trimmed = entry.trim();
            int separator = trimmed.lastIndexOf(':');
            if (separator <= 0 || separator >= trimmed.length() - 1) continue;
            Identifier id = Identifier.tryParse(trimmed.substring(0, separator).trim());
            if (id == null) continue;
            java.util.Optional<RegistryEntry.Reference<Enchantment>> enchantment =
                    enchantments.getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, id));
            if (enchantment.isEmpty()) continue;
            try {
                int level = Math.max(1, Integer.parseInt(trimmed.substring(separator + 1).trim()));
                stack.addEnchantment(enchantment.get(), level);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private static boolean mouseDown() {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    private static int lighten(int color, float amount) {
        int alpha = color >>> 24;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        red += Math.round((255 - red) * amount);
        green += Math.round((255 - green) * amount);
        blue += Math.round((255 - blue) * amount);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    record LayoutMetrics(
            int mainX,
            int mainWidth,
            int progressY,
            int tabsY,
            int contentTop,
            int contentBottom,
            int closeY
    ) {
    }
}
