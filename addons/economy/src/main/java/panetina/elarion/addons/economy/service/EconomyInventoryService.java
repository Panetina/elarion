package panetina.elarion.addons.economy.service;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.EconomyItems;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyBankMode;
import panetina.elarion.addons.economy.model.EconomyBankQuote;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.EconomyMixedPayment;
import panetina.elarion.addons.economy.model.EconomyOperationKey;
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
        long tax = transactions.calculateBankWithdrawalTax(amount);
        if (tax > 0L && transactions.balance(EconomyAccount.player(player.getUuid())) < amount + tax) {
            return panetina.elarion.addons.economy.model.TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INSUFFICIENT_FUNDS,
                    "Withdrawal requires " + identity().currencyAmount(amount + tax)
                            + " including " + identity().currencyAmount(tax) + " tax.");
        }
        TransactionResult taxResult = null;
        if (tax > 0L) {
            taxResult = transactions.execute(
                    EconomyTransactionType.TAX,
                    EconomyAccount.player(player.getUuid()),
                    EconomyAccount.BURN,
                    tax,
                    player.getUuid(),
                    "Bank withdrawal tax",
                    sourceSystem,
                    Map.of("taxBasisPoints", Integer.toString(transactions.config().bankWithdrawalTaxBasisPoints()))
            );
            if (!taxResult.successful()) return taxResult;
        }
        TransactionResult result = transactions.execute(
                EconomyTransactionType.WITHDRAW,
                EconomyAccount.player(player.getUuid()),
                EconomyAccount.PHYSICAL_CURRENCY,
                amount,
                player.getUuid(),
                "Physical currency withdrawal",
                sourceSystem,
                tax > 0L
                        ? Map.of("withdrawalTax", Long.toString(tax),
                        "taxBasisPoints", Integer.toString(transactions.config().bankWithdrawalTaxBasisPoints()))
                        : Map.of()
        );
        if (result.successful()) giveCurrency(player, amount);
        else if (taxResult != null) {
            transactions.reward(EconomyAccount.player(player.getUuid()), tax, player.getUuid(),
                    "Bank withdrawal tax refund", sourceSystem);
        }
        return result;
    }

    public EconomyBankQuote quoteBank(ServerPlayerEntity player, EconomyBankMode mode, int amount) {
        EconomyBankMode safeMode = mode == null ? EconomyBankMode.DEPOSIT : mode;
        long balance = player == null ? 0L : transactions.balance(EconomyAccount.player(player.getUuid()));
        int physical = player == null ? 0 : countCurrency(player);
        return quoteBank(safeMode, amount, balance, physical,
                transactions.config().bankWithdrawalTaxBasisPoints());
    }

    public static EconomyBankQuote quoteBank(
            EconomyBankMode mode,
            int amount,
            long balance,
            int physicalCurrency,
            int withdrawalTaxBasisPoints
    ) {
        EconomyBankMode safeMode = mode == null ? EconomyBankMode.DEPOSIT : mode;
        int safeAmount = Math.max(0, amount);
        long safeBalance = Math.max(0L, balance);
        int safePhysical = Math.max(0, physicalCurrency);
        int safeTaxBasisPoints = Math.max(0, Math.min(10_000, withdrawalTaxBasisPoints));
        if (safeAmount < 1) {
            return new EconomyBankQuote(safeMode, safeAmount, safeBalance, safePhysical,
                    safeTaxBasisPoints, 0L, 0L, false, "Enter an amount.");
        }
        if (safeMode == EconomyBankMode.DEPOSIT) {
            boolean valid = safePhysical >= safeAmount;
            return new EconomyBankQuote(safeMode, safeAmount, safeBalance, safePhysical,
                    0, 0L, safeAmount, valid,
                    valid ? "" : "Not enough carried physical currency.");
        }
        long fee = EconomyTransactionService.calculateBasisPointAmount(safeAmount, safeTaxBasisPoints);
        long total;
        try {
            total = Math.addExact(safeAmount, fee);
        } catch (ArithmeticException exception) {
            return new EconomyBankQuote(safeMode, safeAmount, safeBalance, safePhysical,
                    safeTaxBasisPoints, fee, Long.MAX_VALUE, false, "Amount is too large.");
        }
        boolean valid = safeBalance >= total;
        return new EconomyBankQuote(safeMode, safeAmount, safeBalance, safePhysical,
                safeTaxBasisPoints, fee, total, valid,
                valid ? "" : "Not enough banked currency.");
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

    public EconomyMixedPayment payPhysicalOnly(
            ServerPlayerEntity player,
            long amount,
            String reason,
            String sourceSystem
    ) {
        if (amount < 1 || amount > Integer.MAX_VALUE) {
            return EconomyMixedPayment.failure("Invalid payment amount.");
        }
        int physical = (int) amount;
        if (countCurrency(player) < physical) {
            return EconomyMixedPayment.failure("You need "
                    + identity().currencyAmount(amount) + " in carried physical currency.");
        }
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
        return EconomyMixedPayment.success(physical, 0L);
    }

    public TransactionResult payPhysicalOnlyOnce(
            ServerPlayerEntity player,
            EconomyOperationKey operation,
            EconomyAccount destination,
            long amount,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        if (operation == null) {
            return TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.IDEMPOTENCY_CONFLICT,
                    "Operation key is required.");
        }
        var existing = transactions.receipt(operation);
        if (existing.isPresent()) return existing.get().result();
        if (amount < 1 || amount > Integer.MAX_VALUE) {
            return TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INVALID_AMOUNT,
                    "Invalid payment amount.");
        }
        int physical = (int) amount;
        if (countCurrency(player) < physical) {
            return TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INSUFFICIENT_FUNDS,
                    "You need " + identity().currencyAmount(amount) + " in carried physical currency.");
        }
        removeCurrency(player, physical);
        TransactionResult result = transactions.executeOnce(
                operation,
                EconomyTransactionType.PUBLIC_REVENUE,
                EconomyAccount.PHYSICAL_CURRENCY,
                destination,
                amount,
                player.getUuid(),
                reason,
                sourceSystem,
                metadata == null ? Map.of() : metadata
        );
        if (!result.successful()) giveCurrency(player, physical);
        return result;
    }

    public TransactionResult payPhysicalRewardOnce(
            ServerPlayerEntity player,
            EconomyOperationKey operation,
            long amount,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        if (operation == null) {
            return TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.IDEMPOTENCY_CONFLICT,
                    "Operation key is required.");
        }
        var existing = transactions.receipt(operation);
        if (existing.isPresent()) return existing.get().result();
        if (amount < 1 || amount > Integer.MAX_VALUE) {
            return TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INVALID_AMOUNT,
                    "Invalid payout amount.");
        }
        int physical = (int) amount;
        if (!canAcceptCurrency(player, physical)) {
            return TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INSUFFICIENT_FUNDS,
                    "Not enough inventory space for " + identity().currencyAmount(amount) + ".");
        }
        TransactionResult result = transactions.executeOnce(
                operation,
                EconomyTransactionType.REWARD,
                EconomyAccount.MINT,
                EconomyAccount.PHYSICAL_CURRENCY,
                amount,
                player.getUuid(),
                reason,
                sourceSystem,
                metadata == null ? Map.of() : metadata
        );
        if (result.successful()) giveCurrency(player, physical);
        return result;
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

    private static boolean canAcceptCurrency(ServerPlayerEntity player, int amount) {
        return currencyCapacity(player.getInventory().main) >= amount;
    }

    static int currencyCapacity(Iterable<ItemStack> stacks) {
        int emptySlots = 0;
        int partialCurrencySpace = 0;
        int max = EconomyItems.CURRENCY.getMaxCount();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                emptySlots++;
            } else if (stack.isOf(EconomyItems.CURRENCY)) {
                partialCurrencySpace += Math.max(0, max - stack.getCount());
            }
        }
        return currencyCapacity(emptySlots, partialCurrencySpace, max);
    }

    static int currencyCapacity(int emptySlots, int partialCurrencySpace, int maxStackSize) {
        if (emptySlots < 0 || partialCurrencySpace < 0 || maxStackSize < 1) return 0;
        long capacity = (long) emptySlots * maxStackSize + partialCurrencySpace;
        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }

    private static ServerIdentityConfig identity() {
        try {
            return ElarionApi.get().serverIdentity();
        } catch (IllegalStateException exception) {
            return ServerIdentityConfig.defaults();
        }
    }
}
