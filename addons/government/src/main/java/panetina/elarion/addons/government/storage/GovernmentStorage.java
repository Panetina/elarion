package panetina.elarion.addons.government.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;

public final class GovernmentStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitRoot;

    public GovernmentStorage(Logger logger) {
        this(logger, null);
    }

    public GovernmentStorage(Logger logger, Path explicitRoot) {
        this.logger = logger;
        this.explicitRoot = explicitRoot;
    }

    public GovernmentState load(MinecraftServer server) {
        return load(root(server));
    }

    public GovernmentState load(Path root) {
        return JsonStateStorage.read(root.resolve("state.json"), GSON, GovernmentState.class,
                GovernmentState::new, GovernmentStorage::normalize, logger, "government-state");
    }

    public void save(MinecraftServer server, GovernmentState state) {
        save(root(server), state);
    }

    public void save(Path root, GovernmentState state) {
        JsonStateStorage.writeAtomic(root.resolve("state.json"), GSON, state, logger, "government-state");
    }

    private Path root(MinecraftServer server) {
        return explicitRoot != null ? explicitRoot : JsonStateStorage.addonStateRoot(server, "government");
    }

    private static GovernmentState normalize(GovernmentState state) {
        if (state.schemaVersion <= 0) state.schemaVersion = GovernmentState.CURRENT_SCHEMA_VERSION;
        if (state.schemaVersion != GovernmentState.CURRENT_SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported government state schema " + state.schemaVersion);
        }
        return state.copy();
    }
}
