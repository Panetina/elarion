package panetina.elarion.addons.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.service.ElarionDiagnostics;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class SecurityEvidenceService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Logger logger;
    private final Map<String, Long> countsByType = new LinkedHashMap<>();
    private long totalEvidence;
    private long lastEvidenceAt;
    private MinecraftServer server;
    private boolean dirty;

    SecurityEvidenceService(Logger logger) {
        this.logger = logger;
    }

    void register() {
        ElarionDiagnostics.register("security", this::diagnostics);
        ServerLifecycleEvents.SERVER_STARTED.register(this::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(ignored -> save());
    }

    synchronized void record(String type) {
        if (type == null || type.isBlank()) {
            type = "unknown";
        }
        countsByType.merge(type, 1L, Long::sum);
        totalEvidence++;
        lastEvidenceAt = System.currentTimeMillis();
        dirty = true;
    }

    synchronized Map<String, String> diagnostics() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("state", server == null ? "not-bound" : "active");
        values.put("totalEvidence", Long.toString(totalEvidence));
        values.put("lastEvidenceAt", lastEvidenceAt == 0L ? "never" : Long.toString(lastEvidenceAt));
        values.put("dirty", Boolean.toString(dirty));
        values.put("types", countsByType.isEmpty() ? "(none)" : formatCounts());
        return values;
    }

    private synchronized void load(MinecraftServer server) {
        this.server = server;
        StoredState stored = JsonStateStorage.read(file(server), GSON, StoredState.class,
                StoredState::new, state -> state, logger, "security evidence state");
        countsByType.clear();
        countsByType.putAll(stored.countsByType == null ? Map.of() : stored.countsByType);
        totalEvidence = stored.totalEvidence;
        lastEvidenceAt = stored.lastEvidenceAt;
        dirty = false;
    }

    private synchronized void save() {
        if (server == null || !dirty) {
            return;
        }
        JsonStateStorage.writeAtomic(file(server), GSON,
                new StoredState(totalEvidence, lastEvidenceAt, countsByType),
                logger, "security evidence state");
        dirty = false;
    }

    private String formatCounts() {
        return countsByType.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("(none)");
    }

    private static Path file(MinecraftServer server) {
        return JsonStateStorage.addonStateRoot(server, "security").resolve("evidence.json");
    }

    private static final class StoredState {
        long totalEvidence;
        long lastEvidenceAt;
        Map<String, Long> countsByType = new LinkedHashMap<>();

        StoredState() {
        }

        StoredState(long totalEvidence, long lastEvidenceAt, Map<String, Long> countsByType) {
            this.totalEvidence = totalEvidence;
            this.lastEvidenceAt = lastEvidenceAt;
            this.countsByType = new LinkedHashMap<>(countsByType);
        }
    }
}
