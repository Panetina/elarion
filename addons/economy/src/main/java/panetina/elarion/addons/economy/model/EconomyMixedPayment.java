package panetina.elarion.addons.economy.model;

public record EconomyMixedPayment(
        boolean successful,
        String message,
        int physicalAmount,
        long bankAmount
) {
    public static EconomyMixedPayment failure(String message) {
        return new EconomyMixedPayment(false, message, 0, 0);
    }

    public static EconomyMixedPayment success(int physicalAmount, long bankAmount) {
        return new EconomyMixedPayment(true, "Payment completed.", physicalAmount, bankAmount);
    }
}
