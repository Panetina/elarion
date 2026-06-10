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
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.service.ElarionPerformanceMonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class WorldRuleService {
    private final Logger logger;
    private final ElarionApi api;
    private final ProcessedChunkStorage storage;
    private final WorldService worlds;
    private Map<String, Set<Long>> processedChunks = new HashMap<>();
    private final Map<String, Map<Block, ResolvedBlockRule>> resolvedBlockRuleCache = new ConcurrentHashMap<>();
    private final Map<String, ChunkSliceProgress> pendingBlockRuleChunks = new ConcurrentHashMap<>();
    private MinecraftServer server;
    private boolean dirty;
    private int saveCountdown = 200;

    public WorldRuleService(Logger logger, ElarionApi api, WorldService worlds) {
        this.logger = logger;
        this.api = api;
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
        String pendingKey = worldId + ":" + chunkKey;
        if (processed.contains(chunkKey) || pendingBlockRuleChunks.containsKey(pendingKey)) return;

        Map<Block, ResolvedBlockRule> rules = resolvedBlockRules(definition);
        if (rules.isEmpty()) {
            processed.add(chunkKey);
            dirty = true;
            return;
        }

        java.util.List<ChunkSlice> slices = slices(world);
        ChunkSliceProgress progress = new ChunkSliceProgress(slices.size());
        pendingBlockRuleChunks.put(pendingKey, progress);
        for (ChunkSlice slice : slices) {
            if (!api.system().tasks().enqueueServer("world-block-rules:" + worldId + ":" + chunkKey + ":" + slice.minY(),
                    () -> applyBlockRuleSlice(world, chunk, definition, rules, slice, pendingKey, chunkKey))) {
                pendingBlockRuleChunks.remove(pendingKey);
                ElarionPerformanceMonitor.record("world-block-rules-queue-full", 0L);
                logger.warn("Skipped queued scarcity replacement for {} chunk {} because server queue is full",
                        worldId, chunk.getPos());
                return;
            }
        }
    }

    private java.util.List<ChunkSlice> slices(ServerWorld world) {
        return slices(world.getBottomY(), world.getTopY());
    }

    static java.util.List<ChunkSlice> slices(int bottomY, int topY) {
        java.util.List<ChunkSlice> slices = new java.util.ArrayList<>();
        for (int y = bottomY; y < topY; y += 16) {
            slices.add(new ChunkSlice(y, Math.min(topY, y + 16)));
        }
        return slices;
    }

    private void applyBlockRuleSlice(
            ServerWorld world,
            WorldChunk chunk,
            ManagedWorldDefinition definition,
            Map<Block, ResolvedBlockRule> rules,
            ChunkSlice slice,
            String pendingKey,
            long chunkKey
    ) {
        long started = System.nanoTime();
        try {
            int replacements = applyBlockRules(world, chunk, definition, rules, slice);
            if (replacements > 0) {
                chunk.setNeedsSaving(true);
                ChunkSliceProgress progress = pendingBlockRuleChunks.get(pendingKey);
                if (progress != null) progress.replacements.addAndGet(replacements);
            }
            ElarionPerformanceMonitor.record("world-block-rules-slice-completed", System.nanoTime() - started);
            finishSlice(world, chunk, pendingKey, chunkKey);
        } catch (RuntimeException exception) {
            pendingBlockRuleChunks.remove(pendingKey);
            ElarionPerformanceMonitor.record("world-block-rules-slice-failed", System.nanoTime() - started);
            throw exception;
        }
    }

    private void finishSlice(ServerWorld world, WorldChunk chunk, String pendingKey, long chunkKey) {
        ChunkSliceProgress progress = pendingBlockRuleChunks.get(pendingKey);
        if (progress == null || progress.remaining.decrementAndGet() > 0) return;
        pendingBlockRuleChunks.remove(pendingKey);
        String worldId = world.getRegistryKey().getValue().toString();
        processedChunks.computeIfAbsent(worldId, ignored -> new java.util.HashSet<>()).add(chunkKey);
        dirty = true;
        if (progress.replacements.get() > 0) {
            logger.debug("Applied {} scarcity replacements in {} chunk {}",
                    progress.replacements.get(), worldId, chunk.getPos());
        }
    }

    private int applyBlockRules(
            ServerWorld world,
            WorldChunk chunk,
            ManagedWorldDefinition definition,
            Map<Block, ResolvedBlockRule> rules,
            ChunkSlice slice
    ) {
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int replacements = 0;
        for (int y = slice.minY(); y < slice.maxY(); y++) {
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
        return replacements;
    }

    private Map<Block, ResolvedBlockRule> resolvedBlockRules(ManagedWorldDefinition definition) {
        String cacheKey = definition.id() + "#" + definition.blockRules().hashCode();
        return resolvedBlockRuleCache.computeIfAbsent(cacheKey, ignored -> resolveBlockRules(definition));
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

    record ChunkSlice(int minY, int maxY) {
    }

    private static final class ChunkSliceProgress {
        private final AtomicInteger remaining;
        private final AtomicInteger replacements = new AtomicInteger();

        private ChunkSliceProgress(int slices) {
            this.remaining = new AtomicInteger(slices);
        }
    }
}
