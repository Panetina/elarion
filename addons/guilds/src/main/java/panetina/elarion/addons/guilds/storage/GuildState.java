package panetina.elarion.addons.guilds.storage;

import panetina.elarion.addons.guilds.model.GuildInvite;
import panetina.elarion.addons.guilds.model.GuildRecord;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class GuildState {
    public Map<String, GuildRecord> guilds = new LinkedHashMap<>();
    public Map<UUID, String> playerGuilds = new LinkedHashMap<>();
    public Map<String, GuildInvite> invites = new LinkedHashMap<>();

    public GuildState copy() {
        GuildState copy = new GuildState();
        copy.guilds = new LinkedHashMap<>(guilds);
        copy.playerGuilds = new LinkedHashMap<>(playerGuilds);
        copy.invites = new LinkedHashMap<>(invites);
        return copy;
    }
}
