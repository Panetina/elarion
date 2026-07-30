package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentLawRecord;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Runtime-only Realm projection for civic proposals and records.
 *
 * <p>The persisted maps remain canonical. This index is rebuilt after load and
 * updated by the state service on each owned mutation so ordinary Realm views
 * do not scan records belonging to every Realm.</p>
 */
final class GovernmentRealmRecordIndex {
    private final Map<String, List<GovernmentProposalRecord>> proposalsByRealm = new LinkedHashMap<>();
    private final Map<String, List<GovernmentLawRecord>> lawsByRealm = new LinkedHashMap<>();
    private final Map<String, String> proposalRealmById = new LinkedHashMap<>();
    private final Map<String, String> lawRealmById = new LinkedHashMap<>();
    private final Map<String, Long> proposalOrderById = new LinkedHashMap<>();
    private final Map<String, Long> lawOrderById = new LinkedHashMap<>();
    private long nextProposalOrder;
    private long nextLawOrder;

    void rebuild(
            Collection<GovernmentProposalRecord> proposals,
            Collection<GovernmentLawRecord> laws
    ) {
        clear();
        for (GovernmentProposalRecord proposal : proposals) {
            proposalOrderById.put(proposal.id(), nextProposalOrder++);
            proposalRealmById.put(proposal.id(), proposal.realmId());
            proposalsByRealm.computeIfAbsent(proposal.realmId(), ignored -> new ArrayList<>()).add(proposal);
        }
        proposalsByRealm.values().forEach(records -> records.sort(this::compareProposals));
        for (GovernmentLawRecord law : laws) {
            lawOrderById.put(law.id(), nextLawOrder++);
            lawRealmById.put(law.id(), law.realmId());
            lawsByRealm.computeIfAbsent(law.realmId(), ignored -> new ArrayList<>()).add(law);
        }
        lawsByRealm.values().forEach(records -> records.sort(this::compareLaws));
    }

    List<GovernmentProposalRecord> proposals(String realmId) {
        return List.copyOf(proposalsByRealm.getOrDefault(realmId, List.of()));
    }

    List<GovernmentLawRecord> laws(String realmId) {
        return List.copyOf(lawsByRealm.getOrDefault(realmId, List.of()));
    }

    void putProposal(GovernmentProposalRecord proposal) {
        String id = proposal.id();
        String realmId = proposal.realmId();
        proposalOrderById.computeIfAbsent(id, ignored -> nextProposalOrder++);
        String previousRealm = proposalRealmById.put(id, realmId);
        if (previousRealm != null) removeProposal(previousRealm, id);
        List<GovernmentProposalRecord> records = proposalsByRealm.computeIfAbsent(realmId, ignored -> new ArrayList<>());
        records.add(proposalInsertionIndex(records, proposal), proposal);
    }

    void putLaw(GovernmentLawRecord law) {
        String id = law.id();
        String realmId = law.realmId();
        lawOrderById.computeIfAbsent(id, ignored -> nextLawOrder++);
        String previousRealm = lawRealmById.put(id, realmId);
        if (previousRealm != null) removeLaw(previousRealm, id);
        List<GovernmentLawRecord> records = lawsByRealm.computeIfAbsent(realmId, ignored -> new ArrayList<>());
        records.add(lawInsertionIndex(records, law), law);
    }

    void removeRealm(String realmId) {
        proposalsByRealm.keySet().stream()
                .filter(key -> normalize(key).equals(normalize(realmId)))
                .toList()
                .forEach(this::removeProposalRealm);
        lawsByRealm.keySet().stream()
                .filter(key -> normalize(key).equals(normalize(realmId)))
                .toList()
                .forEach(this::removeLawRealm);
    }

    void clear() {
        proposalsByRealm.clear();
        lawsByRealm.clear();
        proposalRealmById.clear();
        lawRealmById.clear();
        proposalOrderById.clear();
        lawOrderById.clear();
        nextProposalOrder = 0L;
        nextLawOrder = 0L;
    }

    private void removeProposal(String realmId, String id) {
        List<GovernmentProposalRecord> records = proposalsByRealm.get(realmId);
        if (records == null) return;
        records.removeIf(record -> id.equals(record.id()));
        if (records.isEmpty()) proposalsByRealm.remove(realmId);
    }

    private void removeLaw(String realmId, String id) {
        List<GovernmentLawRecord> records = lawsByRealm.get(realmId);
        if (records == null) return;
        records.removeIf(record -> id.equals(record.id()));
        if (records.isEmpty()) lawsByRealm.remove(realmId);
    }

    private int proposalInsertionIndex(List<GovernmentProposalRecord> records, GovernmentProposalRecord proposal) {
        int low = 0;
        int high = records.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (compareProposals(records.get(middle), proposal) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private int lawInsertionIndex(List<GovernmentLawRecord> records, GovernmentLawRecord law) {
        int low = 0;
        int high = records.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (compareLaws(records.get(middle), law) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private int compareProposals(GovernmentProposalRecord left, GovernmentProposalRecord right) {
        int byCreatedAt = Long.compare(right.createdAt(), left.createdAt());
        if (byCreatedAt != 0) return byCreatedAt;
        return Long.compare(proposalOrderById.get(left.id()), proposalOrderById.get(right.id()));
    }

    private int compareLaws(GovernmentLawRecord left, GovernmentLawRecord right) {
        int byEnactedAt = Long.compare(right.enactedAt(), left.enactedAt());
        if (byEnactedAt != 0) return byEnactedAt;
        return Long.compare(lawOrderById.get(left.id()), lawOrderById.get(right.id()));
    }

    private void removeProposalRealm(String realmId) {
        for (GovernmentProposalRecord record : proposalsByRealm.remove(realmId)) {
            proposalRealmById.remove(record.id());
            proposalOrderById.remove(record.id());
        }
    }

    private void removeLawRealm(String realmId) {
        for (GovernmentLawRecord record : lawsByRealm.remove(realmId)) {
            lawRealmById.remove(record.id());
            lawOrderById.remove(record.id());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
