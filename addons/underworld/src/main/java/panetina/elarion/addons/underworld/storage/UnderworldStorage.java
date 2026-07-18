package panetina.elarion.addons.underworld.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;

public final class UnderworldStorage {
    private final Logger logger;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path explicitRoot;

    public UnderworldStorage(Logger logger) {
        this(logger, null);
    }

    public UnderworldStorage(Logger logger, Path explicitRoot) {
        this.logger = logger;
        this.explicitRoot = explicitRoot;
    }

    public UnderworldState load(MinecraftServer server) {
        return load(root(server));
    }

    public UnderworldState load(Path root) {
        return JsonStateStorage.read(
                root.resolve("state.json"), gson, UnderworldState.class,
                UnderworldState::new, UnderworldState::normalized,
                logger, "underworld state");
    }

    public void save(MinecraftServer server, UnderworldState state) {
        save(root(server), state);
    }

    public void save(Path root, UnderworldState state) {
        JsonStateStorage.writeAtomic(root.resolve("state.json"), gson,
                state == null ? new UnderworldState() : state, logger, "underworld state");
    }

    public Path root(MinecraftServer server) {
        return explicitRoot != null ? explicitRoot : JsonStateStorage.addonStateRoot(server, "underworld");
    }
}
