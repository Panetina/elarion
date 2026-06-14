package panetina.elarion.addons.groups.storage;

import panetina.elarion.addons.groups.model.GroupInvite;
import panetina.elarion.addons.groups.model.GroupRecord;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GroupState {
    public Map<String, GroupRecord> groups = new LinkedHashMap<>();
    public Map<UUID, String> playerGroups = new LinkedHashMap<>();
    public Map<String, GroupInvite> invites = new LinkedHashMap<>();
    public Set<String> confederationLockedGroups = new LinkedHashSet<>();

    public GroupState copy() {
        GroupState copy = new GroupState();
        copy.groups = new LinkedHashMap<>(groups);
        copy.playerGroups = new LinkedHashMap<>(playerGroups);
        copy.invites = new LinkedHashMap<>(invites);
        copy.confederationLockedGroups = new LinkedHashSet<>(confederationLockedGroups);
        return copy;
    }
}
