package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EconomyTaxDestinationResolverTest {
    private final EconomyTaxDestinationResolver resolver = new EconomyTaxDestinationResolver();

    @Test
    void realmAuthorityRoutesToItsRealmTreasury() {
        assertEquals(EconomyAccount.realm("oak"),
                resolver.resolve(EconomyTaxAuthority.realm("oak", "elarion:oak")));
    }

    @Test
    void everyNonRealmAuthorityRoutesToStableWorldheartTreasury() {
        assertEquals(EconomyAccount.WORLDHEART_TREASURY,
                resolver.resolve(EconomyTaxAuthority.worldheart("elarion:worldheart")));
        assertEquals(EconomyAccount.WORLDHEART_TREASURY,
                resolver.resolve(EconomyTaxAuthority.worldheart("minecraft:the_nether")));
        assertEquals(EconomyAccount.WORLDHEART_TREASURY,
                resolver.resolve(EconomyTaxAuthority.worldheart("minecraft:the_end")));
    }
}
