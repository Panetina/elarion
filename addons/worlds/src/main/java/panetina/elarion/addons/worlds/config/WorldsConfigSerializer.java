package panetina.elarion.addons.worlds.config;

import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.WorldBorderDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

final class WorldsConfigSerializer {
    private WorldsConfigSerializer() {
    }

    static Map<String, Object> serialize(ManagedWorldDefinition definition) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", definition.enabled());
        result.put("id", definition.id());
        result.put("type", definition.type().name());
        result.put("template", definition.template());
        result.put("biome", definition.biome());
        result.put("platform-block", definition.platformBlock());
        result.put("platform-radius", definition.platformRadius());
        result.put("seed", definition.seed());
        result.put("difficulty", definition.difficulty());
        result.put("tick-time", definition.tickTime());
        result.put("spawn", Map.of(
                "x", definition.spawn().x(), "y", definition.spawn().y(), "z", definition.spawn().z(),
                "yaw", definition.spawn().yaw(), "pitch", definition.spawn().pitch()));
        result.put("border", serializeBorder(definition.border()));
        result.put("gamerules", new LinkedHashMap<>(definition.gameRules()));
        Map<String, Object> blocks = new LinkedHashMap<>();
        definition.blockRules().forEach(rule -> blocks.put(rule.blockId(), Map.of(
                "retain-chance", rule.retainChance(),
                "replace-with", rule.replacementBlockId())));
        result.put("block-abundance", blocks);
        Map<String, Object> mobs = new LinkedHashMap<>();
        definition.mobRules().forEach(rule -> mobs.put(rule.entityId(), Map.of(
                "retain-chance", rule.retainChance())));
        result.put("mob-abundance", mobs);
        return result;
    }

    static Map<String, Object> serializeBorder(WorldBorderDefinition border) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("center-x", border.centerX());
        result.put("center-z", border.centerZ());
        result.put("size", border.size());
        result.put("safe-zone", border.safeZone());
        result.put("damage-per-block", border.damagePerBlock());
        result.put("warning-blocks", border.warningBlocks());
        result.put("warning-time", border.warningTime());
        return result;
    }
}
