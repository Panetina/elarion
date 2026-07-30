package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentVoteOption;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GovernmentVoteResolutionPolicyTest {
    @Test
    void totalsIgnoreInvalidSelectionsAndCountValidBallots() {
        GovernmentVoteState vote = new GovernmentVoteState("realm1", GovernmentVoteType.REALM_COLOR);
        vote.options.put("red", GovernmentVoteOption.realmColor("red", "Red", ""));
        vote.options.put("dark_green", GovernmentVoteOption.realmColor("dark_green", "Dark Green", ""));
        vote.options.put("dark_red", GovernmentVoteOption.realmColor("dark_red", "Dark Red", ""));
        vote.ballots.put("a", List.of("dark_green"));
        vote.ballots.put("b", List.of("dark_green"));
        vote.ballots.put("c", List.of("dark_green"));
        vote.ballots.put("d", List.of("dark_red"));
        vote.ballots.put("e", List.of("red"));
        vote.ballots.put("invalid", List.of("blue"));

        assertEquals(Map.of("dark_green", 3L, "dark_red", 1L, "red", 1L),
                GovernmentVoteResolutionPolicy.totals(vote));
        assertEquals(List.of("dark_green"),
                GovernmentVoteResolutionPolicy.topOptions(GovernmentVoteResolutionPolicy.totals(vote)));
    }

    @Test
    void topOptionsReturnsOnlyLeadingTiedOptions() {
        Map<String, Long> totals = Map.of(
                "a", 4L,
                "b", 4L,
                "c", 4L,
                "d", 3L,
                "e", 3L);

        assertEquals(List.of("a", "b", "c"), GovernmentVoteResolutionPolicy.topOptions(totals));
    }
}
