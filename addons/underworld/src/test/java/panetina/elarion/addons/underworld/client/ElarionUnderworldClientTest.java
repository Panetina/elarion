package panetina.elarion.addons.underworld.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionUnderworldClientTest {
    @Test
    void precipitationSuppressionIsLimitedToTheUnderworld() {
        assertTrue(ElarionUnderworldClient.isUnderworld("elarion:underworld"));
        assertFalse(ElarionUnderworldClient.isUnderworld("minecraft:overworld"));
        assertFalse(ElarionUnderworldClient.isUnderworld("elarion:worldheart"));
    }
}
