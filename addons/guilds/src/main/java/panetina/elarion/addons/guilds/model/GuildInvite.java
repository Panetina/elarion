package panetina.elarion.addons.guilds.model;

import java.util.UUID;

public record GuildInvite(
        String guildId,
        UUID invitedPlayer,
        UUID invitedBy,
        long createdAt
) {
    public GuildInvite {
        guildId = guildId == null ? "" : guildId;
        createdAt = createdAt <= 0L ? System.currentTimeMillis() : createdAt;
    }

    public String key() {
        return guildId + ":" + invitedPlayer;
    }
}
