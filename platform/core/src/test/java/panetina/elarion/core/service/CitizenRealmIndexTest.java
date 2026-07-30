package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CitizenRecord;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenRealmIndexTest {
    @Test
    void replaceAndMultiRealmLookupReturnOnlyRequestedCitizens() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        CitizenRealmIndex index = new CitizenRealmIndex();

        index.replaceAll(List.of(citizen(first, "realm1"), citizen(second, "realm2"), citizen(third, "")));

        assertEquals(Set.of(first), index.citizensIn("realm1"));
        assertEquals(Set.of(first, second), index.citizensInAny(List.of("realm1", "realm2", "realm1")));
        assertTrue(index.citizensIn("").isEmpty());
    }

    @Test
    void updateMovesCitizenAndRemovesEmptyRealmBucket() {
        UUID citizenId = UUID.randomUUID();
        CitizenRecord citizen = citizen(citizenId, "realm1");
        CitizenRealmIndex index = new CitizenRealmIndex();
        index.update(citizen);

        citizen.setRealmId("realm2");
        index.update(citizen);

        assertTrue(index.citizensIn("realm1").isEmpty());
        assertEquals(Set.of(citizenId), index.citizensIn("realm2"));

        citizen.setRealmId("");
        index.update(citizen);
        assertTrue(index.citizensIn("realm2").isEmpty());
    }

    private static CitizenRecord citizen(UUID id, String realmId) {
        CitizenRecord citizen = new CitizenRecord(id, id.toString());
        citizen.setRealmId(realmId);
        return citizen;
    }
}
