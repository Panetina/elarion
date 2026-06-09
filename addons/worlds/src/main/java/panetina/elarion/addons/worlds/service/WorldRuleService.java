package panetina.elarion.addons.worlds.service;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import panetina.elarion.addons.worlds.model.BlockAbundanceRule;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.MobAbundanceRule;
import panetina.elarion.addons.worlds.storage.ProcessedChunkStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class WorldRuleService {
    private final Logger logger;
    private final ProcessedChunkStorage storage;
    private final WorldService worlds;
    private Map<String, Set<Long>> processedChunks = new HashMap<>();
    private MinecraftServer server;
    private boolean dirty;
    private int saveCountdown = 200;

    public WorldRuleService(Logger logger, WorldService worlds) {
        this.logger = logger;
        this.worlds = worlds;
        this.storage = new ProcessedChunkStorage(logger);
    }

    public void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            processedChunks = storage.load(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> storage.save(server, processedChunks));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!dirty || --saveCountdown > 0) return;
            storage.save(server, processedChunks);
            dirty = false;
            saveCountdown = 200;
        });
        ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof MobEntity mob && mob.age >= 0 && mob.age <= 1) applyMobRule(world, mob);
        });
    }

    private void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        ManagedWorldDefinition definition = worlds.definition(world);
        if (definition == null || definition.blockRules().isEmpty()) return;

        String worldId = world.getRegistryKey().getValue().toString();
        Set<Long> processed = processedChunks.computeIfAbsent(worldId, ignored -> new java.util.HashSet<>());
        long chunkKey = chunk.getPos().toLong();
        if (!processed.add(chunkKey)) return;
        dirty = true;

        Map<Block, ResolvedBlockRule> rules = resolveBlockRules(definition);
        if (rules.isEmpty()) return;

        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int replacements = 0;
        for (int y = world.getBottomY(); y < world.getTopY(); y++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    pos.set(startX + localX, y, startZ + localZ);
                    BlockState current = chunk.getBlockState(pos);
                    ResolvedBlockRule rule = rules.get(current.getBlock());
                    if (rule == null || AbundanceSelector.keep(
                            definition.seed(), pos.asLong(), rule.salt(), rule.chance())) continue;
                    chunk.setBlockState(pos, rule.replacement(), false);
                    replacements++;
                }
            }
        }
        if (replacements > 0) {
            chunk.setNeedsSaving(true);
            logger.debug("Applied {} scarcity replacements in {} chunk {}", replacements, worldId, chunk.getPos());
        }
    }

    private Map<Block, ResolvedBlockRule> resolveBlockRules(ManagedWorldDefinition definition) {
        Map<Block, ResolvedBlockRule> resolved = new HashMap<>();
        for (BlockAbundanceRule rule : definition.blockRules()) {
            Identifier blockId = Identifier.tryParse(rule.blockId());
            Identifier replacementId = Identifier.tryParse(rule.replacementBlockId());
            if (blockId == null || replacementId == null
                    || !Registries.BLOCK.containsId(blockId) || !Registries.BLOCK.containsId(replacementId)) {
                logger.error("Skipping invalid block abundance rule {} -> {} in {}",
                        rule.blockId(), rule.replacementBlockId(), definition.id());
                continue;
            }
            resolved.put(Registries.BLOCK.get(blockId), new ResolvedBlockRule(
                    rule.retainChance(),
                    Registries.BLOCK.get(replacementId).getDefaultState(),
                    blockId.hashCode()));
        }
        return resolved;
    }

    private void applyMobRule(ServerWorld world, MobEntity mob) {
        ManagedWorldDefinition definition = worlds.definition(world);
        if (definition == null || definition.mobRules().isEmpty()) return;
        Identifier entityId = EntityType.getId(mob.getType());
        for (MobAbundanceRule rule : definition.mobRules()) {
            if (!rule.entityId().equals(entityId.toString())) continue;
            long seed = definition.seed() ^ mob.getUuid().getMostSignificantBits()
                    ^ mob.getUuid().getLeastSignificantBits();
            if (AbundanceSelector.unit(seed) >= rule.retainChance()) mob.discard();
            return;
        }
    }

    private record ResolvedBlockRule(double chance, BlockState replacement, int salt) {
    }
}
