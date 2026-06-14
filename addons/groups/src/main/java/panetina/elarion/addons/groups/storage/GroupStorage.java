package panetina.elarion.addons.groups.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;

public final class GroupStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitRoot;

    public GroupStorage(Logger logger) {
        this(logger, null);
    }

    public GroupStorage(Logger logger, Path explicitRoot) {
        this.logger = logger;
        this.explicitRoot = explicitRoot;
    }

    public GroupState load(MinecraftServer server) {
        return load(root(server));
    }

    public GroupState load(Path root) {
        return JsonStateStorage.read(root.resolve("groups.json"), GSON, GroupState.class,
                GroupState::new, GroupState::copy, logger, "groups-state");
    }

    public void save(MinecraftServer server, GroupState state) {
        save(root(server), state);
    }

    public void save(Path root, GroupState state) {
        JsonStateStorage.writeAtomic(root.resolve("groups.json"), GSON, state, logger, "groups-state");
    }

    private Path root(MinecraftServer server) {
        return explicitRoot != null ? explicitRoot : JsonStateStorage.addonStateRoot(server, "groups");
    }
}
