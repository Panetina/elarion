package panetina.elarion.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElarionCoreClientTest {
    @Test
    void characterMenuUsesOnlyTheCurrentHiddenCommand() {
        assertTrue(ElarionCoreClient.opensCharacterMenuCommand("charactermenu"));
        assertTrue(ElarionCoreClient.opensCharacterMenuCommand("/charactermenu"));
        assertFalse(ElarionCoreClient.opensCharacterMenuCommand("ledger"));
        assertFalse(ElarionCoreClient.opensCharacterMenuCommand("/ledger"));
        assertFalse(ElarionCoreClient.opensCharacterMenuCommand("collection"));
        assertFalse(ElarionCoreClient.opensCharacterMenuCommand("/collection"));
    }
}
