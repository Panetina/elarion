package panetina.elarion.addons.mounts.service;

import panetina.elarion.addons.mounts.entity.ElarionMountType;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class MountRealmAssignmentService {
    public static final Map<String, ElarionMountType> FIXED_ASSIGNMENTS = Map.of(
            "realm1", ElarionMountType.AIRSHIP,
            "realm2", ElarionMountType.HOT_AIR_BALLOON,
            "realm3", ElarionMountType.GHAST);

    public Optional<ElarionMountType> mountForRealm(String realmId) {
        return Optional.ofNullable(FIXED_ASSIGNMENTS.get(normalize(realmId)));
    }

    public Optional<String> realmForMount(ElarionMountType type) {
        return FIXED_ASSIGNMENTS.entrySet().stream()
                .filter(entry -> entry.getValue() == type)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public boolean isRealmExclusive(ElarionMountType type) {
        return realmForMount(type).isPresent();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
