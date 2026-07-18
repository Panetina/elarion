package panetina.elarion.addons.npcs.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import panetina.elarion.addons.npcs.client.ui.ElarionNpcPortraitRenderer;
import panetina.elarion.addons.npcs.network.NpcDialogueDismissPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOpenPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueOptionPayload;
import panetina.elarion.addons.npcs.network.NpcDialogueSelectPayload;
import panetina.elarion.addons.npcs.network.NpcTradeOfferPayload;
import panetina.elarion.addons.npcs.network.NpcTradeSnapshotPayload;
import panetina.elarion.addons.npcs.network.NpcTradeQuotePayload;
import panetina.elarion.addons.npcs.network.NpcTradeQuoteRequestPayload;
import panetina.elarion.addons.npcs.network.NpcTradePurchaseRequestPayload;
import panetina.elarion.addons.npcs.network.NpcTradePurchaseResultPayload;
import panetina.elarion.core.client.ui.ElarionActionBandLayout;
import panetina.elarion.core.client.ui.ElarionCivicColors;
import panetina.elarion.core.client.ui.ElarionCivicUi;
import panetina.elarion.core.client.ui.ElarionFooterActionLayout;
import panetina.elarion.core.client.ui.ElarionMoneySummary;
import panetina.elarion.core.client.ui.ElarionPanelHeaderLayout;
import panetina.elarion.core.client.ui.ElarionPairedButtonLayout;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionScrollViewportLayout;
import panetina.elarion.core.client.ui.ElarionScreen;
import panetina.elarion.core.client.ui.ElarionSemanticRowLayout;
import panetina.elarion.core.client.ui.ElarionServiceHeaderLayout;
import panetina.elarion.core.client.ui.ElarionUiIcons;
import panetina.elarion.core.client.ui.ElarionUiRenderer;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.client.ui.ElarionUiThemes;
import panetina.elarion.core.client.ui.ElarionUiTypography;

import java.util.List;
import java.util.UUID;

public final class NpcTradeScreen extends ElarionScreen {
    private static final int LOGICAL_WIDTH = 520;
    private static final int LOGICAL_HEIGHT = 390;
    private static final int PADDING = 14;
    private static final int HEADER_HEIGHT = 76;
    private static final int CATALOG_X = 24;
    private static final int CATALOG_Y = 124;
    private static final int CATALOG_WIDTH = 492;
    private static final int CATALOG_HEIGHT = 222;
    private static final int ROW_X = 24;
    private static final int ROW_Y = 166;
    private static final int ROW_WIDTH = 472;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_STEP = 24;
    private static final int ACTION_X = 24;
    private static final int ACTION_Y = 272;
    private static final int ACTION_WIDTH = 472;
    private static final int ACTION_HEIGHT = 64;
    private static final int VISIBLE_ROWS = 4;
    private static final int FOOTER_Y = 356;
    private static final int AMOUNT_BUTTON_SIZE = 22;
    private static final int AMOUNT_VALUE_WIDTH = 34;
    private static final int CONFIRM_WIDTH = 110;
    private static final int CONFIRM_HEIGHT = 26;
    private static final int ROW_ICON_INSET_X = 4;
    private static final int ROW_ICON_SIZE = 16;
    private static final int ROW_TITLE_OFFSET_X = 28;
    private static final int ROW_STOCK_OFFSET_X = 310;
    private static final int ROW_PRICE_ICON_OFFSET_X = 420;
    private static final int ROW_PRICE_ICON_SIZE = 14;
    private static final ElarionServiceHeaderLayout.PortraitTitle SERVICE_HEADER =
            ElarionServiceHeaderLayout.portraitTitle(0, 0, LOGICAL_WIDTH, HEADER_HEIGHT,
                    PADDING, 12, 52, 78, 18, 40, 18,
                    ElarionUiRenderer.CURRENCY_BADGE_WIDTH, 22, 38);
    private static final ElarionPairedButtonLayout.Pair MODE_BUTTONS =
            ElarionPairedButtonLayout.pair(PADDING, 90, 238, 238, 16, 24);
    private static final ElarionFooterActionLayout.Action FOOTER_ACTION =
            ElarionFooterActionLayout.action(PADDING, FOOTER_Y, 170, 22);
    private static final ElarionActionBandLayout.QuantityConfirmBand ACTION_BAND =
            ElarionActionBandLayout.quantityConfirmBand(ACTION_X, ACTION_Y, ACTION_WIDTH, ACTION_HEIGHT,
                    36, AMOUNT_BUTTON_SIZE, AMOUNT_VALUE_WIDTH, 46, CONFIRM_WIDTH, CONFIRM_HEIGHT);

