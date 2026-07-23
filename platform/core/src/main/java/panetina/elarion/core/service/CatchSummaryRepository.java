package panetina.elarion.core.service;

import panetina.elarion.core.model.CatchJournalReplay;
import panetina.elarion.core.model.CatchSummary;
import panetina.elarion.core.storage.CatchSummaryStorage;
import panetina.elarion.core.storage.DirtyTracker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CatchSummaryRepository {
    private final Path elarionRoot;
    private final CatchSummaryStorage storage;
    private final Map<UUID, CatchSummary> summaries = new HashMap<>();
    private final DirtyTracker dirty = new DirtyTracker();

    public CatchSummaryRepository(Path elarionRoot, CatchSummaryStorage storage) {
        this.elarionRoot = elarionRoot;
        this.storage = storage;
    }

    public synchronized CatchSummary get(UUID actorId) throws IOException {
        CatchSummary current = summaries.get(actorId);
        if (current != null) return current;
        CatchSummary loaded = storage.load(elarionRoot, actorId);
        summaries.put(actorId, loaded);
        return loaded;
    }

    /** Memory-only normal gameplay view. Activation/replay owns all storage reads. */
    public synchronized CatchSummary peek(UUID actorId) {
        return summaries.getOrDefault(actorId, CatchSummary.empty(actorId));
    }

    public synchronized CatchSummary apply(UUID actorId, CatchJournalReplay replay) throws IOException {
        CatchSummary current = get(actorId);
        CatchSummary updated = CatchSummaryProjection.apply(current, replay);
        if (!updated.equals(current)) {
            summaries.put(actorId, updated);
            dirty.mark(actorId);
        }
        return updated;
    }

    public synchronized void save(UUID actorId) throws IOException {
        if (!dirty.contains(actorId)) return;
        CatchSummary summary = summaries.get(actorId);
        if (summary == null) return;
        storage.save(elarionRoot, summary);
        dirty.remove(actorId);
    }

    public void saveDirty() throws IOException {
        for (UUID actorId : dirty.snapshot()) save(actorId);
    }

    public Set<UUID> dirtyActors() {
        return dirty.snapshot();
    }

    Path elarionRoot() {
        return elarionRoot;
    }
}
