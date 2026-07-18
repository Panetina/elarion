package panetina.elarion.core.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.network.CharacterRealmAssignmentConfirmPayload;
import panetina.elarion.core.network.CharacterRealmAssignmentPayload;

public final class CharacterRealmAssignmentScreen extends ElarionScreen {
    static final int PANEL_WIDTH = 680;
    static final int PANEL_HEIGHT = 360;
    static final int BUTTON_WIDTH = 154;
    static final int BUTTON_HEIGHT = 24;
    static final int FOOTER_Y = PANEL_HEIGHT - 68;
    static final int FOOTER_HEIGHT = 50;
    static final int CONFIRM_BUTTON_X = PANEL_WIDTH - BUTTON_WIDTH - 30;
    static final int CONFIRM_BUTTON_Y = FOOTER_Y + (FOOTER_HEIGHT - BUTTON_HEIGHT) / 2;
    static final int OPTION_X = 24;
    static final int OPTION_Y = 92;
    static final int OPTION_WIDTH = PANEL_WIDTH - 48;
    static final int OPTION_HEIGHT = 58;
    static final int OPTION_GAP = 8;
    private static final float MAX_SCALE = 0.9F;

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

        ElarionCivicUi.attachedShell(context, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 52);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 - 122, 22, true);
        ElarionCivicUi.headerOrnament(context, PANEL_WIDTH / 2 + 122, 22, false);
        context.drawCenteredTextWithShadow(textRenderer, "Realm Placement", PANEL_WIDTH / 2, 13,
                style.titleColor());
        context.drawCenteredTextWithShadow(textRenderer,
                "To keep Elarion balanced, new Embers are placed where help is needed most.",
                PANEL_WIDTH / 2, 30, style.mutedColor());

        String assigned = assignedName();
        ElarionCivicUi.thinBox(context, 18, 56, PANEL_WIDTH - 36, 26,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionUiIcons.drawOrDefault(context, "placement", 32, 60, 18);
        ElarionUiTypography.draw(context, textRenderer,
                "Assigned Realm", 56, 65, style.titleColor(), false);
        ElarionUiTypography.drawRight(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, assigned, 220),
                PANEL_WIDTH - 32, 65, style.feedbackColor(), false);

        int count = Math.min(3, payload.options().size());
        if (count == 0) {
            ElarionCivicUi.thinBox(context, OPTION_X, OPTION_Y, OPTION_WIDTH, OPTION_HEIGHT,
                    ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
            context.drawCenteredTextWithShadow(textRenderer, "No Realm placement options were sent.",
                    PANEL_WIDTH / 2, OPTION_Y + OPTION_HEIGHT / 2 - 4, style.mutedColor());
        }
        for (int index = 0; index < count; index++) {
            renderRealmOption(context, payload.options().get(index), OPTION_Y + index * (OPTION_HEIGHT + OPTION_GAP),
                    index, style);
        }

        ElarionCivicUi.thinBox(context, 18, FOOTER_Y, PANEL_WIDTH - 36, FOOTER_HEIGHT,
                ElarionCivicColors.HEADER_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionUiIcons.drawOrDefault(context, "approve", 36, FOOTER_Y + 13, 20);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer,
                        "Placement is server-authoritative and keeps early Realm growth fair.",
                        CONFIRM_BUTTON_X - 76),
                64, FOOTER_Y + 13, style.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer,
                        "You can review Realm details later from civic menus.",
                        CONFIRM_BUTTON_X - 76),
                64, FOOTER_Y + 31, style.textColor(), false);

        boolean hovered = inside(logicalMouseX, logicalMouseY, CONFIRM_BUTTON_X, CONFIRM_BUTTON_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        ElarionCivicUi.compactActionButton(context, textRenderer, CONFIRM_BUTTON_X, CONFIRM_BUTTON_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT, "Confirm Placement", hovered, false, true,
                ElarionCivicUi.Tone.PRIMARY, style);
        context.getMatrices().pop();
    }

    private void renderRealmOption(
            DrawContext context, CharacterRealmAssignmentPayload.Option option, int y, int index, ElarionUiStyle style
    ) {
        boolean assigned = option.assigned();
        ElarionCivicUi.rowSurface(context, OPTION_X, y, OPTION_WIDTH, OPTION_HEIGHT, assigned, false, !assigned);
        int border = assigned ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW;
        context.fill(OPTION_X + 1, y + 1, OPTION_X + 3, y + OPTION_HEIGHT - 1, border);
        ElarionUiIcons.drawOrDefault(context, assigned ? "realm" : "placement", OPTION_X + 14, y + 12, 32);
        String name = option.displayName().isBlank() ? option.realmId() : option.displayName();
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, name, 235),
                OPTION_X + 58, y + 10, assigned ? style.feedbackColor() : style.titleColor(), false);
        ElarionUiTypography.draw(context, textRenderer,
                option.realmId().isBlank() ? "Realm record pending" : option.realmId(),
                OPTION_X + 58, y + 28, style.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Population", OPTION_X + 318, y + 10,
                style.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer, option.population() + " Embers",
                OPTION_X + 318, y + 28, style.textColor(), false);
        String status = assigned ? "Assigned" : index == 0 ? "Balancing" : "Closed for start";
        ElarionUiTypography.drawRight(context, textRenderer, status, OPTION_X + OPTION_WIDTH - 18, y + 10,
                assigned ? style.feedbackColor() : style.mutedColor(), false);
        ElarionUiTypography.drawRight(context, textRenderer,
                assigned ? "Your first home" : "Future transfers later",
                OPTION_X + OPTION_WIDTH - 18, y + 28, assigned ? style.textColor() : style.mutedColor(), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ElarionScaledLayout current = currentLayout();
        double logicalMouseX = current.logicalX(mouseX);
        double logicalMouseY = current.logicalY(mouseY);
        if (button == 0 && inside(logicalMouseX, logicalMouseY, CONFIRM_BUTTON_X, CONFIRM_BUTTON_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT)) {
            ClientPlayNetworking.send(CharacterRealmAssignmentConfirmPayload.INSTANCE);
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private ElarionScaledLayout currentLayout() {
        layout = ElarionScaledLayout.fit(width, height, PANEL_WIDTH, PANEL_HEIGHT, 8, 60, MAX_SCALE);
        return layout;
    }

    private String assignedName() {
        if (!payload.assignedRealmName().isBlank()) return payload.assignedRealmName();
        if (!payload.assignedRealmId().isBlank()) return payload.assignedRealmId();
        return "Realm pending";
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
