package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionNumericInputTest {
    @Test
    void acceptsOnlyDigitsWithinBound() {
        ElarionNumericInput input = new ElarionNumericInput(3);

        assertTrue(input.type('1'));
        assertFalse(input.type('x'));
        assertTrue(input.type('2'));
        assertTrue(input.type('3'));
        assertFalse(input.type('4'));
        assertEquals("123", input.value());
    }

    @Test
    void backspaceAndClearAreDeterministic() {
        ElarionNumericInput input = new ElarionNumericInput(10);
        input.type('4');
        input.type('2');

        assertTrue(input.backspace());
        assertEquals("4", input.value());
        input.clear();
        assertTrue(input.empty());
        assertFalse(input.backspace());
    }
}
