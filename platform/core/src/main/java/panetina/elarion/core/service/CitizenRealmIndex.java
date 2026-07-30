package panetina.elarion.core.service;

import panetina.elarion.core.model.CitizenRecord;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class CitizenRealmIndex {
    private final Map<UUID, String> realmByCitizen = new LinkedHashMap<>();
    private final Map<String, LinkedHashSet<UUID>> citizensByRealm = new LinkedHashMap<>();

    void replaceAll(Collection<CitizenRecord> citizens) {
        clear();
        if (citizens == null) return;
        citizens.forEach(this::update);
    }

    void update(CitizenRecord citizen) {
        if (citizen == null || citizen.uuid() == null) return;
        UUID citizenId = citizen.uuid();
        String nextRealm = clean(citizen.realmId());
        String previousRealm = realmByCitizen.remove(citizenId);
        if (previousRealm != null) remove(previousRealm, citizenId);
        if (nextRealm.isBlank()) return;
        realmByCitizen.put(citizenId, nextRealm);
        citizensByRealm.computeIfAbsent(nextRealm, ignored -> new LinkedHashSet<>()).add(citizenId);
    }

    Set<UUID> citizensIn(String realmId) {
        Set<UUID> citizens = citizensByRealm.get(clean(realmId));
        return citizens == null ? Set.of() : Set.copyOf(citizens);
    }

    int citizenCount(String realmId) {
        Set<UUID> citizens = citizensByRealm.get(clean(realmId));
        return citizens == null ? 0 : citizens.size();
    }

    Set<UUID> citizensInAny(Collection<String> realmIds) {
        if (realmIds == null || realmIds.isEmpty()) return Set.of();
        LinkedHashSet<UUID> citizens = new LinkedHashSet<>();
        realmIds.forEach(realmId -> {
            Set<UUID> indexed = citizensByRealm.get(clean(realmId));
            if (indexed != null) citizens.addAll(indexed);
        });
        return Set.copyOf(citizens);
    }

    void clear() {
        realmByCitizen.clear();
        citizensByRealm.clear();
    }

    private void remove(String realmId, UUID citizenId) {
        LinkedHashSet<UUID> citizens = citizensByRealm.get(realmId);
        if (citizens == null) return;
        citizens.remove(citizenId);
        if (citizens.isEmpty()) citizensByRealm.remove(realmId);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
