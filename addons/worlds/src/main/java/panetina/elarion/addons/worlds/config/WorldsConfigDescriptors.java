package panetina.elarion.addons.worlds.config;

import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.WorldType;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class WorldsConfigDescriptors {
    private WorldsConfigDescriptors() {
    }

    public static void register(ElarionConfigRegistry registry, WorldsConfigManager config) {
        registry.registerDomain(domain(config));
    }

    public static ElarionConfigDomain domain(WorldsConfigManager config) {
        Map<String, ManagedWorldDefinition> snapshot = config.worlds();
        return new ElarionConfigDomain(
                "worlds",
                "addons:worlds",
                "Worlds",
                "Managed world routing, lobby, and configured world definitions.",
                List.of("config/elarion/addons/worlds/worlds.yml"),
                "/e world reload",
                List.of(
                        new ElarionConfigCategory(
                                "general",
                                "General",
                                "Worlds schema, lobby routing, and current world list.",
                                List.of(
                                        intEntry("config-version", "Config Version",
                                                "Worlds config schema version.",
                                                "worlds.yml.config-version",
                                                WorldsConfigManager.CONFIG_VERSION,
                                                () -> WorldsConfigManager.CONFIG_VERSION,
                                                1,
                                                WorldsConfigManager.CONFIG_VERSION,
                                                false),
                                        stringEntry("lobby.destination", "Lobby Destination",
                                                "Destination used for unassigned players and fallback routing.",
                                                "worlds.yml.lobby.destination",
                                                "lobby",
                                                config::lobbyDestination),
                                        boolEntry("lobby.enforce-for-unassigned", "Enforce Lobby",
                                                "Routes Realm-less players back to the configured lobby destination.",
                                                "worlds.yml.lobby.enforce-for-unassigned",
                                                true,
                                                config::enforceLobby),
                                        intEntry("worlds.count", "World Count",
                                                "Number of currently loaded managed-world definitions.",
                                                "worlds.yml.worlds",
                                                snapshot.size(),
                                                () -> config.worlds().size(),
                                                0,
                                                Integer.MAX_VALUE,
                                                false),
                                        stringEntry("worlds.keys", "World Keys",
                                                "Comma-separated managed-world keys currently known to Worlds.",
                                                "worlds.yml.worlds",
                                                keys(snapshot),
                                                () -> keys(config.worlds()),
                                                false))),
                        new ElarionConfigCategory(
                                "worlds",
                                "World Definitions",
                                "Current loaded managed-world definition summaries.",
                                worldEntries(config, snapshot))));
    }

    private static List<ElarionConfigEntry<?>> worldEntries(
            WorldsConfigManager config,
            Map<String, ManagedWorldDefinition> snapshot
    ) {
        List<ManagedWorldDefinition> worlds = snapshot.values().stream()
                .sorted(Comparator.comparing(ManagedWorldDefinition::key))
                .toList();
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        for (ManagedWorldDefinition world : worlds) {
            entries.add(worldBoolEntry(world, "enabled", "Enabled",
                    "Whether this managed world should be opened automatically.",
                    config, ManagedWorldDefinition::enabled));
            entries.add(worldStringEntry(world, "id", "World ID",
                    "Minecraft world registry ID for this managed world.",
                    config, ManagedWorldDefinition::id));
            entries.add(worldStringEntry(world, "type", "Type",
                    "World generation type.",
                    config, value -> value.type().name(), worldTypeChoices()));
            entries.add(worldStringEntry(world, "template", "Template",
                    "Template dimension used for generator and dimension settings.",
                    config, ManagedWorldDefinition::template));
            entries.add(worldStringEntry(world, "biome", "Biome",
                    "Configured biome identifier.",
                    config, ManagedWorldDefinition::biome));
            entries.add(worldStringEntry(world, "platform-block", "Platform Block",
                    "Block used for generated spawn platforms.",
                    config, ManagedWorldDefinition::platformBlock));
            entries.add(worldIntEntry(world, "platform-radius", "Platform Radius",
                    "Generated platform radius for void-style worlds.",
                    config, ManagedWorldDefinition::platformRadius, 0, Integer.MAX_VALUE));
            entries.add(worldLongEntry(world, "seed", "Seed",
                    "Configured runtime-world seed.",
                    config, ManagedWorldDefinition::seed));
            entries.add(worldStringEntry(world, "difficulty", "Difficulty",
                    "Configured world difficulty.",
                    config, ManagedWorldDefinition::difficulty,
                    List.of("PEACEFUL", "EASY", "NORMAL", "HARD")));
            entries.add(worldBoolEntry(world, "tick-time", "Tick Time",
                    "Whether this world should advance time.",
                    config, ManagedWorldDefinition::tickTime));
            entries.add(worldStringEntry(world, "spawn", "Spawn",
                    "Configured spawn position and rotation.",
                    config, WorldsConfigDescriptors::spawnSummary, false));
            entries.add(worldStringEntry(world, "border", "Border",
                    "Configured world border summary.",
                    config, WorldsConfigDescriptors::borderSummary, false));
            entries.add(worldIntEntry(world, "gamerules.count", "Game Rule Count",
                    "Number of configured game rules.",
                    config, value -> value.gameRules().size(), 0, Integer.MAX_VALUE));
            entries.add(worldIntEntry(world, "block-abundance.count", "Block Rule Count",
                    "Number of configured block-abundance rules.",
                    config, value -> value.blockRules().size(), 0, Integer.MAX_VALUE));
            entries.add(worldIntEntry(world, "mob-abundance.count", "Mob Rule Count",
                    "Number of configured mob-abundance rules.",
                    config, value -> value.mobRules().size(), 0, Integer.MAX_VALUE));
        }
        return entries;
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id,
            String label,
            String description,
            String path,
            boolean defaultValue,
            Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum,
            int maximum,
            boolean runtimeReloadable
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerRange(path, minimum, maximum), List.of(),
                Integer.toString(minimum), maximum == Integer.MAX_VALUE ? "" : Integer.toString(maximum),
                runtimeReloadable, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Long> longEntry(
            String id,
            String label,
            String description,
            String path,
            long defaultValue,
            Supplier<Long> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.LONG, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of(), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue
    ) {
        return stringEntry(id, label, description, path, defaultValue, currentValue, true);
    }

    private static ElarionConfigEntry<String> stringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                nonBlank ? ElarionConfigValidator.nonBlank(path) : ElarionConfigValidator.pass(),
                List.of(), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Boolean> worldBoolEntry(
            ManagedWorldDefinition world,
            String field,
            String label,
            String description,
            WorldsConfigManager config,
            Function<ManagedWorldDefinition, Boolean> value
    ) {
        return boolEntry(worldId(world, field), worldLabel(world, label), description,
                worldPath(world, field), value.apply(world),
                () -> value.apply(currentWorld(config, world)));
    }

    private static ElarionConfigEntry<Integer> worldIntEntry(
            ManagedWorldDefinition world,
            String field,
            String label,
            String description,
            WorldsConfigManager config,
            Function<ManagedWorldDefinition, Integer> value,
            int minimum,
            int maximum
    ) {
        return intEntry(worldId(world, field), worldLabel(world, label), description,
                worldPath(world, field), value.apply(world),
                () -> value.apply(currentWorld(config, world)), minimum, maximum, true);
    }

    private static ElarionConfigEntry<Long> worldLongEntry(
            ManagedWorldDefinition world,
            String field,
            String label,
            String description,
            WorldsConfigManager config,
            Function<ManagedWorldDefinition, Long> value
    ) {
        return longEntry(worldId(world, field), worldLabel(world, label), description,
                worldPath(world, field), value.apply(world),
                () -> value.apply(currentWorld(config, world)));
    }

    private static ElarionConfigEntry<String> worldStringEntry(
            ManagedWorldDefinition world,
            String field,
            String label,
            String description,
            WorldsConfigManager config,
            Function<ManagedWorldDefinition, String> value
    ) {
        return worldStringEntry(world, field, label, description, config, value, List.of(), true);
    }

    private static ElarionConfigEntry<String> worldStringEntry(
            ManagedWorldDefinition world,
            String field,
            String label,
            String description,
            WorldsConfigManager config,
            Function<ManagedWorldDefinition, String> value,
            boolean nonBlank
    ) {
        return worldStringEntry(world, field, label, description, config, value, List.of(), nonBlank);
    }

    private static ElarionConfigEntry<String> worldStringEntry(
            ManagedWorldDefinition world,
            String field,
            String label,
            String description,
            WorldsConfigManager config,
            Function<ManagedWorldDefinition, String> value,
            List<String> choices
    ) {
        return worldStringEntry(world, field, label, description, config, value, choices, true);
    }

    private static ElarionConfigEntry<String> worldStringEntry(
            ManagedWorldDefinition world,
            String field,
            String label,
            String description,
            WorldsConfigManager config,
            Function<ManagedWorldDefinition, String> value,
            List<String> choices,
            boolean nonBlank
    ) {
        return new ElarionConfigEntry<>(
                worldId(world, field),
                worldLabel(world, label),
                description,
                worldPath(world, field),
                ElarionConfigCodec.STRING,
                value.apply(world),
                () -> value.apply(currentWorld(config, world)),
                nonBlank ? ElarionConfigValidator.nonBlank(worldPath(world, field)) : ElarionConfigValidator.pass(),
                choices,
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ManagedWorldDefinition currentWorld(WorldsConfigManager config, ManagedWorldDefinition fallback) {
        ManagedWorldDefinition current = config.worlds().get(fallback.key());
        return current == null ? fallback : current;
    }

    private static String worldId(ManagedWorldDefinition world, String field) {
        return "worlds." + world.key() + "." + field;
    }

    private static String worldPath(ManagedWorldDefinition world, String field) {
        return "worlds.yml.worlds." + world.key() + "." + field;
    }

    private static String worldLabel(ManagedWorldDefinition world, String fieldLabel) {
        return world.key() + " " + fieldLabel;
    }

    private static String spawnSummary(ManagedWorldDefinition world) {
        return world.spawn().x() + ", " + world.spawn().y() + ", " + world.spawn().z()
                + " yaw=" + world.spawn().yaw() + " pitch=" + world.spawn().pitch();
    }

    private static String borderSummary(ManagedWorldDefinition world) {
        return "center=" + world.border().centerX() + "," + world.border().centerZ()
                + " size=" + world.border().size()
                + " safe=" + world.border().safeZone()
                + " damage=" + world.border().damagePerBlock();
    }

    private static String keys(Map<String, ManagedWorldDefinition> worlds) {
        return worlds.keySet().stream().sorted().reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static List<String> worldTypeChoices() {
        List<String> choices = new ArrayList<>();
        for (WorldType type : WorldType.values()) {
            choices.add(type.name());
        }
        return choices;
    }
}
