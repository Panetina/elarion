package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.RealmGovernmentState;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class GovernmentProposalDecisionPolicy {
    private GovernmentProposalDecisionPolicy() {
    }

    static GovernmentProposalDecision proposalDecision(
            RealmGovernmentState government,
            GovernmentFormDefinition form,
            GovernmentProposalRecord proposal,
            Set<UUID> fallbackAuthorityHolders
    ) {
        if (government.activeGovernmentFormId().isBlank() || form == null) {
            return GovernmentProposalDecision.WAITING;
        }
        Set<UUID> approvers = proposalDecisionMakers(government, form, fallbackAuthorityHolders);
        if (approvers.isEmpty()) return GovernmentProposalDecision.WAITING;
        long approvals = proposal.reviewVotes().entrySet().stream()
                .filter(entry -> approvers.contains(entry.getKey()))
                .filter(Map.Entry::getValue)
                .count();
        long rejections = proposal.reviewVotes().entrySet().stream()
                .filter(entry -> approvers.contains(entry.getKey()))
                .filter(entry -> !entry.getValue())
                .count();
        if ("monarchy".equals(form.id())) {
            Set<UUID> monarchs = government.officeHolders().getOrDefault("monarch", Set.of());
            if (monarchs.stream().anyMatch(holder -> Boolean.TRUE.equals(proposal.reviewVotes().get(holder)))) {
                return GovernmentProposalDecision.APPROVED;
            }
            if (monarchs.stream().anyMatch(holder -> Boolean.FALSE.equals(proposal.reviewVotes().get(holder)))) {
                return GovernmentProposalDecision.REJECTED;
            }
            return GovernmentProposalDecision.WAITING;
        }
        int threshold = approvers.size() / 2 + 1;
        if (approvals >= threshold) return GovernmentProposalDecision.APPROVED;
        if (rejections >= threshold) return GovernmentProposalDecision.REJECTED;
        return GovernmentProposalDecision.WAITING;
    }

    static GovernmentProposalDecision finalTextDecision(
            RealmGovernmentState government,
            GovernmentProposalRecord proposal,
            Set<UUID> fallbackAuthorityHolders
    ) {
        Set<UUID> approvers = finalTextDecisionMakers(government, fallbackAuthorityHolders);
        if (approvers.isEmpty()) return GovernmentProposalDecision.WAITING;
        long approvals = proposal.reviewVotes().entrySet().stream()
                .filter(entry -> approvers.contains(entry.getKey()))
                .filter(Map.Entry::getValue)
                .count();
        long rejections = proposal.reviewVotes().entrySet().stream()
                .filter(entry -> approvers.contains(entry.getKey()))
                .filter(entry -> !entry.getValue())
                .count();
        int threshold = approvers.size() / 2 + 1;
        if (approvals >= threshold) return GovernmentProposalDecision.APPROVED;
        if (rejections >= threshold) return GovernmentProposalDecision.REJECTED;
        return GovernmentProposalDecision.WAITING;
    }

    static Set<UUID> proposalDecisionMakers(
            RealmGovernmentState government,
            GovernmentFormDefinition form,
            Set<UUID> fallbackAuthorityHolders
    ) {
        LinkedHashSet<UUID> holders = new LinkedHashSet<>();
        switch (form.id()) {
            case "monarchy" -> holders.addAll(government.officeHolders().getOrDefault("monarch", Set.of()));
            case "republic" -> holders.addAll(government.officeHolders().getOrDefault("president", Set.of()));
            default -> holders.addAll(fallbackAuthorityHolders);
        }
        return Set.copyOf(holders);
    }

    static Set<UUID> finalTextDecisionMakers(
            RealmGovernmentState government,
            Set<UUID> fallbackAuthorityHolders
    ) {
        LinkedHashSet<UUID> holders = new LinkedHashSet<>();
        holders.addAll(fallbackAuthorityHolders);
        return Set.copyOf(holders);
    }
}
