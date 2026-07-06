package panetina.elarion.addons.quests.model;

import java.util.Locale;

public enum QuestVariableType {
    BOOLEAN,
    INTEGER,
    STRING;

    public static QuestVariableType parse(String raw) {
        if (raw == null || raw.isBlank()) return STRING;
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "bool", "boolean" -> BOOLEAN;
            case "int", "integer", "number" -> INTEGER;
            case "string", "text" -> STRING;
            default -> throw new IllegalArgumentException("Unknown quest variable type " + raw);
        };
    }

    public String normalize(String raw) {
        return switch (this) {
            case BOOLEAN -> Boolean.toString(Boolean.parseBoolean(raw == null ? "false" : raw));
            case INTEGER -> {
                try {
                    yield Integer.toString(Integer.parseInt(raw == null || raw.isBlank() ? "0" : raw));
                } catch (NumberFormatException exception) {
                    yield "0";
                }
            }
            case STRING -> raw == null ? "" : raw;
        };
    }
}
