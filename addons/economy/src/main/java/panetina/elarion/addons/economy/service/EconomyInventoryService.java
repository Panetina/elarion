package panetina.elarion.addons.economy.service;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.EconomyItems;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.EconomyMixedPayment;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ServerIdentityConfig;

import java.util.Map;

public final class EconomyInventoryService {
    private final EconomyTransactionService transactions;

    public EconomyInventoryService(EconomyTransactionService transactions) {
        this.transactions = transactions;
    }

    public TransactionResult deposit(ServerPlayerEntity player, int amount, String sourceSystem) {
        if (amount < 1 || countCurrency(player) < amount) {
            return panetina.elarion.addons.economy.model.TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INSUFFICIENT_FUNDS,
                    "Not enough physical " + identity().currencyPlural().toLowerCase(java.util.Locale.ROOT) + ".");
        }
        removeCurrency(player, amount);
        TransactionResult result = transactions.execute(
                EconomyTransactionType.DEPOSIT,
                EconomyAccount.PHYSICAL_CURRENCY,
                EconomyAccount.player(player.getUuid()),
                amount,
                player.getUuid(),
                "Physical currency deposit",
                sourceSystem,
                Map.of()
        );
        if (!result.successful()) giveCurrency(player, amount);
        return result;
    }

    public TransactionResult withdraw(ServerPlayerEntity player, int amount, String sourceSystem) {
        TransactionResult result = transactions.execute(
                EconomyTransactionType.WITHDRAW,
                EconomyAccount.player(player.getUuid()),
                EconomyAccount.PHYSICAL_CURRENCY,
                amount,
                player.getUuid(),
                "Physical currency withdrawal",
                sourceSystem,
                Map.of()
        );
        if (result.successful()) giveCurrency(player, amount);
        return result;
    }

    public int countCurrency(ServerPlayerEntity player) {
        return player.getInventory().count(EconomyItems.CURRENCY);
    }

    public EconomyMixedPayment payPhysicalThenBank(
            ServerPlayerEntity player,
            long amount,
            String reason,
            String sourceSystem
    ) {
        if (amount < 1 || amount > Integer.MAX_VALUE) {
            return EconomyMixedPayment.failure("Invalid payment amount.");
        }
        int physical = Math.min(countCurrency(player), (int) amount);
        long bank = amount - physical;
        if (transactions.balance(EconomyAccount.player(player.getUuid())) < bank) {
            return EconomyMixedPayment.failure("You do not have enough physical or banked currency.");
        }

        if (physical > 0) {
            removeCurrency(player, physical);
            TransactionResult physicalSink = transactions.execute(
                    EconomyTransactionType.SINK,
                    EconomyAccount.PHYSICAL_CURRENCY,
                    EconomyAccount.BURN,
                    physical,
                    player.getUuid(),
                    reason + " (physical)",
                    sourceSystem,
                    Map.of("paymentPart", "physical")
            );
            if (!physicalSink.successful()) {
                giveCurrency(player, physical);
                return EconomyMixedPayment.failure(physicalSink.message());
            }
        }

        if (bank > 0) {
            TransactionResult bankSink = transactions.sink(
                    EconomyAccount.player(player.getUuid()), bank, player.getUuid(),
                    reason + " (bank)", sourceSystem);
            if (!bankSink.successful()) {
                if (physical > 0) restorePhysical(player, physical, reason, sourceSystem);
                return EconomyMixedPayment.failure(bankSink.message());
            }
        }
        return EconomyMixedPayment.success(physical, bank);
    }

    public void refundMixedPayment(
            ServerPlayerEntity player,
            EconomyMixedPayment payment,
            String reason,
            String sourceSystem
    ) {
        if (payment == null || !payment.successful()) return;
        if (payment.physicalAmount() > 0) {
            restorePhysical(player, payment.physicalAmount(), reason, sourceSystem);
        }
        if (payment.bankAmount() > 0) {
            transactions.reward(
                    EconomyAccount.player(player.getUuid()), payment.bankAmount(), player.getUuid(),
                    reason + " (bank)", sourceSystem);
        }
    }

    private void restorePhysical(
            ServerPlayerEntity player,
            int amount,
            String reason,
            String sourceSystem
    ) {
        TransactionResult restored = transactions.execute(
                EconomyTransactionType.REWARD,
                EconomyAccount.MINT,
                EconomyAccount.PHYSICAL_CURRENCY,
                amount,
                player.getUuid(),
                reason + " (physical)",
                sourceSystem,
                Map.of("paymentPart", "physical")
        );
        if (restored.successful()) giveCurrency(player, amount);
    }

    private static void removeCurrency(ServerPlayerEntity player, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().size() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isOf(EconomyItems.CURRENCY)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.decrement(removed);
            remaining -= removed;
        }
        player.getInventory().markDirty();
    }

    private static void giveCurrency(ServerPlayerEntity player, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int count = Math.min(remaining, EconomyItems.CURRENCY.getMaxCount());
            ItemStack stack = new ItemStack(EconomyItems.CURRENCY, count);
            if (!player.getInventory().insertStack(stack) && !stack.isEmpty()) {
                player.dropItem(stack, false);
            }
            remaining -= count;
        }
    }

    private static ServerIdentityConfig identity() {
        try {
            return ElarionApi.get().serverIdentity();
        } catch (IllegalStateException exception) {
            return ServerIdentityConfig.defaults();
        }
    }
}
