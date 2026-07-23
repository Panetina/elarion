package panetina.elarion.addons.angling.delight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionAnglingDelightAddonTest {
    @Test
    void foundationIdentityIsStable() {
        assertEquals("elarion_angling_delight", ElarionAnglingDelightAddon.MOD_ID);
    }
}
