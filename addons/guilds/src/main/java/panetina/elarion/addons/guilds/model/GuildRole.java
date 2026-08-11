package panetina.elarion.addons.guilds.model;

import java.util.Set;

/** A rank is ordered: lower positions have higher authority. */
public record GuildRole(String id, String displayName, int position, Set<GuildPermission> permissions) {
    public GuildRole {
        id = id == null ? "" : id;
        displayName = displayName == null ? "" : displayName;
        position = position > 0 ? position : defaultPosition(id);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    private static int defaultPosition(String id) {
        return switch (id) {
            case "owner" -> 1;
            case "officer" -> 2;
            case "recruiter" -> 3;
            case "member" -> 4;
            case "veteran" -> 5;
            case "initiate" -> 6;
            case "newcomer" -> 7;
            default -> 100;
        };
    }
}
