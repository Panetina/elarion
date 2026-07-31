package panetina.elarion.addons.mounts.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import panetina.elarion.addons.mounts.entity.ElarionMountEntities;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MountSessionService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int RESTORE_DELAY_TICKS = 8;
    private static final double RESTORE_HEIGHT_OFFSET = 3.0D;
    private static final double REMOUNT_DISTANCE_SQUARED = 20.0D * 20.0D;

    private final Logger logger;
    private final Map<UUID, StoredSession> sessions = new HashMap<>();
    private final Map<UUID, Integer> pendingRestores = new HashMap<>();
    private MinecraftServer server;
    private boolean loaded;
    private boolean dirty;
    private long ticks;

    public MountSessionService(Logger logger) {
        this.logger = logger;
    }

    public void bind(MinecraftServer server) {
        if (this.server == server && loaded) {
            return;
        }
        this.server = server;
        sessions.clear();
        sessions.putAll(loadSessions(file(server)));
        loaded = true;
        dirty = false;
    }

    Map<UUID, StoredSession> loadSessions(Path path) {
        return JsonStateStorage.read(
                path,
                GSON,
                StoredState.class,
                HashMap::new,
                MountSessionService::normalizeStoredSessions,
                logger,
                "mount sessions");
    }

    public void tick(MinecraftServer server) {
        bind(server);
        ticks++;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getVehicle() instanceof ElarionMountEntity mount && mount.isOwner(player)) {
                remember(player, mount);
                continue;
            }
            if (sessions.containsKey(player.getUuid()) && !pendingRestores.containsKey(player.getUuid())) {
                pendingRestores.put(player.getUuid(), RESTORE_DELAY_TICKS);
                continue;
            }
            Integer remaining = pendingRestores.get(player.getUuid());
            if (remaining == null) {
                continue;
            }
            if (remaining > 0) {
                pendingRestores.put(player.getUuid(), remaining - 1);
                continue;
            }
            pendingRestores.remove(player.getUuid());
            restoreOrAttach(player);
        }
        if (dirty && ticks % 100 == 0) {
            save();
        }
    }

    public void scheduleRestore(ServerPlayerEntity player) {
        if (sessions.containsKey(player.getUuid())) {
            pendingRestores.put(player.getUuid(), RESTORE_DELAY_TICKS);
        }
    }

    public void captureAndPark(ServerPlayerEntity player) {
        if (player.getVehicle() instanceof ElarionMountEntity mount && mount.isOwner(player)) {
            remember(player, mount);
            park(mount);
            save();
            return;
        }
        StoredSession session = sessions.get(player.getUuid());
        if (session != null) {
            removeLoadedOwnerMounts(player, session);
            save();
        }
    }

    public void remember(ServerPlayerEntity owner, ElarionMountEntity mount) {
        if (server == null) {
            server = owner.getServer();
        }
        sessions.put(owner.getUuid(), StoredSession.from(owner, mount));
        dirty = true;
    }

    public void clear(UUID ownerId) {
        pendingRestores.remove(ownerId);
        if (sessions.remove(ownerId) != null) {
            dirty = true;
            save();
        }
    }

    public int resetAll() {
        int changed = sessions.size();
        sessions.clear();
        pendingRestores.clear();
        dirty = true;
        save();
        return changed;
    }

    public void clearForMount(ElarionMountEntity mount) {
        mount.ownerUuid().ifPresent(this::clear);
    }

    public void clearNearby(ServerPlayerEntity player, double radius) {
        List<ElarionMountEntity> mounts = player.getWorld().getEntitiesByClass(
                ElarionMountEntity.class,
                player.getBoundingBox().expand(radius),
                mount -> true);
        mounts.forEach(mount -> {
            clearForMount(mount);
            park(mount);
        });
    }

    public boolean summonOrAttach(ServerPlayerEntity player, ElarionMountType type) {
        bind(player.getServer());
        if (player.getVehicle() instanceof ElarionMountEntity current && current.isOwner(player)) {
            if (current.mountType() == type) {
                remember(player, current);
                return true;
            }
            park(current);
        }
        StoredSession previous = sessions.get(player.getUuid());
        if (previous != null) {
            ElarionMountEntity nearby = findOwnedMount(player, previous);
            if (nearby != null && nearby.mountType() == type && player.startRiding(nearby, true)) {
                nearby.updatePassengerPosition(player);
                remember(player, nearby);
                save();
                return true;
            }
            removeLoadedOwnerMounts(player, previous);
        }
        return spawnMount(player, type, false, RESTORE_HEIGHT_OFFSET, 0.75D, player.getYaw());
    }

    public void captureAllAndSave(MinecraftServer server) {
        bind(server);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getVehicle() instanceof ElarionMountEntity mount && mount.isOwner(player)) {
                remember(player, mount);
                park(mount);
            } else {
                StoredSession session = sessions.get(player.getUuid());
                if (session != null) {
                    removeLoadedOwnerMounts(player, session);
                }
            }
        }
        save();
    }

    private void restoreOrAttach(ServerPlayerEntity player) {
        StoredSession session = sessions.get(player.getUuid());
        if (session == null || player.getVehicle() instanceof ElarionMountEntity) {
            return;
        }
        ElarionMountEntity nearby = findOwnedMount(player, session);
        if (nearby != null && player.startRiding(nearby, true)) {
            nearby.updatePassengerPosition(player);
            remember(player, nearby);
            return;
        }
        removeLoadedOwnerMounts(player, session);
        spawnSessionMount(player, session);
    }

    private ElarionMountEntity findOwnedMount(ServerPlayerEntity player, StoredSession session) {
        Identifier worldId = Identifier.tryParse(session.worldId);
        if (worldId == null || !player.getWorld().getRegistryKey().getValue().equals(worldId)) {
            return null;
        }
        return player.getWorld().getEntitiesByClass(
                        ElarionMountEntity.class,
                        player.getBoundingBox().expand(24.0D),
                        mount -> mount.isOwner(player) && mount.squaredDistanceTo(player) <= REMOUNT_DISTANCE_SQUARED)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void spawnSessionMount(ServerPlayerEntity player, StoredSession session) {
        ElarionMountType type = ElarionMountType.byId(session.mountType);
        if (spawnMount(player, type, session.returnWhistleOnDismiss, RESTORE_HEIGHT_OFFSET, 0.5D, session.yaw)) {
            return;
        }
        logger.warn("Could not restore Elarion mount session for {}", player.getGameProfile().getName());
    }

    private boolean spawnMount(
            ServerPlayerEntity player,
            ElarionMountType type,
            boolean returnWhistleOnDismiss,
            double heightOffset,
            double liftBlocks,
            float yaw
    ) {
        ElarionMountEntity mount = ElarionMountEntities.MOUNT.create(player.getWorld());
        if (mount == null) {
            return false;
        }
        mount.setMountType(type);
        mount.setOwner(player.getUuid());
        mount.setReturnWhistleOnDismiss(returnWhistleOnDismiss);
        mount.refreshPositionAndAngles(
                player.getX(),
                player.getY() + heightOffset,
                player.getZ(),
                yaw,
                0.0F);
        mount.startSummonLift(liftBlocks);
        player.getWorld().spawnEntity(mount);
        if (player.startRiding(mount, true)) {
            mount.updatePassengerPosition(player);
        }
        remember(player, mount);
        save();
        return true;
    }

    private void removeLoadedOwnerMounts(ServerPlayerEntity player, StoredSession session) {
        if (server == null) {
            return;
        }
        UUID ownerId = player.getUuid();
        for (ServerWorld world : server.getWorlds()) {
            if (world == player.getWorld()) {
                removeLoadedOwnerMounts(world, player.getBoundingBox().expand(64.0D), ownerId);
            }
            Identifier sessionWorldId = Identifier.tryParse(session.worldId);
            if (sessionWorldId != null && world.getRegistryKey().getValue().equals(sessionWorldId)) {
                Box sessionBox = new Box(
                        session.x - 64.0D, session.y - 64.0D, session.z - 64.0D,
                        session.x + 64.0D, session.y + 64.0D, session.z + 64.0D);
                removeLoadedOwnerMounts(world, sessionBox, ownerId);
            }
        }
    }

    private void removeLoadedOwnerMounts(ServerWorld world, Box box, UUID ownerId) {
        List<ElarionMountEntity> mounts = world.getEntitiesByClass(
                ElarionMountEntity.class,
                box,
                mount -> mount.ownerUuid().map(ownerId::equals).orElse(false));
        mounts.forEach(this::park);
    }

    private void park(ElarionMountEntity mount) {
        for (Entity passenger : List.copyOf(mount.getPassengerList())) {
            passenger.stopRiding();
        }
        mount.discard();
    }

    private void save() {
        if (server == null || !dirty) {
            return;
        }
        JsonStateStorage.writeAtomic(file(server), GSON, new StoredState(sessions), logger, "mount sessions");
        dirty = false;
    }

    private static Path file(MinecraftServer server) {
        return JsonStateStorage.addonStateRoot(server, "mounts").resolve("sessions.json");
    }

    private static Map<UUID, StoredSession> normalizeStoredSessions(StoredState stored) {
        Map<UUID, StoredSession> normalized = new HashMap<>();
        if (stored == null || stored.sessions == null) return normalized;
        stored.sessions.forEach((playerId, session) -> {
            if (playerId == null || session == null) return;
            session.normalize();
            normalized.put(playerId, session);
        });
        return normalized;
    }

    public static final class StoredState {
        public Map<UUID, StoredSession> sessions = new HashMap<>();

        public StoredState() {
        }

        public StoredState(Map<UUID, StoredSession> sessions) {
            this.sessions = new HashMap<>(sessions);
        }
    }

    public static final class StoredSession {
        public String mountType = ElarionMountType.CHINESE_DRAGON.id();
        public String worldId = "minecraft:overworld";
        public double x;
        public double y;
        public double z;
        public float yaw;
        public boolean returnWhistleOnDismiss;
        public long updatedAt;

        public StoredSession() {
        }

        void normalize() {
            String type = mountType == null ? "" : mountType.trim();
            mountType = type.isEmpty()
                    ? ElarionMountType.CHINESE_DRAGON.id()
                    : ElarionMountType.byId(type).id();
            String world = worldId == null ? "" : worldId.trim();
            worldId = world.isEmpty() || Identifier.tryParse(world) == null
                    ? "minecraft:overworld"
                    : world;
            if (!Double.isFinite(x)) x = 0.0D;
            if (!Double.isFinite(y)) y = 0.0D;
            if (!Double.isFinite(z)) z = 0.0D;
            if (!Float.isFinite(yaw)) yaw = 0.0F;
            updatedAt = Math.max(0L, updatedAt);
        }

        static StoredSession from(ServerPlayerEntity owner, ElarionMountEntity mount) {
            StoredSession session = new StoredSession();
            session.mountType = mount.mountType().id();
            session.worldId = mount.getWorld().getRegistryKey().getValue().toString();
            session.x = mount.getX();
            session.y = mount.getY();
            session.z = mount.getZ();
            session.yaw = mount.getYaw();
            session.returnWhistleOnDismiss = mount.returnWhistleOnDismiss();
            session.updatedAt = System.currentTimeMillis();
            return session;
        }
    }
}
