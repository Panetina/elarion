package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentFoundingPhase;
import panetina.elarion.addons.government.model.GovernmentOfficeDefinition;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;
import panetina.elarion.addons.government.model.RealmGovernmentState;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentFoundingPhasePolicyTest {
    @Test
    void votingPhaseDisablesFurtherNominations() {
        UUID citizen = UUID.fromString("00000000-0000-0000-0000-000000000002");
        GovernmentVoteState vote = new GovernmentVoteState("realm1", GovernmentVoteType.FOUNDING_ELECTION);
        vote.proposalStartedAt = 1L;
        vote.proposalEndsAt = 5L;
        vote.startedAt = 5L;
        vote.endsAt = 100L;

        GovernmentFoundingPhase phase = GovernmentFoundingPhasePolicy.phase(
                republic(), Optional.of(vote), citizen,
                true, true, "", 10L);

        assertEquals("president", phase.officeId());
        assertTrue(phase.votingOpen());
        assertFalse(phase.canNominate());
        assertEquals("Nominations are closed. Voting is now open.", phase.nominationReason());
    }

    @Test
    void lastPrimaryOfficeHolderVacancyReopensLeadershipElection() {
        UUID monarch = UUID.fromString("00000000-0000-0000-0000-000000000001");
        RealmGovernmentState previous = RealmGovernmentState.empty("realm1")
                .withForm("monarchy")
                .withOfficeHolder("monarch", monarch)
                .withFoundingElectionComplete();
        RealmGovernmentState updated = previous.withoutOfficeHolder("monarch", monarch);

        assertTrue(GovernmentFoundingPhasePolicy.shouldReopenLeadershipElection(previous, updated, "monarch"));
        assertFalse(GovernmentFoundingPhasePolicy.shouldReopenLeadershipElection(previous, updated, "heir"));
    }

    @Test
    void republicPresidentVacancyReopensLeadershipElection() {
        UUID president = UUID.fromString("00000000-0000-0000-0000-000000000001");
        RealmGovernmentState previous = RealmGovernmentState.empty("realm1")
                .withForm("republic")
                .withOfficeHolder("president", president)
                .withFoundingElectionComplete();
        RealmGovernmentState updated = previous.withoutOfficeHolder("president", president);

        assertTrue(GovernmentFoundingPhasePolicy.shouldReopenLeadershipElection(previous, updated, "president"));
    }

    @Test
    void completedPersistedMonarchyWithNoMonarchRequiresElectionRepair() {
        RealmGovernmentState vacant = RealmGovernmentState.empty("realm1")
                .withForm("monarchy")
                .withFoundingElectionComplete();

        assertTrue(GovernmentFoundingPhasePolicy.hasCompletedLeadershipVacancy(vacant));
        assertFalse(GovernmentFoundingPhasePolicy.hasCompletedLeadershipVacancy(
                vacant.withOfficeHolder("monarch", UUID.fromString("00000000-0000-0000-0000-000000000001"))));
    }

    @Test
    void republicElectionRequiresAndCompletesWithPresident() {
        UUID president = UUID.fromString("00000000-0000-0000-0000-000000000001");
        RealmGovernmentState vacant = RealmGovernmentState.empty("realm1").withForm("republic");

        assertEquals(List.of("president"), GovernmentFoundingPhasePolicy.electionOffices(republic()));
        assertFalse(GovernmentFoundingPhasePolicy.electionComplete(republic(), vacant));
        assertTrue(GovernmentFoundingPhasePolicy.electionComplete(
                republic(), vacant.withOfficeHolder("president", president)));
    }

    private static GovernmentFormDefinition republic() {
        return new GovernmentFormDefinition("republic", "Republic", "", true, "%realm%", List.of(),
                List.of(
                        new GovernmentOfficeDefinition("president", "President", "", 1),
                        new GovernmentOfficeDefinition("officer", "Officer", "", 3)),
                Map.of(), Map.of());
    }
}
