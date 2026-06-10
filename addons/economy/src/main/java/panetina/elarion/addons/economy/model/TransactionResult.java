package panetina.elarion.addons.economy.model;

public record TransactionResult(
        TransactionStatus status,
        String message,
        EconomyTransaction transaction
) {
    public static TransactionResult success(EconomyTransaction transaction) {
        return new TransactionResult(TransactionStatus.SUCCESS, "Transaction completed.", transaction);
    }

    public static TransactionResult failure(TransactionStatus status, String message) {
        return new TransactionResult(status, message, null);
    }

    public boolean successful() {
        return status == TransactionStatus.SUCCESS;
    }
}
