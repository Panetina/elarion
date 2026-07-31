package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.model.DeferredRewardGrant;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DeferredRewardGrantStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitFile;

    public DeferredRewardGrantStorage(Logger logger) {
        this(logger, null);
    }

    public DeferredRewardGrantStorage(Logger logger, Path explicitFile) {
        this.logger = logger;
        this.explicitFile = explicitFile;
    }

    public Map<String, DeferredRewardGrant> load(MinecraftServer server) {
        StoredState state = JsonStateStorage.read(file(server), GSON, StoredState.class,
                StoredState::new, DeferredRewardGrantStorage::normalize, logger, "deferred reward grants");
        return new LinkedHashMap<>(state.grants);
    }

    public void save(MinecraftServer server, Map<String, DeferredRewardGrant> grants) {
        save(file(server), grants);
    }

    public Map<String, DeferredRewardGrant> load(Path file) {
        StoredState state = JsonStateStorage.read(file, GSON, StoredState.class,
                StoredState::new, DeferredRewardGrantStorage::normalize, logger, "deferred reward grants test");
        return new LinkedHashMap<>(state.grants);
    }

    public void save(Path file, Map<String, DeferredRewardGrant> grants) {
        StoredState state = new StoredState();
        state.grants = new LinkedHashMap<>(grants);
        JsonStateStorage.writeAtomic(file, GSON, state, logger, "deferred reward grants");
    }

    private Path file(MinecraftServer server) {
        return explicitFile != null
                ? explicitFile
                : JsonStateStorage.elarionRoot(server).resolve("reward-grants.json");
    }

    private static StoredState normalize(StoredState state) {
        if (state.grants == null) state.grants = new LinkedHashMap<>();
        else state.grants.values().removeIf(grant -> grant == null);
        return state;
    }

    private static final class StoredState {
        Map<String, DeferredRewardGrant> grants = new LinkedHashMap<>();
    }
}
