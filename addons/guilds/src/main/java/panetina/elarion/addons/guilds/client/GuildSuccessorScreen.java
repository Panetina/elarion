package panetina.elarion.addons.guilds.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.guilds.network.GuildScreenActionPayload;
import panetina.elarion.addons.guilds.network.GuildSuccessorOpenPayload;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

/** Scrollable, dialogue-launched ownership transfer selection. Server revalidates on confirmation. */
public final class GuildSuccessorScreen extends ElarionScreen {
    private final GuildSuccessorOpenPayload payload;
    private ElarionScaledLayout layout;
    private int scroll;
    public GuildSuccessorScreen(GuildSuccessorOpenPayload payload) { super(Text.literal("Choose successor")); this.payload = payload; }
    @Override protected void init() { layout = ElarionScaledLayout.fit(width, height, 430, 300, 8, 70); }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default")); layout = ElarionScaledLayout.fit(width, height, 430, 300, 8, 70);
        context.fill(0, 0, width, height, style.backgroundOverlayColor()); context.getMatrices().push(); context.getMatrices().translate(layout.screenX(), layout.screenY(), 0); context.getMatrices().scale(layout.scale(), layout.scale(), 1);
        ElarionCivicUi.attachedShell(context, 0, 0, 430, 300, 40); ElarionUiTypography.drawCentered(context, textRenderer, "CHOOSE A SUCCESSOR", 215, 20, style.titleColor(), true);
        ElarionUiTypography.drawCentered(context, textRenderer, "Transfer ownership of " + payload.guildName() + " for 25 Sigils.", 215, 43, style.mutedColor(), false);
        int start = Math.min(scroll, Math.max(0, payload.candidates().size() - 8)); for (int i = 0; i < 8 && start + i < payload.candidates().size(); i++) { var candidate = payload.candidates().get(start + i); int y = 66 + i * 25; ElarionCivicUi.compactActionButton(context, textRenderer, 32, y, 366, 20, candidate.name(), false, false, true, ElarionCivicUi.Tone.NORMAL, style); }
        ElarionCivicUi.compactActionButton(context, textRenderer, 157, 266, 116, 22, "Cancel", false, false, true, ElarionCivicUi.Tone.NORMAL, style); context.getMatrices().pop();
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { if (button != 0) return false; double x = layout.logicalX(mouseX), y = layout.logicalY(mouseY); if (x >= 32 && x < 398 && y >= 66 && y < 266) { int index = scroll + ((int)y - 66) / 25; if (index < payload.candidates().size()) ClientPlayNetworking.send(new GuildScreenActionPayload("transfer_leadership", payload.candidates().get(index).id(), "", new byte[0])); return true; } close(); return true; }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { scroll = Math.max(0, Math.min(Math.max(0, payload.candidates().size() - 8), scroll + (verticalAmount < 0 ? 1 : -1))); return true; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { if (keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; } return super.keyPressed(keyCode, scanCode, modifiers); }
}
