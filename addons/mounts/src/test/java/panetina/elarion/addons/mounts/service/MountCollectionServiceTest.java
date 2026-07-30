package panetina.elarion.addons.mounts.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MountCollectionServiceTest {
    @TempDir
    Path root;

    @Test
    void persistsUnlocksAndActiveMount() {
        UUID player = UUID.randomUUID();
        MountCollectionService service = new MountCollectionService(LoggerFactory.getLogger("test"));
        service.bindRoot(root);

        assertTrue(service.unlock(player, ElarionMountType.BEE));
        assertTrue(service.setActive(player, ElarionMountType.BEE));

        MountCollectionService loaded = new MountCollectionService(LoggerFactory.getLogger("test"));
        loaded.bindRoot(root);

        assertTrue(loaded.isUnlocked(player, ElarionMountType.BEE));
        assertEquals(ElarionMountType.BEE, loaded.activeMount(player).orElseThrow());
    }

    @Test
    void activeMountRequiresUnlock() {
        UUID player = UUID.randomUUID();
        MountCollectionService service = new MountCollectionService(LoggerFactory.getLogger("test"));
        service.bindRoot(root);

        assertFalse(service.setActive(player, ElarionMountType.WYVERN));
        assertTrue(service.activeMount(player).isEmpty());
    }

    @Test
    void revokingActiveMountFallsBackToRemainingUnlock() {
        UUID player = UUID.randomUUID();
        MountCollectionService service = new MountCollectionService(LoggerFactory.getLogger("test"));
        service.bindRoot(root);

        service.unlock(player, ElarionMountType.BEE);
        service.unlock(player, ElarionMountType.WYVERN);
        service.setActive(player, ElarionMountType.WYVERN);
        service.revoke(player, ElarionMountType.WYVERN);

        assertEquals(ElarionMountType.BEE, service.activeMount(player).orElseThrow());
    }

    @Test
    void recoverableNullPlayerRowsDoNotDiscardValidUnlocks() throws Exception {
        UUID player = UUID.randomUUID();
        UUID discarded = UUID.randomUUID();
        Files.writeString(root.resolve("collection.json"), """
                {
                  "players": {
                    "%s": {
                      "unlockedMounts": [" BEE ", null, ""],
                      "activeMountId": " BEE "
                    },
                    "%s": null
                  }
                }
                """.formatted(player, discarded));
        MountCollectionService service = new MountCollectionService(LoggerFactory.getLogger("test"));

        service.bindRoot(root);

        assertTrue(service.isUnlocked(player, ElarionMountType.BEE));
        assertEquals(ElarionMountType.BEE, service.activeMount(player).orElseThrow());
        assertTrue(service.unlocked(discarded).isEmpty());
        assertTrue(Files.exists(root.resolve("collection.json")));
        assertNoQuarantinedCollection();
    }

    @Test
    void explicitNullPlayerMapLoadsAsMutableEmptyState() throws Exception {
        UUID player = UUID.randomUUID();
        Files.writeString(root.resolve("collection.json"), """
                {"players": null}
                """);
        MountCollectionService service = new MountCollectionService(LoggerFactory.getLogger("test"));

        service.bindRoot(root);

        assertTrue(service.unlock(player, ElarionMountType.WYVERN));
        assertTrue(service.isUnlocked(player, ElarionMountType.WYVERN));
        assertTrue(Files.exists(root.resolve("collection.json")));
        assertNoQuarantinedCollection();
    }

    private void assertNoQuarantinedCollection() throws Exception {
        try (var files = Files.list(root)) {
            assertTrue(files.noneMatch(path ->
                    path.getFileName().toString().startsWith("collection.json.corrupt-")));
        }
    }
}
