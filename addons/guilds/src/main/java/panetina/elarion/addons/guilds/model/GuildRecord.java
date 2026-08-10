package panetina.elarion.addons.guilds.model;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import panetina.elarion.core.model.ElarionPixelAsset32;

public record GuildRecord(
        String id,
        String displayName,
        String tag,
        boolean tagHidden,
        boolean secret,
        UUID leaderId,
        Set<UUID> members,
        Map<String, GuildRole> roles,
        Map<UUID, String> memberRoles,
        Map<UUID, Long> memberJoinedAt,
        GuildProgression progression,
        List<GuildAnnouncement> announcements,
        long iconRevision,
        byte[] iconPaletteIndices,
        long revision,
        long createdAt
) {
    public GuildRecord {
        id = id == null ? "" : id;
        displayName = displayName == null ? "" : displayName;
        tag = tag == null ? "" : tag;
        createdAt = createdAt <= 0L ? System.currentTimeMillis() : createdAt;
        LinkedHashSet<UUID> normalizedMembers = new LinkedHashSet<>(members == null ? Set.of() : members);
        if (leaderId != null) normalizedMembers.add(leaderId);
        members = Set.copyOf(normalizedMembers);
        // Old state had only owner/member. Keep any custom role definitions while
        // backfilling the standard ordered ranks introduced after V1.
        LinkedHashMap<String, GuildRole> normalizedGuildRoles = new LinkedHashMap<>(defaults(leaderId));
        if (roles != null) normalizedGuildRoles.putAll(roles);
        roles = Map.copyOf(normalizedGuildRoles);
        LinkedHashMap<UUID, String> normalizedRoles = new LinkedHashMap<>(memberRoles == null ? Map.of() : memberRoles);
        normalizedRoles.keySet().retainAll(members);
        if (leaderId != null) normalizedRoles.put(leaderId, "owner");
        memberRoles = Map.copyOf(normalizedRoles);
        LinkedHashMap<UUID, Long> normalizedJoinedAt = new LinkedHashMap<>(memberJoinedAt == null ? Map.of() : memberJoinedAt);
        normalizedJoinedAt.keySet().retainAll(members);
        for (UUID member : members) normalizedJoinedAt.putIfAbsent(member, createdAt);
        memberJoinedAt = Map.copyOf(normalizedJoinedAt);
        progression = progression == null ? GuildProgression.empty() : progression;
        announcements = announcements == null ? List.of() : List.copyOf(announcements);
        iconPaletteIndices = new ElarionPixelAsset32(iconRevision, iconPaletteIndices).paletteIndices();
    }

    public GuildRecord(String id, String displayName, String tag, boolean tagHidden, UUID leaderId,
                       Set<UUID> members, long createdAt) {
        this(id, displayName, tag, tagHidden, false, leaderId, members,
                defaults(leaderId), Map.of(leaderId, "owner"), Map.of(leaderId, createdAt), GuildProgression.empty(), List.of(), 0L, new byte[0], 0L, createdAt);
    }

    public static GuildRecord create(String id, String displayName, String tag, UUID leaderId) {
        return create(id, displayName, tag, false, leaderId);
    }

    public static GuildRecord create(String id, String displayName, String tag, boolean secret, UUID leaderId) {
        long now = System.currentTimeMillis();
        return new GuildRecord(id, displayName, tag, false, secret, leaderId, Set.of(leaderId),
                defaults(leaderId), Map.of(leaderId, "owner"), Map.of(leaderId, now), GuildProgression.empty(), List.of(), 0L, new byte[0], 0L, now);
    }

    @Override public byte[] iconPaletteIndices() { return iconPaletteIndices.clone(); }

    public GuildRecord withMembers(Set<UUID> updatedMembers) {
        return withMembers(updatedMembers, System.currentTimeMillis());
    }

    GuildRecord withMembers(Set<UUID> updatedMembers, long joinedAt) {
        Map<UUID, String> updatedRoles = new LinkedHashMap<>(memberRoles);
        updatedRoles.keySet().retainAll(updatedMembers);
        Map<UUID, Long> updatedJoinedAt = new LinkedHashMap<>(memberJoinedAt);
        updatedJoinedAt.keySet().retainAll(updatedMembers);
        for (UUID member : updatedMembers) updatedJoinedAt.putIfAbsent(member, joinedAt);
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, updatedMembers, roles, updatedRoles,
                updatedJoinedAt, progression, announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withLeader(UUID leaderId) {
        LinkedHashSet<UUID> updated = new LinkedHashSet<>(members);
        updated.add(leaderId);
        Map<UUID, String> updatedRoles = new LinkedHashMap<>(memberRoles);
        // `owner` is the unique rank-one role. A transfer must never leave the
        // former leader with its full permission set.
        if (this.leaderId != null && !this.leaderId.equals(leaderId)) {
            updatedRoles.put(this.leaderId, "member");
        }
        updatedRoles.put(leaderId, "owner");
        Map<UUID, Long> updatedJoinedAt = new LinkedHashMap<>(memberJoinedAt);
        updatedJoinedAt.putIfAbsent(leaderId, System.currentTimeMillis());
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, updated, roles, updatedRoles,
                updatedJoinedAt, progression, announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withTagHidden(boolean hidden) {
        return new GuildRecord(id, displayName, tag, hidden, secret, leaderId, members, roles, memberRoles,
                memberJoinedAt, progression, announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withRoles(Map<String, GuildRole> updatedRoles, Map<UUID, String> updatedAssignments) {
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, members, updatedRoles,
                updatedAssignments, memberJoinedAt, progression, announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withAnnouncement(GuildAnnouncement announcement) {
        java.util.ArrayList<GuildAnnouncement> updated = new java.util.ArrayList<>(announcements);
        updated.add(0, announcement);
        if (updated.size() > 50) updated.subList(50, updated.size()).clear();
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, members, roles, memberRoles,
                memberJoinedAt, progression, updated, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withIcon(byte[] palette) {
        ElarionPixelAsset32 icon = new ElarionPixelAsset32(iconRevision, iconPaletteIndices).revised(palette);
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, members, roles, memberRoles,
                memberJoinedAt, progression, announcements, icon.revision(), icon.paletteIndices(), revision + 1L, createdAt);
    }

    public GuildRecord withSecret(boolean updatedSecret) {
        return new GuildRecord(id, displayName, tag, tagHidden, updatedSecret, leaderId, members, roles, memberRoles,
                memberJoinedAt, progression, announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withContribution(UUID operationId, UUID memberId, long amount) {
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, members, roles, memberRoles,
                memberJoinedAt, progression.contribute(operationId, memberId, amount), announcements, iconRevision,
                iconPaletteIndices, revision + 1L, createdAt);
    }

    private static Map<String, GuildRole> defaults(UUID leaderId) {
        return Map.of(
                "owner", new GuildRole("owner", "Leader", 1, Set.of(GuildPermission.values())),
                "officer", new GuildRole("officer", "Officer", 2, Set.of(GuildPermission.INVITE, GuildPermission.REMOVE_MEMBER, GuildPermission.ASSIGN_ROLES, GuildPermission.PUBLISH_ANNOUNCEMENTS)),
                "recruiter", new GuildRole("recruiter", "Recruiter", 3, Set.of(GuildPermission.INVITE)),
                "member", new GuildRole("member", "Member", 4, Set.of()));
    }
}
