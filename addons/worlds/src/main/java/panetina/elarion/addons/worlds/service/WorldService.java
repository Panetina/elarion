package panetina.elarion.addons.worlds.service;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionOptions;
import org.slf4j.Logger;
import panetina.elarion.addons.worlds.config.WorldsConfigManager;
import panetina.elarion.addons.worlds.config.WorldsConfigException;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import panetina.elarion.addons.worlds.model.WorldBorderDefinition;
import panetina.elarion.addons.worlds.model.WorldSpawn;
import panetina.elarion.core.api.ElarionApi;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WorldService {
    private final Logger logger;
    private final ElarionApi api;
    private final WorldsConfigManager config;
    private final Map<String, RuntimeWorldHandle> handles = new LinkedHashMap<>();
    private final Map<UUID, RegistryKey<World>> playerWorlds = new LinkedHashMap<>();
    private final Set<WorldBorder> listeningBorders = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<PendingHistory> pendingHistory = new ArrayList<>();
    private MinecraftServer server;

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
        ServerTickEvents.END_SERVER_TICK.register(this::syncPlayerBorders);
    }

    public void start(MinecraftServer server) {
        this.server = server;
        validateRegistryEntries();
        loadConfiguredWorlds();
    }

    public void reload() {
        requireServer();
        Map<String, ManagedWorldDefinition> previous = config.worlds();
        try {
            config.load();
            validateRegistryEntries();
        } catch (RuntimeException exception) {
            config.restore(previous);
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

        Registry<DimensionOptions> dimensions = server.getRegistryManager().get(RegistryKeys.DIMENSION);
        Identifier templateId = Identifier.of(definition.template());
        DimensionOptions template = dimensions.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Unknown dimension template " + definition.template());
        }

        RuntimeWorldConfig runtimeConfig = new RuntimeWorldConfig()
                .setDimensionType(template.dimensionTypeEntry())
                .setGenerator(template.chunkGenerator())
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
        recordHistory("opened", definition, Map.of(
                "template", definition.template(),
                "seed", Long.toString(definition.seed())));
        logger.info("Opened Elarion managed world {}", definition.id());
        return world;
    }

    public boolean unload(String worldName) {
        ManagedWorldDefinition definition = findDefinition(worldName);
        if (definition == null) return false;
        RuntimeWorldHandle handle = handles.remove(definition.id());
        if (handle == null) return false;
        handle.unload();
        recordHistory("unload-requested", definition, Map.of());
        return true;
    }

    public boolean teleport(ServerPlayerEntity player, String worldName) {
        ManagedWorldDefinition definition = findDefinition(worldName);
        if (definition == null) return false;
        ServerWorld world = getWorld(definition.id());
        if (world == null) world = open(definition);
        WorldSpawn spawn = definition.spawn();
        player.teleportTo(new TeleportTarget(
                world,
                new net.minecraft.util.math.Vec3d(spawn.x(), spawn.y(), spawn.z()),
                net.minecraft.util.math.Vec3d.ZERO,
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

    public ServerWorld getWorld(String id) {
        if (server == null) return null;
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) return null;
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, identifier));
    }

    public boolean isLoaded(ManagedWorldDefinition definition) {
        return getWorld(definition.id()) != null;
    }

    private void configure(ServerWorld world, ManagedWorldDefinition definition) {
        WorldSpawn spawn = definition.spawn();
        world.setSpawnPos(BlockPos.ofFloored(spawn.x(), spawn.y(), spawn.z()), spawn.yaw());
        applyBorder(world, definition.border());
        applyGameRules(world, definition);
        if (listeningBorders.add(world.getWorldBorder())) {
            world.getWorldBorder().addListener(new PerWorldBorderListener(world));
        }
    }

    private void applyBorder(ServerWorld world, WorldBorderDefinition definition) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(definition.centerX(), definition.centerZ());
        border.setSize(definition.size());
        border.setSafeZone(definition.safeZone());
        border.setDamagePerBlock(definition.damagePerBlock());
        border.setWarningBlocks(definition.warningBlocks());
        border.setWarningTime(definition.warningTime());
    }

    private void applyGameRules(ServerWorld world, ManagedWorldDefinition definition) {
        var source = server.getCommandSource().withWorld(world).withLevel(4);
        definition.gameRules().forEach((rule, value) ->
                server.getCommandManager().executeWithPrefix(source, "gamerule " + rule + " " + value));
    }

    private void syncPlayerBorders(MinecraftServer server) {
        flushHistory();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            RegistryKey<World> current = player.getServerWorld().getRegistryKey();
            RegistryKey<World> previous = playerWorlds.put(player.getUuid(), current);
            if (!current.equals(previous)) sendBorder(player, player.getServerWorld());
        }
        playerWorlds.keySet().removeIf(uuid -> server.getPlayerManager().getPlayer(uuid) == null);
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
        events.forEach(event -> api.history().record(
                "world", event.type(), null, "world", event.worldId(), "", event.metadata()));
    }

    private void requireServer() {
        if (server == null) throw new IllegalStateException("Elarion Worlds is not bound to a server");
    }

    private void validateRegistryEntries() {
        List<String> errors = new ArrayList<>();
        for (ManagedWorldDefinition definition : config.worlds().values()) {
            definition.blockRules().forEach(rule -> {
                Identifier block = Identifier.tryParse(rule.blockId());
                Identifier replacement = Identifier.tryParse(rule.replacementBlockId());
                if (block == null || !Registries.BLOCK.containsId(block)) {
                    errors.add("worlds.yml.worlds." + definition.key()
                            + ".block-abundance: unknown block " + rule.blockId());
                }
                if (replacement == null || !Registries.BLOCK.containsId(replacement)) {
                    errors.add("worlds.yml.worlds." + definition.key()
                            + ".block-abundance." + rule.blockId()
                            + ".replace-with: unknown block " + rule.replacementBlockId());
                }
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

    private record PendingHistory(String type, String worldId, Map<String, String> metadata) {
    }
}
