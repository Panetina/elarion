package panetina.elarion.addons.guilds.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

/** Read-only Guild entry screen. Creation remains an NPC Registrar action. */
public final class GuildEmptyScreen extends ElarionScreen {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 210;
    private ElarionScaledLayout layout;

    public GuildEmptyScreen() { super(Text.literal("Guild")); }
    @Override protected void init() { layout = ElarionScaledLayout.fit(width, height, WIDTH, HEIGHT, 8, 72); }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        layout = ElarionScaledLayout.fit(width, height, WIDTH, HEIGHT, 8, 72);
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);
        ElarionCivicUi.attachedShell(context, 0, 0, WIDTH, HEIGHT, 48);
        ElarionUiTypography.drawCentered(context, textRenderer, "GUILD", WIDTH / 2, 19, style.titleColor(), true);
        ElarionUiTypography.drawCentered(context, textRenderer, "You do not belong to a Guild yet.", WIDTH / 2, 82, style.textColor(), false);
        ElarionUiTypography.drawCentered(context, textRenderer, "Speak with a Guild Registrar to create one or accept an invitation.", WIDTH / 2, 108, style.mutedColor(), false);
        ElarionCivicUi.compactActionButton(context, textRenderer, 146, 154, 108, 24, "Close", false, false, true, ElarionCivicUi.Tone.NORMAL, style);
        context.getMatrices().pop();
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && layout != null && mouseX >= layout.screenX() && mouseY >= layout.screenY()) { close(); return true; }
        return false;
    }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_G) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override public void close() { if (client != null) client.setScreen(null); }
}
