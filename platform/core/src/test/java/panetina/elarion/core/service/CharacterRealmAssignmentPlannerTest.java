package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.VisibilityScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CharacterRealmAssignmentPlannerTest {
    @Test
    void countsOnlyCandidateRealms() {
        List<RealmDefinition> realms = realms();
        List<CitizenRecord> citizens = new ArrayList<>();
        citizens.add(citizen("realm1"));
        citizens.add(citizen("realm1"));
        citizens.add(citizen("realm2"));
        citizens.add(citizen("other"));

        assertEquals(2, CharacterRealmAssignmentPlanner.counts(realms, citizens).get("realm1"));
        assertEquals(1, CharacterRealmAssignmentPlanner.counts(realms, citizens).get("realm2"));
        assertEquals(0, CharacterRealmAssignmentPlanner.counts(realms, citizens).get("realm3"));
    }

    @Test
    void selectsLeastPopulatedStarterRealm() {
        List<RealmDefinition> realms = realms();
        List<CitizenRecord> citizens = new ArrayList<>();
        for (int index = 0; index < 4; index++) citizens.add(citizen("realm1"));
        for (int index = 0; index < 5; index++) citizens.add(citizen("realm2"));
        for (int index = 0; index < 3; index++) citizens.add(citizen("realm3"));

        assertEquals("realm3", CharacterRealmAssignmentPlanner
                .selectStarterRealm(realms, citizens, new Random(1L)).orElseThrow().id());
    }

    @Test
    void selectsFromIndexedPopulationCounts() {
        assertEquals("realm2", CharacterRealmAssignmentPlanner
                .selectStarterRealm(realms(), Map.of("realm1", 4, "realm2", 1, "realm3", 3), new Random(1L))
                .orElseThrow().id());
    }

    private static List<RealmDefinition> realms() {
        return List.of(
                realm("realm1", "Wilderness I"),
                realm("realm2", "Wilderness II"),
                realm("realm3", "Wilderness III"));
    }

    private static RealmDefinition realm(String id, String name) {
        return new RealmDefinition(id, name, id.toUpperCase(), "[" + id.toUpperCase() + "]",
                "gray", null, VisibilityScope.REALM, Set.of());
    }

    private static CitizenRecord citizen(String realmId) {
        CitizenRecord citizen = new CitizenRecord(UUID.randomUUID(), "Player");
        citizen.setRealmId(realmId);
        return citizen;
    }
}