    private NpcDialogueOpenPayload dialogue;
    private ElarionUiStyle style;
    private ElarionScaledLayout layout;
    private String mode = "buy";
    private ItemStack hoveredCatalogStack = ItemStack.EMPTY;
    private NpcTradeSnapshotPayload snapshot;
    private int scrollOffset;
    private String selectedOfferId = "";
    private NpcTradeQuotePayload selectedQuote;
    private String purchaseMessage = "";
    private boolean purchaseError;

    public NpcTradeScreen(NpcDialogueOpenPayload dialogue) {
        super(Text.literal(dialogue.npcName() + " Trade"));
        updateDialogue(dialogue);
    }

    public boolean belongsTo(NpcDialogueOpenPayload update) {
        return dialogue.npcId().equals(update.npcId());
    }

    public boolean belongsTo(NpcTradeSnapshotPayload update) {
        return dialogue.npcId().equals(update.npcId()) && dialogue.nodeId().equals(update.nodeId());
    }

    public boolean belongsTo(NpcTradeQuotePayload update) {
        return dialogue.npcId().equals(update.npcId()) && dialogue.nodeId().equals(update.nodeId());
    }

    public boolean belongsTo(NpcTradePurchaseResultPayload update) {
        return dialogue.npcId().equals(update.npcId()) && dialogue.nodeId().equals(update.nodeId());
    }

    public void updateQuote(NpcTradeQuotePayload update) {
        if (!belongsTo(update) || snapshot == null
                || !snapshot.catalogId().equals(update.catalogId())
                || snapshot.revision() != update.catalogRevision()
                || !selectedOfferId.equals(update.offerId())) return;
        selectedQuote = update;
        purchaseMessage = "";
    }

    public void updatePurchaseResult(NpcTradePurchaseResultPayload update) {
        if (!belongsTo(update) || !selectedOfferId.equals(update.offerId())) return;
        purchaseMessage = update.message();
        purchaseError = !update.successful();
    }

    public void updateSnapshot(NpcTradeSnapshotPayload update) {
        if (!belongsTo(update)) return;
        snapshot = update;
        scrollOffset = Math.min(scrollOffset, maxScroll());
        if (modeOffers().stream().noneMatch(offer -> offer.id().equals(selectedOfferId))) {
            selectedOfferId = modeOffers().isEmpty() ? "" : modeOffers().getFirst().id();
        }
        selectedQuote = modeOffers().stream().filter(offer -> offer.id().equals(selectedOfferId))
                .findFirst().map(this::initialQuote).orElse(null);
    }

    public void updateDialogue(NpcDialogueOpenPayload update) {
        dialogue = update;
        style = ElarionUiStyle.from(ElarionUiThemes.variant(update.themeVariant()));
        if (role(mode) == null) {
            mode = role("sell") == null ? "buy" : "sell";
        }
    }

