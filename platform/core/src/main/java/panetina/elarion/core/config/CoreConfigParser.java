package panetina.elarion.core.config;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.core.model.ProgressionRegion;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.model.ServerIdentityConfig;
import panetina.elarion.core.model.SpawnPoint;
import panetina.elarion.core.model.ElarionTitlePresentation;
import panetina.elarion.core.model.TitleAcquisitionMode;
import panetina.elarion.core.model.TitleActiveEffect;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.TitleOwnershipMode;
import panetina.elarion.core.model.TitleUnlockRule;
import panetina.elarion.core.model.VisibilityScope;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class CoreConfigParser {
    private final Logger logger;
    private final Yaml yaml;
    private final Path coreConfigDir;
    private final String defaultTitleId;
    private final ServerIdentityConfig serverIdentity;

    CoreConfigParser(Logger logger, Yaml yaml, Path coreConfigDir, String defaultTitleId,
                     ServerIdentityConfig serverIdentity) {
        this.logger = logger;
        this.yaml = yaml;
        this.coreConfigDir = coreConfigDir;
        this.defaultTitleId = defaultTitleId;
        this.serverIdentity = serverIdentity;
    }

    Map<String, RealmDefinition> loadRealms() {
        Map<String, RealmDefinition> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("realms.yml");
        for (Map.Entry<String, Object> entry : map(root.get("realms")).entrySet()) {
            String id = normalizeId(entry.getKey());
            Map<String, Object> data = map(entry.getValue());
            Map<String, Object> spawn = map(data.get("spawn"));
            SpawnPoint spawnPoint = new SpawnPoint(
                    string(spawn.get("world"), "elarion:lobby"),
                    number(spawn.get("x"), 0).doubleValue(),
                    number(spawn.get("y"), 64).doubleValue(),
                    number(spawn.get("z"), 0).doubleValue(),
                    number(spawn.get("yaw"), 0).floatValue(),
                    number(spawn.get("pitch"), 0).floatValue()
            );
            result.put(id, new RealmDefinition(
                    id,
                    string(data.get("display-name"), id),
                    string(data.get("short-name"), id.toUpperCase(Locale.ROOT)),
                    string(data.get("prefix"), ""),
                    string(data.get("color"), "white"),
                    spawnPoint,
                    enumValue(VisibilityScope.class, data.get("visibility-scope"), VisibilityScope.REALM),
                    stringSet(data.get("flags"))
            ));
        }
        return Map.copyOf(result);
    }

    Map<String, TitleDefinition> loadTitles() {
        Map<String, TitleDefinition> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("titles.yml");
        for (Map.Entry<String, Object> entry : map(root.get("titles")).entrySet()) {
            String id = normalizeId(entry.getKey());
            Map<String, Object> data = map(entry.getValue());
            TitleAcquisitionMode acquisitionMode = enumValue(TitleAcquisitionMode.class, data.get("acquisition-mode"),
                    id.equals(defaultTitleId) ? TitleAcquisitionMode.DEFAULT : TitleAcquisitionMode.ADMIN_ONLY);
            TitleOwnershipMode ownershipMode =
                    enumValue(TitleOwnershipMode.class, data.get("ownership-mode"), TitleOwnershipMode.UNLIMITED);
            result.put(id, new TitleDefinition(
                    id,
                    text(data.get("description"), ""),
                    text(data.get("display-name"), id),
                    text(data.get("prefix"), ""),
                    text(data.get("suffix"), ""),
                    titleColor(id, data.get("color"), ownershipMode),
                    number(data.get("priority"), 0).intValue(),
                    bool(data.get("visible-under-username"), true),
                    acquisitionMode,
                    ownershipMode,
                    bool(data.get("hidden-from-discovery"), false),
                    stringSet(data.get("abilities")),
                    activeEffects(data.get("active-effects"))
            ));
        }
        return Map.copyOf(result);
    }

    private static int titleColor(String titleId, Object raw, TitleOwnershipMode ownershipMode) {
        if (raw == null) {
            return ElarionTitlePresentation.fallbackColor(titleId, ownershipMode);
        }
        String value = string(raw, "").trim();
        if (value.startsWith("#")) value = value.substring(1);
        try {
            return 0xFF000000 | (Integer.parseInt(value, 16) & 0x00FFFFFF);
        } catch (NumberFormatException ignored) {
            return ElarionTitlePresentation.fallbackColor(titleId, ownershipMode);
        }
    }

    Map<String, ProgressionRegion> loadProgressionRegions() {
        Map<String, ProgressionRegion> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("title-progression.yml");
        for (Map.Entry<String, Object> entry : map(root.get("regions")).entrySet()) {
            String id = normalizeId(entry.getKey());
            Map<String, Object> data = map(entry.getValue());
            result.put(id, new ProgressionRegion(
                    id,
                    string(data.get("world"), ""),
                    number(data.get("min-x"), 0).doubleValue(),
                    number(data.get("min-y"), 0).doubleValue(),
                    number(data.get("min-z"), 0).doubleValue(),
                    number(data.get("max-x"), 0).doubleValue(),
                    number(data.get("max-y"), 0).doubleValue(),
                    number(data.get("max-z"), 0).doubleValue()
            ));
        }
        return Map.copyOf(result);
    }

    Map<String, TitleUnlockRule> loadTitleUnlockRules() {
        Map<String, TitleUnlockRule> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("title-progression.yml");
        for (Map.Entry<String, Object> entry : map(root.get("rules")).entrySet()) {
            String id = normalizeId(entry.getKey());
            Map<String, Object> data = map(entry.getValue());
            Map<String, Object> continuous = map(data.get("continuous"));
            TitleUnlockRule.Continuous continuousRule = continuous.isEmpty() ? null : new TitleUnlockRule.Continuous(
                    number(continuous.get("duration"), 1).longValue(),
                    string(continuous.get("duration-unit"), "minecraft_days"),
                    number(continuous.get("sample-interval-ticks"), 100).longValue(),
                    bool(continuous.get("reset-on-failure"), true),
                    stringSet(continuous.get("required-status-effects")),
                    stringSet(continuous.get("allowed-status-effects")),
                    stringSet(continuous.get("required-metadata"))
            );
            result.put(id, new TitleUnlockRule(
                    id,
                    string(data.get("title"), id),
                    string(data.get("trigger"), ""),
                    string(data.get("stat-key"), ""),
                    number(data.get("threshold"), 1).longValue(),
                    number(data.get("amount"), 1).longValue(),
                    matchers(data.get("entities")),
                    matchers(data.get("blocks")),
                    matchers(data.get("items")),
                    matchers(data.get("recipes")),
                    stringSet(data.get("worlds")),
                    stringSet(data.get("dimensions")),
                    stringSet(data.get("biomes")),
                    stringSet(data.get("regions")),
                    stringMap(data.get("metadata")),
                    continuousRule
            ));
        }
        return Map.copyOf(result);
    }

    Map<String, List<RewardAction>> loadRewards() {
        Map<String, List<RewardAction>> result = new LinkedHashMap<>();
        Map<String, Object> root = loadMap("rewards.yml");
        for (Map.Entry<String, Object> entry : map(root.get("rewards")).entrySet()) {
            List<RewardAction> actions = new ArrayList<>();
            Object rawActions = map(entry.getValue()).get("actions");
            if (rawActions instanceof Collection<?> collection) {
                for (Object item : collection) {
                    Map<String, Object> raw = map(item);
                    String type = string(raw.get("type"), "");
                    Map<String, String> parameters = new LinkedHashMap<>();
                    raw.forEach((key, value) -> {
                        if (!key.equals("type")) parameters.put(key, serverIdentity.replace(String.valueOf(value)));
                    });
                    if (!type.isBlank()) actions.add(new RewardAction(type, parameters));
                }
            }
            result.put(normalizeId(entry.getKey()), List.copyOf(actions));
        }
        return Map.copyOf(result);
    }

    private static Set<TitleActiveEffect> activeEffects(Object value) {
        if (!(value instanceof Collection<?> collection)) return Set.of();
        Set<TitleActiveEffect> result = new LinkedHashSet<>();
        for (Object item : collection) {
            Map<String, Object> raw = map(item);
            String type = string(raw.get("type"), "");
            Map<String, String> parameters = new LinkedHashMap<>();
            raw.forEach((key, parameter) -> {
                if (!key.equals("type")) parameters.put(key, String.valueOf(parameter));
            });
            if (!type.isBlank()) result.add(new TitleActiveEffect(type, parameters));
        }
        return Set.copyOf(result);
    }

    private static Set<TitleUnlockRule.RegistryMatcher> matchers(Object value) {
        if (!(value instanceof Collection<?> collection)) return Set.of();
        Set<TitleUnlockRule.RegistryMatcher> result = new LinkedHashSet<>();
        for (Object item : collection) {
            String raw = String.valueOf(item);
            if (!raw.isBlank()) result.add(TitleUnlockRule.RegistryMatcher.parse(raw));
        }
        return Set.copyOf(result);
    }

    private static Map<String, String> stringMap(Object value) {
        Map<String, Object> raw = map(value);
        Map<String, String> result = new LinkedHashMap<>();
        raw.forEach((key, item) -> result.put(key, String.valueOf(item)));
        return Map.copyOf(result);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> loadMap(String fileName) {
        Path path = coreConfigDir.resolve(fileName);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = yaml.load(reader);
            return loaded instanceof Map<?, ?> value ? (Map<String, Object>) value : Map.of();
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load {}", path, exception);
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> result ? (Map<String, Object>) result : Map.of();
    }

    private static String normalizeId(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private String text(Object value, String fallback) {
        return serverIdentity.replace(string(value, fallback));
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

    private static <T extends Enum<T>> T enumValue(Class<T> type, Object value, T fallback) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, String.valueOf(value).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

}
