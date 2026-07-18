package panetina.elarion.addons.government.config;

import net.minecraft.util.Identifier;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentOfficeDefinition;
import panetina.elarion.addons.government.model.GovernmentSettings;
import panetina.elarion.core.api.AddonConfigFiles;
import panetina.elarion.core.api.ElarionApi;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GovernmentConfigLoader {
    private GovernmentConfigLoader() {
    }

    public static Map<String, GovernmentFormDefinition> load(ElarionApi api) {
        Path root = ensureDefaults();
        Path forms = root.resolve("forms");
        Map<String, GovernmentFormDefinition> definitions = new LinkedHashMap<>();
        Yaml yaml = new Yaml();
        try (var directories = Files.list(forms)) {
            for (Path folder : directories.filter(Files::isDirectory).sorted().toList()) {
                Path file = folder.resolve("form.yml");
                if (Files.notExists(file)) continue;
                GovernmentFormDefinition definition = readForm(api, yaml, file);
                validate(definition, file);
                if (definitions.put(definition.id(), definition) != null) {
                    throw new GovernmentConfigException("Duplicate government form id " + definition.id());
                }
            }
        } catch (IOException exception) {
            throw new GovernmentConfigException("Failed to read government forms", exception);
        }
        if (definitions.isEmpty()) throw new GovernmentConfigException("No government form definitions loaded.");
        return Map.copyOf(definitions);
    }

    public static GovernmentSettings loadSettings() {
        Path root = ensureDefaults();
        Path file = root.resolve("government.yml");
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (!(loaded instanceof Map<?, ?> raw)) return GovernmentSettings.defaults();
            Map<?, ?> authority = map(raw.get("authority"));
            return new GovernmentSettings(
                    integer(authority, "inactivity-days", 7),
                    integer(authority, "inactivity-check-interval-seconds", 600));
        } catch (IOException exception) {
            throw new GovernmentConfigException("Failed to read government settings", exception);
        }
    }

    private static Path ensureDefaults() {
        Path government = AddonConfigFiles.writeDefault("government", "government.yml",
                GovernmentConfigDefaults.GOVERNMENT);
        Path root = government.getParent();
        writeDefault(root.resolve("forms").resolve("monarchy").resolve("form.yml"),
                GovernmentConfigDefaults.MONARCHY_FORM);
        writeDefault(root.resolve("forms").resolve("republic").resolve("form.yml"),
                GovernmentConfigDefaults.REPUBLIC_FORM);
        writeDefault(root.resolve("forms").resolve("theocracy").resolve("form.yml"),
                GovernmentConfigDefaults.THEOCRACY_FORM);
        writeDefault(root.resolve("forms").resolve("confederation").resolve("form.yml"),
                GovernmentConfigDefaults.CONFEDERATION_FORM);
        return root;
    }

    private static void writeDefault(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new GovernmentConfigException("Failed to write default " + path, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static GovernmentFormDefinition readForm(ElarionApi api, Yaml yaml, Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object loaded = yaml.load(reader);
            if (!(loaded instanceof Map<?, ?> raw)) {
                throw new GovernmentConfigException(file + " must contain a YAML mapping.");
            }
            Map<String, Object> map = (Map<String, Object>) raw;
            return new GovernmentFormDefinition(
                    string(map, "id"),
                    api.system().placeholders().replaceIdentity(string(map, "display-name")),
                    api.system().placeholders().replaceIdentity(string(map, "description")),
                    bool(map, "enabled", true),
                    api.system().placeholders().replaceIdentity(string(map, "official-name-template")),
                    stringList(map.get("authority-offices")),
                    bool(map, "confederation-delegates-represent-groups", false),
                    offices(api, map.get("offices")),
                    stringListMap(map.get("actions")),
                    stringMap(map.get("transitions"))
            );
        } catch (IOException exception) {
            throw new GovernmentConfigException("Failed to read " + file, exception);
        } catch (RuntimeException exception) {
            if (exception instanceof GovernmentConfigException config) throw config;
            throw new GovernmentConfigException("Invalid government form " + file + ": " + exception.getMessage(),
                    exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<GovernmentOfficeDefinition> offices(ElarionApi api, Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        List<GovernmentOfficeDefinition> result = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> raw)) continue;
            Map<String, Object> map = (Map<String, Object>) raw;
            result.add(new GovernmentOfficeDefinition(
                    string(map, "id"),
                    api.system().placeholders().replaceIdentity(string(map, "display-name")),
                    api.system().placeholders().replaceIdentity(string(map, "description")),
                    integer(map, "max-holders", 1)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> stringListMap(Object value) {
        if (!(value instanceof Map<?, ?> raw)) return Map.of();
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            List<String> values = new ArrayList<>();
            if (entry.getValue() instanceof List<?> list) {
                for (Object item : list) values.add(String.valueOf(item));
            }
            result.put(String.valueOf(entry.getKey()), List.copyOf(values));
        }
        return result;
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) return List.of();
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            String string = item == null ? "" : String.valueOf(item).trim();
            if (!string.isBlank()) result.add(string);
        }
        return List.copyOf(result);
    }

    private static Map<String, String> stringMap(Object value) {
        if (!(value instanceof Map<?, ?> raw)) return Map.of();
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }

    private static Map<?, ?> map(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }

    private static void validate(GovernmentFormDefinition definition, Path file) {
        List<String> errors = new ArrayList<>();
        if (definition.id().isBlank()) errors.add("missing id");
        if (Identifier.tryParse(definition.id()) == null && !definition.id().matches("[a-z0-9_./-]+")) {
            errors.add("id must use lowercase identifier-safe characters");
        }
        for (GovernmentOfficeDefinition office : definition.offices()) {
            if (office.id().isBlank()) errors.add("office missing id");
            if (!office.id().matches("[a-z0-9_./-]+")) errors.add("office id invalid: " + office.id());
            if (office.maxHolders() < 1) errors.add("office max-holders must be positive: " + office.id());
        }
        for (String office : definition.authorityOffices()) {
            boolean found = definition.offices().stream().anyMatch(candidate -> candidate.id().equals(office));
            if (!found) errors.add("authority office does not exist: " + office);
        }
        if (!errors.isEmpty()) throw new GovernmentConfigException(file + ": " + String.join("; ", errors));
    }

    private static String string(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean bool(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    private static int integer(Map<?, ?> map, String key, int fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.intValue();
        if (value == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
