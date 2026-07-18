package panetina.elarion.addons.government.client.seat;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.government.client.GovernmentScreenChrome;
import panetina.elarion.addons.government.client.GovernmentUiComponents;
import panetina.elarion.addons.government.client.GovernmentUiGlyphs;
import panetina.elarion.addons.government.network.GovernmentUiActionPayload;
import panetina.elarion.addons.government.network.GovernmentUiOpenPayload;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.util.ArrayList;
import java.util.List;

public final class SeatOfRuleScreen extends ElarionScreen {
    public static final int LOGICAL_WIDTH = 760;
    public static final int LOGICAL_HEIGHT = 500;
    public static final int READABLE_MINIMUM_SCALE_PERCENT = 75;
    private static final int HEADER_HEIGHT = 46;
    private static final int BODY_Y = HEADER_HEIGHT - 1;
    private static final int TAB_Y = 54;
    private static final int TAB_HEIGHT = 22;
    private static final int CONTENT_TOP = 82;
    private static final int LEFT_X = 18;
    private static final int LEFT_WIDTH = 380;
    private static final int RIGHT_X = 410;
    private static final int RIGHT_WIDTH = 332;
    private static final int MAIN_BOTTOM = 428;
    private static final int BOTTOM_Y = 436;
    private static final int BOTTOM_HEIGHT = 38;
    private static final int ROWS_Y = CONTENT_TOP + 34;
    private static final int ROW_HEIGHT = 42;
    private static final int ROW_GAP = 4;
    private static final int TAB_GAP = 8;
    private static final int HEADER_META_X = 58;
    private static final int HEADER_META_RIGHT = LOGICAL_WIDTH - 40;
    private static final int HEADER_META_COUNT = 4;
    private static final int HEADER_CLOSE_X = LOGICAL_WIDTH - 24;

    private final GovernmentUiOpenPayload payload;
    private final List<Hit> hits = new ArrayList<>();
    private ElarionScaledLayout layout;
    private ElarionUiThemeVariant theme;
    private ElarionUiStyle style;
    private String selectedRowId;
    private int rowFirstVisible;
    private String activeTabOverride = "";
    private boolean overlay;
    private boolean bodyActive;
    private String overlayAction = "";
    private String overlayTarget = "";
    private String titleInput = "";
    private String bodyInput = "";

    public SeatOfRuleScreen(GovernmentUiOpenPayload payload) {
        super(Text.literal("Seat of Rule"));
        this.payload = payload;
        this.selectedRowId = firstSelectable(rowsForActiveTab()).id();
    }

    public static Layout layoutMetrics() {
        return new Layout();
    }

    @Override
    protected void init() {
        theme = ElarionUiThemes.variant(payload.themeVariant());
        style = ElarionUiStyle.from(theme);
        layout = ElarionScaledLayout.fit(width, height, LOGICAL_WIDTH, LOGICAL_HEIGHT, 8,
                Math.max(READABLE_MINIMUM_SCALE_PERCENT, payload.minimumScalePercent()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, theme.backgroundOverlayColor());
        hits.clear();
        if (layout.belowPreferredScale()) {
            renderTooSmall(context, "Seat of Rule");
            return;
        }
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);
        GovernmentUiGlyphs.civicShell(context, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, HEADER_HEIGHT, style);
        renderHeader(context, lx, ly);
        renderTabs(context, lx, ly);
        renderContent(context, lx, ly);
        if (overlay) renderOverlay(context, lx, ly);
        context.getMatrices().pop();
    }

    private void renderTooSmall(DrawContext context, String title) {
        int panelWidth = Math.min(width - 24, 360);
        int panelHeight = 92;
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;
        ElarionUiRenderer.borderedBox(context, x, y, panelWidth, panelHeight, style);
        ElarionUiTypography.draw(context, textRenderer, title, x + 14, y + 14, theme.titleColor(), false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer,
                Text.literal("Window too small for readable authority text. Resize the window or lower GUI scale."),
                x + 14, y + 34, panelWidth - 28, 44, theme.textColor(), theme.mutedColor());
    }

