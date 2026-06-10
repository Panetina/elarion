package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.model.HistoryIndexEntry;
import panetina.elarion.core.model.HistoryMonthIndex;
import panetina.elarion.core.service.ElarionPerformanceMonitor;
import panetina.elarion.core.service.ElarionTaskService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class HistoryIndexStorage {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final DateTimeFormatter FILE_MONTH =
            DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);
    private static final int FLUSH_THRESHOLD = 32;
    private static final long FLUSH_INTERVAL_MILLIS = 5_000L;

    private final Logger logger;
    private final Map<String, List<HistoryIndexEntry>> pendingEntries = new LinkedHashMap<>();
    private ElarionTaskService tasks;
    private CompletableFuture<Void> activeWrite;
    private long lastFlushAt = System.currentTimeMillis();

    public HistoryIndexStorage(Logger logger) {
        this.logger = logger;
    }

    public synchronized void setTaskService(ElarionTaskService tasks) {
        this.tasks = tasks;
    }

    public void append(MinecraftServer server, HistoryEvent event) {
        append(indexDir(server), event);
    }

    public void append(Path directory, HistoryEvent event) {
        appendEntry(directory, HistoryIndexEntry.from(event));
    }

    private void appendEntry(Path directory, HistoryIndexEntry entry) {
        String month = FILE_MONTH.format(Instant.ofEpochMilli(entry.timestamp()));
        boolean shouldFlush;
        synchronized (this) {
            pendingEntries.computeIfAbsent(directory.resolve(month + ".json").toString(), ignored -> new ArrayList<>())
                    .add(entry);
            ElarionPerformanceMonitor.record("history-index-queued", 0L);
            shouldFlush = pendingSizeLocked() >= FLUSH_THRESHOLD;
        }
        if (shouldFlush) flushAsync();
    }

    public void tick() {
        boolean shouldFlush;
        synchronized (this) {
            shouldFlush = activeWrite == null
                    && !pendingEntries.isEmpty()
                    && System.currentTimeMillis() - lastFlushAt >= FLUSH_INTERVAL_MILLIS;
        }
        if (shouldFlush) flushAsync();
    }

    public void flushBlocking() {
        while (true) {
            CompletableFuture<Void> write;
            synchronized (this) {
                write = activeWrite;
            }
            if (write != null) {
                write.join();
                continue;
            }

            Map<Path, List<HistoryIndexEntry>> batch = drainPending();
            if (batch.isEmpty()) return;
            writeBatch(batch);
        }
    }

    public void flushAsync() {
        Map<Path, List<HistoryIndexEntry>> batch;
        ElarionTaskService taskService;
        synchronized (this) {
            if (activeWrite != null || pendingEntries.isEmpty()) return;
            batch = drainPendingLocked();
            taskService = tasks;
        }
        if (taskService == null) {
            writeBatch(batch);
            return;
        }
        CompletableFuture<Void> write = taskService.submitIo("history-index-write", () -> writeBatch(batch));
        synchronized (this) {
            activeWrite = write;
        }
        write.whenComplete((ignored, throwable) -> {
            synchronized (this) {
                if (activeWrite == write) activeWrite = null;
            }
        });
    }

    public List<HistoryIndexEntry> queryEntries(
            MinecraftServer server,
            Predicate<HistoryIndexEntry> filter,
            int limit,
            int maxMonthlyFiles
    ) {
        return queryEntries(indexDir(server), filter, limit, maxMonthlyFiles);
    }

    public List<HistoryIndexEntry> queryEntries(
            Path directory,
            Predicate<HistoryIndexEntry> filter,
            int limit,
            int maxMonthlyFiles
    ) {
        int safeLimit = Math.max(1, limit);
        List<HistoryIndexEntry> results = new ArrayList<>(safeLimit);
        for (HistoryMonthIndex month : loadRecentMonths(directory, maxMonthlyFiles)) {
            month.entries().stream()
                    .filter(filter)
                    .forEach(entry -> {
                        if (results.size() < safeLimit) results.add(entry);
                    });
            if (results.size() >= safeLimit) break;
        }
        return List.copyOf(results);
    }

    public List<HistoryMonthIndex> loadRecentMonths(MinecraftServer server, int maxMonthlyFiles) {
        return loadRecentMonths(indexDir(server), maxMonthlyFiles);
    }

    public List<HistoryMonthIndex> loadRecentMonths(Path directory, int maxMonthlyFiles) {
        flushBlocking();
        if (Files.notExists(directory)) return List.of();
        int safeMaxMonthlyFiles = Math.max(1, maxMonthlyFiles);
        List<HistoryMonthIndex> indexes = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            for (Path path : files
                    .filter(value -> value.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.reverseOrder())
                    .limit(safeMaxMonthlyFiles)
                    .toList()) {
                indexes.add(readIndex(path));
            }
        } catch (IOException exception) {
            logger.error("Failed to list Elarion history indexes", exception);
        }
        return List.copyOf(indexes);
    }

    private Map<Path, List<HistoryIndexEntry>> drainPending() {
        synchronized (this) {
            return drainPendingLocked();
        }
    }

    private Map<Path, List<HistoryIndexEntry>> drainPendingLocked() {
        if (pendingEntries.isEmpty()) return Map.of();
        Map<Path, List<HistoryIndexEntry>> batch = new LinkedHashMap<>();
        pendingEntries.forEach((path, entries) -> batch.put(Path.of(path), List.copyOf(entries)));
        pendingEntries.clear();
        lastFlushAt = System.currentTimeMillis();
        return batch;
    }

    private void writeBatch(Map<Path, List<HistoryIndexEntry>> batch) {
        long started = System.nanoTime();
        try {
            for (Map.Entry<Path, List<HistoryIndexEntry>> entry : batch.entrySet()) {
                Files.createDirectories(entry.getKey().getParent());
                StoredMonthIndex stored = readStoredIndex(entry.getKey());
                merge(stored, monthName(entry.getKey()), entry.getValue());
                Files.writeString(entry.getKey(), GSON.toJson(stored), StandardCharsets.UTF_8);
            }
        } catch (IOException | RuntimeException exception) {
            ElarionPerformanceMonitor.record("history-index-flush-failed", System.nanoTime() - started);
            logger.error("Failed to flush Elarion history index", exception);
        } finally {
            ElarionPerformanceMonitor.record("history-index-write", System.nanoTime() - started);
        }
    }

    private HistoryMonthIndex readIndex(Path path) {
        StoredMonthIndex stored = readStoredIndex(path);
        return new HistoryMonthIndex(
                stored.month == null || stored.month.isBlank() ? monthName(path) : stored.month,
                stored.firstTimestamp,
                stored.lastTimestamp,
                stored.entries.size(),
                stored.categoryCounts,
                stored.typeCounts,
                stored.realmCounts,
                stored.playerCounts,
                stored.entries);
    }

    private StoredMonthIndex readStoredIndex(Path path) {
        if (Files.notExists(path)) return new StoredMonthIndex(monthName(path));
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            StoredMonthIndex stored = GSON.fromJson(content, StoredMonthIndex.class);
            return stored == null ? new StoredMonthIndex(monthName(path)) : stored.normalized(monthName(path));
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to read Elarion history index {}", path, exception);
            return new StoredMonthIndex(monthName(path));
        }
    }

    private static void merge(StoredMonthIndex stored, String month, List<HistoryIndexEntry> additions) {
        stored.month = month;
        Set<UUID> knownIds = new LinkedHashSet<>();
        stored.entries.forEach(entry -> knownIds.add(entry.eventId()));
        for (HistoryIndexEntry entry : additions) {
            if (!knownIds.add(entry.eventId())) continue;
            stored.entries.add(entry);
            increment(stored.categoryCounts, entry.category());
            increment(stored.typeCounts, entry.category() + ":" + entry.type());
            increment(stored.realmCounts, entry.realmId());
            playerIds(entry).forEach(playerId -> increment(stored.playerCounts, playerId));
            if (stored.firstTimestamp == 0 || entry.timestamp() < stored.firstTimestamp) {
                stored.firstTimestamp = entry.timestamp();
            }
            if (entry.timestamp() > stored.lastTimestamp) {
                stored.lastTimestamp = entry.timestamp();
            }
        }
        stored.entries.sort(Comparator.comparingLong(HistoryIndexEntry::timestamp).reversed());
        stored.totalEvents = stored.entries.size();
    }

    private static Set<String> playerIds(HistoryIndexEntry entry) {
        Set<String> ids = new LinkedHashSet<>();
        if (entry.actorId() != null) ids.add(entry.actorId().toString());
        if (entry.subjectType().equals("player") && !entry.subjectId().isBlank()) {
            ids.add(entry.subjectId());
        }
        return ids;
    }

    private static void increment(Map<String, Integer> counts, String key) {
        if (key == null || key.isBlank()) return;
        counts.merge(key, 1, Integer::sum);
    }

    private static Path indexDir(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("elarion/history-index");
    }

    private static String monthName(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
    }

    private int pendingSizeLocked() {
        int total = 0;
        for (List<HistoryIndexEntry> entries : pendingEntries.values()) {
            total += entries.size();
        }
        return total;
    }

    private static final class StoredMonthIndex {
        String month;
        long firstTimestamp;
        long lastTimestamp;
        int totalEvents;
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        Map<String, Integer> typeCounts = new LinkedHashMap<>();
        Map<String, Integer> realmCounts = new LinkedHashMap<>();
        Map<String, Integer> playerCounts = new LinkedHashMap<>();
        List<HistoryIndexEntry> entries = new ArrayList<>();

        StoredMonthIndex(String month) {
            this.month = month;
        }

        StoredMonthIndex normalized(String fallbackMonth) {
            if (month == null || month.isBlank()) month = fallbackMonth;
            if (categoryCounts == null) categoryCounts = new LinkedHashMap<>();
            if (typeCounts == null) typeCounts = new LinkedHashMap<>();
            if (realmCounts == null) realmCounts = new LinkedHashMap<>();
            if (playerCounts == null) playerCounts = new LinkedHashMap<>();
            if (entries == null) entries = new ArrayList<>();
            return this;
        }
    }
}
