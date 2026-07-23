package panetina.elarion.addons.groups.service;

import panetina.elarion.addons.groups.model.GroupRecord;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;
import panetina.elarion.core.model.CitizenRecord;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Publishes the current group lore owner without making website roles canonical. */
public final class GroupWebProjectionPublisher {
    public static final String GROUP_LORE_AUTHORITY = "authority.group.lore";
    public static final String GROUP_MEMBERSHIP = "group.membership";

    private final ElarionApi api;

    public GroupWebProjectionPublisher(ElarionApi api) {
        this.api = api;
    }

    public void publishAll(Collection<GroupRecord> groups) {
        groups.forEach(this::publishActive);
    }

    public void publishActive(GroupRecord group) {
        publishAuthority(group, true);
        if (group != null) group.members().forEach(member -> publishMembership(group, member, true));
    }

    public void publishInactive(GroupRecord group) {
        publishAuthority(group, false);
        if (group != null) group.members().forEach(member -> publishMembership(group, member, false));
    }

    public void publishNoMembership(java.util.UUID memberId) {
        if (memberId == null) return;
        String realmId = api.citizens().find(memberId).map(CitizenRecord::realmId).orElse("");
        api.system().webProjections().publishState(GROUP_MEMBERSHIP, memberId.toString(), realmId,
                Visibility.WHITELISTED, Map.of("active", "false"));
    }

    private void publishAuthority(GroupRecord group, boolean active) {
        if (group == null || group.leaderId() == null) return;
        String realmId = api.citizens().find(group.leaderId()).map(CitizenRecord::realmId).orElse("");
        api.system().webProjections().publishState(
                GROUP_LORE_AUTHORITY,
                group.id(),
                realmId,
                Visibility.AUTHENTICATED,
                authorityPayload(group, active));
    }

    private void publishMembership(GroupRecord group, java.util.UUID memberId, boolean active) {
        if (group == null || memberId == null) return;
        String realmId = api.citizens().find(memberId).map(CitizenRecord::realmId).orElse("");
        api.system().webProjections().publishState(GROUP_MEMBERSHIP, memberId.toString(), realmId,
                Visibility.WHITELISTED, membershipPayload(group, memberId, active));
    }

    static Map<String, String> authorityPayload(GroupRecord group, boolean active) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("active", Boolean.toString(active));
        payload.put("resourceId", group.id());
        payload.put("ownerUuid", group.leaderId().toString());
        payload.put("authorityRole", "leader");
        payload.put("displayName", group.displayName());
        return Map.copyOf(payload);
    }

    static Map<String, String> membershipPayload(GroupRecord group, java.util.UUID memberId, boolean active) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("active", Boolean.toString(active));
        if (active && group != null && memberId != null) {
            payload.put("groupId", group.id());
            payload.put("displayName", group.displayName());
            payload.put("tag", group.tag());
            payload.put("memberRole", group.leaderId().equals(memberId) ? "Leader" : "Member");
        }
        return Map.copyOf(payload);
    }
}
