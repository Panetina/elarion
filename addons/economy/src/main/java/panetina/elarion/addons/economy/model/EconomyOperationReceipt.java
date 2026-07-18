package panetina.elarion.addons.economy.model;

import java.util.UUID;
import java.util.Optional;

public record EconomyOperationReceipt(
        String owner,
        UUID operationId,
        String requestFingerprint,
        TransactionStatus status,
        String message,
        EconomyTransaction transaction,
        long createdAt
) {
    public static final String META_OWNER = "elarionOperationOwner";
    public static final String META_ID = "elarionOperationId";
    public static final String META_FINGERPRINT = "elarionOperationFingerprint";
    public static final String META_MESSAGE = "elarionOperationMessage";

    public EconomyOperationReceipt {
        EconomyOperationKey key = new EconomyOperationKey(owner, operationId);
        owner = key.owner();
        operationId = key.operationId();
        requestFingerprint = requestFingerprint == null ? "" : requestFingerprint.trim();
        if (requestFingerprint.isBlank()) throw new IllegalArgumentException("Request fingerprint is required");
        if (status == null) throw new IllegalArgumentException("Receipt status is required");
        message = message == null ? "" : message;
        if (transaction == null) throw new IllegalArgumentException("Receipt transaction is required");
        createdAt = createdAt <= 0L ? transaction.timestamp() : createdAt;
    }

    public EconomyOperationKey key() {
        return new EconomyOperationKey(owner, operationId);
    }

    public boolean matches(String fingerprint) {
        return requestFingerprint.equals(fingerprint);
    }

    public TransactionResult result() {
        return new TransactionResult(status, message, transaction);
    }

    public static Optional<EconomyOperationReceipt> fromTransaction(EconomyTransaction transaction) {
        if (transaction == null) return Optional.empty();
        String owner = transaction.metadata().getOrDefault(META_OWNER, "");
        String id = transaction.metadata().getOrDefault(META_ID, "");
        String fingerprint = transaction.metadata().getOrDefault(META_FINGERPRINT, "");
        if (owner.isBlank() || id.isBlank() || fingerprint.isBlank()) return Optional.empty();
        try {
            TransactionStatus status = transaction.success()
                    ? TransactionStatus.SUCCESS
                    : TransactionStatus.valueOf(transaction.failure());
            String message = transaction.metadata().getOrDefault(META_MESSAGE,
                    transaction.success() ? "Transaction completed." : transaction.failure());
            return Optional.of(new EconomyOperationReceipt(owner, UUID.fromString(id), fingerprint,
                    status, message, transaction, transaction.timestamp()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
