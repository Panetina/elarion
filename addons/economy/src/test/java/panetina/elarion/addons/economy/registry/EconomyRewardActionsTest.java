package panetina.elarion.addons.economy.registry;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.model.TransactionStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyRewardActionsTest {
    @Test
    void amountRejectsBlankNegativeZeroAndOverflowText() {
        assertEquals(0L, EconomyRewardActions.amount(Map.of()));
        assertEquals(0L, EconomyRewardActions.amount(Map.of("amount", "0")));
        assertEquals(0L, EconomyRewardActions.amount(Map.of("amount", "-10")));
        assertEquals(0L, EconomyRewardActions.amount(Map.of("amount", "not-a-number")));
        assertEquals(0L, EconomyRewardActions.amount(Map.of("amount", "999999999999999999999")));
    }

    @Test
    void amountAcceptsPositiveLongs() {
        assertEquals(1L, EconomyRewardActions.amount(Map.of("amount", "1")));
        assertEquals(50_000L, EconomyRewardActions.amount(Map.of("amount", "50000")));
    }

    @Test
    void transactionResultsBecomeRegistryResults() {
        var success = EconomyRewardActions.result(TransactionResult.success(null), "Granted 10 currency.");
        assertTrue(success.success());
        assertEquals("Granted 10 currency.", success.message());

        var failure = EconomyRewardActions.result(
                TransactionResult.failure(TransactionStatus.INSUFFICIENT_FUNDS, "Insufficient funds."),
                "Granted 10 currency.");
        assertFalse(failure.success());
        assertEquals("Insufficient funds.", failure.message());
    }
}
