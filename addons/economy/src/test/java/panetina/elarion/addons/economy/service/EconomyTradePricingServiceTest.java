package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.addons.economy.model.EconomyServicePrice;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.economy.model.EconomyTradeDirection;
import panetina.elarion.addons.economy.model.EconomyTradePriceRequest;
import panetina.elarion.addons.economy.model.EconomyTradePriceSource;
import panetina.elarion.addons.economy.storage.EconomyTaxPolicyStorage;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyTradePricingServiceTest {
    @TempDir
    Path root;

    @Test
    void quotesBuyWithFixedFallbackAndTax() {
        EconomyTaxAuthority authority = EconomyTaxAuthority.worldheart("elarion:worldheart");
        EconomyTaxPolicyService taxes = taxes(500);
        EconomyPricingService prices = prices();

        var quote = prices.quoteTradePrice(new EconomyTradePriceRequest(
                EconomyTradeDirection.BUY, authority, EconomyTaxCategory.NPC_TRADE,
                "", false, 20L, 3, 64, -1, 0,
                "catalog", "offer", "elarion:test", Map.of()), taxes);

        assertTrue(quote.valid(), quote.message());
        assertEquals(EconomyTradePriceSource.FIXED, quote.priceSource());
        assertEquals(20L, quote.unitPrice());
        assertEquals(60L, quote.subtotal());
        assertEquals(3L, quote.feeOrTax());
        assertEquals(63L, quote.totalCost());
        assertEquals(0L, quote.totalPayout());
    }

    @Test
    void servicePriceKeyOverridesFallback() {
        EconomyTaxAuthority authority = EconomyTaxAuthority.realm("oak", "elarion:oak");
        EconomyTaxPolicyService taxes = taxes(0);
        EconomyPricingService prices = prices(Map.of(
                "npc.sell.cobble", new EconomyServicePrice("npc.sell.cobble", 7L, 1L, 10L)));

        var quote = prices.quoteTradePrice(new EconomyTradePriceRequest(
                EconomyTradeDirection.BUY, authority, EconomyTaxCategory.NPC_TRADE,
                "npc.sell.cobble", false, 99L, 2, 64, -1, 0,
                "catalog", "offer", "elarion:test", Map.of()), taxes);

        assertTrue(quote.valid(), quote.message());
        assertEquals(EconomyTradePriceSource.SERVICE_PRICE, quote.priceSource());
        assertEquals(7L, quote.unitPrice());
        assertEquals(14L, quote.totalCost());
        assertTrue(quote.priceRevision() != 0L);
    }

    @Test
    void unknownRequiredPriceKeyRejectsWithoutFallback() {
        EconomyTaxAuthority authority = EconomyTaxAuthority.worldheart("elarion:worldheart");
        EconomyPricingService prices = prices();

        var quote = prices.quoteTradePrice(new EconomyTradePriceRequest(
                EconomyTradeDirection.BUY, authority, EconomyTaxCategory.NPC_TRADE,
                "missing.key", true, 20L, 1, 64, -1, 0,
                "catalog", "offer", "elarion:test", Map.of()), taxes(0));

        assertFalse(quote.valid());
        assertEquals(EconomyTradePriceSource.UNAVAILABLE, quote.priceSource());
    }

    @Test
    void quotesSellNetPayoutWithFee() {
        EconomyTaxAuthority authority = EconomyTaxAuthority.realm("oak", "elarion:oak");
        EconomyTaxPolicyService taxes = taxes(1_000);
        EconomyPricingService prices = prices();

        var quote = prices.quoteTradePrice(new EconomyTradePriceRequest(
                EconomyTradeDirection.SELL, authority, EconomyTaxCategory.NPC_TRADE,
                "", false, 10L, 5, 64, 12, 64,
                "catalog", "offer", "elarion:test", Map.of()), taxes);

        assertTrue(quote.valid(), quote.message());
        assertEquals(50L, quote.subtotal());
        assertEquals(5L, quote.feeOrTax());
        assertEquals(0L, quote.totalCost());
        assertEquals(45L, quote.totalPayout());
    }

    @Test
    void rejectsOverflowAndOutOfBoundsQuantity() {
        EconomyTaxAuthority authority = EconomyTaxAuthority.worldheart("elarion:worldheart");
        EconomyPricingService prices = prices();

        assertFalse(prices.quoteTradePrice(new EconomyTradePriceRequest(
                EconomyTradeDirection.BUY, authority, EconomyTaxCategory.NPC_TRADE,
                "", false, Long.MAX_VALUE, 2, 64, -1, 0,
                "catalog", "offer", "elarion:test", Map.of()), taxes(0)).valid());

        assertFalse(prices.quoteTradePrice(new EconomyTradePriceRequest(
                EconomyTradeDirection.BUY, authority, EconomyTaxCategory.NPC_TRADE,
                "", false, 1L, 65, 64, -1, 0,
                "catalog", "offer", "elarion:test", Map.of()), taxes(0)).valid());
    }

    @Test
    void requestBoundsContextForFutureDynamicPricing() {
        Map<String, String> context = new LinkedHashMap<>();
        for (int index = 0; index < 20; index++) {
            context.put("key" + index, "x".repeat(160));
        }

        EconomyTradePriceRequest request = new EconomyTradePriceRequest(
                EconomyTradeDirection.SELL, EconomyTaxAuthority.worldheart("elarion:worldheart"),
                EconomyTaxCategory.NPC_TRADE, "", false, 1L, 1, 64, -1, 0,
                "catalog", "offer", "elarion:test", context);

        assertEquals(16, request.context().size());
        assertTrue(request.context().values().stream().allMatch(value -> value.length() == 128));
    }

    private EconomyPricingService prices() {
        return prices(Map.of());
    }

    private EconomyPricingService prices(Map<String, EconomyServicePrice> definitions) {
        return new EconomyPricingService(definitions);
    }

    private EconomyTaxPolicyService taxes(int fallbackRate) {
        EconomyTaxPolicyService service = new EconomyTaxPolicyService(
                new EconomyTaxPolicyStorage(root), () -> fallbackRate);
        service.bind(null);
        return service;
    }
}
