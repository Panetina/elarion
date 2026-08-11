package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.guilds.model.GuildAnnouncement;
import panetina.elarion.addons.guilds.model.GuildRecord;
import panetina.elarion.addons.guilds.model.GuildRole;
import panetina.elarion.addons.guilds.model.GuildProgressionConfig;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Bounded player-facing Guild overview. Mutations use separate validated actions. */
public record GuildScreenOpenPayload(
        String guildId, String displayName, String tag, boolean secret, UUID leaderId,
        long revision, int level, long totalContributed, int memberCapacity, long nextLevelContribution,
        byte[] iconPixels, List<String> viewerPermissions,
        List<Member> members, List<Role> roles, List<Announcement> announcements,
        List<InviteCandidate> inviteCandidates
) implements CustomPayload {
    public static final Id<GuildScreenOpenPayload> ID = new Id<>(Identifier.of("elarion_guilds", "screen_open"));
    public static final PacketCodec<PacketByteBuf, GuildScreenOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.guildId, 64);
                ElarionPacketCodecs.writeString(buffer, payload.displayName, 128);
                ElarionPacketCodecs.writeString(buffer, payload.tag, 32);
                buffer.writeBoolean(payload.secret); buffer.writeUuid(payload.leaderId); buffer.writeLong(payload.revision); buffer.writeVarInt(payload.level); buffer.writeLong(payload.totalContributed); buffer.writeVarInt(payload.memberCapacity); buffer.writeLong(payload.nextLevelContribution); buffer.writeVarInt(payload.iconPixels.length); buffer.writeBytes(payload.iconPixels);
                buffer.writeVarInt(payload.viewerPermissions.size()); for (String permission : payload.viewerPermissions) ElarionPacketCodecs.writeString(buffer, permission, 48);
                buffer.writeVarInt(payload.members.size()); for (Member member : payload.members) member.write(buffer);
                buffer.writeVarInt(payload.roles.size()); for (Role role : payload.roles) role.write(buffer);
                buffer.writeVarInt(payload.announcements.size()); for (Announcement announcement : payload.announcements) announcement.write(buffer);
                buffer.writeVarInt(payload.inviteCandidates.size()); for (InviteCandidate candidate : payload.inviteCandidates) candidate.write(buffer);
            }, buffer -> {
                String id = ElarionPacketCodecs.readString(buffer, 64); String name = ElarionPacketCodecs.readString(buffer, 128);
                String tag = ElarionPacketCodecs.readString(buffer, 32); boolean secret = buffer.readBoolean(); UUID leader = buffer.readUuid(); long revision = buffer.readLong(); int level = ElarionPacketCodecs.readBoundedCount(buffer, 64); long total = buffer.readLong(); int capacity = ElarionPacketCodecs.readBoundedCount(buffer, 4096); long next = buffer.readLong();
                int iconLength = ElarionPacketCodecs.readBoundedCount(buffer, 256); byte[] icon = new byte[iconLength]; buffer.readBytes(icon);
                int permissionCount = ElarionPacketCodecs.readBoundedCount(buffer, 8); List<String> permissions = new ArrayList<>(permissionCount);
                for (int i = 0; i < permissionCount; i++) permissions.add(ElarionPacketCodecs.readString(buffer, 48));
                int memberCount = ElarionPacketCodecs.readBoundedCount(buffer, 256); List<Member> members = new ArrayList<>(memberCount);
                for (int i = 0; i < memberCount; i++) members.add(Member.read(buffer));
                int roleCount = ElarionPacketCodecs.readBoundedCount(buffer, 12); List<Role> roles = new ArrayList<>(roleCount);
                for (int i = 0; i < roleCount; i++) roles.add(Role.read(buffer));
                int announcementCount = ElarionPacketCodecs.readBoundedCount(buffer, 50); List<Announcement> announcements = new ArrayList<>(announcementCount);
                for (int i = 0; i < announcementCount; i++) announcements.add(Announcement.read(buffer));
                int candidateCount = ElarionPacketCodecs.readBoundedCount(buffer, 32); List<InviteCandidate> candidates = new ArrayList<>(candidateCount);
                for (int i = 0; i < candidateCount; i++) candidates.add(InviteCandidate.read(buffer));
                return new GuildScreenOpenPayload(id, name, tag, secret, leader, revision, level, total, capacity, next, icon, permissions,
                        members, roles, announcements, candidates);
            });
    public GuildScreenOpenPayload {
        guildId = guildId == null ? "" : guildId;
        displayName = displayName == null ? "" : displayName;
        tag = tag == null ? "" : tag;
        level = Math.max(1, level);
        totalContributed = Math.max(0L, totalContributed);
        memberCapacity = Math.max(1, memberCapacity);
        nextLevelContribution = Math.max(0L, nextLevelContribution);
        iconPixels = iconPixels == null ? new byte[0] : iconPixels.clone();
        viewerPermissions = viewerPermissions == null ? List.of() : List.copyOf(viewerPermissions);
        members = members == null ? List.of() : List.copyOf(members);
        roles = roles == null ? List.of() : List.copyOf(roles);
        announcements = announcements == null ? List.of() : List.copyOf(announcements);
        inviteCandidates = inviteCandidates == null ? List.of() : List.copyOf(inviteCandidates);
        if ((iconPixels.length != 0 && iconPixels.length != 256)
                || viewerPermissions.size() > 8 || members.size() > 256 || roles.size() > 12
                || announcements.size() > 50 || inviteCandidates.size() > 32
                || roles.stream().anyMatch(role -> role.permissions().size() > 8)) {
            throw new IllegalArgumentException("Guild screen projection exceeds its bounded contract.");
        }
    }
    @Override public byte[] iconPixels() { return iconPixels.clone(); }
    public static GuildScreenOpenPayload from(
            GuildRecord guild, java.util.function.Function<UUID, String> name, java.util.function.Function<UUID, Realm> realm,
            GuildProgressionConfig progression, List<String> viewerPermissions, List<InviteCandidate> inviteCandidates
    ) {
        int level = progression.levelFor(guild.progression().totalContributed());
        long next = level >= progression.tiers().size() ? 0L : progression.tiers().get(level).requiredContributions();
        return new GuildScreenOpenPayload(guild.id(), guild.displayName(), guild.tag(), guild.secret(), guild.leaderId(), guild.revision(), level, guild.progression().totalContributed(), progression.tierFor(guild.progression().totalContributed()).memberCapacity(), next, guild.iconPaletteIndices(), viewerPermissions,
                guild.members().stream()
                        .sorted(java.util.Comparator
                                .comparing((UUID id) -> !id.equals(guild.leaderId()))
                                .thenComparingLong(id -> guild.memberJoinedAt().getOrDefault(id, guild.createdAt()))
                                .thenComparing(UUID::toString))
                        .limit(256)
                        .map(id -> new Member(id, name.apply(id), realm.apply(id), guild.memberRoles().getOrDefault(id, "member"), guild.memberJoinedAt().getOrDefault(id, guild.createdAt())))
                        .toList(),
                guild.roles().values().stream().sorted(java.util.Comparator.comparingInt(GuildRole::position)).limit(12).map(Role::from).toList(),
                guild.announcements().stream().limit(50).map(value -> new Announcement(value.id(), name.apply(value.authorId()), value.body(), value.createdAt())).toList(),
                inviteCandidates == null ? List.of() : inviteCandidates.stream().limit(32).toList());
    }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
    public record Member(UUID id, String name, Realm realm, String role, long joinedAt) {
        public Member { name = name == null ? "" : name; realm = realm == null ? Realm.UNASSIGNED : realm; role = role == null ? "member" : role; }
        void write(PacketByteBuf b) { b.writeUuid(id); ElarionPacketCodecs.writeString(b, name, 128); realm.write(b); ElarionPacketCodecs.writeString(b, role, 32); b.writeLong(joinedAt); }
        static Member read(PacketByteBuf b) { return new Member(b.readUuid(), ElarionPacketCodecs.readString(b, 128), Realm.read(b), ElarionPacketCodecs.readString(b, 32), b.readLong()); }
    }
    /** Realm presentation is Core-authored and included only in this bounded Guild member projection. */
    public record Realm(String displayName, int color) {
        public static final Realm UNASSIGNED = new Realm("Unassigned", 0xFFB89552);
        public Realm { displayName = displayName == null || displayName.isBlank() ? "Unassigned" : displayName; color = 0xFF000000 | (color & 0x00FFFFFF); }
        void write(PacketByteBuf b) { ElarionPacketCodecs.writeString(b, displayName, 128); b.writeInt(color); }
        static Realm read(PacketByteBuf b) { return new Realm(ElarionPacketCodecs.readString(b, 128), b.readInt()); }
    }
    public record Role(String id, String displayName, int position, List<String> permissions) {
        public Role { id = id == null ? "" : id; displayName = displayName == null ? "" : displayName; permissions = permissions == null ? List.of() : List.copyOf(permissions); }
        static Role from(GuildRole role) { return new Role(role.id(), role.displayName(), role.position(), role.permissions().stream().map(Enum::name).sorted().toList()); }
        void write(PacketByteBuf b) { ElarionPacketCodecs.writeString(b, id, 24); ElarionPacketCodecs.writeString(b, displayName, 96); b.writeVarInt(position); b.writeVarInt(permissions.size()); for (String permission : permissions) ElarionPacketCodecs.writeString(b, permission, 48); }
        static Role read(PacketByteBuf b) { String id = ElarionPacketCodecs.readString(b, 24); String displayName = ElarionPacketCodecs.readString(b, 96); int position = ElarionPacketCodecs.readBoundedCount(b, 256); int count = ElarionPacketCodecs.readBoundedCount(b, 8); List<String> values = new ArrayList<>(count); for (int i = 0; i < count; i++) values.add(ElarionPacketCodecs.readString(b, 48)); return new Role(id, displayName, position, values); }
    }
    public record Announcement(String id, String author, String body, long createdAt) {
        public Announcement { id = id == null ? "" : id; author = author == null ? "" : author; body = body == null ? "" : body; }
        void write(PacketByteBuf b) { ElarionPacketCodecs.writeString(b, id, 64); ElarionPacketCodecs.writeString(b, author, 128); ElarionPacketCodecs.writeString(b, body, 500); b.writeLong(createdAt); }
        static Announcement read(PacketByteBuf b) { return new Announcement(ElarionPacketCodecs.readString(b, 64), ElarionPacketCodecs.readString(b, 128), ElarionPacketCodecs.readString(b, 500), b.readLong()); }
    }
    public record InviteCandidate(UUID id, String name) {
        public InviteCandidate { name = name == null ? "" : name; }
        void write(PacketByteBuf b) { b.writeUuid(id); ElarionPacketCodecs.writeString(b, name, 128); }
        static InviteCandidate read(PacketByteBuf b) { return new InviteCandidate(b.readUuid(), ElarionPacketCodecs.readString(b, 128)); }
    }
}
