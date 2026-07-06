package panetina.elarion.core.model;

import java.util.Locale;

public enum ElarionNotificationCategory {
    PERSONAL,
    MAIL,
    REWARD,
    REALM,
    GOVERNMENT,
    WORLD,
    QUEST;

    public static ElarionNotificationCategory parse(String raw) {
        if (raw == null || raw.isBlank()) return PERSONAL;
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return PERSONAL;
        }
    }

    public boolean matchesFilter(String filter) {
        String normalized = filter == null ? "all" : filter.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "personal" -> this == PERSONAL || this == MAIL || this == REWARD;
            case "realm" -> this == REALM || this == GOVERNMENT;
            case "world" -> this == WORLD;
            case "quest" -> this == QUEST;
            case "all", "" -> true;
            default -> name().equalsIgnoreCase(normalized);
        };
    }
}
