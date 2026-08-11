package panetina.elarion.addons.guilds.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.guilds.network.GuildInvitationDecisionPayload;
import panetina.elarion.addons.guilds.network.GuildInvitationPromptPayload;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

/** Small modal surfaced immediately for an online Guild invitation. */
public final class GuildInvitationPromptScreen extends ElarionScreen {
    private static final int LOGICAL_WIDTH = 312;
    private static final int LOGICAL_HEIGHT = 158;
    private final GuildInvitationPromptPayload invitation;
    private ElarionScaledLayout layout;
    private boolean submitted;

    public GuildInvitationPromptScreen(GuildInvitationPromptPayload invitation) {
        super(Text.literal("Guild Invitation"));
        this.invitation = invitation;
    }

    @Override protected void init() {
        layout = ElarionScaledLayout.fit(width, height, LOGICAL_WIDTH, LOGICAL_HEIGHT, 8, 110);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        layout = ElarionScaledLayout.fit(width, height, LOGICAL_WIDTH, LOGICAL_HEIGHT, 8, 110);
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);
        ElarionCivicUi.attachedShell(context, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, 38);
        ElarionUiTypography.drawCentered(context, textRenderer, "GUILD INVITATION", LOGICAL_WIDTH / 2, 18,
                style.titleColor(), true);
        ElarionUiTypography.drawCentered(context, textRenderer, invitation.inviterName() + " invited you to", LOGICAL_WIDTH / 2,
                55, style.textColor(), false);
        ElarionUiTypography.drawCentered(context, textRenderer,
                invitation.guildName() + " [" + invitation.guildTag() + "]", LOGICAL_WIDTH / 2, 76,
                style.titleColor(), false);
        ElarionUiTypography.drawCentered(context, textRenderer, "Choose now.",
                LOGICAL_WIDTH / 2, 101, style.mutedColor(), false);
        ElarionCivicUi.compactActionButton(context, textRenderer, 42, 119, 105, 24, "Deny",
                inside(lx, ly, 42, 119, 105, 24), false, !submitted, ElarionCivicUi.Tone.NORMAL, style);
        ElarionCivicUi.compactActionButton(context, textRenderer, 165, 119, 105, 24,
                submitted ? "Joining..." : "Accept", inside(lx, ly, 165, 119, 105, 24), false, !submitted,
                ElarionCivicUi.Tone.PRIMARY, style);
        context.getMatrices().pop();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || submitted || layout == null) return false;
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        if (inside(lx, ly, 42, 119, 105, 24)) return decide(false);
        if (inside(lx, ly, 165, 119, 105, 24)) return decide(true);
        return true;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return decide(false);
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) return decide(true);
        return true;
    }

    private boolean decide(boolean accepted) {
        if (submitted) return true;
        submitted = true;
        ClientPlayNetworking.send(new GuildInvitationDecisionPayload(invitation.guildId(), accepted));
        close();
        return true;
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    @Override public void close() { if (client != null) client.setScreen(null); }
}
