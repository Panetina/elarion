package panetina.elarion.addons.government.storage;

import panetina.elarion.addons.government.model.GovernmentVoteOption;
import panetina.elarion.addons.government.model.GovernmentVoteState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Normalizes recoverable supported-schema state before runtime services bind. */
final class GovernmentStateNormalizer {
    private GovernmentStateNormalizer() {
    }

    static GovernmentState normalize(GovernmentState state) {
        state.realms = validEntries(state.realms);
        state.proposals = validEntries(state.proposals);
        state.laws = validEntries(state.laws);
        state.officeTerms = validEntries(state.officeTerms);
        state.authorityTitleRestores = validEntries(state.authorityTitleRestores);
        state.votes = normalizedVotes(state.votes);
        return state;
    }

    private static Map<String, GovernmentVoteState> normalizedVotes(Map<String, GovernmentVoteState> source) {
        Map<String, GovernmentVoteState> normalized = new LinkedHashMap<>();
        if (source == null) return normalized;
        source.forEach((key, vote) -> {
            if (!validKey(key) || vote == null || vote.type == null || clean(vote.realmId).isBlank()) return;
            normalizeVote(vote);
            normalized.put(key, vote);
        });
        return normalized;
    }

    private static void normalizeVote(GovernmentVoteState vote) {
        vote.realmId = clean(vote.realmId);
        vote.round = Math.max(1, vote.round);
        vote.options = normalizedOptions(vote.options);
        vote.ballots = normalizedBallots(vote.ballots);
        vote.winnerIds = normalizedIds(vote.winnerIds);
        vote.resultTotals = normalizedTotals(vote.resultTotals);
    }

    private static Map<String, GovernmentVoteOption> normalizedOptions(Map<String, GovernmentVoteOption> source) {
        Map<String, GovernmentVoteOption> normalized = new LinkedHashMap<>();
        if (source == null) return normalized;
        source.forEach((key, option) -> {
            if (!validKey(key) || option == null) return;
            option.id = clean(option.id);
            if (option.id.isBlank()) option.id = key;
            option.title = clean(option.title);
            option.body = clean(option.body);
            option.tag = clean(option.tag);
            option.formId = clean(option.formId);
            option.officeId = clean(option.officeId);
            option.guildId = clean(option.guildId);
            option.createdAt = Math.max(0L, option.createdAt);
            normalized.put(key, option);
        });
        return normalized;
    }

    private static Map<String, List<String>> normalizedBallots(Map<String, List<String>> source) {
        Map<String, List<String>> normalized = new LinkedHashMap<>();
        if (source == null) return normalized;
        source.forEach((voter, selections) -> {
            if (!validKey(voter) || selections == null) return;
            List<String> ids = normalizedIds(selections);
            if (!ids.isEmpty()) normalized.put(voter, ids);
        });
        return normalized;
    }

    private static List<String> normalizedIds(List<String> source) {
        if (source == null) return new ArrayList<>();
        return source.stream()
                .map(GovernmentStateNormalizer::clean)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static Map<String, Long> normalizedTotals(Map<String, Long> source) {
        Map<String, Long> normalized = new LinkedHashMap<>();
        if (source == null) return normalized;
        source.forEach((key, value) -> {
            if (validKey(key) && value != null) normalized.put(key, value);
        });
        return normalized;
    }

    private static <V> Map<String, V> validEntries(Map<String, V> source) {
        Map<String, V> normalized = new LinkedHashMap<>();
        if (source == null) return normalized;
        source.forEach((key, value) -> {
            if (validKey(key) && value != null) normalized.put(key, value);
        });
        return normalized;
    }

    private static boolean validKey(String value) {
        return value != null && !value.isBlank();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
