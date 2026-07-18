package panetina.elarion.addons.npcs.model;

public record NpcBankQuote(
        String mode,
        int amount,
        long balance,
        int physicalCurrency,
        int taxBasisPoints,
        long fee,
        long total,
        boolean valid,
        String message
) {
    public NpcBankQuote {
        mode = mode == null || mode.isBlank() ? "deposit" : mode;
        amount = Math.max(0, amount);
        physicalCurrency = Math.max(0, physicalCurrency);
        taxBasisPoints = Math.max(0, Math.min(10_000, taxBasisPoints));
        fee = Math.max(0L, fee);
        total = Math.max(0L, total);
        message = message == null ? "" : message;
    }
}
