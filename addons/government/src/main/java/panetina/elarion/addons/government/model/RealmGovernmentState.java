package panetina.elarion.addons.government.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record RealmGovernmentState(
        String realmId,
        String activeGovernmentFormId,
        String votedDisplayName,
        String votedTag,
        Map<String, Set<UUID>> officeHolders,
        Set<String> activeLawIds,
        Set<String> pendingProposalIds,
        long nameVoteCompletedAt,
        long foundingElectionCompletedAt,
        long lastReformAt
) {
    public RealmGovernmentState {
        realmId = realmId == null ? "" : realmId;
        activeGovernmentFormId = activeGovernmentFormId == null ? "" : activeGovernmentFormId;
        votedDisplayName = votedDisplayName == null ? "" : votedDisplayName;
        votedTag = votedTag == null ? "" : votedTag;
        officeHolders = officeHolders == null ? Map.of() : copyOffices(officeHolders);
        activeLawIds = activeLawIds == null ? Set.of() : Set.copyOf(activeLawIds);
        pendingProposalIds = pendingProposalIds == null ? Set.of() : Set.copyOf(pendingProposalIds);
    }

    public static RealmGovernmentState empty(String realmId) {
        return new RealmGovernmentState(realmId, "", "", "", Map.of(), Set.of(), Set.of(), 0L, 0L, 0L);
    }

    public RealmGovernmentState withForm(String formId) {
        return new RealmGovernmentState(realmId, formId, votedDisplayName, votedTag, officeHolders, activeLawIds,
                pendingProposalIds, nameVoteCompletedAt, foundingElectionCompletedAt, System.currentTimeMillis());
    }

    public RealmGovernmentState withVotedIdentity(String displayName, String tag) {
        return new RealmGovernmentState(realmId, activeGovernmentFormId, displayName, tag, officeHolders,
                activeLawIds, pendingProposalIds, System.currentTimeMillis(), foundingElectionCompletedAt,
                lastReformAt);
    }

    public RealmGovernmentState withFoundingElectionComplete() {
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, officeHolders,
                activeLawIds, pendingProposalIds, nameVoteCompletedAt, System.currentTimeMillis(), lastReformAt);
    }

    public RealmGovernmentState withOfficeHolder(String officeId, UUID citizenId) {
        Map<String, Set<UUID>> updated = mutableOffices();
        updated.computeIfAbsent(officeId, ignored -> new LinkedHashSet<>()).add(citizenId);
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, updated,
                activeLawIds, pendingProposalIds, nameVoteCompletedAt, foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withoutOfficeHolder(String officeId, UUID citizenId) {
        Map<String, Set<UUID>> updated = mutableOffices();
        Set<UUID> holders = updated.get(officeId);
        if (holders != null) {
            holders.remove(citizenId);
            if (holders.isEmpty()) updated.remove(officeId);
        }
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, updated,
                activeLawIds, pendingProposalIds, nameVoteCompletedAt, foundingElectionCompletedAt, lastReformAt);
    }

    private Map<String, Set<UUID>> mutableOffices() {
        Map<String, Set<UUID>> updated = new LinkedHashMap<>();
        officeHolders.forEach((office, holders) -> updated.put(office, new LinkedHashSet<>(holders)));
        return updated;
    }

    private static Map<String, Set<UUID>> copyOffices(Map<String, Set<UUID>> value) {
        Map<String, Set<UUID>> copy = new LinkedHashMap<>();
        value.forEach((office, holders) -> copy.put(office, Set.copyOf(new LinkedHashSet<>(holders))));
        return Map.copyOf(copy);
    }
}
