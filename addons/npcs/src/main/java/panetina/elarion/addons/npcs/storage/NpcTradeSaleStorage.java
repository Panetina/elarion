package panetina.elarion.addons.npcs.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcTradeSaleRecord;
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

public final class NpcTradeSaleStorage {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Logger logger;
    private final Path fixedRoot;

    public NpcTradeSaleStorage(Logger logger) {
        this(logger, null);
    }

    public NpcTradeSaleStorage(Logger logger, Path fixedRoot) {
        this.logger = logger;
        this.fixedRoot = fixedRoot;
    }

    public Map<UUID, NpcTradeSaleRecord> load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return new LinkedHashMap<>();
        StoredState stored;
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            stored = GSON.fromJson(reader, StoredState.class);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to load NPC trade sale journal", exception);
        }
        if (stored == null) return new LinkedHashMap<>();
        if (stored.schemaVersion != SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported NPC trade sale schema " + stored.schemaVersion);
        }
        Map<UUID, NpcTradeSaleRecord> result = new LinkedHashMap<>();
        if (stored.sales != null) {
            for (NpcTradeSaleRecord record : stored.sales) {
                if (record == null || record.saleId() == null) continue;
                result.put(record.saleId(), record);
            }
        }
        return result;
    }

    public void save(MinecraftServer server, Map<UUID, NpcTradeSaleRecord> records) {
        StoredState stored = new StoredState();
        stored.sales = new ArrayList<>(records.values());
        JsonStateStorage.writeAtomic(file(server), GSON, stored, logger, "NPC trade sale journal");
    }

    public void saveChecked(MinecraftServer server, Map<UUID, NpcTradeSaleRecord> records) {
        StoredState stored = new StoredState();
        stored.sales = new ArrayList<>(records.values());
        try {
            JsonStateStorage.writeAtomicChecked(file(server), GSON, stored, "NPC trade sale journal");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist NPC trade sale journal", exception);
        }
    }

    private Path file(MinecraftServer server) {
        return root(server).resolve("trade-sales.json");
    }

    private Path root(MinecraftServer server) {
        return fixedRoot == null ? JsonStateStorage.addonStateRoot(server, "npcs") : fixedRoot;
    }

    private static final class StoredState {
        int schemaVersion = SCHEMA_VERSION;
        List<NpcTradeSaleRecord> sales = new ArrayList<>();
    }
}
