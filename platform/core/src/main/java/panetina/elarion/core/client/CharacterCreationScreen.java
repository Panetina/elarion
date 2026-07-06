package panetina.elarion.core.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionTextInput;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.network.CharacterCreationRequirementPayload;
import panetina.elarion.core.network.CharacterCreationSubmitPayload;

import java.util.ArrayList;
import java.util.List;

public final class CharacterCreationScreen extends ElarionScreen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 286;
    private static final int BUTTON_WIDTH = 124;
    private static final int BUTTON_HEIGHT = 22;
    private static final float MAX_SCALE = 0.82F;
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

        ElarionCivicUi.attachedShell(context, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 48);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 - 106, 21, true);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 + 106, 21, false);
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

        ElarionUiTypography.draw(context, textRenderer, "Name", 20, 55, style.titleColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Short biography", 20, 100, style.titleColor(), false);
        ElarionCivicUi.thinBox(context, 18, 65, PANEL_WIDTH - 36, 24,
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        renderNameInput(context, 24, 65, PANEL_WIDTH - 48, 24, style);
        int bioX = 18;
        int bioY = 118;
        int bioWidth = PANEL_WIDTH - 36;
        int bioHeight = 70;
        ElarionCivicUi.messageBody(context, bioX, bioY, bioWidth, bioHeight, ElarionCivicColors.GOLD_SHADOW);
        renderBiography(context, textRenderer, bioX + 6, bioY + 6, bioWidth - 12, bioHeight - 12, style);
        ElarionUiTypography.draw(context, textRenderer, biographyInput.length() + " / " + BIO_MAX_LENGTH,
                PANEL_WIDTH - 70, 192, style.mutedColor(), false);
        if (!feedback.isBlank()) {
            context.drawCenteredTextWithShadow(textRenderer, feedback, PANEL_WIDTH / 2, 212,
                    style.errorColor());
        } else {
            context.drawCenteredTextWithShadow(textRenderer,
                    "Names are unique. Names of dead characters remain reserved.",
                    PANEL_WIDTH / 2, 212, style.mutedColor());
        }

        int buttonX = (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = PANEL_HEIGHT - 32;
        boolean hovered = inside(logicalMouseX, logicalMouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        ElarionCivicUi.compactActionButton(context, textRenderer, buttonX, buttonY,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Enter The Living World", hovered, false, true,
                ElarionCivicUi.Tone.PRIMARY, style);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ElarionScaledLayout current = currentLayout();
        double logicalMouseX = current.logicalX(mouseX);
        double logicalMouseY = current.logicalY(mouseY);
        if (cooldownActive()) return true;
        int buttonX = (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = PANEL_HEIGHT - 32;
        if (button == 0 && inside(logicalMouseX, logicalMouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            submit();
            return true;
        }
        boolean inBio = inside(logicalMouseX, logicalMouseY, 18, 118, PANEL_WIDTH - 36, 70);
        boolean inName = inside(logicalMouseX, logicalMouseY, 18, 65, PANEL_WIDTH - 36, 24);
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
        if (!inside(logicalMouseX, logicalMouseY, 18, 118, PANEL_WIDTH - 36, 70)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        int visibleLines = Math.max(1, (70 - 12) / 10);
        int lineCount = biographyLines(textRenderer, PANEL_WIDTH - 48).size();
        int maxScroll = Math.max(0, lineCount - visibleLines);
        biographyInput.scrollBy(verticalAmount > 0.0D ? -1 : 1, maxScroll);
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
        int buttonY = PANEL_HEIGHT - 32;
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
        int maxLines = Math.max(1, maxHeight / 10);
        int maxScroll = Math.max(0, lines.size() - maxLines);
        biographyInput.clampScroll(maxScroll);
        int count = Math.min(lines.size() - biographyInput.scrollLine(), maxLines);
        for (int index = 0; index < count; index++) {
            ElarionUiTypography.draw(context, renderer, lines.get(biographyInput.scrollLine() + index),
                    x, y + index * 10, style.textColor(), false);
        }
        if (biographyInput.focused() && biographyInput.caretVisible()) {
            int caretAbsoluteLine = Math.max(0, lines.size() - 1);
            int caretLine = caretAbsoluteLine - biographyInput.scrollLine();
            if (caretLine < 0 || caretLine >= maxLines) {
                context.disableScissor();
                renderBiographyScrollHints(context, x, y, maxWidth, maxHeight, maxScroll, style);
                return;
            }
            int caretY = y + caretLine * 10;
            String last = lastVisibleLine(renderer, maxWidth);
            int caretX = x + Math.min(maxWidth - 1, ElarionUiTypography.width(renderer, last));
            context.fill(caretX, caretY, caretX + 1, caretY + 9, style.titleColor());
        }
        context.disableScissor();
        renderBiographyScrollHints(context, x, y, maxWidth, maxHeight, maxScroll, style);
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
            DrawContext context, int x, int y, int maxWidth, int maxHeight, int maxScroll, ElarionUiStyle style
    ) {
        if (biographyInput.scrollLine() > 0) {
            int cx = x + maxWidth - 8;
            ElarionUiTypography.draw(context, textRenderer, "^", cx, y, style.titleColor(), false);
        }
        if (biographyInput.scrollLine() < maxScroll) {
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
        int visibleLines = Math.max(1, (70 - 12) / 10);
        int lineCount = biographyLines(textRenderer, PANEL_WIDTH - 48).size();
        biographyInput.scrollToBottom(lineCount, visibleLines);
    }

    private ElarionScaledLayout currentLayout() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        return layout;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
