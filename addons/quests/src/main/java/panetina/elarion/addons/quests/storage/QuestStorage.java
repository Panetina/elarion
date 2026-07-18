package panetina.elarion.addons.quests.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;

public final class QuestStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitRoot;

    public QuestStorage(Logger logger) {
        this(logger, null);
    }

    public QuestStorage(Logger logger, Path explicitRoot) {
        this.logger = logger;
        this.explicitRoot = explicitRoot;
    }

    public QuestRuntimeState load(MinecraftServer server) {
        return load(root(server));
    }

    public QuestRuntimeState load(Path root) {
        return JsonStateStorage.read(root.resolve("state.json"), GSON, QuestRuntimeState.class,
                QuestRuntimeState::new, QuestStorage::normalize, logger, "quests-state");
    }

    public void save(MinecraftServer server, QuestRuntimeState state) {
        save(root(server), state);
    }

    public void save(Path root, QuestRuntimeState state) {
        JsonStateStorage.writeAtomic(root.resolve("state.json"), GSON,
                state == null ? new QuestRuntimeState() : state, logger, "quests-state");
    }

    private Path root(MinecraftServer server) {
        return explicitRoot != null ? explicitRoot : JsonStateStorage.addonStateRoot(server, "quests");
    }

    private static QuestRuntimeState normalize(QuestRuntimeState state) {
        if (state.schemaVersion <= 0) state.schemaVersion = QuestRuntimeState.CURRENT_SCHEMA_VERSION;
        if (state.schemaVersion != QuestRuntimeState.CURRENT_SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported quest state schema " + state.schemaVersion);
        }
        return state.copy();
    }
}