    @Override
    protected void init() {
        layout = ElarionScaledLayout.fit(width, height, LOGICAL_WIDTH, LOGICAL_HEIGHT, 8,
                dialogue.minimumUiScalePercent());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        hoveredCatalogStack = ItemStack.EMPTY;
        context.fill(0, 0, width, height, style.backgroundOverlayColor());
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        context.getMatrices().push();
        context.getMatrices().translate(layout.screenX(), layout.screenY(), 0.0F);
        context.getMatrices().scale(layout.scale(), layout.scale(), 1.0F);

        ElarionCivicUi.attachedShell(context, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT, HEADER_HEIGHT);
        renderHeader(context);
        renderModeButtons(context, x, y);
        renderCatalog(context, x, y);
        renderFooter(context, x, y);
        context.getMatrices().pop();
        if (!hoveredCatalogStack.isEmpty()) {
            context.drawItemTooltip(textRenderer, hoveredCatalogStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        if (!inside(x, y, 0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT)) return false;
        if (SERVICE_HEADER.close().contains(x, y)) {
            close();
            return true;
        }
        if (MODE_BUTTONS.left().contains(x, y) && role("buy") != null) {
            switchMode("buy");
            return true;
        }
        if (MODE_BUTTONS.right().contains(x, y) && role("sell") != null) {
            switchMode("sell");
            return true;
        }
        if (FOOTER_ACTION.button().contains(x, y)) {
            NpcDialogueOptionPayload back = role("back");
            if (back != null) select(back); else close();
            return true;
        }
        if (snapshot != null) {
            List<NpcTradeOfferPayload> offers = modeOffers();
            ElarionScrollViewportLayout.RowViewport viewport = catalogViewport(offers);
            scrollOffset = viewport.firstVisible();
            int itemIndex = viewport.itemIndexAt(x, y);
            if (itemIndex >= 0) {
                selectOffer(offers.get(itemIndex));
                return true;
            }
            if (selectedQuote != null && ACTION_BAND.minus().contains(x, y)) {
                requestQuantity(selectedQuote.quantity() - 1);
                return true;
            }
            if (selectedQuote != null && ACTION_BAND.plus().contains(x, y)) {
                requestQuantity(selectedQuote.quantity() + 1);
                return true;
            }
            if (selectedQuote != null && ACTION_BAND.max().contains(x, y)) {
                requestQuantity(selectedQuote.maxQuantity());
                return true;
            }
            if (selectedQuote != null && selectedQuote.valid()
                    && ACTION_BAND.confirm().contains(x, y)) {
                ClientPlayNetworking.send(new NpcTradePurchaseRequestPayload(
                        UUID.randomUUID(), dialogue.npcId(), dialogue.nodeId(),
                        snapshot.catalogId(), snapshot.revision(), selectedOfferId,
                        selectedQuote.quantity()));
                purchaseMessage = "Submitting purchase...";
                purchaseError = false;
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double x = layout.logicalX(mouseX);
        double y = layout.logicalY(mouseY);
        ElarionScrollViewportLayout.RowViewport viewport = catalogViewport(modeOffers());
        if (!viewport.bounds().contains(x, y) || !viewport.scrollable()) return false;
        scrollOffset = viewport.scrolledFirstVisible(verticalAmount < 0 ? 1 : -1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            NpcDialogueOptionPayload back = role("back");
            if (back != null) select(back); else close();
            return true;
        }
        return true;
    }

    @Override
    public void close() {
        ClientPlayNetworking.send(new NpcDialogueDismissPayload(dialogue.npcId()));
        super.close();
    }

    private void renderHeader(DrawContext context) {
        ElarionServiceHeaderLayout.PortraitTitle header = SERVICE_HEADER;
        ElarionNpcPortraitRenderer.render(context, textRenderer, dialogue,
                header.portrait().x(), header.portrait().y(), header.portrait().width(), style);
        drawTitle(context, dialogue.npcName(), header.titleX(), header.titleY(), header.titleMaxWidth());
        ElarionUiTypography.draw(context, textRenderer, "Trade Services", header.titleX(), header.subtitleY(),
                style.mutedColor(), false);
        if (dialogue.hasCurrencyBalance()) {
            ElarionUiRenderer.currencyBadge(context, textRenderer, header.badge().x(), header.badge().y(),
                    dialogue.currencyBalance(), dialogue.currencyPlural(), style);
        }
        ElarionCivicUi.closeButton(context, header.close().x(), header.close().y(), header.close().width());
    }

    private void renderModeButtons(DrawContext context, double mouseX, double mouseY) {
        renderModeButton(context, mouseX, mouseY, "buy", MODE_BUTTONS.left(), "Buy");
        renderModeButton(context, mouseX, mouseY, "sell", MODE_BUTTONS.right(), "Sell");
    }

    private void renderCatalog(DrawContext context, double mouseX, double mouseY) {
        ElarionPanelHeaderLayout.LeftTitle header =
                ElarionPanelHeaderLayout.leftTitle(PADDING, CATALOG_Y, CATALOG_WIDTH, CATALOG_HEIGHT,
                        24, 10, 8, 10, 34);
        ElarionCivicUi.headerShell(context, header.bounds().x(), header.bounds().y(),
                header.bounds().width(), header.bounds().height(), header.headerHeight());
        String title = "buy".equals(mode) ? "Available Goods" : "Sell From Inventory";
        ElarionUiTypography.draw(context, textRenderer, title, header.titleX(), header.titleY(),
                style.titleColor(), false);
        ElarionCivicUi.divider(context, header.divider().x(), header.divider().y(), header.divider().width());

        if (snapshot == null) {
            renderCatalogMessage(context, "Loading trader catalog...");
            return;
        }
        List<NpcTradeOfferPayload> offers = modeOffers();
        if (offers.isEmpty()) {
            renderCatalogMessage(context, "buy".equals(mode)
                    ? snapshot.emptyMessage().isBlank() ? "No goods are currently listed." : snapshot.emptyMessage()
                    : "No matching inventory items can be sold here.");
            return;
        }
        ElarionScrollViewportLayout.RowViewport viewport = catalogViewport(offers);
        scrollOffset = viewport.firstVisible();
        for (int index = 0; index < viewport.visibleCount(); index++) {
            renderCatalogRow(context, mouseX, mouseY, ROW_X, viewport.rowY(index),
                    offers.get(viewport.firstVisible() + index));
        }
        renderSelectedOffer(context, mouseX, mouseY);
        if (viewport.scrollable()) {
            String page = "Rows " + (viewport.firstVisible() + 1) + "-" + viewport.lastVisibleExclusive()
                    + " / " + offers.size();
            int pageY = 261;
            int pageWidth = ElarionUiTypography.width(textRenderer, page);
            ElarionUiTypography.drawCentered(context, textRenderer, page, 260, pageY,
                    style.mutedColor(), false);
            drawTinyPageArrow(context, 260 - pageWidth / 2 - 10, pageY + 4, false);
            drawTinyPageArrow(context, 260 + pageWidth / 2 + 10, pageY + 4, true);
        }
    }

    private void drawTinyPageArrow(DrawContext context, int x, int y, boolean up) {
        int color = style.mutedColor();
        if (up) {
            context.fill(x + 1, y, x + 2, y + 1, color);
            context.fill(x, y + 1, x + 3, y + 2, color);
            return;
        }
        context.fill(x, y, x + 3, y + 1, color);
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
    }

    private void renderCatalogMessage(DrawContext context, String message) {
        ElarionUiIcons.drawOrDefault(context, "trade", 38, 180, 32);
        ElarionUiTypography.wrappedClipped(context, textRenderer, Text.literal(message),
                82, 182, 382, ElarionUiTypography.lineHeight() * 3,
                style.textColor(), style.mutedColor());
    }

    private void renderCatalogRow(
            DrawContext context, double mouseX, double mouseY, int x, int y,
            NpcTradeOfferPayload offer
    ) {
        ElarionSemanticRowLayout.CompactItemPriceRow row = ElarionSemanticRowLayout.compactItemPriceRow(
                x, y, ROW_WIDTH, ROW_HEIGHT, ROW_ICON_INSET_X, ROW_ICON_SIZE,
                ROW_TITLE_OFFSET_X, ROW_STOCK_OFFSET_X, ROW_PRICE_ICON_OFFSET_X,
                ROW_PRICE_ICON_SIZE);
        boolean hovered = row.row().contains(mouseX, mouseY);
        boolean iconHovered = row.icon().contains(mouseX, mouseY);
        ItemStack stack = offer.preview();
        if (iconHovered) hoveredCatalogStack = stack;
        boolean selected = offer.id().equals(selectedOfferId);
        int border = hovered || selected ? ElarionCivicColors.ACTIVE_GREEN : ElarionCivicColors.GOLD_SHADOW;
        ElarionCivicUi.thinBox(context, row.row().x(), row.row().y(), row.row().width(), row.row().height(),
                ElarionCivicColors.MESSAGE_BODY, border);
        context.drawItem(stack, row.icon().x(), row.icon().y());
        context.drawItemInSlot(textRenderer, stack, row.icon().x(), row.icon().y());
        ElarionUiTypography.draw(context, textRenderer,
                ElarionUiTypography.ellipsize(textRenderer, offer.label(), 280),
                row.titleX(), row.textY(textRenderer),
                style.textColor(), false);
        String stock = stockText(offer);
        if (!stock.isBlank()) {
            ElarionUiTypography.draw(context, textRenderer, stock,
                    row.metaX(), row.textY(textRenderer),
                    style.mutedColor(), false);
        }
        drawPrice(context, offer.price(), row.priceIcon().x(), row.priceIcon().y(),
                row.priceIcon().width());
    }

    private void renderSelectedOffer(DrawContext context, double mouseX, double mouseY) {
        if (selectedQuote == null) return;
        ElarionActionBandLayout.QuantityConfirmBand band = ACTION_BAND;
        ElarionCivicUi.thinBox(context, band.panel().x(), band.panel().y(), band.panel().width(), band.panel().height(),
                ElarionCivicColors.MESSAGE_BODY, ElarionCivicColors.GOLD_SHADOW);
        ElarionCivicUi.divider(context, ACTION_X + 8, band.dividerY(), ACTION_WIDTH - 16);
        boolean canDecrease = selectedQuote.quantity() > 1;
        boolean canIncrease = selectedQuote.quantity() < selectedQuote.maxQuantity();
        ElarionUiTypography.draw(context, textRenderer, "Qty", band.labelX(), band.labelY() + 4,
                style.mutedColor(), false);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.minus().x(), band.minus().y(),
                band.minus().width(), band.minus().height(),
                "-", band.minus().contains(mouseX, mouseY), false, canDecrease,
                ElarionCivicUi.Tone.NORMAL, style, 1);
        ElarionCivicUi.thinBox(context, band.value().x(), band.value().y(),
                band.value().width(), band.value().height(),
                ElarionCivicColors.MESSAGE_BODY_BOTTOM, ElarionCivicColors.GOLD_SHADOW);
        ElarionUiTypography.drawCentered(context, textRenderer,
                Integer.toString(selectedQuote.quantity()), band.value().x() + band.value().width() / 2,
                ElarionCivicUi.centeredTextY(textRenderer, band.value().y(), band.value().height()),
                style.textColor(), false);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.plus().x(), band.plus().y(),
                band.plus().width(), band.plus().height(),
                "+", band.plus().contains(mouseX, mouseY), false, canIncrease,
                ElarionCivicUi.Tone.NORMAL, style, 1);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.max().x(), band.max().y(),
                band.max().width(), band.max().height(),
                "Max", band.max().contains(mouseX, mouseY), false,
                selectedQuote.quantity() < selectedQuote.maxQuantity(),
                ElarionCivicUi.Tone.NORMAL, style, 1);
        drawTotalCell(context, "Subtotal", selectedQuote.subtotal(), 36, band.summaryY(), false);
        String taxLabel = selectedQuote.taxAuthorityLabel().isBlank()
                ? "Tax" : selectedQuote.taxAuthorityLabel();
        drawTotalCell(context, ElarionUiTypography.ellipsize(textRenderer, taxLabel, 76),
                selectedQuote.tax(), 168, band.summaryY(), false);
        drawTotalCell(context, "sell".equals(mode) ? "Payout" : "Total",
                selectedQuote.total(), band.confirm().x(), band.summaryY(), true);
        ElarionCivicUi.compactActionButton(context, textRenderer, band.confirm().x(), band.confirm().y(),
                band.confirm().width(), band.confirm().height(),
                "Confirm", band.confirm().contains(mouseX, mouseY), false,
                selectedQuote.valid(), ElarionCivicUi.Tone.PRIMARY, style, 1);
        if (!purchaseMessage.isBlank()) {
            int color = purchaseError ? ElarionCivicColors.REJECT_RED : ElarionCivicColors.ACTIVE_GREEN;
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiTypography.ellipsize(textRenderer, purchaseMessage, band.statusWidth()),
                    band.statusX(), band.statusY(), color, false);
        } else if (!selectedQuote.valid() && !selectedQuote.message().isBlank()) {
            ElarionUiTypography.draw(context, textRenderer,
                    ElarionUiTypography.ellipsize(textRenderer, selectedQuote.message(), band.statusWidth()),
                    band.statusX(), band.statusY(), ElarionCivicColors.REJECT_RED, false);
        }
    }

