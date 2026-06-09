package panetina.elarion.addons.worlds.config;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.worlds.model.BlockAbundanceRule;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.MobAbundanceRule;
import panetina.elarion.addons.worlds.model.WorldBorderDefinition;
import panetina.elarion.addons.worlds.model.WorldSpawn;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class WorldsConfigManager {
    public static final int CONFIG_VERSION = 1;
    private static final String DEFAULT_CONFIG = """
            # Elarion Worlds desired-state configuration.
            # Worlds with enabled: true are created or restored when the server starts.
            # retain-chance is 0.0 (none) through 1.0 (vanilla amount).
            config-version: 1
            defaults:
              template: "minecraft:overworld"
              difficulty: "NORMAL"
              tick-time: true
              border:
                center-x: 0.0
                center-z: 0.0
                size: 10000.0
                safe-zone: 5.0
                damage-per-block: 0.2
                warning-blocks: 5
                warning-time: 15
            worlds:
              community_world_1:
                enabled: true
                id: "elarion:community_world_1"
                seed: 11001
                spawn: { x: 0.5, y: 80.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                block-abundance:
                  minecraft:iron_ore: { retain-chance: 0.25, replace-with: "minecraft:stone" }
                  minecraft:deepslate_iron_ore: { retain-chance: 0.25, replace-with: "minecraft:deepslate" }
                mob-abundance: {}
                gamerules: {}
              community_world_2:
                enabled: true
                id: "elarion:community_world_2"
                seed: 22002
                spawn: { x: 0.5, y: 80.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                block-abundance:
                  minecraft:sugar_cane: { retain-chance: 0.0, replace-with: "minecraft:air" }
                mob-abundance: {}
                gamerules: {}
              community_world_3:
                enabled: true
                id: "elarion:community_world_3"
                seed: 33003
                spawn: { x: 0.5, y: 80.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                block-abundance: {}
                mob-abundance: {}
                gamerules: {}
            """;

    private final Logger logger;
    private final Path path;
    private volatile Map<String, ManagedWorldDefinition> worlds = Map.of();

    public WorldsConfigManager(Logger logger) {
        this.logger = logger;
        this.path = AddonConfigFiles.writeDefault("worlds", "worlds.yml", DEFAULT_CONFIG);
    }

    public WorldsConfigManager(Logger logger, Path path) {
        this.logger = logger;
        this.path = path;
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) Files.writeString(path, DEFAULT_CONFIG, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create Worlds config " + path, exception);
        }
    }

    public synchronized void load() {
        Map<String, Object> root = readRoot();
        List<String> errors = new ArrayList<>();
        checkKeys("worlds.yml", root, Set.of("config-version", "defaults", "worlds"), errors);
        integer("worlds.yml.config-version", root.get("config-version"), CONFIG_VERSION, errors);

        Map<String, Object> defaults = map("worlds.yml.defaults", root.get("defaults"), errors);
        checkKeys("worlds.yml.defaults", defaults, Set.of("template", "difficulty", "tick-time", "border"), errors);
        String defaultTemplate = string("worlds.yml.defaults.template", defaults.get("template"),
                "minecraft:overworld", errors);
        String defaultDifficulty = difficulty("worlds.yml.defaults.difficulty", defaults.get("difficulty"),
                "NORMAL", errors);
        boolean defaultTickTime = bool("worlds.yml.defaults.tick-time", defaults.get("tick-time"), true, errors);
        WorldBorderDefinition defaultBorder = border("worlds.yml.defaults.border",
                map("worlds.yml.defaults.border", defaults.get("border"), errors), null, errors);

        Map<String, Object> configuredWorlds = map("worlds.yml.worlds", root.get("worlds"), errors);
        Map<String, ManagedWorldDefinition> parsed = new LinkedHashMap<>();
        Set<String> worldIds = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry : configuredWorlds.entrySet()) {
            String key = entry.getKey();
            String field = "worlds.yml.worlds." + key;
            Map<String, Object> data = map(field, entry.getValue(), errors);
            checkKeys(field, data, Set.of(
                    "enabled", "id", "template", "seed", "difficulty", "tick-time", "spawn", "border",
                    "gamerules", "block-abundance", "mob-abundance"), errors);

            String id = string(field + ".id", data.get("id"), "elarion:" + key, errors);
            identifier(field + ".id", id, errors);
            if (!worldIds.add(id.toLowerCase(Locale.ROOT))) errors.add(field + ".id: duplicate world id " + id);

            String template = string(field + ".template", data.get("template"), defaultTemplate, errors);
            identifier(field + ".template", template, errors);
            boolean enabled = bool(field + ".enabled", data.get("enabled"), true, errors);
            long seed = longNumber(field + ".seed", data.get("seed"), key.hashCode(), errors);
            String worldDifficulty = difficulty(field + ".difficulty", data.get("difficulty"),
                    defaultDifficulty, errors);
            boolean tickTime = bool(field + ".tick-time", data.get("tick-time"), defaultTickTime, errors);
            WorldSpawn spawn = spawn(field + ".spawn", map(field + ".spawn", data.get("spawn"), errors), errors);
            WorldBorderDefinition worldBorder = data.containsKey("border")
                    ? border(field + ".border", map(field + ".border", data.get("border"), errors),
                    defaultBorder, errors)
                    : defaultBorder;
            Map<String, String> gameRules = stringMap(field + ".gamerules", data.get("gamerules"), errors);
            List<BlockAbundanceRule> blockRules = blockRules(field + ".block-abundance",
                    data.get("block-abundance"), errors);
            List<MobAbundanceRule> mobRules = mobRules(field + ".mob-abundance",
                    data.get("mob-abundance"), errors);

            parsed.put(key, new ManagedWorldDefinition(key, enabled, id, template, seed, worldDifficulty,
                    tickTime, spawn, worldBorder, gameRules, blockRules, mobRules));
        }

        if (!errors.isEmpty()) throw new WorldsConfigException(errors);
        worlds = Map.copyOf(parsed);
        logger.info("Loaded {} Elarion managed world definitions", worlds.size());
    }

    public Map<String, ManagedWorldDefinition> worlds() {
        return worlds;
    }

    public void restore(Map<String, ManagedWorldDefinition> previous) {
        worlds = Map.copyOf(previous);
    }

    public Path path() {
        return path;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readRoot() {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            if (loaded == null) return Map.of();
            if (loaded instanceof Map<?, ?> map) return (Map<String, Object>) map;
            throw new WorldsConfigException(List.of("worlds.yml: expected a YAML object at the document root"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + path, exception);
        }
    }

    private static WorldSpawn spawn(String field, Map<String, Object> data, List<String> errors) {
        checkKeys(field, data, Set.of("x", "y", "z", "yaw", "pitch"), errors);
        return new WorldSpawn(
                number(field + ".x", data.get("x"), 0.5, errors),
                number(field + ".y", data.get("y"), 80.0, errors),
                number(field + ".z", data.get("z"), 0.5, errors),
                (float) number(field + ".yaw", data.get("yaw"), 0.0, errors),
                (float) number(field + ".pitch", data.get("pitch"), 0.0, errors));
    }

    private static WorldBorderDefinition border(
            String field,
            Map<String, Object> data,
            WorldBorderDefinition fallback,
            List<String> errors
    ) {
        checkKeys(field, data, Set.of(
                "center-x", "center-z", "size", "safe-zone", "damage-per-block",
                "warning-blocks", "warning-time"), errors);
        WorldBorderDefinition defaults = fallback == null
                ? new WorldBorderDefinition(0, 0, 10_000, 5, 0.2, 5, 15)
                : fallback;
        double size = number(field + ".size", data.get("size"), defaults.size(), errors);
        if (size < 1 || size > 59_999_968) errors.add(field + ".size: expected 1 through 59999968");
        return new WorldBorderDefinition(
                number(field + ".center-x", data.get("center-x"), defaults.centerX(), errors),
                number(field + ".center-z", data.get("center-z"), defaults.centerZ(), errors),
                size,
                nonNegative(field + ".safe-zone", data.get("safe-zone"), defaults.safeZone(), errors),
                nonNegative(field + ".damage-per-block", data.get("damage-per-block"),
                        defaults.damagePerBlock(), errors),
                nonNegativeInt(field + ".warning-blocks", data.get("warning-blocks"),
                        defaults.warningBlocks(), errors),
                nonNegativeInt(field + ".warning-time", data.get("warning-time"),
                        defaults.warningTime(), errors));
    }

    private static List<BlockAbundanceRule> blockRules(String field, Object value, List<String> errors) {
        Map<String, Object> entries = map(field, value, errors);
        List<BlockAbundanceRule> rules = new ArrayList<>();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String path = field + "." + entry.getKey();
            identifier(path, entry.getKey(), errors);
            Map<String, Object> data = map(path, entry.getValue(), errors);
            checkKeys(path, data, Set.of("retain-chance", "replace-with"), errors);
            double chance = chance(path + ".retain-chance", data.get("retain-chance"), errors);
            String replacement = string(path + ".replace-with", data.get("replace-with"),
                    "minecraft:air", errors);
            identifier(path + ".replace-with", replacement, errors);
            rules.add(new BlockAbundanceRule(entry.getKey(), chance, replacement));
        }
        return rules;
    }

    private static List<MobAbundanceRule> mobRules(String field, Object value, List<String> errors) {
        Map<String, Object> entries = map(field, value, errors);
        List<MobAbundanceRule> rules = new ArrayList<>();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String path = field + "." + entry.getKey();
            identifier(path, entry.getKey(), errors);
            Map<String, Object> data = map(path, entry.getValue(), errors);
            checkKeys(path, data, Set.of("retain-chance"), errors);
            rules.add(new MobAbundanceRule(entry.getKey(),
                    chance(path + ".retain-chance", data.get("retain-chance"), errors)));
        }
        return rules;
    }

    private static Map<String, String> stringMap(String field, Object value, List<String> errors) {
        Map<String, Object> data = map(field, value, errors);
        Map<String, String> result = new LinkedHashMap<>();
        data.forEach((key, item) -> {
            if (item instanceof String || item instanceof Number || item instanceof Boolean) {
                result.put(key, String.valueOf(item));
            } else {
                errors.add(field + "." + key + ": expected a string, number, or boolean");
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(String field, Object value, List<String> errors) {
        if (value == null) return Map.of();
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        errors.add(field + ": expected an object");
        return Map.of();
    }

    private static String string(String field, Object value, String fallback, List<String> errors) {
        if (value == null) return fallback;
        if (value instanceof String text && !text.isBlank()) return text.trim();
        errors.add(field + ": expected a non-empty string");
        return fallback;
    }

    private static boolean bool(String field, Object value, boolean fallback, List<String> errors) {
        if (value == null) return fallback;
        if (value instanceof Boolean bool) return bool;
        errors.add(field + ": expected true or false");
        return fallback;
    }

    private static double number(String field, Object value, double fallback, List<String> errors) {
        if (value == null) return fallback;
        if (value instanceof Number number) return number.doubleValue();
        errors.add(field + ": expected a number");
        return fallback;
    }

    private static long longNumber(String field, Object value, long fallback, List<String> errors) {
        if (value == null) return fallback;
        if (value instanceof Number number) return number.longValue();
        errors.add(field + ": expected an integer");
        return fallback;
    }

    private static void integer(String field, Object value, int expected, List<String> errors) {
        if (!(value instanceof Number number) || number.intValue() != expected) {
            errors.add(field + ": expected " + expected);
        }
    }

    private static double chance(String field, Object value, List<String> errors) {
        double chance = number(field, value, 1.0, errors);
        if (chance < 0 || chance > 1) errors.add(field + ": expected 0.0 through 1.0");
        return Math.max(0, Math.min(1, chance));
    }

    private static double nonNegative(String field, Object value, double fallback, List<String> errors) {
        double number = number(field, value, fallback, errors);
        if (number < 0) errors.add(field + ": expected zero or greater");
        return Math.max(0, number);
    }

    private static int nonNegativeInt(String field, Object value, int fallback, List<String> errors) {
        long number = longNumber(field, value, fallback, errors);
        if (number < 0 || number > Integer.MAX_VALUE) errors.add(field + ": expected a non-negative integer");
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, number));
    }

    private static String difficulty(String field, Object value, String fallback, List<String> errors) {
        String result = string(field, value, fallback, errors).toUpperCase(Locale.ROOT);
        if (!Set.of("PEACEFUL", "EASY", "NORMAL", "HARD").contains(result)) {
            errors.add(field + ": expected PEACEFUL, EASY, NORMAL, or HARD");
            return fallback;
        }
        return result;
    }

    private static void identifier(String field, String value, List<String> errors) {
        if (!value.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            errors.add(field + ": expected a namespaced identifier such as elarion:community_world_1");
        }
    }

    private static void checkKeys(
            String field,
            Map<String, Object> data,
            Set<String> allowed,
            List<String> errors
    ) {
        data.keySet().stream().filter(key -> !allowed.contains(key))
                .forEach(key -> errors.add(field + "." + key + ": unknown field"));
    }
}
