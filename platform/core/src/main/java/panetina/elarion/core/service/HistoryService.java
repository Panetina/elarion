package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ChronicleArchive;
import panetina.elarion.core.model.ChronicleEntry;
import panetina.elarion.core.model.HistoryEvent;
import panetina.elarion.core.model.HistoryIndexEntry;
import panetina.elarion.core.model.HistoryMonthIndex;
import panetina.elarion.core.model.PublicHistoryConsumer;
import panetina.elarion.core.model.PublicHistoryEntry;
import panetina.elarion.core.model.PublicHistoryQuery;
import panetina.elarion.core.model.PublicHistoryResult;
import panetina.elarion.core.storage.ChronicleArchiveStorage;
import panetina.elarion.core.storage.HistoryIndexStorage;
import panetina.elarion.core.storage.HistoryStorage;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public final class HistoryService {
    private final CoreConfigManager config;
    private final HistoryStorage storage;
    private final HistoryIndexStorage indexes;
    private final ChronicleArchiveStorage archives;
    private final AtomicBoolean archiveGenerationQueued = new AtomicBoolean();
    private ElarionTaskService tasks;
    private MinecraftServer server;
    private long nextArchiveCheckAt;

    public HistoryService(CoreConfigManager config, HistoryStorage storage, ElarionEventBus events) {
        this(config, storage, new HistoryIndexStorage(org.slf4j.LoggerFactory.getLogger("elarion_history_index")),
                new ChronicleArchiveStorage(org.slf4j.LoggerFactory.getLogger("elarion_chronicles")), events);
    }

    public HistoryService(
            CoreConfigManager config,
            HistoryStorage storage,
            HistoryIndexStorage indexes,
            ChronicleArchiveStorage archives,
            ElarionEventBus events
    ) {
        this.config = config;
        this.storage = storage;
        this.indexes = indexes;
        this.archives = archives;
        events.onCitizenChanged(this::recordCitizenChange);
        events.onProgression(this::recordProgression);
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        scheduleWeeklyChronicleArchives();
    }

    public void setTaskService(ElarionTaskService tasks) {
        this.tasks = tasks;
        storage.setTaskService(tasks);
        indexes.setTaskService(tasks);
    }

    public void tick() {
        storage.tick();
        indexes.tick();
        if (server != null && System.currentTimeMillis() >= nextArchiveCheckAt) {
            scheduleWeeklyChronicleArchives();
        }
    }

    public void flush() {
        storage.flushBlocking();
        indexes.flushBlocking();
    }

    public HistoryEvent record(HistoryEvent event) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        if (config.historyRecordingPolicy().allows(event.category(), event.type())) {
            storage.append(server, event);
            indexes.append(server, event);
        }
        return event;
    }

    public HistoryEvent record(
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            Map<String, String> metadata
    ) {
        return record(HistoryEvent.create(
                category, type, actorId, subjectType, subjectId, realmId, metadata));
    }

    public HistoryEvent recordChronicle(
            String category,
            String type,
            UUID actorId,
            String subjectType,
            String subjectId,
            String realmId,
            Map<String, String> metadata,
            String chronicleText
    ) {
        return record(HistoryEvent.create(
                category, type, actorId, subjectType, subjectId, realmId, metadata, chronicleText));
    }

    public List<HistoryEvent> recent(int limit) {
        return query(event -> true, limit);
    }

    public List<HistoryEvent> forPlayer(UUID playerId, int limit) {
        String id = playerId.toString();
        return query(event -> playerId.equals(event.actorId())
                || event.subjectType().equals("player") && event.subjectId().equals(id), limit);
    }

    public List<HistoryEvent> forRealm(String realmId, int limit) {
        return query(event -> event.realmId().equalsIgnoreCase(realmId), limit);
    }

    public List<HistoryEvent> forCategory(String category, int limit) {
        return query(event -> event.category().equalsIgnoreCase(category), limit);
    }

    public List<HistoryEvent> query(Predicate<HistoryEvent> filter, int limit) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        int safeLimit = Math.max(1, Math.min(limit, config.historyCommandLimitMax()));
        return storage.queryRecent(server, filter, safeLimit, config.historyQueryMaxMonths());
    }

    public int commandLimitMax() {
        return config.historyCommandLimitMax();
    }

    public int queryMaxMonths() {
        return config.historyQueryMaxMonths();
    }

    public List<HistoryIndexEntry> indexRecent(Predicate<HistoryIndexEntry> filter, int limit) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        int safeLimit = Math.max(1, Math.min(limit, config.historyCommandLimitMax()));
        return indexes.queryEntries(server, filter, safeLimit, config.historyQueryMaxMonths());
    }

    public List<HistoryMonthIndex> recentIndexes(int maxMonths) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        return indexes.loadRecentMonths(server, Math.max(1, maxMonths));
    }

    public List<ChronicleArchive> recentChronicles(int maxWeeks) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        return archives.loadRecent(server, Math.max(1, maxWeeks));
    }

    public List<ChronicleArchive> generateWeeklyChronicleArchives() {
        nextArchiveCheckAt = System.currentTimeMillis() + 600_000L;
        if (server == null || !config.historyArchiveEnabled()) return List.of();
        int weeks = config.historyArchiveMaxCompletedWeeks();
        int indexMonths = Math.max(config.historyQueryMaxMonths(), weeks / 4 + 2);
        List<HistoryIndexEntry> entries = indexes.loadRecentMonths(server, indexMonths).stream()
                .flatMap(month -> month.entries().stream())
                .toList();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate currentWeekStart = LocalDate.now(zone)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Set<String> categories = normalizeSet(config.historyChronicleCategories());
        List<ChronicleArchive> generated = new ArrayList<>();
        for (int offset = 1; offset <= weeks; offset++) {
            LocalDate weekStart = currentWeekStart.minusWeeks(offset);
            LocalDate weekEnd = weekStart.plusWeeks(1);
            ChronicleArchive archive = buildArchive(entries, weekStart, weekEnd, zone, categories);
            if (archive.entries().isEmpty()) continue;
            archives.saveIfAbsent(server, archive).ifPresent(generated::add);
        }
        return List.copyOf(generated);
    }

    private void scheduleWeeklyChronicleArchives() {
        nextArchiveCheckAt = System.currentTimeMillis() + 600_000L;
        if (server == null || tasks == null || !config.historyArchiveEnabled()) return;
        if (!archiveGenerationQueued.compareAndSet(false, true)) return;
        tasks.submitIo("chronicle-archive-generate", () -> generateWeeklyChronicleArchives())
                .whenComplete((ignored, throwable) -> archiveGenerationQueued.set(false));
    }

    public PublicHistoryResult publicHistory(PublicHistoryQuery query) {
        if (server == null) throw new IllegalStateException("HistoryService is not bound to a server");
        PublicHistoryQuery safeQuery = query == null
                ? PublicHistoryQuery.forConsumer(PublicHistoryConsumer.GUI_SEARCH)
                : query;
        int limit = safeQuery.limit() <= 0
                ? config.publicHistoryDefaultLimit()
                : Math.min(safeQuery.limit(), config.publicHistoryMaxLimit());
        int weeks = safeQuery.weeks() <= 0 ? config.publicHistoryDefaultWeeks() : safeQuery.weeks();
        Set<String> categories = safeQuery.categories().isEmpty()
                ? defaultPublicCategories(safeQuery.consumer())
                : normalizeSet(safeQuery.categories());

        List<PublicHistoryEntry> results = new ArrayList<>(limit);
        Set<UUID> seen = new LinkedHashSet<>();
        int archivesScanned = 0;
        int liveIndexesScanned = 0;

        if (safeQuery.includeArchives()) {
            List<ChronicleArchive> recentArchives = archives.loadRecent(server, weeks);
            archivesScanned = recentArchives.size();
            for (ChronicleArchive archive : recentArchives) {
                for (ChronicleEntry entry : archive.entries()) {
                    addIfMatches(results, seen, PublicHistoryEntry.fromArchive(entry), safeQuery, categories, limit);
                }
                if (results.size() >= limit) break;
            }
        }

        if (results.size() < limit && safeQuery.includeLiveIndex()) {
            int indexMonths = Math.max(config.historyQueryMaxMonths(), weeks / 4 + 2);
            List<HistoryMonthIndex> recentIndexes = indexes.loadRecentMonths(server, indexMonths);
            liveIndexesScanned = recentIndexes.size();
            for (HistoryMonthIndex month : recentIndexes) {
                for (HistoryIndexEntry entry : month.entries()) {
                    addIfMatches(results, seen, PublicHistoryEntry.fromIndex(entry), safeQuery, categories, limit);
                }
                if (results.size() >= limit) break;
            }
        }

        results.sort(Comparator.comparingLong(PublicHistoryEntry::timestamp).reversed());
        return new PublicHistoryResult(safeQuery.consumer(), archivesScanned, liveIndexesScanned, results);
    }

    private void recordCitizenChange(ElarionEventBus.CitizenChanged event) {
        if (server == null) return;
        CitizenRecord citizen = event.citizen();
        Map<String, String> metadata = new LinkedHashMap<>();
        put(metadata, "username", citizen.lastKnownUsername());
        put(metadata, "nickname", citizen.nickname());
        put(metadata, "title", citizen.titleId());
        put(metadata, "status", citizen.status().name());
        record("citizen", normalize(event.reason()), null, "player",
                event.citizenId().toString(), citizen.realmId(), metadata);
    }

    private void recordProgression(ElarionEventBus.ProgressionEvent event) {
        if (server == null) return;
        record("progression", normalize(event.eventId()), event.actorId(), "progression",
                event.subjectId(), "", Map.of("event", event.eventId()));
    }

    private static void put(Map<String, String> values, String key, String value) {
        if (value != null && !value.isBlank()) values.put(key, value);
    }

    private static String normalize(String value) {
        return value == null ? "event" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static ChronicleArchive buildArchive(
            List<HistoryIndexEntry> source,
            LocalDate weekStart,
            LocalDate weekEnd,
            ZoneId zone,
            Set<String> categories
    ) {
        long start = weekStart.atStartOfDay(zone).toInstant().toEpochMilli();
        long end = weekEnd.atStartOfDay(zone).toInstant().toEpochMilli();
        List<ChronicleEntry> entries = source.stream()
                .filter(entry -> entry.timestamp() >= start && entry.timestamp() < end)
                .filter(entry -> categories.isEmpty() || categories.contains(normalize(entry.category())))
                .sorted(Comparator.comparingLong(HistoryIndexEntry::timestamp).reversed())
                .map(ChronicleEntry::from)
                .toList();
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        Map<String, Integer> typeCounts = new LinkedHashMap<>();
        Map<String, Integer> realmCounts = new LinkedHashMap<>();
        Map<String, Integer> playerCounts = new LinkedHashMap<>();
        for (ChronicleEntry entry : entries) {
            increment(categoryCounts, entry.category());
            increment(typeCounts, entry.category() + ":" + entry.type());
            increment(realmCounts, entry.realmId());
            if (entry.actorId() != null) increment(playerCounts, entry.actorId().toString());
            if (entry.subjectType().equals("player") && !entry.subjectId().isBlank()) {
                increment(playerCounts, entry.subjectId());
            }
        }
        return new ChronicleArchive(UUID.randomUUID(), weekStart.toString(), weekEnd.toString(),
                Instant.now().toEpochMilli(), entries.size(), categoryCounts, typeCounts,
                realmCounts, playerCounts, entries);
    }

    private static void addIfMatches(
            List<PublicHistoryEntry> results,
            Set<UUID> seen,
            PublicHistoryEntry entry,
            PublicHistoryQuery query,
            Set<String> categories,
            int limit
    ) {
        if (results.size() >= limit || seen.contains(entry.eventId())) return;
        if (!matches(entry, query, categories)) return;
        seen.add(entry.eventId());
        results.add(entry);
    }

    private static boolean matches(PublicHistoryEntry entry, PublicHistoryQuery query, Set<String> categories) {
        if (!categories.isEmpty() && !categories.contains(normalize(entry.category()))) return false;
        if (!query.realmId().isBlank() && !entry.realmId().equalsIgnoreCase(query.realmId())) return false;
        if (query.playerId() != null && !entry.involvesPlayer(query.playerId())) return false;
        if (!query.text().isBlank()) {
            String needle = query.text().toLowerCase(Locale.ROOT);
            String haystack = (entry.text() + " " + entry.category() + " " + entry.type()
                    + " " + entry.realmId() + " " + entry.subjectId()).toLowerCase(Locale.ROOT);
            return haystack.contains(needle);
        }
        return true;
    }

    private static Set<String> defaultPublicCategories(PublicHistoryConsumer consumer) {
        return switch (consumer) {
            case CHRONICLE -> Set.of("realm", "realm-decision", "diplomacy", "leadership",
                    "title", "reward", "world", "administration", "security");
            case NEWSPAPER -> Set.of("realm", "realm-decision", "diplomacy", "leadership",
                    "title", "reward", "world", "administration");
            case LEDGER -> Set.of("citizen", "title", "progression", "realm", "reward");
            case NPC_RUMOR -> Set.of("realm", "realm-decision", "title", "progression", "world", "reward");
            case GUI_SEARCH -> Set.of();
        };
    }

    private static Set<String> normalizeSet(Set<String> values) {
        return values.stream()
                .map(HistoryService::normalize)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static void increment(Map<String, Integer> counts, String key) {
        if (key == null || key.isBlank()) return;
        counts.merge(key, 1, Integer::sum);
    }
}