    private void renderHeader(DrawContext context, double mouseX, double mouseY) {
        GovernmentUiGlyphs.crest(context, 15, 7, 32, payload.crestIconId(), style);
        drawHeaderTitle(context, "Seat of Rule");
        context.fill(58, 24, HEADER_CLOSE_X - 18, 25, GovernmentUiGlyphs.GOLD_SHADOW);
        int metaY = 29;
        renderHeaderSegment(context, 0, metaY, "realm_name", payload.realmName(), GovernmentUiGlyphs.ACTIVE_GREEN);
        renderHeaderSegment(context, 1, metaY, "office", authorityLabel(), theme.titleColor());
        renderHeaderSegment(context, 2, metaY, "people", roleLabel(), theme.titleColor());
        renderHeaderColorSegment(context, 3, metaY);
        ElarionUiRenderer.compactButton(context, textRenderer, HEADER_CLOSE_X, 9, 16, 16,
                "X", inside(mouseX, mouseY, HEADER_CLOSE_X, 9, 16, 16), true, style);
        hits.add(new Hit(HEADER_CLOSE_X, 9, 16, 16, "close", ""));
    }

    private void drawHeaderTitle(DrawContext context, String title) {
        float scale = ElarionUiTypography.scale() * 1.25F;
        context.getMatrices().push();
        context.getMatrices().translate(58.0F, 9.0F, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(textRenderer, title, 0, 0, theme.titleColor(), false);
        context.getMatrices().pop();
    }

    private void renderHeaderSegment(
            DrawContext context,
            int index,
            int y,
            String iconId,
            String label,
            int color
    ) {
        int x = GovernmentScreenChrome.metadataSegmentX(HEADER_META_X, HEADER_META_RIGHT, HEADER_META_COUNT, index);
        int end = GovernmentScreenChrome.metadataSegmentEnd(HEADER_META_X, HEADER_META_RIGHT, HEADER_META_COUNT, index);
        GovernmentScreenChrome.drawHeaderSegment(context, textRenderer, x, y, end - x - 8, iconId, label, color,
                index > 0, style);
    }

    private void renderHeaderColorSegment(DrawContext context, int index, int y) {
        int x = GovernmentScreenChrome.metadataSegmentX(HEADER_META_X, HEADER_META_RIGHT, HEADER_META_COUNT, index);
        int end = GovernmentScreenChrome.metadataSegmentEnd(HEADER_META_X, HEADER_META_RIGHT, HEADER_META_COUNT, index);
        GovernmentScreenChrome.drawColorSegment(context, textRenderer, x, y, end - x - 8,
                payload.realmColor(), colorLabel(payload.realmColor()), index > 0, style);
    }

    private void renderTabs(DrawContext context, double mouseX, double mouseY) {
        GovernmentScreenChrome.Tab[] tabs = {
                new GovernmentScreenChrome.Tab("review", "Review", "proposal"),
                new GovernmentScreenChrome.Tab("laws", "Laws", "law"),
                new GovernmentScreenChrome.Tab("projects", "Projects", "project"),
                new GovernmentScreenChrome.Tab("offices", "Offices", "office"),
                new GovernmentScreenChrome.Tab("archive", "Archive", "archive")
        };
        int areaX = LEFT_X;
        int areaWidth = RIGHT_X + RIGHT_WIDTH - LEFT_X;
        for (int i = 0; i < tabs.length; i++) {
            GovernmentScreenChrome.Tab tab = tabs[i];
            int x = GovernmentScreenChrome.tabX(areaX, areaWidth, TAB_GAP, tabs.length, i);
            int width = GovernmentScreenChrome.tabWidth(areaWidth, TAB_GAP, tabs.length, i);
            boolean selected = activeTab().equals(tab.id());
            boolean hover = inside(mouseX, mouseY, x, TAB_Y, width, TAB_HEIGHT);
            GovernmentScreenChrome.drawTab(context, textRenderer, x, TAB_Y, width, TAB_HEIGHT, tab, selected, hover,
                    true, style);
            if (!selected) hits.add(new Hit(x, TAB_Y, width, TAB_HEIGHT, "tab", tab.id()));
        }
    }

    private void renderContent(DrawContext context, double mouseX, double mouseY) {
        GovernmentUiGlyphs.sectionBox(context, LEFT_X, CONTENT_TOP, LEFT_WIDTH, MAIN_BOTTOM - CONTENT_TOP, style);
        GovernmentUiGlyphs.sectionBox(context, RIGHT_X, CONTENT_TOP, RIGHT_WIDTH, MAIN_BOTTOM - CONTENT_TOP, style);
        renderRows(context, rowsForActiveTab(), mouseX, mouseY);
        renderDetail(context, selectedRow(), mouseX, mouseY);
        renderBottomBand(context);
    }

    private void renderRows(DrawContext context, List<GovernmentUiOpenPayload.Row> rows, double mouseX, double mouseY) {
        int x = LEFT_X + 10;
        int y = CONTENT_TOP + 12;
        int rowHeight = ROW_HEIGHT;
        int bottom = MAIN_BOTTOM - 10;
        ElarionUiTypography.draw(context, textRenderer, contentHeader(), x + 26, y + 1, theme.titleColor(), false);
        GovernmentUiGlyphs.icon(context, x, y - 2, 16, activeTabIcon(), style);
        y = ROWS_Y;
        List<GovernmentUiOpenPayload.Row> visibleRows = rows.stream()
                .filter(row -> !row.id().equals("empty"))
                .toList();
        if (visibleRows.isEmpty()) {
            String message = rows.isEmpty() ? "No authority records available." : rows.getFirst().body();
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(message),
                    x + 8, y + 8, LEFT_WIDTH - 36, 90, theme.mutedColor(), theme.mutedColor());
            return;
        }
        int capacity = visibleRowCapacity();
        int maxFirst = Math.max(0, visibleRows.size() - capacity);
        if (rowFirstVisible > maxFirst) rowFirstVisible = maxFirst;
        int drawn = 0;
        for (GovernmentUiOpenPayload.Row row : visibleRows.stream().skip(rowFirstVisible).toList()) {
            if (drawn >= capacity) break;
            if (y + rowHeight > bottom) break;
            boolean selected = row.id().equals(selectedRowId);
            boolean hover = inside(mouseX, mouseY, x, y, LEFT_WIDTH - 20, rowHeight);
            boolean office = row.kind().equals("office");
            boolean history = row.kind().equals("history");
            String metric = office
                    ? row.metricLabel().isBlank() ? row.state() : row.metricLabel()
                    : history ? row.metricLabel().isBlank() ? row.state() : row.metricLabel()
                    : row.voteCount() > 0 ? row.voteCount() + " votes" : row.state();
            String secondary = office
                    ? row.voteCount() + " / " + Math.max(1L, row.threshold()) + " seats"
                    : history ? row.createdAt() > 0L ? ageLabel(row.createdAt()).replace("Submitted ", "") : ""
                    : row.createdAt() > 0L ? ageLabel(row.createdAt()).replace("Submitted ", "") : "";
            GovernmentUiComponents.recordRow(context, textRenderer, row, x, y, LEFT_WIDTH - 20, rowHeight,
                    selected || row.selectedByViewer(), hover, !row.unlocked(), row.iconId(),
                    row.category().isBlank() ? row.state() : row.category(), metric,
                    office ? "timer" : history ? "history" : row.voteCount() > 0 ? "people" : "current_votes",
                    secondary, office ? "people" : "timer", theme, style);
            hits.add(new Hit(x, y, LEFT_WIDTH - 20, rowHeight, "select", row.id()));
            y += rowHeight + ROW_GAP;
            drawn++;
        }
        if (drawn < visibleRows.size()) {
            GovernmentUiGlyphs.rowRange(context, textRenderer, x + (LEFT_WIDTH - 20) / 2, bottom - 9,
                    rowFirstVisible + 1, rowFirstVisible + drawn, visibleRows.size(), style);
        }
    }