    private void drawPrice(DrawContext context, long price, int iconX, int y) {
        drawPrice(context, price, iconX, y, 16);
    }

    private void drawPrice(DrawContext context, long price, int iconX, int y, int iconSize) {
        drawPrice(context, price, iconX, y, iconSize, style.titleColor());
    }

    private void drawPrice(DrawContext context, long price, int iconX, int y, int iconSize, int valueColor) {
        String value = Long.toString(price);
        ElarionUiRenderer.currencyIcon(context, iconX, y, iconSize);
        ElarionUiTypography.draw(context, textRenderer, value,
                iconX + iconSize + 4, y + Math.max(1, (iconSize - ElarionUiTypography.lineHeight()) / 2) + 1,
                valueColor, false);
    }

    private void drawTotalCell(DrawContext context, String label, long price, int x, int y, boolean emphasis) {
        ElarionMoneySummary.drawCell(context, textRenderer,
                ElarionMoneySummary.cell(label, price, emphasis), x, y, 14, style);
    }

    private void renderFooter(DrawContext context, double mouseX, double mouseY) {
        ElarionSemanticRowLayout.Rect button = FOOTER_ACTION.button();
        boolean hovered = button.contains(mouseX, mouseY);
        ElarionCivicUi.compactActionButton(context, textRenderer,
                button.x(), button.y(), button.width(), button.height(),
                "Back to Conversation", hovered, false, role("back") != null,
                ElarionCivicUi.Tone.NORMAL, style, 1);
    }

