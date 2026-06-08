package panetina.elarion.core.service;

import panetina.elarion.core.model.CitizenRecord;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class AbilityService {
    private final TitleService titles;
    private final Set<String> registeredAbilities = Collections.synchronizedSet(new LinkedHashSet<>());

    public AbilityService(TitleService titles) {
        this.titles = titles;
    }

    public String register(String abilityId) {
        String normalized = normalize(abilityId);
        registeredAbilities.add(normalized);
        return normalized;
    }

    public Set<String> registeredAbilities() {
        synchronized (registeredAbilities) {
            return Set.copyOf(registeredAbilities);
        }
    }

    public boolean has(CitizenRecord citizen, String abilityId) {
        String normalized = normalize(abilityId);
        if (citizen.grantedAbilities().contains(normalized)) return true;
        return titles.forCitizen(citizen)
                .map(title -> title.abilities().contains(normalized))
                .orElse(false);
    }

    public void grant(CitizenRecord citizen, String abilityId) {
        citizen.grantedAbilities().add(register(abilityId));
    }

    public void revoke(CitizenRecord citizen, String abilityId) {
        citizen.grantedAbilities().remove(normalize(abilityId));
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank() || !value.contains(".")) {
            throw new IllegalArgumentException("Ability IDs must be namespaced, for example elarion.portal.foreign_access");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
