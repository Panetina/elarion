package panetina.elarion.addons.npcs.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcStoryStateRecord;
import panetina.elarion.core.storage.JsonStateStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NpcStoryStateStorage {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Logger logger;
    private final Path fixedRoot;

    public NpcStoryStateStorage(Logger logger) {
        this(logger, null);
    }

    public NpcStoryStateStorage(Logger logger, Path fixedRoot) {
        this.logger = logger;
        this.fixedRoot = fixedRoot;
    }

    public Map<String, NpcStoryStateRecord> load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return new LinkedHashMap<>();
        StoredState stored;
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            stored = GSON.fromJson(reader, StoredState.class);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to load NPC story state", exception);
        }
        if (stored == null) return new LinkedHashMap<>();
        if (stored.schemaVersion != SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported NPC story-state schema " + stored.schemaVersion);
        }
        Map<String, NpcStoryStateRecord> result = new LinkedHashMap<>();
        if (stored.states != null) {
            for (NpcStoryStateRecord state : stored.states) {
                if (state == null || state.playerId() == null || state.npcId() == null) continue;
                result.put(key(state.playerId(), state.npcId()), state);
            }
        }
        return result;
    }

    public void save(MinecraftServer server, Map<String, NpcStoryStateRecord> states) {
        StoredState stored = new StoredState();
        stored.states = new ArrayList<>(states.values());
        JsonStateStorage.writeAtomic(file(server), GSON, stored, logger, "NPC story state");
    }

    public void saveChecked(MinecraftServer server, Map<String, NpcStoryStateRecord> states) {
        StoredState stored = new StoredState();
        stored.states = new ArrayList<>(states.values());
        try {
            JsonStateStorage.writeAtomicChecked(file(server), GSON, stored, "NPC story state");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist NPC story state", exception);
        }
    }

    public static String key(UUID playerId, UUID npcId) {
        return playerId + "::" + npcId;
    }

    private Path file(MinecraftServer server) {
        return root(server).resolve("story-state.json");
    }

    private Path root(MinecraftServer server) {
        return fixedRoot == null ? JsonStateStorage.addonStateRoot(server, "npcs") : fixedRoot;
    }

    private static final class StoredState {
        int schemaVersion = SCHEMA_VERSION;
        List<NpcStoryStateRecord> states = new ArrayList<>();
    }
}
