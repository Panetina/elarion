package panetina.elarion.addons.npcs.model;

import java.util.Locale;

public enum NpcPresentationKind {
    DIALOGUE,
    BANK,
    TRADE;

    public static NpcPresentationKind parse(String value) {
        if (value == null || value.isBlank()) return DIALOGUE;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return DIALOGUE;
        }
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
