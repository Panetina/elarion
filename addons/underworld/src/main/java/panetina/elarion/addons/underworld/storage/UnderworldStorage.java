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

    public UnderworldStorage(Logger logger) {
        this.logger = logger;
    }

    public UnderworldState load(MinecraftServer server) {
        Path file = file(server);
        return JsonStateStorage.read(
                file, gson, UnderworldState.class, UnderworldState::new, state -> state,
                logger, "underworld state");
    }

    public void save(MinecraftServer server, UnderworldState state) {
        JsonStateStorage.writeAtomic(file(server), gson, state, logger, "underworld state");
    }

    public Path root(MinecraftServer server) {
        return JsonStateStorage.addonStateRoot(server, "underworld");
    }

    private Path file(MinecraftServer server) {
        return root(server).resolve("state.json");
    }
}
