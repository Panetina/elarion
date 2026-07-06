package panetina.elarion.core.service;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmPresentation;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class RealmService {
    private static final String TEAM_PREFIX = "elarion_";
    private final CoreConfigManager config;
    private final CitizenService citizens;
    private final Map<String, Optional<RealmDefinition>> worldOwnerCache = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Function<RealmDefinition, Optional<RealmPresentation>>> presentationProviders =
            new CopyOnWriteArrayList<>();
    private Map<String, RealmDefinition> cachedRealmSource = Map.of();

    public RealmService(CoreConfigManager config, CitizenService citizens) {
        this.config = config;
        this.citizens = citizens;
    }

    public Collection<RealmDefinition> all() {
        return config.realms().values();
    }

    public Optional<RealmDefinition> find(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(config.realms().get(id.toLowerCase(Locale.ROOT)));
    }

    public Optional<RealmDefinition> forCitizen(CitizenRecord citizen) {
        return find(citizen.realmId());
    }

    public void registerPresentationProvider(Function<RealmDefinition, Optional<RealmPresentation>> provider) {
        if (provider != null) presentationProviders.add(provider);
    }

    public RealmPresentation presentation(RealmDefinition realm) {
        if (realm == null) return RealmPresentation.from(null);
        for (Function<RealmDefinition, Optional<RealmPresentation>> provider : presentationProviders) {
            try {
                Optional<RealmPresentation> presentation = provider.apply(realm);
                if (presentation != null && presentation.isPresent()) return presentation.get();
            } catch (RuntimeException ignored) {
                // Addon presentation hooks must not break Core Realm rendering.
            }
        }
        return RealmPresentation.from(realm);
    }

    public String displayName(RealmDefinition realm) {
        return presentation(realm).displayName();
    }

    public String officialName(RealmDefinition realm) {
        return presentation(realm).officialName();
    }

    public String shortName(RealmDefinition realm) {
        return presentation(realm).shortName();
    }

    public String prefix(RealmDefinition realm) {
        return presentation(realm).prefix();
    }

    public String color(RealmDefinition realm) {
        return presentation(realm).color();
    }

    public Optional<RealmDefinition> ownerForWorld(String worldId) {
        if (worldId == null || worldId.isBlank()) return Optional.empty();
        Map<String, RealmDefinition> current = config.realms();
        if (cachedRealmSource != current) {
            worldOwnerCache.clear();
            cachedRealmSource = current;
        }
        return worldOwnerCache.computeIfAbsent(worldId, id -> all().stream()
                .filter(realm -> realm.spawn() != null && id.equals(realm.spawn().worldId()))
                .findFirst());
    }

    public boolean assign(ServerPlayerEntity player, String realmId) {
        Optional<RealmDefinition> realm = find(realmId);
        if (realm.isEmpty()) return false;
        citizens.update(player, "realm-assigned", citizen -> {
            if (!realm.get().id().equals(citizen.realmId())) {
                citizen.clearRealmAffiliation();
            }
            citizen.setRealmId(realm.get().id());
        });
        applyScoreboardTeam(player, realm.get());
        return true;
    }

    public void remove(ServerPlayerEntity player) {
        citizens.update(player, "realm-removed", CitizenRecord::clearRealmAffiliation);
        removeElarionTeam(player);
    }

    public void applyCurrentScoreboardTeam(ServerPlayerEntity player) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        forCitizen(citizen).ifPresentOrElse(
                realm -> applyScoreboardTeam(player, realm),
                () -> removeElarionTeam(player)
        );
    }

    public void initializeScoreboardTeams(MinecraftServer server) {
        for (RealmDefinition realm : all()) {
            getOrCreateColorTeam(server.getScoreboard(), realm.color());
        }
    }

    private void applyScoreboardTeam(ServerPlayerEntity player, RealmDefinition realm) {
        Scoreboard scoreboard = player.getServer().getScoreboard();
        removeElarionTeam(player);
        Team team = getOrCreateColorTeam(scoreboard, color(realm));
        scoreboard.addScoreHolderToTeam(player.getGameProfile().getName(), team);
    }

    private void removeElarionTeam(ServerPlayerEntity player) {
        Scoreboard scoreboard = player.getServer().getScoreboard();
        Team current = scoreboard.getScoreHolderTeam(player.getGameProfile().getName());
        if (current != null && current.getName().startsWith(TEAM_PREFIX)) {
            scoreboard.removeScoreHolderFromTeam(player.getGameProfile().getName(), current);
        }
    }

    private static Team getOrCreateColorTeam(Scoreboard scoreboard, String configuredColor) {
        String normalized = configuredColor.toLowerCase(Locale.ROOT).replace(' ', '_');
        String teamName = TEAM_PREFIX + normalized;
        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.addTeam(teamName);
        Formatting color = Formatting.byName(normalized);
        team.setColor(color == null ? Formatting.WHITE : color);
        team.setPrefix(Text.empty());
        return team;
    }
}
