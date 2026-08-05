package panetina.elarion.addons.guilds.model;

import java.util.Set;

public record GuildRole(String id, String displayName, Set<GuildPermission> permissions) {
    public GuildRole {
        id = id == null ? "" : id;
        displayName = displayName == null ? "" : displayName;
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
