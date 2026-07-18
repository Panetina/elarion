package panetina.elarion.core.storage;

import com.google.gson.Gson;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.service.ElarionPerformanceMonitor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JsonStateStorage {
    private JsonStateStorage() {
    }

    public static Path elarionRoot(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("elarion");
    }

    public static Path addonStateRoot(MinecraftServer server, String addonId) {
        return elarionRoot(server).resolve("addon-state").resolve(addonId);
    }

    public static <S, T> T read(
            Path file,
            Gson gson,
            Class<S> storedType,
            Supplier<T> fallback,
            Function<S, T> mapper,
            Logger logger,
            String description
    ) {
        if (Files.notExists(file)) return fallback.get();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            S stored = gson.fromJson(reader, storedType);
            return stored == null ? fallback.get() : mapper.apply(stored);
        } catch (IOException | RuntimeException exception) {
            Path quarantine = quarantine(file, description, exception);
            logger.error("Failed to load {}; quarantined the unreadable state at {}",
                    description, quarantine, exception);
            return fallback.get();
        }
    }

    public static void writeAtomic(Path file, Gson gson, Object value, Logger logger, String description) {
        try {
            writeAtomicChecked(file, gson, value, description);
        } catch (IOException exception) {
            logger.error("Failed to save {}", description, exception);
            throw new IllegalStateException("Failed to save " + description + " at " + file, exception);
        }
    }

    public static void writeAtomicChecked(Path file, Gson gson, Object value, String description) throws IOException {
        long started = System.nanoTime();
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                gson.toJson(value, writer);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        } finally {
            ElarionPerformanceMonitor.record("json-state-save:" + description, System.nanoTime() - started);
        }
    }

    private static Path quarantine(Path file, String description, Exception loadFailure) {
        String suffix = ".corrupt-" + System.currentTimeMillis();
        Path candidate = file.resolveSibling(file.getFileName() + suffix);
        int collision = 0;
        while (Files.exists(candidate)) {
            collision++;
            candidate = file.resolveSibling(file.getFileName() + suffix + "-" + collision);
        }
        try {
            try {
                Files.move(file, candidate, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(file, candidate);
            }
            return candidate;
        } catch (IOException quarantineFailure) {
            loadFailure.addSuppressed(quarantineFailure);
            throw new IllegalStateException(
                    "Failed to quarantine unreadable " + description + " at " + file,
                    loadFailure);
        }
    }
}
