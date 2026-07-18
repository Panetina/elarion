package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.model.WorldheartAuthority;
import panetina.elarion.core.model.WorldheartAuthorityType;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class WorldheartAuthorityStorage {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path fixedRoot;

    public WorldheartAuthorityStorage(Logger logger) {
        this(logger, null);
    }

    public WorldheartAuthorityStorage(Logger logger, Path fixedRoot) {
        this.logger = logger;
        this.fixedRoot = fixedRoot;
    }

    public WorldheartAuthority load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return WorldheartAuthority.defaultSystem();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredAuthority stored = GSON.fromJson(reader, StoredAuthority.class);
            if (stored == null || stored.schemaVersion != SCHEMA_VERSION) {
                throw new IllegalStateException("Unsupported or empty Worldheart authority state");
            }
            WorldheartAuthorityType type = WorldheartAuthorityType.valueOf(stored.authorityType);
            String systemName = stored.systemDisplayName == null || stored.systemDisplayName.isBlank()
                    ? WorldheartAuthority.DEFAULT_SYSTEM_DISPLAY_NAME : stored.systemDisplayName;
            if (type == WorldheartAuthorityType.SYSTEM) {
                if (stored.playerId != null && !stored.playerId.isBlank()) {
                    throw new IllegalStateException("SYSTEM Worldheart authority cannot contain playerId");
                }
                return WorldheartAuthority.system(systemName, stored.changedAt);
            }
            if (stored.playerId == null || stored.playerId.isBlank()) {
                throw new IllegalStateException("PLAYER Worldheart authority requires playerId");
            }
            return WorldheartAuthority.player(UUID.fromString(stored.playerId), systemName, stored.changedAt);
        } catch (Exception exception) {
            logger.error("Invalid Worldheart authority state at {}; using safe system authority", file, exception);
            return WorldheartAuthority.defaultSystem();
        }
    }

    public void save(MinecraftServer server, WorldheartAuthority authority) {
        try {
            JsonStateStorage.writeAtomicChecked(file(server), GSON, StoredAuthority.from(authority),
                    "Worldheart authority");
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Unable to persist Worldheart authority", exception);
        }
    }

    private Path file(MinecraftServer server) {
        Path root = fixedRoot == null ? JsonStateStorage.elarionRoot(server).resolve("worldheart") : fixedRoot;
        return root.resolve("authority.json");
    }

    private static final class StoredAuthority {
        int schemaVersion;
        String authorityType;
        String playerId;
        String systemDisplayName;
        long changedAt;

        static StoredAuthority from(WorldheartAuthority authority) {
            StoredAuthority stored = new StoredAuthority();
            stored.schemaVersion = SCHEMA_VERSION;
            stored.authorityType = authority.type().name();
            stored.playerId = authority.rulerId() == null ? "" : authority.rulerId().toString();
            stored.systemDisplayName = authority.systemDisplayName();
            stored.changedAt = authority.changedAt();
            return stored;
        }
    }
}
