package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.model.ElarionStoredNotification;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class NotificationStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitFile;

    public NotificationStorage(Logger logger) {
        this(logger, null);
    }

    public NotificationStorage(Logger logger, Path explicitFile) {
        this.logger = logger;
        this.explicitFile = explicitFile;
    }

    public List<ElarionStoredNotification> load(MinecraftServer server) {
        return load(file(server));
    }

    public List<ElarionStoredNotification> load(Path file) {
        StoredState state = JsonStateStorage.read(file, GSON, StoredState.class,
                StoredState::new, value -> value, logger, "notifications");
        return state.notifications == null ? List.of() : List.copyOf(state.notifications);
    }

    public void save(MinecraftServer server, List<ElarionStoredNotification> notifications) {
        save(file(server), notifications);
    }

    public void save(Path file, List<ElarionStoredNotification> notifications) {
        StoredState state = new StoredState();
        state.notifications = new ArrayList<>(notifications);
        JsonStateStorage.writeAtomic(file, GSON, state, logger, "notifications");
    }

    private Path file(MinecraftServer server) {
        return explicitFile != null
                ? explicitFile
                : JsonStateStorage.elarionRoot(server).resolve("notifications").resolve("notifications.json");
    }

    private static final class StoredState {
        List<ElarionStoredNotification> notifications = new ArrayList<>();
    }
}
