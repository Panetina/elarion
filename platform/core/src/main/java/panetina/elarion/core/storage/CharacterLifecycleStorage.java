package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class CharacterLifecycleStorage {
    private final Logger logger;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public CharacterLifecycleStorage(Logger logger) {
        this.logger = logger;
    }

    public CharacterLifecycleState load(MinecraftServer server) {
        return load(file(server));
    }

    public void save(MinecraftServer server, CharacterLifecycleState state) {
        save(file(server), state);
    }

    public CharacterLifecycleState load(Path file) {
        return JsonStateStorage.read(file, gson, CharacterLifecycleState.class,
                CharacterLifecycleState::new, CharacterLifecycleState::normalized,
                logger, "character lifecycle state");
    }

    public void save(Path file, CharacterLifecycleState state) {
        JsonStateStorage.writeAtomic(file, gson, state, logger, "character lifecycle state");
    }

    private Path file(MinecraftServer server) {
        return JsonStateStorage.elarionRoot(server).resolve("core").resolve("characters").resolve("state.json");
    }
}
