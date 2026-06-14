package panetina.elarion.addons.groups.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public record GroupRecord(
        String id,
        String displayName,
        String tag,
        UUID leaderId,
        Set<UUID> members,
        long createdAt
) {
    public GroupRecord {
        id = id == null ? "" : id;
        displayName = displayName == null ? "" : displayName;
        tag = tag == null ? "" : tag;
        members = members == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(members));
        createdAt = createdAt <= 0L ? System.currentTimeMillis() : createdAt;
    }

    public static GroupRecord create(String id, String displayName, String tag, UUID leaderId) {
        return new GroupRecord(id, displayName, tag, leaderId, Set.of(leaderId), System.currentTimeMillis());
    }

    public GroupRecord withMembers(Set<UUID> updatedMembers) {
        return new GroupRecord(id, displayName, tag, leaderId, updatedMembers, createdAt);
    }

    public GroupRecord withLeader(UUID leaderId) {
        LinkedHashSet<UUID> updated = new LinkedHashSet<>(members);
        updated.add(leaderId);
        return new GroupRecord(id, displayName, tag, leaderId, updated, createdAt);
    }
}
