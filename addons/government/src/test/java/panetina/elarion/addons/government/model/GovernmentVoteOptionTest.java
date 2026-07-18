package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GovernmentVoteOptionTest {
    @Test
    void candidateCarriesOfficeAndCitizen() {
        UUID candidate = UUID.randomUUID();

        GovernmentVoteOption option = GovernmentVoteOption.candidate(
                "president:" + candidate,
                "Matie",
                "President candidate",
                "president",
                candidate,
                "");

        assertEquals("president", option.officeId());
        assertEquals(candidate, option.candidateId());
        assertEquals("", option.groupId());
    }
}
