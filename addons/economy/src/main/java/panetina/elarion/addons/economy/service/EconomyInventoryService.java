package panetina.elarion.addons.economy.service;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.EconomyItems;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionResult;

import java.util.Map;

public final class EconomyInventoryService {
    private final EconomyTransactionService transactions;

    public EconomyInventoryService(EconomyTransactionService transactions) {
        this.transactions = transactions;
    }

    public TransactionResult deposit(ServerPlayerEntity player, int amount, String sourceSystem) {
        if (amount < 1 || countSigils(player) < amount) {
            return panetina.elarion.addons.economy.model.TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INSUFFICIENT_FUNDS,
                    "Not enough physical sigils.");
        }
        removeSigils(player, amount);
        TransactionResult result = transactions.execute(
                EconomyTransactionType.DEPOSIT,
                EconomyAccount.PHYSICAL_SIGIL,
                EconomyAccount.player(player.getUuid()),
                amount,
                player.getUuid(),
                "Physical sigil deposit",
                sourceSystem,
                Map.of()
        );
        if (!result.successful()) giveSigils(player, amount);
        return result;
    }

    public TransactionResult withdraw(ServerPlayerEntity player, int amount, String sourceSystem) {
        TransactionResult result = transactions.execute(
                EconomyTransactionType.WITHDRAW,
                EconomyAccount.player(player.getUuid()),
                EconomyAccount.PHYSICAL_SIGIL,
                amount,
                player.getUuid(),
                "Physical sigil withdrawal",
                sourceSystem,
                Map.of()
        );
        if (result.successful()) giveSigils(player, amount);
        return result;
    }

    public int countSigils(ServerPlayerEntity player) {
        return player.getInventory().count(EconomyItems.SIGIL);
    }

    private static void removeSigils(ServerPlayerEntity player, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().size() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isOf(EconomyItems.SIGIL)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.decrement(removed);
            remaining -= removed;
        }
        player.getInventory().markDirty();
    }

    private static void giveSigils(ServerPlayerEntity player, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int count = Math.min(remaining, EconomyItems.SIGIL.getMaxCount());
            ItemStack stack = new ItemStack(EconomyItems.SIGIL, count);
            if (!player.getInventory().insertStack(stack) && !stack.isEmpty()) {
                player.dropItem(stack, false);
            }
            remaining -= count;
        }
    }
}
