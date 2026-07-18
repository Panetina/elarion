package panetina.elarion.addons.npcs.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcTradePurchaseRecord;
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

public final class NpcTradePurchaseStorage {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Logger logger;
    private final Path fixedRoot;

    public NpcTradePurchaseStorage(Logger logger) {
        this(logger, null);
    }

    public NpcTradePurchaseStorage(Logger logger, Path fixedRoot) {
        this.logger = logger;
        this.fixedRoot = fixedRoot;
    }

    public Map<UUID, NpcTradePurchaseRecord> load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return new LinkedHashMap<>();
        StoredState stored;
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            stored = GSON.fromJson(reader, StoredState.class);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to load NPC trade purchase journal", exception);
        }
        if (stored == null) return new LinkedHashMap<>();
        if (stored.schemaVersion != SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported NPC trade purchase schema " + stored.schemaVersion);
        }
        Map<UUID, NpcTradePurchaseRecord> result = new LinkedHashMap<>();
        if (stored.purchases != null) {
            for (NpcTradePurchaseRecord record : stored.purchases) {
                if (record == null || record.purchaseId() == null) continue;
                result.put(record.purchaseId(), record);
            }
        }
        return result;
    }

    public void save(MinecraftServer server, Map<UUID, NpcTradePurchaseRecord> records) {
        StoredState stored = new StoredState();
        stored.purchases = new ArrayList<>(records.values());
        JsonStateStorage.writeAtomic(file(server), GSON, stored, logger, "NPC trade purchase journal");
    }

    public void saveChecked(MinecraftServer server, Map<UUID, NpcTradePurchaseRecord> records) {
        StoredState stored = new StoredState();
        stored.purchases = new ArrayList<>(records.values());
        try {
            JsonStateStorage.writeAtomicChecked(file(server), GSON, stored, "NPC trade purchase journal");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist NPC trade purchase journal", exception);
        }
    }

    private Path file(MinecraftServer server) {
        return root(server).resolve("trade-purchases.json");
    }

    private Path root(MinecraftServer server) {
        return fixedRoot == null ? JsonStateStorage.addonStateRoot(server, "npcs") : fixedRoot;
    }

    private static final class StoredState {
        int schemaVersion = SCHEMA_VERSION;
        List<NpcTradePurchaseRecord> purchases = new ArrayList<>();
    }
}
