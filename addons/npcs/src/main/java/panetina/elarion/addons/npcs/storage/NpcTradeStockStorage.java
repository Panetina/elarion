package panetina.elarion.addons.npcs.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.model.NpcTradeStockRecord;
import panetina.elarion.core.storage.JsonStateStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NpcTradeStockStorage {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final Logger logger;
    private final Path fixedRoot;

    public NpcTradeStockStorage(Logger logger) {
        this(logger, null);
    }

    public NpcTradeStockStorage(Logger logger, Path fixedRoot) {
        this.logger = logger;
        this.fixedRoot = fixedRoot;
    }

    public Map<String, NpcTradeStockRecord> load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return new LinkedHashMap<>();
        StoredState stored;
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            stored = GSON.fromJson(reader, StoredState.class);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to load NPC trade stock state", exception);
        }
        if (stored == null) return new LinkedHashMap<>();
        if (stored.schemaVersion != SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported NPC trade stock schema " + stored.schemaVersion);
        }
        Map<String, NpcTradeStockRecord> result = new LinkedHashMap<>();
        if (stored.stocks != null) {
            for (NpcTradeStockRecord record : stored.stocks) {
                if (record == null || record.npcId() == null || record.offerId().isBlank()) continue;
                result.put(key(record.npcId().toString(), record.offerId()), record);
            }
        }
        return result;
    }

    public void save(MinecraftServer server, Map<String, NpcTradeStockRecord> records) {
        StoredState stored = new StoredState();
        stored.stocks = new ArrayList<>(records.values());
        JsonStateStorage.writeAtomic(file(server), GSON, stored, logger, "NPC trade stock state");
    }

    public void saveChecked(MinecraftServer server, Map<String, NpcTradeStockRecord> records) {
        StoredState stored = new StoredState();
        stored.stocks = new ArrayList<>(records.values());
        try {
            JsonStateStorage.writeAtomicChecked(file(server), GSON, stored, "NPC trade stock state");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist NPC trade stock state", exception);
        }
    }

    public static String key(String npcId, String offerId) {
        return (npcId == null ? "" : npcId) + "::" + (offerId == null ? "" : offerId);
    }

    private Path file(MinecraftServer server) {
        return root(server).resolve("trade-stock.json");
    }

    private Path root(MinecraftServer server) {
        return fixedRoot == null ? JsonStateStorage.addonStateRoot(server, "npcs") : fixedRoot;
    }

    private static final class StoredState {
        int schemaVersion = SCHEMA_VERSION;
        List<NpcTradeStockRecord> stocks = new ArrayList<>();
    }
}
