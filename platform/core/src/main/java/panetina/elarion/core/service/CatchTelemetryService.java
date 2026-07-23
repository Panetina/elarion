package panetina.elarion.core.service;

import org.slf4j.Logger;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.AcceptedCatchRecord;
import panetina.elarion.core.model.CatchJournalReplay;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.model.CatchTelemetryEvent;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.CatchTelemetryFormatException;
import panetina.elarion.core.storage.CatchTelemetryJournalStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CatchTelemetryService {
    static final int DEFAULT_REPLAY_LINES_PER_PAGE = 256;
    static final int DEFAULT_ACTORS_PER_TICK = 4;
    static final long DEFAULT_SAVE_INTERVAL_MILLIS = 300_000L;
    private static final long REPLAY_INTERVAL_MILLIS = 1_000L;

    private final CatchTelemetryJournalStorage journal;
    private final CatchSummaryStorage summaryStorage;
    private final Logger logger;
    private final int replayLinesPerPage;
    private final int actorsPerTick;
    private final long saveIntervalMillis;
    private final Set<UUID> replayQueue = new LinkedHashSet<>();
    private final Map<UUID, Map<UUID, AcceptedCatchRecord>> replaySeen = new HashMap<>();
    private final Map<UUID, RuntimeException> blockedActors = new HashMap<>();
    private CatchSummaryRepository summaries;
    private AutoCloseable eventSubscription;
    private long lastSaveAt;
    private long nextReplayAt;

    public CatchTelemetryService(
            CatchTelemetryJournalStorage journal,
            CatchSummaryStorage summaryStorage,
            Logger logger
    ) {
        this(
                journal,
                summaryStorage,
                logger,
                DEFAULT_REPLAY_LINES_PER_PAGE,
                DEFAULT_ACTORS_PER_TICK,
                DEFAULT_SAVE_INTERVAL_MILLIS);
    }

    CatchTelemetryService(
            CatchTelemetryJournalStorage journal,
            CatchSummaryStorage summaryStorage,
            Logger logger,
            int replayLinesPerPage,
            int actorsPerTick,
            long saveIntervalMillis
    ) {
        this.journal = Objects.requireNonNull(journal, "journal");
        this.summaryStorage = Objects.requireNonNull(summaryStorage, "summaryStorage");
        this.logger = Objects.requireNonNull(logger, "logger");
        if (replayLinesPerPage <= 0) throw new IllegalArgumentException("replayLinesPerPage must be positive");
        if (actorsPerTick <= 0) throw new IllegalArgumentException("actorsPerTick must be positive");
        if (saveIntervalMillis <= 0) throw new IllegalArgumentException("saveIntervalMillis must be positive");
        this.replayLinesPerPage = replayLinesPerPage;
        this.actorsPerTick = actorsPerTick;
        this.saveIntervalMillis = saveIntervalMillis;
    }

    public synchronized void registerEvents(ElarionEventBus events) {
        Objects.requireNonNull(events, "events");
        if (eventSubscription != null) {
            throw new IllegalStateException("CatchTelemetryService events are already registered");
        }
        eventSubscription = events.onCatchTelemetry(this::accept);
    }

    public synchronized void bind(Path elarionRoot) {
        summaries = new CatchSummaryRepository(
                Objects.requireNonNull(elarionRoot, "elarionRoot"),
                summaryStorage);
        replayQueue.clear();
        replaySeen.clear();
        blockedActors.clear();
        long now = System.currentTimeMillis();
        lastSaveAt = now;
        nextReplayAt = now;
    }

    public synchronized AcceptedCatchRecord accept(CatchTelemetryEvent event) {
        Objects.requireNonNull(event, "event");
        CatchSummaryRepository repository = requireBound();
        AcceptedCatchRecord record = AcceptedCatchRecord.from(event);
        try {
            journal.append(repositoryRoot(repository), record);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to append catch telemetry for " + event.actorId(), exception);
        }

        replayQueue.add(event.actorId());
        throwIfBlocked(event.actorId());
        processActor(event.actorId());
        return record;
    }

    public synchronized void activate(UUID actorId) {
        Objects.requireNonNull(actorId, "actorId");
        requireBound();
        replayQueue.add(actorId);
        try {
            throwIfBlocked(actorId);
            processActor(actorId);
        } catch (RuntimeException exception) {
            logger.error("Catch telemetry activation failed for {}", actorId, exception);
        }
    }

    public synchronized CatchSummary summary(UUID actorId) {
        Objects.requireNonNull(actorId, "actorId");
        requireBound();
        replayQueue.add(actorId);
        throwIfBlocked(actorId);
        return processActor(actorId);
    }

    /** Memory-only projection query; never reads or replays telemetry storage. */
    public synchronized CatchSummary cachedSummary(UUID actorId) {
        Objects.requireNonNull(actorId, "actorId");
        return requireBound().peek(actorId);
    }

    public synchronized void tick() {
        requireBound();
        long now = System.currentTimeMillis();
        if (now >= nextReplayAt) {
            processQueuedActors();
            nextReplayAt = now + REPLAY_INTERVAL_MILLIS;
        }
        if (now - lastSaveAt >= saveIntervalMillis) {
            saveDirtySafely("periodic save");
            lastSaveAt = now;
        }
    }

    public synchronized void save(UUID actorId) {
        Objects.requireNonNull(actorId, "actorId");
        try {
            requireBound().save(actorId);
        } catch (IOException exception) {
            logger.error("Failed to save catch summary for {}; it remains replayable", actorId, exception);
        }
    }

    public synchronized void shutdown() {
        if (summaries == null) return;
        saveDirtySafely("server shutdown");
        replayQueue.clear();
        replaySeen.clear();
        blockedActors.clear();
        summaries = null;
    }

    synchronized Set<UUID> pendingActors() {
        return Set.copyOf(replayQueue);
    }

    private void processQueuedActors() {
        List<UUID> actors = new ArrayList<>(replayQueue);
        int limit = Math.min(actorsPerTick, actors.size());
        for (int index = 0; index < limit; index++) {
            UUID actorId = actors.get(index);
            if (blockedActors.containsKey(actorId)) {
                replayQueue.remove(actorId);
                continue;
            }
            try {
                processActor(actorId);
            } catch (RuntimeException exception) {
                if (exception instanceof CatchTelemetryFormatException
                        || exception.getCause() instanceof CatchTelemetryFormatException) {
                    blockedActors.put(actorId, exception);
                    replayQueue.remove(actorId);
                    logger.error("Catch telemetry replay blocked for {}", actorId, exception);
                } else {
                    logger.error("Catch telemetry replay failed for {}; it will be retried", actorId, exception);
                }
            }
        }
    }

    private CatchSummary processActor(UUID actorId) {
        CatchSummaryRepository repository = requireBound();
        try {
            CatchSummary current = repository.get(actorId);
            Map<UUID, AcceptedCatchRecord> seen = replaySeen.computeIfAbsent(
                    actorId,
                    ignored -> recentByEventId(current));
            CatchJournalReplay replay = journal.replay(
                    repositoryRoot(repository),
                    actorId,
                    current.checkpoint(),
                    seen,
                    replayLinesPerPage);
            CatchSummary updated = repository.apply(actorId, replay);
            if (replay.hasMore()) {
                replayQueue.add(actorId);
            } else {
                replayQueue.remove(actorId);
                replaySeen.remove(actorId);
            }
            return updated;
        } catch (IOException exception) {
            replayQueue.add(actorId);
            throw new UncheckedIOException("Failed to replay catch telemetry for " + actorId, exception);
        } catch (CatchTelemetryFormatException exception) {
            blockedActors.put(actorId, exception);
            replayQueue.remove(actorId);
            throw exception;
        }
    }

    private void saveDirty() {
        try {
            requireBound().saveDirty();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to save dirty catch summaries", exception);
        }
    }

    private void saveDirtySafely(String reason) {
        try {
            saveDirty();
        } catch (UncheckedIOException exception) {
            logger.error("Catch summary {} failed; journal replay remains authoritative", reason, exception);
        }
    }

    private void throwIfBlocked(UUID actorId) {
        RuntimeException failure = blockedActors.get(actorId);
        if (failure != null) {
            throw new IllegalStateException("Catch telemetry processing is blocked for " + actorId, failure);
        }
    }

    private CatchSummaryRepository requireBound() {
        if (summaries == null) {
            throw new IllegalStateException("CatchTelemetryService is not bound to a world");
        }
        return summaries;
    }

    private static Path repositoryRoot(CatchSummaryRepository repository) {
        return repository.elarionRoot();
    }

    private static Map<UUID, AcceptedCatchRecord> recentByEventId(CatchSummary summary) {
        Map<UUID, AcceptedCatchRecord> seen = new HashMap<>();
        for (AcceptedCatchRecord record : summary.recentCatches()) {
            seen.put(record.eventId(), record);
        }
        return seen;
    }

}
