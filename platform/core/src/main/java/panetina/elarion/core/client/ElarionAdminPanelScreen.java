package panetina.elarion.core.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionModalLayout;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionTextInput;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionVirtualList;
import panetina.elarion.core.config.ElarionConfigEditControl;
import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.model.ElarionAdminPanelSnapshot;
import panetina.elarion.core.model.ElarionAdminPanelTab;
import panetina.elarion.core.network.AdminPanelActionPayload;
import panetina.elarion.core.network.AdminPanelOpenRequestPayload;
import panetina.elarion.core.network.ElarionConfigEditRequestPayload;
import panetina.elarion.core.network.ElarionConfigEditResultPayload;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ElarionAdminPanelScreen extends ElarionScreen {
    private static final int PANEL_WIDTH = 660;
    private static final int PANEL_HEIGHT = 430;
    private static final int TAB_X = 12;
    private static final int TAB_GAP = 6;
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_Y = 58;
    private static final int ROW_HEIGHT = 54;
    private static final int ROW_GAP = 6;
    private static final int LIST_WIDTH = 392;
    private static final int DETAIL_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int CONTENT_TOP = 110;
    private static final int CONTENT_BOTTOM = PANEL_HEIGHT - 32;
    private static final int ACTION_TOP = CONTENT_TOP + 118;
    private static final int ACTION_GAP = 5;
    private static final int CONFIG_MODAL_X = 102;
    private static final int CONFIG_MODAL_Y = 58;
    private static final int CONFIG_MODAL_WIDTH = 456;
    private static final int CONFIG_MODAL_HEIGHT = 324;
    private static final int CONFIG_EDIT_VALUE_MAX = 2048;
    private static final String CONFIG_EDIT_REASON = "admin-panel-config-edit-preview";
    private static final String CONFIG_EDIT_APPLY_REASON = "admin-panel-config-edit-apply";
    private static final int ACTION_MODAL_WIDTH = 380;
    private static final int ACTION_MODAL_CONFIRM_HEIGHT = 158;
    private static final int ACTION_MODAL_INPUT_HEIGHT = 176;
    private static final int ACTION_MODAL_BUTTON_WIDTH = 112;
    private static final int ACTION_MODAL_BUTTON_HEIGHT = 20;
    private static final float MAX_SCALE = 0.95F;

    private ElarionAdminPanelSnapshot snapshot;
    private ElarionScaledLayout layout;
    private ElarionVirtualList list = new ElarionVirtualList(0, 1, 0);
    private ElarionVirtualList actionList = new ElarionVirtualList(0, 1, 0);
    private String selectedTabId;
    private String selectedRowId;
    private String filter = "";
    private ElarionAdminPanelAction pendingAction;
    private ElarionTextInput input;
    private ElarionTextInput configEditInput;
    private String configEditInputTarget = "";

    public ElarionAdminPanelScreen(ElarionAdminPanelSnapshot snapshot) {
        super(Text.literal("Admin Panel"));
        this.snapshot = snapshot;
        this.selectedTabId = snapshot.selectedTabId().isBlank() ? "overview" : snapshot.selectedTabId();
        this.selectedRowId = snapshot.selectedRowId();
        selectFirstRowIfNeeded();
    }

    public void update(ElarionAdminPanelSnapshot snapshot) {
        this.snapshot = snapshot;
        if (!snapshot.selectedTabId().isBlank()) selectedTabId = snapshot.selectedTabId();
        if (!snapshot.selectedRowId().isBlank()) selectedRowId = snapshot.selectedRowId();
        selectFirstRowIfNeeded();
    }

    @Override
    protected void init() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        selectFirstRowIfNeeded();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 52);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 - 124, 21, true);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 + 124, 21, false);
        context.drawCenteredTextWithShadow(textRenderer, snapshot.title(), PANEL_WIDTH / 2, 13, style.titleColor());
        context.drawCenteredTextWithShadow(textRenderer, snapshot.subtitle(), PANEL_WIDTH / 2, 30, style.mutedColor());

        renderTabs(context, lx, ly, style);
        renderFilter(context, style);
        renderRows(context, lx, ly, style);
        renderDetail(context, lx, ly, style);
        renderMessage(context, style);
        renderModal(context, lx, ly, style);
        renderConfigEditShell(context, lx, ly, style);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        ElarionScaledLayout current = currentLayout();
        double lx = current.logicalX(mouseX);
        double ly = current.logicalY(mouseY);
        if (handleConfigEditShellClick(lx, ly)) return true;
        if (handleModalClick(lx, ly)) return true;
        List<ElarionAdminPanelTab> tabs = snapshot.tabs();
        for (int index = 0; index < tabs.size(); index++) {
            int x = tabX(index, tabs.size());
            if (inside(lx, ly, x, TAB_Y, tabWidth(tabs.size()), TAB_HEIGHT)) {
                selectedTabId = tabs.get(index).id();
                selectedRowId = "";
                filter = "";
                list = new ElarionVirtualList(0, 1, 0);
                actionList = new ElarionVirtualList(0, 1, 0);
                selectFirstRowIfNeeded();
                ClientPlayNetworking.send(new AdminPanelOpenRequestPayload(selectedTabId, selectedRowId));
                return true;
            }
        }

        List<ElarionAdminPanelRow> rows = filteredRows();
        int visibleRows = visibleRows();
        list.update(rows.size(), visibleRows, list.firstVisible());
        int rowIndex = itemAt(ly, rows.size());
        if (rowIndex >= 0 && inside(lx, ly, listX(), CONTENT_TOP, LIST_WIDTH, rowsHeight())) {
            selectedRowId = rows.get(rowIndex).id();
            list.select(rowIndex);
            actionList = new ElarionVirtualList(0, 1, 0);
            if (shouldRequestScopedRows(selectedTabId, selectedRowId)) {
                ClientPlayNetworking.send(new AdminPanelOpenRequestPayload(selectedTabId, selectedRowId));
            }
            return true;
        }

        ElarionAdminPanelRow row = selectedRow();
        if (row == null) return false;
        int x = detailX() + 10;
        int y = ACTION_TOP;
        List<ElarionAdminPanelAction> actions = row.actions();
        actionList.update(actions.size(), visibleActionCount(), actionList.firstVisible());
        for (int index = actionList.firstVisible(); index < actionList.lastVisibleExclusive(); index++) {
            ElarionAdminPanelAction action = actions.get(index);
            if (inside(lx, ly, x, y, DETAIL_WIDTH - 20, BUTTON_HEIGHT)) {
                if (action.enabled()) startAction(action);
                return true;
            }
            y += BUTTON_HEIGHT + ACTION_GAP;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        ElarionScaledLayout current = currentLayout();
        double lx = current.logicalX(mouseX);
        double ly = current.logicalY(mouseY);
        if (ElarionConfigEditClientState.openControl().isPresent()) return true;
        ElarionAdminPanelRow row = selectedRow();
        if (row != null && inside(lx, ly, detailX(), CONTENT_TOP - 24, DETAIL_WIDTH, rowsHeight() + 24)) {
            actionList.update(row.actions().size(), visibleActionCount(), actionList.firstVisible());
            actionList.scroll(verticalAmount > 0 ? -1 : 1);
            return true;
        }
        list.update(filteredRows().size(), visibleRows(), list.firstVisible());
        list.scroll(verticalAmount > 0 ? -1 : 1);
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        ElarionConfigEditControl configControl = configEditControl();
        if (configControl != null) {
            if (configControl.inputEditable() && configEditInput != null && configEditInput.type(chr)) {
                ElarionConfigEditClientState.clearLastResult();
            }
            return true;
        }
        if (input != null) return input.type(chr);
        if (ElarionTextInput.isAllowedTextCharacter(chr) && filter.length() < 36) {
            filter += chr;
            list = new ElarionVirtualList(0, 1, 0);
            actionList = new ElarionVirtualList(0, 1, 0);
            selectedRowId = "";
            selectFirstRowIfNeeded();
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ElarionConfigEditControl configControl = configEditControl();
        if (configControl != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeConfigEditShell();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                sendConfigEditValidation(configControl);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && configControl.inputEditable() && configEditInput != null) {
                if (configEditInput.backspace()) ElarionConfigEditClientState.clearLastResult();
                return true;
            }
            return true;
        }
        if (input != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelModal();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submitModal();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_TAB && cycleModalSuggestion((modifiers & GLFW.GLFW_MOD_SHIFT) != 0)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) return input.backspace();
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !filter.isEmpty()) {
            filter = filter.substring(0, filter.length() - 1);
            selectedRowId = "";
            actionList = new ElarionVirtualList(0, 1, 0);
            selectFirstRowIfNeeded();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S) {
            moveSelection(1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W) {
            moveSelection(-1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderTabs(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        int count = snapshot.tabs().size();
        int width = tabWidth(count);
        for (int index = 0; index < count; index++) {
            ElarionAdminPanelTab tab = snapshot.tabs().get(index);
            int x = tabX(index, count);
            boolean selected = tab.id().equals(selectedTabId);
            boolean hovered = inside(mouseX, mouseY, x, TAB_Y, width, TAB_HEIGHT);
            ElarionUiRenderer.compactButton(context, textRenderer, x, TAB_Y, width, TAB_HEIGHT,
                    tab.title(), hovered, selected, true, style);
        }
    }

    private void renderFilter(DrawContext context, ElarionUiStyle style) {
        ElarionCivicUi.thinBox(context, listX(), CONTENT_TOP - 24, LIST_WIDTH, 18,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        String text = filter.isBlank() ? "Type to filter..." : filter;
        ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, text, LIST_WIDTH - 12),
                listX() + 6, CONTENT_TOP - 19, filter.isBlank() ? style.mutedColor() : style.textColor(), false);
    }

    private void renderRows(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        ElarionCivicUi.thinBox(context, listX(), CONTENT_TOP, LIST_WIDTH, rowsHeight(),
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        List<ElarionAdminPanelRow> rows = filteredRows();
        if (rows.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "No matching rows.",
                    listX() + LIST_WIDTH / 2, CONTENT_TOP + rowsHeight() / 2 - 4, style.mutedColor());
            return;
        }
        list.update(rows.size(), visibleRows(), list.firstVisible());
        int y = CONTENT_TOP + 8;
        for (int index = list.firstVisible(); index < list.lastVisibleExclusive(); index++) {
            ElarionAdminPanelRow row = rows.get(index);
            int rowX = listX() + 8;
            boolean selected = row.id().equals(selectedRowId);
            boolean hovered = inside(mouseX, mouseY, rowX, y, LIST_WIDTH - 16, ROW_HEIGHT);
            if (row.danger()) {
                renderDangerRowSurface(context, rowX, y, LIST_WIDTH - 16, ROW_HEIGHT, selected, hovered, style);
            } else {
                ElarionCivicUi.rowSurface(context, rowX, y, LIST_WIDTH - 16, ROW_HEIGHT,
                        selected, hovered, false);
            }
            ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.title(), LIST_WIDTH - 112),
                    rowX + 10, y + 7,
                    row.danger() ? ElarionCivicColors.DESTRUCTIVE_TEXT : style.titleColor(), false);
            ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.subtitle(), LIST_WIDTH - 112),
                    rowX + 10, y + 22, style.textColor(), false);
            ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.state(), 78),
                    rowX + LIST_WIDTH - 96, y + 16, style.feedbackColor(), false);
            y += ROW_HEIGHT + ROW_GAP;
        }
    }

    private void renderDetail(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        int x = detailX();
        ElarionCivicUi.headerShell(context, x, CONTENT_TOP - 24, DETAIL_WIDTH, rowsHeight() + 24, 24);
        ElarionAdminPanelRow row = selectedRow();
        if (row == null) {
            context.drawCenteredTextWithShadow(textRenderer, "Select a row.",
                    x + DETAIL_WIDTH / 2, CONTENT_TOP + 64, style.mutedColor());
            return;
        }
        context.drawCenteredTextWithShadow(textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, row.title(), DETAIL_WIDTH - 16),
                x + DETAIL_WIDTH / 2, CONTENT_TOP - 15,
                row.danger() ? ElarionCivicColors.DESTRUCTIVE_TEXT : style.titleColor());
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(row.body()),
                x + 10, CONTENT_TOP + 8, DETAIL_WIDTH - 20, 90, style.textColor(), style.mutedColor());

        ElarionUiTypography.draw(context, textRenderer, "Actions", x + 10, ACTION_TOP - 14, style.titleColor(), false);
        int buttonY = ACTION_TOP;
        List<ElarionAdminPanelAction> actions = row.actions();
        actionList.update(actions.size(), visibleActionCount(), actionList.firstVisible());
        for (int index = actionList.firstVisible(); index < actionList.lastVisibleExclusive(); index++) {
            ElarionAdminPanelAction action = actions.get(index);
            boolean hovered = inside(mouseX, mouseY, x + 10, buttonY, DETAIL_WIDTH - 20, BUTTON_HEIGHT);
            String label = ElarionUiRenderer.ellipsize(textRenderer, action.label(), DETAIL_WIDTH - 32);
            ElarionCivicUi.compactActionButton(context, textRenderer, x + 10, buttonY,
                    DETAIL_WIDTH - 20, BUTTON_HEIGHT, label, hovered, false, action.enabled(),
                    actionTone(action.style()), style);
            buttonY += BUTTON_HEIGHT + ACTION_GAP;
        }
        if (actions.size() > visibleActionCount()) {
            drawRangeMarker(context, x + DETAIL_WIDTH / 2, CONTENT_BOTTOM - 9,
                    actionList.firstVisible() + 1, actionList.lastVisibleExclusive(), actions.size(),
                    style.mutedColor());
        }
    }

    private void renderMessage(DrawContext context, ElarionUiStyle style) {
        if (snapshot.message().isBlank()) return;
        context.drawCenteredTextWithShadow(textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, snapshot.message(), PANEL_WIDTH - 28),
                PANEL_WIDTH / 2, PANEL_HEIGHT - 18, style.feedbackColor());
    }

    private void renderModal(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        if (pendingAction == null) return;
        ActionModalLayout modal = actionModalLayout(pendingActionHasInput());
        context.fill(0, 0, PANEL_WIDTH, PANEL_HEIGHT, ElarionCivicColors.MODAL_OVERLAY);
        ElarionCivicUi.headerShell(context, modal.x(), modal.y(), modal.width(), modal.height(), 30);
        String title = pendingAction.requiresConfirmation()
                ? pendingAction.confirmationTitle()
                : pendingAction.parameterLabel();
        if (title.isBlank()) title = pendingAction.label();
        ElarionUiTypography.drawCentered(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, title, modal.width() - 42),
                modal.x() + modal.width() / 2, modal.y() + 12,
                "danger".equals(pendingAction.style()) ? ElarionCivicColors.DESTRUCTIVE_TEXT : style.titleColor(),
                false);
        String body = pendingAction.requiresConfirmation()
                ? pendingAction.confirmationBody()
                : "Enter " + pendingAction.parameterLabel() + ".";
        ElarionCivicUi.messageBody(context, modal.bodyX(), modal.bodyY(), modal.bodyWidth(), modal.bodyHeight(),
                "danger".equals(pendingAction.style()) ? ElarionCivicColors.REJECT_RED : ElarionCivicColors.GOLD_BORDER);
        ElarionUiTypography.wrappedClipped(context, textRenderer, Text.literal(body),
                modal.bodyX() + 8, modal.bodyY() + 8, modal.bodyWidth() - 16, modal.bodyHeight() - 12,
                style.textColor(), style.mutedColor());
        if (input != null) {
            ElarionCivicUi.thinBox(context, modal.inputX(), modal.inputY(), modal.inputWidth(), 18,
                    ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
            boolean empty = input.text().isBlank();
            String value = empty ? pendingAction.parameterPlaceholder()
                    : input.text() + (input.caretVisible() ? "_" : "");
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, value, modal.inputWidth() - 12),
                    modal.inputX() + 6, modal.inputY() + 5, empty ? style.mutedColor() : style.textColor(), false);
            if (pendingAction != null && !pendingAction.parameterSuggestions().isEmpty()) {
                String hint = "Tab: " + ElarionUiRenderer.ellipsize(textRenderer,
                        String.join(", ", pendingAction.parameterSuggestions()), modal.inputWidth() - 12);
                ElarionUiTypography.draw(context, textRenderer, hint, modal.inputX() + 6, modal.inputY() + 22,
                        style.mutedColor(), false);
            }
        }
        ElarionCivicUi.divider(context, modal.x() + 14, modal.buttonY() - 9, modal.width() - 28);
        ElarionCivicUi.compactActionButton(context, textRenderer, modal.cancelX(), modal.buttonY(),
                ACTION_MODAL_BUTTON_WIDTH, ACTION_MODAL_BUTTON_HEIGHT,
                "Cancel", inside(mouseX, mouseY, modal.cancelX(), modal.buttonY(),
                        ACTION_MODAL_BUTTON_WIDTH, ACTION_MODAL_BUTTON_HEIGHT), false, true,
                ElarionCivicUi.Tone.NORMAL, style);
        ElarionCivicUi.compactActionButton(context, textRenderer, modal.submitX(), modal.buttonY(),
                ACTION_MODAL_BUTTON_WIDTH, ACTION_MODAL_BUTTON_HEIGHT,
                pendingAction.requiresConfirmation() ? "Confirm" : "Submit",
                inside(mouseX, mouseY, modal.submitX(), modal.buttonY(),
                        ACTION_MODAL_BUTTON_WIDTH, ACTION_MODAL_BUTTON_HEIGHT),
                false, true, actionTone(pendingAction.style()), style);
    }

    private void renderConfigEditShell(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        ElarionConfigEditControl control = configEditControl();
        if (control == null) return;
        ConfigEditLayout modal = configEditLayout();
        int x = modal.x();
        int y = modal.y();
        int w = modal.width();
        context.fill(0, 0, PANEL_WIDTH, PANEL_HEIGHT, ElarionCivicColors.MODAL_OVERLAY);
        ElarionCivicUi.headerShell(context, x, y, w, modal.height(), modal.headerHeight());
        ElarionUiTypography.drawCentered(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, control.label(), w - 76),
                x + w / 2, y + ElarionCivicUi.centeredTextY(textRenderer, 0, modal.headerHeight()),
                style.titleColor(), false);
        ElarionCivicUi.compactActionButton(context, textRenderer, modal.topCloseX(), modal.topCloseY(),
                modal.topCloseWidth(), modal.topCloseHeight(),
                "Close", inside(mouseX, mouseY, modal.topCloseX(), modal.topCloseY(),
                        modal.topCloseWidth(), modal.topCloseHeight()), false, true,
                ElarionCivicUi.Tone.NORMAL, style);

        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(control.description()),
                modal.bodyX(), modal.descriptionY(), modal.bodyWidth(), modal.descriptionHeight(),
                style.textColor(), style.mutedColor());

        int lineY = modal.metadataY();
        lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Path", control.path());
        lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Current", control.currentDisplayValue());
        lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Default", control.defaultDisplayValue());
        lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Type",
                control.valueType().name().toLowerCase(Locale.ROOT));
        String bounds = boundsLabel(control);
        if (!bounds.isBlank()) {
            lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Bounds", bounds);
        }
        if (!control.choices().isEmpty()) {
            lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Choices",
                    String.join(", ", control.choices()));
        }
        lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Runtime",
                control.restartRequired() ? "Restart required" : control.runtimeReloadable() ? "Reload required" : "Static");
        lineY = renderConfigEditLine(context, style, modal.bodyX(), lineY, modal.bodyWidth(), "Permissions",
                "read " + control.readPermission().label() + " / write " + control.writePermission().label());

        int proposedY = Math.max(lineY + 4, modal.proposedY());
        ElarionUiTypography.draw(context, textRenderer, "Proposed:", modal.bodyX(), proposedY + 4, style.titleColor(), false);
        ElarionCivicUi.thinBox(context, modal.proposedInputX(), proposedY + 1, modal.proposedInputWidth(), 18,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        String proposed = configEditInput == null ? control.currentDisplayValue()
                : configEditInput.text() + (configEditInput.caretVisible() ? "_" : "");
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, proposed, modal.proposedInputWidth() - 12),
                modal.proposedInputX() + 6, proposedY + 6, configEditInput == null || configEditInput.text().isBlank()
                        ? style.mutedColor() : style.textColor(), false);

        int resultY = proposedY + 26;
        int resultHeight = Math.max(28, modal.buttonY() - 10 - resultY);
        ElarionCivicUi.messageBody(context, modal.bodyX(), resultY, modal.bodyWidth(), resultHeight,
                matchingConfigEditResult(control) == null
                        ? ElarionCivicColors.GOLD_SHADOW : ElarionCivicColors.ACTIVE_GREEN);
        ElarionUiRenderer.wrappedClipped(context, textRenderer,
                Text.literal(configEditResultText(control)),
                modal.bodyX() + 8, resultY + 8, modal.bodyWidth() - 16, resultHeight - 12,
                matchingConfigEditResult(control) == null ? style.mutedColor() : style.feedbackColor(),
                style.mutedColor());

        ElarionCivicUi.divider(context, x + 14, modal.buttonY() - 10, w - 28);
        ElarionCivicUi.compactActionButton(context, textRenderer, modal.validateX(), modal.buttonY(), modal.buttonWidth(), 20,
                "Validate", inside(mouseX, mouseY, modal.validateX(), modal.buttonY(), modal.buttonWidth(), 20), false,
                canValidateConfigEdit(), ElarionCivicUi.Tone.NORMAL, style);
        ElarionCivicUi.compactActionButton(context, textRenderer, modal.applyX(), modal.buttonY(), modal.buttonWidth(), 20,
                "Apply", inside(mouseX, mouseY, modal.applyX(), modal.buttonY(), modal.buttonWidth(), 20), false,
                canApplyConfigEdit(control), ElarionCivicUi.Tone.PRIMARY, style);
        ElarionCivicUi.compactActionButton(context, textRenderer, modal.closeX(), modal.buttonY(), modal.buttonWidth(), 20,
                "Close", inside(mouseX, mouseY, modal.closeX(), modal.buttonY(), modal.buttonWidth(), 20), false, true,
                ElarionCivicUi.Tone.NORMAL, style);
    }

    private int renderConfigEditLine(
            DrawContext context,
            ElarionUiStyle style,
            int x,
            int y,
            int width,
            String label,
            String value
    ) {
        String prefix = label + ": ";
        ElarionUiTypography.draw(context, textRenderer, prefix, x, y, style.titleColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, value == null || value.isBlank() ? "-" : value,
                        width - ElarionUiTypography.width(textRenderer, prefix)),
                x + ElarionUiTypography.width(textRenderer, prefix), y, style.textColor(), false);
        return y + 14;
    }

    private static ElarionCivicUi.Tone actionTone(String style) {
        return "danger".equals(style) ? ElarionCivicUi.Tone.DESTRUCTIVE : ElarionCivicUi.Tone.NORMAL;
    }

    private void renderDangerRowSurface(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            boolean selected,
            boolean hovered,
            ElarionUiStyle style
    ) {
        int fill = hovered ? ElarionCivicColors.BUTTON_DESTRUCTIVE_HOVER : ElarionCivicColors.BUTTON_DESTRUCTIVE;
        int border = selected ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.DESTRUCTIVE_BORDER;
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, border, style);
        if (selected) {
            context.fill(x + 2, y + 3, x + 4, y + height - 3, ElarionCivicColors.ACTIVE_GREEN);
        }
        if (width > 6 && height > 6) {
            context.fill(x + 3, y + 2, x + width - 3, y + 3, ElarionCivicColors.REJECT_RED);
        }
    }

    private static String boundsLabel(ElarionConfigEditControl control) {
        if (control.minimum().isBlank() && control.maximum().isBlank()) return "";
        return (control.minimum().isBlank() ? "*" : control.minimum())
                + ".." + (control.maximum().isBlank() ? "*" : control.maximum());
    }

    private boolean handleModalClick(double lx, double ly) {
        if (pendingAction == null) return false;
        ActionModalLayout modal = actionModalLayout(pendingActionHasInput());
        if (inside(lx, ly, modal.cancelX(), modal.buttonY(),
                ACTION_MODAL_BUTTON_WIDTH, ACTION_MODAL_BUTTON_HEIGHT)) {
            cancelModal();
            return true;
        }
        if (inside(lx, ly, modal.submitX(), modal.buttonY(),
                ACTION_MODAL_BUTTON_WIDTH, ACTION_MODAL_BUTTON_HEIGHT)) {
            submitModal();
            return true;
        }
        return inside(lx, ly, modal.x(), modal.y(), modal.width(), modal.height());
    }

    private boolean handleConfigEditShellClick(double lx, double ly) {
        ElarionConfigEditControl control = configEditControl();
        if (control == null) return false;
        ConfigEditLayout modal = configEditLayout();
        if (inside(lx, ly, modal.topCloseX(), modal.topCloseY(), modal.topCloseWidth(), modal.topCloseHeight())
                || inside(lx, ly, modal.closeX(), modal.buttonY(), modal.buttonWidth(), 20)) {
            closeConfigEditShell();
            return true;
        }
        if (inside(lx, ly, modal.validateX(), modal.buttonY(), modal.buttonWidth(), 20)) {
            sendConfigEditValidation(control);
            return true;
        }
        if (inside(lx, ly, modal.applyX(), modal.buttonY(), modal.buttonWidth(), 20)) {
            sendConfigEditApply(control);
            return true;
        }
        return true;
    }

    private ElarionConfigEditControl configEditControl() {
        ElarionConfigEditControl control = ElarionConfigEditClientState.openControl().orElse(null);
        if (control == null) {
            configEditInput = null;
            configEditInputTarget = "";
            return null;
        }
        String targetKey = control.target().targetKey();
        if (configEditInput == null || !targetKey.equals(configEditInputTarget)) {
            configEditInput = new ElarionTextInput(CONFIG_EDIT_VALUE_MAX, false);
            configEditInput.text(control.currentDisplayValue());
            configEditInput.focused(true);
            configEditInputTarget = targetKey;
        }
        return control;
    }

    private void closeConfigEditShell() {
        ElarionConfigEditClientState.closeOpenControl();
        configEditInput = null;
        configEditInputTarget = "";
    }

    private boolean canValidateConfigEdit() {
        return configEditInput != null
                && ElarionConfigEditClientState.openControl()
                .map(ElarionConfigEditControl::inputEditable)
                .orElse(false)
                && !configEditInput.text().isBlank();
    }

    private boolean canApplyConfigEdit(ElarionConfigEditControl control) {
        if (control == null || configEditInput == null || configEditInput.text().isBlank()) return false;
        if (!control.inputEditable() || !control.applyAvailable()) return false;
        ElarionConfigEditResultPayload result = matchingConfigEditResult(control);
        if (result == null) return false;
        String proposed = cleanConfigValue(configEditInput.text());
        return result.status() == panetina.elarion.core.config.ElarionConfigChangeResult.Status.VALIDATED
                && result.canApply()
                && result.errors().isEmpty()
                && cleanConfigValue(result.oldDisplayValue()).equals(cleanConfigValue(control.currentDisplayValue()))
                && cleanConfigValue(result.newDisplayValue()).equals(proposed);
    }

    private void sendConfigEditValidation(ElarionConfigEditControl control) {
        if (!canValidateConfigEdit()) return;
        sendConfigEdit(control, ElarionConfigEditRequestPayload.Intent.VALIDATE, CONFIG_EDIT_REASON);
    }

    private void sendConfigEditApply(ElarionConfigEditControl control) {
        if (!canApplyConfigEdit(control)) return;
        sendConfigEdit(control, ElarionConfigEditRequestPayload.Intent.APPLY, CONFIG_EDIT_APPLY_REASON);
    }

    private void sendConfigEdit(
            ElarionConfigEditControl control,
            ElarionConfigEditRequestPayload.Intent intent,
            String reason
    ) {
        ClientPlayNetworking.send(new ElarionConfigEditRequestPayload(
                control.target(),
                control.currentDisplayValue(),
                configEditInput.text(),
                reason,
                intent));
    }

    private ElarionConfigEditResultPayload matchingConfigEditResult(ElarionConfigEditControl control) {
        return ElarionConfigEditClientState.lastResult()
                .filter(result -> result.target().equals(control.target()))
                .orElse(null);
    }

    private String configEditResultText(ElarionConfigEditControl control) {
        ElarionConfigEditResultPayload result = matchingConfigEditResult(control);
        if (result == null) {
            if (!control.inputEditable()) return control.disabledReason();
            if (!control.applyAvailable() && !control.applyDisabledReason().isBlank()) {
                return "Enter a proposed value and validate it.\nApply unavailable: "
                        + control.applyDisabledReason();
            }
            return "Enter a proposed value and validate it.";
        }
        StringBuilder text = new StringBuilder(result.message());
        if (!result.oldDisplayValue().isBlank() || !result.newDisplayValue().isBlank()) {
            text.append("\nOld: ").append(result.oldDisplayValue().isBlank() ? "-" : result.oldDisplayValue())
                    .append(" -> New: ").append(result.newDisplayValue().isBlank() ? "-" : result.newDisplayValue());
        }
        if (result.reloadRequired() || result.restartRequired()) {
            text.append("\nPolicy: ").append(result.restartRequired() ? "Restart required" : "Reload required");
        }
        if (!result.auditPreview().isBlank()) {
            text.append("\n").append(result.auditPreview());
        }
        if (!result.errors().isEmpty()) {
            text.append("\n").append(result.errors().getFirst().message());
        }
        return text.toString();
    }

    private static String cleanConfigValue(String value) {
        return value == null ? "" : value.trim();
    }

    private void startAction(ElarionAdminPanelAction action) {
        if (action.requiresConfirmation() || !action.parameterKey().isBlank()) {
            pendingAction = action;
            input = action.parameterKey().isBlank() ? null : new ElarionTextInput(128, false);
            if (input != null) {
                input.focused(true);
            }
            return;
        }
        send(action, Map.of(), false);
    }

    private boolean cycleModalSuggestion(boolean reverse) {
        if (pendingAction == null || input == null || pendingAction.parameterSuggestions().isEmpty()) return false;
        List<String> suggestions = pendingAction.parameterSuggestions();
        String current = input.text().trim().toLowerCase(Locale.ROOT);
        List<String> matches = suggestions.stream()
                .filter(value -> current.isBlank() || value.toLowerCase(Locale.ROOT).startsWith(current)
                        || value.toLowerCase(Locale.ROOT).contains(current))
                .toList();
        if (matches.isEmpty()) matches = suggestions;
        int currentIndex = -1;
        for (int index = 0; index < suggestions.size(); index++) {
            if (suggestions.get(index).equalsIgnoreCase(input.text().trim())) {
                currentIndex = index;
                break;
            }
        }
        List<String> source = currentIndex >= 0 ? suggestions : matches;
        int index = currentIndex >= 0 ? currentIndex : -1;
        int next = reverse
                ? (index <= 0 ? source.size() - 1 : index - 1)
                : (index + 1) % source.size();
        input.text(source.get(next));
        return true;
    }

    private void submitModal() {
        if (pendingAction == null) return;
        Map<String, String> params = input == null ? Map.of() : Map.of(pendingAction.parameterKey(), input.text());
        send(pendingAction, params, pendingAction.requiresConfirmation());
        cancelModal();
    }

    private void cancelModal() {
        pendingAction = null;
        input = null;
    }

    private boolean pendingActionHasInput() {
        return pendingAction != null && !pendingAction.parameterKey().isBlank();
    }

    private void send(ElarionAdminPanelAction action, Map<String, String> params, boolean confirmed) {
        ElarionAdminPanelRow row = selectedRow();
        if (row == null) return;
        ClientPlayNetworking.send(new AdminPanelActionPayload(
                selectedTabId, action.providerId(), row.id(), action.id(), params, confirmed));
    }

    private void moveSelection(int direction) {
        List<ElarionAdminPanelRow> rows = filteredRows();
        if (rows.isEmpty()) return;
        int index = selectedIndex(rows);
        int next = Math.max(0, Math.min(rows.size() - 1, index + direction));
        selectedRowId = rows.get(next).id();
        list.select(next);
        actionList = new ElarionVirtualList(0, 1, 0);
    }

    private void selectFirstRowIfNeeded() {
        List<ElarionAdminPanelRow> rows = filteredRows();
        if (rows.isEmpty()) {
            selectedRowId = "";
            return;
        }
        if (rows.stream().noneMatch(row -> row.id().equals(selectedRowId))) {
            selectedRowId = rows.getFirst().id();
        }
    }

    private ElarionAdminPanelTab selectedTab() {
        return snapshot.tabs().stream()
                .filter(tab -> tab.id().equals(selectedTabId))
                .findFirst()
                .orElse(snapshot.tabs().isEmpty() ? null : snapshot.tabs().getFirst());
    }

    private ElarionAdminPanelRow selectedRow() {
        return filteredRows().stream().filter(row -> row.id().equals(selectedRowId)).findFirst().orElse(null);
    }

    private List<ElarionAdminPanelRow> filteredRows() {
        ElarionAdminPanelTab tab = selectedTab();
        if (tab == null) return List.of();
        String needle = filter.trim().toLowerCase(Locale.ROOT);
        if (needle.isBlank()) return tab.rows();
        return tab.rows().stream()
                .filter(row -> (row.title() + " " + row.subtitle() + " " + row.state())
                        .toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    private int selectedIndex(List<ElarionAdminPanelRow> rows) {
        for (int index = 0; index < rows.size(); index++) {
            if (rows.get(index).id().equals(selectedRowId)) return index;
        }
        return 0;
    }

    private int itemAt(double logicalY, int count) {
        int y = CONTENT_TOP + 8;
        for (int index = list.firstVisible(); index < Math.min(count, list.firstVisible() + visibleRows()); index++) {
            if (logicalY >= y && logicalY < y + ROW_HEIGHT) return index;
            y += ROW_HEIGHT + ROW_GAP;
        }
        return -1;
    }

    private int visibleRows() {
        return Math.max(1, (rowsHeight() - 16 + ROW_GAP) / (ROW_HEIGHT + ROW_GAP));
    }

    private int rowsHeight() {
        return CONTENT_BOTTOM - CONTENT_TOP;
    }

    static Layout layoutMetrics() {
        return new Layout(CONTENT_TOP, CONTENT_BOTTOM, LIST_WIDTH, DETAIL_WIDTH,
                ROW_HEIGHT, ROW_GAP, visibleRowsFor(CONTENT_BOTTOM - CONTENT_TOP),
                ACTION_TOP, visibleActionsFor(), tabWidth(6), tabX(5, 6) + tabWidth(6));
    }

    static ActionModalLayout actionModalLayout(boolean withInput) {
        int width = ACTION_MODAL_WIDTH;
        int height = withInput ? ACTION_MODAL_INPUT_HEIGHT : ACTION_MODAL_CONFIRM_HEIGHT;
        int bodyHeight = withInput ? 48 : 66;
        ElarionModalLayout.TwoButtonModal layout = ElarionModalLayout.twoButtonModal(
                PANEL_WIDTH,
                PANEL_HEIGHT,
                new ElarionModalLayout.Spec(
                        width,
                        height,
                        18,
                        36,
                        bodyHeight,
                        8,
                        30,
                        ACTION_MODAL_BUTTON_WIDTH,
                        ACTION_MODAL_BUTTON_HEIGHT,
                        16
                )
        );
        return new ActionModalLayout(
                layout.x(),
                layout.y(),
                layout.width(),
                layout.height(),
                layout.bodyX(),
                layout.bodyY(),
                layout.bodyWidth(),
                layout.bodyHeight(),
                layout.inputX(),
                layout.inputY(),
                layout.inputWidth(),
                layout.cancelX(),
                layout.submitX(),
                layout.buttonY()
        );
    }

    static ConfigEditLayout configEditLayout() {
        int x = CONFIG_MODAL_X;
        int y = CONFIG_MODAL_Y;
        int width = CONFIG_MODAL_WIDTH;
        int height = CONFIG_MODAL_HEIGHT;
        int headerHeight = 30;
        int bodyX = x + 18;
        int bodyWidth = width - 36;
        int descriptionY = y + 36;
        int descriptionHeight = 32;
        int metadataY = y + 78;
        int proposedY = y + 190;
        int proposedInputX = x + 94;
        int proposedInputWidth = width - 112;
        int buttonY = y + height - 34;
        int buttonWidth = 94;
        int buttonGap = 12;
        int totalButtonWidth = buttonWidth * 3 + buttonGap * 2;
        int validateX = x + (width - totalButtonWidth) / 2;
        int applyX = validateX + buttonWidth + buttonGap;
        int closeX = applyX + buttonWidth + buttonGap;
        return new ConfigEditLayout(
                x, y, width, height, headerHeight,
                x + width - 56, y + 8, 40, 18,
                bodyX, bodyWidth, descriptionY, descriptionHeight, metadataY,
                proposedY, proposedInputX, proposedInputWidth,
                validateX, applyX, closeX, buttonY, buttonWidth);
    }

    static int tabWidth(int tabCount) {
        int count = Math.max(1, tabCount);
        return Math.max(76, (PANEL_WIDTH - TAB_X * 2 - TAB_GAP * (count - 1)) / count);
    }

    private static int tabX(int index, int tabCount) {
        return TAB_X + index * (tabWidth(tabCount) + TAB_GAP);
    }

    private static int visibleRowsFor(int rowsHeight) {
        return Math.max(1, (rowsHeight - 16 + ROW_GAP) / (ROW_HEIGHT + ROW_GAP));
    }

    private int visibleActionCount() {
        return visibleActionsFor();
    }

    private static int visibleActionsFor() {
        int height = CONTENT_BOTTOM - ACTION_TOP - 14;
        return Math.max(1, (height + ACTION_GAP) / (BUTTON_HEIGHT + ACTION_GAP));
    }

    private void drawRangeMarker(
            DrawContext context,
            int centerX,
            int y,
            int first,
            int last,
            int total,
            int color
    ) {
        String label = "Rows " + first + "-" + last + " / " + total;
        int labelWidth = ElarionUiTypography.width(textRenderer, label);
        int left = centerX - (labelWidth + 16) / 2;
        drawRangeArrow(context, left, y + 4, false, color);
        ElarionUiTypography.draw(context, textRenderer, label, left + 8, y, color, false);
        drawRangeArrow(context, left + labelWidth + 10, y + 4, true, color);
    }

    private static void drawRangeArrow(DrawContext context, int x, int y, boolean up, int color) {
        if (up) {
            context.fill(x + 1, y, x + 2, y + 1, color);
            context.fill(x, y + 1, x + 3, y + 2, color);
            return;
        }
        context.fill(x, y, x + 3, y + 1, color);
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
    }

    private int listX() {
        return 16;
    }

    private int detailX() {
        return PANEL_WIDTH - DETAIL_WIDTH - 16;
    }

    private ElarionScaledLayout currentLayout() {
        return layout == null ? ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE) : layout;
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    static boolean shouldRequestScopedRows(String selectedTabId, String selectedRowId) {
        return "configs".equals(selectedTabId)
                && selectedRowId != null
                && selectedRowId.startsWith("config:");
    }

    record Layout(
            int contentTop,
            int contentBottom,
            int listWidth,
            int detailWidth,
            int rowHeight,
            int rowGap,
            int visibleRows,
            int actionTop,
            int visibleActions,
            int tabWidth,
            int tabRight
    ) {
        int contentHeight() {
            return contentBottom - contentTop;
        }
    }

    record ActionModalLayout(
            int x,
            int y,
            int width,
            int height,
            int bodyX,
            int bodyY,
            int bodyWidth,
            int bodyHeight,
            int inputX,
            int inputY,
            int inputWidth,
            int cancelX,
            int submitX,
            int buttonY
    ) {
        int bottom() {
            return y + height;
        }

        int buttonBottom() {
            return buttonY + ACTION_MODAL_BUTTON_HEIGHT;
        }
    }

    record ConfigEditLayout(
            int x,
            int y,
            int width,
            int height,
            int headerHeight,
            int topCloseX,
            int topCloseY,
            int topCloseWidth,
            int topCloseHeight,
            int bodyX,
            int bodyWidth,
            int descriptionY,
            int descriptionHeight,
            int metadataY,
            int proposedY,
            int proposedInputX,
            int proposedInputWidth,
            int validateX,
            int applyX,
            int closeX,
            int buttonY,
            int buttonWidth
    ) {
        int bottom() {
            return y + height;
        }

        int buttonBottom() {
            return buttonY + BUTTON_HEIGHT;
        }
    }
}
