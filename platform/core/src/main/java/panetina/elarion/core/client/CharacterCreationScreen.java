package panetina.elarion.core.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionSectionHeaderLayout;
import panetina.elarion.core.client.ui.ElarionTextInput;
import panetina.elarion.core.client.ui.ElarionTextViewportLayout;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.network.CharacterCreationRequirementPayload;
import panetina.elarion.core.network.CharacterCreationSubmitPayload;

import java.util.ArrayList;
import java.util.List;

public final class CharacterCreationScreen extends ElarionScreen {
    static final int PANEL_WIDTH = 680;
    static final int PANEL_HEIGHT = 398;
    static final int BUTTON_WIDTH = 154;
    static final int BUTTON_HEIGHT = 24;
    static final int FOOTER_Y = PANEL_HEIGHT - 50;
    static final int FOOTER_HEIGHT = 38;
    static final int PRIMARY_BUTTON_X = PANEL_WIDTH - BUTTON_WIDTH - 30;
    static final int PRIMARY_BUTTON_Y = FOOTER_Y + (FOOTER_HEIGHT - BUTTON_HEIGHT) / 2;
    static final String PRIMARY_BUTTON_LABEL = "Continue";
    static final int NAME_X = 40;
    static final int NAME_Y = 151;
    static final int NAME_WIDTH = 228;
    static final int NAME_HEIGHT = 24;
    static final int BIO_X = 312;
    static final int BIO_Y = 151;
    static final int BIO_WIDTH = 328;
    static final int BIO_HEIGHT = 124;
    private static final float MAX_SCALE = 0.9F;
    private static final int NAME_MAX_LENGTH = 32;
    private static final int BIO_MAX_LENGTH = 500;

    private CharacterCreationRequirementPayload requirement;
    private ElarionScaledLayout layout;
    private final ElarionTextInput nameInput = new ElarionTextInput(NAME_MAX_LENGTH, false);
    private final ElarionTextInput biographyInput = new ElarionTextInput(BIO_MAX_LENGTH, true);
    private String feedback;

    public CharacterCreationScreen(CharacterCreationRequirementPayload requirement) {
        super(Text.literal("Create Character"));
        this.requirement = requirement;
        this.feedback = requirement.feedback();
        this.nameInput.text(requirement.prefilledName());
        this.biographyInput.text(requirement.prefilledBiography());
    }

