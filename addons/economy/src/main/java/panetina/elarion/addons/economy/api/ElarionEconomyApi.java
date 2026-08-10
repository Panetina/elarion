package panetina.elarion.addons.economy.api;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyBankMode;
import panetina.elarion.addons.economy.model.EconomyBankQuote;
import panetina.elarion.addons.economy.model.EconomyPulse;
import panetina.elarion.addons.economy.model.EconomyMixedPayment;
import panetina.elarion.addons.economy.model.EconomyOperationKey;
import panetina.elarion.addons.economy.model.EconomyOperationReceipt;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.economy.model.EconomyTaxQuote;
import panetina.elarion.addons.economy.model.EconomyTaxPolicySnapshot;
import panetina.elarion.addons.economy.model.EconomyTradePriceQuote;
import panetina.elarion.addons.economy.model.EconomyTradePriceRequest;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.service.EconomyGovernorService;
import panetina.elarion.addons.economy.service.EconomyInventoryService;
import panetina.elarion.addons.economy.service.EconomyPricingService;
import panetina.elarion.addons.economy.service.EconomyTransactionService;
import panetina.elarion.addons.economy.service.EconomyTaxPolicyService;
import panetina.elarion.addons.economy.service.EconomyTaxDestinationResolver;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

public final class ElarionEconomyApi {
    private static ElarionEconomyApi instance;
    private final EconomyTransactionService transactions;
    private final EconomyInventoryService inventory;
    private final EconomyGovernorService governor;
    private final EconomyPricingService pricing;
    private final EconomyTaxPolicyService taxPolicies;
    private final EconomyTaxDestinationResolver taxDestinations = new EconomyTaxDestinationResolver();

    public ElarionEconomyApi(
            EconomyTransactionService transactions,
            EconomyInventoryService inventory,
            EconomyGovernorService governor,
            EconomyPricingService pricing,
            EconomyTaxPolicyService taxPolicies
    ) {
        if (instance != null) throw new IllegalStateException("ElarionEconomyApi is already initialized");
        this.transactions = transactions;
        this.inventory = inventory;
        this.governor = governor;
        this.pricing = pricing;
        this.taxPolicies = taxPolicies;
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

    public long worldheartTreasury() {
        return transactions.balance(EconomyAccount.WORLDHEART_TREASURY);
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

    public TransactionResult transactOnce(
            EconomyOperationKey operation,
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        return transactions.executeOnce(operation, type, from, to, amount, actor,
                reason, sourceSystem, metadata);
    }

    public Optional<EconomyOperationReceipt> operationReceipt(EconomyOperationKey operation) {
        return transactions.receipt(operation);
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

    public TransactionResult rewardOnce(
            EconomyOperationKey operation,
            EconomyAccount destination,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        return transactions.rewardOnce(operation, destination, amount, actor, reason, sourceSystem, metadata);
    }

    public TransactionResult payPlayerBalanceRewardOnce(
            UUID playerId,
            EconomyOperationKey operation,
            long amount,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        if (playerId == null) {
            return TransactionResult.failure(
                    panetina.elarion.addons.economy.model.TransactionStatus.INVALID_ACCOUNT,
                    "Player ID is required.");
        }
        return transactions.rewardOnce(operation, EconomyAccount.player(playerId), amount, playerId,
                reason, sourceSystem, metadata);
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

    public EconomyBankQuote quoteBank(ServerPlayerEntity player, EconomyBankMode mode, int amount) {
        return inventory.quoteBank(player, mode, amount);
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

    public EconomyMixedPayment payPhysicalOnly(
            ServerPlayerEntity player,
            long amount,
            String reason,
            String sourceSystem
    ) {
        return inventory.payPhysicalOnly(player, amount, reason, sourceSystem);
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
        return inventory.payPhysicalOnlyOnce(player, operation, destination, amount,
                reason, sourceSystem, metadata);
    }

    public TransactionResult payPhysicalRewardOnce(
            ServerPlayerEntity player,
            EconomyOperationKey operation,
            long amount,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        return inventory.payPhysicalRewardOnce(player, operation, amount, reason, sourceSystem, metadata);
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

    public EconomyTaxQuote quoteTax(
            EconomyTaxAuthority authority,
            EconomyTaxCategory category,
            long unitPrice,
            int quantity,
            int maxQuantity
    ) {
        return taxPolicies.quote(authority, category, unitPrice, quantity, maxQuantity);
    }

    public EconomyTradePriceQuote quoteTradePrice(EconomyTradePriceRequest request) {
        return pricing.quoteTradePrice(request, taxPolicies);
    }

    public int taxRate(EconomyTaxAuthority authority, EconomyTaxCategory category) {
        return taxPolicies.rate(authority, category);
    }

    public void setTaxRate(EconomyTaxAuthority authority, EconomyTaxCategory category, int basisPoints) {
        taxPolicies.setRate(authority, category, basisPoints);
    }

    public EconomyTaxPolicySnapshot taxPolicy(EconomyTaxAuthority authority) {
        return taxPolicies.snapshot(authority);
    }

    public void setTaxRates(EconomyTaxAuthority authority, long expectedRevision,
                            Map<EconomyTaxCategory, Integer> rates) {
        taxPolicies.setRates(authority, expectedRevision, rates);
    }

    public EconomyAccount taxDestination(EconomyTaxAuthority authority) {
        return taxDestinations.resolve(authority);
    }
}
