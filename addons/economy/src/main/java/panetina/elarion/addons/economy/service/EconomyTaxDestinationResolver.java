package panetina.elarion.addons.economy.service;

import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxAuthorityKind;

public final class EconomyTaxDestinationResolver {
    public EconomyAccount resolve(EconomyTaxAuthority authority) {
        if (authority == null) throw new IllegalArgumentException("Tax authority is required");
        return authority.kind() == EconomyTaxAuthorityKind.REALM
                ? EconomyAccount.realm(authority.id())
                : EconomyAccount.WORLDHEART_TREASURY;
    }
}
