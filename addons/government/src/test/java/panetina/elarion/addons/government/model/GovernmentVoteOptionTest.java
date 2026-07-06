package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GovernmentVoteOptionTest {
    @Test
    void delegateCandidateCarriesRepresentedGroup() {
        UUID candidate = UUID.randomUUID();

        GovernmentVoteOption option = GovernmentVoteOption.candidate(
                "delegate:" + candidate,
                "Matie",
                "Delegate for [MERC]",
                "delegate",
                candidate,
                "merc");

        assertEquals("delegate", option.officeId());
        assertEquals(candidate, option.candidateId());
        assertEquals("merc", option.groupId());
    }
}
