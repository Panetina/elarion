package panetina.elarion.addons.guilds.model;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import panetina.elarion.core.model.ElarionPixelAsset16;

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
        boolean legacyIcon = iconPaletteIndices != null && iconPaletteIndices.length == 1024;
        iconPaletteIndices = new ElarionPixelAsset16(iconRevision, legacyIcon ? downsampleLegacyIcon(iconPaletteIndices) : iconPaletteIndices).paletteIndices();
        if (legacyIcon && iconRevision < Long.MAX_VALUE) iconRevision++;
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
        ElarionPixelAsset16 icon = new ElarionPixelAsset16(iconRevision, iconPaletteIndices).revised(palette);
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
                "member", new GuildRole("member", "Member", 4, Set.of()),
                "veteran", new GuildRole("veteran", "Veteran", 5, Set.of()),
                "initiate", new GuildRole("initiate", "Initiate", 6, Set.of()),
                "newcomer", new GuildRole("newcomer", "Newcomer", 7, Set.of()));
    }

    /** Deterministically migrates a legacy 32x32 emblem by selecting each 2x2 block's dominant palette colour. */
    private static byte[] downsampleLegacyIcon(byte[] legacy) {
        byte[] migrated = new byte[ElarionPixelAsset16.PIXEL_COUNT];
        for (int row = 0; row < ElarionPixelAsset16.HEIGHT; row++) {
            for (int column = 0; column < ElarionPixelAsset16.WIDTH; column++) {
                int[] counts = new int[ElarionPixelAsset16.PALETTE_SIZE];
                for (int dy = 0; dy < 2; dy++) for (int dx = 0; dx < 2; dx++) {
                    int value = Byte.toUnsignedInt(legacy[(row * 2 + dy) * 32 + column * 2 + dx]);
                    if (value < counts.length) counts[value]++;
                }
                int chosen = 0;
                for (int color = 1; color < counts.length; color++) if (counts[color] > counts[chosen]) chosen = color;
                migrated[row * ElarionPixelAsset16.WIDTH + column] = (byte) chosen;
            }
        }
        return migrated;
    }
}
