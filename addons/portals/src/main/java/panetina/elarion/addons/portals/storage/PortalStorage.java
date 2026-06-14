package panetina.elarion.addons.portals.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;

public final class PortalStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitRoot;

    public PortalStorage(Logger logger) {
        this(logger, null);
    }

    public PortalStorage(Logger logger, Path explicitRoot) {
        this.logger = logger;
        this.explicitRoot = explicitRoot;
    }

    public PortalState load(MinecraftServer server) {
        return JsonStateStorage.read(root(server).resolve("state.json"), GSON, PortalState.class,
                PortalState::new, PortalState::copy, logger, "portals-state");
    }

    public void save(MinecraftServer server, PortalState state) {
        JsonStateStorage.writeAtomic(root(server).resolve("state.json"), GSON, state, logger, "portals-state");
    }

    public PortalState load(Path root) {
        return JsonStateStorage.read(root.resolve("state.json"), GSON, PortalState.class,
                PortalState::new, PortalState::copy, logger, "portals-state-test");
    }

    public void save(Path root, PortalState state) {
        JsonStateStorage.writeAtomic(root.resolve("state.json"), GSON, state, logger, "portals-state-test");
    }

    private Path root(MinecraftServer server) {
        return explicitRoot != null ? explicitRoot : JsonStateStorage.addonStateRoot(server, "portals");
    }
}
