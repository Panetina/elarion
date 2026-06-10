package panetina.elarion.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerStats {
    private final UUID uuid;
    private long zombieKills;
    private long dragonKills;
    private final Map<String, Long> customCounters = new LinkedHashMap<>();

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() { return uuid; }
    public long zombieKills() { return zombieKills; }
    public long dragonKills() { return dragonKills; }
    public Map<String, Long> customCounters() { return customCounters; }

    public void setZombieKills(long value) { zombieKills = Math.max(0, value); }
    public void setDragonKills(long value) { dragonKills = Math.max(0, value); }

    public long increment(String key, long amount) {
        long safeAmount = Math.max(0, amount);
        return switch (key) {
            case "zombie_kills" -> {
                zombieKills += safeAmount;
                yield zombieKills;
            }
            case "dragon_kills" -> {
                dragonKills += safeAmount;
                yield dragonKills;
            }
            default -> customCounters.merge(key, safeAmount, Long::sum);
        };
    }
}
