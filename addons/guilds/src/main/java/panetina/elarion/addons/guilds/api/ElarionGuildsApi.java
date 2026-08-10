package panetina.elarion.addons.guilds.api;

import panetina.elarion.addons.guilds.model.GuildRecord;
import panetina.elarion.addons.guilds.service.GuildService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class ElarionGuildsApi {
    private static ElarionGuildsApi instance;
    private final GuildService guilds;

    public ElarionGuildsApi(GuildService guilds) {
        if (instance != null) throw new IllegalStateException("ElarionGuildsApi is already initialized");
        this.guilds = guilds;
        instance = this;
    }

    public static ElarionGuildsApi get() {
        if (instance == null) throw new IllegalStateException("Elarion Guilds has not initialized yet");
        return instance;
    }

    public GuildService guilds() {
        return guilds;
    }

    public Collection<GuildRecord> all() {
        return guilds.guilds();
    }

    public Optional<GuildRecord> find(String id) {
        return guilds.find(id);
    }

    public Optional<GuildRecord> guildFor(UUID playerId) {
        return guilds.guildFor(playerId);
    }
}
