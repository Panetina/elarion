package panetina.elarion.core.api.reset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerResetFilesTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void accessListResetIsAtomicAndBackupRemainsRecoverable() throws Exception {
        Path source = temporaryDirectory.resolve("server/whitelist.json");
        Path backup = temporaryDirectory.resolve("backup/whitelist.json");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                [
                  {"name":"Panyel","uuid":"5eda14b6-711c-4729-ab92-0e9b3ffcb7c1"},
                  {"name":"MuteMusic","uuid":"38e6b73e-c699-4db2-9dbe-95c714c5c30f"}
                ]
                """);

        PlayerResetFiles.copyTree(source, backup);
        PlayerResetFiles.writeEmptyJsonArrayAtomic(source);

        assertEquals(0L, PlayerResetFiles.countJsonArrayEntries(source));
        assertEquals(2L, PlayerResetFiles.countJsonArrayEntries(backup));
        assertEquals("[]", Files.readString(source).trim());
        try (var siblings = Files.list(source.getParent())) {
            assertTrue(siblings.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    void missingAccessListCountsAsEmpty() throws Exception {
        assertEquals(0L, PlayerResetFiles.countJsonArrayEntries(temporaryDirectory.resolve("ops.json")));
    }

    @Test
    void backupManifestIsAtomicAndContainsOnlyBackupRelativeTargets() throws Exception {
        Path backup = temporaryDirectory.resolve("backup");
        PlayerResetFiles.writeBackupManifestAtomic(backup, Map.of(
                "z_addon", List.of("z_addon/state.json"),
                "minecraft", List.of("minecraft/playerdata", "minecraft/ops.json")
        ));

        String manifest = Files.readString(backup.resolve("manifest.json"));
        assertTrue(manifest.contains("\"schemaVersion\":1"));
        assertTrue(manifest.contains("minecraft/playerdata"));
        assertTrue(manifest.indexOf("minecraft") < manifest.indexOf("z_addon"));
        try (var siblings = Files.list(backup)) {
            assertTrue(siblings.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }
}
