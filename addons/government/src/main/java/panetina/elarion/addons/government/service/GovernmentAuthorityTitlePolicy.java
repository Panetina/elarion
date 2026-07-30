package panetina.elarion.addons.government.service;

import panetina.elarion.core.model.CitizenRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class GovernmentAuthorityTitlePolicy {
    private static final Map<String, String> TITLE_BY_OFFICE = Map.of(
            "monarch", "government_monarch",
            "heir", "government_heir",
            "president", "government_president",
            "officer", "government_officer");
    private static final List<String> OFFICE_PRIORITY = List.of(
            "monarch", "president", "heir", "officer");
    private static final Set<String> TEMPORARY_TITLES = Set.copyOf(TITLE_BY_OFFICE.values());

    private GovernmentAuthorityTitlePolicy() {
    }

    static String highestTitleId(Map<String, Set<UUID>> offices, UUID citizenId) {
        if (offices == null || citizenId == null) return "";
        for (String officeId : OFFICE_PRIORITY) {
            if (offices.getOrDefault(officeId, Set.of()).contains(citizenId)) {
                return TITLE_BY_OFFICE.getOrDefault(officeId, "");
            }
        }
        return offices.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().contains(citizenId))
                .map(entry -> TITLE_BY_OFFICE.getOrDefault(entry.getKey(), ""))
                .filter(titleId -> !titleId.isBlank())
                .findFirst()
                .orElse("");
    }

    static boolean removeTemporaryUnlocks(CitizenRecord citizen, String keepTitleId) {
        if (citizen == null) return false;
        String keep = keepTitleId == null ? "" : keepTitleId.trim().toLowerCase(java.util.Locale.ROOT);
        boolean changed = false;
        for (String titleId : List.copyOf(citizen.unlockedTitleIds())) {
            if (!isTemporaryTitle(titleId) || titleId.equals(keep)) continue;
            citizen.revokeTitle(titleId);
            changed = true;
        }
        return changed;
    }

    static boolean isTemporaryTitle(String titleId) {
        return TEMPORARY_TITLES.contains(titleId);
    }
}
