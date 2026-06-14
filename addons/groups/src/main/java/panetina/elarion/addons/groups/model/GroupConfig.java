package panetina.elarion.addons.groups.model;

import java.util.Set;

public record GroupConfig(
        boolean enabled,
        long creationFee,
        int minTagLength,
        int maxTagLength,
        int maxNameLength,
        String idPattern,
        String tagPattern,
        Set<String> blockedTags
) {
    public GroupConfig {
        creationFee = Math.max(0L, creationFee);
        minTagLength = Math.max(1, minTagLength);
        maxTagLength = Math.max(minTagLength, maxTagLength);
        maxNameLength = Math.max(3, maxNameLength);
        idPattern = idPattern == null || idPattern.isBlank() ? "[a-z0-9_-]{3,32}" : idPattern;
        tagPattern = tagPattern == null || tagPattern.isBlank() ? "[A-Z0-9]{2,6}" : tagPattern;
        blockedTags = blockedTags == null ? Set.of() : Set.copyOf(blockedTags);
    }

    public static GroupConfig defaults() {
        return new GroupConfig(true, 25L, 2, 6, 48,
                "[a-z0-9_-]{3,32}", "[A-Z0-9]{2,6}", Set.of("ADMIN", "STAFF", "OP"));
    }
}
