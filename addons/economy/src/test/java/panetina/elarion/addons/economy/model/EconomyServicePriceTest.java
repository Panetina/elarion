package panetina.elarion.addons.economy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EconomyServicePriceTest {
    @Test
    void acceptsBoundedServicePrice() {
        EconomyServicePrice price = new EconomyServicePrice("ancient_gate.passage", 5, 1, 15);
        assertEquals(5, price.base());
    }

    @Test
    void rejectsBaseOutsideBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new EconomyServicePrice("invalid", 3, 4, 10));
    }
}
