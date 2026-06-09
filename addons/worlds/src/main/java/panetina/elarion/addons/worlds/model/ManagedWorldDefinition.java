package panetina.elarion.addons.worlds.model;

import java.util.List;
import java.util.Map;

public record ManagedWorldDefinition(
        String key,
        boolean enabled,
        String id,
        WorldType type,
        String template,
        String biome,
        String platformBlock,
        int platformRadius,
        long seed,
        String difficulty,
        boolean tickTime,
        WorldSpawn spawn,
        WorldBorderDefinition border,
        Map<String, String> gameRules,
        List<BlockAbundanceRule> blockRules,
        List<MobAbundanceRule> mobRules
) {
    public ManagedWorldDefinition {
        gameRules = Map.copyOf(gameRules);
        blockRules = List.copyOf(blockRules);
        mobRules = List.copyOf(mobRules);
    }
}
