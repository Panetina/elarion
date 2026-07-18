package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.GovernmentProposalStatus;
import panetina.elarion.addons.government.model.RealmGovernmentState;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentProposalLifecyclePolicyTest {
    @Test
    void republicCitizenLawProposalsNoLongerStartAsPetitions() {
        assertEquals(GovernmentProposalStatus.PENDING,
                GovernmentStateService.initialProposalStatus("republic", "law"));
    }

    @Test
    void otherProposalKindsStartInAuthorityReview() {
        assertEquals(GovernmentProposalStatus.PENDING,
                GovernmentStateService.initialProposalStatus("republic", "realm_project"));
        assertEquals(GovernmentProposalStatus.PENDING,
                GovernmentStateService.initialProposalStatus("monarchy", "law"));
    }

    @Test
    void untouchedLegacyRepublicLawPetitionsDoNotMigrateToCitizenRatification() {
        RealmGovernmentState government = RealmGovernmentState.empty("realm1").withForm("republic");
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                "proposal-1", "realm1", UUID.randomUUID(), "law", "Road Tax", "Build roads.", 10L);

        assertFalse(GovernmentStateService.shouldMigrateLegacyRepublicPetition(government, proposal));
    }

    @Test
    void reviewedOrNonRepublicLegacyProposalsDoNotMigrate() {
        UUID reviewer = UUID.randomUUID();
        RealmGovernmentState republic = RealmGovernmentState.empty("realm1").withForm("republic");
        RealmGovernmentState monarchy = RealmGovernmentState.empty("realm1").withForm("monarchy");
        GovernmentProposalRecord reviewed = GovernmentProposalRecord.create(
                "proposal-1", "realm1", UUID.randomUUID(), "law", "Road Tax", "Build roads.", 10L)
                .withVote(reviewer, true);
        GovernmentProposalRecord project = GovernmentProposalRecord.create(
                "proposal-2", "realm1", UUID.randomUUID(), "realm_project", "Harbor", "Build harbor.", 10L);

        assertFalse(GovernmentStateService.shouldMigrateLegacyRepublicPetition(republic, reviewed));
        assertFalse(GovernmentStateService.shouldMigrateLegacyRepublicPetition(republic, project));
        assertFalse(GovernmentStateService.shouldMigrateLegacyRepublicPetition(monarchy, project));
    }
}
