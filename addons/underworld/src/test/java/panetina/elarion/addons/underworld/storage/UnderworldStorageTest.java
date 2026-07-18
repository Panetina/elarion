package panetina.elarion.addons.underworld.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnderworldStorageTest {
    @TempDir
    Path root;

    @Test
    void legacyStateLoadsAndRewritesWithCurrentSchema() throws Exception {
        Files.writeString(root.resolve("state.json"), "{\"corpses\":{},\"sessions\":{},\"souls\":{}}");
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        UnderworldState loaded = storage.load(root);
        storage.save(root, loaded);

        assertEquals(UnderworldState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertTrue(loaded.recoveryVaults.isEmpty());
        assertTrue(Files.readString(root.resolve("state.json")).contains("\"schemaVersion\": 1"));
    }

    @Test
    void unsupportedFutureSchemaIsQuarantined() throws Exception {
        Path stateFile = root.resolve("state.json");
        Files.writeString(stateFile, "{\"schemaVersion\":99,\"corpses\":{}}");
        UnderworldStorage storage = new UnderworldStorage(LoggerFactory.getLogger("test"), root);

        UnderworldState loaded = storage.load(root);

        assertEquals(UnderworldState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertTrue(loaded.corpses.isEmpty());
        assertFalse(Files.exists(stateFile));
        try (var files = Files.list(root)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().startsWith("state.json.corrupt-")));
        }
    }
}
