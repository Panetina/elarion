package panetina.elarion.addons.portals.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import panetina.elarion.addons.portals.model.PortalArrival;
import panetina.elarion.addons.portals.model.PortalRouteDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

final class PortalWorldTravelGuard {
    private final Logger logger;
    private final PortalDefinitionService definitions;
    private final Function<String, ServerWorld> worldResolver;
    private final Set<UUID> authorizedWorldChanges = new HashSet<>();
    private final Map<UUID, PortalArrival> lastKnownPositions = new HashMap<>();
    private final Map<UUID, PortalArrival> setupOrigins = new HashMap<>();

    PortalWorldTravelGuard(
            Logger logger,
            PortalDefinitionService definitions,
            Function<String, ServerWorld> worldResolver
    ) {
        this.logger = logger;
        this.definitions = definitions;
        this.worldResolver = worldResolver;
    }

    void rememberPosition(UUID playerId, PortalArrival arrival) {
        lastKnownPositions.put(playerId, arrival);
    }

    boolean teleport(ServerPlayerEntity player, ServerWorld destination, PortalArrival arrival) {
        boolean changesWorld = player.getWorld() != destination;
        try {
            if (changesWorld) authorizedWorldChanges.add(player.getUuid());
            player.teleport(destination, arrival.x(), arrival.y(), arrival.z(),
                    Set.of(), arrival.yaw(), arrival.pitch());
            if (!changesWorld) authorizedWorldChanges.remove(player.getUuid());
            return true;
        } catch (RuntimeException exception) {
            authorizedWorldChanges.remove(player.getUuid());
            logger.error("Portal teleport failed for {}", player.getGameProfile().getName(), exception);
            return false;
        }
    }

    void enterSetupDestination(
            ServerPlayerEntity player,
            String routeId,
            Vec3d requestedPosition
    ) {
        PortalRouteDefinition definition = definitions.require(routeId);
        if (unrestrictedDestination(definition)) {
            throw new IllegalArgumentException(
                    "This route accepts any destination world. Travel there normally, then set its endpoint and arrival.");
        }
        ServerWorld destination = worldResolver.apply(definition.destinationDimension());
        if (destination == null) {
            throw new IllegalArgumentException(
                    "Destination world is unavailable: " + definition.destinationDimension());
        }
        setupOrigins.putIfAbsent(player.getUuid(), new PortalArrival(
                worldId(player), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()));
        Vec3d target = requestedPosition == null
                ? Vec3d.ofBottomCenter(destination.getSpawnPos()).add(0.0, 1.0, 0.0)
                : requestedPosition;
        PortalArrival arrival = new PortalArrival(
                definition.destinationDimension(), target.x, target.y, target.z,
                player.getYaw(), player.getPitch());
        if (!teleport(player, destination, arrival)) {
            throw new IllegalArgumentException("Could not enter the destination world for setup.");
        }
    }

    void returnFromSetup(ServerPlayerEntity player) {
        PortalArrival origin = setupOrigins.get(player.getUuid());
        if (origin == null) {
            throw new IllegalArgumentException("No portal setup origin is stored for you.");
        }
        ServerWorld destination = worldResolver.apply(origin.worldId());
        if (destination == null || !teleport(player, destination, origin)) {
            throw new IllegalArgumentException("Could not return to the stored setup origin.");
        }
        setupOrigins.remove(player.getUuid());
    }

    void clearSetupOrigin(UUID playerId) {
        setupOrigins.remove(playerId);
    }

    boolean consumeAuthorizedWorldChange(UUID playerId) {
        return authorizedWorldChanges.remove(playerId);
    }

    void rejectUnauthorizedWorldChange(
            ServerPlayerEntity player,
            ServerWorld origin,
            ServerWorld destination
    ) {
        if (consumeAuthorizedWorldChange(player.getUuid())) return;
        String destinationId = worldId(destination);
        String originId = worldId(origin);
        if (!isRestrictedDimension(destinationId) && !isRestrictedDimension(originId)) return;
        PortalArrival previous = lastKnownPositions.get(player.getUuid());
        PortalArrival arrival = previous != null && previous.worldId().equals(originId)
                ? previous
                : new PortalArrival(
                        originId,
                        origin.getSpawnPos().getX() + 0.5,
                        origin.getSpawnPos().getY() + 1.0,
                        origin.getSpawnPos().getZ() + 0.5,
                        0,
                        0);
        authorizedWorldChanges.add(player.getUuid());
        teleport(player, origin, arrival);
        player.sendMessage(Text.literal(
                "Unregistered dimension travel is blocked. Use a scheduled Elarion gate."), false);
    }

    static boolean isRestrictedDimension(String worldId) {
        return "minecraft:the_nether".equals(worldId) || "minecraft:the_end".equals(worldId);
    }

    private static boolean unrestrictedDestination(PortalRouteDefinition definition) {
        return "*".equals(definition.destinationDimension());
    }

    private static String worldId(ServerPlayerEntity player) {
        return player.getWorld().getRegistryKey().getValue().toString();
    }

    private static String worldId(ServerWorld world) {
        return world.getRegistryKey().getValue().toString();
    }
}
