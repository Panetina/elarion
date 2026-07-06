package panetina.elarion.addons.npcs.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.npcs.client.ui.ElarionConversationController;
import panetina.elarion.addons.npcs.client.ui.ElarionNpcPortraitRenderer;
import panetina.elarion.addons.npcs.client.ui.ElarionUiSound;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionNumericInput;
import panetina.elarion.core.client.ui.ElarionUiCard;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionVirtualList;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueDismissPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOptionPayload;
import panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueSelectPayload;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NpcDialogueScreen extends ElarionScreen {
    private static final String TYPING_SOUND = "minecraft:ui.button.click";
    private static final Map<String, Integer> SAVED_SCROLL = new ConcurrentHashMap<>();
    private static final Map<String, Integer> SAVED_SELECTION = new ConcurrentHashMap<>();

    private NpcDialogueOpenPayload dialogue;
    private ElarionConversationController conversation;
    private ElarionUiStyle style;
    private ElarionScaledLayout layout;
    private ElarionVirtualList optionList;

    private int padding;
    private int logicalWidth;
    private int logicalHeight;
    private int headerY;
    private int npcRowY;
    private int playerRowY;
    private int cardsY;
    private int optionY;
    private int optionHeight;
    private int footerY;
    private int footerHeight;
    private int optionRowHeight;
    private int scrollbarWidth;
    private ElarionConversationController.Phase lastPhase;
    private int lastTypingSoundIndex = -1;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;
    private NpcDialogueOptionPayload activePrompt;
    private ElarionNumericInput promptInput;
    private boolean relationshipHovered;

    public NpcDialogueScreen(NpcDialogueOpenPayload dialogue) {
        super(Text.literal(dialogue.npcName()));
        applyDialogue(dialogue);
    }

    public boolean belongsTo(NpcDialogueOpenPayload update) {
        return dialogue.npcId().equals(update.npcId());
    }

    public void updateDialogue(NpcDialogueOpenPayload update) {
        saveScroll();
        applyDialogue(update);
        recalculateLayout();
    }

    @Override
    protected void init() {
        recalculateLayout();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        relationshipHovered = false;
        updatePhaseSounds();
        playTypingTickSound();
        context.fill(0, 0, width, height, style.backgroundOverlayColor());

        double logicalMouseX = layout.logicalX(mouseX);
        double logicalMouseY = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, logicalWidth, logicalHeight, headerBandHeight() + 6);
        renderHeader(context, logicalMouseX, logicalMouseY);
        renderNpcRow(context);
        renderPlayerRow(context);
        if (!dialogue.cards().isEmpty()) {
            ElarionUiRenderer.cards(context, textRenderer, padding, cardsY,
                    logicalWidth - padding * 2, dialogue.cards().stream()
                            .map(card -> new ElarionUiCard(
                                    card.id(), card.label(), card.icon(), card.count(),
                                    card.currencyAmount(), card.disabled()))
                            .toList(), style);
        }
        renderOptions(context, logicalMouseX, logicalMouseY);
        renderPromptOverlay(context, logicalMouseX, logicalMouseY);
        renderFooter(context, logicalMouseX, logicalMouseY);

        context.getMatrices().pop();
        if (relationshipHovered) {
            context.drawTooltip(textRenderer, Text.literal(relationTooltip()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        if (!insidePanel(x, y)) return false;

        if (inside(x, y, closeX(), footerY, closeWidth(), footerHeight)) {
            close();
            return true;
        }
        if (activePrompt != null) {
            return true;
        }
        if (conversation.completeCurrentPhase()) return true;

        if (inside(x, y, scrollbarX(), optionY, scrollbarWidth, optionHeight)
                && optionList.maximumFirstVisible() > 0) {
            int thumbY = scrollbarThumbY();
            int thumbHeight = scrollbarThumbHeight();
            if (y >= thumbY && y < thumbY + thumbHeight) {
                draggingScrollbar = true;
                scrollbarDragOffset = (int) y - thumbY;
            } else {
                optionList.page(y < thumbY ? -1 : 1);
            }
            saveScroll();
            return true;
        }
        if (!conversation.canSubmit()) return true;

        int optionIndex = optionList.itemAt(y, optionY, optionRowHeight);
        if (optionIndex >= 0 && x >= padding && x < logicalWidth - padding - scrollbarWidth) {
            optionList.select(optionIndex);
            activateOption(optionIndex);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double deltaX,
            double deltaY
    ) {
        if (!draggingScrollbar || optionList.maximumFirstVisible() <= 0) return false;
        double y = layout.logicalY(mouseY);
        int travel = Math.max(1, optionHeight - scrollbarThumbHeight() - 2);
        int relative = clamp((int) y - scrollbarDragOffset - optionY - 1, 0, travel);
        int first = Math.round(relative * optionList.maximumFirstVisible() / (float) travel);
        if (optionList.setFirstVisible(first)) saveScroll();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = draggingScrollbar;
        draggingScrollbar = false;
        return wasDragging || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        if (inside(x, y, padding, optionY, logicalWidth - padding * 2, optionHeight)) {
            int direction = verticalAmount > 0.0D ? -1 : verticalAmount < 0.0D ? 1 : 0;
            if (direction != 0 && optionList.scroll(direction)) {
                saveScroll();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (activePrompt != null) {
                cancelPrompt();
                return true;
            }
            close();
            return true;
        }
        if (activePrompt != null) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submitPrompt();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && promptInput != null) {
                promptInput.backspace();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                promptInput.clear();
                return true;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER
                || keyCode == GLFW.GLFW_KEY_SPACE) {
            if (conversation.completeCurrentPhase()) return true;
            if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
                    && conversation.canSubmit() && optionList.selectedIndex() >= 0) {
                activateOption(optionList.selectedIndex());
                return true;
            }
        }
        if (!conversation.canSubmit()) return false;
        if (keyCode == GLFW.GLFW_KEY_UP) {
            optionList.moveSelection(-1);
            saveScroll();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            optionList.moveSelection(1);
            saveScroll();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            optionList.page(-1);
            saveScroll();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            optionList.page(1);
            saveScroll();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (activePrompt == null) return false;
        return promptInput != null && promptInput.type(chr);
    }

    @Override
    public void close() {
        saveScroll();
        ClientPlayNetworking.send(new NpcDialogueDismissPayload(dialogue.npcId()));
        super.close();
    }

    private void applyDialogue(NpcDialogueOpenPayload dialogue) {
        this.dialogue = dialogue;
        this.conversation = new ElarionConversationController(dialogue);
        this.style = ElarionUiStyle.from(ElarionUiThemes.variant(dialogue.themeVariant()));
        this.lastPhase = null;
        this.lastTypingSoundIndex = -1;
        this.activePrompt = null;
        this.promptInput = null;
    }

    private void recalculateLayout() {
        padding = clamp(dialogue.padding(), 8, 20);
        logicalWidth = clamp(dialogue.panelWidth(), 320, 560);
        logicalHeight = clamp(dialogue.maxPanelHeight(), Math.max(250, dialogue.minPanelHeight()), 440);
        layout = ElarionScaledLayout.fit(
                width, height, logicalWidth, logicalHeight, 8, dialogue.minimumUiScalePercent());

        int headerHeight = dialogue.showRelationBar() ? 30 : 18;
        int npcRowHeight = Math.max(clamp(dialogue.npcRowHeight(), 58, 110),
                clamp(dialogue.portraitSize(), 44, 80) + 8);
        int playerRowHeight = Math.max(clamp(dialogue.playerRowHeight(), 44, 90),
                clamp(dialogue.playerPortraitSize(), 28, 48) + 8);
        int cardsHeight = dialogue.cards().isEmpty() ? 0 : 30 + dialogue.contentGap();

        headerY = padding;
        npcRowY = padding + headerHeight + dialogue.contentGap();
        playerRowY = npcRowY + npcRowHeight + dialogue.contentGap();
        cardsY = playerRowY + playerRowHeight + dialogue.contentGap();
        footerHeight = clamp(dialogue.compactButtonHeight(), 14, 20);
        footerY = logicalHeight - padding - footerHeight;
        optionY = cardsY + cardsHeight;
        optionHeight = Math.max(dialogue.optionRowHeight(), footerY - dialogue.contentGap() - optionY);
        optionRowHeight = clamp(dialogue.optionRowHeight(), 14, 24);
        scrollbarWidth = clamp(dialogue.scrollbarWidth(), 4, 10);
        int rowsThatFit = Math.max(1, optionHeight / optionRowHeight);
        int visibleRows = Math.min(Math.max(1, dialogue.visibleOptionRows()), rowsThatFit);
        optionHeight = visibleRows * optionRowHeight;

        int saved = SAVED_SCROLL.getOrDefault(scrollKey(), 0);
        optionList = new ElarionVirtualList(dialogue.options().size(), visibleRows, saved);
        optionList.select(SAVED_SELECTION.getOrDefault(scrollKey(), optionList.selectedIndex()));
    }

    private void renderHeader(DrawContext context, double mouseX, double mouseY) {
        ElarionUiTypography.draw(context, textRenderer, dialogue.npcName(), padding, headerY, style.titleColor(), false);
        if (dialogue.hasCurrencyBalance()) {
            int badgeY = 3 + (headerBandHeight() - 30) / 2;
            ElarionUiRenderer.currencyBadge(context, textRenderer,
                    logicalWidth - padding - ElarionUiRenderer.CURRENCY_BADGE_WIDTH,
                    badgeY, dialogue.currencyBalance(), dialogue.currencyPlural(), style);
        }
        if (dialogue.showRelationBar()) {
            renderRelationshipHearts(context, padding, headerY + 13, mouseX, mouseY);
        }
    }

    private void renderNpcRow(DrawContext context) {
        int portraitSize = clamp(dialogue.portraitSize(), 44, 80);
        int portraitX = padding;
        int portraitY = npcRowY + Math.max(0, (dialogue.npcRowHeight() - portraitSize) / 2);
        int bubbleX = portraitX + portraitSize + dialogue.contentGap();
        int bubbleWidth = logicalWidth - padding - bubbleX;
        ElarionNpcPortraitRenderer.render(
                context, textRenderer, dialogue, portraitX, portraitY, portraitSize, style);
        ElarionUiRenderer.dialogueBox(
                context, textRenderer, bubbleX, npcRowY, bubbleWidth,
                Math.max(dialogue.npcRowHeight(), portraitSize), "", conversation.npcText(),
                dialogue.feedbackError() ? style.errorColor() : style.textColor(), style);
    }

    private void renderPlayerRow(DrawContext context) {
        int portraitSize = clamp(dialogue.playerPortraitSize(), 28, 48);
        int portraitX = logicalWidth - padding - portraitSize;
        int portraitY = playerRowY + Math.max(0, (dialogue.playerRowHeight() - portraitSize) / 2);
        int bubbleWidth = portraitX - dialogue.contentGap() - padding;
        ElarionUiRenderer.dialogueBox(
                context, textRenderer, padding, playerRowY, bubbleWidth,
                Math.max(dialogue.playerRowHeight(), portraitSize), "You", conversation.playerText(), style);
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            ElarionUiRenderer.portraitFrame(
                    context, textRenderer, portraitX, portraitY, portraitSize, "", style);
            PlayerSkinDrawer.draw(
                    context, player.getSkinTextures(), portraitX + 4, portraitY + 4, portraitSize - 8);
        } else {
            ElarionUiRenderer.portraitFrame(context, textRenderer, portraitX, portraitY, portraitSize, "You", style);
        }
    }

    private void renderOptions(DrawContext context, double mouseX, double mouseY) {
        boolean scrollable = dialogue.options().size() > optionList.visibleRows();
        int scrollbarReserve = scrollable ? scrollbarWidth + 3 : 0;
        int listWidth = logicalWidth - padding * 2 - scrollbarReserve;
        boolean active = conversation.canSubmit();
        boolean mouseDown = mouseDown();
        for (int index = optionList.firstVisible(); index < optionList.lastVisibleExclusive(); index++) {
            int row = index - optionList.firstVisible();
            int y = optionY + row * optionRowHeight;
            boolean hovered = active && inside(mouseX, mouseY, padding, y, listWidth, optionRowHeight - 2);
            boolean selected = active && optionList.selectedIndex() == index;
            ElarionCivicUi.compactActionButton(
                    context, textRenderer, padding, y, listWidth, optionRowHeight - 2,
                    dialogue.options().get(index).buttonText(), hovered || selected,
                    hovered && mouseDown, active, optionTone(active, selected), style);
        }
        renderScrollbar(context);
    }

    private void renderScrollbar(DrawContext context) {
        if (dialogue.options().size() <= optionList.visibleRows()) return;
        ElarionUiRenderer.scrollbar(
                context, scrollbarX(), optionY, scrollbarWidth, optionHeight,
                optionList.firstVisible(), optionList.visibleRows(), dialogue.options().size(),
                ElarionUiThemes.variant(dialogue.themeVariant()));
    }

    private void renderFooter(DrawContext context, double mouseX, double mouseY) {
        boolean hovered = inside(mouseX, mouseY, closeX(), footerY, closeWidth(), footerHeight);
        ElarionCivicUi.compactActionButton(
                context, textRenderer, closeX(), footerY, closeWidth(), footerHeight,
                "Close", hovered, hovered && mouseDown(), true, ElarionCivicUi.Tone.NORMAL, style);
    }

    private void submitOption(int index) {
        if (index < 0 || index >= dialogue.options().size() || !conversation.markSubmitted()) return;
        saveScroll();
        var option = dialogue.options().get(index);
        ClientPlayNetworking.send(new NpcDialogueSelectPayload(
                dialogue.npcId(), dialogue.nodeId(), option.id()));
    }

    private void activateOption(int index) {
        if (index < 0 || index >= dialogue.options().size()) return;
        var option = dialogue.options().get(index);
        if ("number".equals(option.promptType())) {
            openPrompt(option);
        } else {
            submitOption(index);
        }
    }

    private void openPrompt(NpcDialogueOptionPayload option) {
        activePrompt = option;
        promptInput = new ElarionNumericInput(promptMaxDigits());
    }

    private void cancelPrompt() {
        activePrompt = null;
        promptInput = null;
    }

    private void submitPrompt() {
        if (activePrompt == null || promptInput == null || promptInput.empty()
                || !conversation.markSubmitted()) return;
        saveScroll();
        ClientPlayNetworking.send(new NpcDialoguePromptSubmitPayload(
                dialogue.npcId(), dialogue.nodeId(), activePrompt.id(), promptInput.value()));
    }

    private void renderPromptOverlay(DrawContext context, double mouseX, double mouseY) {
        if (activePrompt == null) return;
        int x = padding;
        int y = playerRowY;
        int width = logicalWidth - padding * 2;
        int height = Math.max(dialogue.playerRowHeight(), 56);
        ElarionCivicUi.headerShell(context, x, y, width, height, 20);
        ElarionUiTypography.draw(context, textRenderer, activePrompt.promptQuestion(), x + 7, y + 6, style.titleColor(), false);
        int inputX = x + 7;
        int inputY = y + 22;
        int inputWidth = width - 14;
        int inputHeight = 16;
        ElarionCivicUi.thinBox(context, inputX, inputY, inputWidth, inputHeight,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        boolean caret = promptInput != null && promptInput.caretVisible();
        String value = promptInput == null ? "" : promptInput.value();
        ElarionUiTypography.draw(context, textRenderer, value + (caret ? "_" : ""),
                inputX + 4, inputY + 4, style.textColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Enter to confirm. Esc to cancel.",
                inputX, y + height - 14, style.mutedColor(), false);
    }

    private void updatePhaseSounds() {
        ElarionConversationController.Phase phase = conversation.phase();
        if (phase == lastPhase) return;
        lastPhase = phase;
        lastTypingSoundIndex = -1;
        if (phase == ElarionConversationController.Phase.PLAYER_TYPING) {
            ElarionUiSound.play(dialogue.playerSound());
        } else if (phase == ElarionConversationController.Phase.NPC_TYPING) {
            ElarionUiSound.play(dialogue.npcSound());
        }
    }

    private void playTypingTickSound() {
        if (!dialogue.typingSoundEnabled()
                || conversation.phase() == ElarionConversationController.Phase.AWAITING_INPUT) {
            return;
        }
        int index = conversation.typedIntervalIndex(dialogue.typingSoundIntervalCharacters());
        if (index != lastTypingSoundIndex) {
            lastTypingSoundIndex = index;
            ElarionUiSound.play(TYPING_SOUND);
        }
    }

    private void saveScroll() {
        if (optionList == null) return;
        SAVED_SCROLL.put(scrollKey(), optionList.firstVisible());
        SAVED_SELECTION.put(scrollKey(), optionList.selectedIndex());
    }

    private String scrollKey() {
        return dialogue.npcId() + "|" + dialogue.dialogueId() + "|" + dialogue.nodeId();
    }

    private boolean insidePanel(double x, double y) {
        return x >= 0 && y >= 0 && x < logicalWidth && y < logicalHeight;
    }

    private int closeWidth() {
        return Math.min(84, (logicalWidth - padding * 2) / 3);
    }

    private int headerBandHeight() {
        return Math.max(24, npcRowY - dialogue.contentGap() - 3);
    }

    private static ElarionCivicUi.Tone optionTone(boolean active, boolean selected) {
        if (!active) return ElarionCivicUi.Tone.MUTED;
        return selected ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.NORMAL;
    }

    private int closeX() {
        return (logicalWidth - closeWidth()) / 2;
    }

    private int promptMaxDigits() {
        return activePrompt == null ? 10 : clamp(activePrompt.promptMaxDigits(), 1, 10);
    }

    private int scrollbarX() {
        return logicalWidth - padding - scrollbarWidth;
    }

    private int scrollbarThumbHeight() {
        if (dialogue.options().isEmpty()) return optionHeight;
        return Math.max(8, optionHeight * optionList.visibleRows() / dialogue.options().size());
    }

    private int scrollbarThumbY() {
        int travel = Math.max(0, optionHeight - scrollbarThumbHeight() - 2);
        return optionY + 1 + travel * optionList.firstVisible()
                / Math.max(1, optionList.maximumFirstVisible());
    }

    private void renderRelationshipHearts(
            DrawContext context, int x, int y, double mouseX, double mouseY
    ) {
        int halfHearts = Math.round((clamp(dialogue.relationValue(), -100, 100) + 100) / 20.0F);
        int heartX = x;
        for (int index = 0; index < 5; index++) {
            int filledHalves = clamp(halfHearts - index * 2, 0, 2);
            drawHeart(context, heartX + index * 11, y, filledHalves);
        }
        relationshipHovered = inside(mouseX, mouseY, heartX, y, 53, 9);
    }

    private String relationTooltip() {
        String label = dialogue.relationLabel();
        if (label == null || label.isBlank()) return "Neutral";
        int separator = label.indexOf(':');
        return separator >= 0 && separator + 1 < label.length()
                ? label.substring(separator + 1).trim()
                : label.trim();
    }

    private void drawHeart(DrawContext context, int x, int y, int filledHalves) {
        int top = y;
        int shadow = style.bevelShadowColor();
        int empty = style.buttonDisabledColor();
        int fill = style.titleColor();
        drawHeartShape(context, x + 1, top + 1, shadow, 9);
        drawHeartShape(context, x, top, empty, 9);
        if (filledHalves == 2) {
            drawHeartShape(context, x, top, fill, 9);
        } else if (filledHalves == 1) {
            drawHalfHeartShape(context, x, top, fill);
        }
    }

    private static void drawHeartShape(DrawContext context, int x, int y, int color, int size) {
        if (size != 9) return;
        context.fill(x + 1, y, x + 4, y + 1, color);
        context.fill(x + 5, y, x + 8, y + 1, color);
        context.fill(x, y + 1, x + 9, y + 5, color);
        context.fill(x + 1, y + 5, x + 8, y + 6, color);
        context.fill(x + 2, y + 6, x + 7, y + 7, color);
        context.fill(x + 3, y + 7, x + 6, y + 8, color);
        context.fill(x + 4, y + 8, x + 5, y + 9, color);
    }

    private static void drawHalfHeartShape(DrawContext context, int x, int y, int color) {
        context.fill(x + 1, y, x + 4, y + 1, color);
        context.fill(x, y + 1, x + 5, y + 5, color);
        context.fill(x + 1, y + 5, x + 5, y + 6, color);
        context.fill(x + 2, y + 6, x + 5, y + 7, color);
        context.fill(x + 3, y + 7, x + 5, y + 8, color);
        context.fill(x + 4, y + 8, x + 5, y + 9, color);
    }

    private static boolean mouseDown() {
        MinecraftClient client = MinecraftClient.getInstance();
        return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && y >= top && x < left + width && y < top + height;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
