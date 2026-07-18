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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void readQuarantinesMalformedStateBeforeReturningFallback() throws Exception {
        Path file = tempDir.resolve("state.json");
        Files.writeString(file, "{broken");

        Stored loaded = JsonStateStorage.read(
                file,
                GSON,
                Stored.class,
                () -> new Stored("fallback", 1),
                stored -> stored,
                LoggerFactory.getLogger("test"),
                "malformed state");

        assertEquals("fallback", loaded.realm);
        assertFalse(Files.exists(file));
        try (var quarantined = Files.list(tempDir)) {
            Path backup = quarantined
                    .filter(path -> path.getFileName().toString().startsWith("state.json.corrupt-"))
                    .findFirst()
                    .orElseThrow();
            assertEquals("{broken", Files.readString(backup));
        }
    }

    @Test
    void writeAtomicPropagatesDurabilityFailure() throws Exception {
        Path parentFile = tempDir.resolve("not-a-directory");
        Files.writeString(parentFile, "occupied");
        Path state = parentFile.resolve("state.json");

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> JsonStateStorage.writeAtomic(state, GSON, new Stored("oak", 3),
                        LoggerFactory.getLogger("test"), "blocked state"));

        assertTrue(failure.getMessage().contains("blocked state"));
        assertEquals("occupied", Files.readString(parentFile));
    }

    private record Stored(String realm, int count) {
    }
}
