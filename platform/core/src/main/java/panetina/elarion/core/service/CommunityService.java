package panetina.elarion.core.service;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CommunityDefinition;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public final class CommunityService {
    private static final String TEAM_PREFIX = "elarion_";
    private final CoreConfigManager config;
    private final CitizenService citizens;

    public CommunityService(CoreConfigManager config, CitizenService citizens) {
        this.config = config;
        this.citizens = citizens;
    }

    public Collection<CommunityDefinition> all() {
        return config.communities().values();
    }

    public Optional<CommunityDefinition> find(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(config.communities().get(id.toLowerCase(Locale.ROOT)));
    }

    public Optional<CommunityDefinition> forCitizen(CitizenRecord citizen) {
        return find(citizen.communityId());
    }

    public boolean assign(ServerPlayerEntity player, String communityId) {
        Optional<CommunityDefinition> community = find(communityId);
        if (community.isEmpty()) return false;
        citizens.update(player, "community-assigned", citizen -> citizen.setCommunityId(community.get().id()));
        applyScoreboardTeam(player, community.get());
        return true;
    }

    public void remove(ServerPlayerEntity player) {
        citizens.update(player, "community-removed", citizen -> citizen.setCommunityId(null));
        removeElarionTeam(player);
    }

    public void applyCurrentScoreboardTeam(ServerPlayerEntity player) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        forCitizen(citizen).ifPresentOrElse(
                community -> applyScoreboardTeam(player, community),
                () -> removeElarionTeam(player)
        );
    }

    public void initializeScoreboardTeams(MinecraftServer server) {
        for (CommunityDefinition community : all()) {
            getOrCreateColorTeam(server.getScoreboard(), community.color());
        }
    }

    private void applyScoreboardTeam(ServerPlayerEntity player, CommunityDefinition community) {
        Scoreboard scoreboard = player.getServer().getScoreboard();
        removeElarionTeam(player);
        Team team = getOrCreateColorTeam(scoreboard, community.color());
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
