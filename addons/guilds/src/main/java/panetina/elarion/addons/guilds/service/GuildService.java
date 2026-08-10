package panetina.elarion.addons.guilds.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.guilds.model.GuildConfig;
import panetina.elarion.addons.guilds.model.GuildInvite;
import panetina.elarion.addons.guilds.model.GuildRecord;
import panetina.elarion.addons.guilds.model.GuildPermission;
import panetina.elarion.addons.guilds.model.GuildRole;
import panetina.elarion.addons.guilds.model.GuildAnnouncement;
import panetina.elarion.addons.guilds.storage.GuildState;
import panetina.elarion.addons.guilds.storage.GuildStorage;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.service.ElarionNotificationService;
import panetina.elarion.core.service.PlayerRestrictionService;
import panetina.elarion.core.model.CitizenRecord;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.text.Normalizer;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class GuildService {
    /** Server-authored text projection for an immediate invitation prompt. */
    public record GuildInvitationView(String guildId, String guildName, String guildTag, String inviterName) { }

    private final ElarionApi api;
    private final GuildStorage storage;
    private final GuildWebProjectionPublisher webProjections;
    private GuildConfig config;
    private GuildState state = new GuildState();
    private MinecraftServer server;

    public GuildService(ElarionApi api, GuildStorage storage, GuildConfig config) {
        this.api = api;
        this.storage = storage;
        this.config = config;
        this.webProjections = new GuildWebProjectionPublisher(api);
    }

    public void reload(GuildConfig config) {
        this.config = config;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
        expireInvites();
        rebuildPlayerIndex();
        save();
        webProjections.publishAll(guilds());
    }

    public GuildConfig config() {
        return config;
    }

    public Collection<GuildRecord> guilds() {
        return state.guilds.values().stream()
                .sorted(Comparator.comparing(GuildRecord::id))
                .toList();
    }

    public Optional<GuildRecord> find(String id) {
        return Optional.ofNullable(state.guilds.get(normalizeId(id)));
    }

    public Optional<GuildRecord> guildFor(UUID playerId) {
        return Optional.ofNullable(state.playerGuilds.get(playerId)).flatMap(this::find);
    }

    public Set<GuildPermission> permissionsFor(UUID playerId) {
        return guildFor(playerId).map(guild -> effectivePermissions(guild, playerId)).orElse(Set.of());
    }

    public String tagFor(UUID playerId) {
        // Guild tags remain canonical internal identifiers, but are not part of
        // player-facing identity/chat presentation.
        return "";
    }

    public GuildRecord create(ServerPlayerEntity creator, String id, String tag, String displayName) {
        return create(creator, id, tag, displayName, false);
    }

    public GuildRecord create(ServerPlayerEntity creator, String id, String tag, String displayName, boolean secret) {
        requireEnabled();
        String normalizedId = normalizeId(id);
        String normalizedTag = normalizeTag(tag);
        String cleanName = normalizeName(displayName);
        validateIdentity(normalizedId, normalizedTag, cleanName);
        if (state.guilds.containsKey(normalizedId)) throw new IllegalArgumentException("Guild already exists.");
        if (state.guilds.values().stream().anyMatch(guild -> guild.tag().equals(normalizedTag))) {
            throw new IllegalArgumentException("Guild tag is already taken.");
        }
        if (state.playerGuilds.containsKey(creator.getUuid())) {
            throw new IllegalArgumentException("You are already in a guild.");
        }
        if (config.creationFee() > 0L) {
            var result = ElarionEconomyApi.get().payPhysicalOnly(
                    creator,
                    config.creationFee(),
                    "Guild creation: " + normalizedId,
                    "guilds"
            );
            if (!result.successful()) throw new IllegalArgumentException(result.message());
        }
        GuildRecord guild = GuildRecord.create(normalizedId, cleanName, normalizedTag, secret, creator.getUuid());
        state.guilds.put(guild.id(), guild);
        state.playerGuilds.put(creator.getUuid(), guild.id());
        save();
        api.history().record("guilds", "created", creator.getUuid(), "guild", guild.id(),
                realmOf(creator.getUuid()), java.util.Map.of("tag", guild.tag(), "name", guild.displayName()));
        webProjections.publishActive(guild);
        return guild;
    }

    /**
     * Player-facing creation never asks for a storage key. The canonical ID is
     * generated once on the server and remains an internal persistence/admin identity.
     */
    public GuildRecord createFromRegistrar(
            ServerPlayerEntity creator, String tag, String displayName, boolean secret
    ) {
        String cleanName = normalizeName(displayName);
        return create(creator, availableGeneratedId(cleanName, creator.getUuid()), tag, cleanName, secret);
    }

    String availableGeneratedId(String displayName, UUID creatorId) {
        String ascii = Normalizer.normalize(normalizeName(displayName), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        String base = ascii.isBlank() ? "guild" : ascii;
        for (int suffix = 1; suffix <= 10_000; suffix++) {
            String ending = suffix == 1 ? "" : "-" + suffix;
            int maximumBaseLength = Math.max(1, 32 - ending.length());
            String candidateBase = base.substring(0, Math.min(base.length(), maximumBaseLength))
                    .replaceAll("-+$", "");
            String candidate = candidateBase + ending;
            if (Pattern.matches(config.idPattern(), candidate) && !state.guilds.containsKey(candidate)) {
                return candidate;
            }
        }
        String fallback = "guild-" + creatorId.toString().replace("-", "").substring(0, 8);
        if (Pattern.matches(config.idPattern(), fallback) && !state.guilds.containsKey(fallback)) return fallback;
        throw new IllegalArgumentException("The configured Guild ID rules do not accept generated IDs.");
    }

    public GuildInvite invite(ServerPlayerEntity actor, ServerPlayerEntity target) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.INVITE);
        if (actor.getUuid().equals(target.getUuid())) throw new IllegalArgumentException("You cannot invite yourself.");
        if (state.playerGuilds.containsKey(target.getUuid())) {
            throw new IllegalArgumentException(target.getGameProfile().getName() + " is already in a guild.");
        }
        GuildInvite invite = new GuildInvite(guild.id(), target.getUuid(), actor.getUuid(), System.currentTimeMillis());
        state.invites.put(invite.key(), invite);
        save();
        api.history().record("guilds", "invite", actor.getUuid(), "guild", guild.id(),
                realmOf(actor.getUuid()), java.util.Map.of("target", target.getUuid().toString()));
        api.notifications().publishPersonal(
                target.getUuid(),
                ElarionNotificationCategory.PERSONAL,
                "elarion_guilds",
                "guild-invite",
                "guild-invite:" + invite.key(),
                "Guild Invitation",
                actor.getGameProfile().getName() + " invited you to " + guild.displayName()
                        + " [" + guild.tag() + "].",
                "Invitation",
                "item:minecraft:paper",
                java.util.List.of(
                        new ElarionNotificationAction("elarion_guilds:accept_invite", "Accept", true),
                        new ElarionNotificationAction("elarion_guilds:decline_invite", "Decline", true)),
                java.util.Map.of("guildId", guild.id(), "invitedBy", actor.getUuidAsString()),
                invite.createdAt() + config.inviteLifetimeMillis());
        return invite;
    }

    /** Resolves prompt copy from canonical membership state immediately after inviting. */
    public GuildInvitationView invitationView(ServerPlayerEntity inviter, UUID invitedPlayer) {
        GuildRecord guild = requirePermissionGuild(inviter.getUuid(), GuildPermission.INVITE);
        GuildInvite invite = state.invites.get(guild.id() + ":" + invitedPlayer);
        if (invite == null || !invite.invitedBy().equals(inviter.getUuid())) {
            throw new IllegalArgumentException("That Guild invitation is no longer pending.");
        }
        return new GuildInvitationView(guild.id(), guild.displayName(), guild.tag(), inviter.getGameProfile().getName());
    }

    public GuildRecord accept(ServerPlayerEntity player, String guildId) {
        requireEnabled();
        if (state.playerGuilds.containsKey(player.getUuid())) throw new IllegalArgumentException("You are already in a guild.");
        GuildRecord guild = requireGuild(guildId);
        int capacity = config.progression().tierFor(guild.progression().totalContributed()).memberCapacity();
        if (guild.members().size() >= capacity) {
            throw new IllegalArgumentException("This guild has reached its member capacity of " + capacity + ".");
        }
        GuildInvite invite = state.invites.get(guild.id() + ":" + player.getUuid());
        if (invite == null) throw new IllegalArgumentException("You do not have an invite to that guild.");
        if (invite.createdAt() + config.inviteLifetimeMillis() <= System.currentTimeMillis()) {
            state.invites.remove(invite.key());
            save();
            throw new IllegalArgumentException("That guild invitation expired.");
        }
        LinkedHashSet<UUID> members = new LinkedHashSet<>(guild.members());
        members.add(player.getUuid());
        GuildRecord updated = guild.withMembers(members);
        state.guilds.put(updated.id(), updated);
        state.playerGuilds.put(player.getUuid(), updated.id());
        state.invites.remove(invite.key());
        save();
        api.history().record("guilds", "joined", player.getUuid(), "guild", updated.id(),
                realmOf(player.getUuid()), java.util.Map.of("tag", updated.tag()));
        webProjections.publishActive(updated);
        notifyPersonal(invite.invitedBy(), "Guild Invitation Accepted",
                player.getGameProfile().getName() + " joined " + updated.displayName() + ".",
                "guild-invite-accepted:" + invite.key());
        return updated;
    }

    public GuildInvite decline(ServerPlayerEntity player, String guildId) {
        GuildRecord guild = requireGuild(guildId);
        GuildInvite invite = state.invites.remove(guild.id() + ":" + player.getUuid());
        if (invite == null) throw new IllegalArgumentException("You do not have an invite to that guild.");
        save();
        notifyPersonal(invite.invitedBy(), "Guild Invitation Declined",
                player.getGameProfile().getName() + " declined the invitation to " + guild.displayName() + ".",
                "guild-invite-declined:" + invite.key());
        return invite;
    }

    /** Member-accessible physical-Sigil contribution. Economy remains payment authority. */
    public GuildRecord donate(ServerPlayerEntity player, long amount) {
        GuildRecord guild = guildFor(player.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("You are not in a guild."));
        if (amount < 1L || amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Donation amount is invalid.");
        }
        var payment = ElarionEconomyApi.get().payPhysicalOnly(
                player, amount, "Guild contribution: " + guild.id(), "guilds");
        if (!payment.successful()) throw new IllegalArgumentException(payment.message());
        GuildRecord updated;
        try {
            updated = guild.withContribution(player.getUuid(), amount);
        } catch (ArithmeticException exception) {
            ElarionEconomyApi.get().refundMixedPayment(player, payment,
                    "Guild contribution rollback: " + guild.id(), "guilds");
            throw new IllegalArgumentException("Guild contribution total is at its safe limit.");
        }
        state.guilds.put(updated.id(), updated);
        save();
        api.history().record("guilds", "contributed", player.getUuid(), "guild", updated.id(),
                realmOf(player.getUuid()), java.util.Map.of("amount", Long.toString(amount)));
        webProjections.publishActive(updated);
        return updated;
    }

    public GuildRecord kick(ServerPlayerEntity actor, ServerPlayerEntity target) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.REMOVE_MEMBER);
        if (guild.leaderId().equals(target.getUuid())) throw new IllegalArgumentException("Transfer leadership before leaving.");
        if (!guild.members().contains(target.getUuid())) throw new IllegalArgumentException("Player is not in your guild.");
        LinkedHashSet<UUID> members = new LinkedHashSet<>(guild.members());
        members.remove(target.getUuid());
        GuildRecord updated = guild.withMembers(members);
        state.guilds.put(updated.id(), updated);
        state.playerGuilds.remove(target.getUuid());
        save();
        api.history().record("guilds", "kicked", actor.getUuid(), "guild", updated.id(),
                realmOf(actor.getUuid()), java.util.Map.of("target", target.getUuid().toString()));
        webProjections.publishActive(updated);
        webProjections.publishNoMembership(target.getUuid());
        notifyPersonal(target.getUuid(), "Removed From Guild",
                "You were removed from " + updated.displayName() + ".", "guild-kicked:" + updated.id()
                        + ":" + target.getUuid() + ":" + System.currentTimeMillis());
        return updated;
    }

    public GuildRecord leave(ServerPlayerEntity player) {
        GuildRecord guild = guildFor(player.getUuid()).orElseThrow(() -> new IllegalArgumentException("You are not in a guild."));
        if (guild.leaderId().equals(player.getUuid())) throw new IllegalArgumentException("Transfer leadership before leaving.");
        LinkedHashSet<UUID> members = new LinkedHashSet<>(guild.members());
        members.remove(player.getUuid());
        GuildRecord updated = guild.withMembers(members);
        state.guilds.put(updated.id(), updated);
        state.playerGuilds.remove(player.getUuid());
        save();
        api.history().record("guilds", "left", player.getUuid(), "guild", updated.id(),
                realmOf(player.getUuid()), java.util.Map.of("tag", updated.tag()));
        webProjections.publishActive(updated);
        webProjections.publishNoMembership(player.getUuid());
        return updated;
    }

    public GuildRecord transfer(ServerPlayerEntity actor, ServerPlayerEntity target) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.TRANSFER_OWNERSHIP);
        return transfer(guild.id(), target.getUuid(), actor.getUuid());
    }

    public GuildRecord transfer(String guildId, UUID target, UUID actorId) {
        GuildRecord guild = requireGuild(guildId);
        if (!guild.members().contains(target)) throw new IllegalArgumentException("New leader must be a guild member.");
        GuildRecord updated = guild.withLeader(target);
        state.guilds.put(updated.id(), updated);
        save();
        api.history().record("guilds", "leader-transferred", actorId, "guild", updated.id(),
                realmOf(target), java.util.Map.of("leader", target.toString()));
        webProjections.publishActive(updated);
        notifyPersonal(target, "Guild Leadership Transferred",
                "You are now the leader of " + updated.displayName() + ".",
                "guild-leader:" + updated.id() + ":" + target + ":" + System.currentTimeMillis());
        return updated;
    }

    public GuildRecord setTagHidden(ServerPlayerEntity actor, boolean hidden) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.EDIT_IDENTITY);
        GuildRecord updated = guild.withTagHidden(hidden);
        state.guilds.put(updated.id(), updated);
        save();
        api.history().record("guilds", hidden ? "tag-hidden" : "tag-shown", actor.getUuid(), "guild", updated.id(),
                realmOf(actor.getUuid()), java.util.Map.of("tag", updated.tag()));
        webProjections.publishActive(updated);
        return updated;
    }

    public GuildRecord assignRole(ServerPlayerEntity actor, UUID memberId, String roleId) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.ASSIGN_ROLES);
        if (!guild.members().contains(memberId)) throw new IllegalArgumentException("Player is not in your guild.");
        if (!guild.roles().containsKey(roleId) || "owner".equals(roleId)) throw new IllegalArgumentException("That role cannot be assigned.");
        int actorPosition = rolePosition(guild, actor.getUuid());
        int targetPosition = rolePosition(guild, memberId);
        int assignedPosition = guild.roles().get(roleId).position();
        if (actorPosition >= targetPosition || actorPosition >= assignedPosition) {
            throw new IllegalArgumentException("You may only manage ranks below your own.");
        }
        java.util.Map<UUID, String> assignments = new java.util.LinkedHashMap<>(guild.memberRoles());
        assignments.put(memberId, roleId);
        GuildRecord updated = guild.withRoles(guild.roles(), assignments);
        state.guilds.put(updated.id(), updated);
        save();
        webProjections.publishActive(updated);
        return updated;
    }

    public GuildRecord createRole(ServerPlayerEntity actor, String id, String name, Set<GuildPermission> permissions) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.MANAGE_ROLES);
        String normalized = normalizeId(id);
        if (normalized.isBlank() || normalized.length() > 24 || "owner".equals(normalized) || "member".equals(normalized)) {
            throw new IllegalArgumentException("Role ID is invalid.");
        }
        if (guild.roles().size() >= 12 || guild.roles().containsKey(normalized)) throw new IllegalArgumentException("Role limit reached or ID already used.");
        Set<GuildPermission> allowed = permissions == null ? Set.of() : Set.copyOf(permissions);
        if (!effectivePermissions(guild, actor.getUuid()).containsAll(allowed)) throw new IllegalArgumentException("You cannot grant permissions you do not hold.");
        java.util.Map<String, GuildRole> roles = new java.util.LinkedHashMap<>(guild.roles());
        int position = roles.values().stream().mapToInt(GuildRole::position).max().orElse(0) + 1;
        roles.put(normalized, new GuildRole(normalized, normalizeName(name), position, allowed));
        GuildRecord updated = guild.withRoles(roles, guild.memberRoles());
        state.guilds.put(updated.id(), updated);
        save();
        webProjections.publishActive(updated);
        return updated;
    }

    public GuildRecord publishAnnouncement(ServerPlayerEntity actor, String body) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.PUBLISH_ANNOUNCEMENTS);
        String message = normalizeName(body);
        if (message.isBlank() || message.length() > 500) throw new IllegalArgumentException("Announcement must be 1-500 characters.");
        GuildRecord updated = guild.withAnnouncement(new GuildAnnouncement(UUID.randomUUID().toString(), actor.getUuid(), message, System.currentTimeMillis()));
        state.guilds.put(updated.id(), updated);
        save();
        webProjections.publishActive(updated);
        for (UUID member : updated.members()) notifyPersonal(member, "Guild Announcement", message, "guild-announcement:" + updated.id() + ":" + updated.revision());
        return updated;
    }

    public GuildRecord redrawIcon(ServerPlayerEntity actor, byte[] paletteIndices) {
        GuildRecord guild = requirePermissionGuild(actor.getUuid(), GuildPermission.REDRAW_ICON);
        GuildRecord updated = guild.withIcon(paletteIndices);
        state.guilds.put(updated.id(), updated);
        save();
        webProjections.publishActive(updated);
        return updated;
    }

    public GuildRecord delete(String guildId, UUID actorId) {
        GuildRecord guild = requireGuild(guildId);
        state.guilds.remove(guild.id());
        guild.members().forEach(state.playerGuilds::remove);
        state.invites.entrySet().removeIf(entry -> entry.getValue().guildId().equals(guild.id()));
        save();
        api.history().record("guilds", "deleted", actorId, "guild", guild.id(),
                realmOf(guild.leaderId()), java.util.Map.of("tag", guild.tag()));
        webProjections.publishInactive(guild);
        guild.members().forEach(member -> notifyPersonal(member, "Guild Disbanded",
                guild.displayName() + " was disbanded.", "guild-deleted:" + guild.id() + ":" + member));
        return guild;
    }

    public void resetCharacter(UUID accountId) {
        GuildRecord guild = guildFor(accountId).orElse(null);
        state.invites.entrySet().removeIf(entry -> accountId.equals(entry.getValue().invitedPlayer())
                || accountId.equals(entry.getValue().invitedBy()));
        if (guild == null) {
            save();
            return;
        }
        if (guild.leaderId().equals(accountId)) {
            delete(guild.id(), accountId);
            return;
        }
        LinkedHashSet<UUID> members = new LinkedHashSet<>(guild.members());
        members.remove(accountId);
        state.guilds.put(guild.id(), guild.withMembers(members));
        state.playerGuilds.remove(accountId);
        save();
        webProjections.publishActive(guild.withMembers(members));
        webProjections.publishNoMembership(accountId);
    }

    public synchronized int resetAllPlayerState() {
        int changed = state.playerGuilds.size();
        state = new GuildState();
        save();
        return changed;
    }

    private void expireInvites() {
        long now = System.currentTimeMillis();
        boolean changed = state.invites.entrySet().removeIf(entry ->
                entry.getValue().createdAt() + config.inviteLifetimeMillis() <= now);
        if (changed) save();
    }

    private void notifyPersonal(UUID recipient, String title, String body, String dedupe) {
        api.notifications().publishPersonal(
                recipient,
                ElarionNotificationCategory.PERSONAL,
                "elarion_guilds",
                "guild-membership",
                dedupe,
                title,
                body,
                "Guilds",
                "item:minecraft:paper",
                java.util.List.of(new ElarionNotificationAction(
                        ElarionNotificationService.DISMISS, "Dismiss", true)),
                java.util.Map.of(),
                api.notifications().defaultExpiry());
    }

    public boolean sendGuildMessage(ServerPlayerEntity sender, String message) {
        if (message == null || message.isBlank()) return false;
        if (api.system().restrictions().denyWithMessage(sender, PlayerRestrictionService.GUILD_CHAT)) return false;
        GuildRecord guild = guildFor(sender.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("You are not in a guild."));
        Text output = Text.literal("[Guild] ").formatted(Formatting.DARK_AQUA)
                .append(api.identities().resolve(sender).chatName())
                .append(Text.literal(" \u00bb " + message).formatted(Formatting.GRAY));
        for (ServerPlayerEntity recipient : sender.getServer().getPlayerManager().getPlayerList()) {
            if (guild.members().contains(recipient.getUuid())) {
                recipient.sendMessage(output, false);
            } else if (api.chat().isChatSpy(recipient)) {
                recipient.sendMessage(Text.literal("[Spy:Guild] ")
                        .append(api.identities().resolve(sender).chatName())
                        .append(Text.literal(": " + message).formatted(Formatting.GRAY)), false);
            }
        }
        api.history().record("chat", "guild-message", sender.getUuid(), "guild", guild.id(),
                realmOf(sender.getUuid()), java.util.Map.of("channel", "guild", "guild", guild.id()));
        return true;
    }

    private GuildRecord requirePermissionGuild(UUID playerId, GuildPermission permission) {
        GuildRecord guild = guildFor(playerId).orElseThrow(() -> new IllegalArgumentException("You are not in a guild."));
        if (!effectivePermissions(guild, playerId).contains(permission)) throw new IllegalArgumentException("You do not have that guild permission.");
        return guild;
    }

    private static Set<GuildPermission> effectivePermissions(GuildRecord guild, UUID playerId) {
        if (guild.leaderId().equals(playerId)) return Set.of(GuildPermission.values());
        String roleId = guild.memberRoles().getOrDefault(playerId, "member");
        GuildRole role = guild.roles().get(roleId);
        return role == null ? Set.of() : role.permissions();
    }

    private static int rolePosition(GuildRecord guild, UUID playerId) {
        if (guild.leaderId().equals(playerId)) return 1;
        String roleId = guild.memberRoles().getOrDefault(playerId, "member");
        GuildRole role = guild.roles().get(roleId);
        return role == null ? Integer.MAX_VALUE : role.position();
    }

    private GuildRecord requireGuild(String guildId) {
        return find(guildId).orElseThrow(() -> new IllegalArgumentException("Unknown guild: " + guildId));
    }

    private void validateIdentity(String id, String tag, String displayName) {
        if (!Pattern.matches(config.idPattern(), id)) throw new IllegalArgumentException("Guild ID is invalid.");
        if (tag.length() < config.minTagLength() || tag.length() > config.maxTagLength()
                || !Pattern.matches(config.tagPattern(), tag)) {
            throw new IllegalArgumentException("Guild tag is invalid.");
        }
        if (config.blockedTags().contains(tag)) throw new IllegalArgumentException("Guild tag is blocked.");
        if (displayName.isBlank() || displayName.length() > config.maxNameLength()) {
            throw new IllegalArgumentException("Guild display name is invalid.");
        }
    }

    private void rebuildPlayerIndex() {
        state.playerGuilds.clear();
        for (GuildRecord guild : state.guilds.values()) {
            for (UUID member : guild.members()) {
                state.playerGuilds.putIfAbsent(member, guild.id());
            }
        }
    }

    private void requireEnabled() {
        if (!config.enabled()) throw new IllegalArgumentException("Guilds are disabled.");
    }

    private void save() {
        if (server != null) storage.save(server, state);
    }

    private String realmOf(UUID playerId) {
        if (playerId == null) return "";
        return api.citizens().find(playerId).map(CitizenRecord::realmId).orElse("");
    }

    private static String normalizeId(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeRealm(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeTag(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
