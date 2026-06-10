package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenRecordTest {
    @Test
    void unassignedRealmIsAlwaysBlank() {
        CitizenRecord citizen = new CitizenRecord(UUID.randomUUID(), "Player");

        assertEquals("", citizen.realmId());

        citizen.setRealmId("oak");
        assertEquals("oak", citizen.realmId());

        citizen.setRealmId(null);
        assertEquals("", citizen.realmId());
    }

    @Test
    void activeTitleIsAlsoUnlockedForMigrationCompatibility() {
        CitizenRecord citizen = new CitizenRecord(UUID.randomUUID(), "Player");

        citizen.setTitleId("Diplomat");

        assertEquals("diplomat", citizen.activeTitleId());
        assertTrue(citizen.hasUnlockedTitle("diplomat"));
        assertTrue(citizen.titleUnlockTimes().containsKey("diplomat"));
    }

    @Test
    void revokingActiveTitleClearsActiveSelection() {
        CitizenRecord citizen = new CitizenRecord(UUID.randomUUID(), "Player");
        citizen.setActiveTitleId("citizen");

        citizen.revokeTitle("citizen");

        assertEquals("", citizen.activeTitleId());
        assertTrue(citizen.unlockedTitleIds().isEmpty());
    }
}
