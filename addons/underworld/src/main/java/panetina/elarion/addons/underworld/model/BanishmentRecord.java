package panetina.elarion.addons.underworld.model;

public final class BanishmentRecord {
    public static final int MAX_REASON_LENGTH = 256;

    public String playerId = "";
    public String playerName = "";
    public String issuedBy = "";
    public String reason = "";
    public long issuedAt;
    /** Zero means permanent. */
    public long expiresAt;

    public boolean permanent() {
        return expiresAt <= 0L;
    }

    public boolean activeAt(long now) {
        return permanent() || expiresAt > now;
    }

    public long remainingMillis(long now) {
        return permanent() ? 0L : Math.max(0L, expiresAt - now);
    }

    public BanishmentRecord normalized() {
        playerId = clean(playerId, 64);
        playerName = clean(playerName, 64);
        issuedBy = clean(issuedBy, 64);
        reason = clean(reason, MAX_REASON_LENGTH);
        issuedAt = Math.max(0L, issuedAt);
        expiresAt = Math.max(0L, expiresAt);
        return this;
    }

    private static String clean(String value, int maximum) {
        if (value == null) return "";
        String clean = value.strip();
        return clean.length() <= maximum ? clean : clean.substring(0, maximum);
    }
}
