package panetina.elarion.addons.economy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EconomyItemsTest {
    @Test
    void currencyStackConstantIsNineHundredNinetyNine() {
        assertEquals(999, EconomyItems.CURRENCY_MAX_STACK_SIZE);
    }
}
