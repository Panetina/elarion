package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
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
        citizen.unlockTitle("government_officer", 1L);
        citizen.setActiveTitleId("government_president");

        boolean changed = GovernmentAuthorityTitlePolicy.removeTemporaryUnlocks(citizen, "government_officer");

        assertTrue(changed);
        assertFalse(citizen.hasUnlockedTitle("government_president"));
        assertTrue(citizen.hasUnlockedTitle("government_officer"));
        assertTrue(citizen.hasUnlockedTitle("citizen"));
    }

    @Test
    void removesAllTemporaryAuthorityTitlesWhenRankIsGone() {
        CitizenRecord citizen = new CitizenRecord(UUID.randomUUID(), "Player");
        citizen.unlockTitle("citizen", 1L);
        citizen.unlockTitle("government_monarch", 1L);
        citizen.setActiveTitleId("government_monarch");

        boolean changed = GovernmentAuthorityTitlePolicy.removeTemporaryUnlocks(citizen, "");

        assertTrue(changed);
        assertFalse(citizen.hasUnlockedTitle("government_monarch"));
        assertTrue(citizen.hasUnlockedTitle("citizen"));
        assertTrue(citizen.activeTitleId().isBlank());
    }

    @Test
    void highestTitleUsesStableAuthorityPriority() {
        UUID holder = UUID.randomUUID();

        assertEquals("government_monarch", GovernmentAuthorityTitlePolicy.highestTitleId(
                Map.of("officer", Set.of(holder), "monarch", Set.of(holder)), holder));
        assertEquals("", GovernmentAuthorityTitlePolicy.highestTitleId(Map.of(), holder));
    }
}
