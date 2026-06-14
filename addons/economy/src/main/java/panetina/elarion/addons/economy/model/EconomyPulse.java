package panetina.elarion.addons.economy.model;

public record EconomyPulse(
        EconomyGovernorMode mode,
        EconomyHealth health,
        long walletCurrency,
        long treasuryCurrency,
        long trackedSupply,
        long createdInWindow,
        long destroyedInWindow,
        long transactionsInWindow,
        double faucetSinkRatio,
        double topTenWalletShare
) {
}
