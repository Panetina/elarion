package panetina.elarion.addons.economy.api;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyPulse;
import panetina.elarion.addons.economy.model.EconomyMixedPayment;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.service.EconomyGovernorService;
import panetina.elarion.addons.economy.service.EconomyInventoryService;
import panetina.elarion.addons.economy.service.EconomyPricingService;
import panetina.elarion.addons.economy.service.EconomyTransactionService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ElarionEconomyApi {
    private static ElarionEconomyApi instance;
    private final EconomyTransactionService transactions;
    private final EconomyInventoryService inventory;
    private final EconomyGovernorService governor;
    private final EconomyPricingService pricing;

    public ElarionEconomyApi(
            EconomyTransactionService transactions,
            EconomyInventoryService inventory,
            EconomyGovernorService governor,
            EconomyPricingService pricing
    ) {
        if (instance != null) throw new IllegalStateException("ElarionEconomyApi is already initialized");
        this.transactions = transactions;
        this.inventory = inventory;
        this.governor = governor;
        this.pricing = pricing;
        instance = this;
    }

    public static ElarionEconomyApi get() {
        if (instance == null) throw new IllegalStateException("Elarion Economy has not initialized yet");
        return instance;
    }

    public long wallet(UUID playerId) {
        return transactions.balance(EconomyAccount.player(playerId));
    }

    public long treasury(String realmId) {
        return transactions.balance(EconomyAccount.realm(realmId));
    }

    public TransactionResult transact(
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        return transactions.execute(type, from, to, amount, actor, reason, sourceSystem, metadata);
    }

    public TransactionResult reward(
            EconomyAccount destination,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem
    ) {
        return transactions.reward(destination, amount, actor, reason, sourceSystem);
    }

    public TransactionResult sink(
            EconomyAccount source,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem
    ) {
        return transactions.sink(source, amount, actor, reason, sourceSystem);
    }

    public TransactionResult deposit(ServerPlayerEntity player, int amount, String sourceSystem) {
        return inventory.deposit(player, amount, sourceSystem);
    }

    public TransactionResult withdraw(ServerPlayerEntity player, int amount, String sourceSystem) {
        return inventory.withdraw(player, amount, sourceSystem);
    }

    public int physicalCurrency(ServerPlayerEntity player) {
        return inventory.countCurrency(player);
    }

    public EconomyMixedPayment payPhysicalThenBank(
            ServerPlayerEntity player,
            long amount,
            String reason,
            String sourceSystem
    ) {
        return inventory.payPhysicalThenBank(player, amount, reason, sourceSystem);
    }

    public void refundMixedPayment(
            ServerPlayerEntity player,
            EconomyMixedPayment payment,
            String reason,
            String sourceSystem
    ) {
        inventory.refundMixedPayment(player, payment, reason, sourceSystem);
    }

    public List<EconomyTransaction> recent(EconomyAccount account, int limit) {
        return transactions.recentFor(account, limit);
    }

    public EconomyPulse pulse() {
        return governor.pulse();
    }

    public long servicePrice(String priceId) {
        return pricing.currentPrice(priceId);
    }

    public EconomyPricingService pricing() {
        return pricing;
    }
}
