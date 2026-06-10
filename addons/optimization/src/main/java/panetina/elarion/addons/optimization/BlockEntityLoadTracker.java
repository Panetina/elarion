package panetina.elarion.addons.optimization;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class BlockEntityLoadTracker {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> countsByWorld =
            new ConcurrentHashMap<>();

    void loaded(BlockEntity blockEntity, ServerWorld world) {
        if (blockEntity == null || world == null) return;
        String worldId = world.getRegistryKey().getValue().toString();
        String typeId = Registries.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()).toString();
        countsByWorld.computeIfAbsent(worldId, ignored -> new ConcurrentHashMap<>())
                .merge(typeId, 1, Integer::sum);
    }

    void unloaded(BlockEntity blockEntity, ServerWorld world) {
        if (blockEntity == null || world == null) return;
        String worldId = world.getRegistryKey().getValue().toString();
        String typeId = Registries.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()).toString();
        ConcurrentHashMap<String, Integer> counts = countsByWorld.get(worldId);
        if (counts == null) return;
        counts.computeIfPresent(typeId, (ignored, value) -> value <= 1 ? null : value - 1);
        if (counts.isEmpty()) countsByWorld.remove(worldId, counts);
    }

    Snapshot snapshot(String worldId) {
        Map<String, Integer> source = countsByWorld.get(worldId);
        if (source == null || source.isEmpty()) return Snapshot.empty();
        Map<String, Integer> groups = new LinkedHashMap<>();
        int total = 0;
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            int value = Math.max(0, entry.getValue());
            if (value <= 0) continue;
            groups.put(entry.getKey(), value);
            total += value;
        }
        return new Snapshot(total, Map.copyOf(groups));
    }

    record Snapshot(int total, Map<String, Integer> groups) {
        static Snapshot empty() {
            return new Snapshot(0, Map.of());
        }
    }
}
