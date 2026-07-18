package panetina.elarion.core.integration.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class AdvancementLeaderboardStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    AdvancementLeaderboardStorage(Logger logger) {
        this.logger = logger;
    }

    Map<UUID, AdvancementLeaderboardProjection.Entry> load(Path root) {
        Stored stored = JsonStateStorage.read(path(root), GSON, Stored.class, Stored::empty,
                value -> value, logger, "advancement leaderboard index");
        Map<UUID, AdvancementLeaderboardProjection.Entry> entries = new LinkedHashMap<>();
        if (stored.entries != null) {
            stored.entries.forEach((id, entry) -> {
                try {
                    entries.put(UUID.fromString(id), entry.normalized());
                } catch (IllegalArgumentException ignored) {
                    logger.warn("Ignored invalid citizen id in advancement leaderboard index");
                }
            });
        }
        return entries;
    }

    void save(Path root, Map<UUID, AdvancementLeaderboardProjection.Entry> entries) {
        Map<String, AdvancementLeaderboardProjection.Entry> stored = new LinkedHashMap<>();
        entries.forEach((id, entry) -> stored.put(id.toString(), entry));
        JsonStateStorage.writeAtomic(path(root), GSON, new Stored(stored), logger,
                "advancement leaderboard index");
    }

    private static Path path(Path root) {
        return root.resolve("core").resolve("advancement-leaderboard.json");
    }

    private record Stored(Map<String, AdvancementLeaderboardProjection.Entry> entries) {
        static Stored empty() {
            return new Stored(Map.of());
        }
    }
}
