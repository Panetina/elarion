package panetina.elarion.addons.npcs.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class NpcBankQuotePayloadTest {
    @Test
    void requestNormalizesModeAndAmount() {
        NpcBankQuoteRequestPayload request = new NpcBankQuoteRequestPayload(
                UUID.randomUUID(), null, "WITHDRAW", -50);
        NpcBankQuoteRequestPayload unknown = new NpcBankQuoteRequestPayload(
                UUID.randomUUID(), "node", "transfer", 25);

        assertEquals("", request.nodeId());
        assertEquals("withdraw", request.mode());
        assertEquals(0, request.amount());
        assertEquals("deposit", unknown.mode());
    }

    @Test
    void quoteBoundsTaxAndNormalizesText() {
        NpcBankQuotePayload quote = new NpcBankQuotePayload(
                UUID.randomUUID(), null, "WITHDRAW", -5, 100L, -1,
                20_000, -4L, -8L, false, null);

        assertEquals("", quote.nodeId());
        assertEquals("withdraw", quote.mode());
        assertEquals(0, quote.amount());
        assertEquals(0, quote.physicalCurrency());
        assertEquals(10_000, quote.taxBasisPoints());
        assertEquals(0L, quote.fee());
        assertEquals(0L, quote.total());
        assertFalse(quote.valid());
        assertEquals("", quote.message());
    }
}
