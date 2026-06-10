package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.model.ChronicleArchive;
import panetina.elarion.core.service.ElarionPerformanceMonitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class ChronicleArchiveStorage {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private final Logger logger;

    public ChronicleArchiveStorage(Logger logger) {
        this.logger = logger;
    }

    public Optional<ChronicleArchive> saveIfAbsent(MinecraftServer server, ChronicleArchive archive) {
        return saveIfAbsent(archiveDir(server), archive);
    }

    public Optional<ChronicleArchive> saveIfAbsent(Path directory, ChronicleArchive archive) {
        long started = System.nanoTime();
        Path path = archivePath(directory, archive.weekStart());
        try {
            if (Files.exists(path)) return load(path);
            Files.createDirectories(directory);
            writeAtomically(path, GSON.toJson(archive));
            ElarionPerformanceMonitor.record("chronicle-archive-write", System.nanoTime() - started);
            return Optional.of(archive);
        } catch (FileAlreadyExistsException exception) {
            return load(path);
        } catch (IOException | RuntimeException exception) {
            ElarionPerformanceMonitor.record("chronicle-archive-write-failed", System.nanoTime() - started);
            logger.error("Failed to save Chronicle archive {}", path, exception);
            return Optional.empty();
        }
    }

    public List<ChronicleArchive> loadRecent(MinecraftServer server, int maxWeeks) {
        return loadRecent(archiveDir(server), maxWeeks);
    }

    public List<ChronicleArchive> loadRecent(Path directory, int maxWeeks) {
        if (Files.notExists(directory)) return List.of();
        int safeMaxWeeks = Math.max(1, maxWeeks);
        List<ChronicleArchive> archives = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            for (Path path : files
                    .filter(value -> value.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.reverseOrder())
                    .limit(safeMaxWeeks)
                    .toList()) {
                load(path).ifPresent(archives::add);
            }
        } catch (IOException exception) {
            logger.error("Failed to list Chronicle archives", exception);
        }
        return List.copyOf(archives);
    }

    private Optional<ChronicleArchive> load(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return Optional.ofNullable(GSON.fromJson(content, ChronicleArchive.class));
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to read Chronicle archive {}", path, exception);
            return Optional.empty();
        }
    }

    private static Path archiveDir(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("elarion/chronicles/weekly");
    }

    private static Path archivePath(Path directory, String weekStart) {
        return directory.resolve(weekStart + ".json");
    }

    private static void writeAtomically(Path path, String content) throws IOException {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, content, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            Files.move(temporary, path);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
