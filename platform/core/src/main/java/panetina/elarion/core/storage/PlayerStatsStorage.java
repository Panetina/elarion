package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.model.PlayerStats;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import panetina.elarion.core.api.reset.PlayerResetFiles;

public final class PlayerStatsStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public PlayerStatsStorage(Logger logger) {
        this.logger = logger;
    }

    public PlayerStats load(MinecraftServer server, UUID uuid) {
        Path file = statsDir(server).resolve(uuid + ".json");
        if (Files.notExists(file)) return new PlayerStats(uuid);
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredStats stored = GSON.fromJson(reader, StoredStats.class);
            return stored == null ? new PlayerStats(uuid) : stored.toStats(uuid);
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load Elarion player stats {}", uuid, exception);
            return new PlayerStats(uuid);
        }
    }

    public void save(MinecraftServer server, PlayerStats stats) {
        JsonStateStorage.writeAtomic(
                statsDir(server).resolve(stats.uuid() + ".json"),
                GSON,
                StoredStats.from(stats),
                logger,
                "Elarion player stats " + stats.uuid());
    }

    public void deleteAll(MinecraftServer server) throws IOException {
        PlayerResetFiles.deleteTree(statsDir(server));
    }

    private static Path statsDir(MinecraftServer server) {
        return JsonStateStorage.elarionRoot(server).resolve("player-stats");
    }

    private static final class StoredStats {
        long zombieKills;
        long dragonKills;
        Map<String, Long> customCounters = new LinkedHashMap<>();

        static StoredStats from(PlayerStats stats) {
            StoredStats stored = new StoredStats();
            stored.zombieKills = stats.zombieKills();
            stored.dragonKills = stats.dragonKills();
            stored.customCounters = new LinkedHashMap<>(stats.customCounters());
            return stored;
        }

        PlayerStats toStats(UUID uuid) {
            PlayerStats stats = new PlayerStats(uuid);
            stats.setZombieKills(zombieKills);
            stats.setDragonKills(dragonKills);
            if (customCounters != null) stats.customCounters().putAll(customCounters);
            return stats;
        }
    }
}
