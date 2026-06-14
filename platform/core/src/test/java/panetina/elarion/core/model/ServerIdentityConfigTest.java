package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ServerIdentityConfigTest {
    @Test
    void formatsCurrencyAmountsAndRealmLabels() {
        ServerIdentityConfig identity = ServerIdentityConfig.defaults();

        assertEquals("1 sigil", identity.currencyAmount(1));
        assertEquals("12 sigils", identity.currencyAmount(12));
        assertEquals("Realm oak", identity.realmLabel("oak"));
        assertEquals("Realm unknown", identity.realmLabel(""));
    }

    @Test
    void replacesIdentityPlaceholdersWithCaseVariants() {
        ServerIdentityConfig identity = ServerIdentityConfig.defaults();

        assertEquals(
                "Elarion | WORLDHEART | sigils | Realm | SHRINE OF FOUNDATION",
                identity.replace(
                        "%server% | %capital_upper% | %currency_plural_lower% | "
                                + "%realm_term_title% | %shrine_of_foundation_upper%"));
    }
}