    private void renderDetail(DrawContext context, GovernmentUiOpenPayload.Row row, double mouseX, double mouseY) {
        int x = RIGHT_X + 14;
        int y = CONTENT_TOP + 16;
        if (row.id().equals("empty")) {
            ElarionUiTypography.draw(context, textRenderer, "No Selection", x, y, theme.titleColor(), false);
            renderPrimaryAction(context, x, MAIN_BOTTOM - 46, RIGHT_WIDTH - 28, mouseX, mouseY);
            return;
        }
        String actor = row.kind().equals("office")
                ? row.actorName().isBlank() ? "Vacant" : "Holder " + row.actorName()
                : row.actorName().isBlank() || row.actorName().equals(row.body()) ? "" : "By " + row.actorName();
        GovernmentUiComponents.detailHeader(context, textRenderer, row, x, y, RIGHT_WIDTH - 28,
                row.iconId(), row.category().isBlank() ? row.state() : row.category(), actor, theme, style);
        int bodyY = y + 62;
        GovernmentUiComponents.bodyText(context, textRenderer, stripCategory(row.body()),
                x, bodyY, RIGHT_WIDTH - 28, 56, theme);
        int voteY = bodyY + 68;
        if (row.kind().equals("office")) {
            GovernmentUiComponents.sectionTitle(context, textRenderer, x, voteY, "office",
                    "Office Details", theme, style);
            int lineY = voteY + 22;
            ElarionUiTypography.draw(context, textRenderer, row.metricLabel(), x, lineY, theme.textColor(), false);
            lineY += 16;
            ElarionUiTypography.draw(context, textRenderer, "Holder: " + (row.actorName().isBlank() ? "Vacant" : row.actorName()),
                    x, lineY, theme.textColor(), false);
            lineY += 16;
            ElarionUiTypography.draw(context, textRenderer, "Seats: " + row.voteCount() + " / " + row.threshold(),
                    x, lineY, theme.textColor(), false);
            lineY += 16;
            ElarionUiTypography.draw(context, textRenderer, "Approved: " + row.approveCount(), x, lineY, GovernmentUiGlyphs.ACTIVE_GREEN, false);
            ElarionUiTypography.draw(context, textRenderer, "Refused: " + row.rejectCount(), x + 120, lineY, GovernmentUiGlyphs.REJECT_RED, false);
            renderRowActions(context, row, x, MAIN_BOTTOM - 34, RIGHT_WIDTH - 28, mouseX, mouseY);
            return;
        }
        if (row.approveCount() > 0 || row.rejectCount() > 0 || row.threshold() > 0 || row.voteCount() > 0) {
            GovernmentUiComponents.divider(context, x, voteY - 12, RIGHT_WIDTH - 28);
            GovernmentUiComponents.sectionTitle(context, textRenderer, x, voteY, "current_votes",
                    "Decision State", theme, style);
            drawDecisionOption(context, x, voteY + 18, RIGHT_WIDTH - 28, "Approve", row.approveCount(), total(row),
                    GovernmentUiGlyphs.ACTIVE_GREEN);
            drawDecisionOption(context, x, voteY + 51, RIGHT_WIDTH - 28, "Reject", row.rejectCount(), total(row),
                    GovernmentUiGlyphs.REJECT_RED);
            if (row.threshold() > 0) drawDecisionOption(context, x, voteY + 84, RIGHT_WIDTH - 28, "Required",
                    Math.max(row.approveCount(), row.rejectCount()), row.threshold(), theme.warningColor());
        } else {
            ElarionUiTypography.draw(context, textRenderer, "Status", x, voteY, theme.titleColor(), false);
            ElarionUiTypography.draw(context, textRenderer, row.state(), x, voteY + 18,
                    row.unlocked() ? GovernmentUiGlyphs.ACTIVE_GREEN : theme.mutedColor(), false);
        }
        if (row.createdAt() > 0L) {
            renderSubmittedAge(context, x + (RIGHT_WIDTH - 28) / 2, MAIN_BOTTOM - 62, row.createdAt());
        }
        renderRowActions(context, row, x, MAIN_BOTTOM - 34, RIGHT_WIDTH - 28, mouseX, mouseY);
    }

