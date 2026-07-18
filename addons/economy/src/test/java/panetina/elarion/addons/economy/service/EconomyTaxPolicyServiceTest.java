package panetina.elarion.addons.economy.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.economy.model.EconomyTaxQuote;
import panetina.elarion.addons.economy.storage.EconomyTaxPolicyStorage;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EconomyTaxPolicyServiceTest {
    @TempDir
    Path root;

    @Test
    void usesConfigFallbackAndKeepsRealmPoliciesIndependent() {
        EconomyTaxPolicyService service = service(250);
        EconomyTaxAuthority realm = EconomyTaxAuthority.realm("oak", "elarion:oak");
        EconomyTaxAuthority worldheart = EconomyTaxAuthority.worldheart("elarion:worldheart");

        assertEquals(250, service.rate(realm, EconomyTaxCategory.NPC_TRADE));
        service.setRate(realm, EconomyTaxCategory.NPC_TRADE, 750);

        assertEquals(750, service.rate(realm, EconomyTaxCategory.NPC_TRADE));
        assertEquals(250, service.rate(worldheart, EconomyTaxCategory.NPC_TRADE));
        assertEquals(0, service.rate(realm, EconomyTaxCategory.PORTAL_SERVICE));
    }

    @Test
    void persistsPoliciesAndRevisionAcrossRestart() {
        EconomyTaxAuthority realm = EconomyTaxAuthority.realm("oak", "elarion:oak");
        EconomyTaxPolicyService first = service(0);
        first.setRate(realm, EconomyTaxCategory.NPC_TRADE, 1_000);

        EconomyTaxPolicyService restarted = service(0);

        assertEquals(1_000, restarted.rate(realm, EconomyTaxCategory.NPC_TRADE));
        assertEquals(1L, restarted.revision());
    }

    @Test
    void quotesBoundedQuantitySubtotalTaxAndTotal() {
        EconomyTaxPolicyService service = service(0);
        EconomyTaxAuthority realm = EconomyTaxAuthority.realm("oak", "elarion:oak");
        service.setRate(realm, EconomyTaxCategory.NPC_TRADE, 1_000);

        EconomyTaxQuote quote = service.quote(realm, EconomyTaxCategory.NPC_TRADE, 25L, 4, 64);

        assertTrue(quote.valid());
        assertEquals(100L, quote.subtotal());
        assertEquals(10L, quote.tax());
        assertEquals(110L, quote.total());
        assertEquals(4, quote.quantity());
    }

    @Test
    void rejectsOutOfBoundsAndOverflowingQuotes() {
        EconomyTaxPolicyService service = service(0);
        EconomyTaxAuthority authority = EconomyTaxAuthority.worldheart("elarion:worldheart");

        assertFalse(service.quote(authority, EconomyTaxCategory.NPC_TRADE, 1L, 65, 64).valid());
        assertFalse(service.quote(authority, EconomyTaxCategory.NPC_TRADE, Long.MAX_VALUE, 2, 64).valid());
        assertThrows(IllegalArgumentException.class,
                () -> service.setRate(authority, EconomyTaxCategory.NPC_TRADE, 10_001));
    }

    @Test
    void malformedAndUnsupportedStateFailClosed() throws Exception {
        Files.createDirectories(root);
        Path file = root.resolve("tax-policies.json");
        Files.writeString(file, "{not json");
        assertThrows(IllegalStateException.class, () -> service(0));

        Files.writeString(file, "{\"schemaVersion\":99,\"revision\":0,\"rates\":[]}");
        assertThrows(IllegalStateException.class, () -> service(0));
    }

    private EconomyTaxPolicyService service(int fallbackRate) {
        EconomyTaxPolicyService service = new EconomyTaxPolicyService(
                new EconomyTaxPolicyStorage(root), () -> fallbackRate);
        service.bind(null);
        return service;
    }
}