    @Override
    protected void init() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        nameInput.focused(true);
        biographyInput.focused(false);
    }

    public void updateRequirement(CharacterCreationRequirementPayload payload) {
        boolean sameSession = requirement != null && requirement.nonce().equals(payload.nonce());
        this.requirement = payload;
        this.feedback = payload.feedback();
        if (!sameSession) {
            this.nameInput.text(payload.prefilledName());
            this.biographyInput.text(payload.prefilledBiography());
            this.biographyInput.scrollLine(0);
            this.biographyInput.focused(false);
            this.nameInput.focused(true);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        double logicalMouseX = layout.logicalX(mouseX);
        double logicalMouseY = layout.logicalY(mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 52);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 - 132, 22, true);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 + 132, 22, false);
        context.drawCenteredTextWithShadow(textRenderer,
                cooldownActive() ? "Character Recreation Cooldown" : "Create Your Character",
                PANEL_WIDTH / 2, 13,
                style.titleColor());
        context.drawCenteredTextWithShadow(textRenderer,
                cooldownActive()
                        ? "A new character will be available after the cooldown."
                        : "Your character identity is separate from your Minecraft account.",
                PANEL_WIDTH / 2, 30, style.mutedColor());

        if (cooldownActive()) {
            renderCooldown(context, style);
            context.getMatrices().pop();
            return;
        }

        renderStepStrip(context, style);
        context.drawCenteredTextWithShadow(textRenderer,
                "Tell us who you are. Realm placement happens after your biography and is balanced by the server.",
                PANEL_WIDTH / 2, 85, style.mutedColor());

        renderIdentityPanel(context, style);
        renderBiographyPanel(context, style);
        renderBottomBand(context, style, logicalMouseX, logicalMouseY);
        context.getMatrices().pop();
    }

    private void renderIdentityPanel(DrawContext context, ElarionUiStyle style) {
        ElarionCivicUi.thinBox(context, 24, 108, 260, 214,
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        drawPanelHeader(context, 24, 108, 260, "IDENTITY", "identity", style);
        ElarionUiTypography.draw(context, textRenderer, "Character Name", 40, 138, style.titleColor(), false);
        ElarionCivicUi.thinBox(context, NAME_X, NAME_Y, NAME_WIDTH, NAME_HEIGHT,
                ElarionCivicColors.MESSAGE_BODY, nameInput.focused()
                        ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW);
        ElarionUiIcons.drawOrDefault(context, "identity", NAME_X + 5, NAME_Y + 3, 18);
        renderNameInput(context, NAME_X + 26, NAME_Y, NAME_WIDTH - 34, NAME_HEIGHT, style);
        ElarionUiTypography.drawRight(context, textRenderer,
                nameInput.length() + " / " + NAME_MAX_LENGTH, NAME_X + NAME_WIDTH - 6, NAME_Y + 8,
                style.mutedColor(), false);

        ElarionUiTypography.draw(context, textRenderer, "Identity Preview", 40, 184, style.titleColor(), false);
        ElarionCivicUi.messageBody(context, 40, 198, 228, 72, ElarionCivicColors.GOLD_SHADOW);
        drawCurrentPlayerHead(context, 54, 211, 36, style);
        String previewName = nameInput.text().isBlank() ? "Unnamed Ember" : nameInput.text();
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, previewName, 156),
                100, 214, style.feedbackColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Aspiring Ember", 100, 232, style.textColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Realm placement pending", 100, 250, style.mutedColor(), false);

        ElarionCivicUi.thinBox(context, 40, 282, 228, 28,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_SHADOW);
        ElarionUiIcons.drawOrDefault(context, "rules", 49, 287, 16);
        ElarionUiTypography.draw(context, textRenderer, "Names are unique and stay reserved.",
                69, 291, style.mutedColor(), false);
    }

    private void drawCurrentPlayerHead(DrawContext context, int x, int y, int size, ElarionUiStyle style) {
        if (client != null && client.player != null) {
            PlayerSkinDrawer.draw(context, client.player.getSkinTextures(), x, y, size);
            return;
        }
        ElarionUiTypography.drawCentered(context, textRenderer, "?", x + size / 2, y + size / 2 - 4,
                style.titleColor(), false);
    }

    private void renderBiographyPanel(DrawContext context, ElarionUiStyle style) {
        ElarionCivicUi.thinBox(context, 296, 108, 360, 214,
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        drawPanelHeader(context, 296, 108, 360, "BIOGRAPHY", "biography", style);
        ElarionUiTypography.draw(context, textRenderer,
                "Write a short story, goal, or promise for your character.",
                312, 138, style.mutedColor(), false);
        ElarionCivicUi.messageBody(context, BIO_X, BIO_Y, BIO_WIDTH, BIO_HEIGHT,
                biographyInput.focused() ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW);
        renderBiography(context, textRenderer, BIO_X + 8, BIO_Y + 8, BIO_WIDTH - 16, BIO_HEIGHT - 16, style);
        ElarionUiTypography.drawRight(context, textRenderer, biographyInput.length() + " / " + BIO_MAX_LENGTH,
                BIO_X + BIO_WIDTH - 6, BIO_Y + BIO_HEIGHT + 7, style.mutedColor(), false);

        int checkY = 292;
        ElarionCivicUi.thinBox(context, 312, checkY, 328, 20,
                biographyInput.length() > 0 ? ElarionCivicColors.CARD_SELECTED : ElarionCivicColors.HEADER_SURFACE,
                biographyInput.length() > 0 ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW);
        ElarionUiIcons.drawOrDefault(context, "approve", 320, checkY + 2, 16);
        ElarionUiTypography.draw(context, textRenderer,
                biographyInput.length() > 0 ? "Biography ready for server review." : "Biography is required before placement.",
                340, checkY + 6, biographyInput.length() > 0 ? style.feedbackColor() : style.mutedColor(), false);
        if (!feedback.isBlank()) {
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiTypography.ellipsize(textRenderer, feedback, 320),
                    312, 327, style.errorColor(), false);
        }
    }

    private void renderBottomBand(DrawContext context, ElarionUiStyle style, double logicalMouseX, double logicalMouseY) {
        ElarionCivicUi.thinBox(context, 18, FOOTER_Y, PANEL_WIDTH - 36, FOOTER_HEIGHT,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionUiIcons.drawOrDefault(context, "placement", 36, FOOTER_Y + 9, 20);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer,
                        "Next: the server places you in the Realm that needs new Embers most.",
                        PRIMARY_BUTTON_X - 76),
                64, FOOTER_Y + 13, style.mutedColor(), false);
        boolean hovered = inside(logicalMouseX, logicalMouseY, PRIMARY_BUTTON_X, PRIMARY_BUTTON_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        ElarionCivicUi.compactActionButton(context, textRenderer, PRIMARY_BUTTON_X, PRIMARY_BUTTON_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT, PRIMARY_BUTTON_LABEL, hovered, false, true,
                ElarionCivicUi.Tone.PRIMARY, style);
    }

    private void renderStepStrip(DrawContext context, ElarionUiStyle style) {
        ElarionCivicUi.thinBox(context, 18, 56, PANEL_WIDTH - 36, 26,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        drawStep(context, 28, 61, 188, "1 Identity", true, "identity", style);
        drawStep(context, 228, 61, 188, "2 Biography", true, "biography", style);
        drawStep(context, 428, 61, 224, "3 Realm Placement", false, "placement", style);
    }

    private void drawStep(
            DrawContext context, int x, int y, int width, String label, boolean active,
            String iconId, ElarionUiStyle style
    ) {
        ElarionCivicUi.rowSurface(context, x, y, width, 16, active, false, !active);
        ElarionUiIcons.drawOrDefault(context, iconId, x + 8, y, 16);
        ElarionUiTypography.draw(context, textRenderer, label, x + 28, y + 4,
                active ? style.feedbackColor() : style.mutedColor(), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ElarionScaledLayout current = currentLayout();
        double logicalMouseX = current.logicalX(mouseX);
        double logicalMouseY = current.logicalY(mouseY);
        if (cooldownActive()) return true;
        if (button == 0 && inside(logicalMouseX, logicalMouseY, PRIMARY_BUTTON_X, PRIMARY_BUTTON_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT)) {
            submit();
            return true;
        }
        boolean inBio = inside(logicalMouseX, logicalMouseY, BIO_X, BIO_Y, BIO_WIDTH, BIO_HEIGHT);
        boolean inName = inside(logicalMouseX, logicalMouseY, NAME_X, NAME_Y, NAME_WIDTH, NAME_HEIGHT);
        if (button == 0 && inName) {
            nameInput.focused(true);
            biographyInput.focused(false);
            return true;
        }
        if (button == 0 && inBio) {
            biographyInput.focused(true);
            nameInput.focused(false);
            return true;
        }
        if (button == 0) {
            nameInput.focused(false);
            biographyInput.focused(false);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (cooldownActive()) return true;
        ElarionScaledLayout current = currentLayout();
        double logicalMouseX = current.logicalX(mouseX);
        double logicalMouseY = current.logicalY(mouseY);
        if (!inside(logicalMouseX, logicalMouseY, BIO_X, BIO_Y, BIO_WIDTH, BIO_HEIGHT)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        int lineCount = biographyLines(textRenderer, BIO_WIDTH - 16).size();
        ElarionTextViewportLayout.TextViewport viewport = biographyViewport(
                BIO_X + 8, BIO_Y + 8, BIO_WIDTH - 16, BIO_HEIGHT - 16, lineCount);
        biographyInput.scrollLine(viewport.clampedFirstLine(
                biographyInput.scrollLine() + (verticalAmount > 0.0D ? -1 : 1)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
                && hasControlDown()) {
            submit();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return true;
        if (cooldownActive()) return true;
        if (nameInput.focused()) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                nameInput.backspace();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_V && hasControlDown() && client != null) {
                nameInput.append(client.keyboard.getClipboard());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submit();
                return true;
            }
        }
        if (biographyInput.focused()) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                biographyInput.backspace();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                appendBiography("\n");
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_V && hasControlDown() && client != null) {
                appendBiography(client.keyboard.getClipboard());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (cooldownActive()) return true;
        if (biographyInput.focused()) {
            if (chr == '\n' || chr == '\r' || ElarionTextInput.isAllowedTextCharacter(chr)) {
                appendBiography(String.valueOf(chr));
            }
            return true;
        }
        if (nameInput.focused()) {
            nameInput.type(chr);
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        // Mandatory flow: disconnect remains available, gameplay does not resume without submission.
    }

    private void submit() {
        if (cooldownActive()) return;
        feedback = "";
        ClientPlayNetworking.send(new CharacterCreationSubmitPayload(
                requirement.nonce(), nameInput.text(), biographyInput.text()));
    }

    private void renderCooldown(DrawContext context, ElarionUiStyle style) {
        long remaining = requirement.eligibleAt() - System.currentTimeMillis();
        String time = remaining > 0L ? formatRemaining(remaining) : "waiting for server sync";
        context.drawCenteredTextWithShadow(textRenderer, "True Death cleanup is complete.",
                PANEL_WIDTH / 2, 88, style.titleColor());
        context.drawCenteredTextWithShadow(textRenderer, "Next character available in:",
                PANEL_WIDTH / 2, 114, style.mutedColor());
        context.drawCenteredTextWithShadow(textRenderer, time,
                PANEL_WIDTH / 2, 138, style.titleColor());
        if (!feedback.isBlank()) {
            context.drawCenteredTextWithShadow(textRenderer, feedback, PANEL_WIDTH / 2, 176,
                    style.errorColor());
        }
        int buttonX = (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = PANEL_HEIGHT - 37;
        ElarionCivicUi.compactActionButton(context, textRenderer, buttonX, buttonY,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Waiting", false, false, false,
                ElarionCivicUi.Tone.MUTED, style);
    }

    private boolean cooldownActive() {
        return requirement != null && "TRUE_DEAD_COOLDOWN".equals(requirement.status());
    }

    private static String formatRemaining(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;
        if (hours > 0L) return hours + "h " + minutes + "m";
        if (minutes > 0L) return minutes + "m " + remainingSeconds + "s";
        return remainingSeconds + "s";
    }

    private void appendBiography(String raw) {
        if (raw == null || raw.isEmpty()) return;
        biographyInput.append(raw);
        scrollBiographyToCaret();
    }

    private void renderBiography(
            DrawContext context, TextRenderer renderer, int x, int y, int maxWidth, int maxHeight,
            ElarionUiStyle style
    ) {
        ElarionScaledLayout current = currentLayout();
        int sx1 = current.screenX() + Math.round(x * current.scale());
        int sy1 = current.screenY() + Math.round(y * current.scale());
        int sx2 = current.screenX() + Math.round((x + maxWidth) * current.scale());
        int sy2 = current.screenY() + Math.round((y + maxHeight) * current.scale());
        context.enableScissor(sx1, sy1, sx2, sy2);
        List<OrderedText> lines = biographyLines(renderer, maxWidth);
        ElarionTextViewportLayout.TextViewport viewport =
                biographyViewport(x, y, maxWidth, maxHeight, lines.size());
        biographyInput.scrollLine(viewport.firstLine());
        for (int index = 0; index < viewport.visibleLineCount(); index++) {
            ElarionUiTypography.draw(context, renderer, lines.get(viewport.firstLine() + index),
                    x, viewport.lineY(index), style.textColor(), false);
        }
        if (biographyInput.focused() && biographyInput.caretVisible()) {
            int caretAbsoluteLine = Math.max(0, lines.size() - 1);
            int caretLine = viewport.visibleLineForAbsolute(caretAbsoluteLine);
            if (caretLine < 0) {
                context.disableScissor();
                renderBiographyScrollHints(context, x, y, maxWidth, maxHeight, viewport, style);
                return;
            }
            int caretY = viewport.lineY(caretLine);
            String last = lastVisibleLine(renderer, maxWidth);
            int caretX = x + Math.min(maxWidth - 1, ElarionUiTypography.width(renderer, last));
            context.fill(caretX, caretY, caretX + 1, caretY + 9, style.titleColor());
        }
        context.disableScissor();
        renderBiographyScrollHints(context, x, y, maxWidth, maxHeight, viewport, style);
    }

    private void renderNameInput(
            DrawContext context, int x, int boxY, int maxWidth, int boxHeight, ElarionUiStyle style
    ) {
        String visible = textRenderer.trimToWidth(nameInput.text(), maxWidth - 8);
        int textY = boxY + Math.max(0, (boxHeight - 8) / 2);
        ElarionUiTypography.draw(context, textRenderer, visible, x, textY, style.textColor(), false);
        if (nameInput.focused() && nameInput.caretVisible()) {
            int caretX = x + Math.min(maxWidth - 1, ElarionUiTypography.width(textRenderer, visible));
            context.fill(caretX, textY, caretX + 1, textY + 9, style.titleColor());
        }
    }

    private void renderBiographyScrollHints(
            DrawContext context,
            int x,
            int y,
            int maxWidth,
            int maxHeight,
            ElarionTextViewportLayout.TextViewport viewport,
            ElarionUiStyle style
    ) {
        if (viewport.canScrollUp()) {
            int cx = x + maxWidth - 8;
            ElarionUiTypography.draw(context, textRenderer, "^", cx, y, style.titleColor(), false);
        }
        if (viewport.canScrollDown()) {
            int cx = x + maxWidth - 8;
            ElarionUiTypography.draw(context, textRenderer, "v", cx, y + maxHeight - 9, style.titleColor(), false);
        }
    }

    private List<OrderedText> biographyLines(TextRenderer renderer, int maxWidth) {
        ArrayList<OrderedText> result = new ArrayList<>();
        String[] paragraphs = biographyInput.text().split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                result.add(Text.literal("").asOrderedText());
            } else {
                result.addAll(renderer.wrapLines(Text.literal(paragraph), maxWidth));
            }
        }
        if (result.isEmpty()) result.add(Text.literal("").asOrderedText());
        return result;
    }

    private String lastVisibleLine(TextRenderer renderer, int maxWidth) {
        String[] paragraphs = biographyInput.text().split("\n", -1);
        String last = paragraphs.length == 0 ? "" : paragraphs[paragraphs.length - 1];
        if (last.isBlank()) return "";
        List<OrderedText> wrapped = renderer.wrapLines(Text.literal(last), maxWidth);
        if (wrapped.size() <= 1) return renderer.trimToWidth(last, maxWidth);
        return renderer.trimToWidth(last, maxWidth);
    }

    private void scrollBiographyToCaret() {
        int lineCount = biographyLines(textRenderer, BIO_WIDTH - 16).size();
        ElarionTextViewportLayout.TextViewport viewport = biographyViewport(
                BIO_X + 8, BIO_Y + 8, BIO_WIDTH - 16, BIO_HEIGHT - 16, lineCount);
        biographyInput.scrollToBottom(lineCount, viewport.visibleLineCapacity());
    }

    private ElarionTextViewportLayout.TextViewport biographyViewport(
            int x, int y, int maxWidth, int maxHeight, int lineCount
    ) {
        return ElarionTextViewportLayout.lines(
                x, y, maxWidth, maxHeight, 10, lineCount, biographyInput.scrollLine());
    }

    private void drawPanelHeader(
            DrawContext context, int x, int y, int width, String title, String iconId, ElarionUiStyle style
    ) {
        ElarionSectionHeaderLayout.CenteredIconHeader header =
                ElarionSectionHeaderLayout.centeredIconHeader(x, y, width, 36,
                        14, 9, 20, 14, 10, 34);
        context.fill(header.divider().x(), header.divider().y(),
                header.divider().x() + header.divider().width(),
                header.divider().y() + header.divider().height(), ElarionCivicColors.DIVIDER);
        ElarionUiIcons.drawOrDefault(context, iconId, header.icon().x(), header.icon().y(), header.icon().width());
        ElarionUiTypography.drawCentered(context, textRenderer, title, header.titleCenterX(), header.titleY(),
                style.titleColor(), false);
    }

    private ElarionScaledLayout currentLayout() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        return layout;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
