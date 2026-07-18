package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CitizenStatus;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class CitizenStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Logger logger;

    public CitizenStorage(Logger logger) {
        this.logger = logger;
    }

    public CitizenRecord load(MinecraftServer server, UUID uuid) {
        Path file = citizenDir(server).resolve(uuid + ".json");
        if (Files.notExists(file)) return null;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredCitizen stored = GSON.fromJson(reader, StoredCitizen.class);
            return stored == null ? null : stored.toRecord(uuid);
        } catch (IOException | RuntimeException exception) {
            logger.error("Failed to load Ember {}", uuid, exception);
            return null;
        }
    }

    public void save(MinecraftServer server, CitizenRecord citizen) {
        JsonStateStorage.writeAtomic(
                citizenDir(server).resolve(citizen.uuid() + ".json"),
                GSON,
                StoredCitizen.from(citizen),
                logger,
                "Ember " + citizen.uuid());
    }

    public List<CitizenRecord> loadAll(MinecraftServer server) {
        Path directory = citizenDir(server);
        if (Files.notExists(directory)) return List.of();

        List<CitizenRecord> citizens = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        try {
                            UUID uuid = UUID.fromString(fileName.substring(0, fileName.length() - 5));
                            CitizenRecord citizen = load(server, uuid);
                            if (citizen != null) citizens.add(citizen);
                        } catch (IllegalArgumentException exception) {
                            logger.warn("Ignoring Ember file with invalid UUID name: {}", path);
                        }
                    });
        } catch (IOException exception) {
            logger.error("Failed to list Embers in {}", directory, exception);
        }
        return List.copyOf(citizens);
    }

    private static Path citizenDir(MinecraftServer server) {
        return JsonStateStorage.elarionRoot(server).resolve("citizens");
    }

    private static final class StoredCitizen {
        String lastKnownUsername;
        String realmId;
        String leaderRealmId;
        String titleId;
        String activeTitleId;
        String nickname;
        String status;
        long joinedAt;
        long lastSeenAt;
        List<String> flags = new ArrayList<>();
        List<String> grantedAbilities = new ArrayList<>();
        List<String> unlockedTitleIds = new ArrayList<>();
        Map<String, Long> titleUnlockTimes = new LinkedHashMap<>();

        static StoredCitizen from(CitizenRecord citizen) {
            StoredCitizen stored = new StoredCitizen();
            stored.lastKnownUsername = citizen.lastKnownUsername();
            stored.realmId = citizen.realmId();
            stored.leaderRealmId = citizen.leaderRealmId();
            stored.activeTitleId = citizen.activeTitleId();
            stored.nickname = citizen.nickname();
            stored.status = citizen.status().name();
            stored.joinedAt = citizen.joinedAt();
            stored.lastSeenAt = citizen.lastSeenAt();
            stored.flags = new ArrayList<>(citizen.flags());
            stored.grantedAbilities = new ArrayList<>(citizen.grantedAbilities());
            stored.unlockedTitleIds = new ArrayList<>(citizen.unlockedTitleIds());
            stored.titleUnlockTimes = new LinkedHashMap<>(citizen.titleUnlockTimes());
            return stored;
        }

        CitizenRecord toRecord(UUID uuid) {
            CitizenRecord record = new CitizenRecord(uuid, lastKnownUsername == null ? uuid.toString() : lastKnownUsername);
            record.setRealmId(realmId);
            record.setLeaderRealmId(leaderRealmId);
            if (unlockedTitleIds != null) {
                for (String title : unlockedTitleIds) {
                    long unlockedAt = titleUnlockTimes == null
                            ? joinedAt
                            : titleUnlockTimes.getOrDefault(title, joinedAt);
                    record.unlockTitle(title, unlockedAt);
                }
            }
            String loadedActiveTitle = activeTitleId == null || activeTitleId.isBlank()
                    ? titleId
                    : activeTitleId;
            record.setActiveTitleId(loadedActiveTitle);
            record.setNickname(nickname);
            record.setJoinedAt(joinedAt);
            record.setLastSeenAt(lastSeenAt > 0 ? lastSeenAt : System.currentTimeMillis());
            try {
                record.setStatus(CitizenStatus.valueOf(status));
            } catch (IllegalArgumentException | NullPointerException ignored) {
                record.setStatus(CitizenStatus.ACTIVE);
            }
            record.flags().addAll(flags == null ? new LinkedHashSet<>() : flags);
            record.grantedAbilities().addAll(grantedAbilities == null ? new LinkedHashSet<>() : grantedAbilities);
            return record;
        }
    }
}
