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

    public void set(String key, long value) {
        String normalized = key == null ? "" : key.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.isBlank()) return;
        switch (normalized) {
            case "zombie_kills" -> zombieKills = Math.max(0L, value);
            case "dragon_kills" -> dragonKills = Math.max(0L, value);
            default -> customCounters.put(normalized, Math.max(0L, value));
        }
    }

    public long value(String key) {
        String normalized = key == null ? "" : key.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "zombie_kills" -> zombieKills;
            case "dragon_kills" -> dragonKills;
            default -> customCounters.getOrDefault(normalized, 0L);
        };
    }
}
