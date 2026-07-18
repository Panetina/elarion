package panetina.elarion.addons.npcs.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcTradePurchasePayloadTest {
    @Test
    void requestBoundsQuantityAndNormalizesText() {
        UUID purchaseId = UUID.randomUUID();
        NpcTradePurchaseRequestPayload low = new NpcTradePurchaseRequestPayload(
                purchaseId, null, null, null, 1L, null, -8);
        NpcTradePurchaseRequestPayload high = new NpcTradePurchaseRequestPayload(
                purchaseId, UUID.randomUUID(), "node", "catalog", 2L, "offer", 500);

        assertEquals(1, low.quantity());
        assertEquals("", low.nodeId());
        assertEquals("", low.catalogId());
        assertEquals("", low.offerId());
        assertEquals(64, high.quantity());
    }

    @Test
    void resultNormalizesMessageAndNonNegativeAmounts() {
        NpcTradePurchaseResultPayload result = new NpcTradePurchaseResultPayload(
                null, null, null, null, 0, -1L, -2L, -3L, true, null);

        assertEquals(1, result.quantity());
        assertEquals(0L, result.subtotal());
        assertEquals(0L, result.tax());
        assertEquals(0L, result.total());
        assertEquals("", result.message());
    }
}
