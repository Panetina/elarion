package panetina.elarion.core.service;

import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class CharacterRealmAssignmentPlanner {
    private CharacterRealmAssignmentPlanner() {
    }

    public static Optional<RealmDefinition> selectStarterRealm(
            List<RealmDefinition> candidates,
            Collection<CitizenRecord> citizens,
            RandomGenerator random
    ) {
        return selectStarterRealm(candidates, counts(candidates, citizens), random);
    }

    public static Optional<RealmDefinition> selectStarterRealm(
            List<RealmDefinition> candidates,
            Map<String, Integer> populationByRealm,
            RandomGenerator random
    ) {
        if (candidates == null || candidates.isEmpty()) return Optional.empty();
        RandomGenerator rng = random == null ? RandomGenerator.getDefault() : random;
        Map<String, Integer> counts = populationByRealm == null ? Map.of() : populationByRealm;
        int minimum = candidates.stream()
                .mapToInt(realm -> counts.getOrDefault(realm.id(), 0))
                .min()
                .orElse(0);
        List<RealmDefinition> leastPopulated = candidates.stream()
                .filter(realm -> counts.getOrDefault(realm.id(), 0) == minimum)
                .toList();
        if (leastPopulated.isEmpty()) return Optional.empty();
        return Optional.of(leastPopulated.get(rng.nextInt(leastPopulated.size())));
    }

    public static Map<String, Integer> counts(List<RealmDefinition> candidates, Collection<CitizenRecord> citizens) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (candidates != null) {
            for (RealmDefinition realm : candidates) counts.put(realm.id(), 0);
        }
        if (citizens != null) {
            for (CitizenRecord citizen : citizens) {
                counts.computeIfPresent(citizen.realmId(), (ignored, count) -> count + 1);
            }
        }
        return counts;
    }
}
