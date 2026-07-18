package panetina.elarion.core.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.core.client.ui.ElarionActionBandLayout;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionDetailBodyLayout;
import panetina.elarion.core.client.ui.ElarionDetailCardLayout;
import panetina.elarion.core.client.ui.ElarionFooterActionLayout;
import panetina.elarion.core.client.ui.ElarionListRangeMarker;
import panetina.elarion.core.client.ui.ElarionMoneySummary;
import panetina.elarion.core.client.ui.ElarionPairedButtonLayout;
import panetina.elarion.core.client.ui.ElarionPresetButtonRowLayout;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionSemanticRowLayout;
import panetina.elarion.core.client.ui.ElarionServiceHeaderLayout;
import panetina.elarion.core.client.ui.ElarionSplitSummaryLayout;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

public final class ElarionUiComponentGalleryScreen extends ElarionScreen {
    private static final int WIDTH = 560;
    private static final int HEIGHT = 520;
    private static final int HEADER = 44;
    private static final int CLOSE_SIZE = 18;

    private ElarionScaledLayout layout;

    public ElarionUiComponentGalleryScreen() {
        super(Text.literal("Elarion UI Component Gallery"));
    }

    @Override
    protected void init() {
        layout = ElarionScaledLayout.fit(width, height, WIDTH, HEIGHT, 8, 72);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ElarionUiStyle style = ElarionUiStyle.from(ElarionUiThemes.variant("default"));
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        layout = ElarionScaledLayout.fit(width, height, WIDTH, HEIGHT, 8, 72);
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);

        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, WIDTH, HEIGHT, HEADER);
        ElarionCivicUi.headerOrnament(context, WIDTH / 2 - 130, 19, true);
        ElarionCivicUi.headerOrnament(context, WIDTH / 2 + 130, 19, false);
        ElarionUiTypography.drawCentered(context, textRenderer, "UI COMPONENT GALLERY", WIDTH / 2,
                ElarionCivicUi.centeredTextY(textRenderer, 0, HEADER), style.titleColor(), false);
        ElarionCivicUi.closeButton(context, closeX(), 13, CLOSE_SIZE);

        drawListAndMoney(context, style);
        drawActionBand(context, lx, ly, style);
        drawRows(context, lx, ly, style);
        drawDetailHeader(context, style);
        drawServiceHelpers(context, lx, ly, style);

        ElarionUiTypography.drawCentered(context, textRenderer,
                "Development-only static reference. Use docs/ui/COMPONENT_REFERENCE.md for contracts.",
                WIDTH / 2, HEIGHT - 14, style.mutedColor(), false);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || layout == null) return false;
        double lx = layout.logicalX(mouseX);
        double ly = layout.logicalY(mouseY);
        if (inside(lx, ly, closeX(), 13, CLOSE_SIZE, CLOSE_SIZE)) {
            close();
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawListAndMoney(DrawContext context, ElarionUiStyle style) {
        int x = 22;
        int y = 58;
        ElarionCivicUi.headerShell(context, x, y, 238, 92, 22);
        ElarionUiTypography.draw(context, textRenderer, "Range + Money", x + 12, y + 7,
                style.titleColor(), false);
        ElarionListRangeMarker.draw(context, textRenderer, x + 119, y + 35,
                new ElarionListRangeMarker.Range(3, 6, 12, true, true), style.mutedColor());
        ElarionMoneySummary.drawCell(context, textRenderer,
                ElarionMoneySummary.cell("Subtotal", 25, false), x + 20, y + 58, 14, style);
        ElarionMoneySummary.drawCell(context, textRenderer,
                ElarionMoneySummary.cell("Total", 28, true), x + 132, y + 58, 14, style);
    }

    private void drawActionBand(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        ElarionActionBandLayout.QuantityConfirmBand band = ElarionActionBandLayout.quantityConfirmBand(
                282, 58, 254, 92, 34, 22, 34, 46, 88, 24);
        ElarionCivicUi.thinBox(context, band.panel().x(), band.panel().y(), band.panel().width(),
                band.panel().height(), ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        ElarionUiTypography.draw(context, textRenderer, "Action Band", band.panel().x() + 12,
                band.panel().y() + 8, style.titleColor(), false);
        ElarionCivicUi.divider(context, band.panel().x() + 8, band.dividerY(), band.panel().width() - 16);
        ElarionUiTypography.draw(context, textRenderer, "Qty", band.labelX(), band.labelY() + 4,
                style.mutedColor(), false);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.minus().x(), band.minus().y(),
                band.minus().width(), band.minus().height(), "-", band.minus().contains(mouseX, mouseY),
                false, true, ElarionCivicUi.Tone.NORMAL, style, 1);
        ElarionCivicUi.thinBox(context, band.value().x(), band.value().y(), band.value().width(),
                band.value().height(), ElarionCivicColors.MESSAGE_BODY_BOTTOM, ElarionCivicColors.GOLD_SHADOW);
        ElarionUiTypography.drawCentered(context, textRenderer, "2",
                band.value().x() + band.value().width() / 2,
                ElarionCivicUi.centeredTextY(textRenderer, band.value().y(), band.value().height()),
                style.textColor(), false);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.plus().x(), band.plus().y(),
                band.plus().width(), band.plus().height(), "+", band.plus().contains(mouseX, mouseY),
                false, true, ElarionCivicUi.Tone.NORMAL, style, 1);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.max().x(), band.max().y(),
                band.max().width(), band.max().height(), "Max", band.max().contains(mouseX, mouseY),
                false, true, ElarionCivicUi.Tone.NORMAL, style, 1);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.confirm().x(), band.confirm().y(),
                band.confirm().width(), band.confirm().height(), "Confirm",
                band.confirm().contains(mouseX, mouseY), false, true, ElarionCivicUi.Tone.PRIMARY, style, 1);
        ElarionMoneySummary.drawCell(context, textRenderer,
                ElarionMoneySummary.cell("Total", 50, true), band.confirm().x(), band.summaryY(), 14, style);
    }

    private void drawRows(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        int x = 22;
        int y = 170;
        ElarionCivicUi.headerShell(context, x, y, 514, 132, 22);
        ElarionUiTypography.draw(context, textRenderer, "Semantic Rows", x + 12, y + 7,
                style.titleColor(), false);

        ElarionSemanticRowLayout.CompactItemPriceRow itemRow =
                ElarionSemanticRowLayout.compactItemPriceRow(x + 14, y + 36, 486, 20,
                        4, 16, 28, 310, 420, 14);
        boolean itemHover = itemRow.row().contains(mouseX, mouseY);
        ElarionCivicUi.thinBox(context, itemRow.row().x(), itemRow.row().y(), itemRow.row().width(),
                itemRow.row().height(), ElarionCivicColors.MESSAGE_BODY,
                itemHover ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW);
        ElarionUiIcons.drawOrDefault(context, "nether_ticket", itemRow.icon().x(), itemRow.icon().y(),
                itemRow.icon().width());
        ElarionUiTypography.draw(context, textRenderer, "Nether Gate Ticket", itemRow.titleX(),
                itemRow.textY(textRenderer), style.textColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Stock 8", itemRow.metaX(),
                itemRow.textY(textRenderer), style.mutedColor(), false);
        ElarionUiRenderer.currencyIcon(context, itemRow.priceIcon().x(), itemRow.priceIcon().y(),
                itemRow.priceIcon().width());
        ElarionUiTypography.draw(context, textRenderer, "25", itemRow.priceValueX(),
                itemRow.textY(textRenderer), style.titleColor(), false);

        ElarionSemanticRowLayout.CompactRecordRow recordRow =
                ElarionSemanticRowLayout.compactRecordRow(x + 14, y + 68, 486, 40,
                        10, 20, 34, 126);
        boolean recordHover = recordRow.row().contains(mouseX, mouseY);
        ElarionCivicUi.rowSurface(context, recordRow.row().x(), recordRow.row().y(), recordRow.row().width(),
                recordRow.row().height(), true, recordHover, false);
        ElarionUiIcons.drawOrDefault(context, "history", recordRow.icon().x(), recordRow.icon().y(),
                recordRow.icon().width());
        ElarionUiTypography.draw(context, textRenderer, "Founding Election Completed",
                recordRow.titleX(), recordRow.titleY(), ElarionCivicColors.ACTIVE_GREEN, false);
        ElarionCivicUi.statusChip(context, textRenderer, recordRow.titleX(), recordRow.tagY(),
                "History", 62, ElarionCivicUi.Tone.INFO, style);
        ElarionUiIcons.drawOrDefault(context, "timer", recordRow.metricX(), recordRow.metricY() - 2, 12);
        ElarionUiTypography.draw(context, textRenderer, "1d ago", recordRow.metricX() + 17,
                recordRow.metricY(), style.textColor(), false);
        ElarionUiIcons.drawOrDefault(context, "people", recordRow.metricX(),
                recordRow.secondaryMetricY() - 2, 12);
        ElarionUiTypography.draw(context, textRenderer, "12 votes", recordRow.metricX() + 17,
                recordRow.secondaryMetricY(), style.titleColor(), false);
    }

    private void drawDetailHeader(DrawContext context, ElarionUiStyle style) {
        int x = 22;
        int y = 314;
        ElarionDetailCardLayout.IdentityHeader header =
                ElarionDetailCardLayout.identityHeader(x, y, 300, 30, 10, 2, 18, 32);
        ElarionCivicUi.thinBox(context, header.bounds().x(), header.bounds().y(),
                header.bounds().width(), header.bounds().height(),
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        ElarionCivicUi.thinBox(context, header.icon().x(), header.icon().y(),
                header.icon().width(), header.icon().height(),
                ElarionCivicColors.ROOT_SURFACE, ElarionCivicColors.GOLD_BORDER);
        ElarionUiIcons.drawOrDefault(context, "proposal", header.icon().x() + 5, header.icon().y() + 5, 20);
        ElarionUiTypography.draw(context, textRenderer, "Detail Header", header.textX(),
                header.titleY(), ElarionCivicColors.ACTIVE_GREEN, false);
        ElarionCivicUi.statusChip(context, textRenderer, header.textX(), header.tagY(),
                "Tag", 44, ElarionCivicUi.Tone.INFO, style);
        ElarionUiTypography.draw(context, textRenderer, "Actor or subtitle", header.textX(),
                header.subtitleY(), style.mutedColor(), false);

        ElarionDetailBodyLayout.SectionTitle section =
                ElarionDetailBodyLayout.sectionTitle(x + 324, y + 4, 16, 6, -3);
        ElarionUiIcons.drawOrDefault(context, "history", section.icon().x(), section.icon().y(),
                section.icon().width());
        ElarionUiTypography.draw(context, textRenderer, "Detail Body", section.textX(),
                section.textY(), style.titleColor(), false);
        ElarionDetailBodyLayout.BodyText body =
                ElarionDetailBodyLayout.bodyText(x + 324, y + 24, 190, 34);
        ElarionUiRenderer.wrappedClipped(context, textRenderer,
                Text.literal("Wrapped body text remains bounded inside the detail viewport."),
                body.body().x(), body.body().y(), body.body().width(), body.body().height(),
                style.textColor(), style.mutedColor());
        ElarionDetailBodyLayout.KeyValueRow keyValue =
                ElarionDetailBodyLayout.keyValueRow(x + 324, y + 64, 190, 62, 8);
        ElarionUiTypography.draw(context, textRenderer, "Status", keyValue.labelX(),
                keyValue.textY(), style.mutedColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Ready", keyValue.valueX(),
                keyValue.textY(), ElarionCivicColors.ACTIVE_GREEN, false);
    }

    private void drawServiceHelpers(DrawContext context, double mouseX, double mouseY, ElarionUiStyle style) {
        int x = 22;
        int y = 390;
        ElarionCivicUi.headerShell(context, x, y, 514, 104, 22);
        ElarionUiTypography.draw(context, textRenderer, "Service Helpers", x + 12, y + 7,
                style.titleColor(), false);

        ElarionServiceHeaderLayout.PortraitTitle header =
                ElarionServiceHeaderLayout.portraitTitle(x + 12, y + 28, 242, 42,
                        4, 4, 34, 48, 7, 24, 14, 78, 18, 10);
        ElarionCivicUi.thinBox(context, header.portrait().x(), header.portrait().y(),
                header.portrait().width(), header.portrait().height(),
                ElarionCivicColors.MESSAGE_BODY_BOTTOM, ElarionCivicColors.GOLD_BORDER);
        ElarionUiIcons.drawOrDefault(context, "bank", header.portrait().x() + 7,
                header.portrait().y() + 7, 20);
        ElarionUiTypography.draw(context, textRenderer, "Service Header", header.titleX(),
                header.titleY(), style.titleColor(), false);
        ElarionUiTypography.draw(context, textRenderer, "Portrait, badge, close", header.titleX(),
                header.subtitleY(), style.mutedColor(), false);
        ElarionCivicUi.thinBox(context, header.badge().x(), header.badge().y(),
                header.badge().width(), header.badge().height(),
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        ElarionUiRenderer.currencyIcon(context, header.badge().x() + 5, header.badge().y() + 4, 12);
        ElarionUiTypography.draw(context, textRenderer, "76", header.badge().x() + 22,
                header.badge().y() + 6, 0xFF9696D1, false);
        ElarionCivicUi.closeButton(context, header.close().x(), header.close().y(), header.close().width());

        ElarionPairedButtonLayout.Pair pair =
                ElarionPairedButtonLayout.pair(x + 276, y + 30, 92, 92, 10, 18);
        ElarionCivicUi.compactActionButton(context, textRenderer, pair.left().x(), pair.left().y(),
                pair.left().width(), pair.left().height(), "Buy",
                pair.left().contains(mouseX, mouseY), false, true, ElarionCivicUi.Tone.PRIMARY, style, 1);
        ElarionCivicUi.compactActionButton(context, textRenderer, pair.right().x(), pair.right().y(),
                pair.right().width(), pair.right().height(), "Sell",
                pair.right().contains(mouseX, mouseY), false, true, ElarionCivicUi.Tone.NORMAL, style, 1);

        ElarionPresetButtonRowLayout.PresetConfirmRow preset =
                ElarionPresetButtonRowLayout.presetConfirmRow(x + 276, y + 56,
                        42, 18, 8, 3, 14, 100);
        String[] labels = {"+100", "+1K", "+10K"};
        for (int index = 0; index < preset.presets().buttons().size(); index++) {
            ElarionSemanticRowLayout.Rect button = preset.presets().button(index);
            ElarionCivicUi.compactActionButton(context, textRenderer, button.x(), button.y(),
                    button.width(), button.height(), labels[index], button.contains(mouseX, mouseY),
                    false, true, ElarionCivicUi.Tone.NORMAL, style, 1);
        }
        ElarionCivicUi.compactActionButton(context, textRenderer, preset.confirm().x(), preset.confirm().y(),
                preset.confirm().width(), preset.confirm().height(), "Confirm",
                preset.confirm().contains(mouseX, mouseY), false, true, ElarionCivicUi.Tone.PRIMARY, style, 1);

        ElarionSplitSummaryLayout.Split summary = ElarionSplitSummaryLayout.split(x + 12, y + 76, 242, y + 82, x + 140);
        ElarionCivicUi.divider(context, summary.divider().x(), summary.divider().y(), summary.divider().width());
        ElarionMoneySummary.drawCell(context, textRenderer,
                ElarionMoneySummary.cell("Fee", 3, false), summary.leftX(), summary.leftY(), 14, style);
        ElarionMoneySummary.drawCell(context, textRenderer,
                ElarionMoneySummary.cell("Total", 103, true), summary.rightX(), summary.rightY(), 14, style);

        ElarionFooterActionLayout.Action footer = ElarionFooterActionLayout.action(x + 276, y + 80, 192, 18);
        ElarionCivicUi.compactActionButton(context, textRenderer, footer.button().x(), footer.button().y(),
                footer.button().width(), footer.button().height(), "Back to Conversation",
                footer.button().contains(mouseX, mouseY), false, true, ElarionCivicUi.Tone.NORMAL, style, 1);
    }

    private int closeX() {
        return WIDTH - 24 - CLOSE_SIZE;
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && y >= top && x < left + width && y < top + height;
    }
}
