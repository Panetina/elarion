package panetina.elarion.addons.groups.model;

import java.util.UUID;

public record GroupInvite(
        String groupId,
        UUID invitedPlayer,
        UUID invitedBy,
        long createdAt
) {
    public GroupInvite {
        groupId = groupId == null ? "" : groupId;
        createdAt = createdAt <= 0L ? System.currentTimeMillis() : createdAt;
    }

    public String key() {
        return groupId + ":" + invitedPlayer;
    }
}
