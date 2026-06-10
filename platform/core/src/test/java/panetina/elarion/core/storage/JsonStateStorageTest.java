package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class JsonStateStorageTest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @TempDir
    Path tempDir;

    @Test
    void writesJsonAtomicallyAndCleansTemporaryFile() throws Exception {
        Path file = tempDir.resolve("state.json");

        JsonStateStorage.writeAtomic(file, GSON, new Stored("oak", 3),
                LoggerFactory.getLogger("test"), "test state");

        Stored loaded = GSON.fromJson(Files.readString(file), Stored.class);
        assertEquals("oak", loaded.realm);
        assertEquals(3, loaded.count);
        assertFalse(Files.exists(tempDir.resolve("state.json.tmp")));
    }

    @Test
    void readFallsBackOnMissingFile() {
        Stored loaded = JsonStateStorage.read(
                tempDir.resolve("missing.json"),
                GSON,
                Stored.class,
                () -> new Stored("fallback", 1),
                stored -> stored,
                LoggerFactory.getLogger("test"),
                "missing state");

        assertEquals("fallback", loaded.realm);
        assertEquals(1, loaded.count);
    }

    private record Stored(String realm, int count) {
    }
}
