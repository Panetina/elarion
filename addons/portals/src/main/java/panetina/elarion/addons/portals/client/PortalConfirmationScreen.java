package panetina.elarion.addons.portals.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.network.PortalTravelConfirmPayload;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.time.Duration;
import java.time.Instant;

public final class PortalConfirmationScreen extends ElarionScreen {
    private final PortalTravelPromptPayload payload;
    private ElarionScaledLayout layout;
    private ElarionUiThemeVariant theme;
    private ElarionUiStyle style;
    private boolean submitted;

    public PortalConfirmationScreen(PortalTravelPromptPayload payload) {
        super(Text.literal(payload.gateName()));
        this.payload = payload;
    }

    @Override
    protected void init() {
        theme = ElarionUiThemes.variant(payload.themeVariant());
        style = ElarionUiStyle.from(theme);
        layout = ElarionScaledLayout.fit(width, height, payload.logicalWidth(), payload.logicalHeight(),
                8, payload.minimumScalePercent());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, theme.backgroundOverlayColor());
        double mx = layout.logicalX(mouseX);
        double my = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1);
        ElarionCivicUi.attachedShell(context, 0, 0, payload.logicalWidth(), payload.logicalHeight(), 42);
        ElarionCivicUi.headerOrnament(context, payload.logicalWidth() / 2 - 88, 22, true);
        ElarionCivicUi.headerOrnament(context, payload.logicalWidth() / 2 + 88, 22, false);
        String heading = "Enter " + payload.gateName() + "?";
        ElarionUiTypography.draw(context, textRenderer, heading,
                (payload.logicalWidth() - ElarionUiTypography.width(textRenderer, heading)) / 2,
                ElarionCivicUi.centeredTextY(textRenderer, 0, 42), theme.titleColor(), false);
        ElarionCivicUi.messageBody(context, 18, 49, payload.logicalWidth() - 36, 78,
                payload.allowed() ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.REJECT_RED);
        boolean hasPaymentSlot = hasPaymentSlot(payload.costKind());
        int textX = hasPaymentSlot ? 78 : 34;
        int textWidth = payload.logicalWidth() - textX - 26;
        if (hasPaymentSlot) {
            drawGateIcon(context, 32, 64);
        }
        int requirementColor = payload.requirementColor() != 0
                ? payload.requirementColor()
                : payload.allowed() ? theme.textColor() : theme.warningColor();
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, payload.requirement(), textWidth),
                textX, 63, requirementColor, false);
        String timing = payload.closesAt() <= 0 ? "Open continuously." : "Closes in " + remaining() + ".";
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, timing, textWidth),
                textX, 81, theme.mutedColor(), false);
        if (!payload.message().isBlank()) {
            ElarionUiTypography.wrappedClipped(context, textRenderer, Text.literal(payload.message()),
                    textX, 99, textWidth, 24,
                    payload.allowed() ? theme.textColor() : theme.errorColor(), theme.mutedColor());
        }
        int buttonY = payload.logicalHeight() - 42;
        ButtonLayout buttons = buttonLayout(payload.logicalWidth(), payload.confirmButtonWidth(),
                payload.closeButtonWidth(), 12);
        boolean confirmHover = inside(mx, my, buttons.confirmX(), buttonY, payload.confirmButtonWidth(), 20);
        boolean closeHover = inside(mx, my, buttons.closeX(), buttonY, payload.closeButtonWidth(), 20);
        ElarionCivicUi.compactActionButton(context, textRenderer, buttons.confirmX(), buttonY,
                payload.confirmButtonWidth(), 20, "Yes", confirmHover, false,
                payload.allowed() && !submitted,
                payload.allowed() && !submitted ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.MUTED,
                style);
        ElarionCivicUi.compactActionButton(context, textRenderer, buttons.closeX(), buttonY,
                payload.closeButtonWidth(), 20, "No", closeHover, false, true,
                ElarionCivicUi.Tone.NORMAL, style);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mx = layout.logicalX(mouseX);
        double my = layout.logicalY(mouseY);
        int y = payload.logicalHeight() - 42;
        ButtonLayout buttons = buttonLayout(payload.logicalWidth(), payload.confirmButtonWidth(),
                payload.closeButtonWidth(), 12);
        if (inside(mx, my, buttons.closeX(), y, payload.closeButtonWidth(), 20)) {
            close();
            return true;
        }
        if (!submitted && payload.allowed()
                && inside(mx, my, buttons.confirmX(), y, payload.confirmButtonWidth(), 20)) {
            submitted = true;
            ClientPlayNetworking.send(new PortalTravelConfirmPayload(payload.routeId(), payload.direction()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String remaining() {
        long seconds = Math.max(0, Duration.between(Instant.now(), Instant.ofEpochMilli(payload.closesAt())).toSeconds());
        long hours = seconds / 3600;
        long minutes = seconds % 3600 / 60;
        long remainingSeconds = seconds % 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m " + remainingSeconds + "s";
    }

    private void drawGateIcon(DrawContext context, int x, int y) {
        ElarionCivicUi.thinBox(context, x, y, 32, 32,
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        if (PortalTravelPromptPayload.COST_FEE.equals(payload.costKind())) {
            ElarionUiRenderer.currencyIcon(context, x + 7, y + 7, 18);
            return;
        }
        String semantic = semanticGateIcon();
        if (ElarionUiIcons.has(semantic)) {
            ElarionUiIcons.drawOrDefault(context, semantic, x + 5, y + 5, 22);
            return;
        }
        Identifier id = Identifier.tryParse(payload.iconItem());
        if (id != null && Registries.ITEM.containsId(id)) {
            context.drawItem(new ItemStack(Registries.ITEM.get(id)), x + 8, y + 8);
        }
    }

    private String semanticGateIcon() {
        return semanticGateIcon(payload.routeId(), payload.costKind());
    }

    static boolean hasPaymentSlot(String costKind) {
        return !PortalTravelPromptPayload.COST_FREE.equals(costKind);
    }

    static String semanticGateIcon(String routeId, String costKind) {
        String route = routeId == null ? "" : routeId.toLowerCase(java.util.Locale.ROOT);
        if (PortalTravelPromptPayload.COST_TICKET.equals(costKind)) {
            if (route.contains("nether")) return "nether_ticket";
            if (route.contains("end")) return "end_ticket";
            return "portal_ticket";
        }
        if (PortalTravelPromptPayload.COST_FEE.equals(costKind)) return "portal_fee";
        if (PortalTravelPromptPayload.COST_FREE.equals(costKind)) return "portal_free";
        if (route.contains("nether")) return "nether_gate";
        if (route.contains("end")) return "end_gate";
        return "portal";
    }

    static ButtonLayout buttonLayout(int logicalWidth, int confirmWidth, int closeWidth, int gap) {
        int total = confirmWidth + closeWidth + gap;
        int confirmX = (logicalWidth - total) / 2;
        return new ButtonLayout(confirmX, confirmX + confirmWidth + gap);
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    record ButtonLayout(int confirmX, int closeX) {
    }
}
