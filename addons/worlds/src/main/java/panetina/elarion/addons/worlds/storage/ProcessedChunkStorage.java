package panetina.elarion.addons.worlds.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProcessedChunkStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type FILE_TYPE = new TypeToken<Map<String, List<Long>>>() {}.getType();
    private final Logger logger;

    public ProcessedChunkStorage(Logger logger) {
        this.logger = logger;
    }

    public Map<String, Set<Long>> load(MinecraftServer server) {
        Path file = path(server);
        if (Files.notExists(file)) return new HashMap<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Map<String, List<Long>> stored = GSON.fromJson(reader, FILE_TYPE);
            Map<String, Set<Long>> result = new HashMap<>();
            if (stored != null) stored.forEach((world, chunks) -> result.put(world, new HashSet<>(chunks)));
            return result;
        } catch (IOException | RuntimeException exception) {
            logger.error("Unable to load processed world chunks from {}", file, exception);
            return new HashMap<>();
        }
    }

    public void save(MinecraftServer server, Map<String, Set<Long>> processed) {
        Path file = path(server);
        Map<String, List<Long>> stored = new HashMap<>();
        processed.forEach((world, chunks) -> stored.put(world, chunks.stream().sorted().toList()));
        JsonStateStorage.writeAtomic(file, GSON, stored, logger, "processed Elarion world chunks");
    }

    private static Path path(MinecraftServer server) {
        return JsonStateStorage.addonStateRoot(server, "worlds").resolve("processed-chunks.json");
    }
}
