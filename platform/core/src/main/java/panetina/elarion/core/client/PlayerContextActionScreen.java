package panetina.elarion.core.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;
import panetina.elarion.core.network.PlayerContextActionExecutePayload;
import panetina.elarion.core.network.PlayerContextActionSnapshotPayload;

/** Compact screen-space context menu; its entries are server-authored. */
public final class PlayerContextActionScreen extends ElarionScreen {
    private static final int WIDTH = 218;
    private static final int HEADER = 42;
    private static final int ROW_HEIGHT = 24;
    private final PlayerContextActionSnapshotPayload snapshot;
    private final int originX;
    private final int originY;

    public PlayerContextActionScreen(PlayerContextActionSnapshotPayload snapshot, int mouseX, int mouseY) {
        super(Text.literal("Player actions"));
        this.snapshot = snapshot;
        this.originX = mouseX;
        this.originY = mouseY;
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        int height = HEADER + snapshot.actions().size() * ROW_HEIGHT + 8;
        int x = Math.max(6, Math.min(width - WIDTH - 6, originX - WIDTH / 2));
        int y = Math.max(6, Math.min(this.height - height - 6, originY - height - 18));
        ElarionCivicUi.attachedShell(context, x, y, WIDTH, height, 26);
        ElarionUiTypography.drawCentered(context, textRenderer, snapshot.targetName(), x + WIDTH / 2, y + 12,
                style.titleColor(), true);
        ElarionUiTypography.drawCentered(context, textRenderer, "PLAYER ACTIONS", x + WIDTH / 2, y + 27,
                style.mutedColor(), false);
        for (int index = 0; index < snapshot.actions().size(); index++) {
            int rowY = y + HEADER + index * ROW_HEIGHT;
            PlayerContextActionSnapshotPayload.Entry entry = snapshot.actions().get(index);
            ElarionCivicUi.compactActionButton(context, textRenderer, x + 10, rowY, WIDTH - 20, 20, entry.label(),
                    inside(mouseX, mouseY, x + 10, rowY, WIDTH - 20, 20), false, true, ElarionCivicUi.Tone.PRIMARY, style);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return true;
        int height = HEADER + snapshot.actions().size() * ROW_HEIGHT + 8;
        int x = Math.max(6, Math.min(width - WIDTH - 6, originX - WIDTH / 2));
        int y = Math.max(6, Math.min(this.height - height - 6, originY - height - 18));
        for (int index = 0; index < snapshot.actions().size(); index++) {
            int rowY = y + HEADER + index * ROW_HEIGHT;
            if (inside(mouseX, mouseY, x + 10, rowY, WIDTH - 20, 20)) {
                ClientPlayNetworking.send(new PlayerContextActionExecutePayload(snapshot.targetId(), snapshot.actions().get(index).id()));
                close();
                return true;
            }
        }
        close();
        return true;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // The menu is opened with Shift + right click.  Closing on every key event also
        // closed it as the Shift key was released, before the player could select an action.
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) close();
        return true;
    }
    @Override public void close() { if (client != null) client.setScreen(null); }
    private static boolean inside(double x, double y, int left, int top, int width, int height) { return x >= left && x < left + width && y >= top && y < top + height; }
}
