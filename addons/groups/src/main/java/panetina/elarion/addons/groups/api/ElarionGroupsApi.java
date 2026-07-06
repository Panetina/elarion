package panetina.elarion.addons.groups.api;

import panetina.elarion.addons.groups.model.GroupRecord;
import panetina.elarion.addons.groups.service.GroupService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class ElarionGroupsApi {
    private static ElarionGroupsApi instance;
    private final GroupService groups;

    public ElarionGroupsApi(GroupService groups) {
        if (instance != null) throw new IllegalStateException("ElarionGroupsApi is already initialized");
        this.groups = groups;
        instance = this;
    }

    public static ElarionGroupsApi get() {
        if (instance == null) throw new IllegalStateException("Elarion Groups has not initialized yet");
        return instance;
    }

    public GroupService groups() {
        return groups;
    }

    public Collection<GroupRecord> all() {
        return groups.groups();
    }

    public Optional<GroupRecord> find(String id) {
        return groups.find(id);
    }

    public Optional<GroupRecord> groupFor(UUID playerId) {
        return groups.groupFor(playerId);
    }

    public boolean eligibleForConfederationDelegate(String groupId, String realmId) {
        return groups.eligibleForConfederationDelegate(groupId, realmId);
    }

    public boolean isConfederationLocked(String groupId) {
        return groups.isConfederationLocked(groupId);
    }

    public void setConfederationLocked(String groupId, boolean locked) {
        groups.setConfederationLocked(groupId, locked);
    }
}
