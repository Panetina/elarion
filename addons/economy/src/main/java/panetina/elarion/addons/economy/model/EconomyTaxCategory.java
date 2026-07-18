package panetina.elarion.addons.economy.model;

import java.util.Locale;

public enum EconomyTaxCategory {
    NPC_TRADE,
    PORTAL_SERVICE,
    MARKETPLACE,
    GENERAL_SERVICE;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static EconomyTaxCategory fromId(String id) {
        return valueOf(id.trim().toUpperCase(Locale.ROOT));
    }
}
