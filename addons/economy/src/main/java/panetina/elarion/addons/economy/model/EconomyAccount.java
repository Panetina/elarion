package panetina.elarion.addons.economy.model;

import java.util.Locale;
import java.util.UUID;

public record EconomyAccount(EconomyAccountType type, String id) {
    public static final EconomyAccount MINT = system("mint");
    public static final EconomyAccount BURN = system("burn");
    public static final EconomyAccount PHYSICAL_CURRENCY = system("physical_currency");

    public EconomyAccount {
        if (type == null) throw new IllegalArgumentException("Account type is required");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Account ID is required");
        id = id.trim().toLowerCase(Locale.ROOT);
    }

    public static EconomyAccount player(UUID playerId) {
        return new EconomyAccount(EconomyAccountType.PLAYER, playerId.toString());
    }

    public static EconomyAccount realm(String realmId) {
        return new EconomyAccount(EconomyAccountType.REALM, realmId);
    }

    public static EconomyAccount system(String id) {
        return new EconomyAccount(EconomyAccountType.SYSTEM, id);
    }

    public String key() {
        return type.name().toLowerCase(Locale.ROOT) + ":" + id;
    }
}
