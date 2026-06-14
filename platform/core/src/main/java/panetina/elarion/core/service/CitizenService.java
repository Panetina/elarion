package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.storage.CitizenStorage;

import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class CitizenService {
    private final CitizenStorage storage;
    private final CoreConfigManager config;
    private final ElarionEventBus events;
    private final Map<UUID, CitizenRecord> cache = new ConcurrentHashMap<>();
    private MinecraftServer server;

    public CitizenService(CitizenStorage storage, CoreConfigManager config, ElarionEventBus events) {
        this.storage = storage;
        this.config = config;
        this.events = events;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        cache.clear();
    }

    public MinecraftServer server() {
        return server;
    }

    public CitizenRecord getOrCreate(ServerPlayerEntity player) {
        return getOrCreate(player.getUuid(), player.getGameProfile().getName());
    }

    public CitizenRecord getOrCreate(UUID uuid, String username) {
        requireServer();
        CitizenRecord citizen = cache.computeIfAbsent(uuid, id -> {
            CitizenRecord loaded = storage.load(server, id);
            if (loaded != null) return loaded;
            CitizenRecord created = new CitizenRecord(id, username);
            created.setTitleId(config.defaultTitleId());
            storage.save(server, created);
            return created;
        });
        if (username != null && !username.equals(citizen.lastKnownUsername())) {
            citizen.setLastKnownUsername(username);
            storage.save(server, citizen);
        }
        return citizen;
    }

    public Optional<CitizenRecord> find(UUID uuid) {
        requireServer();
        CitizenRecord cached = cache.get(uuid);
        if (cached != null) return Optional.of(cached);
        CitizenRecord loaded = storage.load(server, uuid);
        if (loaded != null) cache.put(uuid, loaded);
        return Optional.ofNullable(loaded);
    }

    public Collection<CitizenRecord> all() {
        requireServer();
        Map<UUID, CitizenRecord> citizens = new LinkedHashMap<>();
        storage.loadAll(server).forEach(citizen -> citizens.put(citizen.uuid(), citizen));
        citizens.putAll(cache);
        return List.copyOf(citizens.values());
    }

    public CitizenRecord markSeen(ServerPlayerEntity player) {
        CitizenRecord citizen = getOrCreate(player);
        citizen.setLastSeenAt(System.currentTimeMillis());
        storage.save(server, citizen);
        return citizen;
    }

    public boolean isActiveCitizen(UUID uuid) {
        return find(uuid).map(this::isActiveCitizen).orElse(false);
    }

    public boolean isActiveCitizen(CitizenRecord citizen) {
        if (citizen == null) return false;
        if (server != null && server.getPlayerManager().getPlayer(citizen.uuid()) != null) return true;
        long cutoff = System.currentTimeMillis() - config.citizenActivityWindowMillis();
        return citizen.lastSeenAt() >= cutoff;
    }

    public CitizenRecord update(ServerPlayerEntity player, String reason, Consumer<CitizenRecord> mutation) {
        CitizenRecord citizen = getOrCreate(player);
        mutation.accept(citizen);
        storage.save(server, citizen);
        events.emitCitizenChanged(new ElarionEventBus.CitizenChanged(citizen.uuid(), citizen, reason));
        return citizen;
    }

    public void save(CitizenRecord citizen, String reason) {
        requireServer();
        cache.put(citizen.uuid(), citizen);
        storage.save(server, citizen);
        events.emitCitizenChanged(new ElarionEventBus.CitizenChanged(citizen.uuid(), citizen, reason));
    }

    private void requireServer() {
        if (server == null) throw new IllegalStateException("CitizenService is not bound to a server");
    }
}
