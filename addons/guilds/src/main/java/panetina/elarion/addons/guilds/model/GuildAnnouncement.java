package panetina.elarion.addons.guilds.model;

import java.util.UUID;

public record GuildAnnouncement(String id, UUID authorId, String body, long createdAt) {
    public GuildAnnouncement {
        id = id == null ? "" : id;
        body = body == null ? "" : body;
    }
}
