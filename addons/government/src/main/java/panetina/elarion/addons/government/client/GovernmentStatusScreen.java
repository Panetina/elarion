package panetina.elarion.addons.government.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.util.List;

public final class GovernmentStatusScreen extends Screen {
    private static final int PADDING = 24;
    private static final int GAP = 10;
    private static final int HEADER_HEIGHT = 78;
    private static final int ROW_HEIGHT = 42;
    private static final int CLOSE_WIDTH = 130;
    private static final int CLOSE_HEIGHT = 20;

    private final GovernmentUiOpenPayload payload;
    private ElarionScaledLayout layout;
    private ElarionUiThemeVariant theme;
    private ElarionUiStyle style;

    public GovernmentStatusScreen(GovernmentUiOpenPayload payload) {
        super(Text.literal(payload.title()));
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
        double logicalMouseX = layout.logicalX(mouseX);
        double logicalMouseY = layout.logicalY(mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionUiRenderer.panel(context, 0, 0, payload.logicalWidth(), payload.logicalHeight(), theme);
        ElarionUiRenderer.headerBand(context, 4, 4, payload.logicalWidth() - 8, HEADER_HEIGHT, style);
        renderHeader(context);
        if ("seat_of_rule".equals(payload.screenType())) renderSeat(context);
        else renderCivicForum(context);
        renderClose(context, logicalMouseX, logicalMouseY);

        context.getMatrices().pop();
    }

    private void renderHeader(DrawContext context) {
        String title = ElarionUiRenderer.ellipsize(textRenderer, payload.title(), payload.logicalWidth() - PADDING * 2);
        String subtitle = ElarionUiRenderer.ellipsize(textRenderer, payload.subtitle(),
                payload.logicalWidth() - PADDING * 2);
        String realm = ElarionUiRenderer.ellipsize(textRenderer, payload.realmName(),
                payload.logicalWidth() - PADDING * 2);
        context.drawText(textRenderer, title, PADDING, 18, theme.titleColor(), false);
        context.drawText(textRenderer, realm, PADDING, 34, theme.textColor(), false);
        context.drawText(textRenderer, subtitle, PADDING, 50, theme.mutedColor(), false);
    }

    private void renderCivicForum(DrawContext context) {
        int top = HEADER_HEIGHT + GAP + 8;
        int leftWidth = 330;
        int rightX = PADDING + leftWidth + GAP;
        int rightWidth = payload.logicalWidth() - rightX - PADDING;
        int contentBottom = payload.logicalHeight() - PADDING - CLOSE_HEIGHT - GAP;

        renderSection(context, "Founding Stages", payload.stageRows(), PADDING, top, leftWidth,
                contentBottom - top);
        renderSection(context, "Government Forms", payload.formRows(), rightX, top, rightWidth,
                (contentBottom - top) * 2 / 3 - GAP);
        int moduleY = top + (contentBottom - top) * 2 / 3;
        renderSection(context, "Future Civic Modules", payload.moduleRows(), rightX, moduleY, rightWidth,
                contentBottom - moduleY);
    }

    private void renderSeat(DrawContext context) {
        int top = HEADER_HEIGHT + GAP + 8;
        int leftWidth = 300;
        int centerWidth = 250;
        int rightX = PADDING + leftWidth + GAP + centerWidth + GAP;
        int rightWidth = payload.logicalWidth() - rightX - PADDING;
        int contentBottom = payload.logicalHeight() - PADDING - CLOSE_HEIGHT - GAP;

        renderSection(context, "Authority State", payload.stageRows(), PADDING, top, leftWidth,
                120);
        renderSection(context, "Government", payload.formRows(), PADDING, top + 130, leftWidth,
                contentBottom - top - 130);
        renderSection(context, "Offices", payload.officeRows(), PADDING + leftWidth + GAP, top,
                centerWidth, contentBottom - top);
        renderSection(context, "Seat Modules", payload.moduleRows(), rightX, top, rightWidth,
                contentBottom - top);
    }

    private void renderSection(
            DrawContext context,
            String title,
            List<GovernmentUiOpenPayload.Row> rows,
            int x,
            int y,
            int width,
            int height
    ) {
        ElarionUiRenderer.borderedBox(context, x, y, width, height, style);
        context.drawText(textRenderer, title, x + 8, y + 8, theme.titleColor(), false);
        int rowY = y + 24;
        int rowWidth = width - 16;
        int maxRows = Math.max(0, (height - 32) / ROW_HEIGHT);
        List<GovernmentUiOpenPayload.Row> visible = rows == null ? List.of()
                : rows.stream().limit(maxRows).toList();
        if (visible.isEmpty()) {
            context.drawText(textRenderer, "Nothing to show yet.", x + 8, rowY, theme.mutedColor(), false);
            return;
        }
        for (GovernmentUiOpenPayload.Row row : visible) {
            renderRow(context, row, x + 8, rowY, rowWidth, ROW_HEIGHT - 4);
            rowY += ROW_HEIGHT;
        }
        if (rows != null && rows.size() > visible.size()) {
            context.drawText(textRenderer, "...", x + 8, y + height - 14, theme.mutedColor(), false);
        }
    }

    private void renderRow(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int width,
            int height
    ) {
        int fill = row.complete() ? theme.progressCompleteColor()
                : row.unlocked() ? theme.buttonHoverColor() : theme.cardColor();
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, style);
        int stateColor = row.complete() ? theme.successColor()
                : row.unlocked() ? theme.warningColor() : theme.mutedColor();
        String state = ElarionUiRenderer.ellipsize(textRenderer, row.state(), 78);
        int stateWidth = textRenderer.getWidth(state);
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.title(),
                        width - stateWidth - 24),
                x + 7, y + 6, row.unlocked() || row.complete() ? theme.textColor() : theme.mutedColor(), false);
        context.drawText(textRenderer, state, x + width - stateWidth - 7, y + 6, stateColor, false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(row.body()),
                x + 7, y + 19, width - 14, height - 22,
                row.unlocked() || row.complete() ? theme.textColor() : theme.mutedColor(), theme.mutedColor());
    }

    private void renderClose(DrawContext context, double mouseX, double mouseY) {
        int x = (payload.logicalWidth() - CLOSE_WIDTH) / 2;
        int y = payload.logicalHeight() - PADDING - CLOSE_HEIGHT;
        ElarionUiRenderer.compactButton(context, textRenderer, x, y, CLOSE_WIDTH, CLOSE_HEIGHT,
                "Close", inside(mouseX, mouseY, x, y, CLOSE_WIDTH, CLOSE_HEIGHT), true, style);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        int x = (payload.logicalWidth() - CLOSE_WIDTH) / 2;
        int y = payload.logicalHeight() - PADDING - CLOSE_HEIGHT;
        if (inside(lx, ly, x, y, CLOSE_WIDTH, CLOSE_HEIGHT)) {
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
