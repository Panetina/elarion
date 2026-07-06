package panetina.elarion.core.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.network.CharacterRealmAssignmentPayload;

public final class CharacterRealmAssignmentScreen extends ElarionScreen {
    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 238;
    private static final int BUTTON_WIDTH = 124;
    private static final int BUTTON_HEIGHT = 22;
    private static final float MAX_SCALE = 0.86F;

    private final CharacterRealmAssignmentPayload payload;
    private ElarionScaledLayout layout;

    public CharacterRealmAssignmentScreen(CharacterRealmAssignmentPayload payload) {
        super(Text.literal("Realm Assignment"));
        this.payload = payload;
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
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 - 96, 21, true);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 + 96, 21, false);
        context.drawCenteredTextWithShadow(textRenderer, "Realm Placement", PANEL_WIDTH / 2, 13,
                style.titleColor());
        context.drawCenteredTextWithShadow(textRenderer,
                "Realm choosing will unlock later. For now, balance decides your start.",
                PANEL_WIDTH / 2, 30, style.mutedColor());

        int optionY = 60;
        int optionHeight = 34;
        int gap = 8;
        for (CharacterRealmAssignmentPayload.Option option : payload.options()) {
            boolean assigned = option.assigned();
            ElarionCivicUi.rowSurface(context, 24, optionY, PANEL_WIDTH - 48, optionHeight,
                    assigned, false, !assigned);
            String name = option.displayName().isBlank() ? option.realmId() : option.displayName();
            ElarionUiTypography.draw(context, textRenderer, name, 36, optionY + 7,
                    assigned ? style.titleColor() : style.mutedColor(), false);
            ElarionUiTypography.draw(context, textRenderer, option.population() + " citizens", PANEL_WIDTH - 112, optionY + 7,
                    style.mutedColor(), false);
            ElarionUiTypography.draw(context, textRenderer, assigned ? "Assigned" : "Future choice",
                    36, optionY + 19, assigned ? style.feedbackColor() : style.mutedColor(), false);
            optionY += optionHeight + gap;
        }

        String assigned = payload.assignedRealmName().isBlank()
                ? payload.assignedRealmId() : payload.assignedRealmName();
        context.drawCenteredTextWithShadow(textRenderer,
                "You were placed in " + assigned + ".",
                PANEL_WIDTH / 2, PANEL_HEIGHT - 58, style.textColor());

        int buttonX = (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = PANEL_HEIGHT - 32;
        boolean hovered = inside(logicalMouseX, logicalMouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        ElarionCivicUi.compactActionButton(context, textRenderer, buttonX, buttonY,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Continue", hovered, false, true,
                ElarionCivicUi.Tone.PRIMARY, style);
        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ElarionScaledLayout current = currentLayout();
        double logicalMouseX = current.logicalX(mouseX);
        double logicalMouseY = current.logicalY(mouseY);
        int buttonX = (PANEL_WIDTH - BUTTON_WIDTH) / 2;
        int buttonY = PANEL_HEIGHT - 32;
        if (button == 0 && inside(logicalMouseX, logicalMouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private ElarionScaledLayout currentLayout() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        return layout;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
