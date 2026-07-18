package panetina.elarion.core.integration.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftBridgeStateStorage {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public MinecraftBridgeStateStorage(Logger logger) {
        this.logger = logger;
    }

    public State load(Path elarionRoot) {
        return JsonStateStorage.read(file(elarionRoot), gson, State.class,
                State::empty, State::normalized, logger, "Minecraft bridge state");
    }

    public void save(Path elarionRoot, State state) {
        JsonStateStorage.writeAtomic(file(elarionRoot), gson, state.normalized(), logger, "Minecraft bridge state");
    }

    private Path file(Path elarionRoot) {
        return elarionRoot.resolve("core").resolve("minecraft-bridge").resolve("state.json");
    }

    public record State(long cursor, List<Long> pendingAcknowledgements) {
        public State {
            pendingAcknowledgements = pendingAcknowledgements == null
                    ? List.of() : List.copyOf(pendingAcknowledgements);
        }

        public static State empty() {
            return new State(0, List.of());
        }

        public State applied(long sequence) {
            if (sequence <= cursor) return this;
            List<Long> pending = new ArrayList<>(pendingAcknowledgements);
            pending.add(sequence);
            return new State(sequence, pending);
        }

        public State acknowledged() {
            return new State(cursor, List.of());
        }

        State normalized() {
            if (cursor < 0) return empty();
            List<Long> pending = pendingAcknowledgements.stream()
                    .filter(sequence -> sequence != null && sequence > 0 && sequence <= cursor)
                    .distinct()
                    .sorted()
                    .toList();
            return new State(cursor, pending);
        }
    }
}
