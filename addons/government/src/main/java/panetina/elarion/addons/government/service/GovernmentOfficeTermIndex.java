package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class GovernmentOfficeTermIndex {
    private static final int MAX_QUERY_RESULTS = 32;

    private GovernmentOfficeTermIndex() {
    }

    static List<GovernmentOfficeTermRecord> query(
            Map<UUID, List<GovernmentOfficeTermRecord>> index,
            UUID holderId,
            int limit
    ) {
        if (holderId == null || index == null) {
            return List.of();
        }
        int boundedLimit = Math.max(1, Math.min(MAX_QUERY_RESULTS, limit));
        return index.getOrDefault(holderId, List.of()).stream()
                .sorted(Comparator.comparingLong(GovernmentOfficeTermRecord::chosenAt).reversed())
                .limit(boundedLimit)
                .toList();
    }
}
