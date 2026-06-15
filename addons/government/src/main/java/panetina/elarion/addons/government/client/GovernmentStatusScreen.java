package panetina.elarion.addons.government.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.government.network.GovernmentUiActionPayload;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.util.ArrayList;
import java.util.List;

public class GovernmentStatusScreen extends Screen {
    private static final int PADDING = 18;
    private static final int GAP = 8;
    private static final int HEADER_HEIGHT = 62;
    private static final int CLOSE_WIDTH = 120;
    private static final int CLOSE_HEIGHT = 20;
    private static final int PRIMARY_WIDTH = 150;
    private static final int PRIMARY_HEIGHT = 20;
    private static final int ROW_HEIGHT = 38;

    private final GovernmentUiOpenPayload payload;
    private final List<Hit> hits = new ArrayList<>();
    private ElarionScaledLayout layout;
    private ElarionUiThemeVariant theme;
    private ElarionUiStyle style;
    private boolean proposalOverlay;
    private boolean tagActive;
    private String nameInput = "";
    private String tagInput = "";
    private int nameProposalFirstVisible;
    private int electionFirstVisible;
    private final long messageExpiresAt;

    public GovernmentStatusScreen(GovernmentUiOpenPayload payload) {
        super(Text.literal(payload.title()));
        this.payload = payload;
        this.messageExpiresAt = payload.message().isBlank() ? 0L : System.currentTimeMillis() + 2500L;
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
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        hits.clear();

        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionUiRenderer.panel(context, 0, 0, payload.logicalWidth(), payload.logicalHeight(), theme);
        ElarionUiRenderer.headerBand(context, 4, 4, payload.logicalWidth() - 8, HEADER_HEIGHT, style);
        renderHeader(context);
        renderContent(context, lx, ly);
        renderFooter(context, lx, ly);
        if (proposalOverlay) renderProposalOverlay(context, lx, ly);

        context.getMatrices().pop();
    }

