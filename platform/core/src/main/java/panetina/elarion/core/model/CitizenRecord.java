package panetina.elarion.core.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CitizenRecord {
    private final UUID uuid;
    private String lastKnownUsername;
    private String realmId;
    private String leaderRealmId;
    private String activeTitleId;
    private String nickname;
    private CitizenStatus status;
    private long joinedAt;
    private final Set<String> flags;
    private final Set<String> grantedAbilities;
    private final Set<String> unlockedTitleIds;
    private final Map<String, Long> titleUnlockTimes;

    public CitizenRecord(UUID uuid, String username) {
        this.uuid = uuid;
        this.lastKnownUsername = username;
        this.status = CitizenStatus.ACTIVE;
        this.joinedAt = Instant.now().toEpochMilli();
        this.flags = new LinkedHashSet<>();
        this.grantedAbilities = new LinkedHashSet<>();
        this.unlockedTitleIds = new LinkedHashSet<>();
        this.titleUnlockTimes = new LinkedHashMap<>();
    }

    public UUID uuid() { return uuid; }
    public String lastKnownUsername() { return lastKnownUsername; }
    public String realmId() { return realmId == null ? "" : realmId; }
    public String leaderRealmId() { return leaderRealmId == null ? "" : leaderRealmId; }
    public boolean isRealmLeader() { return !leaderRealmId().isBlank(); }
    public String titleId() { return activeTitleId(); }
    public String activeTitleId() { return activeTitleId == null ? "" : activeTitleId; }
    public String nickname() { return nickname; }
    public CitizenStatus status() { return status; }
    public long joinedAt() { return joinedAt; }
    public Set<String> flags() { return flags; }
    public Set<String> grantedAbilities() { return grantedAbilities; }
    public Set<String> unlockedTitleIds() { return unlockedTitleIds; }
    public Map<String, Long> titleUnlockTimes() { return titleUnlockTimes; }
    public boolean hasUnlockedTitle(String titleId) {
        return titleId != null && unlockedTitleIds.contains(normalizeTitleId(titleId));
    }

    public void setLastKnownUsername(String value) { this.lastKnownUsername = value; }
    public void setRealmId(String value) { this.realmId = value == null ? "" : value; }
    public void setLeaderRealmId(String value) { this.leaderRealmId = value == null ? "" : value; }
    public void clearRealmAffiliation() {
        this.realmId = "";
        this.leaderRealmId = "";
    }
    public void setTitleId(String value) { setActiveTitleId(value); }
    public void setActiveTitleId(String value) {
        String normalized = normalizeTitleId(value);
        this.activeTitleId = normalized;
        if (!normalized.isBlank()) unlockTitle(normalized, Instant.now().toEpochMilli());
    }
    public void clearActiveTitle() { this.activeTitleId = ""; }
    public void unlockTitle(String titleId, long unlockedAt) {
        String normalized = normalizeTitleId(titleId);
        if (normalized.isBlank()) return;
        unlockedTitleIds.add(normalized);
        titleUnlockTimes.putIfAbsent(normalized, unlockedAt <= 0 ? Instant.now().toEpochMilli() : unlockedAt);
    }
    public void revokeTitle(String titleId) {
        String normalized = normalizeTitleId(titleId);
        if (normalized.isBlank()) return;
        unlockedTitleIds.remove(normalized);
        titleUnlockTimes.remove(normalized);
        if (normalized.equals(activeTitleId())) clearActiveTitle();
    }
    public void setNickname(String value) { this.nickname = value; }
    public void setStatus(CitizenStatus value) { this.status = value; }
    public void setJoinedAt(long value) { this.joinedAt = value; }

    private static String normalizeTitleId(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
