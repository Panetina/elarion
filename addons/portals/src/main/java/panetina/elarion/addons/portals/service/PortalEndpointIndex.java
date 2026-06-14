package panetina.elarion.addons.portals.service;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.addons.portals.model.PortalTravelDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PortalEndpointIndex {
    private final Map<String, Map<Long, List<Entry>>> byWorldChunk = new HashMap<>();

    void rebuild(Map<String, PortalRouteState> routes) {
        byWorldChunk.clear();
        routes.forEach((routeId, route) -> {
            index(routeId, route.source, PortalTravelDirection.OUTBOUND);
            index(routeId, route.returnEndpoint, PortalTravelDirection.RETURN);
        });
    }

    List<Entry> nearby(String worldId, BlockPos position) {
        return byWorldChunk.getOrDefault(worldId, Map.of()).getOrDefault(
                ChunkPos.toLong(position.getX() >> 4, position.getZ() >> 4), List.of());
    }

    private void index(String routeId, PortalEndpoint endpoint, PortalTravelDirection direction) {
        if (endpoint == null) return;
        Map<Long, List<Entry>> world =
                byWorldChunk.computeIfAbsent(endpoint.worldId(), ignored -> new HashMap<>());
        int minChunkX = endpoint.bounds().minX() >> 4;
        int maxChunkX = endpoint.bounds().maxX() >> 4;
        int minChunkZ = endpoint.bounds().minZ() >> 4;
        int maxChunkZ = endpoint.bounds().maxZ() >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                world.computeIfAbsent(ChunkPos.toLong(chunkX, chunkZ), ignored -> new ArrayList<>())
                        .add(new Entry(routeId, endpoint, direction));
            }
        }
    }

    record Entry(String routeId, PortalEndpoint endpoint, PortalTravelDirection direction) {
    }
}
