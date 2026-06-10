package panetina.elarion.core.config;

import panetina.elarion.core.model.HistoryRecordingPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CoreConfigHistorySupport {
    static final Set<String> DEFAULT_CHRONICLE_CATEGORIES = Set.of(
            "realm", "realm-decision", "diplomacy", "leadership", "title",
            "reward", "world", "administration", "security");

    private CoreConfigHistorySupport() {
    }

    static Settings load(Map<String, Object> history, Set<String> fallbackChronicleCategories) {
        Map<String, Object> recording = map(history.get("recording"));
        HistoryRecordingPolicy policy = new HistoryRecordingPolicy(
                bool(recording.get("enabled"), true),
                bool(recording.get("default-category-enabled"), true),
                stringSet(recording.get("enabled-categories")),
                stringSet(recording.get("disabled-categories")),
                bool(recording.get("default-type-enabled"), true),
                stringSet(recording.get("enabled-types")),
                stringSet(recording.get("disabled-types")));

        Map<String, Object> query = map(history.get("query"));
        int queryMaxMonths = Math.max(1, number(query.get("max-months-scanned"), 3).intValue());
        int commandLimitMax = Math.max(1, Math.min(number(query.get("command-limit-max"), 100).intValue(), 500));

        Map<String, Object> archive = map(history.get("archive"));
        boolean archiveEnabled = bool(archive.get("enabled"), true);
        int archiveMaxCompletedWeeks =
                Math.max(1, number(archive.get("max-completed-weeks-per-generation"), 8).intValue());
        Set<String> chronicleCategories = stringSet(archive.get("chronicle-categories"));
        if (chronicleCategories.isEmpty()) {
            chronicleCategories = fallbackChronicleCategories;
        }

        Map<String, Object> publicQuery = map(history.get("public-query"));
        int publicDefaultWeeks = Math.max(1, number(publicQuery.get("default-weeks"), 8).intValue());
        int publicMaxLimit = Math.max(1, Math.min(number(publicQuery.get("max-limit"), 200).intValue(), 1000));
        int publicDefaultLimit = Math.max(1, Math.min(
                number(publicQuery.get("default-limit"), 50).intValue(), publicMaxLimit));

        return new Settings(policy, queryMaxMonths, commandLimitMax, archiveEnabled,
                archiveMaxCompletedWeeks, chronicleCategories, publicDefaultWeeks,
                publicDefaultLimit, publicMaxLimit);
    }

    static void appendDefaults(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        StringBuilder addition = new StringBuilder();

        if (content.lines().noneMatch(line -> line.trim().equals("query:"))) {
            addition.append("""

                    query:
                      # History commands and ordinary API reads scan newest monthly
                      # JSONL files first and stop after this many months.
                      max-months-scanned: 3
                      command-limit-max: 100
                    """);
        }
        if (content.lines().noneMatch(line -> line.trim().equals("archive:"))) {
            addition.append("""

                    archive:
                      enabled: true
                      max-completed-weeks-per-generation: 8
                      chronicle-categories:
                        - "realm"
                        - "realm-decision"
                        - "diplomacy"
                        - "leadership"
                        - "title"
                        - "reward"
                        - "world"
                        - "administration"
                        - "security"
                    """);
        }
        if (content.lines().noneMatch(line -> line.trim().equals("public-query:"))) {
            addition.append("""

                    public-query:
                      default-weeks: 8
                      default-limit: 50
                      max-limit: 200
                    """);
        }
        if (!addition.isEmpty()) {
            Files.writeString(path, addition.toString(), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND);
        }
    }

    static void validate(Map<String, Object> history, int configVersion, List<String> errors) {
        checkKeys("history.yml", history,
                Set.of("config-version", "recording", "query", "archive", "public-query"), errors);
        checkVersion("history.yml", history, configVersion, errors);
        Map<String, Object> recording = requiredMap("history.yml.recording", history.get("recording"), errors);
        checkKeys("history.yml.recording", recording, Set.of("enabled",
                "default-category-enabled", "enabled-categories", "disabled-categories",
                "default-type-enabled", "enabled-types", "disabled-types"), errors);
        requireBoolean("history.yml.recording.enabled", recording.get("enabled"), errors);
        requireBoolean("history.yml.recording.default-category-enabled",
                recording.get("default-category-enabled"), errors);
        requireStringCollection("history.yml.recording.enabled-categories",
                recording.get("enabled-categories"), errors);
        requireStringCollection("history.yml.recording.disabled-categories",
                recording.get("disabled-categories"), errors);
        requireBoolean("history.yml.recording.default-type-enabled",
                recording.get("default-type-enabled"), errors);
        requireStringCollection("history.yml.recording.enabled-types", recording.get("enabled-types"), errors);
        requireStringCollection("history.yml.recording.disabled-types", recording.get("disabled-types"), errors);

        Map<String, Object> query = requiredMap("history.yml.query", history.get("query"), errors);
        checkKeys("history.yml.query", query, Set.of("max-months-scanned", "command-limit-max"), errors);
        requireNumber("history.yml.query.max-months-scanned", query.get("max-months-scanned"), errors);
        requireNumber("history.yml.query.command-limit-max", query.get("command-limit-max"), errors);

        Map<String, Object> archive = requiredMap("history.yml.archive", history.get("archive"), errors);
        checkKeys("history.yml.archive", archive,
                Set.of("enabled", "max-completed-weeks-per-generation", "chronicle-categories"), errors);
        requireBoolean("history.yml.archive.enabled", archive.get("enabled"), errors);
        requireNumber("history.yml.archive.max-completed-weeks-per-generation",
                archive.get("max-completed-weeks-per-generation"), errors);
        requireStringCollection("history.yml.archive.chronicle-categories",
                archive.get("chronicle-categories"), errors);

        Map<String, Object> publicQuery = requiredMap("history.yml.public-query", history.get("public-query"), errors);
        checkKeys("history.yml.public-query", publicQuery,
                Set.of("default-weeks", "default-limit", "max-limit"), errors);
        requireNumber("history.yml.public-query.default-weeks", publicQuery.get("default-weeks"), errors);
        requireNumber("history.yml.public-query.default-limit", publicQuery.get("default-limit"), errors);
        requireNumber("history.yml.public-query.max-limit", publicQuery.get("max-limit"), errors);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> result ? (Map<String, Object>) result : Map.of();
    }

    private static Number number(Object value, Number fallback) {
        return value instanceof Number number ? number : fallback;
    }

    private static boolean bool(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static Set<String> stringSet(Object value) {
        if (!(value instanceof Collection<?> collection)) return Set.of();
        Set<String> result = new LinkedHashSet<>();
        collection.forEach(item -> result.add(String.valueOf(item)));
        return result;
    }

    private static void checkVersion(String file, Map<String, Object> root, int configVersion, List<String> errors) {
        Number version = requireNumber(file + ".config-version", root.get("config-version"), errors);
        if (version != null && version.intValue() != configVersion) {
            errors.add(file + ".config-version: expected " + configVersion + " but found " + version);
        }
    }

    private static void checkKeys(String path, Map<String, Object> values, Set<String> allowed, List<String> errors) {
        values.keySet().stream()
                .filter(key -> !allowed.contains(key))
                .forEach(key -> errors.add(path + "." + key + ": unknown field"));
    }

    private static Map<String, Object> requiredMap(String path, Object value, List<String> errors) {
        if (value instanceof Map<?, ?>) return map(value);
        errors.add(path + ": expected a mapping");
        return Map.of();
    }

    private static Number requireNumber(String path, Object value, List<String> errors) {
        if (value instanceof Number number) return number;
        errors.add(path + ": expected a number");
        return null;
    }

    private static void requireBoolean(String path, Object value, List<String> errors) {
        if (!(value instanceof Boolean)) errors.add(path + ": expected true or false");
    }

    private static void requireStringCollection(String path, Object value, List<String> errors) {
        if (!(value instanceof Collection<?> collection)) {
            errors.add(path + ": expected a list of strings");
            return;
        }
        if (collection.stream().anyMatch(item -> !(item instanceof String))) {
            errors.add(path + ": every list item must be a string");
        }
    }

    record Settings(
            HistoryRecordingPolicy recordingPolicy,
            int queryMaxMonths,
            int commandLimitMax,
            boolean archiveEnabled,
            int archiveMaxCompletedWeeks,
            Set<String> chronicleCategories,
            int publicDefaultWeeks,
            int publicDefaultLimit,
            int publicMaxLimit
    ) {
    }
}
