package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IdentitySyncServiceTest {
    @Test
    void opCanSeeOtherRealmTabEntries() {
        assertTrue(IdentitySyncService.tabVisible(
                UUID.randomUUID(), "realm1", true, UUID.randomUUID(), "realm2"));
    }

    @Test
    void ordinaryPlayersDoNotSeeOtherRealmTabEntries() {
        assertFalse(IdentitySyncService.tabVisible(
                UUID.randomUUID(), "realm1", false, UUID.randomUUID(), "realm2"));
    }

    @Test
    void playerAlwaysSeesSelfAndUnassignedEntriesStayVisible() {
        UUID uuid = UUID.randomUUID();

        assertTrue(IdentitySyncService.tabVisible(uuid, "realm1", false, uuid, "realm2"));
        assertTrue(IdentitySyncService.tabVisible(
                UUID.randomUUID(), "", false, UUID.randomUUID(), "realm2"));
        assertTrue(IdentitySyncService.tabVisible(
                UUID.randomUUID(), "realm1", false, UUID.randomUUID(), ""));
    }
}
