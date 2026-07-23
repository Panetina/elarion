package panetina.elarion.addons.angling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ElarionAnglingAddonTest {
    @Test
    void foundationIdentityAndSourceRevisionAreStable() {
        assertEquals("elarion_angling", ElarionAnglingAddon.MOD_ID);
        assertEquals("016161dfc2d556d20fa641cd275e18c539256d4d",
                ElarionAnglingAddon.SOURCE_REVISION);
    }
}
