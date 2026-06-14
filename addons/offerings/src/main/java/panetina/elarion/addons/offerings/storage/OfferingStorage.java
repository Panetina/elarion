package panetina.elarion.addons.offerings.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.io.IOException;

public final class OfferingStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;
    private final Path explicitRoot;

    public OfferingStorage(Logger logger) {
        this(logger, null);
    }

    public OfferingStorage(Logger logger, Path explicitRoot) {
        this.logger = logger;
        this.explicitRoot = explicitRoot;
    }

    public OfferingState load(MinecraftServer server) {
        return JsonStateStorage.read(root(server).resolve("state.json"), GSON, OfferingState.class,
                OfferingState::new, OfferingState::copy, logger, "offerings-state");
    }

    public void save(MinecraftServer server, OfferingState state) {
        Path root = root(server);
        JsonStateStorage.writeAtomic(root.resolve("projects.json"), GSON, state.instances, logger,
                "offerings-projects");
        JsonStateStorage.writeAtomic(root.resolve("anchors.json"), GSON, state.anchors, logger,
                "offerings-anchors");
        JsonStateStorage.writeAtomic(root.resolve("state.json"), GSON, state, logger,
                "offerings-state");
    }

    public void saveChecked(MinecraftServer server, OfferingState state) throws IOException {
        Path root = root(server);
        JsonStateStorage.writeAtomicChecked(root.resolve("projects.json"), GSON, state.instances,
                "offerings-projects");
        JsonStateStorage.writeAtomicChecked(root.resolve("anchors.json"), GSON, state.anchors,
                "offerings-anchors");
        JsonStateStorage.writeAtomicChecked(root.resolve("state.json"), GSON, state,
                "offerings-state");
    }

    public OfferingState load(Path root) {
        return JsonStateStorage.read(root.resolve("state.json"), GSON, OfferingState.class,
                OfferingState::new, OfferingState::copy, logger, "offerings-state-test");
    }

    public void save(Path root, OfferingState state) {
        JsonStateStorage.writeAtomic(root.resolve("state.json"), GSON, state, logger,
                "offerings-state-test");
    }

    private Path root(MinecraftServer server) {
        return explicitRoot != null ? explicitRoot : JsonStateStorage.addonStateRoot(server, "offerings");
    }
}
