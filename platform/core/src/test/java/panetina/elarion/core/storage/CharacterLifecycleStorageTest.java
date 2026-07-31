package panetina.elarion.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.model.CharacterLifecycleRecord;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CharacterLifecycleStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsCanonicalLifecycleState() {
        Path file = tempDir.resolve("state.json");
        CharacterLifecycleStorage storage = new CharacterLifecycleStorage(LoggerFactory.getLogger("test"));
        UUID accountId = UUID.randomUUID();
        CharacterLifecycleRecord account = CharacterLifecycleRecord.migration(accountId);
        CharacterLifecycleState state = new CharacterLifecycleState();
        state.accounts.put(accountId.toString(), account);
        state.reservedNames.add("fallen_name");

        storage.save(file, state);
        CharacterLifecycleState loaded = storage.load(file);

        assertEquals(account.accountId, loaded.accounts.get(accountId.toString()).accountId);
        assertEquals(new LinkedHashSet<>(state.reservedNames), loaded.reservedNames);
    }

    @Test
    void recoverableNullRowsAndFieldsDoNotDiscardValidLifecycleState() throws Exception {
        Path file = tempDir.resolve("state.json");
        UUID accountId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        Files.writeString(file, """
                {
                  "accounts": {
                    "%s": {
                      "accountId": null,
                      "activeCharacterId": "%s",
                      "biography": null,
                      "nonce": null,
                      "resetReason": null,
                      "completedResetSteps": ["elarion_core", null, ""]
                    },
                    "invalid-account": {},
                    "broken": null
                  },
                  "archives": {
                    "%s": {
                      "characterId": null,
                      "accountId": "%s",
                      "displayName": null,
                      "unlockedTitleIds": ["title_one", null, ""],
                      "metadata": {"valid": "value", "broken": null}
                    },
                    "broken": null
                  },
                  "reservedNames": ["fallen_name", null, ""]
                }
                """.formatted(accountId.toString().toUpperCase(), characterId, characterId, accountId));
        CharacterLifecycleStorage storage = new CharacterLifecycleStorage(LoggerFactory.getLogger("test"));

        CharacterLifecycleState loaded = storage.load(file);

        assertEquals(1, loaded.accounts.size());
        CharacterLifecycleRecord account = loaded.accounts.get(accountId.toString());
        assertEquals(accountId.toString(), account.accountId);
        assertEquals("", account.biography);
        assertEquals("", account.nonce);
        assertEquals(new LinkedHashSet<>(java.util.Set.of("elarion_core")), account.completedResetSteps);
        assertEquals(1, loaded.archives.size());
        assertEquals(characterId.toString(), loaded.archives.get(characterId.toString()).characterId);
        assertEquals(java.util.List.of("title_one"),
                loaded.archives.get(characterId.toString()).unlockedTitleIds);
        assertEquals(new LinkedHashMap<>(java.util.Map.of("valid", "value")),
                loaded.archives.get(characterId.toString()).metadata);
        assertEquals(new LinkedHashSet<>(java.util.Set.of("fallen_name")), loaded.reservedNames);
        assertTrue(Files.exists(file));
        try (var files = Files.list(tempDir)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().startsWith("state.json.corrupt-")));
        }
    }

    @Test
    void explicitNullCollectionsLoadAsEmpty() throws Exception {
        Path file = tempDir.resolve("state.json");
        Files.writeString(file, """
                {"accounts": null, "archives": null, "reservedNames": null}
                """);
        CharacterLifecycleStorage storage = new CharacterLifecycleStorage(LoggerFactory.getLogger("test"));

        CharacterLifecycleState loaded = storage.load(file);

        assertTrue(loaded.accounts.isEmpty());
        assertTrue(loaded.archives.isEmpty());
        assertTrue(loaded.reservedNames.isEmpty());
        assertTrue(Files.exists(file));
    }
}
