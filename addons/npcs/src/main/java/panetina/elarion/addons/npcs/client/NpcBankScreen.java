package panetina.elarion.addons.npcs.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.npcs.client.ui.ElarionNpcPortraitRenderer;
import panetina.elarion.addons.npcs.network.NpcDialogueDismissPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOptionPayload;
import panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueSelectPayload;
import panetina.elarion.addons.npcs.network.NpcBankQuotePayload;
import panetina.elarion.addons.npcs.network.NpcBankQuoteRequestPayload;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionFooterActionLayout;
import panetina.elarion.core.client.ui.ElarionIconLabelLineLayout;
import panetina.elarion.core.client.ui.ElarionInputFieldLayout;
import panetina.elarion.core.client.ui.ElarionNumericInput;
import panetina.elarion.core.client.ui.ElarionPanelHeaderLayout;
import panetina.elarion.core.client.ui.ElarionPairedButtonLayout;
import panetina.elarion.core.client.ui.ElarionPresetButtonRowLayout;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionSemanticRowLayout;
import panetina.elarion.core.client.ui.ElarionServiceHeaderLayout;
import panetina.elarion.core.client.ui.ElarionSplitSummaryLayout;
import panetina.elarion.core.client.ui.ElarionStatusLineLayout;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcBankScreen extends ElarionScreen {
    private static final int LOGICAL_WIDTH = 500;
    private static final int LOGICAL_HEIGHT = 300;
    private static final int PADDING = 14;
    private static final int HEADER_HEIGHT = 76;
    private static final ElarionServiceHeaderLayout.PortraitTitle SERVICE_HEADER =
            ElarionServiceHeaderLayout.portraitTitle(0, 0, LOGICAL_WIDTH, HEADER_HEIGHT,
                    PADDING, 12, 52, 78, 18, 40, 18,
                    ElarionUiRenderer.CURRENCY_BADGE_WIDTH, 14, 28);
    private static final ElarionPairedButtonLayout.Pair MODE_BUTTONS =
            ElarionPairedButtonLayout.pair(PADDING, 90, 226, 236, 10, 24);
    private static final ElarionFooterActionLayout.Action FOOTER_ACTION =
            ElarionFooterActionLayout.action(PADDING, 264, 156, 22);
    private static final ElarionPresetButtonRowLayout.PresetConfirmRow AMOUNT_ACTIONS =
            ElarionPresetButtonRowLayout.presetConfirmRow(24, 184, 90, 22, 10, 3, 12, 160);
    private static final ElarionSplitSummaryLayout.Split AMOUNT_SUMMARY =
            ElarionSplitSummaryLayout.split(24, 216, 452, 223, 254);
    private static final String[] PRESET_AMOUNTS = {"100", "1000", "10000"};
    private static final String[] PRESET_LABELS = {"+100", "+1K", "+10K"};
    private static final Map<UUID, String> LAST_ROLE_BY_NPC = new ConcurrentHashMap<>();

    private NpcDialogueOpenPayload dialogue;
    private ElarionUiStyle style;
    private ElarionScaledLayout layout;
    private NpcDialogueOptionPayload activeOption;
    private ElarionNumericInput amountInput;
    private NpcBankQuotePayload latestQuote;

    public NpcBankScreen(NpcDialogueOpenPayload dialogue) {
        super(Text.literal(dialogue.npcName() + " Bank"));
        updateDialogue(dialogue);
    }

    public boolean belongsTo(NpcDialogueOpenPayload update) {
        return dialogue.npcId().equals(update.npcId());
    }

    public boolean belongsTo(NpcBankQuotePayload update) {
        return dialogue.npcId().equals(update.npcId())
                && dialogue.nodeId().equals(update.nodeId());
    }

    public void updateQuote(NpcBankQuotePayload update) {
        if (quoteMatchesInput(update)) latestQuote = update;
    }

    public void updateDialogue(NpcDialogueOpenPayload update) {
        dialogue = update;
        style = ElarionUiStyle.from(ElarionUiThemes.variant(update.themeVariant()));
        selectDefaultOption();
    }

    @Override
    protected void init() {
        layout = ElarionScaledLayout.fit(width, height, LOGICAL_WIDTH, LOGICAL_HEIGHT, 8,
                dialogue.minimumUiScalePercent());
        selectDefaultOption();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, HEADER_HEIGHT);
        renderHeader(context);
        renderModeButtons(context, x, y);
        renderAmountPanel(context, x, y);
        renderStatus(context);
        renderFooter(context, x, y);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        if (!inside(x, y, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT)) return false;
        if (SERVICE_HEADER.close().contains(x, y)) {
            close();
            return true;
        }
        if (clickRole(x, y, "deposit", MODE_BUTTONS.left())) return true;
        if (clickRole(x, y, "withdraw", MODE_BUTTONS.right())) return true;
        int presetIndex = AMOUNT_ACTIONS.presets().hitIndex(x, y);
        if (presetIndex >= 0) {
            setAmount(PRESET_AMOUNTS[presetIndex]);
            return true;
        }
        if (AMOUNT_ACTIONS.confirm().contains(x, y)) {
            submitActive();
            return true;
        }
        if (FOOTER_ACTION.button().contains(x, y)) {
            NpcDialogueOptionPayload back = role("back");
            if (back != null) select(back);
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            NpcDialogueOptionPayload back = role("back");
            if (back != null) select(back); else close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submitActive();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && amountInput != null) {
            amountInput.backspace();
            requestQuote();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE && amountInput != null) {
            amountInput.clear();
            requestQuote();
            return true;
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (amountInput == null || !amountInput.type(chr)) return false;
        requestQuote();
        return true;
    }

    @Override
    public void close() {
        ClientPlayNetworking.send(new NpcDialogueDismissPayload(dialogue.npcId()));
        super.close();
    }

    private void renderHeader(DrawContext context) {
        ElarionServiceHeaderLayout.PortraitTitle header = SERVICE_HEADER;
        ElarionNpcPortraitRenderer.render(context, textRenderer, dialogue,
                header.portrait().x(), header.portrait().y(), header.portrait().width(), style);
        drawTitle(context, dialogue.npcName(), header.titleX(), header.titleY(), header.titleMaxWidth());
        ElarionUiTypography.draw(context, textRenderer, "Banking Services", header.titleX(), header.subtitleY(),
                style.mutedColor(), false);
        if (dialogue.hasCurrencyBalance()) {
            ElarionUiRenderer.currencyBadge(context, textRenderer, header.badge().x(), header.badge().y(),
                    dialogue.currencyBalance(), dialogue.currencyPlural(), style);
        }
        ElarionCivicUi.closeButton(context, header.close().x(), header.close().y(), header.close().width());
    }

    private void renderModeButtons(DrawContext context, double mouseX, double mouseY) {
        renderRoleButton(context, mouseX, mouseY, "deposit", MODE_BUTTONS.left(), "Deposit");
        renderRoleButton(context, mouseX, mouseY, "withdraw", MODE_BUTTONS.right(), "Withdraw");
    }

    private void renderAmountPanel(DrawContext context, double mouseX, double mouseY) {
        ElarionPanelHeaderLayout.LeftTitle header =
                ElarionPanelHeaderLayout.leftTitle(PADDING, 122, 472, 126,
                        24, 10, 8, 10, 34);
        ElarionCivicUi.headerShell(context, header.bounds().x(), header.bounds().y(),
                header.bounds().width(), header.bounds().height(), header.headerHeight());
        String mode = activeOption == null ? "Bank" : title(activeOption.presentationRole());
        ElarionUiTypography.draw(context, textRenderer, mode + " " + dialogue.currencyPlural(),
                header.titleX(), header.titleY(), style.titleColor(), false);
        ElarionCivicUi.thinBox(context, 24, 154, 290, 22,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        ElarionInputFieldLayout.SingleLine inputLayout =
                ElarionInputFieldLayout.singleLine(24, 154, 290, 22, 4, 16, 4, true);
        ElarionUiRenderer.currencyIcon(context, inputLayout.icon().x(), inputLayout.icon().y(),
                inputLayout.icon().width());
        String value = amountInput == null ? "" : amountInput.value();
        boolean placeholder = value.isBlank();
        if (placeholder) value = "Enter amount";
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, value, inputLayout.textMaxWidth()),
                inputLayout.textX(), inputLayout.textY(textRenderer),
                placeholder ? style.mutedColor() : style.textColor(), false);
        renderAmountCaret(context, inputLayout, placeholder ? "" : value);

        for (int index = 0; index < PRESET_LABELS.length; index++) {
            presetButton(context, mouseX, mouseY, AMOUNT_ACTIONS.presets().button(index), PRESET_LABELS[index]);
        }
        boolean validQuote = currentQuote() != null && currentQuote().valid();
        ElarionSemanticRowLayout.Rect confirm = AMOUNT_ACTIONS.confirm();
        boolean confirmHovered = confirm.contains(mouseX, mouseY);
        ElarionCivicUi.compactActionButton(context, textRenderer,
                confirm.x(), confirm.y(), confirm.width(), confirm.height(),
                "Confirm " + mode, confirmHovered, false,
                activeOption != null && validQuote,
                ElarionCivicUi.Tone.PRIMARY, style);

        ElarionCivicUi.divider(context, AMOUNT_SUMMARY.divider().x(),
                AMOUNT_SUMMARY.divider().y(), AMOUNT_SUMMARY.divider().width());
        NpcBankQuotePayload quote = currentQuote();
        drawCurrencyPair(context, "Fee", quote == null ? "0" : Long.toString(quote.fee()),
                AMOUNT_SUMMARY.leftX(), AMOUNT_SUMMARY.leftY(), style.mutedColor(), style.textColor());
        drawCurrencyPair(context, "Total", quote == null ? "0" : Long.toString(quote.total()),
                AMOUNT_SUMMARY.rightX(), AMOUNT_SUMMARY.rightY(), style.titleColor(), style.titleColor());
        if (quote != null && !quote.valid() && !quote.message().isBlank()) {
            ElarionStatusLineLayout.SingleLine status =
                    ElarionStatusLineLayout.singleLine(188, 270, 260,
                            ElarionUiTypography.lineHeight(), ElarionUiTypography.lineHeight());
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiTypography.ellipsize(textRenderer, quote.message(), status.textMaxWidth()),
                    status.textX(), status.textY(), style.errorColor(), false);
        }
    }

    private void renderStatus(DrawContext context) {
        if (dialogue.feedback() == null || dialogue.feedback().isBlank()) return;
        int color = dialogue.feedbackError() ? style.errorColor() : ElarionCivicColors.ACTIVE_GREEN;
        ElarionStatusLineLayout.SingleLine status =
                ElarionStatusLineLayout.singleLine(178, 270, 300,
                        ElarionUiTypography.lineHeight(), ElarionUiTypography.lineHeight());
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, dialogue.feedback(), status.textMaxWidth()),
                status.textX(), status.textY(), color, false);
    }

    private void renderFooter(DrawContext context, double mouseX, double mouseY) {
        ElarionSemanticRowLayout.Rect button = FOOTER_ACTION.button();
        boolean hovered = button.contains(mouseX, mouseY);
        ElarionCivicUi.compactActionButton(context, textRenderer,
                button.x(), button.y(), button.width(), button.height(),
                "Back to Conversation", hovered, false, role("back") != null,
                ElarionCivicUi.Tone.NORMAL, style);
    }

    private void renderRoleButton(
            DrawContext context, double mouseX, double mouseY, String role,
            ElarionSemanticRowLayout.Rect button, String label
    ) {
        NpcDialogueOptionPayload option = role(role);
        boolean selected = activeOption != null && role.equals(activeOption.presentationRole());
        boolean hovered = button.contains(mouseX, mouseY);
        ElarionCivicUi.compactActionButton(context, textRenderer,
                button.x(), button.y(), button.width(), button.height(),
                label, hovered || selected, false, option != null,
                selected ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.NORMAL, style);
    }

    private void presetButton(DrawContext context, double mouseX, double mouseY,
                              ElarionSemanticRowLayout.Rect button, String label) {
        boolean hovered = button.contains(mouseX, mouseY);
        ElarionCivicUi.compactActionButton(context, textRenderer,
                button.x(), button.y(), button.width(), button.height(),
                label, hovered, false, activeOption != null, ElarionCivicUi.Tone.NORMAL, style);
    }

    private void renderAmountCaret(
            DrawContext context,
            ElarionInputFieldLayout.SingleLine inputLayout,
            String inputValue
    ) {
        if (amountInput == null || !amountInput.caretVisible()) return;
        int caretX = inputLayout.caretX(textRenderer, inputValue);
        int top = inputLayout.bounds().y() + Math.max(1, (inputLayout.bounds().height() - 12) / 2);
        context.fill(caretX, top, caretX + 1, top + 12, style.textColor());
    }

    private void drawCurrencyPair(
            DrawContext context,
            String label,
            String value,
            int x,
            int y,
            int labelColor,
            int valueColor
    ) {
        ElarionIconLabelLineLayout.CompactLine line =
                ElarionIconLabelLineLayout.compactCurrency(x, y, ElarionUiTypography.width(textRenderer, label), 12);
        ElarionUiTypography.draw(context, textRenderer, label, line.labelX(), line.labelY(), labelColor, false);
        ElarionUiRenderer.currencyIcon(context, line.icon().x(), line.icon().y(), line.icon().width());
        ElarionUiTypography.draw(context, textRenderer, value, line.valueX(), line.valueY(), valueColor, false);
    }

    private boolean clickRole(double mouseX, double mouseY, String role, ElarionSemanticRowLayout.Rect button) {
        if (!button.contains(mouseX, mouseY)) return false;
        NpcDialogueOptionPayload option = role(role);
        if (option != null) {
            activeOption = option;
            amountInput = new ElarionNumericInput(option.promptMaxDigits());
            latestQuote = null;
            LAST_ROLE_BY_NPC.put(dialogue.npcId(), role);
            requestQuote();
        }
        return true;
    }

    private void selectDefaultOption() {
        String preferredRole = activeOption == null || activeOption.presentationRole().isBlank()
                ? LAST_ROLE_BY_NPC.getOrDefault(dialogue.npcId(), "deposit")
                : activeOption.presentationRole();
        NpcDialogueOptionPayload preferred = role(preferredRole);
        if (preferred == null) preferred = role("deposit");
        if (preferred == null) preferred = role("withdraw");
        activeOption = preferred;
        if (preferred != null && !preferred.presentationRole().isBlank()) {
            LAST_ROLE_BY_NPC.put(dialogue.npcId(), preferred.presentationRole());
        }
        amountInput = new ElarionNumericInput(preferred == null ? 10 : preferred.promptMaxDigits());
        latestQuote = null;
    }

    private NpcDialogueOptionPayload role(String role) {
        return dialogue.options().stream()
                .filter(option -> role.equals(option.presentationRole()))
                .findFirst()
                .orElse(null);
    }

    private void submitActive() {
        if (activeOption == null || currentQuote() == null || !currentQuote().valid()) return;
        if (!activeOption.presentationRole().isBlank()) {
            LAST_ROLE_BY_NPC.put(dialogue.npcId(), activeOption.presentationRole());
        }
        ClientPlayNetworking.send(new NpcDialoguePromptSubmitPayload(
                dialogue.npcId(), dialogue.nodeId(), activeOption.id(), amountInput.value()));
    }

    private void select(NpcDialogueOptionPayload option) {
        ClientPlayNetworking.send(new NpcDialogueSelectPayload(
                dialogue.npcId(), dialogue.nodeId(), option.id()));
    }

    private void setAmount(String amount) {
        if (amountInput == null) amountInput = new ElarionNumericInput(10);
        amountInput.clear();
        for (int index = 0; index < amount.length(); index++) amountInput.type(amount.charAt(index));
        requestQuote();
    }

    private void requestQuote() {
        latestQuote = null;
        if (activeOption == null || amountInput == null || amountInput.empty()) return;
        int amount;
        try {
            amount = Integer.parseInt(amountInput.value());
        } catch (NumberFormatException exception) {
            return;
        }
        ClientPlayNetworking.send(new NpcBankQuoteRequestPayload(
                dialogue.npcId(), dialogue.nodeId(), activeOption.presentationRole(), amount));
    }

    private NpcBankQuotePayload currentQuote() {
        return latestQuote != null && quoteMatchesInput(latestQuote) ? latestQuote : null;
    }

    private boolean quoteMatchesInput(NpcBankQuotePayload quote) {
        if (quote == null || activeOption == null || amountInput == null || amountInput.empty()) return false;
        if (!dialogue.npcId().equals(quote.npcId()) || !dialogue.nodeId().equals(quote.nodeId())) return false;
        if (!activeOption.presentationRole().equals(quote.mode())) return false;
        try {
            return Integer.parseInt(amountInput.value()) == quote.amount();
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void drawTitle(DrawContext context, String title, int x, int y, int maxWidth) {
        float scale = 1.25F * ElarionUiTypography.scale();
        int unscaledWidth = Math.max(1, (int) Math.floor(maxWidth / scale));
        String visible = textRenderer.trimToWidth(title == null ? "" : title, unscaledWidth);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(textRenderer, visible, 0, 0, style.titleColor(), false);
        context.getMatrices().pop();
    }

    private static String title(String role) {
        if (role == null || role.isBlank()) return "Bank";
        return Character.toUpperCase(role.charAt(0)) + role.substring(1);
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && y >= top && x < left + width && y < top + height;
    }
}
