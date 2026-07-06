package panetina.elarion.core.client;

import net.minecraft.util.Formatting;
import panetina.elarion.core.network.IdentitySyncPayload;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientIdentityTest {
    @Test
    void tabNameDoesNotPrefixRealmNameBeforeEveryPlayer() {
        ClientIdentity identity = new ClientIdentity(
                UUID.randomUUID(),
                "Matie",
                "",
                "",
                "",
                "",
                "",
                Formatting.GOLD,
                "Kingdom of Oak",
                "realm1",
                true,
                true);

        assertEquals("Matie", identity.tabName().getString());
        assertFalse(identity.tabName().getString().contains("Kingdom of Oak"));
    }

    @Test
    void tabVisibilityIsSeparateFromOverheadVisibility() {
        UUID uuid = UUID.randomUUID();
        ClientIdentityCache.clear();

        ClientIdentityCache.update(new IdentitySyncPayload(
                uuid,
                "Matie",
                "",
                "",
                "",
                "Citizen",
                "",
                "gold",
                "Kingdom of Oak",
                "realm1",
                false,
                true));

        assertTrue(ClientIdentityCache.isKnownTabHidden(uuid));
        assertFalse(ClientIdentityCache.isKnownHidden(uuid));
    }

    @Test
    void unknownTabEntriesStayVisibleUntilServerSendsIdentity() {
        ClientIdentityCache.clear();

        assertFalse(ClientIdentityCache.shouldHideTabEntry(UUID.randomUUID()));
    }
}
