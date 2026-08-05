package panetina.elarion.addons.guilds.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GuildStorage {
    private static final String STATE_FILE = "guilds.json";
    private static final String LEGACY_STATE_FILE = "groups.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitRoot;

    public GuildStorage(Logger logger) {
        this(logger, null);
    }

    public GuildStorage(Logger logger, Path explicitRoot) {
        this.logger = logger;
        this.explicitRoot = explicitRoot;
    }

    public GuildState load(MinecraftServer server) {
        return load(root(server));
    }

    public GuildState load(Path root) {
        Path current = root.resolve(STATE_FILE);
        if (Files.exists(current)) {
            return JsonStateStorage.read(current, GSON, GuildState.class,
                    GuildState::new, GuildState::copy, logger, "guilds-state");
        }
        Path legacy = legacyFile(root);
        if (!Files.exists(legacy)) {
            return JsonStateStorage.read(current, GSON, GuildState.class,
                    GuildState::new, GuildState::copy, logger, "guilds-state");
        }
        GuildState migrated = readLegacy(legacy);
        save(root, migrated);
        try {
            Files.move(legacy, legacy.resolveSibling(LEGACY_STATE_FILE + ".migrated-v1.bak"));
        } catch (IOException exception) {
            throw new IllegalStateException("Guild state was migrated but legacy backup could not be created: " + legacy,
                    exception);
        }
        logger.info("Migrated legacy group state {} to {}", legacy, current);
        return migrated;
    }

    public void save(MinecraftServer server, GuildState state) {
        save(root(server), state);
    }

    public void save(Path root, GuildState state) {
        JsonStateStorage.writeAtomic(root.resolve(STATE_FILE), GSON, state, logger, "guilds-state");
    }

    private Path root(MinecraftServer server) {
        return explicitRoot != null ? explicitRoot : JsonStateStorage.addonStateRoot(server, "guilds");
    }

    private Path legacyFile(Path root) {
        Path parent = root.toAbsolutePath().getParent();
        if (parent == null) throw new IllegalStateException("Guild storage root has no parent: " + root);
        return parent.resolve("groups").resolve(LEGACY_STATE_FILE);
    }

    private GuildState readLegacy(Path legacy) {
        try (Reader reader = Files.newBufferedReader(legacy, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            rename(root, "groups", "guilds");
            rename(root, "playerGroups", "playerGuilds");
            JsonElement invites = root.get("invites");
            if (invites != null && invites.isJsonObject()) {
                for (JsonElement invite : invites.getAsJsonObject().asMap().values()) {
                    if (invite.isJsonObject()) rename(invite.getAsJsonObject(), "groupId", "guildId");
                }
            }
            GuildState state = GSON.fromJson(root, GuildState.class);
            return state == null ? new GuildState() : state.copy();
        } catch (IOException | IllegalStateException exception) {
            throw new IllegalStateException("Unable to migrate legacy group state " + legacy, exception);
        }
    }

    private static void rename(JsonObject object, String legacyKey, String currentKey) {
        if (!object.has(currentKey) && object.has(legacyKey)) {
            object.add(currentKey, object.remove(legacyKey));
        }
    }
}
