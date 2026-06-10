package panetina.elarion.addons.economy.service;

import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyHealth;
import panetina.elarion.addons.economy.model.EconomyPulse;
import panetina.elarion.addons.economy.model.EconomyTransaction;

import java.time.Duration;
import java.util.List;

public final class EconomyGovernorService {
    private final EconomyTransactionService transactions;

    public EconomyGovernorService(EconomyTransactionService transactions) {
        this.transactions = transactions;
    }

    public EconomyPulse pulse() {
        int limit = transactions.config().governorMaxTransactions();
        long cutoff = System.currentTimeMillis()
                - Duration.ofDays(transactions.config().governorWindowDays()).toMillis();
        List<EconomyTransaction> recent = transactions.recent(
                transaction -> transaction.success() && transaction.timestamp() >= cutoff, limit);
        long created = recent.stream()
                .filter(transaction -> transaction.fromAccount().equals(EconomyAccount.MINT))
                .mapToLong(EconomyTransaction::amount)
                .sum();
        long destroyed = recent.stream()
                .filter(transaction -> transaction.toAccount().equals(EconomyAccount.BURN))
                .mapToLong(EconomyTransaction::amount)
                .sum();
        long wallets = transactions.walletTotal();
        long treasuries = transactions.treasuryTotal();
        long supply;
        try {
            supply = Math.addExact(wallets, treasuries);
        } catch (ArithmeticException exception) {
            supply = Long.MAX_VALUE;
        }
        double ratio = destroyed == 0L ? (created == 0L ? 1.0D : Double.POSITIVE_INFINITY)
                : (double) created / (double) destroyed;
        double concentration = topTenShare(transactions.walletBalancesDescending(), wallets);
        EconomyHealth health = classify(
                recent.size(), transactions.walletAccountCount(), supply, ratio, concentration);
        return new EconomyPulse(transactions.config().governorMode(), health, wallets, treasuries,
                supply, created, destroyed, recent.size(), ratio, concentration);
    }

    private static EconomyHealth classify(
            int transactionCount,
            int walletAccounts,
            long supply,
            double ratio,
            double concentration
    ) {
        if (transactionCount == 0 && supply > 0) return EconomyHealth.STAGNANT;
        if (walletAccounts > 10 && concentration > 0.65D) return EconomyHealth.CONCENTRATED;
        if (ratio < 0.75D) return EconomyHealth.DEFLATIONARY;
        if (ratio <= 1.05D) return EconomyHealth.HEALTHY;
        if (ratio <= 1.15D) return EconomyHealth.WARM;
        if (ratio <= 1.30D) return EconomyHealth.INFLATIONARY;
        return EconomyHealth.OVERHEATED;
    }

    private static double topTenShare(List<Long> balances, long total) {
        if (total <= 0L) return 0.0D;
        long top = balances.stream().limit(10).mapToLong(Long::longValue).sum();
        return Math.min(1.0D, (double) top / (double) total);
    }
}
