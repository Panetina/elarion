package panetina.elarion.addons.atlas;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.atlas.api.ElarionAtlasApi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionAtlasAddonTest {
    @Test
    void commonShellInitializerDoesNotRequireCoreServices() {
        assertDoesNotThrow(() -> new ElarionAtlasAddon().initialize(null));
        assertEquals("elarion_atlas", ElarionAtlasAddon.MOD_ID);
    }

    @Test
    void publicShellApiIsStableAndExposesNoImplementedCapability() {
        ElarionAtlasApi first = ElarionAtlasApi.get();

        assertNotNull(first);
        assertSame(first, ElarionAtlasApi.get());
        assertTrue(ElarionAtlasApi.implementedCapabilities().isEmpty());
    }
}