    private void renderModeButton(
            DrawContext context, double mouseX, double mouseY, String role,
            ElarionSemanticRowLayout.Rect button, String label
    ) {
        boolean selected = role.equals(mode);
        boolean hovered = button.contains(mouseX, mouseY);
        ElarionCivicUi.compactActionButton(context, textRenderer,
                button.x(), button.y(), button.width(), button.height(),
                label, hovered || selected, false, role(role) != null,
                selected ? ElarionCivicUi.Tone.PRIMARY : ElarionCivicUi.Tone.NORMAL, style, 1);
    }

    private int maxScroll() {
        if (snapshot == null) return 0;
        return catalogViewport(modeOffers()).maximumFirstVisible();
    }

    private ElarionScrollViewportLayout.RowViewport catalogViewport(List<NpcTradeOfferPayload> offers) {
        return ElarionScrollViewportLayout.rows(
                ROW_X, ROW_Y, ROW_WIDTH,
                VISIBLE_ROWS * ROW_STEP - (ROW_STEP - ROW_HEIGHT),
                ROW_HEIGHT, ROW_STEP - ROW_HEIGHT, offers.size(), scrollOffset);
    }

    private List<NpcTradeOfferPayload> modeOffers() {
        return snapshot == null ? List.of() : snapshot.offers().stream()
                .filter(offer -> mode.equals(offer.direction())).toList();
    }

