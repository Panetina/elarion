package panetina.elarion.addons.mounts.service;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class MountAdminPanelProviderTest {
    @Test
    void providerExposesPlayerCollectionActionsWithoutRuntimeReset() {
        MountAdminPanelProvider provider = new MountAdminPanelProvider(
                new MountCollectionService(LoggerFactory.getLogger("mount-admin-panel-test")),
                new MountSessionService(LoggerFactory.getLogger("mount-admin-panel-test")));

        assertEquals("mounts", provider.id());
        assertEquals("Mounts", provider.title());
        assertFalse(provider.supportsRuntimeReset());
    }
}
