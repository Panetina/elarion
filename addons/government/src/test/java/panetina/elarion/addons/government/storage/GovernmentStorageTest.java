package panetina.elarion.addons.government.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.model.GovernmentLawRecord;
import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.GovernmentProposalStatus;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentStorageTest {
    @TempDir
    Path root;

    @Test
    void stateRoundTrips() {
        GovernmentState state = new GovernmentState();
        state.realms.put("realm1", RealmGovernmentState.empty("realm1")
                .withVotedIdentity("Oak", "OAK")
                .withVotedColor("gold")
                .withForm("theocracy")
                .withFaithIdentity("Church of Oak", "OAK")
                .withFoundingElectionComplete());
        state.realms.put("vacant", RealmGovernmentState.empty("vacant")
                .withForm("monarchy")
                .withFoundingElectionComplete()
                .withFoundingElectionReopened());
        GovernmentVoteState vote = new GovernmentVoteState("realm1", GovernmentVoteType.REALM_NAME);
        vote.resolved = true;
        vote.winnerIds = java.util.List.of("oak");
        vote.resultTotals = Map.of("oak", 7L, "vale", 4L);
        state.votes.put("realm1:realm_name", vote);
        UUID author = UUID.randomUUID();
        UUID citizen = UUID.randomUUID();
        GovernmentProposalRecord proposal = GovernmentProposalRecord.create(
                "realm1_proposal_oak_roads", "realm1", author, "law",
                "Oak Roads", "Build and maintain public roads.", 123L)
                .withStatusAndSponsor(GovernmentProposalStatus.APPROVED_PENDING_FINALIZATION, author, author, 234L)
                .withStatus(GovernmentProposalStatus.CITIZEN_RATIFICATION, author, 456L)
                .withCitizenVote(citizen, true);
        GovernmentLawRecord law = GovernmentLawRecord.enact("realm1_law_oak_roads", proposal,
                "Oak Roads", "Build and maintain public roads.", author, 456L);
        GovernmentLawRecord archived = law.archived(author, 789L);
        state.proposals.put(proposal.id(), proposal);
        state.laws.put(archived.id(), archived);
        GovernmentOfficeTermRecord term = GovernmentOfficeTermRecord.active("realm1", "high_priest", citizen, 321L)
                .withDecision(true)
                .withDecision(false);
        state.officeTerms.put(term.key(), term);
        state.authorityTitleRestores.put("realm1|" + citizen, "aquatic");

        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("government-test"), root);
        storage.save(root, state);
        GovernmentState loaded = storage.load(root);

        assertEquals(GovernmentState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertEquals("theocracy", loaded.realms.get("realm1").activeGovernmentFormId());
        assertEquals("Oak", loaded.realms.get("realm1").votedDisplayName());
        assertEquals("OAK", loaded.realms.get("realm1").votedTag());
        assertEquals("gold", loaded.realms.get("realm1").votedColor());
        assertEquals("Church of Oak", loaded.realms.get("realm1").faithDisplayName());
        assertEquals("OAK", loaded.realms.get("realm1").faithTag());
        assertEquals(true, loaded.realms.get("realm1").colorVoteCompletedAt() > 0L);
        assertEquals(true, loaded.realms.get("realm1").faithVoteCompletedAt() > 0L);
        assertEquals(true, loaded.realms.get("realm1").foundingElectionCompletedAt() > 0L);
        assertEquals(0L, loaded.realms.get("vacant").foundingElectionCompletedAt());
        assertEquals(7L, loaded.votes.get("realm1:realm_name").resultTotals.get("oak"));
        assertEquals("Oak Roads", loaded.proposals.get("realm1_proposal_oak_roads").title());
        assertEquals(GovernmentProposalStatus.CITIZEN_RATIFICATION,
                loaded.proposals.get("realm1_proposal_oak_roads").status());
        assertEquals(author, loaded.proposals.get("realm1_proposal_oak_roads").sponsorId());
        assertEquals(true, loaded.proposals.get("realm1_proposal_oak_roads").citizenVotes().get(citizen));
        assertEquals("realm1_proposal_oak_roads", loaded.laws.get("realm1_law_oak_roads").sourceProposalId());
        assertEquals(false, loaded.laws.get("realm1_law_oak_roads").active());
        assertEquals(true, loaded.laws.get("realm1_law_oak_roads").restored().active());
        GovernmentOfficeTermRecord loadedTerm = loaded.officeTerms.get(term.key());
        assertEquals("high_priest", loadedTerm.officeId());
        assertEquals(321L, loadedTerm.chosenAt());
        assertEquals(1L, loadedTerm.approvedCount());
        assertEquals(1L, loadedTerm.rejectedCount());
        assertEquals("aquatic", loaded.authorityTitleRestores.get("realm1|" + citizen));
    }

    @Test
    void unsupportedFutureSchemaIsQuarantined() throws Exception {
        Files.createDirectories(root);
        Path stateFile = root.resolve("state.json");
        Files.writeString(stateFile, "{\"schemaVersion\":99,\"realms\":{}}");

        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("government-test"), root);
        GovernmentState loaded = storage.load(root);

        assertEquals(GovernmentState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertTrue(loaded.realms.isEmpty());
        assertFalse(Files.exists(stateFile));
        try (var files = Files.list(root)) {
            assertTrue(files.anyMatch(path -> path.getFileName().toString().startsWith("state.json.corrupt-")));
        }
    }
}
