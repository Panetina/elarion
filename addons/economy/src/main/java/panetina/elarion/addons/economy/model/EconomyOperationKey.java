package panetina.elarion.addons.economy.model;

import java.util.Locale;
import java.util.UUID;

public record EconomyOperationKey(String owner, UUID operationId) {
    public EconomyOperationKey {
        owner = owner == null ? "" : owner.trim().toLowerCase(Locale.ROOT);
        if (owner.isBlank() || owner.length() > 128 || !owner.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Operation owner must be a namespaced identifier");
        }
        if (operationId == null) throw new IllegalArgumentException("Operation ID is required");
    }

    public String value() {
        return owner + ":" + operationId;
    }
}
