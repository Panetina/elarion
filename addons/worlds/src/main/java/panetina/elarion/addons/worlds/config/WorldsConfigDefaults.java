package panetina.elarion.addons.worlds.config;

import panetina.elarion.addons.worlds.model.WorldBorderDefinition;
import panetina.elarion.addons.worlds.model.WorldSpawn;

import java.util.LinkedHashMap;
import java.util.Map;

final class WorldsConfigDefaults {
    static final WorldSpawn SPAWN = new WorldSpawn(0.5, 80, 0.5, 0, 0);
    static final WorldBorderDefinition BORDER =
            new WorldBorderDefinition(0, 0, 10_000, 5, 0.2, 5, 15);

    static final String CONFIG = """
            # Elarion Worlds desired-state configuration.
            # Types: VOID, FLAT, OVERWORLD, NETHER, END, CAVE, CUSTOM.
            # VOID creates only the configured spawn platform. CAVE is a Nether-style enclosed cave.
            # retain-chance is 0.0 (none) through 1.0 (vanilla amount).
            config-version: 4
            lobby:
              destination: "lobby"
              enforce-for-unassigned: true
            defaults:
              type: "OVERWORLD"
              template: "minecraft:overworld"
              biome: "minecraft:plains"
              platform-block: "minecraft:stone"
              platform-radius: 5
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
              lobby:
                enabled: true
                id: "elarion:lobby"
                type: "VOID"
                difficulty: "PEACEFUL"
                tick-time: false
                spawn: { x: 0.5, y: 65.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                border: { center-x: 0.5, center-z: 0.5, size: 32.0 }
                gamerules: { doMobSpawning: false, doDaylightCycle: false, spawnChunkRadius: 0 }
                block-abundance: {}
                mob-abundance: {}
              realm_world_1:
                enabled: true
                id: "elarion:realm_world_1"
                type: "OVERWORLD"
                seed: 11001
                spawn: { x: 0.5, y: 80.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                block-abundance:
                  minecraft:iron_ore: { retain-chance: 0.25, replace-with: "minecraft:stone" }
                  minecraft:deepslate_iron_ore: { retain-chance: 0.25, replace-with: "minecraft:deepslate" }
                mob-abundance: {}
                gamerules: {}
              realm_world_2:
                enabled: true
                id: "elarion:realm_world_2"
                type: "OVERWORLD"
                seed: 22002
                spawn: { x: 0.5, y: 80.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                block-abundance:
                  minecraft:sugar_cane: { retain-chance: 0.0, replace-with: "minecraft:air" }
                mob-abundance: {}
                gamerules: {}
              realm_world_3:
                enabled: true
                id: "elarion:realm_world_3"
                type: "OVERWORLD"
                seed: 33003
                spawn: { x: 0.5, y: 80.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                block-abundance: {}
                mob-abundance: {}
                gamerules: {}
              underworld:
                enabled: true
                id: "elarion:underworld"
                type: "VOID"
                difficulty: "PEACEFUL"
                tick-time: false
                spawn: { x: 0.5, y: 80.0, z: 0.5, yaw: 0.0, pitch: 0.0 }
                border: { center-x: 0.5, center-z: 0.5, size: 256.0 }
                platform-block: "minecraft:deepslate_tiles"
                platform-radius: 12
                gamerules: { doMobSpawning: false, doDaylightCycle: false, spawnChunkRadius: 0 }
                block-abundance: {}
                mob-abundance: {}
            """;

    private WorldsConfigDefaults() {
    }

    static Map<String, Object> defaultValues() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("type", "OVERWORLD");
        defaults.put("template", "minecraft:overworld");
        defaults.put("biome", "minecraft:plains");
        defaults.put("platform-block", "minecraft:stone");
        defaults.put("platform-radius", 0);
        defaults.put("difficulty", "NORMAL");
        defaults.put("tick-time", true);
        defaults.put("border", WorldsConfigSerializer.serializeBorder(BORDER));
        return defaults;
    }

    static Map<String, Object> lobbyWorld() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("enabled", true);
        value.put("id", "elarion:lobby");
        value.put("type", "VOID");
        value.put("difficulty", "PEACEFUL");
        value.put("tick-time", false);
        value.put("spawn", Map.of("x", 0.5, "y", 65.0, "z", 0.5, "yaw", 0.0, "pitch", 0.0));
        value.put("border", Map.of("center-x", 0.5, "center-z", 0.5, "size", 32.0));
        value.put("gamerules", Map.of(
                "doMobSpawning", false, "doDaylightCycle", false, "spawnChunkRadius", 0));
        value.put("block-abundance", Map.of());
        value.put("mob-abundance", Map.of());
        return value;
    }
}
