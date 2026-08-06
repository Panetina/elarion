package panetina.elarion.addons.economy.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class EconomyContentApiTest {
    @Test
    void exposesTheStableEconomyContentIdentities() {
        assertEquals("elarion:currency", EconomyContentApi.currencyItemId().toString());
        assertEquals("elarion:economy", EconomyContentApi.itemGroupKey().getValue().toString());
    }
}
