package panetina.elarion.addons.angling.persistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.component.AnglingBaitDebitCursor;
import panetina.elarion.addons.angling.fishing.AnglingCatchCommit;
import panetina.elarion.addons.angling.fishing.AnglingCatchReward;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Lazy per-player append-first projection of bait costs accepted by the catch
 * transaction. No ordinary action scans another player or historical catch.
 */
public final class AnglingBaitDebitLedger {
    static final int CHECKPOINT_INTERVAL = 256;
    static final int MAX_REPLAY_LINES = CHECKPOINT_INTERVAL * 2;
    static final int MAX_LINE_CHARACTERS = 2_048;
    static final int MAX_LOADED_ACTORS = 4_096;

    private final LinkedHashMap<UUID, ActorState> actors = new LinkedHashMap<>(16, 0.75F, true);
    private Path root;

    public synchronized void bind(Path elarionRoot) {
        if (root != null) throw new IllegalStateException("bait debit ledger is already bound");
        root = Objects.requireNonNull(elarionRoot, "elarionRoot")
                .resolve("addon-state").resolve("elarion_angling").resolve("bait-debits");
        actors.clear();
    }

    public synchronized void shutdown() {
        if (root == null) return;
        try {
            for (Map.Entry<UUID, ActorState> entry : actors.entrySet()) {
                if (entry.getValue().journalEntries > 0) checkpoint(entry.getKey(), entry.getValue());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to checkpoint Angling bait debits", exception);
        } finally {
            actors.clear();
            root = null;
        }
    }

    /** Durable idempotent materialization performed on the catch transaction worker. */
    public synchronized long record(AnglingCatchCommit commit) throws IOException {
        AnglingCatchReward.BaitDebit debit = commit.reward().baitDebit().orElse(null);
        if (debit == null) return 0;
        UUID actorId = commit.telemetry().actorId();
        Entry entry = new Entry(actorId, commit.metrics().sequence(), commit.telemetry().eventId(),
                debit.rodItemId(), debit.baitItemId());
        ActorState state = actor(actorId);
        long existing = validateOrder(state, entry, false);
        if (existing >= 0) return existing;

        append(entry);
        long total = Math.addExact(state.totals.getOrDefault(entry.baitItemId, 0L), 1L);
        state.totals.put(entry.baitItemId, total);
        state.lastSequence = entry.sequence;
        state.lastEventId = entry.eventId;
        state.lastRodItemId = entry.rodItemId;
        state.lastBaitItemId = entry.baitItemId;
        state.journalEntries++;
        if (state.journalEntries >= CHECKPOINT_INTERVAL) checkpoint(actorId, state);
        evictIfNeeded(actorId);
        return total;
    }

    /** Bounded current totals for one player; this never reads another actor. */
    public synchronized Map<Identifier, Long> totals(UUID actorId) {
        try {
            return Map.copyOf(actor(actorId).totals);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to load Angling bait debits for " + actorId, exception);
        }
    }

    public synchronized void unload(UUID actorId) {
        ActorState state = actors.remove(actorId);
        if (state == null || state.journalEntries == 0) return;
        try {
            checkpoint(actorId, state);
        } catch (IOException exception) {
            actors.put(actorId, state);
            throw new IllegalStateException("failed to unload Angling bait debits for " + actorId, exception);
        }
    }

    private ActorState actor(UUID actorId) throws IOException {
        requireRoot();
        ActorState current = actors.get(actorId);
        if (current != null) return current;
        ActorState loaded = load(actorId);
        actors.put(actorId, loaded);
        evictIfNeeded(actorId);
        return loaded;
    }

    private ActorState load(UUID actorId) throws IOException {
        ActorState state = loadSnapshot(actorId);
        Path journal = journal(actorId);
        if (Files.notExists(journal)) return state;
        int lines = 0;
        try (BufferedReader reader = Files.newBufferedReader(journal, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                if (lines > MAX_REPLAY_LINES || line.length() > MAX_LINE_CHARACTERS) {
                    throw new IllegalArgumentException("bait debit journal exceeds its replay bound");
                }
                if (line.isBlank()) continue;
                Entry entry = decodeEntry("bait debit journal line " + lines, line);
                if (!entry.actorId.equals(actorId)) throw new IllegalArgumentException("bait debit actor mismatch");
                long existing = validateOrder(state, entry, true);
                if (existing >= 0) continue;
                state.totals.put(entry.baitItemId,
                        Math.addExact(state.totals.getOrDefault(entry.baitItemId, 0L), 1L));
                state.lastSequence = entry.sequence;
                state.lastEventId = entry.eventId;
                state.lastRodItemId = entry.rodItemId;
                state.lastBaitItemId = entry.baitItemId;
            }
        }
        state.journalEntries = lines;
        return state;
    }

    private ActorState loadSnapshot(UUID actorId) throws IOException {
        Path file = snapshot(actorId);
        if (Files.notExists(file)) return new ActorState();
        JsonObject value = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        if (value.get("schema_version").getAsInt() != 1
                || !actorId.equals(UUID.fromString(value.get("actor_id").getAsString()))) {
            throw new IllegalArgumentException("invalid bait debit snapshot identity");
        }
        ActorState state = new ActorState();
        state.lastSequence = value.get("last_sequence").getAsLong();
        if (state.lastSequence < 0) throw new IllegalArgumentException("negative bait debit sequence");
        if (state.lastSequence > 0) {
            state.lastEventId = UUID.fromString(value.get("last_event_id").getAsString());
            state.lastRodItemId = id(value, "last_rod_item_id");
            state.lastBaitItemId = id(value, "last_bait_item_id");
        }
        JsonObject totals = value.getAsJsonObject("totals");
        if (totals.size() > AnglingBaitDebitCursor.MAX_BAIT_TYPES) {
            throw new IllegalArgumentException("too many bait debit total types");
        }
        for (String key : totals.keySet()) {
            Identifier baitId = Identifier.tryParse(key);
            long total = totals.get(key).getAsLong();
            if (baitId == null || total < 0) throw new IllegalArgumentException("invalid bait debit total");
            state.totals.put(baitId, total);
        }
        return state;
    }

    private static long validateOrder(ActorState state, Entry entry, boolean replay) {
        if (entry.sequence < 1) throw new IllegalArgumentException("bait debit sequence must be positive");
        if (entry.sequence < state.lastSequence) {
            if (replay) return state.totals.getOrDefault(entry.baitItemId, 0L);
            throw new IllegalStateException("stale bait debit sequence");
        }
        if (entry.sequence == state.lastSequence) {
            if (entry.eventId.equals(state.lastEventId)
                    && entry.rodItemId.equals(state.lastRodItemId)
                    && entry.baitItemId.equals(state.lastBaitItemId)) {
                return state.totals.getOrDefault(entry.baitItemId, 0L);
            }
            throw new IllegalStateException("conflicting bait debit sequence");
        }
        if (entry.eventId.equals(state.lastEventId)) throw new IllegalStateException("bait debit event was resequenced");
        if (!state.totals.containsKey(entry.baitItemId)
                && state.totals.size() >= AnglingBaitDebitCursor.MAX_BAIT_TYPES) {
            throw new IllegalStateException("bait debit type bound exceeded");
        }
        return -1;
    }

    private void append(Entry entry) throws IOException {
        Path file = journal(entry.actorId);
        Files.createDirectories(file.getParent());
        Files.writeString(file, encodeEntry(entry) + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
            channel.force(false);
        }
    }

    private void checkpoint(UUID actorId, ActorState state) throws IOException {
        Path file = snapshot(actorId);
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.createDirectories(file.getParent());
        Files.writeString(temporary, encodeSnapshot(actorId, state), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
            channel.force(false);
        }
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveFailure) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
        Path journal = journal(actorId);
        Files.writeString(journal, "", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try (FileChannel channel = FileChannel.open(journal, StandardOpenOption.WRITE)) {
            channel.force(false);
        }
        state.journalEntries = 0;
    }

    private void evictIfNeeded(UUID protectedActor) throws IOException {
        while (actors.size() > MAX_LOADED_ACTORS) {
            UUID actorId = actors.keySet().iterator().next();
            if (actorId.equals(protectedActor) && actors.size() == 1) return;
            ActorState state = actors.remove(actorId);
            if (state != null && state.journalEntries > 0) checkpoint(actorId, state);
        }
    }

    private String encodeSnapshot(UUID actorId, ActorState state) {
        JsonObject value = new JsonObject();
        value.addProperty("schema_version", 1);
        value.addProperty("actor_id", actorId.toString());
        value.addProperty("last_sequence", state.lastSequence);
        if (state.lastSequence > 0) {
            value.addProperty("last_event_id", state.lastEventId.toString());
            value.addProperty("last_rod_item_id", state.lastRodItemId.toString());
            value.addProperty("last_bait_item_id", state.lastBaitItemId.toString());
        }
        JsonObject totals = new JsonObject();
        state.totals.forEach((id, total) -> totals.addProperty(id.toString(), total));
        value.add("totals", totals);
        return value.toString();
    }

    private static String encodeEntry(Entry entry) {
        JsonObject value = new JsonObject();
        value.addProperty("schema_version", 1);
        value.addProperty("actor_id", entry.actorId.toString());
        value.addProperty("sequence", entry.sequence);
        value.addProperty("event_id", entry.eventId.toString());
        value.addProperty("rod_item_id", entry.rodItemId.toString());
        value.addProperty("bait_item_id", entry.baitItemId.toString());
        return value.toString();
    }

    private static Entry decodeEntry(String source, String encoded) {
        try {
            JsonObject value = JsonParser.parseString(encoded).getAsJsonObject();
            if (value.get("schema_version").getAsInt() != 1) throw new IllegalArgumentException("schema");
            return new Entry(UUID.fromString(value.get("actor_id").getAsString()),
                    value.get("sequence").getAsLong(), UUID.fromString(value.get("event_id").getAsString()),
                    id(value, "rod_item_id"), id(value, "bait_item_id"));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid " + source, exception);
        }
    }

    private static Identifier id(JsonObject value, String field) {
        Identifier id = Identifier.tryParse(value.get(field).getAsString());
        if (id == null) throw new IllegalArgumentException("invalid " + field);
        return id;
    }

    private Path snapshot(UUID actorId) {
        return requireRoot().resolve(actorId + ".snapshot.json");
    }

    private Path journal(UUID actorId) {
        return requireRoot().resolve(actorId + ".journal.jsonl");
    }

    private Path requireRoot() {
        if (root == null) throw new IllegalStateException("bait debit ledger is not bound");
        return root;
    }

    private record Entry(UUID actorId, long sequence, UUID eventId,
                         Identifier rodItemId, Identifier baitItemId) {
    }

    private static final class ActorState {
        private final LinkedHashMap<Identifier, Long> totals = new LinkedHashMap<>();
        private long lastSequence;
        private UUID lastEventId;
        private Identifier lastRodItemId;
        private Identifier lastBaitItemId;
        private int journalEntries;
    }
}
