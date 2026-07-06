package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentLawRecordTest {
    @Test
    void finalizedRecordPreservesSourceProposalAndType() {
        UUID author = UUID.randomUUID();
        UUID authority = UUID.randomUUID();
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                "realm1_proposal_market_rules",
                "realm1",
                author,
                "civic_rule",
                "Market rules",
                "Please make market rules.",
                10L);

        GovernmentLawRecord record = GovernmentLawRecord.enact(
                "realm1_law_market_rules",
                proposal,
                "Market Stall Rules",
                "Market stalls must be labeled and kept clear.",
                authority,
                20L);

        assertEquals("realm1", record.realmId());
        assertEquals("realm1_proposal_market_rules", record.sourceProposalId());
        assertEquals("civic_rule", record.category());
        assertEquals("Market Stall Rules", record.title());
        assertTrue(record.active());
    }

    @Test
    void directRecordHasNoSourceProposalAndCanArchiveRestore() {
        UUID monarch = UUID.randomUUID();
        UUID archivist = UUID.randomUUID();
        GovernmentLawRecord record = GovernmentLawRecord.direct(
                "realm1_law_crown_notice",
                "realm1",
                "public_notice",
                "Crown Notice",
                "The court will meet at sundown.",
                monarch,
                30L);

        GovernmentLawRecord archived = record.archived(archivist, 40L);
        GovernmentLawRecord restored = archived.restored();

        assertEquals("", record.sourceProposalId());
        assertEquals("public_notice", record.category());
        assertEquals(false, archived.active());
        assertEquals(40L, archived.archivedAt());
        assertEquals(archivist, archived.archivedBy());
        assertTrue(restored.active());
        assertEquals(0L, restored.archivedAt());
        assertEquals(null, restored.archivedBy());
    }
}
