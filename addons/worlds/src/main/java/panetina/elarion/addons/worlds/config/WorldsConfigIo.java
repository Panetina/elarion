package panetina.elarion.addons.worlds.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class WorldsConfigIo {
    private WorldsConfigIo() {
    }

    static void writeYaml(Path destination, Map<String, Object> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        Files.createDirectories(destination.getParent());
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            new Yaml(options).dump(data, writer);
        }
        try {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> readRoot(Path path, String displayName) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (loaded == null) return Map.of();
            if (loaded instanceof Map<?, ?> map) return (Map<String, Object>) map;
            throw new WorldsConfigException(List.of(displayName
                    + ": expected a YAML object at the document root"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }

    static Map<String, Object> deepMutableMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            Object copied = value;
            if (value instanceof Map<?, ?> mapValue) copied = deepMutableMap(mapValue);
            else if (value instanceof List<?> listValue) copied = new ArrayList<>(listValue);
            result.put(String.valueOf(key), copied);
        });
        return result;
    }

    static Map<String, Object> mutableChild(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        Map<String, Object> child = value instanceof Map<?, ?> mapValue
                ? deepMutableMap(mapValue)
                : new LinkedHashMap<>();
        parent.put(key, child);
        return child;
    }
}
