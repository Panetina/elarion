package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.economy.model.EconomyBankMode;
import panetina.elarion.addons.economy.model.EconomyBankQuote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyInventoryServiceTest {
    @Test
    void currencyCapacityCombinesEmptySlotsAndPartialCurrencySpace() {
        assertEquals(999, EconomyInventoryService.currencyCapacity(1, 0, 999));
        assertEquals(989, EconomyInventoryService.currencyCapacity(0, 989, 999));
        assertEquals(1_498, EconomyInventoryService.currencyCapacity(1, 499, 999));
        assertEquals(0, EconomyInventoryService.currencyCapacity(-1, 0, 999));
        assertEquals(Integer.MAX_VALUE, EconomyInventoryService.currencyCapacity(Integer.MAX_VALUE, 999, 999));
    }

    @Test
    void depositQuoteUsesPhysicalCurrencyAndNoFee() {
        EconomyBankQuote quote = EconomyInventoryService.quoteBank(
                EconomyBankMode.DEPOSIT, 100, 10L, 120, 500);

        assertTrue(quote.valid());
        assertEquals(100, quote.amount());
        assertEquals(0L, quote.fee());
        assertEquals(100L, quote.total());
        assertEquals(0, quote.taxBasisPoints());
    }

    @Test
    void depositQuoteRejectsInsufficientPhysicalCurrency() {
        EconomyBankQuote quote = EconomyInventoryService.quoteBank(
                EconomyBankMode.DEPOSIT, 100, 1_000L, 99, 0);

        assertFalse(quote.valid());
        assertEquals("Not enough carried physical currency.", quote.message());
    }

    @Test
    void withdrawQuoteIncludesConfiguredTaxAndTotalDebit() {
        EconomyBankQuote quote = EconomyInventoryService.quoteBank(
                EconomyBankMode.WITHDRAW, 100, 120L, 0, 250);

        assertTrue(quote.valid());
        assertEquals(250, quote.taxBasisPoints());
        assertEquals(3L, quote.fee());
        assertEquals(103L, quote.total());
    }

    @Test
    void withdrawQuoteRejectsInsufficientBankBalanceWithoutMutating() {
        EconomyBankQuote quote = EconomyInventoryService.quoteBank(
                EconomyBankMode.WITHDRAW, 100, 102L, 999, 250);

        assertFalse(quote.valid());
        assertEquals(102L, quote.balance());
        assertEquals(999, quote.physicalCurrency());
        assertEquals("Not enough banked currency.", quote.message());
    }
}
