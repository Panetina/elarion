package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.core.model.CitizenRecord;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentAuthorityTitleTest {
    @Test
    void removesTemporaryAuthorityTitlesExceptCurrentOfficeTitle() {
        CitizenRecord citizen = new CitizenRecord(UUID.randomUUID(), "Player");
        citizen.unlockTitle("citizen", 1L);
        citizen.unlockTitle("government_president", 1L);
        citizen.unlockTitle("government_councilor", 1L);
        citizen.setActiveTitleId("government_president");

        boolean changed = GovernmentStateService.removeAuthorityTitleUnlocks(citizen, "government_councilor");

        assertTrue(changed);
        assertFalse(citizen.hasUnlockedTitle("government_president"));
        assertTrue(citizen.hasUnlockedTitle("government_councilor"));
        assertTrue(citizen.hasUnlockedTitle("citizen"));
    }

    @Test
    void removesAllTemporaryAuthorityTitlesWhenRankIsGone() {
        CitizenRecord citizen = new CitizenRecord(UUID.randomUUID(), "Player");
        citizen.unlockTitle("citizen", 1L);
        citizen.unlockTitle("government_monarch", 1L);
        citizen.setActiveTitleId("government_monarch");

        boolean changed = GovernmentStateService.removeAuthorityTitleUnlocks(citizen, "");

        assertTrue(changed);
        assertFalse(citizen.hasUnlockedTitle("government_monarch"));
        assertTrue(citizen.hasUnlockedTitle("citizen"));
        assertTrue(citizen.activeTitleId().isBlank());
    }

    @Test
    void republicPresidentCannotAlsoHoldCouncilSeat() {
        UUID president = UUID.randomUUID();
        RealmGovernmentState state = new RealmGovernmentState(
                "realm1", "republic", "", "", "", "", "",
                Map.of("president", Set.of(president)), Map.of(), Set.of(), Set.of(),
                0L, 0L, 0L, 0L, 0L);

        assertEquals("The President cannot also hold a Councilor seat.",
                GovernmentStateService.republicOfficeConflict(
                        "republic", "council_member", state, president));
    }

    @Test
    void republicCouncilorCannotAlsoBecomePresident() {
        UUID councilor = UUID.randomUUID();
        RealmGovernmentState state = new RealmGovernmentState(
                "realm1", "republic", "", "", "", "", "",
                Map.of("council_member", Set.of(councilor)), Map.of(), Set.of(), Set.of(),
                0L, 0L, 0L, 0L, 0L);

        assertEquals("A Councilor cannot also hold the President office.",
                GovernmentStateService.republicOfficeConflict(
                        "republic", "president", state, councilor));
    }
}
