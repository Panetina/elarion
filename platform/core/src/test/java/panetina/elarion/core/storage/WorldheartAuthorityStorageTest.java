package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.WorldheartAuthority;
import panetina.elarion.core.model.WorldheartAuthorityType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldheartAuthorityStorageTest {
    @TempDir
    Path root;

    @Test
    void missingStateDefaultsToHollowEmperorSystemAuthority() {
        WorldheartAuthority authority = storage().load(null);

        assertEquals(WorldheartAuthorityType.SYSTEM, authority.type());
        assertEquals("Hollow Emperor", authority.systemDisplayName());
        assertTrue(authority.playerRulerId().isEmpty());
    }

    @Test
    void playerAuthorityRoundTrips() {
        UUID ruler = UUID.randomUUID();
        storage().save(null, WorldheartAuthority.player(ruler, "Hollow Emperor", 123L));

        WorldheartAuthority loaded = storage().load(null);

        assertEquals(WorldheartAuthorityType.PLAYER, loaded.type());
        assertEquals(ruler, loaded.rulerId());
        assertEquals(123L, loaded.changedAt());
    }

    @Test
    void invalidPlayerStateFallsBackWithoutDeletingSource() throws Exception {
        Files.createDirectories(root);
        Path file = root.resolve("authority.json");
        String invalid = "{\"schemaVersion\":1,\"authorityType\":\"PLAYER\",\"playerId\":\"\"}";
        Files.writeString(file, invalid);

        WorldheartAuthority loaded = storage().load(null);

        assertEquals(WorldheartAuthorityType.SYSTEM, loaded.type());
        assertEquals(invalid, Files.readString(file));
    }

    private WorldheartAuthorityStorage storage() {
        return new WorldheartAuthorityStorage(LoggerFactory.getLogger("worldheart-test"), root);
    }
}
