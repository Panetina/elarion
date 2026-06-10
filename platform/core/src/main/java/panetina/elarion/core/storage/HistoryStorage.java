package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.service.ElarionPerformanceMonitor;
import panetina.elarion.core.service.ElarionTaskService;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class HistoryStorage {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final DateTimeFormatter FILE_MONTH =
            DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);
    private static final int FLUSH_THRESHOLD = 32;
    private static final long FLUSH_INTERVAL_MILLIS = 5_000L;
    private final Logger logger;
    private final Map<Path, List<String>> pendingLines = new LinkedHashMap<>();
    private final List<CompletableFuture<Void>> activeWrites = new ArrayList<>();
    private ElarionTaskService tasks;
    private long lastFlushAt = System.currentTimeMillis();

    public HistoryStorage(Logger logger) {
        this.logger = logger;
    }

    public synchronized void setTaskService(ElarionTaskService tasks) {
        this.tasks = tasks;
    }

    public void append(MinecraftServer server, HistoryEvent event) {
        append(historyDir(server), event);
    }

    public void append(Path directory, HistoryEvent event) {
        Path file = directory.resolve(FILE_MONTH.format(Instant.ofEpochMilli(event.timestamp())) + ".jsonl");
        boolean shouldFlush;
        synchronized (this) {
            pendingLines.computeIfAbsent(file, ignored -> new ArrayList<>())
                    .add(GSON.toJson(event) + System.lineSeparator());
            ElarionPerformanceMonitor.record("history-write-queued", 0L);
            shouldFlush = pendingSizeLocked() >= FLUSH_THRESHOLD;
        }
        if (shouldFlush) flushAsync();
    }

    public void tick() {
        boolean shouldFlush;
        synchronized (this) {
            shouldFlush = !pendingLines.isEmpty()
                    && System.currentTimeMillis() - lastFlushAt >= FLUSH_INTERVAL_MILLIS;
        }
        if (shouldFlush) flushAsync();
    }

    public void flushBlocking() {
        while (true) {
            List<CompletableFuture<Void>> writes;
            synchronized (this) {
                writes = new ArrayList<>(activeWrites);
                activeWrites.clear();
            }
            for (CompletableFuture<Void> write : writes) {
                write.join();
            }

            Map<Path, List<String>> batch = drainPending();
            if (batch.isEmpty()) return;
            writeBatch(batch);
        }
    }

    public void flushAsync() {
        Map<Path, List<String>> batch = drainPending();
        if (batch.isEmpty()) return;
        ElarionTaskService taskService;
        synchronized (this) {
            taskService = tasks;
        }
        if (taskService == null) {
            writeBatch(batch);
            return;
        }
        CompletableFuture<Void> write = taskService.submitIo("history-write", () -> writeBatch(batch));
        synchronized (this) {
            activeWrites.add(write);
        }
        write.whenComplete((ignored, throwable) -> {
            synchronized (this) {
                activeWrites.remove(write);
            }
        });
    }

    private Map<Path, List<String>> drainPending() {
        synchronized (this) {
            if (pendingLines.isEmpty()) return Map.of();
            Map<Path, List<String>> batch = new LinkedHashMap<>();
            pendingLines.forEach((path, lines) -> batch.put(path, List.copyOf(lines)));
            pendingLines.clear();
            lastFlushAt = System.currentTimeMillis();
            return batch;
        }
    }

    private void writeBatch(Map<Path, List<String>> batch) {
        long started = System.nanoTime();
        try {
            for (Map.Entry<Path, List<String>> entry : batch.entrySet()) {
                Files.createDirectories(entry.getKey().getParent());
                Files.writeString(entry.getKey(), String.join("", entry.getValue()),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException exception) {
            ElarionPerformanceMonitor.record("history-write-flush-failed", System.nanoTime() - started);
            logger.error("Failed to flush Elarion history batch", exception);
        } finally {
            ElarionPerformanceMonitor.record("history-write-batch", System.nanoTime() - started);
        }
    }

    public List<HistoryEvent> loadAll(MinecraftServer server) {
        return loadAll(historyDir(server));
    }

    public List<HistoryEvent> loadAll(Path directory) {
        flushBlocking();
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

    public List<HistoryEvent> queryRecent(
            MinecraftServer server,
            Predicate<HistoryEvent> filter,
            int limit,
            int maxMonthlyFiles
    ) {
        return queryRecent(historyDir(server), filter, limit, maxMonthlyFiles);
    }

    public List<HistoryEvent> queryRecent(
            Path directory,
            Predicate<HistoryEvent> filter,
            int limit,
            int maxMonthlyFiles
    ) {
        flushBlocking();
        if (Files.notExists(directory)) return List.of();
        int safeLimit = Math.max(1, limit);
        int safeMaxMonthlyFiles = Math.max(1, maxMonthlyFiles);
        List<HistoryEvent> events = new ArrayList<>(safeLimit);
        try (Stream<Path> files = Files.list(directory)) {
            for (Path path : files
                    .filter(value -> value.getFileName().toString().endsWith(".jsonl"))
                    .sorted(Comparator.reverseOrder())
                    .limit(safeMaxMonthlyFiles)
                    .toList()) {
                List<HistoryEvent> monthlyEvents = new ArrayList<>();
                readFile(path, monthlyEvents);
                monthlyEvents.stream()
                        .sorted(Comparator.comparingLong(HistoryEvent::timestamp).reversed())
                        .filter(filter)
                        .forEach(event -> {
                            if (events.size() < safeLimit) events.add(event);
                        });
                if (events.size() >= safeLimit) break;
            }
        } catch (IOException exception) {
            logger.error("Failed to query Elarion history files", exception);
        }
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

    private int pendingSizeLocked() {
        int total = 0;
        for (List<String> lines : pendingLines.values()) {
            total += lines.size();
        }
        return total;
    }
}
