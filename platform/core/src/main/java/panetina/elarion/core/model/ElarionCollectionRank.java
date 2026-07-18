package panetina.elarion.core.model;

import java.util.Locale;

public enum ElarionCollectionRank {
    COMMON("COMMON", 0xFF72C878),
    UNCOMMON("UNCOMMON", 0xFF5CB7E8),
    RARE("RARE", 0xFFC084FF),
    EPIC("EPIC", 0xFFE45CBA),
    LEGENDARY("LEGENDARY", 0xFFFFA83D),
    SOVEREIGN("SOVEREIGN", 0xFFFFD36A),
    HEIR("HEIR", 0xFFE6B45A),
    COUNCIL("COUNCIL", 0xFF58D1A5),
    SYNOD("SYNOD", 0xFFC084FF),
    OFFICER("OFFICER", 0xFF5CB7E8),
    TRUSTED("TRUSTED", 0xFF9CC8FF);

    private final String label;
    private final int color;

    ElarionCollectionRank(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String label() {
        return label;
    }

    public int color() {
        return color;
    }

    public static ElarionCollectionRank byLabel(String label) {
        if (label == null || label.isBlank()) {
            return COMMON;
        }
        String normalized = label.trim().replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        for (ElarionCollectionRank rank : values()) {
            if (rank.label.equals(normalized) || rank.name().equals(normalized)) {
                return rank;
            }
        }
        return COMMON;
    }
}
