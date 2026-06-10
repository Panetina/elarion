package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RealmDeliveryStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public RealmDeliveryStorage(Logger logger) {
        this.logger = logger;
    }

    public List<PendingDelivery> load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return List.of();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredDeliveries stored = GSON.fromJson(reader, StoredDeliveries.class);
            return stored == null || stored.deliveries == null ? List.of() : List.copyOf(stored.deliveries);
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load realm deliveries", exception);
            return List.of();
        }
    }

    public void save(MinecraftServer server, List<PendingDelivery> deliveries) {
        StoredDeliveries stored = new StoredDeliveries();
        stored.deliveries = new ArrayList<>(deliveries);
        JsonStateStorage.writeAtomic(file(server), GSON, stored, logger, "realm deliveries");
    }

    private static Path file(MinecraftServer server) {
        return JsonStateStorage.addonStateRoot(server, "realms").resolve("deliveries.json");
    }

    public static final class PendingDelivery {
        public UUID playerId;
        public String type;
        public String realmId;
        public String payload;
        public long createdAt;

        public PendingDelivery() {
        }

        public PendingDelivery(UUID playerId, String type, String realmId, String payload) {
            this.playerId = playerId;
            this.type = type;
            this.realmId = realmId;
            this.payload = payload;
            this.createdAt = Instant.now().toEpochMilli();
        }
    }

    private static final class StoredDeliveries {
        List<PendingDelivery> deliveries = new ArrayList<>();
    }
}
