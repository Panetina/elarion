package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentVoteState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class GovernmentVoteResolutionPolicy {
    private GovernmentVoteResolutionPolicy() {
    }

    static Map<String, Long> totals(GovernmentVoteState vote) {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (List<String> selections : vote.ballots.values()) {
            for (String selected : selections) {
                if (vote.options.containsKey(selected)) {
                    totals.merge(selected, 1L, Long::sum);
                }
            }
        }
        return totals;
    }

    static List<String> topOptions(Map<String, Long> totals) {
        long best = totals.values().stream().mapToLong(Long::longValue).max().orElse(0L);
        return totals.entrySet().stream()
                .filter(entry -> entry.getValue() == best)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }
}
