package panetina.elarion.addons.underworld.model;

public final class UnderworldSession {
    public String playerId = "";
    public String corpseId = "";
    public long startedAt;
    public long remainingMillis;
    public boolean paused;
    public long pausedAt;
    public ElarionDeathType deathType = ElarionDeathType.UNKNOWN;
    public boolean wasAuthority;
}
