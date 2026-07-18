package panetina.elarion.addons.economy.model;

import java.util.Locale;
import java.util.Optional;

public enum EconomyBankMode {
    DEPOSIT,
    WITHDRAW;

    public static Optional<EconomyBankMode> fromRole(String role) {
        if (role == null || role.isBlank()) return Optional.empty();
        try {
            return Optional.of(EconomyBankMode.valueOf(role.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public String role() {
        return name().toLowerCase(Locale.ROOT);
    }
}
