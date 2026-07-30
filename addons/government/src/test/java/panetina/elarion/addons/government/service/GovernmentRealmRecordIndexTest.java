package panetina.elarion.addons.government.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentLawRecord;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.GovernmentProposalStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentRealmRecordIndexTest {
    @Test
    void rebuildPartitionsLoadedRecordsAndPreservesStableNewestFirstOrder() {
        GovernmentRealmRecordIndex index = new GovernmentRealmRecordIndex();
        GovernmentProposalRecord first = proposal("first", "realm1", 10L);
        GovernmentProposalRecord tied = proposal("tied", "realm1", 10L);
        GovernmentProposalRecord newest = proposal("newest", "realm1", 20L);
        GovernmentProposalRecord otherRealm = proposal("other", "realm2", 30L);
        GovernmentLawRecord olderLaw = law("older-law", "realm1", 5L);
        GovernmentLawRecord newerLaw = law("newer-law", "realm1", 15L);

        index.rebuild(List.of(first, tied, newest, otherRealm), List.of(olderLaw, newerLaw));

        assertEquals(List.of("newest", "first", "tied"),
                index.proposals("realm1").stream().map(GovernmentProposalRecord::id).toList());
        assertEquals(List.of("other"),
                index.proposals("realm2").stream().map(GovernmentProposalRecord::id).toList());
        assertEquals(List.of("newer-law", "older-law"),
                index.laws("realm1").stream().map(GovernmentLawRecord::id).toList());
        assertThrows(UnsupportedOperationException.class,
                () -> index.proposals("realm1").add(proposal("illegal", "realm1", 40L)));
    }

    @Test
    void replacementKeepsOriginalTieOrderAndUpdatesProjectedValue() {
        GovernmentRealmRecordIndex index = new GovernmentRealmRecordIndex();
        UUID resolver = UUID.randomUUID();
        GovernmentProposalRecord first = proposal("first", "realm1", 10L);
        GovernmentProposalRecord second = proposal("second", "realm1", 10L);
        index.rebuild(List.of(first, second), List.of());

        index.putProposal(first.withStatus(GovernmentProposalStatus.REJECTED, resolver, 30L));

        List<GovernmentProposalRecord> records = index.proposals("realm1");
        assertEquals(List.of("first", "second"), records.stream().map(GovernmentProposalRecord::id).toList());
        assertEquals(GovernmentProposalStatus.REJECTED, records.getFirst().status());
    }

    @Test
    void realmResetAndGlobalResetRemoveOnlyProjectedRuntimeState() {
        GovernmentRealmRecordIndex index = new GovernmentRealmRecordIndex();
        index.rebuild(
                List.of(proposal("one", "realm1", 1L), proposal("two", "realm2", 2L)),
                List.of(law("law-one", "realm1", 1L), law("law-two", "realm2", 2L)));

        index.removeRealm(" REALM1 ");

        assertTrue(index.proposals("realm1").isEmpty());
        assertTrue(index.laws("realm1").isEmpty());
        assertEquals(1, index.proposals("realm2").size());
        assertEquals(1, index.laws("realm2").size());

        index.clear();

        assertTrue(index.proposals("realm2").isEmpty());
        assertTrue(index.laws("realm2").isEmpty());
    }

    private static GovernmentProposalRecord proposal(String id, String realmId, long createdAt) {
        return GovernmentProposalRecord.create(
                id, realmId, UUID.randomUUID(), "law", "Test Law", "Test law body.", createdAt);
    }

    private static GovernmentLawRecord law(String id, String realmId, long enactedAt) {
        return GovernmentLawRecord.direct(
                id, realmId, "law", "Test Law", "Test law body.", UUID.randomUUID(), enactedAt);
    }
}