    private void selectOffer(NpcTradeOfferPayload offer) {
        selectedOfferId = offer.id();
        selectedQuote = initialQuote(offer);
        purchaseMessage = "";
        purchaseError = false;
    }

    private void switchMode(String nextMode) {
        mode = nextMode;
        scrollOffset = 0;
        if (snapshot == null) {
            selectedOfferId = "";
            selectedQuote = null;
            return;
        }
        selectedOfferId = modeOffers().isEmpty() ? "" : modeOffers().getFirst().id();
        selectedQuote = modeOffers().stream().filter(offer -> offer.id().equals(selectedOfferId))
                .findFirst().map(this::initialQuote).orElse(null);
        purchaseMessage = "";
        purchaseError = false;
    }

    private NpcTradeQuotePayload initialQuote(NpcTradeOfferPayload offer) {
        return new NpcTradeQuotePayload(dialogue.npcId(), dialogue.nodeId(), snapshot.catalogId(),
                snapshot.revision(), offer.id(), offer.quantity(), offer.maxQuantity(), offer.subtotal(),
                offer.taxBasisPoints(), offer.tax(), offer.total(), offer.policyRevision(),
                offer.taxAuthorityLabel(), offer.enabled(), offer.disabledReason());
    }

    private static String stockText(NpcTradeOfferPayload offer) {
        if (offer.stockRemaining() < 0) return "";
        return "Stock " + offer.stockRemaining();
    }

    private void requestQuantity(int quantity) {
        if (snapshot == null || selectedQuote == null
                || quantity < 1 || quantity > selectedQuote.maxQuantity()) return;
        ClientPlayNetworking.send(new NpcTradeQuoteRequestPayload(
                dialogue.npcId(), dialogue.nodeId(), snapshot.catalogId(), snapshot.revision(),
                selectedOfferId, quantity));
    }

    private NpcDialogueOptionPayload role(String role) {
        return dialogue.options().stream()
                .filter(option -> role.equals(option.presentationRole()))
                .findFirst()
                .orElse(null);
    }

    private void select(NpcDialogueOptionPayload option) {
        ClientPlayNetworking.send(new NpcDialogueSelectPayload(
                dialogue.npcId(), dialogue.nodeId(), option.id()));
    }

    private void drawTitle(DrawContext context, String title, int x, int y, int maxWidth) {
        float scale = 1.25F * ElarionUiTypography.scale();
        int unscaledWidth = Math.max(1, (int) Math.floor(maxWidth / scale));
        String visible = textRenderer.trimToWidth(title == null ? "" : title, unscaledWidth);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0F);
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(textRenderer, visible, 0, 0, style.titleColor(), false);
        context.getMatrices().pop();
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && y >= top && x < left + width && y < top + height;
    }
}
