package panetina.elarion.addons.economy.model;

public record EconomyPulse(
        EconomyGovernorMode mode,
        EconomyHealth health,
        long walletSigils,
        long treasurySigils,
        long trackedSupply,
        long createdInWindow,
        long destroyedInWindow,
        long transactionsInWindow,
        double faucetSinkRatio,
        double topTenWalletShare
) {
}
