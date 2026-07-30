package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.service.ElarionNotificationService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GovernmentNotificationPolicyTest {
    @Test
    void realmMetadataOffersForumActionBeforeDismiss() {
        var actions = GovernmentNotificationPolicy.actions(Map.of("realmId", "realm1"));

        assertEquals(2, actions.size());
        assertEquals("elarion_government:open_civic_forum", actions.getFirst().id());
        assertEquals(ElarionNotificationService.DISMISS, actions.getLast().id());
        assertEquals(1, GovernmentNotificationPolicy.actions(Map.of()).size());
        assertEquals(ElarionNotificationService.DISMISS,
                GovernmentNotificationPolicy.actions(null).getFirst().id());
    }

    @Test
    void republicLawReviewTargetsPresidentWhileOtherReviewsUseDecisionMakers() {
        UUID president = UUID.randomUUID();
        UUID reviewer = UUID.randomUUID();

        assertEquals(Set.of(president), GovernmentNotificationPolicy.initialProposalReviewers(
                "republic", "law", Set.of(president), Set.of(reviewer)));
        assertEquals(Set.of(reviewer), GovernmentNotificationPolicy.initialProposalReviewers(
                "monarchy", "audience_request", Set.of(president), Set.of(reviewer)));
    }

    @Test
    void dedupeKeysRemainRealmAndRecipientScoped() {
        UUID holder = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertEquals("realm1:founding:2",
                GovernmentNotificationPolicy.voteStageDedupe("realm1", "founding:2"));
        assertEquals("realm1:proposal:7:" + holder,
                GovernmentNotificationPolicy.authorityReviewDedupe("realm1", "proposal:7", holder));
    }
}
