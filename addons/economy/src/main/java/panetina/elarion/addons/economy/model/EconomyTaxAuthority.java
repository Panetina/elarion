package panetina.elarion.addons.economy.model;

import java.util.Locale;

public record EconomyTaxAuthority(EconomyTaxAuthorityKind kind, String id, String sourceWorldId) {
    public EconomyTaxAuthority {
        if (kind == null) throw new IllegalArgumentException("Tax authority kind is required");
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Tax authority ID is required");
        id = id.trim().toLowerCase(Locale.ROOT);
        sourceWorldId = sourceWorldId == null ? "" : sourceWorldId.trim().toLowerCase(Locale.ROOT);
    }

    public static EconomyTaxAuthority realm(String realmId, String sourceWorldId) {
        return new EconomyTaxAuthority(EconomyTaxAuthorityKind.REALM, realmId, sourceWorldId);
    }

    public static EconomyTaxAuthority worldheart(String sourceWorldId) {
        return new EconomyTaxAuthority(EconomyTaxAuthorityKind.WORLDHEART, "worldheart", sourceWorldId);
    }

    public String key() {
        return kind.name().toLowerCase(Locale.ROOT) + ":" + id;
    }
}
