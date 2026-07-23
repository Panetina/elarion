package panetina.elarion.core.integration.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import panetina.elarion.core.storage.JsonStateStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    /**
     * The bridge must not make a website-side historical removal override a
     * deliberate console whitelist addition.  This bounded record tracks only
     * entries the bridge itself has added, so a REMOVE command can safely act
     * on its own authority without claiming ownership of vanilla-managed
     * entries.
     */
    public record State(long cursor, List<Long> pendingAcknowledgements, List<String> bridgeManagedUuids) {
        public State {
            pendingAcknowledgements = pendingAcknowledgements == null
                    ? List.of() : List.copyOf(pendingAcknowledgements);
            bridgeManagedUuids = bridgeManagedUuids == null
                    ? List.of() : List.copyOf(bridgeManagedUuids);
        }

        public static State empty() {
            return new State(0, List.of(), List.of());
        }

        public State applied(long sequence) {
            if (sequence <= cursor) return this;
            List<Long> pending = new ArrayList<>(pendingAcknowledgements);
            pending.add(sequence);
            return new State(sequence, pending, bridgeManagedUuids);
        }

        public State acknowledged() {
            return new State(cursor, List.of(), bridgeManagedUuids);
        }

        public boolean isBridgeManaged(UUID minecraftUuid) {
            return bridgeManagedUuids.contains(minecraftUuid.toString());
        }

        public State bridgeAdded(UUID minecraftUuid) {
            String value = minecraftUuid.toString();
            if (bridgeManagedUuids.contains(value)) return this;
            List<String> managed = new ArrayList<>(bridgeManagedUuids);
            managed.add(value);
            return new State(cursor, pendingAcknowledgements, managed);
        }

        public State bridgeRemoved(UUID minecraftUuid) {
            String value = minecraftUuid.toString();
            if (!bridgeManagedUuids.contains(value)) return this;
            return new State(cursor, pendingAcknowledgements,
                    bridgeManagedUuids.stream().filter(uuid -> !uuid.equals(value)).toList());
        }

        State normalized() {
            if (cursor < 0) return empty();
            List<Long> pending = pendingAcknowledgements.stream()
                    .filter(sequence -> sequence != null && sequence > 0 && sequence <= cursor)
                    .distinct()
                    .sorted()
                    .toList();
            List<String> managed = bridgeManagedUuids.stream()
                    .filter(uuid -> {
                        try {
                            UUID.fromString(uuid);
                            return true;
                        } catch (IllegalArgumentException exception) {
                            return false;
                        }
                    })
                    .map(String::toLowerCase)
                    .distinct()
                    .sorted()
                    .toList();
            return new State(cursor, pending, managed);
        }
    }
}
