package panetina.elarion.addons.worlds.model;

import java.util.Locale;

public enum WorldType {
    VOID,
    FLAT,
    OVERWORLD,
    NETHER,
    END,
    CAVE,
    CUSTOM;

    public static WorldType parse(String value) {
        return valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}
