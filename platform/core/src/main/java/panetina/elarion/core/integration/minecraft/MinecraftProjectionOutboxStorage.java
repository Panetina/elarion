package panetina.elarion.core.integration.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Mode;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Projection;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MinecraftProjectionOutboxStorage {
    public static final int MAX_PENDING = 10_000;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public MinecraftProjectionOutboxStorage(Logger logger) {
        this.logger = logger;
    }

    public State load(Path elarionRoot) {
        return JsonStateStorage.read(file(elarionRoot), gson, State.class,
                State::empty, State::normalized, logger, "Minecraft projection outbox");
    }

    public void save(Path elarionRoot, State state) {
        JsonStateStorage.writeAtomic(file(elarionRoot), gson, state.normalized(), logger, "Minecraft projection outbox");
    }

    private Path file(Path elarionRoot) {
        return elarionRoot.resolve("core").resolve("minecraft-bridge").resolve("projection-outbox.json");
    }

    public record State(long nextSequence, List<Projection> pending) {
        public State {
            pending = pending == null ? List.of() : List.copyOf(pending);
        }

        public static State empty() {
            return new State(1, List.of());
        }

        public State enqueue(Projection value) {
            List<Projection> next = new ArrayList<>(pending);
            if (value.mode() == Mode.STATE) {
                for (int index = 0; index < next.size(); index++) {
                    Projection existing = next.get(index);
                    if (existing.mode() == Mode.STATE && existing.kind().equals(value.kind())
                            && existing.entityId().equals(value.entityId())) {
                        next.set(index, withSequence(value, existing.sequence()));
                        return new State(nextSequence, next);
                    }
                }
            }
            if (next.size() >= MAX_PENDING) throw new IllegalStateException("projection_outbox_full");
            next.add(withSequence(value, nextSequence));
            return new State(nextSequence + 1, next);
        }

        public State acknowledged(long acceptedThrough) {
            if (acceptedThrough <= 0) return this;
            return new State(nextSequence, pending.stream()
                    .filter(value -> value.sequence() > acceptedThrough)
                    .toList());
        }

        public List<Projection> batch(int limit) {
            return pending.stream().sorted(Comparator.comparingLong(Projection::sequence))
                    .limit(Math.max(1, Math.min(limit, 100))).toList();
        }

        State normalized() {
            Map<Long, Projection> unique = new LinkedHashMap<>();
            pending.stream().filter(value -> value != null && value.sequence() > 0)
                    .sorted(Comparator.comparingLong(Projection::sequence))
                    .limit(MAX_PENDING)
                    .forEach(value -> unique.putIfAbsent(value.sequence(), value));
            long highest = unique.keySet().stream().mapToLong(Long::longValue).max().orElse(0);
            return new State(Math.max(Math.max(1, nextSequence), highest + 1), List.copyOf(unique.values()));
        }

        private static Projection withSequence(Projection value, long sequence) {
            return new Projection(sequence, value.mode(), value.kind(), value.entityId(), value.realmId(),
                    value.visibility(), value.version(), value.occurredAt(), value.payload());
        }
    }
}
