package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentFoundingPhase;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.RealmGovernmentState;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class GovernmentFoundingPhasePolicy {
    private GovernmentFoundingPhasePolicy() {
    }

    static GovernmentFoundingPhase phase(
            GovernmentFormDefinition form,
            Optional<GovernmentVoteState> existingVote,
            UUID playerId,
            boolean activeCitizen,
            boolean electionUnlocked,
            String lockMessage,
            long now
    ) {
        String officeId = electionOffices(form).stream().findFirst().orElse("");
        String title = officeLabel(form, officeId) + " Election";
        GovernmentVoteState vote = existingVote.orElse(null);
        boolean nominationsOpen = vote == null || !vote.runoff && !vote.proposalEnded(now);
        boolean votingOpen = vote != null && (vote.runoff || vote.proposalEnded(now));
        boolean alreadyNominated = vote != null && playerId != null && vote.options.values().stream()
                .anyMatch(option -> playerId.equals(option.proposedBy));
        String reason = officeId.isBlank()
                ? "No founding office is currently open."
                : "Nominate yourself for " + officeLabel(form, officeId) + ".";
        boolean canNominate = electionUnlocked && activeCitizen && nominationsOpen && !alreadyNominated;
        if (!electionUnlocked) {
            reason = lockMessage == null || lockMessage.isBlank() ? "Founding election is locked." : lockMessage;
            canNominate = false;
        } else if (!activeCitizen) {
            reason = "Only active Embers of this Realm may enter the election.";
            canNominate = false;
        } else if (!nominationsOpen) {
            reason = "Nominations are closed. Voting is now open.";
            canNominate = false;
        } else if (alreadyNominated) {
            reason = "You are already nominated for " + officeLabel(form, officeId) + ".";
            canNominate = false;
        }
        String phaseLabel = title + (votingOpen ? " voting" : " nominations");
        return new GovernmentFoundingPhase(officeId, title, phaseLabel,
                nominationsOpen, votingOpen, canNominate, reason);
    }

    static List<String> electionOffices(GovernmentFormDefinition form) {
        return switch (form.id()) {
            case "monarchy" -> List.of("monarch");
            case "republic" -> List.of("president");
            default -> form.authorityOffices().stream().filter(office -> !"officer".equals(office)).toList();
        };
    }

    static boolean electionComplete(GovernmentFormDefinition form, RealmGovernmentState government) {
        return switch (form.id()) {
            case "monarchy" -> government.officeHolders().containsKey("monarch");
            case "republic" -> government.officeHolders().containsKey("president");
            default -> !electionOffices(form).isEmpty()
                    && electionOffices(form).stream()
                    .allMatch(office -> government.officeHolders().containsKey(office));
        };
    }

    static String officeLabel(GovernmentFormDefinition form, String officeId) {
        if (officeId == null || officeId.isBlank()) return "Leadership";
        return form.offices().stream()
                .filter(office -> office.id().equals(officeId))
                .findFirst()
                .map(office -> office.displayName().isBlank() ? office.id() : office.displayName())
                .orElse(officeId);
    }

    static String primaryOffice(String formId) {
        return switch (formId == null ? "" : formId) {
            case "monarchy" -> "monarch";
            case "republic" -> "president";
            default -> "";
        };
    }

    static boolean shouldReopenLeadershipElection(
            RealmGovernmentState previous,
            RealmGovernmentState updated,
            String removedOfficeId
    ) {
        if (previous == null || updated == null || previous.foundingElectionCompletedAt() <= 0L) return false;
        String primary = primaryOffice(previous.activeGovernmentFormId());
        return !primary.isBlank()
                && primary.equals(removedOfficeId)
                && updated.officeHolders().getOrDefault(primary, Set.of()).isEmpty();
    }

    static boolean hasCompletedLeadershipVacancy(RealmGovernmentState government) {
        if (government == null || government.foundingElectionCompletedAt() <= 0L) return false;
        String primary = primaryOffice(government.activeGovernmentFormId());
        return !primary.isBlank()
                && government.officeHolders().getOrDefault(primary, Set.of()).isEmpty();
    }
}
