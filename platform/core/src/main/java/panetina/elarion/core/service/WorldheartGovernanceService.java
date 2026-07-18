package panetina.elarion.core.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.model.WorldheartAuthority;
import panetina.elarion.core.model.WorldheartAuthorityType;
import panetina.elarion.core.model.WorldheartGovernanceRole;
import panetina.elarion.core.storage.WorldheartAuthorityStorage;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public final class WorldheartGovernanceService {
    private final WorldheartAuthorityStorage storage;
    private final Predicate<UUID> citizenExists;
    private final Function<UUID, Optional<String>> defaultDisplayResolver;
    private final ElarionEventBus events;
    private final CopyOnWriteArrayList<Function<UUID, Optional<String>>> displayResolvers =
            new CopyOnWriteArrayList<>();
    private MinecraftServer server;
    private boolean bound;
    private WorldheartAuthority authority = WorldheartAuthority.defaultSystem();

    public WorldheartGovernanceService(
            WorldheartAuthorityStorage storage,
            Predicate<UUID> citizenExists,
            Function<UUID, Optional<String>> defaultDisplayResolver,
            ElarionEventBus events
    ) {
        this.storage = storage;
        this.citizenExists = citizenExists;
        this.defaultDisplayResolver = defaultDisplayResolver;
        this.events = events;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        this.bound = true;
        WorldheartAuthority loaded = storage.load(server);
        if (loaded.type() == WorldheartAuthorityType.PLAYER
                && !citizenExists.test(loaded.rulerId())) {
            authority = WorldheartAuthority.defaultSystem();
            storage.save(server, authority);
            return;
        }
        authority = loaded;
    }

    public synchronized WorldheartAuthority authority() {
        return authority;
    }

    public synchronized boolean isSystemGoverned() {
        return authority.type() == WorldheartAuthorityType.SYSTEM;
    }

    public synchronized Optional<UUID> rulerId() {
        return authority.playerRulerId();
    }

    public synchronized boolean isRuler(UUID playerId) {
        return playerId != null && authority.type() == WorldheartAuthorityType.PLAYER
                && playerId.equals(authority.rulerId());
    }

    public WorldheartGovernanceRole role(ServerPlayerEntity player) {
        if (player == null) return WorldheartGovernanceRole.NONE;
        return role(player.getUuid(), player.hasPermissionLevel(4));
    }

    public synchronized WorldheartGovernanceRole role(UUID playerId, boolean serverAdministrator) {
        if (serverAdministrator) return WorldheartGovernanceRole.ADMINISTRATOR;
        return isRuler(playerId) ? WorldheartGovernanceRole.RULER : WorldheartGovernanceRole.NONE;
    }

    public boolean canGovern(ServerPlayerEntity player) {
        return role(player) != WorldheartGovernanceRole.NONE;
    }

    public void registerPlayerDisplayResolver(Function<UUID, Optional<String>> resolver) {
        if (resolver != null) displayResolvers.add(resolver);
    }

    public String authorityDisplayName() {
        WorldheartAuthority current = authority();
        if (current.type() == WorldheartAuthorityType.SYSTEM) return current.systemDisplayName();
        for (Function<UUID, Optional<String>> resolver : displayResolvers) {
            try {
                Optional<String> resolved = resolver.apply(current.rulerId());
                if (resolved != null && resolved.isPresent() && !resolved.get().isBlank()) return resolved.get();
            } catch (RuntimeException ignored) {
                // Optional display providers cannot break authority checks.
            }
        }
        return defaultDisplayResolver.apply(current.rulerId()).orElse("Unknown Ruler");
    }

    public synchronized void setSystemAuthority(UUID actorId) {
        if (authority.type() == WorldheartAuthorityType.SYSTEM) return;
        change(WorldheartAuthority.system(authority.systemDisplayName(), System.currentTimeMillis()), actorId);
    }

    public synchronized void setPlayerAuthority(UUID rulerId, UUID actorId) {
        requireBound();
        if (rulerId == null || !citizenExists.test(rulerId)) {
            throw new IllegalArgumentException("Worldheart ruler must be an existing Ember");
        }
        if (isRuler(rulerId)) return;
        change(WorldheartAuthority.player(rulerId, authority.systemDisplayName(), System.currentTimeMillis()), actorId);
    }

    private void change(WorldheartAuthority next, UUID actorId) {
        requireBound();
        WorldheartAuthority previous = authority;
        if (previous.equals(next)) return;
        storage.save(server, next);
        authority = next;
        events.emitDomainEvent(ElarionDomainEvent.of(
                "elarion_core", "worldheart-authority-changed", actorId, "",
                "worldheart_authority", "worldheart",
                Map.of(
                        "previousType", previous.type().name(),
                        "previousRulerId", previous.rulerId() == null ? "" : previous.rulerId().toString(),
                        "newType", next.type().name(),
                        "newRulerId", next.rulerId() == null ? "" : next.rulerId().toString(),
                        "changedAt", Long.toString(next.changedAt()))));
    }

    private void requireBound() {
        if (!bound) throw new IllegalStateException("Worldheart governance is not bound");
    }
}
