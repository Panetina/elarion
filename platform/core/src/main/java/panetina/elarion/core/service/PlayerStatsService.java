package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import panetina.elarion.core.model.PlayerStats;
import panetina.elarion.core.storage.DirtyTracker;
import panetina.elarion.core.storage.PlayerStatsStorage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStatsService {
    private static final long SAVE_INTERVAL_MILLIS = 300_000L;
    private final PlayerStatsStorage storage;
    private final TitleService titles;
    private final Map<UUID, PlayerStats> cache = new ConcurrentHashMap<>();
    private final DirtyTracker dirty = new DirtyTracker();
    private MinecraftServer server;
    private long lastSaveAt;

    public PlayerStatsService(PlayerStatsStorage storage, TitleService titles) {
        this.storage = storage;
        this.titles = titles;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        this.cache.clear();
        this.dirty.clear();
        this.lastSaveAt = System.currentTimeMillis();
    }

    public PlayerStats get(UUID uuid) {
        requireServer();
        return cache.computeIfAbsent(uuid, id -> storage.load(server, id));
    }

    public long increment(UUID uuid, String key, long amount) {
        PlayerStats stats = get(uuid);
        long value = stats.increment(normalize(key), amount);
        dirty.mark(uuid);
        titles.checkStatUnlocks(uuid, normalize(key), value);
        return value;
    }

    public void set(UUID uuid, String key, long value) {
        PlayerStats stats = get(uuid);
        stats.set(normalize(key), value);
        dirty.mark(uuid);
    }

    public long value(UUID uuid, String key) {
        return get(uuid).value(normalize(key));
    }

    public void saveIfDue() {
        long now = System.currentTimeMillis();
        if (now - lastSaveAt < SAVE_INTERVAL_MILLIS) return;
        saveDirty();
        lastSaveAt = now;
    }

    public void save(UUID uuid) {
        if (dirty.remove(uuid)) {
            PlayerStats stats = cache.get(uuid);
            if (stats != null) storage.save(server, stats);
        }
    }

    public void saveDirty() {
        if (server == null) return;
        dirty.flush(this::save);
    }

    public void reset(UUID uuid) {
        requireServer();
        PlayerStats fresh = new PlayerStats(uuid);
        cache.put(uuid, fresh);
        dirty.remove(uuid);
        storage.save(server, fresh);
    }

    public int resetAll() throws java.io.IOException {
        requireServer();
        int count = cache.size();
        cache.clear();
        dirty.clear();
        storage.deleteAll(server);
        return count;
    }

    private void requireServer() {
        if (server == null) throw new IllegalStateException("PlayerStatsService is not bound to a server");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
