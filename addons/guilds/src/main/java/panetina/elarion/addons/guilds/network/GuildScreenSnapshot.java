package panetina.elarion.addons.guilds.network;

import panetina.elarion.addons.guilds.model.GuildAnnouncement;
import panetina.elarion.addons.guilds.model.GuildRecord;
import panetina.elarion.addons.guilds.model.GuildRole;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Bounded, viewer-safe projection for the Guild management client surface. */
public record GuildScreenSnapshot(
        String guildId, String displayName, String tag, boolean secret, UUID leaderId,
        long revision, List<UUID> members, Map<String, GuildRole> roles,
        Map<UUID, String> memberRoles, Map<UUID, Long> memberJoinedAt, List<GuildAnnouncement> announcements
) {
    public static GuildScreenSnapshot from(GuildRecord guild) {
        return new GuildScreenSnapshot(guild.id(), guild.displayName(), guild.tag(), guild.secret(), guild.leaderId(),
                guild.revision(), guild.members().stream().limit(256).toList(), guild.roles(), guild.memberRoles(), guild.memberJoinedAt(),
                guild.announcements().stream().limit(50).toList());
    }
}
