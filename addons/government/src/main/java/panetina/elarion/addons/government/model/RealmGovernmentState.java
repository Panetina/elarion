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
        String votedColor,
        String faithDisplayName,
        String faithTag,
        Map<String, Set<UUID>> officeHolders,
        Map<UUID, String> delegateGroupIds,
        Set<String> activeLawIds,
        Set<String> pendingProposalIds,
        long nameVoteCompletedAt,
        long colorVoteCompletedAt,
        long faithVoteCompletedAt,
        long foundingElectionCompletedAt,
        long lastReformAt
) {
    public RealmGovernmentState {
        realmId = realmId == null ? "" : realmId;
        activeGovernmentFormId = activeGovernmentFormId == null ? "" : activeGovernmentFormId;
        votedDisplayName = votedDisplayName == null ? "" : votedDisplayName;
        votedTag = votedTag == null ? "" : votedTag;
        votedColor = votedColor == null ? "" : votedColor;
        faithDisplayName = faithDisplayName == null ? "" : faithDisplayName;
        faithTag = faithTag == null ? "" : faithTag;
        officeHolders = officeHolders == null ? Map.of() : copyOffices(officeHolders);
        delegateGroupIds = delegateGroupIds == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(delegateGroupIds));
        activeLawIds = activeLawIds == null ? Set.of() : Set.copyOf(activeLawIds);
        pendingProposalIds = pendingProposalIds == null ? Set.of() : Set.copyOf(pendingProposalIds);
    }

    public static RealmGovernmentState empty(String realmId) {
        return new RealmGovernmentState(realmId, "", "", "", "", "", "", Map.of(), Map.of(), Set.of(), Set.of(),
                0L, 0L, 0L, 0L, 0L);
    }

    public RealmGovernmentState withForm(String formId) {
        return new RealmGovernmentState(realmId, formId, votedDisplayName, votedTag, votedColor, faithDisplayName,
                faithTag, officeHolders, delegateGroupIds, activeLawIds, pendingProposalIds, nameVoteCompletedAt,
                colorVoteCompletedAt, faithVoteCompletedAt, foundingElectionCompletedAt, System.currentTimeMillis());
    }

    public RealmGovernmentState withVotedIdentity(String displayName, String tag) {
        return new RealmGovernmentState(realmId, activeGovernmentFormId, displayName, tag, votedColor,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, activeLawIds, pendingProposalIds,
                System.currentTimeMillis(), colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt,
                lastReformAt);
    }

    public RealmGovernmentState withVotedColor(String color) {
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, color,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, activeLawIds, pendingProposalIds,
                nameVoteCompletedAt, System.currentTimeMillis(), faithVoteCompletedAt,
                foundingElectionCompletedAt,
                lastReformAt);
    }

    public RealmGovernmentState withFaithIdentity(String displayName, String tag) {
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                displayName, tag, officeHolders, delegateGroupIds, activeLawIds, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, System.currentTimeMillis(),
                foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withFoundingElectionComplete() {
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, activeLawIds, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                System.currentTimeMillis(), lastReformAt);
    }

    public RealmGovernmentState withFoundingElectionReopened() {
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, activeLawIds, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                0L, lastReformAt);
    }

    public RealmGovernmentState withOfficeHolder(String officeId, UUID citizenId) {
        Map<String, Set<UUID>> updated = mutableOffices();
        updated.computeIfAbsent(officeId, ignored -> new LinkedHashSet<>()).add(citizenId);
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, updated, delegateGroupIds, activeLawIds, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withDelegateGroup(UUID citizenId, String groupId) {
        Map<UUID, String> updated = new LinkedHashMap<>(delegateGroupIds);
        if (citizenId != null && groupId != null && !groupId.isBlank()) {
            updated.put(citizenId, groupId);
        }
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, officeHolders, updated, activeLawIds, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withPendingProposal(String proposalId) {
        Set<String> proposals = new LinkedHashSet<>(pendingProposalIds);
        if (proposalId != null && !proposalId.isBlank()) proposals.add(proposalId);
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, activeLawIds, proposals,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withoutPendingProposal(String proposalId) {
        Set<String> proposals = new LinkedHashSet<>(pendingProposalIds);
        proposals.remove(proposalId);
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, activeLawIds, proposals,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withActiveLaw(String lawId) {
        Set<String> laws = new LinkedHashSet<>(activeLawIds);
        if (lawId != null && !lawId.isBlank()) laws.add(lawId);
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, laws, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withoutActiveLaw(String lawId) {
        Set<String> laws = new LinkedHashSet<>(activeLawIds);
        laws.remove(lawId);
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, officeHolders, delegateGroupIds, laws, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt, lastReformAt);
    }

    public RealmGovernmentState withoutOfficeHolder(String officeId, UUID citizenId) {
        Map<String, Set<UUID>> updated = mutableOffices();
        Set<UUID> holders = updated.get(officeId);
        if (holders != null) {
            holders.remove(citizenId);
            if (holders.isEmpty()) updated.remove(officeId);
        }
        Map<UUID, String> delegateGroups = new LinkedHashMap<>(delegateGroupIds);
        if ("delegate".equals(officeId)) {
            delegateGroups.remove(citizenId);
        }
        return new RealmGovernmentState(realmId, activeGovernmentFormId, votedDisplayName, votedTag, votedColor,
                faithDisplayName, faithTag, updated, delegateGroups, activeLawIds, pendingProposalIds,
                nameVoteCompletedAt, colorVoteCompletedAt, faithVoteCompletedAt,
                foundingElectionCompletedAt, lastReformAt);
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
