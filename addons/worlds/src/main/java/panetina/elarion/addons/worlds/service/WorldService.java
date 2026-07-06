package panetina.elarion.addons.worlds.service;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.block.Block;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.slf4j.Logger;
import panetina.elarion.addons.worlds.config.WorldsConfigException;
import panetina.elarion.addons.worlds.config.WorldsConfigManager;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.WorldBorderDefinition;
import panetina.elarion.addons.worlds.model.WorldSpawn;
import panetina.elarion.addons.worlds.model.WorldType;
import panetina.elarion.core.api.ElarionApi;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WorldService {
    private static final int PLAYER_ROUTING_SWEEP_INTERVAL_TICKS = 20;
    private final Logger logger;
    private final ElarionApi api;
    private final WorldsConfigManager config;
    private final Map<String, RuntimeWorldHandle> handles = new LinkedHashMap<>();
    private final Map<UUID, RegistryKey<World>> playerWorlds = new LinkedHashMap<>();
    private final Set<WorldBorder> listeningBorders = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<PendingHistory> pendingHistory = new ArrayList<>();
    private MinecraftServer server;
    private boolean applyingConfiguration;
    private long ticks;

    public WorldService(Logger logger, ElarionApi api, WorldsConfigManager config) {
        this.logger = logger;
        this.api = api;
        this.config = config;
    }

    public void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::start);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            handles.clear();
            playerWorlds.clear();
            listeningBorders.clear();
            this.server = null;
        });
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
        ServerPlayerEvents.JOIN.register(this::routePlayerLocation);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer.getSpawnPointPosition() == null
                    && api.realm().citizens().getOrCreate(newPlayer).realmId().isBlank()) {
                teleport(newPlayer, config.lobbyDestination());
            }
        });
    }

    public void start(MinecraftServer server) {
        this.server = server;
        validateRegistryEntries();
        for (ServerWorld world : server.getWorlds()) attachBorderListener(world);
        loadConfiguredWorlds();
    }

    public void reload() {
        requireServer();
        Map<String, ManagedWorldDefinition> previousWorlds = config.worlds();
        String previousLobby = config.lobbyDestination();
        boolean previousEnforceLobby = config.enforceLobby();
        try {
            config.load();
            validateRegistryEntries();
        } catch (RuntimeException exception) {
            config.restore(previousWorlds, previousLobby, previousEnforceLobby);
            throw exception;
        }
        loadConfiguredWorlds();
    }

    public void loadConfiguredWorlds() {
        requireServer();
        for (ManagedWorldDefinition definition : config.worlds().values()) {
            if (definition.enabled()) open(definition);
        }
    }

    public ServerWorld open(ManagedWorldDefinition definition) {
        requireServer();
        ServerWorld existing = getWorld(definition.id());
        if (existing != null) {
            configure(existing, definition);
            return existing;
        }

        WorldGeneratorFactory.GeneratedWorld generated = WorldGeneratorFactory.create(server, definition);
        RuntimeWorldConfig runtimeConfig = new RuntimeWorldConfig()
                .setDimensionType(generated.template().dimensionTypeEntry())
                .setGenerator(generated.generator())
                .setSeed(definition.seed())
                .setDifficulty(Difficulty.valueOf(definition.difficulty()))
                .setShouldTickTime(definition.tickTime())
                .setMirrorOverworldDifficulty(false)
                .setMirrorOverworldGameRules(false)
                .setSunny(0);

        RuntimeWorldHandle handle = Fantasy.get(server)
                .getOrOpenPersistentWorld(Identifier.of(definition.id()), runtimeConfig);
        handle.setTickWhenEmpty(false);
        handles.put(definition.id(), handle);
        ServerWorld world = handle.asWorld();
        configure(world, definition);
        createSpawnPlatform(world, definition);
        recordHistory("opened", definition, Map.of(
                "type", definition.type().name(),
                "template", definition.template(),
                "seed", Long.toString(definition.seed())));
        logger.info("Opened Elarion managed world {} ({})", definition.id(), definition.type());
        return world;
    }

    public ManagedWorldDefinition create(String key, WorldType type, long seed) {
        requireServer();
        ManagedWorldDefinition definition = config.create(key, type, seed);
        open(definition);
        recordHistory("created", definition, Map.of("type", type.name(), "seed", Long.toString(seed)));
        return definition;
    }

    public boolean remove(String worldName) {
        requireServer();
        ManagedWorldDefinition definition = config.remove(worldName);
        if (definition == null) return false;
        ServerWorld fallback = resolveWorld("lobby");
        ServerWorld removedWorld = getWorld(definition.id());
        if (removedWorld != null && fallback != null && removedWorld != fallback) {
            for (ServerPlayerEntity player : List.copyOf(removedWorld.getPlayers())) {
                teleport(player, "lobby");
            }
        }
        RuntimeWorldHandle handle = handles.remove(definition.id());
        if (handle != null) {
            listeningBorders.remove(handle.asWorld().getWorldBorder());
            handle.delete();
        }
        recordHistory("delete-requested", definition, Map.of());
        return true;
    }

    public boolean unload(String worldName) {
        ManagedWorldDefinition definition = findDefinition(worldName);
        if (definition == null) return false;
        RuntimeWorldHandle handle = handles.remove(definition.id());
        if (handle == null) return false;
        listeningBorders.remove(handle.asWorld().getWorldBorder());
        handle.unload();
        recordHistory("unload-requested", definition, Map.of());
        return true;
    }

    public boolean teleport(ServerPlayerEntity player, String destination) {
        ResolvedDestination resolved = resolve(destination);
        if (resolved == null) return false;
        ServerWorld world = resolved.world();
        WorldSpawn spawn = resolved.spawn();
        if (spawn == null) {
            BlockPos position = world.getSpawnPos();
            spawn = new WorldSpawn(position.getX() + 0.5, position.getY(), position.getZ() + 0.5,
                    world.getSpawnAngle(), 0);
        }
        player.teleportTo(new TeleportTarget(
                world,
                new Vec3d(spawn.x(), spawn.y(), spawn.z()),
                Vec3d.ZERO,
                spawn.yaw(),
                spawn.pitch(),
                TeleportTarget.NO_OP));
        sendBorder(player, world);
        return true;
    }

    public ManagedWorldDefinition definition(ServerWorld world) {
        String worldId = world.getRegistryKey().getValue().toString();
        return config.worlds().values().stream()
                .filter(definition -> definition.id().equals(worldId))
                .findFirst()
                .orElse(null);
    }

    public ManagedWorldDefinition findDefinition(String name) {
        ManagedWorldDefinition direct = config.worlds().get(name);
        if (direct != null) return direct;
        return config.worlds().values().stream()
                .filter(definition -> definition.id().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Map<String, ManagedWorldDefinition> definitions() {
        return config.worlds();
    }

    public Set<String> destinationNames() {
        return Set.copyOf(config.worlds().keySet());
    }

    public ServerWorld resolveWorld(String name) {
        ResolvedDestination destination = resolve(name);
        return destination == null ? null : destination.world();
    }

    public ServerWorld getWorld(String id) {
        if (server == null) return null;
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) return null;
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, identifier));
    }

    public boolean isLoaded(ManagedWorldDefinition definition) {
        return getWorld(definition.id()) != null;
    }

    private ResolvedDestination resolve(String name) {
        ManagedWorldDefinition managed = findDefinitionWithoutAliases(name);
        if (managed != null) {
            ServerWorld world = getWorld(managed.id());
            if (world == null) world = open(managed);
            return new ResolvedDestination(world, managed.spawn());
        }
        ServerWorld world = getWorld(name);
        return world == null ? null : new ResolvedDestination(world, null);
    }

    private ManagedWorldDefinition findDefinitionWithoutAliases(String name) {
        ManagedWorldDefinition direct = config.worlds().get(name);
        if (direct != null) return direct;
        return config.worlds().values().stream()
                .filter(definition -> definition.id().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void configure(ServerWorld world, ManagedWorldDefinition definition) {
        WorldSpawn spawn = definition.spawn();
        world.setSpawnPos(BlockPos.ofFloored(spawn.x(), spawn.y(), spawn.z()), spawn.yaw());
        applyingConfiguration = true;
        try {
            applyBorder(world, definition.border());
            applyGameRules(world, definition.gameRules());
            attachBorderListener(world);
        } finally {
            applyingConfiguration = false;
        }
    }

    private void createSpawnPlatform(ServerWorld world, ManagedWorldDefinition definition) {
        if (definition.type() != WorldType.VOID) return;
        Identifier blockId = Identifier.of(definition.platformBlock());
        Block block = Registries.BLOCK.get(blockId);
        BlockPos center = BlockPos.ofFloored(
                definition.spawn().x(), definition.spawn().y() - 1, definition.spawn().z());
        int radius = definition.platformRadius();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                world.setBlockState(center.add(x, 0, z), block.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }

    private void attachBorderListener(ServerWorld world) {
        if (listeningBorders.add(world.getWorldBorder())) {
            world.getWorldBorder().addListener(new PerWorldBorderListener(
                    world,
                    border -> persistBorder(world, border)));
        }
    }

    private void persistBorder(ServerWorld world, WorldBorder border) {
        if (applyingConfiguration || definition(world) == null) return;
        config.updateBorder(
                world.getRegistryKey().getValue().toString(),
                new WorldBorderDefinition(
                        border.getCenterX(),
                        border.getCenterZ(),
                        border.getSizeLerpTime() > 0 ? border.getSizeLerpTarget() : border.getSize(),
                        border.getSafeZone(),
                        border.getDamagePerBlock(),
                        border.getWarningBlocks(),
                        border.getWarningTime()));
    }

    private static void applyBorder(ServerWorld world, WorldBorderDefinition definition) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(definition.centerX(), definition.centerZ());
        border.setSize(definition.size());
        border.setSafeZone(definition.safeZone());
        border.setDamagePerBlock(definition.damagePerBlock());
        border.setWarningBlocks(definition.warningBlocks());
        border.setWarningTime(definition.warningTime());
    }

    private void applyGameRules(ServerWorld world, Map<String, String> gameRules) {
        var source = server.getCommandSource().withWorld(world).withLevel(4);
        gameRules.forEach((rule, value) ->
                server.getCommandManager().executeWithPrefix(source, "gamerule " + rule + " " + value));
    }

    private void tick(MinecraftServer server) {
        ticks++;
        flushHistory();
        if (ticks % PLAYER_ROUTING_SWEEP_INTERVAL_TICKS != 0) return;
        ServerWorld lobby = config.enforceLobby() ? resolveWorld(config.lobbyDestination()) : null;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            String realmId = api.realm().citizens().getOrCreate(player).realmId();
            if (player.getServerWorld().getRegistryKey().equals(World.OVERWORLD)) {
                if (realmId.isBlank()
                        || !api.realm().spawns().teleportToRealmSpawn(player, "overworld-fallback")) {
                    teleport(player, config.lobbyDestination());
                }
            } else if (lobby != null && player.getServerWorld() != lobby && realmId.isBlank()) {
                teleport(player, config.lobbyDestination());
            }
            RegistryKey<World> current = player.getServerWorld().getRegistryKey();
            RegistryKey<World> previous = playerWorlds.put(player.getUuid(), current);
            if (!current.equals(previous)) sendBorder(player, player.getServerWorld());
        }
        playerWorlds.keySet().removeIf(uuid -> server.getPlayerManager().getPlayer(uuid) == null);
    }

    private void routePlayerLocation(ServerPlayerEntity player) {
        String realmId = api.realm().citizens().getOrCreate(player).realmId();
        if (player.getServerWorld().getRegistryKey().equals(World.OVERWORLD)) {
            if (realmId.isBlank()
                    || !api.realm().spawns().teleportToRealmSpawn(player, "login-overworld-fallback")) {
                teleport(player, config.lobbyDestination());
            }
            return;
        }
        if (config.enforceLobby() && realmId.isBlank()) {
            ServerWorld lobby = resolveWorld(config.lobbyDestination());
            if (lobby != null && player.getServerWorld() != lobby) {
                teleport(player, config.lobbyDestination());
            }
        }
    }

    private static void sendBorder(ServerPlayerEntity player, ServerWorld world) {
        player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(world.getWorldBorder()));
    }

    private void recordHistory(String type, ManagedWorldDefinition definition, Map<String, String> metadata) {
        pendingHistory.add(new PendingHistory(type, definition.id(), metadata));
    }

    private void flushHistory() {
        if (pendingHistory.isEmpty()) return;
        List<PendingHistory> events = List.copyOf(pendingHistory);
        pendingHistory.clear();
        events.forEach(event -> api.progressionApi().history().record(
                "world", event.type(), null, "world", event.worldId(), "", event.metadata()));
    }

    private void requireServer() {
        if (server == null) throw new IllegalStateException("Elarion Worlds is not bound to a server");
    }

    private void validateRegistryEntries() {
        List<String> errors = new ArrayList<>();
        for (ManagedWorldDefinition definition : config.worlds().values()) {
            validateBlock(errors, definition, definition.platformBlock(), ".platform-block");
            Identifier biome = Identifier.tryParse(definition.biome());
            if (biome == null || !server.getRegistryManager().get(RegistryKeys.BIOME).containsId(biome)) {
                errors.add("worlds.yml.worlds." + definition.key() + ".biome: unknown biome "
                        + definition.biome());
            }
            definition.blockRules().forEach(rule -> {
                validateBlock(errors, definition, rule.blockId(), ".block-abundance");
                validateBlock(errors, definition, rule.replacementBlockId(),
                        ".block-abundance." + rule.blockId() + ".replace-with");
            });
            definition.mobRules().forEach(rule -> {
                Identifier entity = Identifier.tryParse(rule.entityId());
                if (entity == null || !Registries.ENTITY_TYPE.containsId(entity)) {
                    errors.add("worlds.yml.worlds." + definition.key()
                            + ".mob-abundance: unknown entity type " + rule.entityId());
                }
            });
        }
        if (!errors.isEmpty()) throw new WorldsConfigException(errors);
    }

    private static void validateBlock(
            List<String> errors, ManagedWorldDefinition definition, String value, String suffix
    ) {
        Identifier block = Identifier.tryParse(value);
        if (block == null || !Registries.BLOCK.containsId(block)) {
            errors.add("worlds.yml.worlds." + definition.key() + suffix + ": unknown block " + value);
        }
    }

    private record ResolvedDestination(ServerWorld world, WorldSpawn spawn) {
    }

    private record PendingHistory(String type, String worldId, Map<String, String> metadata) {
    }
}
