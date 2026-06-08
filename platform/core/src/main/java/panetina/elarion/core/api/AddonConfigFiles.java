package panetina.elarion.core.api;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AddonConfigFiles {
    private AddonConfigFiles() {}

    public static Path writeDefault(String addonId, String fileName, String content) {
        Path directory = FabricLoader.getInstance().getConfigDir()
                .resolve("elarion/addons")
                .resolve(addonId);
        Path file = directory.resolve(fileName);
        try {
            Files.createDirectories(directory);
            if (Files.notExists(file)) {
                Files.writeString(file, content, StandardCharsets.UTF_8);
            }
            return file;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create addon config " + file, exception);
        }
    }
}
