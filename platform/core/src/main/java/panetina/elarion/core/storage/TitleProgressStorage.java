package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.model.TitleProgressRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import panetina.elarion.core.api.reset.PlayerResetFiles;

public final class TitleProgressStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public TitleProgressStorage(Logger logger) {
        this.logger = logger;
    }

    public TitleProgressRecord load(MinecraftServer server, UUID uuid) {
        Path file = progressDir(server).resolve(uuid + ".json");
        if (Files.notExists(file)) return new TitleProgressRecord(uuid);
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredProgress stored = GSON.fromJson(reader, StoredProgress.class);
            return stored == null ? new TitleProgressRecord(uuid) : stored.toRecord(uuid);
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load Elarion title progress {}", uuid, exception);
            return new TitleProgressRecord(uuid);
        }
    }

    public void save(MinecraftServer server, TitleProgressRecord record) {
        JsonStateStorage.writeAtomic(
                progressDir(server).resolve(record.uuid() + ".json"),
                GSON,
                StoredProgress.from(record),
                logger,
                "Elarion title progress " + record.uuid());
    }

    public void deleteAll(MinecraftServer server) throws IOException {
        PlayerResetFiles.deleteTree(progressDir(server));
    }

    private static Path progressDir(MinecraftServer server) {
        return JsonStateStorage.elarionRoot(server).resolve("progression/title-progress");
    }

    private static final class StoredProgress {
        Map<String, Long> progressTicks = new LinkedHashMap<>();

        static StoredProgress from(TitleProgressRecord record) {
            StoredProgress stored = new StoredProgress();
            stored.progressTicks = new LinkedHashMap<>(record.progressTicks());
            return stored;
        }

        TitleProgressRecord toRecord(UUID uuid) {
            TitleProgressRecord record = new TitleProgressRecord(uuid);
            if (progressTicks != null) record.progressTicks().putAll(progressTicks);
            return record;
        }
    }
}
