package panetina.elarion.addons.economy.registry;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.model.TransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyNpcActionsTest {
    @Test
    void formatsSuccessfulWithdraw() {
        var result = EconomyNpcActions.withdrawResult(TransactionResult.success(null), 10);

        assertTrue(result.success());
        assertEquals("Withdrew 10 sigils.", result.message());
    }

    @Test
    void rejectsInvalidWithdrawAmount() {
        var result = EconomyNpcActions.withdrawResult(TransactionResult.success(null), 0);

        assertFalse(result.success());
        assertEquals("Withdrawal amount must be positive.", result.message());
    }

    @Test
    void returnsTransactionFailureMessage() {
        var result = EconomyNpcActions.depositAllResult(
                TransactionResult.failure(TransactionStatus.INSUFFICIENT_FUNDS, "Not enough physical currency."), 4);

        assertFalse(result.success());
        assertEquals("Not enough physical currency.", result.message());
    }

    @Test
    void formatsSuccessfulDepositAmount() {
        var result = EconomyNpcActions.depositResult(TransactionResult.success(null), 12);

        assertTrue(result.success());
        assertEquals("Deposited 12 sigils.", result.message());
    }

    @Test
    void rejectsInvalidDepositAmount() {
        var result = EconomyNpcActions.depositResult(TransactionResult.success(null), 0);

        assertFalse(result.success());
        assertEquals("Deposit amount must be positive.", result.message());
    }
}
