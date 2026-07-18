package panetina.elarion.addons.npcs.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcTradeQuotePayloadTest {
    @Test
    void boundsRequestQuantityAndNormalizesText() {
        NpcTradeQuoteRequestPayload low = new NpcTradeQuoteRequestPayload(
                UUID.randomUUID(), null, null, 1L, null, -5);
        NpcTradeQuoteRequestPayload high = new NpcTradeQuoteRequestPayload(
                UUID.randomUUID(), "node", "catalog", 1L, "offer", 500);

        assertEquals(1, low.quantity());
        assertEquals("", low.nodeId());
        assertEquals(64, high.quantity());
    }

    @Test
    void boundsServerQuoteMetadata() {
        NpcTradeQuotePayload quote = new NpcTradeQuotePayload(
                UUID.randomUUID(), "node", "catalog", 3L, "offer",
                80, 100, 10L, 20_000, 2L, 12L, 4L,
                null, true, null);

        assertEquals(64, quote.quantity());
        assertEquals(64, quote.maxQuantity());
        assertEquals(10_000, quote.taxBasisPoints());
        assertEquals("", quote.taxAuthorityLabel());
        assertEquals("", quote.message());
    }
}
