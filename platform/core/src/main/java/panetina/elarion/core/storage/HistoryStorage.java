package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.model.HistoryEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class HistoryStorage {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final DateTimeFormatter FILE_MONTH =
            DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);
    private final Logger logger;

    public HistoryStorage(Logger logger) {
        this.logger = logger;
    }

    public synchronized void append(MinecraftServer server, HistoryEvent event) {
        append(historyDir(server), event);
    }

    public synchronized void append(Path directory, HistoryEvent event) {
        Path file = directory.resolve(FILE_MONTH.format(Instant.ofEpochMilli(event.timestamp())) + ".jsonl");
        try {
            Files.createDirectories(directory);
            Files.writeString(file, GSON.toJson(event) + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            logger.error("Failed to append Elarion history event {}", event.id(), exception);
        }
    }

    public List<HistoryEvent> loadAll(MinecraftServer server) {
        return loadAll(historyDir(server));
    }

    public List<HistoryEvent> loadAll(Path directory) {
        if (Files.notExists(directory)) return List.of();
        List<HistoryEvent> events = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                    .sorted()
                    .forEach(path -> readFile(path, events));
        } catch (IOException exception) {
            logger.error("Failed to list Elarion history files", exception);
        }
        events.sort(Comparator.comparingLong(HistoryEvent::timestamp).reversed());
        return List.copyOf(events);
    }

    private void readFile(Path path, List<HistoryEvent> events) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                try {
                    HistoryEvent event = GSON.fromJson(line, HistoryEvent.class);
                    if (event != null) events.add(event);
                } catch (RuntimeException exception) {
                    logger.error("Invalid history entry at {}:{}", path, lineNumber, exception);
                }
            }
        } catch (IOException exception) {
            logger.error("Failed to read Elarion history file {}", path, exception);
        }
    }

    private static Path historyDir(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("elarion/history");
    }
}
