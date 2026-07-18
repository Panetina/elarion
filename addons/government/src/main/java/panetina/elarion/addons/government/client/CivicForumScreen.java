package panetina.elarion.addons.government.client;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
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

public final class CivicForumScreen extends ElarionScreen {
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
    private static final int CURRENT_ROWS_Y = CONTENT_TOP + 114;
    private static final int CURRENT_ROW_HEIGHT = 38;
    private static final int MODULE_ROWS_Y = CONTENT_TOP + 12;
    private static final int MODULE_ROW_HEIGHT = 42;
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
    private boolean overlay;
    private boolean secondaryActive;
    private int rowFirstVisible;
    private int choiceFirstVisible;
    private String activeTabOverride = "";
    private String overlayAction = "";
    private String overlayTarget = "";
    private String titleInput = "";
    private String bodyInput = "";
    private String proposalCategory = "law";
    private String feedbackMessage = "";

    public CivicForumScreen(GovernmentUiOpenPayload payload) {
        super(Text.literal("Civic Forum"));
        this.payload = payload;
        this.selectedRowId = firstSelectable(rowsForActiveTab()).id();
        this.feedbackMessage = payload.message();
    }

    public static Layout layoutMetrics() {
        return new Layout();
    }

    public void setFeedbackMessage(String message) {
        this.feedbackMessage = message == null ? "" : message.trim();
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
            renderTooSmall(context, "Civic Forum");
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
        renderCurrentContent(context, lx, ly);
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
                Text.literal("Window too small for readable civic text. Resize the window or lower GUI scale."),
                x + 14, y + 34, panelWidth - 28, 44, theme.textColor(), theme.mutedColor());
    }

    private void renderHeader(DrawContext context, double mouseX, double mouseY) {
        GovernmentUiGlyphs.crest(context, 15, 7, 32, payload.crestIconId(), style);
        drawHeaderTitle(context, "Civic Forum");
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
                new GovernmentScreenChrome.Tab("current_votes", "Current Votes", "current_votes"),
                new GovernmentScreenChrome.Tab("proposals", "Proposals", "proposal"),
                new GovernmentScreenChrome.Tab("laws", "Laws", "law"),
                new GovernmentScreenChrome.Tab("projects", "Projects", "project"),
                new GovernmentScreenChrome.Tab("offices", "Offices", "office"),
                new GovernmentScreenChrome.Tab("history", "History", "history")
        };
        int areaX = LEFT_X;
        int areaWidth = RIGHT_X + RIGHT_WIDTH - LEFT_X;
        for (int i = 0; i < tabs.length; i++) {
            GovernmentScreenChrome.Tab tab = tabs[i];
            int x = GovernmentScreenChrome.tabX(areaX, areaWidth, TAB_GAP, tabs.length, i);
            int width = GovernmentScreenChrome.tabWidth(areaWidth, TAB_GAP, tabs.length, i);
            boolean selected = activeTab().equals(tab.id());
            boolean enabled = tabEnabled(tab.id());
            boolean hover = inside(mouseX, mouseY, x, TAB_Y, width, TAB_HEIGHT);
            GovernmentScreenChrome.drawTab(context, textRenderer, x, TAB_Y, width, TAB_HEIGHT, tab, selected, hover,
                    enabled, style);
            if (!selected && enabled) hits.add(new Hit(x, TAB_Y, width, TAB_HEIGHT, "tab", tab.id()));
        }
    }

    private void renderCurrentContent(DrawContext context, double mouseX, double mouseY) {
        GovernmentUiGlyphs.sectionBox(context, LEFT_X, CONTENT_TOP, LEFT_WIDTH, MAIN_BOTTOM - CONTENT_TOP, style);
        GovernmentUiGlyphs.sectionBox(context, RIGHT_X, CONTENT_TOP, RIGHT_WIDTH, MAIN_BOTTOM - CONTENT_TOP, style);
        if (activeTab().equals("current_votes")) renderFoundingDecisions(context);
        renderRows(context, rowsForActiveTab(), mouseX, mouseY);
        renderDetail(context, selectedRow(), mouseX, mouseY);
        renderBottomBand(context);
    }

    private void renderFoundingDecisions(DrawContext context) {
        int x = LEFT_X + 10;
        int y = CONTENT_TOP + 9;
        renderListSectionTitle(context, x, y, "Founding Decisions", "government_form");
        String[][] decisions = foundingDecisions();
        int rowY = y + 18;
        for (String[] decision : decisions) {
            boolean selected = payload.screenType().equals(decision[3]);
            GovernmentUiGlyphs.rowBox(context, x, rowY, LEFT_WIDTH - 20, 18, selected, false, false, style);
            GovernmentUiGlyphs.icon(context, x + 7, rowY + 3, 11, decision[2], style);
            ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, decision[0], LEFT_WIDTH - 116),
                    x + 24, rowY + 5, theme.textColor(), false);
            String state = decision[1];
            int color = "Settled".equals(state) ? GovernmentUiGlyphs.ACTIVE_GREEN : theme.warningColor();
            ElarionUiTypography.draw(context, textRenderer, state, x + LEFT_WIDTH - 44 - ElarionUiTypography.width(textRenderer, state), rowY + 5,
                    color, false);
            if ("Settled".equals(state)) GovernmentUiGlyphs.icon(context, x + LEFT_WIDTH - 34, rowY + 4, 10, "settled", style);
            rowY += 21;
        }
    }

    private void renderRows(DrawContext context, List<GovernmentUiOpenPayload.Row> rows, double mouseX, double mouseY) {
        int x = LEFT_X + 10;
        int y = activeTab().equals("current_votes") ? CURRENT_ROWS_Y : MODULE_ROWS_Y;
        int rowHeight = activeTab().equals("current_votes") ? CURRENT_ROW_HEIGHT : MODULE_ROW_HEIGHT;
        int rowWidth = LEFT_WIDTH - 20;
        int bottom = MAIN_BOTTOM - 14;
        boolean currentVotes = activeTab().equals("current_votes");
        List<GovernmentUiOpenPayload.Row> visibleRows = rows.stream()
                .filter(row -> !row.id().equals("empty"))
                .toList();
        if (rows.isEmpty() || visibleRows.isEmpty()) {
            ElarionUiRenderer.wrappedClipped(context, textRenderer,
                    Text.literal(rows.isEmpty() ? "No civic records available." : rows.getFirst().body()),
                    x + 8, y + 8, LEFT_WIDTH - 36, 60, theme.mutedColor(), theme.mutedColor());
            return;
        }
        int capacity = visibleRowCapacity();
        int maxFirst = Math.max(0, visibleRows.size() - capacity);
        if (rowFirstVisible > maxFirst) rowFirstVisible = maxFirst;
        int visible = 0;
        if (currentVotes) {
            long activeCount = visibleRows.stream().filter(row -> !row.kind().equals("recent_vote")).count();
            if (activeCount > 0) {
                renderListSectionTitle(context, x, y, activeCount > 1 ? "Active Votes" : "Active Vote", "proposal");
                y += 17;
            }
        }
        boolean recentHeaderDrawn = false;
        for (GovernmentUiOpenPayload.Row row : visibleRows.stream().skip(rowFirstVisible).toList()) {
            if (visible >= capacity) break;
            if (currentVotes && row.kind().equals("recent_vote") && !recentHeaderDrawn) {
                y += 3;
                renderListSectionTitle(context, x, y, "Recent Votes", "history");
                y += 17;
                recentHeaderDrawn = true;
            }
            if (y + rowHeight > bottom) break;
            renderCivicRow(context, row, x, y, rowWidth, rowHeight, currentVotes, mouseX, mouseY);
            hits.add(new Hit(x, y, rowWidth, rowHeight, "select", row.id()));
            y += rowHeight + ROW_GAP;
            visible++;
        }
        if (visible < visibleRows.size()) {
            GovernmentUiGlyphs.rowRange(context, textRenderer, x + rowWidth / 2, MAIN_BOTTOM - 12,
                    rowFirstVisible + 1, rowFirstVisible + visible, visibleRows.size(), style);
        }
    }

    private void renderCivicRow(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            int x,
            int y,
            int rowWidth,
            int rowHeight,
            boolean currentVotes,
            double mouseX,
            double mouseY
    ) {
        boolean selected = row.id().equals(selectedRowId) || row.selectedByViewer();
        boolean hover = inside(mouseX, mouseY, x, y, rowWidth, rowHeight);
        boolean muted = !row.unlocked() && !row.kind().equals("recent_vote");
        boolean recent = row.kind().equals("recent_vote");
        boolean history = activeTab().equals("history") && row.kind().equals("history");
        String metric = currentVotes ? currentVoteMetric(row)
                : history ? row.metricLabel().isBlank() ? row.state() : row.metricLabel()
                : row.metricLabel().isBlank() ? row.state() : row.metricLabel();
        String metricIcon = history ? "history"
                : recent ? row.state().equalsIgnoreCase("Rejected") ? "reject" : "settled"
                : row.voteCount() > 0 ? "people" : "current_votes";
        String secondLine = history ? row.state() : currentVotes && !recent ? compactTimeRemaining() : "";
        String secondIcon = "timer";
        if (!secondLine.isBlank()) {
            secondIcon = "timer";
        } else if (!recent && currentVotes && !row.state().isBlank()) {
            secondLine = row.state();
            secondIcon = row.complete() ? "settled" : row.unlocked() ? "current_votes" : "timer";
        }
        GovernmentUiComponents.recordRow(context, textRenderer, row, x, y, rowWidth, rowHeight,
                selected, hover, muted, iconForRow(row), chipLabel(row), metric, metricIcon,
                secondLine, secondIcon, theme, style);
    }

    private void renderListSectionTitle(DrawContext context, int x, int y, String title, String iconId) {
        GovernmentUiGlyphs.icon(context, x + 2, y - 2, 14, iconId, style);
        ElarionUiTypography.draw(context, textRenderer, title, x + 24, y + 1, theme.titleColor(), false);
    }

    static int rowMetricIconX(int rowX, int rowWidth, boolean currentVotes) {
        return GovernmentUiComponents.metricColumnX(rowX, rowWidth);
    }

    private String chipLabel(GovernmentUiOpenPayload.Row row) {
        String category = row.category() == null ? "" : row.category().trim();
        if (!category.isBlank()) return category;
        String state = row.state() == null ? "" : row.state().trim();
        if (state.equalsIgnoreCase(row.metricLabel())) return "";
        if (state.equalsIgnoreCase("Unlocked") || state.equalsIgnoreCase("Ready")) return "";
        return state;
    }

    private void renderDetail(DrawContext context, GovernmentUiOpenPayload.Row row, double mouseX, double mouseY) {
        int x = RIGHT_X + 14;
        int y = CONTENT_TOP + 14;
        List<GovernmentUiOpenPayload.Row> choices = currentVoteChoiceRows();
        if (activeTab().equals("current_votes") && !choices.isEmpty()) {
            renderChoiceListPanel(context, choices, mouseX, mouseY, x, y);
            return;
        }
        if (row.id().equals("empty")) {
            ElarionUiTypography.draw(context, textRenderer, "No Selection", x, y, theme.titleColor(), false);
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(row.body()),
                    x, y + 18, RIGHT_WIDTH - 28, 58, theme.mutedColor(), theme.mutedColor());
            renderPrimaryAction(context, x, MAIN_BOTTOM - 34, RIGHT_WIDTH - 28, mouseX, mouseY);
            return;
        }
        if (row.kind().equals("choice")) {
            renderChoiceDetail(context, row, mouseX, mouseY, x, y);
            return;
        }
        String actorPrefix = row.kind().equals("office") ? ""
                : row.kind().equals("history") ? "Recorded by " : "Proposed by ";
        String actor = row.actorName().isBlank() || actorPrefix.isBlank() ? "" : actorPrefix + row.actorName();
        GovernmentUiComponents.detailHeader(context, textRenderer, row, x, y, RIGHT_WIDTH - 28,
                iconForRow(row), row.category().isBlank() ? row.state() : row.category(), actor, theme, style);
        int bodyY = y + 62;
        GovernmentUiComponents.bodyText(context, textRenderer, stripCategory(row.body()),
                x, bodyY, RIGHT_WIDTH - 28, 54, theme);
        if (showsProposalVote(row)) {
            renderProposalVoteDetail(context, row, mouseX, mouseY, x, bodyY + 66);
        } else {
            renderInformationDetail(context, row, x, bodyY + 70);
            if (activeTab().equals("proposals")) {
                renderPrimaryAction(context, x, MAIN_BOTTOM - 34, RIGHT_WIDTH - 28, mouseX, mouseY);
            }
        }
    }

    private void renderChoiceListPanel(
            DrawContext context,
            List<GovernmentUiOpenPayload.Row> choices,
            double mouseX,
            double mouseY,
            int x,
            int y
    ) {
        ElarionUiTypography.draw(context, textRenderer, optionPanelTitle(), x, y, theme.titleColor(), false);
        ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(payload.subtitle()),
                x, y + 16, RIGHT_WIDTH - 28, 32, theme.mutedColor(), theme.mutedColor());
        context.fill(x, y + 54, x + RIGHT_WIDTH - 28, y + 55, GovernmentUiGlyphs.GOLD_SHADOW);
        List<GovernmentUiOpenPayload.Row> visibleChoices = choices.stream()
                .filter(choice -> !choice.id().equals("empty"))
                .toList();
        if (visibleChoices.isEmpty()) {
            GovernmentUiOpenPayload.Row empty = choices.stream().findFirst()
                    .orElse(new GovernmentUiOpenPayload.Row("empty", "No Entries", "", "Waiting", false, false, "static"));
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(empty.body()),
                    x, y + 72, RIGHT_WIDTH - 28, 58, theme.mutedColor(), theme.mutedColor());
            renderPrimaryAction(context, x, MAIN_BOTTOM - 34, RIGHT_WIDTH - 28, mouseX, mouseY);
            return;
        }
        int rowY = y + 62;
        int rowHeight = 36;
        int listBottom = choicePanelShowsPrimaryAction(payload.primaryAction(), choices)
                ? MAIN_BOTTOM - 48 : MAIN_BOTTOM - 16;
        int capacity = Math.max(1, (listBottom - rowY) / (rowHeight + 5));
        int maxFirst = Math.max(0, visibleChoices.size() - capacity);
        if (choiceFirstVisible > maxFirst) choiceFirstVisible = maxFirst;
        int visible = 0;
        for (GovernmentUiOpenPayload.Row choice : visibleChoices.stream().skip(choiceFirstVisible).toList()) {
            if (visible >= capacity) break;
            boolean hover = inside(mouseX, mouseY, x, rowY, RIGHT_WIDTH - 28, rowHeight);
            boolean selected = choice.selectedByViewer() || choice.complete();
            String metric = choice.unlocked() ? choice.voteCount() + " votes" : choice.state();
            String tag = choice.selectedByViewer() ? "Your Vote"
                    : choice.complete() ? "Current" : choice.state();
            GovernmentUiComponents.recordRow(context, textRenderer, choice, x, rowY, RIGHT_WIDTH - 28, rowHeight,
                    selected, hover, !choice.unlocked(), iconForRow(choice), tag, metric,
                    choice.voteCount() > 0 ? "people" : "current_votes",
                    choice.selectedByViewer() ? "Selected" : "", "settled", theme, style);
            if (choice.unlocked() && payload.eligible()) {
                hits.add(new Hit(x, rowY, RIGHT_WIDTH - 28, rowHeight, "vote", choice.id()));
            }
            rowY += rowHeight + 5;
            visible++;
        }
        if (visibleChoices.size() > capacity) {
            String hint = (choiceFirstVisible + 1) + "-" + (choiceFirstVisible + visible) + " / " + visibleChoices.size();
            ElarionUiTypography.draw(context, textRenderer, hint, x + RIGHT_WIDTH - 28 - ElarionUiTypography.width(textRenderer, hint),
                    listBottom + 4, theme.mutedColor(), false);
        }
        renderPrimaryAction(context, x, MAIN_BOTTOM - 34, RIGHT_WIDTH - 28, mouseX, mouseY);
    }

    private void renderProposalVoteDetail(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            double mouseX,
            double mouseY,
            int x,
            int voteY
    ) {
        GovernmentUiComponents.divider(context, x, voteY - 12, RIGHT_WIDTH - 28);
        GovernmentUiComponents.sectionTitle(context, textRenderer, x, voteY, "current_votes",
                "Vote Progress", theme, style);
        drawVoteOption(context, x, voteY + 18, RIGHT_WIDTH - 28, "Yes, approve", row.approveCount(), total(row),
                GovernmentUiGlyphs.ACTIVE_GREEN, row.selectedByViewer(), true);
        drawVoteOption(context, x, voteY + 51, RIGHT_WIDTH - 28, "No, oppose", row.rejectCount(), total(row),
                GovernmentUiGlyphs.REJECT_RED, false, true);
        if (row.threshold() > 0) {
            drawVoteOption(context, x, voteY + 84, RIGHT_WIDTH - 28, "Threshold",
                    Math.max(row.approveCount(), row.rejectCount()), row.threshold(), theme.warningColor(), false, false);
        }
        int yesX = x + 35;
        int noX = x + 165;
        int buttonY = MAIN_BOTTOM - 34;
        boolean active = row.unlocked() && payload.eligible();
        GovernmentUiGlyphs.actionButton(context, textRenderer, yesX, buttonY, 112, 24, "Vote Yes",
                active && inside(mouseX, mouseY, yesX, buttonY, 112, 24), active, true, style);
        GovernmentUiGlyphs.actionButton(context, textRenderer, noX, buttonY, 112, 24, "Vote No",
                active && inside(mouseX, mouseY, noX, buttonY, 112, 24), active, false, style);
        if (active) {
            hits.add(new Hit(yesX, buttonY, 112, 24, "ratify_proposal", row.id()));
            hits.add(new Hit(noX, buttonY, 112, 24, "oppose_proposal", row.id()));
        }
    }

    private void renderInformationDetail(DrawContext context, GovernmentUiOpenPayload.Row row, int x, int y) {
        GovernmentUiComponents.divider(context, x, y - 12, RIGHT_WIDTH - 28);
        GovernmentUiComponents.sectionTitle(context, textRenderer, x, y, iconForRow(row), "Details", theme, style);
        int lineY = y + 22;
        if (row.kind().equals("office")) {
            ElarionUiTypography.draw(context, textRenderer, row.metricLabel(), x, lineY, theme.titleColor(), false);
            lineY += 16;
            long occupied = row.actorName().isBlank() ? row.voteCount() : Math.max(1L, row.voteCount());
            ElarionUiTypography.draw(context, textRenderer, "Seats: " + occupied + " / " + row.threshold(),
                    x, lineY, theme.textColor(), false);
            lineY += 16;
            ElarionUiTypography.draw(context, textRenderer, "Laws approved: " + row.approveCount(),
                    x, lineY, GovernmentUiGlyphs.ACTIVE_GREEN, false);
            ElarionUiTypography.draw(context, textRenderer, "Refused: " + row.rejectCount(),
                    x + 142, lineY, GovernmentUiGlyphs.REJECT_RED, false);
            lineY += 16;
            if (!row.actorName().isBlank()) {
                ElarionUiTypography.draw(context, textRenderer, "Holder: " + row.actorName(), x, lineY, theme.textColor(), false);
            }
            return;
        }
        if (isLawResultRow(row)) {
            ElarionUiTypography.draw(context, textRenderer, "Status", x, lineY, theme.titleColor(), false);
            ElarionUiTypography.draw(context, textRenderer, row.state(), x + 106, lineY,
                    row.complete() ? GovernmentUiGlyphs.ACTIVE_GREEN : theme.textColor(), false);
            lineY += 16;
            if (row.approveCount() > 0 || row.rejectCount() > 0 || row.voteCount() > 0) {
                ElarionUiTypography.draw(context, textRenderer, "Ember vote: " + row.approveCount() + " yes / "
                                + row.rejectCount() + " no",
                        x, lineY, theme.textColor(), false);
                lineY += 16;
            }
            if (row.createdAt() > 0L) {
                ElarionUiTypography.draw(context, textRenderer, row.complete() ? "Resolved: " + ageValue(row.createdAt())
                                : "Submitted: " + ageValue(row.createdAt()),
                        x, lineY, theme.textColor(), false);
                lineY += 16;
            }
            if (!row.actorName().isBlank()) {
                ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal("By " + row.actorName()),
                        x, lineY, RIGHT_WIDTH - 28, 42, theme.textColor(), theme.mutedColor());
            }
            return;
        }
        if (!row.metricLabel().isBlank()) {
            drawDetailMetricLine(context, row.metricLabel(), row.state(), x, lineY, RIGHT_WIDTH - 28,
                    row.complete() ? GovernmentUiGlyphs.ACTIVE_GREEN : theme.textColor());
            lineY += 16;
        }
        if (row.threshold() > 0) {
            String seats = "Seats: " + row.voteCount() + " / " + row.threshold();
            ElarionUiTypography.draw(context, textRenderer, seats, x, lineY, theme.textColor(), false);
            lineY += 16;
        }
        if (!row.actorName().isBlank()) {
            ElarionUiRenderer.wrappedClipped(context, textRenderer, Text.literal(row.actorName()),
                    x, lineY, RIGHT_WIDTH - 28, 42, theme.textColor(), theme.mutedColor());
        }
    }

    private static boolean isLawResultRow(GovernmentUiOpenPayload.Row row) {
        return row.kind().equals("recent_vote")
                || row.kind().equals("record")
                || row.kind().equals("record_action");
    }

    private void drawDetailMetricLine(
            DrawContext context, String label, String value, int x, int y, int width, int valueColor
    ) {
        String right = value == null ? "" : value.trim();
        int rightWidth = right.isBlank() ? 0 : ElarionUiTypography.width(textRenderer, right);
        int labelWidth = Math.max(40, width - rightWidth - (right.isBlank() ? 0 : 12));
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiRenderer.ellipsize(textRenderer, label, labelWidth),
                x, y, theme.titleColor(), false);
        if (!right.isBlank()) {
            ElarionUiTypography.draw(context, textRenderer, right,
                    x + width - rightWidth, y, valueColor, false);
        }
    }

    private void renderChoiceDetail(
            DrawContext context,
            GovernmentUiOpenPayload.Row row,
            double mouseX,
            double mouseY,
            int x,
            int y
    ) {
        GovernmentUiComponents.detailHeader(context, textRenderer, row, x, y, RIGHT_WIDTH - 28,
                iconForRow(row), row.selectedByViewer() ? "Your Vote" : row.state(), "", theme, style);
        int bodyY = y + 66;
        GovernmentUiComponents.bodyText(context, textRenderer, stripCategory(row.body()),
                x, bodyY, RIGHT_WIDTH - 28, 56, theme);
        int progressY = bodyY + 76;
        GovernmentUiComponents.divider(context, x, progressY - 12, RIGHT_WIDTH - 28);
        GovernmentUiComponents.sectionTitle(context, textRenderer, x, progressY, "current_votes",
                "Vote Progress", theme, style);
        drawVoteOption(context, x, progressY + 18, RIGHT_WIDTH - 28, "Support", row.voteCount(), Math.max(1L, total(row)),
                row.selectedByViewer() ? GovernmentUiGlyphs.ACTIVE_GREEN : theme.titleColor(), row.selectedByViewer());
        String remaining = timeRemaining();
        GovernmentUiComponents.timerBlock(context, textRenderer, x + (RIGHT_WIDTH - 28) / 2,
                MAIN_BOTTOM - 62, remaining, theme, style);
        String action = actionFor(row);
        if (!action.isBlank()) {
            int buttonX = x + 55;
            int buttonY = MAIN_BOTTOM - 34;
            boolean active = row.unlocked() && payload.eligible();
            boolean hover = active && inside(mouseX, mouseY, buttonX, buttonY, 180, 24);
            GovernmentUiGlyphs.actionButton(context, textRenderer, buttonX, buttonY, 180, 24,
                    labelFor(action), hover, active, true, style);
            if (active) hits.add(new Hit(buttonX, buttonY, 180, 24, action, row.id()));
        }
    }

    private void renderPrimaryAction(DrawContext context, int x, int y, int width, double mouseX, double mouseY) {
        if (payload.primaryAction().isBlank()) return;
        int buttonWidth = 180;
        int buttonX = x + (width - buttonWidth) / 2;
        boolean active = !payload.locked() && payload.eligible();
        boolean hover = active && inside(mouseX, mouseY, buttonX, y, buttonWidth, 24);
        GovernmentUiGlyphs.actionButton(context, textRenderer, buttonX, y, buttonWidth, 24,
                labelFor(payload.primaryAction()), hover, active, true, style);
        if (active) hits.add(new Hit(buttonX, y, buttonWidth, 24, payload.primaryAction(), selectedRowId));
    }

    private void renderBottomBand(DrawContext context) {
        GovernmentUiGlyphs.sectionBox(context, LEFT_X, BOTTOM_Y, LOGICAL_WIDTH - 32, BOTTOM_HEIGHT, style);
        boolean feedback = feedbackMessage != null && !feedbackMessage.isBlank();
        GovernmentUiGlyphs.icon(context, LEFT_X + 16, BOTTOM_Y + 11, 16,
                feedback ? "notice" : "published_record", style);
        String title = feedback ? "Civic Notice"
                : activeTab().equals("current_votes") ? "Passed Laws Awaiting Publication" : "Recent Civic Records";
        ElarionUiTypography.draw(context, textRenderer, title, LEFT_X + 40, BOTTOM_Y + 9, theme.titleColor(), false);
        GovernmentUiOpenPayload.Row row = firstSelectable(payload.stageRows());
        String text = feedback ? feedbackMessage
                : row.id().equals("empty") ? "No recent record selected." : row.title() + " - " + row.state();
        ElarionUiTypography.draw(context, textRenderer, ElarionUiRenderer.ellipsize(textRenderer, text, 520),
                LEFT_X + 40, BOTTOM_Y + 25,
                feedback ? theme.warningColor() : theme.textColor(), false);
    }

    private void renderOverlay(DrawContext context, double mouseX, double mouseY) {
        int w = 440;
        int h = textOverlay() ? 226 : 116;
        int x = (LOGICAL_WIDTH - w) / 2;
        int y = (LOGICAL_HEIGHT - h) / 2;
        context.fill(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, 0xAA000000);
        ElarionUiRenderer.borderedBox(context, x, y, w, h, style);
        ElarionUiTypography.draw(context, textRenderer, overlayTitle(), x + 12, y + 12, theme.titleColor(), false);
        renderInput(context, x + 12, y + 34, w - 24, textOverlay() ? "Title" : "Name", titleInput, !secondaryActive);
        if (textOverlay()) {
            renderTextArea(context, x + 12, y + 62, w - 24, 100, "Body", bodyInput, secondaryActive);
            ElarionUiTypography.draw(context, textRenderer, "Category: " + proposalCategory.replace('_', ' '),
                    x + 14, y + 170, theme.mutedColor(), false);
            if (overlayAction.equals("create_proposal")) {
                ElarionUiTypography.draw(context, textRenderer, "Left / Right changes category.",
                        x + 14, y + 182, theme.mutedColor(), false);
            }
        } else {
            renderInput(context, x + 12, y + 62, w - 24, "Tag", bodyInput, secondaryActive);
        }
        int cancelX = x + w - 150;
        int submitX = x + w - 76;
        int by = y + h - 28;
        GovernmentUiGlyphs.actionButton(context, textRenderer, cancelX, by, 66, 18, "Cancel",
                inside(mouseX, mouseY, cancelX, by, 66, 18), true, false, style);
        GovernmentUiGlyphs.actionButton(context, textRenderer, submitX, by, 66, 18, "Submit",
                inside(mouseX, mouseY, submitX, by, 66, 18),
                !titleInput.isBlank() && !bodyInput.isBlank(), true, style);
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
                width - 14, height - 14, value.isBlank() ? theme.mutedColor() : theme.textColor(), theme.mutedColor());
    }

    private void drawVoteOption(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            long value,
            long total,
            int color,
            boolean selected
    ) {
        drawVoteOption(context, x, y, width, label, value, total, color, selected, true);
    }

    private void drawVoteOption(
            DrawContext context,
            int x,
            int y,
            int width,
            String label,
            long value,
            long total,
            int color,
            boolean selected,
            boolean selectable
    ) {
        GovernmentUiComponents.voteOptionRow(context, textRenderer, x, y, width, label, value, total, color,
                selected, selectable, style);
    }

    private List<GovernmentUiOpenPayload.Row> rowsForActiveTab() {
        if (activeTab().equals("proposals")) return payload.stageRows();
        if (activeTab().equals("laws") || activeTab().equals("projects") || activeTab().equals("history")) return payload.stageRows();
        if (activeTab().equals("offices")) return payload.officeRows().isEmpty() ? payload.moduleRows() : payload.officeRows();
        if (activeTab().equals("current_votes")) return rowsForCurrentVotes(payload.screenType(),
                payload.stageRows(), payload.formRows(), payload.officeRows(), payload.moduleRows());
        List<GovernmentUiOpenPayload.Row> rows = new ArrayList<>();
        rows.addAll(payload.stageRows());
        rows.addAll(payload.formRows());
        rows.addAll(payload.officeRows());
        return rows.stream().filter(row -> !row.id().equals("rules") && !row.id().equals("complete")).toList();
    }

    static String currentVoteRowSource(String screenType) {
        return switch (screenType == null ? "" : screenType) {
            default -> "stageRows";
        };
    }

    private static List<GovernmentUiOpenPayload.Row> rowsForCurrentVotes(
            String screenType,
            List<GovernmentUiOpenPayload.Row> stageRows,
            List<GovernmentUiOpenPayload.Row> formRows,
            List<GovernmentUiOpenPayload.Row> officeRows,
            List<GovernmentUiOpenPayload.Row> moduleRows
    ) {
        return switch (currentVoteRowSource(screenType)) {
            default -> stageRows;
        };
    }

    private List<GovernmentUiOpenPayload.Row> currentVoteChoiceRows() {
        if (!activeTab().equals("current_votes")) return List.of();
        return switch (payload.screenType()) {
            case "civic_color", "civic_form" -> payload.formRows();
            case "civic_election" -> payload.officeRows();
            default -> List.of();
        };
    }

    private String optionPanelTitle() {
        return switch (payload.screenType()) {
            case "civic_color" -> "Choose Realm Color";
            case "civic_form" -> "Choose Government Form";
            case "civic_election" -> leadershipLabel();
            default -> payload.title();
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
        if (tab.equals("rules") || tab.equals("notices")) return "laws";
        if (tab.equals("archive")) return "history";
        return tab.isBlank() ? "current_votes" : tab;
    }

    private boolean tabEnabled(String tab) {
        return tabEnabledForScreen(payload.screenType(), tab, activeTab());
    }

    static boolean tabEnabledForScreen(String screenType, String tab, String activeTab) {
        String safeScreen = screenType == null ? "" : screenType;
        String safeTab = tab == null ? "" : tab;
        boolean foundingComplete = "civic_features".equals(safeScreen) || safeScreen.startsWith("civic_module_");
        return switch (safeTab) {
            case "current_votes" -> !"current_votes".equals(activeTab);
            case "proposals", "laws", "projects", "offices", "history" -> foundingComplete;
            default -> false;
        };
    }

    private String[][] foundingDecisions() {
        String name = payload.screenType().equals("civic_name") ? payload.title() : "Realm Name";
        String color = payload.screenType().equals("civic_color") ? payload.title()
                : "Realm Color: " + colorLabel(payload.realmColor());
        String form = payload.screenType().equals("civic_form") ? payload.title() : "Government Form";
        String leader = leadershipLabel();
        return new String[][]{
                {name, foundingState("civic_name"), "realm_name", "civic_name"},
                {color, foundingState("civic_color"), realmColorIconId(), "civic_color"},
                {form, foundingState("civic_form"), "government_form", "civic_form"},
                {leader, foundingState("civic_election"), "leader_election", "civic_election"}
        };
    }

    private String foundingState(String screen) {
        if (payload.screenType().equals(screen)) return payload.locked() ? "Locked" : "Open";
        if (payload.screenType().startsWith("civic_") && screenOrder(payload.screenType()) < screenOrder(screen)) return "Locked";
        return "Settled";
    }

    private String realmColorIconId() {
        String color = payload.realmColor() == null ? "" : payload.realmColor().trim().toLowerCase().replace(' ', '_');
        return GovernmentUiIcons.REALM_COLOR_IDS.contains(color) ? color : "realm_name";
    }

    private int screenOrder(String screen) {
        return switch (screen) {
            case "civic_name" -> 0;
            case "civic_color" -> 1;
            case "civic_form" -> 2;
            case "civic_theocracy_faith" -> 3;
            case "civic_election" -> 4;
            default -> 5;
        };
    }

    private String leadershipLabel() {
        String form = formLabel().toLowerCase();
        if (form.contains("republic")) return "Leader Election";
        if (form.contains("monarchy")) return "Monarch Election";
        if (form.contains("theocracy")) return "High Priest Election";
        if (form.contains("confederation")) return "Delegate Election";
        return "Leadership Election";
    }

    private String actionFor(GovernmentUiOpenPayload.Row row) {
        if (row.kind().equals("choice") && payload.eligible()) return "vote";
        return "";
    }

    private String labelFor(String action) {
        return switch (action) {
            case "ratify_proposal" -> selectedRow().selectedByViewer() ? "Change Vote" : "Vote Yes";
            case "vote" -> selectedRow().selectedByViewer() ? "Change Vote" : "Vote";
            case "nominate_self" -> "Nominate Yourself";
            case "propose_faith" -> "Propose Faith";
            case "create_proposal" -> "Create Proposal";
            case "propose_name" -> "Propose Name";
            default -> "Open";
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
        return payload.roleLabel().isBlank() ? "Ember Assembly" : payload.roleLabel();
    }

    private String stripCategory(String body) {
        int index = body.indexOf(" - ");
        return index >= 0 ? body.substring(index + 3) : body;
    }

    private String iconForRow(GovernmentUiOpenPayload.Row row) {
        if (payload.screenType().equals("civic_color") && GovernmentUiIcons.hasTexture(row.id())) {
            return row.id();
        }
        if (GovernmentUiIcons.hasTexture(row.iconId())) {
            return row.iconId();
        }
        return row.category().isBlank() ? row.iconId() : row.category();
    }

    private boolean showsProposalVote(GovernmentUiOpenPayload.Row row) {
        return (payload.screenType().equals("civic_module_proposals") || row.kind().equals("active_vote"))
                && (row.state().equals("Ratify") || row.state().equals("Voted") || row.state().equals("Active"));
    }

    private long total(GovernmentUiOpenPayload.Row row) {
        return Math.max(row.threshold(), Math.max(row.voteCount(), row.approveCount() + row.rejectCount()));
    }

    private String ageValue(long timestamp) {
        long elapsed = Math.max(0L, System.currentTimeMillis() - timestamp);
        long minutes = Math.max(1L, elapsed / 60_000L);
        long hours = minutes / 60L;
        long days = hours / 24L;
        if (days > 0L) return days + "d ago";
        if (hours > 0L) return hours + "h " + (minutes % 60L) + "m ago";
        return minutes + "m ago";
    }

    private String overlayTitle() {
        return switch (overlayAction) {
            case "create_proposal" -> "Create Civic Proposal";
            case "propose_faith" -> "Propose Founding Faith";
            default -> textOverlay() ? "Write Official Text" : "Propose Realm Identity";
        };
    }

    private boolean textOverlay() {
        return overlayAction.equals("create_proposal") || overlayAction.startsWith("add_") || overlayAction.equals("finalize_proposal");
    }

    private void sendAction(String action, String target, String value, String secondary) {
        ClientPlayNetworking.send(new GovernmentUiActionPayload(payload.screenType(), action, payload.realmId(),
                target, value, secondary, payload.sessionId()));
    }

    private void submitOverlay() {
        if (titleInput.isBlank() || bodyInput.isBlank()) return;
        if (overlayAction.equals("create_proposal")) sendAction("create_proposal", proposalCategory, titleInput, bodyInput);
        else sendAction(overlayAction, overlayTarget, titleInput, bodyInput);
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
                choiceFirstVisible = 0;
                selectedRowId = firstSelectable(rowsForActiveTab()).id();
                if (hit.target.equals("current_votes")) sendAction("back", payload.homePageId(), "", "");
                else sendAction("open_module", hit.target, "", "");
            }
            case "vote" -> sendAction("vote", hit.target, "", "");
            case "ratify_proposal" -> sendAction("ratify_proposal", hit.target, "", "");
            case "oppose_proposal" -> sendAction("oppose_proposal", hit.target, "", "");
            case "nominate_self" -> sendAction("nominate_self", hit.target, "", "");
            case "create_proposal", "propose_name", "propose_faith" -> {
                overlay = true;
                overlayAction = hit.action;
                overlayTarget = hit.target;
                secondaryActive = false;
            }
            case "overlay_cancel" -> overlay = false;
            case "overlay_submit" -> submitOverlay();
            default -> {
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (layout.belowPreferredScale()) return true;
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        if (overlay) {
            int w = 440;
            int h = textOverlay() ? 226 : 116;
            int x = (LOGICAL_WIDTH - w) / 2;
            int y = (LOGICAL_HEIGHT - h) / 2;
            if (inside(lx, ly, x + 12, y + 34, w - 24, 22)) {
                secondaryActive = false;
                return true;
            }
            if (inside(lx, ly, x + 12, y + 62, w - 24, textOverlay() ? 100 : 22)) {
                secondaryActive = true;
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
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        List<GovernmentUiOpenPayload.Row> choiceRows = currentVoteChoiceRows();
        if (!choiceRows.isEmpty() && inside(lx, ly, RIGHT_X, CONTENT_TOP, RIGHT_WIDTH, MAIN_BOTTOM - CONTENT_TOP)) {
            List<GovernmentUiOpenPayload.Row> visibleChoices = choiceRows.stream()
                    .filter(row -> !row.id().equals("empty"))
                    .toList();
            int rowY = CONTENT_TOP + 14 + 62;
            int rowHeight = 34;
            int listBottom = choicePanelShowsPrimaryAction(payload.primaryAction(), choiceRows)
                    ? MAIN_BOTTOM - 48 : MAIN_BOTTOM - 16;
            int capacity = Math.max(1, (listBottom - rowY) / (rowHeight + 5));
            int maxFirst = Math.max(0, visibleChoices.size() - capacity);
            if (maxFirst <= 0) return false;
            int direction = verticalAmount > 0 ? -1 : verticalAmount < 0 ? 1 : 0;
            if (direction == 0) return false;
            choiceFirstVisible = Math.max(0, Math.min(maxFirst, choiceFirstVisible + direction));
            return true;
        }
        List<GovernmentUiOpenPayload.Row> visibleRows = rowsForActiveTab().stream()
                .filter(row -> !row.id().equals("empty"))
                .toList();
        int capacity = visibleRowCapacity();
        int maxFirst = Math.max(0, visibleRows.size() - capacity);
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
        if (overlayAction.equals("create_proposal")
                && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
            cycleProposalCategory(keyCode == GLFW.GLFW_KEY_RIGHT ? 1 : -1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            secondaryActive = !secondaryActive;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submitOverlay();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (secondaryActive && !bodyInput.isEmpty()) bodyInput = bodyInput.substring(0, bodyInput.length() - 1);
            else if (!secondaryActive && !titleInput.isEmpty()) titleInput = titleInput.substring(0, titleInput.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (secondaryActive) bodyInput = "";
            else titleInput = "";
            return true;
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!overlay || Character.isISOControl(chr)) return super.charTyped(chr, modifiers);
        if (secondaryActive) {
            if (bodyInput.length() < (textOverlay() ? 1500 : 6)) {
                bodyInput += textOverlay() ? chr : Character.toUpperCase(chr);
            }
        } else if (titleInput.length() < 64) {
            if (!textOverlay() && !overlayAction.equals("propose_faith")) {
                if (Character.isLetter(chr) || chr == ' ') titleInput += chr;
            } else {
                titleInput += chr;
            }
        }
        return true;
    }

    private int visibleRowCapacity() {
        int y = activeTab().equals("current_votes") ? CURRENT_ROWS_Y : MODULE_ROWS_Y;
        int rowHeight = activeTab().equals("current_votes") ? CURRENT_ROW_HEIGHT : MODULE_ROW_HEIGHT;
        int bottom = MAIN_BOTTOM - 24;
        if (activeTab().equals("current_votes")) {
            // Reserve both section labels so mixed active/recent pages never hide the last row.
            bottom -= 34;
        }
        return Math.max(1, (bottom - y + ROW_GAP) / (rowHeight + ROW_GAP));
    }

    private String compactTimeRemaining() {
        String remaining = timeRemaining();
        if (remaining.isBlank()) return "";
        return remaining.replace(" ", "");
    }

    private String timeRemaining() {
        long endsAt = payload.voteEndsAt();
        if (endsAt <= 0L) return "";
        long seconds = Math.max(0L, (endsAt - System.currentTimeMillis()) / 1000L);
        if (seconds <= 0L) return "Ended";
        long days = seconds / 86_400L;
        seconds %= 86_400L;
        long hours = seconds / 3_600L;
        seconds %= 3_600L;
        long minutes = seconds / 60L;
        long secs = seconds % 60L;
        if (days > 0L) return days + "d " + hours + "h";
        if (hours > 0L) return hours + "h " + minutes + "m";
        return minutes + "m " + secs + "s";
    }

    private void cycleProposalCategory(int direction) {
        List<String> categories = List.of("law", "realm_project", "civic_rule");
        int index = categories.indexOf(proposalCategory);
        if (index < 0) index = 0;
        proposalCategory = categories.get(Math.floorMod(index + direction, categories.size()));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    static boolean choicePanelShowsPrimaryAction(
            String primaryAction,
            List<GovernmentUiOpenPayload.Row> choices
    ) {
        return primaryAction != null && !primaryAction.isBlank() && choices != null;
    }

    static String currentVoteMetric(GovernmentUiOpenPayload.Row row) {
        if (row == null) return "";
        if ("recent_vote".equals(row.kind())) return row.state();
        if (row.voteCount() > 0L) return row.voteCount() + (row.voteCount() == 1L ? " vote" : " votes");
        return row.metricLabel();
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
        public int tabCount() { return 6; }
        public int tabRightEdge() { return GovernmentScreenChrome.tabRightEdge(LEFT_X, RIGHT_X + RIGHT_WIDTH - LEFT_X); }
        public int tabAreaX() { return LEFT_X; }
        public int tabAreaWidth() { return RIGHT_X + RIGHT_WIDTH - LEFT_X; }
        public int tabGap() { return TAB_GAP; }
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
        public int headerAuthorityX() { return headerSegmentX(1); }
        public int headerAuthorityWidth() { return headerSegmentWidth(); }
        public int headerRoleX() { return headerSegmentX(2); }
        public int headerRoleWidth() { return headerSegmentWidth(); }
        public int headerColorX() { return headerSegmentX(3); }
        public int headerColorWidth() { return headerSegmentEnd(3) - headerSegmentX(3); }
        public int closeX() { return HEADER_CLOSE_X; }
        public int foundingDecisionBottom() { return CONTENT_TOP + 9 + 18 + 3 * 21 + 18; }
        public int currentRowsY() { return CURRENT_ROWS_Y; }
        public int currentRowHeight() { return CURRENT_ROW_HEIGHT; }
        public int rowWidth() { return LEFT_WIDTH - 20; }
        public int currentRowMetricIconX() { return rowMetricIconX(LEFT_X + 10, rowWidth(), true); }
        public int moduleVisibleRows() { return (MAIN_BOTTOM - 10 - MODULE_ROWS_Y + ROW_GAP) / (MODULE_ROW_HEIGHT + ROW_GAP); }
        public int currentVoteVisibleRows() { return (MAIN_BOTTOM - 10 - CURRENT_ROWS_Y + ROW_GAP) / (CURRENT_ROW_HEIGHT + ROW_GAP); }
        public int readableMinimumScalePercent() { return READABLE_MINIMUM_SCALE_PERCENT; }
    }
}
