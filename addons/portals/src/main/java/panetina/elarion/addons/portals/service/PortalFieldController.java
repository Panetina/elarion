package panetina.elarion.addons.portals.service;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import panetina.elarion.addons.portals.PortalContent;
import panetina.elarion.addons.portals.PortalFieldBlock;
import panetina.elarion.addons.portals.model.PortalEndpoint;
import panetina.elarion.addons.portals.model.PortalRouteState;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.service.ElarionPerformanceMonitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

final class PortalFieldController {
    private final Logger logger;
    private final ElarionApi api;
    private final Function<String, ServerWorld> worldResolver;
    private final Set<String> queuedWork = new HashSet<>();
    private final Set<String> activeRoutes = new HashSet<>();

    PortalFieldController(Logger logger, ElarionApi api, Function<String, ServerWorld> worldResolver) {
        this.logger = logger;
        this.api = api;
        this.worldResolver = worldResolver;
    }

    boolean isActive(String routeId) {
        return activeRoutes.contains(routeId);
    }

    void activate(String routeId, PortalRouteState route) {
        if (!route.complete()) return;
        String workKey = routeId + ":activate";
        if (!queuedWork.add(workKey)) return;
        List<PortalEndpoint> endpoints = List.of(route.source, route.returnEndpoint);
        try {
            for (PortalEndpoint endpoint : endpoints) validateInterior(endpoint, true);
            boolean allQueued = true;
            for (PortalEndpoint endpoint : endpoints) {
                ServerWorld world = requireWorld(endpoint.worldId());
                BlockState field = PortalContent.FIELD.getDefaultState()
                        .with(PortalFieldBlock.AXIS, endpoint.bounds().axis().minecraft());
                for (BlockPos pos : endpoint.bounds().positions()) {
                    if (!queuePlace(routeId, world, pos, field)) {
                        allQueued = false;
                        break;
                    }
                }
                if (!allQueued) break;
            }
            finish(routeId, workKey, allQueued, true);
        } catch (RuntimeException exception) {
            queuedWork.remove(workKey);
            throw exception;
        }
    }

    void deactivate(String routeId, PortalRouteState route) {
        String workKey = routeId + ":deactivate";
        if (!queuedWork.add(workKey)) return;
        boolean allQueued = true;
        for (PortalEndpoint endpoint : PortalRouteService.endpoints(route)) {
            ServerWorld world = worldResolver.apply(endpoint.worldId());
            if (world == null) continue;
            for (BlockPos pos : endpoint.bounds().positions()) {
                if (!queueRemove(routeId, world, pos)) {
                    allQueued = false;
                    break;
                }
            }
            if (!allQueued) break;
        }
        finish(routeId, workKey, allQueued, false);
    }

    void validateInterior(PortalEndpoint endpoint, boolean allowOwnField) {
        ServerWorld world = requireWorld(endpoint.worldId());
        for (BlockPos pos : endpoint.bounds().positions()) {
            BlockState block = world.getBlockState(pos);
            if (block.isAir() || block.isReplaceable() || allowOwnField && block.isOf(PortalContent.FIELD)) continue;
            throw new IllegalArgumentException("Portal interior is obstructed at "
                    + pos.getX() + " " + pos.getY() + " " + pos.getZ() + ".");
        }
    }

    private boolean queuePlace(String routeId, ServerWorld world, BlockPos pos, BlockState field) {
        boolean queued = api.tasks().enqueueServer("portal-field-place:" + routeId, () -> {
            try {
                world.setBlockState(pos, field, Block.NOTIFY_ALL);
                metric("portal-field-placed");
            } catch (RuntimeException exception) {
                metric("portal-field-failed");
                logger.error("Failed to place portal field for {} at {}", routeId, pos, exception);
            }
        });
        if (!queued) {
            metric("portal-field-queue-full");
            logger.warn("Portal field queue full while activating {} at {}", routeId, pos);
            return false;
        }
        metric("portal-field-queued");
        return true;
    }

    private boolean queueRemove(String routeId, ServerWorld world, BlockPos pos) {
        boolean queued = api.tasks().enqueueServer("portal-field-remove:" + routeId, () -> {
            try {
                if (world.getBlockState(pos).isOf(PortalContent.FIELD)) {
                    world.removeBlock(pos, false);
                    metric("portal-field-removed");
                }
            } catch (RuntimeException exception) {
                metric("portal-field-failed");
                logger.error("Failed to remove portal field for {} at {}", routeId, pos, exception);
            }
        });
        if (!queued) {
            metric("portal-field-queue-full");
            return false;
        }
        metric("portal-field-queued");
        return true;
    }

    private void finish(String routeId, String workKey, boolean completedQueue, boolean activating) {
        if (!api.tasks().enqueueServer("portal-field-finish:" + routeId, () -> {
            queuedWork.remove(workKey);
            if (completedQueue) {
                if (activating) activeRoutes.add(routeId);
                else activeRoutes.remove(routeId);
            }
        })) {
            queuedWork.remove(workKey);
        }
    }

    private ServerWorld requireWorld(String worldId) {
        ServerWorld world = worldResolver.apply(worldId);
        if (world == null) throw new IllegalArgumentException("Unknown or unloaded world " + worldId);
        return world;
    }

    private static void metric(String name) {
        ElarionPerformanceMonitor.record(name, 0L);
    }
}
