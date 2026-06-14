package panetina.elarion.addons.portals.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.portals.network.PortalTravelConfirmPayload;
import panetina.elarion.addons.portals.network.PortalTravelPromptPayload;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.time.Duration;
import java.time.Instant;

public final class PortalConfirmationScreen extends Screen {
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
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, theme.backgroundOverlayColor());
        double mx = layout.logicalX(mouseX);
        double my = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1);
        ElarionUiRenderer.panel(context, 0, 0, payload.logicalWidth(), payload.logicalHeight(), theme);
        ElarionUiRenderer.headerBand(context, 3, 3, payload.logicalWidth() - 6, 36, style);
        String heading = "Enter " + payload.gateName() + "?";
        context.drawText(textRenderer, heading,
                (payload.logicalWidth() - textRenderer.getWidth(heading)) / 2,
                15, theme.titleColor(), false);
        ElarionUiRenderer.borderedBox(context, 18, 49,
                payload.logicalWidth() - 36, 78, style);
        drawGateIcon(context, 32, 64);
        int requirementColor = payload.requirementColor() != 0
                ? payload.requirementColor()
                : payload.allowed() ? theme.textColor() : theme.warningColor();
        context.drawText(textRenderer, payload.requirement(), 78, 63, requirementColor, false);
        String timing = payload.closesAt() <= 0 ? "Open continuously." : "Closes in " + remaining() + ".";
        context.drawText(textRenderer, timing, 78, 81, theme.mutedColor(), false);
        if (!payload.message().isBlank()) {
            context.drawText(textRenderer,
                    ElarionUiRenderer.ellipsize(textRenderer, payload.message(), payload.logicalWidth() - 104),
                    78, 99, payload.allowed() ? theme.textColor() : theme.errorColor(), false);
        }
        int buttonY = payload.logicalHeight() - 42;
        int gap = 12;
        int total = payload.confirmButtonWidth() + payload.closeButtonWidth() + gap;
        int confirmX = (payload.logicalWidth() - total) / 2;
        int closeX = confirmX + payload.confirmButtonWidth() + gap;
        boolean confirmHover = inside(mx, my, confirmX, buttonY, payload.confirmButtonWidth(), 20);
        boolean closeHover = inside(mx, my, closeX, buttonY, payload.closeButtonWidth(), 20);
        ElarionUiRenderer.compactButton(context, textRenderer, confirmX, buttonY,
                payload.confirmButtonWidth(), 20, "Yes",
                confirmHover, payload.allowed() && !submitted, style);
        ElarionUiRenderer.compactButton(context, textRenderer, closeX, buttonY,
                payload.closeButtonWidth(), 20, "No", closeHover, true, style);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double mx = layout.logicalX(mouseX);
        double my = layout.logicalY(mouseY);
        int y = payload.logicalHeight() - 42;
        int gap = 12;
        int total = payload.confirmButtonWidth() + payload.closeButtonWidth() + gap;
        int confirmX = (payload.logicalWidth() - total) / 2;
        int closeX = confirmX + payload.confirmButtonWidth() + gap;
        if (inside(mx, my, closeX, y, payload.closeButtonWidth(), 20)) {
            close();
            return true;
        }
        if (!submitted && payload.allowed()
                && inside(mx, my, confirmX, y, payload.confirmButtonWidth(), 20)) {
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
        ElarionUiRenderer.borderedBox(context, x, y, 32, 32, style);
        Identifier id = Identifier.tryParse(payload.iconItem());
        if (id != null && Registries.ITEM.containsId(id)) {
            context.drawItem(new ItemStack(Registries.ITEM.get(id)), x + 8, y + 8);
        }
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}
