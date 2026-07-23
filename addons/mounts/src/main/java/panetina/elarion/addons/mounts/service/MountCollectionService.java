package panetina.elarion.addons.mounts.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class MountCollectionService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Logger logger;
    private final Map<UUID, PlayerMountCollection> players = new HashMap<>();
    private MinecraftServer server;
    private Path fixedRoot;
    private boolean loaded;
    private boolean dirty;

    public MountCollectionService(Logger logger) {
        this.logger = logger;
    }

    public void bind(MinecraftServer server) {
        if (this.server == server && loaded) return;
        this.server = server;
        bindRoot(JsonStateStorage.addonStateRoot(server, "mounts"));
    }

    public void bindRoot(Path root) {
        this.fixedRoot = root;
        players.clear();
        players.putAll(JsonStateStorage.read(
                file(),
                GSON,
                StoredState.class,
                HashMap::new,
                stored -> new HashMap<>(stored.players),
                logger,
                "mount collection state"));
        players.values().forEach(PlayerMountCollection::normalize);
        loaded = true;
        dirty = false;
    }

    public boolean unlock(ServerPlayerEntity player, ElarionMountType type) {
        return unlock(player.getUuid(), type);
    }

    public boolean unlock(UUID playerId, ElarionMountType type) {
        PlayerMountCollection collection = collection(playerId);
        boolean changed = collection.unlockedMounts.add(type.id());
        if (collection.activeMountId.isBlank()) {
            collection.activeMountId = type.id();
            changed = true;
        }
        if (changed) markDirty();
        return changed;
    }

    public boolean revoke(UUID playerId, ElarionMountType type) {
        PlayerMountCollection collection = collection(playerId);
        boolean changed = collection.unlockedMounts.remove(type.id());
        if (collection.activeMountId.equals(type.id())) {
            collection.activeMountId = collection.unlockedMounts.stream().findFirst().orElse("");
            changed = true;
        }
        if (changed) markDirty();
        return changed;
    }

    public boolean setActive(ServerPlayerEntity player, ElarionMountType type) {
        return setActive(player.getUuid(), type);
    }

    public boolean setActive(UUID playerId, ElarionMountType type) {
        PlayerMountCollection collection = collection(playerId);
        if (!collection.unlockedMounts.contains(type.id())) {
            return false;
        }
        if (collection.activeMountId.equals(type.id())) {
            return true;
        }
        collection.activeMountId = type.id();
        markDirty();
        return true;
    }

    public boolean isUnlocked(UUID playerId, ElarionMountType type) {
        PlayerMountCollection collection = players.get(playerId);
        return collection != null && collection.unlockedMounts.contains(type.id());
    }

    public Set<String> unlocked(UUID playerId) {
        PlayerMountCollection collection = players.get(playerId);
        return collection == null ? Set.of() : Set.copyOf(collection.unlockedMounts);
    }

    public Optional<ElarionMountType> activeMount(UUID playerId) {
        PlayerMountCollection collection = players.get(playerId);
        if (collection == null) return Optional.empty();
        if (collection.activeMountId.isBlank()) {
            return Optional.empty();
        }
        ElarionMountType type = ElarionMountType.byId(collection.activeMountId);
        return collection.unlockedMounts.contains(type.id()) ? Optional.of(type) : Optional.empty();
    }

    public void save() {
        if (!dirty || file() == null) return;
        JsonStateStorage.writeAtomic(file(), GSON, new StoredState(players), logger, "mount collection state");
        dirty = false;
    }

    public int resetAll() {
        int changed = players.size();
        players.clear();
        dirty = true;
        save();
        return changed;
    }

    private PlayerMountCollection collection(UUID playerId) {
        return players.computeIfAbsent(playerId, ignored -> new PlayerMountCollection());
    }

    private void markDirty() {
        dirty = true;
        save();
    }

    private Path file() {
        Path root = fixedRoot != null ? fixedRoot
                : server == null ? null : JsonStateStorage.addonStateRoot(server, "mounts");
        return root == null ? null : root.resolve("collection.json");
    }

    public static final class StoredState {
        public Map<UUID, PlayerMountCollection> players = new HashMap<>();

        public StoredState() {
        }

        public StoredState(Map<UUID, PlayerMountCollection> players) {
            this.players = new HashMap<>(players);
        }
    }

    public static final class PlayerMountCollection {
        public Set<String> unlockedMounts = new LinkedHashSet<>();
        public String activeMountId = "";

        public void normalize() {
            if (unlockedMounts == null) unlockedMounts = new LinkedHashSet<>();
            Set<String> normalized = new LinkedHashSet<>();
            for (String id : unlockedMounts) {
                if (id != null && !id.isBlank()) {
                    normalized.add(id.trim().toLowerCase(Locale.ROOT));
                }
            }
            unlockedMounts = normalized;
            activeMountId = activeMountId == null ? "" : activeMountId.trim().toLowerCase(Locale.ROOT);
            if (!activeMountId.isBlank() && !unlockedMounts.contains(activeMountId)) {
                activeMountId = "";
            }
        }
    }
}
