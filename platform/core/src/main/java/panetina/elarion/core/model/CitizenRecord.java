package panetina.elarion.core.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class CitizenRecord {
    private final UUID uuid;
    private String lastKnownUsername;
    private String realmId;
    private String titleId;
    private String nickname;
    private CitizenStatus status;
    private long joinedAt;
    private final Set<String> flags;
    private final Set<String> grantedAbilities;

    public CitizenRecord(UUID uuid, String username) {
        this.uuid = uuid;
        this.lastKnownUsername = username;
        this.status = CitizenStatus.ACTIVE;
        this.joinedAt = Instant.now().toEpochMilli();
        this.flags = new LinkedHashSet<>();
        this.grantedAbilities = new LinkedHashSet<>();
    }

    public UUID uuid() { return uuid; }
    public String lastKnownUsername() { return lastKnownUsername; }
    public String realmId() { return realmId == null ? "" : realmId; }
    public String titleId() { return titleId; }
    public String nickname() { return nickname; }
    public CitizenStatus status() { return status; }
    public long joinedAt() { return joinedAt; }
    public Set<String> flags() { return flags; }
    public Set<String> grantedAbilities() { return grantedAbilities; }

    public void setLastKnownUsername(String value) { this.lastKnownUsername = value; }
    public void setRealmId(String value) { this.realmId = value == null ? "" : value; }
    public void setTitleId(String value) { this.titleId = value; }
    public void setNickname(String value) { this.nickname = value; }
    public void setStatus(CitizenStatus value) { this.status = value; }
    public void setJoinedAt(long value) { this.joinedAt = value; }
}