    private void renderSubmittedAge(DrawContext context, int centerX, int y, long createdAt) {
        String value = ageLabel(createdAt).replace("Submitted ", "");
        int iconX = centerX - 36;
        GovernmentUiGlyphs.icon(context, iconX, y - 2, 16, "history", style);
        ElarionUiTypography.draw(context, textRenderer, "Submitted", iconX + 22, y, theme.titleColor(), false);
        ElarionUiTypography.draw(context, textRenderer, value, centerX - ElarionUiTypography.width(textRenderer, value) / 2, y + 13,
                GovernmentUiGlyphs.ACTIVE_GREEN, false);
    }

    private void renderRowActions(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int width,
            double mouseX,
            double mouseY
    ) {
        if (row.kind().equals("office")) {
            if (primaryOffice(row.id())) {
                if (row.complete()) {
                    button(context, x + (width - 100) / 2, y, 100, "Resign", "resign_office",
                            row.id(), mouseX, mouseY, true, false);
                }
                return;
            }
            int left = x + 18;
            int right = x + width - 118;
            button(context, left, y, 100, "Appoint", "appoint_office", row.id(), mouseX, mouseY, true, true);
            if (row.complete()) {
                button(context, right, y, 100, "Remove", "remove_office", row.id(), mouseX, mouseY, true, false);
                button(context, x + (width - 100) / 2, y - 28, 100, "Resign", "resign_office", row.id(), mouseX, mouseY, true, false);
            }
            return;
        }
        if (row.state().equals("Review")) {
            int approveX = x + 32;
            int rejectX = x + width - 112;
            button(context, approveX, y, 96, "Approve", "approve_proposal", row.id(), mouseX, mouseY, true, true);
            button(context, rejectX, y, 96, "Reject", "reject_proposal", row.id(), mouseX, mouseY, true, false);
            return;
        }
        if (row.state().equals("Finalize")) {
            button(context, x + (width - 170) / 2, y, 170, "Finalize Official Text",
                    "finalize_proposal", row.id(), mouseX, mouseY, true, true);
            return;
        }
        if (row.state().equals("Archive")) {
            button(context, x + (width - 150) / 2, y, 150, "Archive Record",
                    "archive_record", row.id(), mouseX, mouseY, true, false);
            return;
        }
        if (row.state().equals("Restore")) {
            button(context, x + (width - 150) / 2, y, 150, "Restore Record",
                    "restore_record", row.id(), mouseX, mouseY, true, true);
            return;
        }
        renderPrimaryAction(context, x, y, width, mouseX, mouseY);
    }

