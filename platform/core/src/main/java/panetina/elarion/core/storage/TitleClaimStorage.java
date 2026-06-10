package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class TitleClaimStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public TitleClaimStorage(Logger logger) {
        this.logger = logger;
    }

    public TitleClaimState load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return new TitleClaimState();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredState stored = GSON.fromJson(reader, StoredState.class);
            return stored == null ? new TitleClaimState() : stored.toState();
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load title claim state", exception);
            return new TitleClaimState();
        }
    }

    public void save(MinecraftServer server, TitleClaimState state) {
        JsonStateStorage.writeAtomic(file(server), GSON, StoredState.from(state), logger, "title claim state");
    }

    private static Path file(MinecraftServer server) {
        return JsonStateStorage.elarionRoot(server).resolve("title-claims.json");
    }

    public static final class TitleClaimState {
        private final Map<String, TitleClaim> claims = new LinkedHashMap<>();

        public Map<String, TitleClaim> claims() { return claims; }
    }

    public record TitleClaim(UUID owner, long claimedAt, String reason) {}

    private static final class StoredState {
        Map<String, StoredClaim> claims = new LinkedHashMap<>();

        static StoredState from(TitleClaimState state) {
            StoredState stored = new StoredState();
            state.claims().forEach((title, claim) -> {
                StoredClaim storedClaim = new StoredClaim();
                storedClaim.owner = claim.owner().toString();
                storedClaim.claimedAt = claim.claimedAt();
                storedClaim.reason = claim.reason();
                stored.claims.put(title, storedClaim);
            });
            return stored;
        }

        TitleClaimState toState() {
            TitleClaimState state = new TitleClaimState();
            if (claims == null) return state;
            claims.forEach((title, claim) -> {
                try {
                    state.claims().put(title, new TitleClaim(
                            UUID.fromString(claim.owner), claim.claimedAt, claim.reason));
                } catch (IllegalArgumentException | NullPointerException ignored) {
                }
            });
            return state;
        }
    }

    private static final class StoredClaim {
        String owner;
        long claimedAt;
        String reason;
    }
}
