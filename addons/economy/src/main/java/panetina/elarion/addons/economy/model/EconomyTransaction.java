package panetina.elarion.addons.economy.model;

import java.util.Map;
import java.util.UUID;

public record EconomyTransaction(
        long sequence,
        UUID id,
        long timestamp,
        EconomyTransactionType type,
        EconomyAccount fromAccount,
        EconomyAccount toAccount,
        long amount,
        UUID actor,
        String reason,
        String sourceSystem,
        boolean success,
        String failure,
        long fromBalanceBefore,
        long fromBalanceAfter,
        long toBalanceBefore,
        long toBalanceAfter,
        Map<String, String> metadata
) {
    public EconomyTransaction {
        if (sequence < 1) throw new IllegalArgumentException("Transaction sequence must be positive");
        id = id == null ? UUID.randomUUID() : id;
        timestamp = timestamp <= 0 ? System.currentTimeMillis() : timestamp;
        if (type == null) throw new IllegalArgumentException("Transaction type is required");
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Transaction accounts are required");
        }
        if (fromAccount.equals(toAccount)) throw new IllegalArgumentException("Transaction accounts must differ");
        if (success && amount < 1) throw new IllegalArgumentException("Successful transaction amount must be positive");
        reason = reason == null || reason.isBlank() ? "unspecified" : reason.trim();
        sourceSystem = sourceSystem == null || sourceSystem.isBlank() ? "unknown" : sourceSystem.trim();
        failure = failure == null ? "" : failure.trim();
        if (success) failure = "";
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
