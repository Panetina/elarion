package panetina.elarion.addons.npcs.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

public final class NpcPlacementStorage {
    private static final int SCHEMA_VERSION = 2;
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
        return load(server, UnaryOperator.identity());
    }

    public Map<UUID, PlacedNpcRecord> load(
            MinecraftServer server,
            UnaryOperator<PlacedNpcRecord> migrator
    ) {
        Path stateFile = file(server);
        StoredState stored = JsonStateStorage.read(
                stateFile,
                GSON,
                StoredState.class,
                StoredState::new,
                state -> state,
                logger,
                "NPC placement state");
        if (stored.schemaVersion < 1 || stored.schemaVersion > SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported NPC placement schema " + stored.schemaVersion);
        }
        boolean changed = stored.schemaVersion < SCHEMA_VERSION;
        if (changed) backup(stateFile, stored.schemaVersion);
        Map<UUID, PlacedNpcRecord> result = new LinkedHashMap<>();
        if (stored.placed != null) {
            for (PlacedNpcRecord record : stored.placed) {
                if (record == null || record.id() == null) continue;
                PlacedNpcRecord migrated = migrator == null ? record : migrator.apply(record);
                if (migrated == null || migrated.id() == null) {
                    throw new IllegalStateException("NPC placement migration returned an invalid record");
                }
                changed |= !migrated.equals(record);
                result.put(migrated.id(), migrated);
            }
        }
        if (changed) saveChecked(server, result, "NPC placement schema migration");
        return result;
    }

    public void save(MinecraftServer server, Map<UUID, PlacedNpcRecord> records) {
        StoredState stored = new StoredState();
        stored.placed = new ArrayList<>(records.values());
        JsonStateStorage.writeAtomic(file(server), GSON, stored, logger, "NPC placement state");
    }

    private void backup(Path stateFile, int schemaVersion) {
        Path backup = stateFile.resolveSibling(stateFile.getFileName() + ".schema-v" + schemaVersion + ".bak");
        try {
            if (Files.notExists(backup)) Files.copy(stateFile, backup, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to back up NPC placement state before migration", exception);
        }
    }

    private void saveChecked(
            MinecraftServer server,
            Map<UUID, PlacedNpcRecord> records,
            String description
    ) {
        StoredState stored = new StoredState();
        stored.placed = new ArrayList<>(records.values());
        try {
            JsonStateStorage.writeAtomicChecked(file(server), GSON, stored, description);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist " + description, exception);
        }
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
