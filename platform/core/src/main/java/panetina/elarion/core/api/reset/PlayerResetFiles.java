package panetina.elarion.core.api.reset;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public final class PlayerResetFiles {
    private PlayerResetFiles() {
    }

    public static long countRegularFiles(Path path) throws IOException {
        if (path == null || Files.notExists(path)) return 0L;
        try (Stream<Path> files = Files.walk(path)) {
            return files.filter(Files::isRegularFile).count();
        }
    }

    public static long countJsonArrayEntries(Path path) throws IOException {
        if (path == null || Files.notExists(path)) return 0L;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement value = JsonParser.parseReader(reader);
            if (!value.isJsonArray()) throw new IOException("Expected a JSON array in " + path);
            return value.getAsJsonArray().size();
        } catch (RuntimeException exception) {
            throw new IOException("Could not read JSON array from " + path, exception);
        }
    }

    public static void writeEmptyJsonArrayAtomic(Path path) throws IOException {
        if (path == null) throw new IllegalArgumentException("path");
        Path absolute = path.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (parent == null) throw new IOException("JSON file has no parent directory: " + absolute);
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, absolute.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, "[]\n", StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static void copyTree(Path source, Path destination) throws IOException {
        if (source == null || Files.notExists(source)) return;
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.sorted().toList()) {
                Path target = destination.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) Files.createDirectories(target);
                else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }
    }

    public static void deleteTree(Path path) throws IOException {
        if (path == null || Files.notExists(path)) return;
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path value : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(value);
        }
    }
}
