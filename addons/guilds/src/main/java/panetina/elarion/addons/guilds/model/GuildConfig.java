package panetina.elarion.addons.guilds.model;

import java.util.Set;

public record GuildConfig(
        boolean enabled,
        long creationFee,
        int minTagLength,
        int maxTagLength,
        int maxNameLength,
        String idPattern,
        String tagPattern,
        Set<String> blockedTags,
        long inviteLifetimeMillis
) {
    public GuildConfig(
            boolean enabled, long creationFee, int minTagLength, int maxTagLength, int maxNameLength,
            String idPattern, String tagPattern, Set<String> blockedTags
    ) {
        this(enabled, creationFee, minTagLength, maxTagLength, maxNameLength,
                idPattern, tagPattern, blockedTags, java.time.Duration.ofDays(7).toMillis());
    }

    public GuildConfig {
        creationFee = Math.max(0L, creationFee);
        minTagLength = Math.max(1, minTagLength);
        maxTagLength = Math.max(minTagLength, maxTagLength);
        maxNameLength = Math.max(3, maxNameLength);
        idPattern = idPattern == null || idPattern.isBlank() ? "[a-z0-9_-]{3,32}" : idPattern;
        tagPattern = tagPattern == null || tagPattern.isBlank() ? "[A-Z0-9]{2,6}" : tagPattern;
        blockedTags = blockedTags == null ? Set.of() : Set.copyOf(blockedTags);
        inviteLifetimeMillis = Math.max(60_000L, inviteLifetimeMillis);
    }

    public static GuildConfig defaults() {
        return new GuildConfig(true, 25L, 2, 6, 48,
                "[a-z0-9_-]{3,32}", "[A-Z0-9]{2,6}", Set.of("ADMIN", "STAFF", "OP"),
                java.time.Duration.ofDays(7).toMillis());
    }
}
