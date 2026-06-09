package panetina.elarion.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CitizenStatus;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
            logger.error("Failed to load citizen {}", uuid, exception);
            return null;
        }
    }

    public void save(MinecraftServer server, CitizenRecord citizen) {
        Path directory = citizenDir(server);
        try {
            Files.createDirectories(directory);
            try (Writer writer = Files.newBufferedWriter(directory.resolve(citizen.uuid() + ".json"), StandardCharsets.UTF_8)) {
                GSON.toJson(StoredCitizen.from(citizen), writer);
            }
        } catch (IOException exception) {
            logger.error("Failed to save citizen {}", citizen.uuid(), exception);
        }
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
                            logger.warn("Ignoring citizen file with invalid UUID name: {}", path);
                        }
                    });
        } catch (IOException exception) {
            logger.error("Failed to list citizens in {}", directory, exception);
        }
        return List.copyOf(citizens);
    }

    private static Path citizenDir(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve("elarion/citizens");
    }

    private static final class StoredCitizen {
        String lastKnownUsername;
        String realmId;
        String titleId;
        String nickname;
        String status;
        long joinedAt;
        List<String> flags = new ArrayList<>();
        List<String> grantedAbilities = new ArrayList<>();

        static StoredCitizen from(CitizenRecord citizen) {
            StoredCitizen stored = new StoredCitizen();
            stored.lastKnownUsername = citizen.lastKnownUsername();
            stored.realmId = citizen.realmId();
            stored.titleId = citizen.titleId();
            stored.nickname = citizen.nickname();
            stored.status = citizen.status().name();
            stored.joinedAt = citizen.joinedAt();
            stored.flags = new ArrayList<>(citizen.flags());
            stored.grantedAbilities = new ArrayList<>(citizen.grantedAbilities());
            return stored;
        }

        CitizenRecord toRecord(UUID uuid) {
            CitizenRecord record = new CitizenRecord(uuid, lastKnownUsername == null ? uuid.toString() : lastKnownUsername);
            record.setRealmId(realmId);
            record.setTitleId(titleId);
            record.setNickname(nickname);
            record.setJoinedAt(joinedAt);
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
