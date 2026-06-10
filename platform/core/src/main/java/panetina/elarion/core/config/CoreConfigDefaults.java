package panetina.elarion.core.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

final class CoreConfigDefaults {
    private CoreConfigDefaults() {
    }

    static void write(Path coreConfigDir, Map<String, String> defaults) throws IOException {
        Files.createDirectories(coreConfigDir);
        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            Path path = coreConfigDir.resolve(entry.getKey());
            if (Files.notExists(path)) {
                Files.writeString(path, entry.getValue(), StandardCharsets.UTF_8);
            }
        }
    }
}
