package panetina.elarion.core.client;

import net.minecraft.util.Formatting;
import panetina.elarion.core.network.IdentitySyncPayload;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
                0xFFD19B42,
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
                0xFFD19B42,
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

    @Test
    void titleTextUsesConfiguredTitleColor() {
        ClientIdentity identity = new ClientIdentity(
                UUID.randomUUID(),
                "Matie",
                "",
                "",
                "",
                "Monarch",
                0xFFFFD36A,
                "",
                Formatting.GOLD,
                "Kingdom of Oak",
                "realm1",
                true,
                true);

        assertEquals("Monarch", identity.titleText().getString());
        assertNotNull(identity.titleText().getStyle().getColor());
        assertEquals(0xFFD36A, identity.titleText().getStyle().getColor().getRgb());
    }

    @Test
    void titlePreviewUsesNicknameInsteadOfAccountUsername() {
        UUID uuid = UUID.randomUUID();
        ClientIdentityCache.clear();
        ClientIdentityCache.update(new IdentitySyncPayload(
                uuid,
                "ElarionAdmin",
                "The First Flame",
                "",
                "",
                "Monarch",
                0xFFFFD36A,
                "",
                "gold",
                "Kingdom of Oak",
                "realm1",
                true,
                true));

        assertEquals("The First Flame", ElarionCollectionScreen.titlePreviewName(uuid, "ElarionAdmin"));
        assertEquals(0xFFFFAA00, ElarionCollectionScreen.titlePreviewNameColor(uuid, 0xFFFFFFFF));
    }

    @Test
    void titlePreviewNameColorFallsBackWithoutSynchronizedIdentity() {
        ClientIdentityCache.clear();

        assertEquals(0xFFABCDEF,
                ElarionCollectionScreen.titlePreviewNameColor(UUID.randomUUID(), 0xFFABCDEF));
    }
}
