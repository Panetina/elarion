package panetina.elarion.addons.mounts.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MountCollectionTextConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsCollectionTextFromYaml() throws Exception {
        Path file = tempDir.resolve("collection.yml");
        Files.writeString(file, """
                mounts:
                  wyvern:
                    locked-row: "Sky trial reward."
                    unlocked-row: "Wyvern ready."
                    locked-detail: "Earn this from the sky trial."
                    unlocked-detail: "Set this wyvern active."
                """);

        MountCollectionTextConfig config = MountCollectionTextConfig.load(file, null);

        MountCollectionTextConfig.Entry entry = config.entry(ElarionMountType.WYVERN);
        assertEquals("Sky trial reward.", entry.lockedRow());
        assertEquals("Wyvern ready.", entry.unlockedRow());
        assertEquals("Earn this from the sky trial.", entry.lockedDetail());
        assertEquals("Set this wyvern active.", entry.unlockedDetail());
        assertEquals("Realm vendor: {realm}", config.entry(ElarionMountType.AIRSHIP).lockedRow());
    }

    @Test
    void malformedCollectionTextFallsBackToDefaults() throws Exception {
        Path file = tempDir.resolve("collection.yml");
        Files.writeString(file, "mounts: [");

        MountCollectionTextConfig config = MountCollectionTextConfig.load(file, null);

        assertEquals("Future collection reward.",
                config.entry(ElarionMountType.WYVERN).lockedRow());
        assertEquals("Realm vendor: {realm}",
                config.entry(ElarionMountType.AIRSHIP).lockedRow());
    }
}