    private void renderHeader(DrawContext context) {
        int textWidth = payload.logicalWidth() - PADDING * 2;
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, payload.title(), textWidth),
                PADDING, 16, theme.titleColor(), false);
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, payload.realmName(), textWidth),
                PADDING, 31, theme.textColor(), false);
        if (payload.voteEndsAt() > System.currentTimeMillis()) {
            String remaining = formatRemaining(payload.voteEndsAt() - System.currentTimeMillis());
            context.drawText(textRenderer, remaining,
                    payload.logicalWidth() - PADDING - textRenderer.getWidth(remaining),
                    31, theme.warningColor(), false);
        }
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, payload.subtitle(), textWidth),
                PADDING, 46, theme.mutedColor(), false);
    }

    private void renderContent(DrawContext context, double mouseX, double mouseY) {
        int top = HEADER_HEIGHT + GAP + 8;
        int bottom = payload.logicalHeight() - PADDING - CLOSE_HEIGHT - GAP;
        int width = payload.logicalWidth() - PADDING * 2;
        ElarionUiRenderer.borderedBox(context, PADDING, top, width, bottom - top, style);
        int contentX = PADDING + 10;
        int contentY = top + 10;
        int contentWidth = width - 20;

        if ("civic_name".equals(payload.screenType())) {
            renderNameScreen(context, contentX, contentY, contentWidth, bottom - contentY - 34, mouseX, mouseY);
            renderPrimaryButton(context, contentX, bottom - 28, contentWidth, mouseX, mouseY);
        } else if ("civic_form".equals(payload.screenType())) {
            renderFormCards(context, contentX, contentY, contentWidth, bottom - contentY - 6, mouseX, mouseY);
        } else if ("civic_election".equals(payload.screenType())) {
            renderElectionScreen(context, contentX, contentY, contentWidth, bottom - contentY - 34, mouseX, mouseY);
            renderPrimaryButton(context, contentX, bottom - 28, contentWidth, mouseX, mouseY);
        } else if ("seat_of_rule".equals(payload.screenType())) {
            renderSeat(context, contentX, contentY, contentWidth, bottom - contentY - 6, mouseX, mouseY);
        } else {
            renderRows(context, primaryRows(), contentX, contentY, contentWidth, bottom - contentY - 34,
                    actionForRows(), mouseX, mouseY);
            renderPrimaryButton(context, contentX, bottom - 28, contentWidth, mouseX, mouseY);
        }
        renderMessageToast(context, contentX, top + 8, contentWidth);
    }

    private List<GovernmentUiOpenPayload.Row> primaryRows() {
        if ("civic_election".equals(payload.screenType())) {
            List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>(payload.stageRows());
            rows.addAll(payload.officeRows());
            return rows;
        }
        if ("civic_features".equals(payload.screenType())) {
            List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>(payload.stageRows());
            rows.addAll(payload.moduleRows());
            return rows;
        }
        return payload.stageRows();
    }

    private String actionForRows() {
        if ("civic_name".equals(payload.screenType()) || "civic_election".equals(payload.screenType())) return "vote";
        if ("civic_features".equals(payload.screenType())) return "open_module";
        return "";
    }

    private void renderRows(
            DrawContext context,
            List<GovernmentUiOpenPayload.Row> rows,
            int x,
            int y,
            int width,
            int height,
            String action,
            double mouseX,
            double mouseY
    ) {
        int rowY = y;
        int maximumRows = Math.max(1, height / ROW_HEIGHT);
        int visible = Math.min(rows.size(), maximumRows);
        for (int index = 0; index < visible; index++) {
            GovernmentUiOpenPayload.Row row = rows.get(index);
            boolean clickable = row.unlocked() && !payload.locked() && payload.eligible()
                    && !row.id().equals("rules") && !row.id().equals("empty")
                    && !action.isBlank();
            renderRow(context, row, x, rowY, width, ROW_HEIGHT - 4, clickable, mouseX, mouseY);
            if (clickable) hits.add(new Hit(x, rowY, width, ROW_HEIGHT - 4, action, row.id()));
            rowY += ROW_HEIGHT;
        }
        if (rows.size() > visible) {
            context.drawText(textRenderer, "...", x, y + height - 10, theme.mutedColor(), false);
        }
    }

    private void renderNameScreen(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        List<GovernmentUiOpenPayload.Row> proposals = payload.stageRows();
        if (proposals.isEmpty()) {
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal("Waiting for proposals."),
                    x, y, width, height, theme.mutedColor(), theme.mutedColor());
        } else if (proposals.size() == 1 && proposals.getFirst().id().equals("empty")) {
            renderCard(context, proposals.getFirst(), x + width / 2 - 92, y + 10, 184, 62,
                    false, mouseX, mouseY);
        } else {
            int columns = 3;
            int cardGap = 6;
            int cardWidth = (width - cardGap * (columns - 1)) / columns;
            int cardHeight = 56;
            int visibleRows = Math.max(1, height / (cardHeight + cardGap));
            int visibleCapacity = visibleRows * columns;
            int maxFirst = Math.max(0, proposals.size() - visibleCapacity);
            nameProposalFirstVisible = Math.max(0, Math.min(nameProposalFirstVisible, maxFirst));
            int visible = Math.min(proposals.size() - nameProposalFirstVisible, visibleCapacity);
            for (int index = 0; index < visible; index++) {
                GovernmentUiOpenPayload.Row row = proposals.get(index + nameProposalFirstVisible);
                int column = index % columns;
                int rowIndex = index / columns;
                int cardX = x + column * (cardWidth + cardGap);
                int cardY = y + rowIndex * (cardHeight + cardGap);
                boolean clickable = row.unlocked() && !payload.locked() && payload.eligible();
                renderProposalCard(context, row, cardX, cardY, cardWidth, cardHeight, clickable, mouseX, mouseY);
                if (clickable) hits.add(new Hit(cardX, cardY, cardWidth, cardHeight, "vote", row.id()));
            }
            if (nameProposalFirstVisible > 0) {
                drawArrowHint(context, x + width - 12, y + 2, true);
            }
            if (nameProposalFirstVisible + visible < proposals.size()) {
                drawArrowHint(context, x + width - 12, y + height - 12, false);
            }
        }
        if (payload.locked()) {
            renderLocalLockOverlay(context, x, y, width, height,
                    "Locked until Foundation I at the Shrine is completed.",
                    "Name: 3-24 characters, maximum two words. Government and settlement terms "
                            + "(Kingdom, Empire, City, Holy Land, Republic, and similar titles) are not allowed.\n"
                            + "Examples after founding: Kingdom of Oak, Republic of Oak, Holy Oak, "
                            + "or Oak Confederation.");
        }
    }

    private void drawArrowHint(DrawContext context, int x, int y, boolean up) {
        int color = theme.titleColor();
        if (up) {
            context.fill(x + 3, y, x + 5, y + 2, color);
            context.fill(x + 2, y + 2, x + 6, y + 4, color);
            context.fill(x + 1, y + 4, x + 7, y + 6, color);
        } else {
            context.fill(x + 1, y, x + 7, y + 2, color);
            context.fill(x + 2, y + 2, x + 6, y + 4, color);
            context.fill(x + 3, y + 4, x + 5, y + 6, color);
        }
    }

    private void renderFormCards(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        if (!payload.stageRows().isEmpty()) {
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(payload.stageRows().get(0).body()),
                    x, y, width, 28, theme.textColor(), theme.mutedColor());
            y += 34;
            height -= 34;
        }
        int columns = 4;
        int cardGap = 6;
        int cardWidth = (width - cardGap * (columns - 1)) / columns;
        int cardHeight = Math.min(142, Math.max(92, height - 2));
        for (int index = 0; index < payload.formRows().size(); index++) {
            GovernmentUiOpenPayload.Row row = payload.formRows().get(index);
            int cardX = x + index * (cardWidth + cardGap);
            if (cardX + cardWidth > x + width) break;
            boolean clickable = row.unlocked() && !payload.locked() && payload.eligible();
            renderCard(context, row, cardX, y, cardWidth, cardHeight, clickable, mouseX, mouseY);
            if (clickable) hits.add(new Hit(cardX, y, cardWidth, cardHeight, "vote", row.id()));
        }
    }

    private void renderElectionScreen(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        if (!payload.stageRows().isEmpty()) {
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(payload.stageRows().get(0).body()),
                    x, y, width, 30, theme.textColor(), theme.mutedColor());
            y += 36;
            height -= 36;
        }
        List<GovernmentUiOpenPayload.Row> candidates = payload.officeRows();
        if (candidates.isEmpty() || candidates.stream().allMatch(row -> row.id().equals("empty"))) {
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal("No candidates have nominated yet."),
                    x, y + 8, width, height, theme.mutedColor(), theme.mutedColor());
            return;
        }
        int columns = 3;
        int cardGap = 6;
        int cardWidth = (width - cardGap * (columns - 1)) / columns;
        int cardHeight = 56;
        int visibleRows = Math.max(1, height / (cardHeight + cardGap));
        int visibleCapacity = visibleRows * columns;
        int maxFirst = Math.max(0, candidates.size() - visibleCapacity);
        electionFirstVisible = Math.max(0, Math.min(electionFirstVisible, maxFirst));
        int visible = Math.min(candidates.size() - electionFirstVisible, visibleCapacity);
        for (int index = 0; index < visible; index++) {
            GovernmentUiOpenPayload.Row row = candidates.get(index + electionFirstVisible);
            int column = index % columns;
            int rowIndex = index / columns;
            int cardX = x + column * (cardWidth + cardGap);
            int cardY = y + rowIndex * (cardHeight + cardGap);
            boolean clickable = row.unlocked() && !payload.locked() && payload.eligible() && !row.id().equals("empty");
            renderProposalCard(context, row, cardX, cardY, cardWidth, cardHeight, clickable, mouseX, mouseY);
            if (clickable) hits.add(new Hit(cardX, cardY, cardWidth, cardHeight, "vote", row.id()));
        }
        if (electionFirstVisible > 0) drawArrowHint(context, x + width - 12, y + 2, true);
        if (electionFirstVisible + visible < candidates.size()) drawArrowHint(context, x + width - 12, y + height - 12, false);
    }

    private void renderSeat(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        int leftWidth = 220;
        renderRows(context, payload.stageRows(), x, y, leftWidth, height / 2 - GAP, "", mouseX, mouseY);
        renderRows(context, payload.formRows(), x, y + height / 2, leftWidth, height / 2, "", mouseX, mouseY);
        int rightX = x + leftWidth + GAP;
        int rightWidth = width - leftWidth - GAP;
        int officeHeight = Math.max(86, height / 2 - GAP);
        renderRows(context, payload.officeRows(), rightX, y, rightWidth, officeHeight, "", mouseX, mouseY);
        renderRows(context, payload.moduleRows(), rightX, y + officeHeight + GAP, rightWidth,
                height - officeHeight - GAP, "open_module", mouseX, mouseY);
        if (payload.locked()) {
            renderLocalLockOverlay(context, x, y, width, height,
                    "Seat of Rule Locked",
                    "Complete Foundation III and the founding election before authority modules become available.");
        }
    }

    private void renderRow(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int width,
            int height,
            boolean clickable,
            double mouseX,
            double mouseY
    ) {
        boolean hovered = clickable && inside(mouseX, mouseY, x, y, width, height);
        int fill = row.complete() ? theme.buttonHoverColor()
                : hovered ? theme.buttonHoverColor()
                : row.unlocked() ? theme.cardColor() : theme.insetColor();
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, style);
        int stateWidth = textRenderer.getWidth(row.state());
        int textColor = row.unlocked() || row.complete() ? theme.textColor() : theme.mutedColor();
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.title(),
                        width - stateWidth - 18),
                x + 7, y + 5, row.complete() ? theme.titleColor() : textColor, false);
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.state(), 88),
                x + width - stateWidth - 7, y + 5, row.complete() ? theme.warningColor()
                        : row.unlocked() ? theme.warningColor() : theme.mutedColor(), false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(row.body()),
                x + 7, y + 18, width - 14, height - 20, textColor, theme.mutedColor());
    }

    private void renderCard(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int width,
            int height,
            boolean clickable,
            double mouseX,
            double mouseY
    ) {
        boolean hovered = clickable && inside(mouseX, mouseY, x, y, width, height);
        boolean selected = "Your vote".equals(row.state());
        int fill = selected ? theme.buttonHoverColor()
                : row.complete() ? theme.buttonHoverColor()
                : hovered ? theme.buttonHoverColor()
                : row.unlocked() ? theme.cardColor() : theme.insetColor();
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, style);
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.title(), width - 12),
                x + 6, y + 7, row.complete() ? theme.titleColor() : theme.textColor(), false);
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, row.state(), width - 12),
                x + 6, y + 21, row.complete() ? theme.warningColor()
                        : row.unlocked() ? theme.warningColor() : theme.mutedColor(), false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(row.body()),
                x + 6, y + 38, width - 12, height - 44,
                row.unlocked() ? theme.textColor() : theme.mutedColor(), theme.mutedColor());
    }

    private void renderProposalCard(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int width,
            int height,
            boolean clickable,
            double mouseX,
            double mouseY
    ) {
        boolean hovered = clickable && inside(mouseX, mouseY, x, y, width, height);
        boolean selected = "Your vote".equals(row.state());
        int fill = selected ? theme.buttonHoverColor()
                : hovered ? theme.buttonHoverColor()
                : row.unlocked() ? theme.cardColor() : theme.insetColor();
        ElarionUiRenderer.beveledBox(context, x, y, width, height, fill, style);
        String title = ElarionUiRenderer.ellipsize(textRenderer, row.title(), width - 12);
        String body = ElarionUiRenderer.ellipsize(textRenderer, row.body(), width - 12);
        context.drawText(textRenderer, title,
                x + (width - textRenderer.getWidth(title)) / 2, y + 8,
                selected ? theme.titleColor() : theme.textColor(), false);
        context.drawText(textRenderer, body,
                x + (width - textRenderer.getWidth(body)) / 2, y + 24,
                row.unlocked() || selected ? theme.textColor() : theme.mutedColor(), false);
        String state = row.state();
        if (!"Proposed".equals(state)) {
            int stateWidth = textRenderer.getWidth(state);
            context.drawText(textRenderer, state, x + (width - stateWidth) / 2, y + height - 13,
                    selected ? theme.titleColor() : theme.warningColor(), false);
        }
    }

    private void renderMessageToast(DrawContext context, int x, int y, int width) {
        if (payload.message().isBlank() || System.currentTimeMillis() >= messageExpiresAt) return;
        int toastWidth = Math.min(width - 20, Math.max(160, textRenderer.getWidth(payload.message()) + 18));
        int toastX = x + (width - toastWidth) / 2;
        int toastY = payload.logicalHeight() / 2 - 11;
        int color = payload.locked() ? theme.warningColor()
                : payload.eligible() ? style.feedbackColor() : theme.errorColor();
        ElarionUiRenderer.beveledBox(context, toastX, toastY, toastWidth, 22, theme.insetColor(), style);
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, payload.message(), toastWidth - 12),
                toastX + 6, toastY + 7, color, false);
    }

    private void renderPrimaryButton(DrawContext context, int x, int y, int width, double mouseX, double mouseY) {
        if (payload.primaryAction().isBlank()) return;
        String label = "nominate_self".equals(payload.primaryAction()) ? "Nominate Yourself" : "Propose Name";
        int buttonX = x + (width - PRIMARY_WIDTH) / 2;
        boolean active = !payload.locked() && payload.eligible();
        boolean hovered = active && inside(mouseX, mouseY, buttonX, y, PRIMARY_WIDTH, PRIMARY_HEIGHT);
        ElarionUiRenderer.compactButton(context, textRenderer, buttonX, y, PRIMARY_WIDTH, PRIMARY_HEIGHT,
                label, hovered, active, style);
        if (active) hits.add(new Hit(buttonX, y, PRIMARY_WIDTH, PRIMARY_HEIGHT, payload.primaryAction(), ""));
    }

    private void renderFooter(DrawContext context, double mouseX, double mouseY) {
        int x = (payload.logicalWidth() - CLOSE_WIDTH) / 2;
        int y = payload.logicalHeight() - PADDING - CLOSE_HEIGHT;
        ElarionUiRenderer.compactButton(context, textRenderer, x, y, CLOSE_WIDTH, CLOSE_HEIGHT,
                "Close", inside(mouseX, mouseY, x, y, CLOSE_WIDTH, CLOSE_HEIGHT), true, style);
        hits.add(new Hit(x, y, CLOSE_WIDTH, CLOSE_HEIGHT, "close", ""));
    }

    private void renderProposalOverlay(DrawContext context, double mouseX, double mouseY) {
        int overlayWidth = 330;
        int overlayHeight = 116;
        int x = (payload.logicalWidth() - overlayWidth) / 2;
        int y = (payload.logicalHeight() - overlayHeight) / 2;
        context.fill(0, 0, payload.logicalWidth(), payload.logicalHeight(), 0xAA000000);
        ElarionUiRenderer.borderedBox(context, x, y, overlayWidth, overlayHeight, style);
        context.drawText(textRenderer, "Propose Realm Identity", x + 10, y + 10, theme.titleColor(), false);
        renderInput(context, x + 10, y + 30, overlayWidth - 20, "Realm name", nameInput, !tagActive);
        renderInput(context, x + 10, y + 58, overlayWidth - 20, "Tag", tagInput, tagActive);
        int cancelX = x + overlayWidth - 150;
        int submitX = x + overlayWidth - 76;
        int buttonY = y + overlayHeight - 26;
        ElarionUiRenderer.compactButton(context, textRenderer, cancelX, buttonY, 66, 18,
                "Cancel", inside(mouseX, mouseY, cancelX, buttonY, 66, 18), true, style);
        ElarionUiRenderer.compactButton(context, textRenderer, submitX, buttonY, 66, 18,
                "Submit", inside(mouseX, mouseY, submitX, buttonY, 66, 18), !nameInput.isBlank() && !tagInput.isBlank(),
                style);
        hits.add(new Hit(cancelX, buttonY, 66, 18, "overlay_cancel", ""));
        hits.add(new Hit(submitX, buttonY, 66, 18, "overlay_submit", ""));
    }

    private void renderInput(DrawContext context, int x, int y, int width, String label, String value, boolean active) {
        ElarionUiRenderer.beveledBox(context, x, y, width, 22, active ? theme.buttonHoverColor() : theme.insetColor(),
                style);
        String text = value.isBlank() ? label : value;
        int color = value.isBlank() ? theme.mutedColor() : theme.textColor();
        if (active && (System.currentTimeMillis() / 450L) % 2L == 0L) text += "_";
        context.drawText(textRenderer, ElarionUiRenderer.ellipsize(textRenderer, text, width - 10),
                x + 5, y + 7, color, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        if (proposalOverlay) {
            int overlayWidth = 330;
            int x = (payload.logicalWidth() - overlayWidth) / 2;
            int y = (payload.logicalHeight() - 116) / 2;
            if (inside(lx, ly, x + 10, y + 30, overlayWidth - 20, 22)) {
                tagActive = false;
                return true;
            }
            if (inside(lx, ly, x + 10, y + 58, overlayWidth - 20, 22)) {
                tagActive = true;
                return true;
            }
        }
        for (Hit hit : List.copyOf(hits)) {
            if (!inside(lx, ly, hit.x, hit.y, hit.width, hit.height)) continue;
            runHit(hit);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if ((!"civic_name".equals(payload.screenType()) && !"civic_election".equals(payload.screenType()))
                || proposalOverlay) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        int proposalCount = "civic_name".equals(payload.screenType())
                ? (int) payload.stageRows().stream().filter(row -> !row.id().equals("empty")).count()
                : (int) payload.officeRows().stream().filter(row -> !row.id().equals("empty")).count();
        if (proposalCount <= 0) return false;
        int top = HEADER_HEIGHT + GAP + 8;
        int bottom = payload.logicalHeight() - PADDING - CLOSE_HEIGHT - GAP;
        int contentTop = top + 10;
        int contentHeight = bottom - contentTop - 34;
        if ("civic_election".equals(payload.screenType())) contentHeight -= 36;
        int columns = 3;
        int visibleRows = Math.max(1, contentHeight / (56 + 6));
        int visibleCapacity = visibleRows * columns;
        int maxFirst = Math.max(0, proposalCount - visibleCapacity);
        if (maxFirst <= 0) return false;
        int direction = verticalAmount > 0 ? -columns : verticalAmount < 0 ? columns : 0;
        if (direction == 0) return false;
        if ("civic_name".equals(payload.screenType())) {
            nameProposalFirstVisible = Math.max(0, Math.min(maxFirst, nameProposalFirstVisible + direction));
        } else {
            electionFirstVisible = Math.max(0, Math.min(maxFirst, electionFirstVisible + direction));
        }
        return true;
    }

    private void runHit(Hit hit) {
        switch (hit.action) {
            case "close" -> close();
            case "propose_name" -> {
                proposalOverlay = true;
                tagActive = false;
            }
            case "overlay_cancel" -> proposalOverlay = false;
            case "overlay_submit" -> submitNameProposal();
            case "vote", "nominate_self", "open_module" -> sendAction(hit.action, hit.target, "", "");
            default -> {
            }
        }
    }

    private void submitNameProposal() {
        if (nameInput.isBlank() || tagInput.isBlank()) return;
        sendAction("propose_name", "", nameInput, tagInput);
        proposalOverlay = false;
    }

    private void sendAction(String action, String target, String value, String secondary) {
        ClientPlayNetworking.send(new GovernmentUiActionPayload(
                payload.screenType(), action, payload.realmId(), target, value, secondary, payload.sessionId()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (proposalOverlay) {
                proposalOverlay = false;
                return true;
            }
            close();
            return true;
        }
        if (proposalOverlay) {
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                tagActive = !tagActive;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                submitNameProposal();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (tagActive && !tagInput.isEmpty()) tagInput = tagInput.substring(0, tagInput.length() - 1);
                else if (!tagActive && !nameInput.isEmpty()) nameInput = nameInput.substring(0, nameInput.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (tagActive) tagInput = "";
                else nameInput = "";
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!proposalOverlay) return super.charTyped(chr, modifiers);
        if (tagActive) {
            if (tagInput.length() >= 6 || !Character.toString(chr).matches("[A-Za-z0-9]")) return false;
            tagInput += Character.toUpperCase(chr);
        } else {
            if (nameInput.length() >= 24 || (Character.isISOControl(chr) && chr != ' ')) return false;
            if (Character.isLetterOrDigit(chr) || chr == ' ' || chr == '-' || chr == '\'') nameInput += chr;
            else return false;
        }
        return true;
    }

    @Override
    public void blur() {
    }

    @Override
    protected void applyBlur(float delta) {
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void renderLocalLockOverlay(
            DrawContext context,
            int x,
            int y,
            int width,
            int height,
            String title,
            String body
    ) {
        context.fill(x, y, x + width, y + height, 0xC816100C);
        int boxWidth = Math.min(width - 30, 360);
        int boxHeight = Math.min(height - 24, 112);
        int boxX = x + (width - boxWidth) / 2;
        int boxY = y + (height - boxHeight) / 2;
        ElarionUiRenderer.borderedBox(context, boxX, boxY, boxWidth, boxHeight, style);
        String shownTitle = ElarionUiRenderer.ellipsize(textRenderer, title, boxWidth - 20);
        context.drawText(textRenderer, shownTitle,
                boxX + (boxWidth - textRenderer.getWidth(shownTitle)) / 2,
                boxY + 12, theme.titleColor(), false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(body),
                boxX + 12, boxY + 32, boxWidth - 24, boxHeight - 42,
                theme.textColor(), theme.mutedColor());
    }

    private static String formatRemaining(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (hours > 0L) return hours + "h " + minutes + "m";
        return Math.max(1L, minutes) + "m";
    }

    private record Hit(int x, int y, int width, int height, String action, String target) {
    }
}