    private void renderPrimaryAction(DrawContext context, int x, int y, int width, double mouseX, double mouseY) {
        if (payload.primaryAction().isBlank()) return;
        int buttonWidth = 170;
        button(context, x + (width - buttonWidth) / 2, y, buttonWidth, labelFor(payload.primaryAction()),
                payload.primaryAction(), selectedRowId, mouseX, mouseY, payload.eligible() && !payload.locked(), true);
    }

    private void button(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            String action,
            String target,
            double mouseX,
            double mouseY,
            boolean active,
            boolean primary
    ) {
        boolean hover = active && inside(mouseX, mouseY, x, y, width, 24);
        GovernmentUiGlyphs.actionButton(context, textRenderer, x, y, width, 24, label, hover, active, primary, style);
        if (active) hits.add(new Hit(x, y, width, 24, action, target));
    }

    private void renderBottomBand(DrawContext context) {
        GovernmentUiGlyphs.sectionBox(context, LEFT_X, BOTTOM_Y, LOGICAL_WIDTH - 32, BOTTOM_HEIGHT, style);
        GovernmentUiGlyphs.icon(context, LEFT_X + 16, BOTTOM_Y + 11, 16, "office", style);
        ElarionUiTypography.draw(context, textRenderer, "Authority Holders", LEFT_X + 40, BOTTOM_Y + 9, theme.titleColor(), false);
        GovernmentUiOpenPayload.Row office = firstSelectable(payload.officeRows());
        String text = office.id().equals("empty") ? "No current holders." : office.title() + " - " + office.body();
        ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, text, 560),
                LEFT_X + 40, BOTTOM_Y + 25, theme.textColor(), false);
    }

    private void renderOverlay(DrawContext context, double mouseX, double mouseY) {
        int w = 470;
        boolean officeTool = overlayAction.equals("appoint_office") || overlayAction.equals("remove_office");
        int h = officeTool ? 128 : 230;
        int x = (LOGICAL_WIDTH - w) / 2;
        int y = (LOGICAL_HEIGHT - h) / 2;
        context.fill(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, 0xAA000000);
        ElarionUiRenderer.borderedBox(context, x, y, w, h, style);
        GovernmentUiGlyphs.icon(context, x + 14, y + 12, 18, officeTool ? "office" : "law", style);
        ElarionUiTypography.draw(context, textRenderer, overlayTitle(), x + 40, y + 16, theme.titleColor(), false);
        renderInput(context, x + 14, y + 42, w - 28,
                officeTool ? "Ember nickname" : "Official title", titleInput, !bodyActive);
        if (!officeTool) {
            renderTextArea(context, x + 14, y + 72, w - 28, 104, "Official body", bodyInput, bodyActive);
        }
        String hint = officeTool
                ? "Uses Core nickname first; username also works."
                : overlayAction.equals("finalize_proposal")
                ? "This publishes the approved Ember proposal."
                : "This creates a direct authority civic record.";
        ElarionUiTypography.draw(context, textRenderer, hint, x + 16, y + (officeTool ? 72 : 184), theme.mutedColor(), false);
        int cancelX = x + w - 152;
        int submitX = x + w - 76;
        int by = y + h - 28;
        GovernmentUiGlyphs.actionButton(context, textRenderer, cancelX, by, 66, 18, "Cancel",
                inside(mouseX, mouseY, cancelX, by, 66, 18), true, false, style);
        GovernmentUiGlyphs.actionButton(context, textRenderer, submitX, by, 66, 18, "Submit",
                inside(mouseX, mouseY, submitX, by, 66, 18),
                !titleInput.isBlank() && (officeTool || !bodyInput.isBlank()), true, style);
        hits.add(new Hit(cancelX, by, 66, 18, "overlay_cancel", ""));
        hits.add(new Hit(submitX, by, 66, 18, "overlay_submit", ""));
    }

    private void renderInput(DrawContext context, int x, int y, int width, String label, String value, boolean active) {
        ElarionUiRenderer.beveledBox(context, x, y, width, 22, active ? theme.buttonHoverColor() : theme.insetColor(), style);
        String text = value.isBlank() ? label : value;
        if (active && (System.currentTimeMillis() / 450L) % 2 == 0) text += "_";
        ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, text, width - 10),
                x + 5, y + 7, value.isBlank() ? theme.mutedColor() : theme.textColor(), false);
    }

    private void renderTextArea(DrawContext context, int x, int y, int width, int height, String label, String value, boolean active) {
        ElarionUiRenderer.beveledBox(context, x, y, width, height, active ? theme.buttonHoverColor() : theme.insetColor(), style);
        String text = value.isBlank() ? label : value;
        if (active && (System.currentTimeMillis() / 450L) % 2 == 0) text += "_";
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(text), x + 7, y + 7,
                width - 14, height - 20, value.isBlank() ? theme.mutedColor() : theme.textColor(), theme.mutedColor());
        String chars = value.length() + " chars";
        ElarionUiTypography.draw(context, textRenderer, chars, x + width - 7 - ElarionUiTypography.width(textRenderer, chars),
                y + height - 12, theme.mutedColor(), false);
    }

    private void drawDecisionOption(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            long value,
            long total,
            int color
    ) {
        GovernmentUiComponents.voteOptionRow(context, textRenderer, x, y, width, label, value, total, color,
                false, false, style);
    }

    private List<GovernmentUiOpenPayload.Row> rowsForActiveTab() {
        return rowsForTab(activeTab(), payload.stageRows(), payload.officeRows());
    }

    public static List<GovernmentUiOpenPayload.Row> rowsForTab(
            String activeTab,
            List<GovernmentUiOpenPayload.Row> stageRows,
            List<GovernmentUiOpenPayload.Row> officeRows
    ) {
        String tab = activeTab == null || activeTab.isBlank() ? "review" : activeTab;
        List<GovernmentUiOpenPayload.Row> safeStageRows = stageRows == null ? List.of() : stageRows;
        List<GovernmentUiOpenPayload.Row> safeOfficeRows = officeRows == null ? List.of() : officeRows;
        return switch (tab) {
            case "offices" -> safeOfficeRows.isEmpty() ? safeStageRows : safeOfficeRows;
            default -> safeStageRows;
        };
    }

    private GovernmentUiOpenPayload.Row selectedRow() {
        return rowsForActiveTab().stream().filter(row -> row.id().equals(selectedRowId)).findFirst()
                .orElseGet(() -> firstSelectable(rowsForActiveTab()));
    }

    private static GovernmentUiOpenPayload.Row firstSelectable(List<GovernmentUiOpenPayload.Row> rows) {
        return rows.stream().filter(row -> !row.id().equals("empty")).findFirst()
                .orElse(new GovernmentUiOpenPayload.Row("empty", "No Entries", "", "Empty", false, false, "static"));
    }

    private String activeTab() {
        String tab = activeTabOverride.isBlank() ? payload.activeTabId() : activeTabOverride;
        if (tab.equals("notices") || tab.equals("rules") || tab.equals("proposals")) return "review";
        return tab.isBlank() ? "review" : tab;
    }

    private String contentHeader() {
        return switch (activeTab()) {
            case "laws" -> "Laws";
            case "projects" -> "Projects";
            case "offices" -> "Government Offices";
            case "archive" -> "Civic Archive";
            default -> "Proposal Review";
        };
    }

    private String activeTabIcon() {
        return switch (activeTab()) {
            case "laws" -> "law";
            case "projects" -> "project";
            case "offices" -> "office";
            case "archive" -> "archive";
            default -> "proposal";
        };
    }

    private String formLabel() {
        if (!payload.governmentFormLabel().isBlank()) return payload.governmentFormLabel();
        return payload.formRows().stream().filter(GovernmentUiOpenPayload.Row::complete)
                .map(GovernmentUiOpenPayload.Row::title).findFirst().orElse("Unchosen");
    }

    private String authorityLabel() {
        return payload.authorityLabel().isBlank() ? formLabel() : payload.authorityLabel();
    }

    private String roleLabel() {
        return payload.roleLabel().isBlank() ? "Authority Seat" : payload.roleLabel();
    }

    private String stripCategory(String body) {
        int index = body.indexOf(" - ");
        return index >= 0 ? body.substring(index + 3) : body;
    }

    private long total(GovernmentUiOpenPayload.Row row) {
        return Math.max(row.threshold(), Math.max(row.voteCount(), row.approveCount() + row.rejectCount()));
    }

    private int visibleRowCapacity() {
        int bottom = MAIN_BOTTOM - 24;
        return Math.max(1, (bottom - ROWS_Y + ROW_GAP) / (ROW_HEIGHT + ROW_GAP));
    }

    private String ageLabel(long createdAt) {
        long seconds = Math.max(0L, (System.currentTimeMillis() - createdAt) / 1000L);
        long days = seconds / 86_400L;
        seconds %= 86_400L;
        long hours = seconds / 3_600L;
        seconds %= 3_600L;
        long minutes = seconds / 60L;
        if (days > 0L) return "Submitted " + days + "d ago";
        if (hours > 0L) return "Submitted " + hours + "h " + minutes + "m ago";
        return "Submitted " + minutes + "m ago";
    }

    private String labelFor(String action) {
        return switch (action) {
            case "add_notice_record" -> "Add Notice";
            case "add_rule_record" -> "Add Civic Rule";
            case "add_project_record" -> "Add Project";
            case "add_law_vote" -> "Open Law Vote";
            case "add_law_record" -> "Add Law";
            case "send_notice" -> "Send Notice";
            default -> "Add Record";
        };
    }

    private String overlayTitle() {
        return switch (overlayAction) {
            case "finalize_proposal" -> "Finalize Official Text";
            case "add_notice_record" -> "Add Notice";
            case "add_rule_record" -> "Add Civic Rule";
            case "add_project_record" -> "Add Project Record";
            case "add_law_vote" -> "Open Republic Law Vote";
            case "send_notice" -> "Send Realm Notice";
            case "appoint_office" -> "Appoint Office Holder";
            case "remove_office" -> "Remove Office Holder";
            default -> "Add Law";
        };
    }

    private void sendAction(String action, String target, String value, String secondary) {
        ClientPlayNetworking.send(new GovernmentUiActionPayload(payload.screenType(), action, payload.realmId(),
                target, value, secondary, payload.sessionId()));
    }

    private void submitOverlay() {
        boolean officeTool = overlayAction.equals("appoint_office") || overlayAction.equals("remove_office");
        if (titleInput.isBlank() || (!officeTool && bodyInput.isBlank())) return;
        sendAction(overlayAction, overlayTarget, titleInput, officeTool ? "" : bodyInput);
        overlay = false;
        titleInput = "";
        bodyInput = "";
    }

    private void runHit(Hit hit) {
        switch (hit.action) {
            case "close" -> close();
            case "select" -> selectedRowId = hit.target;
            case "tab" -> {
                activeTabOverride = hit.target;
                rowFirstVisible = 0;
                selectedRowId = firstSelectable(rowsForActiveTab()).id();
                sendAction("open_module", hit.target, "", "");
            }
            case "open_module", "approve_proposal", "reject_proposal", "archive_record", "restore_record" ->
                    sendAction(hit.action, hit.target, "", "");
            case "finalize_proposal", "add_law_record", "add_law_vote", "add_notice_record", "add_rule_record",
                    "add_project_record", "send_notice", "appoint_office", "remove_office" -> {
                overlay = true;
                bodyActive = false;
                overlayAction = hit.action;
                overlayTarget = hit.target;
                titleInput = "";
                bodyInput = "";
            }
            case "resign_office" -> sendAction(hit.action, hit.target, "", "");
            case "overlay_cancel" -> overlay = false;
            case "overlay_submit" -> submitOverlay();
            default -> {
            }
        }
    }

    public static boolean primaryOffice(String officeId) {
        return switch (officeId == null ? "" : officeId) {
            case "monarch", "president" -> true;
            default -> false;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (layout.belowPreferredScale()) return true;
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        if (overlay) {
            boolean officeTool = overlayAction.equals("appoint_office") || overlayAction.equals("remove_office");
            int w = 470;
            int h = officeTool ? 128 : 230;
            int x = (LOGICAL_WIDTH - w) / 2;
            int y = (LOGICAL_HEIGHT - h) / 2;
            if (inside(lx, ly, x + 14, y + 42, w - 28, 22)) {
                bodyActive = false;
                return true;
            }
            if (!officeTool && inside(lx, ly, x + 14, y + 72, w - 28, 104)) {
                bodyActive = true;
                return true;
            }
        }
        for (Hit hit : List.copyOf(hits)) {
            if (inside(lx, ly, hit.x, hit.y, hit.width, hit.height)) {
                runHit(hit);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (overlay) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        if (layout.belowPreferredScale()) return true;
        int rowCount = (int) rowsForActiveTab().stream().filter(row -> !row.id().equals("empty")).count();
        int maxFirst = Math.max(0, rowCount - visibleRowCapacity());
        if (maxFirst <= 0) return false;
        int direction = verticalAmount > 0 ? -1 : verticalAmount < 0 ? 1 : 0;
        if (direction == 0) return false;
        rowFirstVisible = Math.max(0, Math.min(maxFirst, rowFirstVisible + direction));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (overlay) {
                overlay = false;
                return true;
            }
            close();
            return true;
        }
        if (!overlay) return super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            bodyActive = !bodyActive;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submitOverlay();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (bodyActive && !bodyInput.isEmpty()) bodyInput = bodyInput.substring(0, bodyInput.length() - 1);
            else if (!bodyActive && !titleInput.isEmpty()) titleInput = titleInput.substring(0, titleInput.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (bodyActive) bodyInput = "";
            else titleInput = "";
            return true;
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!overlay || Character.isISOControl(chr)) return super.charTyped(chr, modifiers);
        if (bodyActive) {
            if (bodyInput.length() < 2000) bodyInput += chr;
        } else if (titleInput.length() < 64) {
            titleInput += chr;
        }
        return true;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int colorArgb(String color) {
        return switch (color == null ? "" : color.toLowerCase()) {
            case "black" -> 0xFF000000;
            case "dark_blue", "blue" -> 0xFF2F55D4;
            case "dark_green", "green" -> 0xFF2F9E44;
            case "dark_red", "red" -> 0xFFC93C37;
            case "gold", "yellow" -> 0xFFE1B84B;
            case "dark_purple", "light_purple" -> 0xFF8C5DD9;
            case "aqua", "dark_aqua" -> 0xFF42B8B2;
            default -> 0xFF8A7250;
        };
    }

    private static String colorLabel(String color) {
        String clean = color == null || color.isBlank() ? "Default" : color.trim().replace('_', ' ');
        StringBuilder builder = new StringBuilder(clean.length());
        boolean capitalize = true;
        for (int i = 0; i < clean.length(); i++) {
            char ch = clean.charAt(i);
            if (Character.isWhitespace(ch)) {
                builder.append(ch);
                capitalize = true;
            } else if (capitalize) {
                builder.append(Character.toUpperCase(ch));
                capitalize = false;
            } else {
                builder.append(Character.toLowerCase(ch));
            }
        }
        return builder.toString();
    }

    private record Hit(int x, int y, int width, int height, String action, String target) {
    }

    public static final class Layout {
        public int logicalWidth() { return LOGICAL_WIDTH; }
        public int logicalHeight() { return LOGICAL_HEIGHT; }
        public int leftX() { return LEFT_X; }
        public int leftWidth() { return LEFT_WIDTH; }
        public int rightX() { return RIGHT_X; }
        public int rightWidth() { return RIGHT_WIDTH; }
        public int headerHeight() { return HEADER_HEIGHT; }
        public int bodyY() { return BODY_Y; }
        public int tabY() { return TAB_Y; }
        public int tabHeight() { return TAB_HEIGHT; }
        public int contentTop() { return CONTENT_TOP; }
        public int mainBottom() { return MAIN_BOTTOM; }
        public int bottomY() { return BOTTOM_Y; }
        public int tabCount() { return 5; }
        public int tabAreaX() { return LEFT_X; }
        public int tabAreaWidth() { return RIGHT_X + RIGHT_WIDTH - LEFT_X; }
        public int tabGap() { return TAB_GAP; }
        public int tabRightEdge() { return GovernmentScreenChrome.tabRightEdge(LEFT_X, RIGHT_X + RIGHT_WIDTH - LEFT_X); }
        public int headerSegmentWidth() {
            return GovernmentScreenChrome.metadataSegmentWidth(HEADER_META_X, HEADER_META_RIGHT, HEADER_META_COUNT);
        }
        public int headerSegmentX(int index) {
            return GovernmentScreenChrome.metadataSegmentX(HEADER_META_X, HEADER_META_RIGHT, HEADER_META_COUNT, index);
        }
        public int headerSegmentEnd(int index) {
            return GovernmentScreenChrome.metadataSegmentEnd(HEADER_META_X, HEADER_META_RIGHT, HEADER_META_COUNT, index);
        }
        public int headerIdentityX() { return headerSegmentX(0); }
        public int headerIdentityWidth() { return headerSegmentWidth(); }
        public int headerRoleX() { return headerSegmentX(2); }
        public int headerRoleWidth() { return headerSegmentWidth(); }
        public int headerColorX() { return headerSegmentX(3); }
        public int headerColorWidth() { return headerSegmentEnd(3) - headerSegmentX(3); }
        public int closeX() { return HEADER_CLOSE_X; }
        public int rowsY() { return ROWS_Y; }
        public int visibleRows() { return (MAIN_BOTTOM - 10 - ROWS_Y + ROW_GAP) / (ROW_HEIGHT + ROW_GAP); }
        public int readableMinimumScalePercent() { return READABLE_MINIMUM_SCALE_PERCENT; }
    }
}
