package panetina.elarion.addons.quests.model;

import java.util.Locale;

public enum QuestVariableScope {
    SHARED,
    PLAYER;

    public static QuestVariableScope parse(String raw) {
        if (raw == null || raw.isBlank()) return SHARED;
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "player", "personal" -> PLAYER;
            case "shared", "quest", "realm", "world" -> SHARED;
            default -> throw new IllegalArgumentException("Unknown quest variable scope " + raw);
        };
    }
}
