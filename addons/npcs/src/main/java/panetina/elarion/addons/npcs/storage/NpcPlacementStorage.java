package panetina.elarion.addons.npcs.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NpcPlacementStorage {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Logger logger;
    private final Path fixedRoot;

    public NpcPlacementStorage(Logger logger) {
        this(logger, null);
    }

    public NpcPlacementStorage(Logger logger, Path fixedRoot) {
        this.logger = logger;
        this.fixedRoot = fixedRoot;
    }

    public Map<UUID, PlacedNpcRecord> load(MinecraftServer server) {
        StoredState stored = JsonStateStorage.read(file(server), GSON, StoredState.class,
                StoredState::new, state -> state, logger, "NPC placement state");
        if (stored.schemaVersion != SCHEMA_VERSION) {
            logger.error("Unsupported NPC placement schema {}", stored.schemaVersion);
            return new LinkedHashMap<>();
        }
        Map<UUID, PlacedNpcRecord> result = new LinkedHashMap<>();
        if (stored.placed != null) {
            for (PlacedNpcRecord record : stored.placed) {
                if (record != null && record.id() != null) result.put(record.id(), record);
            }
        }
        return result;
    }

    public void save(MinecraftServer server, Map<UUID, PlacedNpcRecord> records) {
        StoredState stored = new StoredState();
        stored.placed = new ArrayList<>(records.values());
        JsonStateStorage.writeAtomic(file(server), GSON, stored, logger, "NPC placement state");
    }

    private Path file(MinecraftServer server) {
        return root(server).resolve("placed-npcs.json");
    }

    private Path root(MinecraftServer server) {
        return fixedRoot == null ? JsonStateStorage.addonStateRoot(server, "npcs") : fixedRoot;
    }

    private static final class StoredState {
        int schemaVersion = SCHEMA_VERSION;
        List<PlacedNpcRecord> placed = new ArrayList<>();
    }
}
