package panetina.elarion.addons.groups.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.groups.model.GroupConfig;
import panetina.elarion.addons.groups.model.GroupInvite;
import panetina.elarion.addons.groups.model.GroupRecord;
import panetina.elarion.addons.groups.storage.GroupState;
import panetina.elarion.addons.groups.storage.GroupStorage;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class GroupService {
    private final ElarionApi api;
    private final GroupStorage storage;
    private GroupConfig config;
    private GroupState state = new GroupState();
    private MinecraftServer server;

    public GroupService(ElarionApi api, GroupStorage storage, GroupConfig config) {
        this.api = api;
        this.storage = storage;
        this.config = config;
    }

    public void reload(GroupConfig config) {
        this.config = config;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
        expireInvites();
        rebuildPlayerIndex();
        save();
    }

    public GroupConfig config() {
        return config;
    }

    public Collection<GroupRecord> groups() {
        return state.groups.values().stream()
                .sorted(Comparator.comparing(GroupRecord::id))
                .toList();
    }

    public Optional<GroupRecord> find(String id) {
        return Optional.ofNullable(state.groups.get(normalizeId(id)));
    }

    public Optional<GroupRecord> groupFor(UUID playerId) {
        return Optional.ofNullable(state.playerGroups.get(playerId)).flatMap(this::find);
    }

    public String tagFor(UUID playerId) {
        return groupFor(playerId)
                .filter(group -> !group.tagHidden())
                .map(group -> "[" + group.tag() + "]")
                .orElse("");
    }

    public GroupRecord create(ServerPlayerEntity creator, String id, String tag, String displayName) {
        requireEnabled();
        String normalizedId = normalizeId(id);
        String normalizedTag = normalizeTag(tag);
        String cleanName = normalizeName(displayName);
        validateIdentity(normalizedId, normalizedTag, cleanName);
        if (state.groups.containsKey(normalizedId)) throw new IllegalArgumentException("Group already exists.");
        if (state.groups.values().stream().anyMatch(group -> group.tag().equals(normalizedTag))) {
            throw new IllegalArgumentException("Group tag is already taken.");
        }
        if (state.playerGroups.containsKey(creator.getUuid())) {
            throw new IllegalArgumentException("You are already in a group.");
        }
        if (config.creationFee() > 0L) {
            var result = ElarionEconomyApi.get().sink(
                    EconomyAccount.player(creator.getUuid()),
                    config.creationFee(),
                    creator.getUuid(),
                    "Group creation: " + normalizedId,
                    "groups"
            );
            if (!result.successful()) throw new IllegalArgumentException(result.message());
        }
        GroupRecord group = GroupRecord.create(normalizedId, cleanName, normalizedTag, creator.getUuid());
        state.groups.put(group.id(), group);
        state.playerGroups.put(creator.getUuid(), group.id());
        save();
        api.history().record("groups", "created", creator.getUuid(), "group", group.id(),
                realmOf(creator.getUuid()), java.util.Map.of("tag", group.tag(), "name", group.displayName()));
        return group;
    }

    public GroupInvite invite(ServerPlayerEntity actor, ServerPlayerEntity target) {
        GroupRecord group = requireLeaderGroup(actor.getUuid());
        if (actor.getUuid().equals(target.getUuid())) throw new IllegalArgumentException("You cannot invite yourself.");
        if (state.playerGroups.containsKey(target.getUuid())) {
            throw new IllegalArgumentException(target.getGameProfile().getName() + " is already in a group.");
        }
        validateConfederationLock(group, target.getUuid());
        GroupInvite invite = new GroupInvite(group.id(), target.getUuid(), actor.getUuid(), System.currentTimeMillis());
        state.invites.put(invite.key(), invite);
        save();
        api.history().record("groups", "invite", actor.getUuid(), "group", group.id(),
                realmOf(actor.getUuid()), java.util.Map.of("target", target.getUuid().toString()));
        api.notifications().publishPersonal(
                target.getUuid(),
                ElarionNotificationCategory.PERSONAL,
                "elarion_groups",
                "group-invite",
                "group-invite:" + invite.key(),
                "Group Invitation",
                actor.getGameProfile().getName() + " invited you to " + group.displayName()
                        + " [" + group.tag() + "].",
                "Invitation",
                "item:minecraft:paper",
                java.util.List.of(
                        new ElarionNotificationAction("elarion_groups:accept_invite", "Accept", true),
                        new ElarionNotificationAction("elarion_groups:decline_invite", "Decline", true)),
                java.util.Map.of("groupId", group.id(), "invitedBy", actor.getUuidAsString()),
                invite.createdAt() + config.inviteLifetimeMillis());
        return invite;
    }

    public GroupRecord accept(ServerPlayerEntity player, String groupId) {
        requireEnabled();
        if (state.playerGroups.containsKey(player.getUuid())) throw new IllegalArgumentException("You are already in a group.");
        GroupRecord group = requireGroup(groupId);
        GroupInvite invite = state.invites.get(group.id() + ":" + player.getUuid());
        if (invite == null) throw new IllegalArgumentException("You do not have an invite to that group.");
        if (invite.createdAt() + config.inviteLifetimeMillis() <= System.currentTimeMillis()) {
            state.invites.remove(invite.key());
            save();
            throw new IllegalArgumentException("That group invitation expired.");
        }
        validateConfederationLock(group, player.getUuid());
        LinkedHashSet<UUID> members = new LinkedHashSet<>(group.members());
        members.add(player.getUuid());
        GroupRecord updated = group.withMembers(members);
        state.groups.put(updated.id(), updated);
        state.playerGroups.put(player.getUuid(), updated.id());
        state.invites.remove(invite.key());
        save();
        api.history().record("groups", "joined", player.getUuid(), "group", updated.id(),
                realmOf(player.getUuid()), java.util.Map.of("tag", updated.tag()));
        notifyPersonal(invite.invitedBy(), "Group Invitation Accepted",
                player.getGameProfile().getName() + " joined " + updated.displayName() + ".",
                "group-invite-accepted:" + invite.key());
        return updated;
    }

    public GroupInvite decline(ServerPlayerEntity player, String groupId) {
        GroupRecord group = requireGroup(groupId);
        GroupInvite invite = state.invites.remove(group.id() + ":" + player.getUuid());
        if (invite == null) throw new IllegalArgumentException("You do not have an invite to that group.");
        save();
        notifyPersonal(invite.invitedBy(), "Group Invitation Declined",
                player.getGameProfile().getName() + " declined the invitation to " + group.displayName() + ".",
                "group-invite-declined:" + invite.key());
        return invite;
    }

    public GroupRecord kick(ServerPlayerEntity actor, ServerPlayerEntity target) {
        GroupRecord group = requireLeaderGroup(actor.getUuid());
        if (group.leaderId().equals(target.getUuid())) throw new IllegalArgumentException("Transfer leadership before leaving.");
        if (!group.members().contains(target.getUuid())) throw new IllegalArgumentException("Player is not in your group.");
        LinkedHashSet<UUID> members = new LinkedHashSet<>(group.members());
        members.remove(target.getUuid());
        GroupRecord updated = group.withMembers(members);
        state.groups.put(updated.id(), updated);
        state.playerGroups.remove(target.getUuid());
        save();
        api.history().record("groups", "kicked", actor.getUuid(), "group", updated.id(),
                realmOf(actor.getUuid()), java.util.Map.of("target", target.getUuid().toString()));
        notifyPersonal(target.getUuid(), "Removed From Group",
                "You were removed from " + updated.displayName() + ".", "group-kicked:" + updated.id()
                        + ":" + target.getUuid() + ":" + System.currentTimeMillis());
        return updated;
    }

    public GroupRecord leave(ServerPlayerEntity player) {
        GroupRecord group = groupFor(player.getUuid()).orElseThrow(() -> new IllegalArgumentException("You are not in a group."));
        if (group.leaderId().equals(player.getUuid())) throw new IllegalArgumentException("Transfer leadership before leaving.");
        LinkedHashSet<UUID> members = new LinkedHashSet<>(group.members());
        members.remove(player.getUuid());
        GroupRecord updated = group.withMembers(members);
        state.groups.put(updated.id(), updated);
        state.playerGroups.remove(player.getUuid());
        save();
        api.history().record("groups", "left", player.getUuid(), "group", updated.id(),
                realmOf(player.getUuid()), java.util.Map.of("tag", updated.tag()));
        return updated;
    }

    public GroupRecord transfer(ServerPlayerEntity actor, ServerPlayerEntity target) {
        GroupRecord group = requireLeaderGroup(actor.getUuid());
        return transfer(group.id(), target.getUuid(), actor.getUuid());
    }

    public GroupRecord transfer(String groupId, UUID target, UUID actorId) {
        GroupRecord group = requireGroup(groupId);
        if (!group.members().contains(target)) throw new IllegalArgumentException("New leader must be a group member.");
        GroupRecord updated = group.withLeader(target);
        state.groups.put(updated.id(), updated);
        save();
        api.history().record("groups", "leader-transferred", actorId, "group", updated.id(),
                realmOf(target), java.util.Map.of("leader", target.toString()));
        notifyPersonal(target, "Group Leadership Transferred",
                "You are now the leader of " + updated.displayName() + ".",
                "group-leader:" + updated.id() + ":" + target + ":" + System.currentTimeMillis());
        return updated;
    }

    public GroupRecord setTagHidden(ServerPlayerEntity actor, boolean hidden) {
        GroupRecord group = requireLeaderGroup(actor.getUuid());
        GroupRecord updated = group.withTagHidden(hidden);
        state.groups.put(updated.id(), updated);
        save();
        api.history().record("groups", hidden ? "tag-hidden" : "tag-shown", actor.getUuid(), "group", updated.id(),
                realmOf(actor.getUuid()), java.util.Map.of("tag", updated.tag()));
        return updated;
    }

    public GroupRecord delete(String groupId, UUID actorId) {
        GroupRecord group = requireGroup(groupId);
        state.groups.remove(group.id());
        group.members().forEach(state.playerGroups::remove);
        state.invites.entrySet().removeIf(entry -> entry.getValue().groupId().equals(group.id()));
        state.confederationLockedGroups.remove(group.id());
        save();
        api.history().record("groups", "deleted", actorId, "group", group.id(),
                realmOf(group.leaderId()), java.util.Map.of("tag", group.tag()));
        group.members().forEach(member -> notifyPersonal(member, "Group Disbanded",
                group.displayName() + " was disbanded.", "group-deleted:" + group.id() + ":" + member));
        return group;
    }

    public void resetCharacter(UUID accountId) {
        GroupRecord group = groupFor(accountId).orElse(null);
        state.invites.entrySet().removeIf(entry -> accountId.equals(entry.getValue().invitedPlayer())
                || accountId.equals(entry.getValue().invitedBy()));
        if (group == null) {
            save();
            return;
        }
        if (group.leaderId().equals(accountId)) {
            delete(group.id(), accountId);
            return;
        }
        LinkedHashSet<UUID> members = new LinkedHashSet<>(group.members());
        members.remove(accountId);
        state.groups.put(group.id(), group.withMembers(members));
        state.playerGroups.remove(accountId);
        save();
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
                "elarion_groups",
                "group-membership",
                dedupe,
                title,
                body,
                "Groups",
                "item:minecraft:paper",
                java.util.List.of(new ElarionNotificationAction(
                        ElarionNotificationService.DISMISS, "Dismiss", true)),
                java.util.Map.of(),
                api.notifications().defaultExpiry());
    }

    public boolean eligibleForConfederationDelegate(String groupId, String realmId) {
        GroupRecord group = state.groups.get(normalizeId(groupId));
        String realm = normalizeRealm(realmId);
        if (group == null || realm.isBlank()) return false;
        return group.members().stream().allMatch(member -> realm.equals(realmOf(member)));
    }

    public void setConfederationLocked(String groupId, boolean locked) {
        String id = normalizeId(groupId);
        requireGroup(id);
        if (locked) state.confederationLockedGroups.add(id);
        else state.confederationLockedGroups.remove(id);
        save();
    }

    public boolean isConfederationLocked(String groupId) {
        return state.confederationLockedGroups.contains(normalizeId(groupId));
    }

    public boolean sendGroupMessage(ServerPlayerEntity sender, String message) {
        if (message == null || message.isBlank()) return false;
        if (api.system().restrictions().denyWithMessage(sender, PlayerRestrictionService.GROUP_CHAT)) return false;
        GroupRecord group = groupFor(sender.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("You are not in a group."));
        Text output = Text.literal("[GC:" + group.tag() + "] ").formatted(Formatting.DARK_AQUA)
                .append(api.identities().resolve(sender).chatName())
                .append(Text.literal(" \u00bb " + message).formatted(Formatting.GRAY));
        for (ServerPlayerEntity recipient : sender.getServer().getPlayerManager().getPlayerList()) {
            if (group.members().contains(recipient.getUuid())) {
                recipient.sendMessage(output, false);
            } else if (api.chat().isChatSpy(recipient)) {
                recipient.sendMessage(Text.literal("[Spy:GC:" + group.tag() + "] ")
                        .append(api.identities().resolve(sender).chatName())
                        .append(Text.literal(": " + message).formatted(Formatting.GRAY)), false);
            }
        }
        api.history().record("chat", "group-message", sender.getUuid(), "group", group.id(),
                realmOf(sender.getUuid()), java.util.Map.of("channel", "group", "group", group.id()));
        return true;
    }

    private void validateConfederationLock(GroupRecord group, UUID joiningPlayer) {
        if (!state.confederationLockedGroups.contains(group.id())) return;
        String leaderRealm = realmOf(group.leaderId());
        String targetRealm = realmOf(joiningPlayer);
        if (!leaderRealm.equals(targetRealm)) {
            throw new IllegalArgumentException("This group represents its Realm in a Confederation and cannot invite cross-Realm members.");
        }
    }

    private GroupRecord requireLeaderGroup(UUID playerId) {
        GroupRecord group = groupFor(playerId).orElseThrow(() -> new IllegalArgumentException("You are not in a group."));
        if (!group.leaderId().equals(playerId)) throw new IllegalArgumentException("Only the group leader can do that.");
        return group;
    }

    private GroupRecord requireGroup(String groupId) {
        return find(groupId).orElseThrow(() -> new IllegalArgumentException("Unknown group: " + groupId));
    }

    private void validateIdentity(String id, String tag, String displayName) {
        if (!Pattern.matches(config.idPattern(), id)) throw new IllegalArgumentException("Group ID is invalid.");
        if (tag.length() < config.minTagLength() || tag.length() > config.maxTagLength()
                || !Pattern.matches(config.tagPattern(), tag)) {
            throw new IllegalArgumentException("Group tag is invalid.");
        }
        if (config.blockedTags().contains(tag)) throw new IllegalArgumentException("Group tag is blocked.");
        if (displayName.isBlank() || displayName.length() > config.maxNameLength()) {
            throw new IllegalArgumentException("Group display name is invalid.");
        }
    }

    private void rebuildPlayerIndex() {
        state.playerGroups.clear();
        for (GroupRecord group : state.groups.values()) {
            for (UUID member : group.members()) {
                state.playerGroups.putIfAbsent(member, group.id());
            }
        }
    }

    private void requireEnabled() {
        if (!config.enabled()) throw new IllegalArgumentException("Groups are disabled.");
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
