package panetina.elarion.addons.atlas.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import java.util.List;

public final class AtlasPlaceholderScreen extends ElarionScreen {
    static final int LOGICAL_WIDTH = 520;
    static final int LOGICAL_HEIGHT = 340;
    private static final int HEADER_HEIGHT = 48;
    private static final int CLOSE_SIZE = 18;
    private static final List<FeatureSlot> FEATURE_SLOTS = List.of(
            new FeatureSlot("Worlds", false),
            new FeatureSlot("Realms", false),
            new FeatureSlot("Shrines", false),
            new FeatureSlot("Portals", false),
            new FeatureSlot("NPCs", false),
            new FeatureSlot("Filters", false));

    private ElarionScaledLayout layout;

    public AtlasPlaceholderScreen() {
        super(Text.literal("Elarion Atlas"));
    }

    static List<FeatureSlot> featureSlots() {
        return FEATURE_SLOTS;
    }

    @Override
    protected void init() {
        layout = fit(width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        layout = fit(width, height);
        double logicalMouseX = layout.logicalX(mouseX);
        double logicalMouseY = layout.logicalY(mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, HEADER_HEIGHT);
        ElarionCivicUi.headerOrnament(context, LOGICAL_WIDTH / 2 - 112, 21, true);
        ElarionCivicUi.headerOrnament(context, LOGICAL_WIDTH / 2 + 112, 21, false);
        ElarionUiTypography.drawCentered(context, textRenderer, "ELARION ATLAS",
                LOGICAL_WIDTH / 2, ElarionCivicUi.centeredTextY(textRenderer, 0, HEADER_HEIGHT),
                style.titleColor(), false);
        ElarionCivicUi.closeButton(context, closeX(), 15, CLOSE_SIZE);

        ElarionUiTypography.drawCentered(context, textRenderer,
                "The Atlas foundation is prepared. Exploration systems are not active yet.",
                LOGICAL_WIDTH / 2, 70, style.textColor(), false);
        ElarionUiTypography.drawCentered(context, textRenderer,
                "Future map data will remain server-authoritative, bounded, and Realm-aware.",
                LOGICAL_WIDTH / 2, 88, style.mutedColor(), false);

        for (int index = 0; index < FEATURE_SLOTS.size(); index++) {
            FeatureSlot slot = FEATURE_SLOTS.get(index);
            int column = index % 3;
            int row = index / 3;
            int x = 34 + column * 154;
            int y = 126 + row * 54;
            ElarionCivicUi.rowSurface(context, x, y, 142, 38, false,
                    inside(logicalMouseX, logicalMouseY, x, y, 142, 38), !slot.enabled());
            ElarionUiTypography.drawCentered(context, textRenderer, slot.label(), x + 71,
                    ElarionCivicUi.centeredTextY(textRenderer, y, 38), style.mutedColor(), false);
        }

        int buttonX = LOGICAL_WIDTH / 2 - 54;
        int buttonY = LOGICAL_HEIGHT - 48;
        ElarionCivicUi.compactActionButton(context, textRenderer, buttonX, buttonY, 108, 24,
                "Close", inside(logicalMouseX, logicalMouseY, buttonX, buttonY, 108, 24),
                false, true, ElarionCivicUi.Tone.NORMAL, style);
        ElarionUiTypography.drawCentered(context, textRenderer, "Shell only - no map data is loaded",
                LOGICAL_WIDTH / 2, LOGICAL_HEIGHT - 70, style.mutedColor(), false);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || layout == null) return false;
        double logicalMouseX = layout.logicalX(mouseX);
        double logicalMouseY = layout.logicalY(mouseY);
        int buttonX = LOGICAL_WIDTH / 2 - 54;
        int buttonY = LOGICAL_HEIGHT - 48;
        if (inside(logicalMouseX, logicalMouseY, closeX(), 15, CLOSE_SIZE, CLOSE_SIZE)
                || inside(logicalMouseX, logicalMouseY, buttonX, buttonY, 108, 24)) {
            close();
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == ElarionAtlasClient.DEFAULT_KEY_CODE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(null);
    }

    private static ElarionScaledLayout fit(int screenWidth, int screenHeight) {
        return ElarionScaledLayout.fit(screenWidth, screenHeight, LOGICAL_WIDTH, LOGICAL_HEIGHT, 8, 72);
    }

    private static int closeX() {
        return LOGICAL_WIDTH - 32;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    record FeatureSlot(String label, boolean enabled) {
    }
}
