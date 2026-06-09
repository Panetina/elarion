package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
