package panetina.elarion.addons.optimization;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.service.ElarionTaskService;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PerformanceSampler {
    private final ElarionApi api;
    private final BlockEntityLoadTracker blockEntities = new BlockEntityLoadTracker();
    private final WorldTrendTracker trends = new WorldTrendTracker();
    private long ticksUntilSample;
    private volatile Sample sample = Sample.empty();

    PerformanceSampler(ElarionApi api) {
        this.api = api;
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(blockEntities::loaded);
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(blockEntities::unloaded);
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    Sample sample() {
        return sample;
    }

    private void tick(MinecraftServer server) {
        int intervalTicks = Math.max(20, api.system().tasks().snapshot().sampleIntervalSeconds() * 20);
        if (ticksUntilSample-- > 0) {
            return;
        }
        ticksUntilSample = intervalTicks;
        ElarionApi api = this.api;
        if (!api.system().tasks().snapshot().worldSamplesEnabled()
                && !api.system().tasks().snapshot().realmSamplesEnabled()) {
            sample = Sample.empty();
            return;
        }
        sample = collect(server, api, sample, blockEntities, trends);
    }

    private static Sample collect(
            MinecraftServer server,
            ElarionApi api,
            Sample previous,
            BlockEntityLoadTracker blockEntities,
            WorldTrendTracker trends
    ) {
        ElarionTaskService.Snapshot taskSnapshot = api.system().tasks().snapshot();
        long averageTickNanos = Math.round(server.getAverageTickTime() * 1_000_000.0D);
        Headroom headroom = Headroom.from(averageTickNanos, taskSnapshot);
        List<WorldSample> worlds = new ArrayList<>();
        if (taskSnapshot.worldSamplesEnabled()) {
            Map<String, WorldSample> previousWorlds = worldsById(previous);
            for (ServerWorld world : server.getWorlds()) {
                String worldId = world.getRegistryKey().getValue().toString();
                int loadedChunks = world.getChunkManager().getLoadedChunkCount();
                EntitySummary entities = countEntities(world);
                BlockEntityLoadTracker.Snapshot blockEntitySnapshot = blockEntities.snapshot(worldId);
                WorldTrendTracker.Trend trend = trends.record(
                        worldId, loadedChunks, entities.total(), blockEntitySnapshot.total());
                WorldSample old = previousWorlds.get(worldId);
                worlds.add(new WorldSample(
                        worldId,
                        world.getPlayers().size(),
                        loadedChunks,
                        entities.total(),
                        entities.groups(),
                        blockEntitySnapshot.total(),
                        blockEntitySnapshot.groups(),
                        old == null ? 0 : loadedChunks - old.loadedChunks(),
                        old == null ? 0 : entities.total() - old.entities(),
                        old == null ? 0 : blockEntitySnapshot.total() - old.blockEntities(),
                        trend.samples(),
                        trend.loadedChunkTrend(),
                        trend.entityTrend(),
                        trend.blockEntityTrend()
                ));
            }
        }

        List<RealmSample> realms = new ArrayList<>();
        if (taskSnapshot.realmSamplesEnabled()) {
            Map<String, Integer> playersByRealm = new LinkedHashMap<>();
            for (RealmDefinition realm : api.realm().realms().all()) {
                playersByRealm.put(realm.id(), 0);
            }
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                CitizenRecord citizen = api.realm().citizens().getOrCreate(player);
                if (citizen.realmId() != null && playersByRealm.containsKey(citizen.realmId())) {
                    playersByRealm.computeIfPresent(citizen.realmId(), (ignored, count) -> count + 1);
                }
            }
            for (RealmDefinition realm : api.realm().realms().all()) {
                String worldId = realm.spawn() == null ? "(none)" : realm.spawn().worldId();
                realms.add(new RealmSample(realm.id(), worldId, playersByRealm.getOrDefault(realm.id(), 0)));
            }
        }

        return new Sample(System.currentTimeMillis(), averageTickNanos, headroom, worlds, realms);
    }

    private static Map<String, WorldSample> worldsById(Sample previous) {
        Map<String, WorldSample> values = new LinkedHashMap<>();
        for (WorldSample world : previous.worlds()) {
            values.put(world.worldId(), world);
        }
        return values;
    }

    private static EntitySummary countEntities(ServerWorld world) {
        int count = 0;
        Map<String, Integer> groups = new LinkedHashMap<>();
        for (Entity entity : world.iterateEntities()) {
            count++;
            SpawnGroup group = entity.getType().getSpawnGroup();
            String key = group == null ? "unknown" : group.getName();
            groups.merge(key, 1, Integer::sum);
        }
        return new EntitySummary(count, Map.copyOf(groups));
    }

    record Sample(long timestamp, long averageTickNanos, Headroom headroom,
                  List<WorldSample> worlds, List<RealmSample> realms) {
        static Sample empty() {
            return new Sample(0L, 0L, Headroom.UNKNOWN, List.of(), List.of());
        }

        double averageTickMillis() {
            return averageTickNanos / 1_000_000.0D;
        }
    }

    record WorldSample(String worldId, int players, int loadedChunks, int entities,
                       Map<String, Integer> entityGroups, int blockEntities,
                       Map<String, Integer> blockEntityTypes, int loadedChunkDelta,
                       int entityDelta, int blockEntityDelta, int trendSamples,
                       int loadedChunkTrend, int entityTrend, int blockEntityTrend) {
    }

    private record EntitySummary(int total, Map<String, Integer> groups) {
    }

    record RealmSample(String realmId, String worldId, int onlinePlayers) {
    }

    enum Headroom {
        UNKNOWN,
        HEALTHY,
        WARM,
        PRESSURE,
        OVERLOADED;

        static Headroom from(long averageTickNanos, ElarionTaskService.Snapshot snapshot) {
            if (averageTickNanos <= 0L) {
                return UNKNOWN;
            }
            if (averageTickNanos >= snapshot.headroomOverloadedNanos()) {
                return OVERLOADED;
            }
            if (averageTickNanos >= snapshot.headroomPressureNanos()) {
                return PRESSURE;
            }
            if (averageTickNanos >= snapshot.headroomWarmNanos()) {
                return WARM;
            }
            return HEALTHY;
        }
    }
}
