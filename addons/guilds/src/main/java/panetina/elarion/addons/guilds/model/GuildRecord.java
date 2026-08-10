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
        members = members == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(members));
        roles = roles == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(roles));
        memberRoles = memberRoles == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(memberRoles));
        announcements = announcements == null ? List.of() : List.copyOf(announcements);
        iconPaletteIndices = new ElarionPixelAsset32(iconRevision, iconPaletteIndices).paletteIndices();
        createdAt = createdAt <= 0L ? System.currentTimeMillis() : createdAt;
    }

    public GuildRecord(String id, String displayName, String tag, boolean tagHidden, UUID leaderId,
                       Set<UUID> members, long createdAt) {
        this(id, displayName, tag, tagHidden, false, leaderId, members,
                defaults(leaderId), Map.of(leaderId, "owner"), List.of(), 0L, new byte[0], 0L, createdAt);
    }

    public static GuildRecord create(String id, String displayName, String tag, UUID leaderId) {
        return create(id, displayName, tag, false, leaderId);
    }

    public static GuildRecord create(String id, String displayName, String tag, boolean secret, UUID leaderId) {
        return new GuildRecord(id, displayName, tag, false, secret, leaderId, Set.of(leaderId),
                defaults(leaderId), Map.of(leaderId, "owner"), List.of(), 0L, new byte[0], 0L, System.currentTimeMillis());
    }

    @Override public byte[] iconPaletteIndices() { return iconPaletteIndices.clone(); }

    public GuildRecord withMembers(Set<UUID> updatedMembers) {
        Map<UUID, String> updatedRoles = new LinkedHashMap<>(memberRoles);
        updatedRoles.keySet().retainAll(updatedMembers);
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, updatedMembers, roles, updatedRoles,
                announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
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
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, updated, roles, updatedRoles,
                announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withTagHidden(boolean hidden) {
        return new GuildRecord(id, displayName, tag, hidden, secret, leaderId, members, roles, memberRoles,
                announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withRoles(Map<String, GuildRole> updatedRoles, Map<UUID, String> updatedAssignments) {
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, members, updatedRoles,
                updatedAssignments, announcements, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withAnnouncement(GuildAnnouncement announcement) {
        java.util.ArrayList<GuildAnnouncement> updated = new java.util.ArrayList<>(announcements);
        updated.add(0, announcement);
        if (updated.size() > 50) updated.subList(50, updated.size()).clear();
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, members, roles, memberRoles,
                updated, iconRevision, iconPaletteIndices, revision + 1L, createdAt);
    }

    public GuildRecord withIcon(byte[] palette) {
        ElarionPixelAsset32 icon = new ElarionPixelAsset32(iconRevision, iconPaletteIndices).revised(palette);
        return new GuildRecord(id, displayName, tag, tagHidden, secret, leaderId, members, roles, memberRoles,
                announcements, icon.revision(), icon.paletteIndices(), revision + 1L, createdAt);
    }

    private static Map<String, GuildRole> defaults(UUID leaderId) {
        return Map.of("owner", new GuildRole("owner", "Owner", Set.of(GuildPermission.values())),
                "member", new GuildRole("member", "Member", Set.of()));
    }
}
