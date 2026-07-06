package panetina.elarion.core.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionTextInputTest {
    @Test
    void singleLineInputFiltersNewlinesAndRespectsLimit() {
        ElarionTextInput input = new ElarionTextInput(5, false);

        assertTrue(input.append("abc\ndef"));

        assertEquals("abcde", input.text());
        assertEquals(5, input.length());
        assertFalse(input.append("x"));
        assertTrue(input.backspace());
        assertEquals("abcd", input.text());
    }

    @Test
    void multilineInputKeepsNewlinesAndClampsScroll() {
        ElarionTextInput input = new ElarionTextInput(20, true);

        assertTrue(input.append("one\r\ntwo\nthree"));
        input.scrollLine(10);
        input.clampScroll(2);
        assertEquals(2, input.scrollLine());
        input.scrollBy(-10, 2);
        assertEquals(0, input.scrollLine());

        assertEquals("one\ntwo\nthree", input.text());
    }
}
