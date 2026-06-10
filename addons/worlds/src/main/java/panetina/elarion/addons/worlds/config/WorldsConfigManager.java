package panetina.elarion.addons.worlds.config;

import org.slf4j.Logger;
import panetina.elarion.addons.worlds.model.BlockAbundanceRule;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.MobAbundanceRule;
import panetina.elarion.addons.worlds.model.WorldBorderDefinition;
import panetina.elarion.addons.worlds.model.WorldSpawn;
import panetina.elarion.addons.worlds.model.WorldType;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
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
    public static final int CONFIG_VERSION = 4;

    private final Logger logger;
    private final Path path;
    private volatile Map<String, ManagedWorldDefinition> worlds = Map.of();
    private volatile String lobbyDestination = "lobby";
    private volatile boolean enforceLobby;

    public WorldsConfigManager(Logger logger) {
        this(logger, AddonConfigFiles.writeDefault("worlds", "worlds.yml", WorldsConfigDefaults.CONFIG));
    }

    public WorldsConfigManager(Logger logger, Path path) {
        this.logger = logger;
        this.path = path;
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) Files.writeString(path, WorldsConfigDefaults.CONFIG, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create Worlds config " + path, exception);
        }
    }

    public synchronized void load() {
        Map<String, Object> root = WorldsConfigIo.readRoot(path, "worlds.yml");
        migrate(root);
        root = WorldsConfigIo.readRoot(path, "worlds.yml");
        List<String> errors = new ArrayList<>();
        checkKeys("worlds.yml", root, Set.of(
                "config-version", "lobby", "defaults", "worlds"), errors);
        integer("worlds.yml.config-version", root.get("config-version"), CONFIG_VERSION, errors);

        Map<String, Object> defaults = map("worlds.yml.defaults", root.get("defaults"), errors);
        checkKeys("worlds.yml.defaults", defaults, Set.of(
                "type", "template", "biome", "platform-block", "platform-radius",
                "difficulty", "tick-time", "border"), errors);
        WorldType defaultType = worldType("worlds.yml.defaults.type", defaults.get("type"),
                WorldType.OVERWORLD, errors);
        String defaultTemplate = string("worlds.yml.defaults.template", defaults.get("template"),
                "minecraft:overworld", errors);
        String defaultBiome = string("worlds.yml.defaults.biome", defaults.get("biome"),
                "minecraft:plains", errors);
        String defaultPlatform = string("worlds.yml.defaults.platform-block", defaults.get("platform-block"),
                "minecraft:stone", errors);
        int defaultPlatformRadius = nonNegativeInt("worlds.yml.defaults.platform-radius",
                defaults.get("platform-radius"), 0, errors);
        String defaultDifficulty = difficulty("worlds.yml.defaults.difficulty", defaults.get("difficulty"),
                "NORMAL", errors);
        boolean defaultTickTime = bool("worlds.yml.defaults.tick-time", defaults.get("tick-time"), true, errors);
        WorldBorderDefinition defaultBorder = border("worlds.yml.defaults.border",
                map("worlds.yml.defaults.border", defaults.get("border"), errors), WorldsConfigDefaults.BORDER, errors);

        identifier("worlds.yml.defaults.template", defaultTemplate, errors);
        identifier("worlds.yml.defaults.biome", defaultBiome, errors);
        identifier("worlds.yml.defaults.platform-block", defaultPlatform, errors);

        Map<String, Object> lobby = map("worlds.yml.lobby", root.get("lobby"), errors);
        checkKeys("worlds.yml.lobby", lobby, Set.of("destination", "enforce-for-unassigned"), errors);
        String parsedLobbyDestination = string("worlds.yml.lobby.destination", lobby.get("destination"),
                "lobby", errors);
        boolean parsedEnforceLobby = bool("worlds.yml.lobby.enforce-for-unassigned",
                lobby.get("enforce-for-unassigned"), false, errors);

        Map<String, ManagedWorldDefinition> parsedWorlds = parseWorlds(
                "worlds.yml.worlds", root.get("worlds"), defaultType, defaultTemplate, defaultBiome,
                defaultPlatform, defaultPlatformRadius, defaultDifficulty, defaultTickTime, defaultBorder, errors);

        if (!errors.isEmpty()) throw new WorldsConfigException(errors);
        worlds = Map.copyOf(parsedWorlds);
        lobbyDestination = parsedLobbyDestination;
        enforceLobby = parsedEnforceLobby;
        logger.info("Loaded {} Elarion worlds", worlds.size());
    }

    public synchronized ManagedWorldDefinition create(String key, WorldType type, long seed) {
        if (!key.matches("[a-z0-9_.-]+")) {
            throw new IllegalArgumentException("World name may contain lowercase letters, numbers, _, - and . only");
        }
        if (worlds.containsKey(key)) throw new IllegalArgumentException("World already exists: " + key);
        ManagedWorldDefinition definition = defaultsFor(key, type, seed);
        Map<String, ManagedWorldDefinition> updated = new LinkedHashMap<>(worlds);
        updated.put(key, definition);
        saveWorlds(updated);
        worlds = Map.copyOf(updated);
        return definition;
    }

    public synchronized ManagedWorldDefinition remove(String name) {
        ManagedWorldDefinition definition = worlds.get(name);
        if (definition == null) {
            definition = worlds.values().stream()
                    .filter(item -> item.id().equals(name)).findFirst().orElse(null);
        }
        if (definition == null || definition.key().equals("lobby")) return null;
        Map<String, ManagedWorldDefinition> updated = new LinkedHashMap<>(worlds);
        updated.remove(definition.key());
        saveWorlds(updated);
        worlds = Map.copyOf(updated);
        return definition;
    }

    public synchronized void updateBorder(String worldId, WorldBorderDefinition border) {
        ManagedWorldDefinition current = worlds.values().stream()
                .filter(definition -> definition.id().equals(worldId))
                .findFirst()
                .orElse(null);
        if (current == null || current.border().equals(border)) return;
        ManagedWorldDefinition updatedDefinition = new ManagedWorldDefinition(
                current.key(), current.enabled(), current.id(), current.type(), current.template(),
                current.biome(), current.platformBlock(), current.platformRadius(), current.seed(),
                current.difficulty(), current.tickTime(), current.spawn(), border, current.gameRules(),
                current.blockRules(), current.mobRules());
        Map<String, ManagedWorldDefinition> updated = new LinkedHashMap<>(worlds);
        updated.put(current.key(), updatedDefinition);
        saveWorlds(updated);
        worlds = Map.copyOf(updated);
    }

    public Map<String, ManagedWorldDefinition> worlds() { return worlds; }

    public String lobbyDestination() {
        return lobbyDestination;
    }

    public boolean enforceLobby() {
        return enforceLobby;
    }

    public void restore(
            Map<String, ManagedWorldDefinition> previousWorlds,
            String lobby,
            boolean enforce
    ) {
        worlds = Map.copyOf(previousWorlds);
        lobbyDestination = lobby;
        enforceLobby = enforce;
    }

    public Path path() {
        return path;
    }

    private void migrate(Map<String, Object> root) {
        Object versionValue = root.get("config-version");
        if (!(versionValue instanceof Number number) || number.intValue() == CONFIG_VERSION) return;
        if (number.intValue() < 1 || number.intValue() > 3) return;

        Map<String, Object> migrated = WorldsConfigIo.deepMutableMap(root);
        migrated.put("config-version", CONFIG_VERSION);

        Map<String, Object> defaults = WorldsConfigIo.mutableChild(migrated, "defaults");
        defaults.putIfAbsent("type", "OVERWORLD");
        defaults.putIfAbsent("template", "minecraft:overworld");
        defaults.putIfAbsent("biome", "minecraft:plains");
        defaults.putIfAbsent("platform-block", "minecraft:stone");
        defaults.putIfAbsent("platform-radius", 0);

        Map<String, Object> lobby = WorldsConfigIo.mutableChild(migrated, "lobby");
        lobby.putIfAbsent("destination", "lobby");
        lobby.putIfAbsent("enforce-for-unassigned", true);

        migrated.remove("destinations");
        migrated.remove("existing-worlds");

        Map<String, Object> worlds = WorldsConfigIo.mutableChild(migrated, "worlds");
        for (Map.Entry<String, Object> entry : worlds.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> rawWorld) {
                Map<String, Object> world = WorldsConfigIo.deepMutableMap(rawWorld);
                world.putIfAbsent("type", inferType(world.get("template")));
                entry.setValue(world);
            }
        }
        worlds.putIfAbsent("lobby", WorldsConfigDefaults.lobbyWorld());

        Path legacyCreated = path.resolveSibling("created-worlds.yml");
        if (Files.exists(legacyCreated)) {
            Map<String, Object> createdRoot = WorldsConfigIo.readRoot(legacyCreated, "created-worlds.yml");
            Map<String, Object> created = WorldsConfigIo.mutableChild(createdRoot, "worlds");
            created.forEach(worlds::putIfAbsent);
        }

        Path backup = path.resolveSibling(path.getFileName() + ".bak-v" + number.intValue());
        try {
            if (Files.notExists(backup)) Files.copy(path, backup);
            WorldsConfigIo.writeYaml(path, migrated);
            if (Files.exists(legacyCreated)) Files.delete(legacyCreated);
            logger.info("Migrated {} from schema {} to schema {}; backup saved to {}",
                    path, number.intValue(), CONFIG_VERSION, backup);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to migrate " + path, exception);
        }
    }

    private static Map<String, ManagedWorldDefinition> parseWorlds(
            String field,
            Object value,
            WorldType defaultType,
            String defaultTemplate,
            String defaultBiome,
            String defaultPlatform,
            int defaultPlatformRadius,
            String defaultDifficulty,
            boolean defaultTickTime,
            WorldBorderDefinition defaultBorder,
            List<String> errors
    ) {
        Map<String, Object> configured = map(field, value, errors);
        Map<String, ManagedWorldDefinition> parsed = new LinkedHashMap<>();
        Set<String> worldIds = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry : configured.entrySet()) {
            String key = entry.getKey();
            String itemField = field + "." + key;
            Map<String, Object> data = map(itemField, entry.getValue(), errors);
            checkKeys(itemField, data, Set.of(
                    "enabled", "id", "type", "template", "biome", "platform-block", "platform-radius",
                    "seed", "difficulty", "tick-time", "spawn", "border",
                    "gamerules", "block-abundance", "mob-abundance"), errors);

            String id = string(itemField + ".id", data.get("id"), "elarion:" + key, errors);
            identifier(itemField + ".id", id, errors);
            if (!worldIds.add(id.toLowerCase(Locale.ROOT))) {
                errors.add(itemField + ".id: duplicate world id " + id);
            }
            WorldType type = worldType(itemField + ".type", data.get("type"), defaultType, errors);
            String template = string(itemField + ".template", data.get("template"),
                    templateFor(type, defaultTemplate), errors);
            String biome = string(itemField + ".biome", data.get("biome"), defaultBiome, errors);
            String platform = string(itemField + ".platform-block", data.get("platform-block"),
                    defaultPlatform, errors);
            identifier(itemField + ".template", template, errors);
            identifier(itemField + ".biome", biome, errors);
            identifier(itemField + ".platform-block", platform, errors);

            parsed.put(key, new ManagedWorldDefinition(
                    key,
                    bool(itemField + ".enabled", data.get("enabled"), true, errors),
                    id,
                    type,
                    template,
                    biome,
                    platform,
                    nonNegativeInt(itemField + ".platform-radius", data.get("platform-radius"),
                            defaultPlatformRadius, errors),
                    longNumber(itemField + ".seed", data.get("seed"), key.hashCode(), errors),
                    difficulty(itemField + ".difficulty", data.get("difficulty"), defaultDifficulty, errors),
                    bool(itemField + ".tick-time", data.get("tick-time"), defaultTickTime, errors),
                    spawn(itemField + ".spawn", map(itemField + ".spawn", data.get("spawn"), errors), errors),
                    data.containsKey("border")
                            ? border(itemField + ".border", map(itemField + ".border", data.get("border"), errors),
                            defaultBorder, errors)
                            : defaultBorder,
                    stringMap(itemField + ".gamerules", data.get("gamerules"), errors),
                    blockRules(itemField + ".block-abundance", data.get("block-abundance"), errors),
                    mobRules(itemField + ".mob-abundance", data.get("mob-abundance"), errors)));
        }
        return parsed;
    }

    private ManagedWorldDefinition defaultsFor(String key, WorldType type, long seed) {
        WorldSpawn spawn = type == WorldType.VOID
                ? new WorldSpawn(0.5, 65, 0.5, 0, 0)
                : WorldsConfigDefaults.SPAWN;
        return new ManagedWorldDefinition(
                key, true, "elarion:" + key, type, templateFor(type, "minecraft:overworld"),
                "minecraft:plains", "minecraft:stone", 0, seed, "NORMAL", true,
                spawn, WorldsConfigDefaults.BORDER, Map.of(), List.of(), List.of());
    }

    private void saveWorlds(Map<String, ManagedWorldDefinition> worlds) {
        Map<String, Object> serialized = new LinkedHashMap<>();
        serialized.put("config-version", CONFIG_VERSION);
        serialized.put("lobby", Map.of(
                "destination", lobbyDestination,
                "enforce-for-unassigned", enforceLobby));
        serialized.put("defaults", WorldsConfigDefaults.defaultValues());
        Map<String, Object> entries = new LinkedHashMap<>();
        worlds.forEach((key, definition) -> entries.put(key, WorldsConfigSerializer.serialize(definition)));
        serialized.put("worlds", entries);
        try {
            WorldsConfigIo.writeYaml(path, serialized);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save " + path, exception);
        }
    }

    private static String inferType(Object template) {
        if ("minecraft:the_nether".equals(template)) return "NETHER";
        if ("minecraft:the_end".equals(template)) return "END";
        return "OVERWORLD";
    }

    private static String templateFor(WorldType type, String customFallback) {
        return switch (type) {
            case NETHER, CAVE -> "minecraft:the_nether";
            case END -> "minecraft:the_end";
            case CUSTOM -> customFallback;
            default -> "minecraft:overworld";
        };
    }

    private static WorldType worldType(String field, Object value, WorldType fallback, List<String> errors) {
        if (value == null) return fallback;
        try {
            return WorldType.parse(String.valueOf(value));
        } catch (IllegalArgumentException exception) {
            errors.add(field + ": expected VOID, FLAT, OVERWORLD, NETHER, END, CAVE, or CUSTOM");
            return fallback;
        }
    }

    private static WorldSpawn spawn(String field, Map<String, Object> data, List<String> errors) {
        checkKeys(field, data, Set.of("x", "y", "z", "yaw", "pitch"), errors);
        return new WorldSpawn(
                number(field + ".x", data.get("x"), WorldsConfigDefaults.SPAWN.x(), errors),
                number(field + ".y", data.get("y"), WorldsConfigDefaults.SPAWN.y(), errors),
                number(field + ".z", data.get("z"), WorldsConfigDefaults.SPAWN.z(), errors),
                (float) number(field + ".yaw", data.get("yaw"), WorldsConfigDefaults.SPAWN.yaw(), errors),
                (float) number(field + ".pitch", data.get("pitch"), WorldsConfigDefaults.SPAWN.pitch(), errors));
    }

    private static WorldBorderDefinition border(
            String field, Map<String, Object> data, WorldBorderDefinition fallback, List<String> errors
    ) {
        checkKeys(field, data, Set.of(
                "center-x", "center-z", "size", "safe-zone", "damage-per-block",
                "warning-blocks", "warning-time"), errors);
        double size = number(field + ".size", data.get("size"), fallback.size(), errors);
        if (size < 1 || size > 59_999_968) errors.add(field + ".size: expected 1 through 59999968");
        return new WorldBorderDefinition(
                number(field + ".center-x", data.get("center-x"), fallback.centerX(), errors),
                number(field + ".center-z", data.get("center-z"), fallback.centerZ(), errors),
                size,
                nonNegative(field + ".safe-zone", data.get("safe-zone"), fallback.safeZone(), errors),
                nonNegative(field + ".damage-per-block", data.get("damage-per-block"),
                        fallback.damagePerBlock(), errors),
                nonNegativeInt(field + ".warning-blocks", data.get("warning-blocks"),
                        fallback.warningBlocks(), errors),
                nonNegativeInt(field + ".warning-time", data.get("warning-time"),
                        fallback.warningTime(), errors));
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
            errors.add(field + ": expected a namespaced identifier such as elarion:realm_world_1");
        }
    }

    private static void checkKeys(
            String field, Map<String, Object> data, Set<String> allowed, List<String> errors
    ) {
        data.keySet().stream().filter(key -> !allowed.contains(key))
                .forEach(key -> errors.add(field + "." + key + ": unknown field"));
    }
}
