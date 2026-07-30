package panetina.elarion.addons.government.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.model.GovernmentLawRecord;
import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.GovernmentProposalStatus;
import panetina.elarion.addons.government.model.GovernmentVoteOption;
import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
                .withForm("republic")
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
        GovernmentOfficeTermRecord term = GovernmentOfficeTermRecord.active("realm1", "president", citizen, 321L)
                .withDecision(true)
                .withDecision(false);
        state.officeTerms.put(term.key(), term);
        state.authorityTitleRestores.put("realm1|" + citizen, "aquatic");

        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("government-test"), root);
        storage.save(root, state);
        GovernmentState loaded = storage.load(root);

        assertEquals(GovernmentState.CURRENT_SCHEMA_VERSION, loaded.schemaVersion);
        assertEquals("republic", loaded.realms.get("realm1").activeGovernmentFormId());
        assertEquals("Oak", loaded.realms.get("realm1").votedDisplayName());
        assertEquals("OAK", loaded.realms.get("realm1").votedTag());
        assertEquals("gold", loaded.realms.get("realm1").votedColor());
        assertEquals(true, loaded.realms.get("realm1").colorVoteCompletedAt() > 0L);
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
        assertEquals("president", loadedTerm.officeId());
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

    @Test
    void recoverableNullRowsAndVoteCollectionsAreNormalizedBeforeBind() throws Exception {
        Files.createDirectories(root);
        Path stateFile = root.resolve("state.json");
        Files.writeString(stateFile, """
                {
                  "schemaVersion": %d,
                  "realms": {"invalid": null},
                  "votes": {
                    "realm1:realm_name": {
                      "realmId": " realm1 ",
                      "type": "REALM_NAME",
                      "round": 0,
                      "options": {
                        "oak": {"id": null, "title": null, "body": null, "tag": null},
                        "invalid": null,
                        "": {"id": "ignored"}
                      },
                      "ballots": {
                        "voter": [null, "", "oak", "oak"],
                        "empty": null,
                        "": ["oak"]
                      },
                      "winnerIds": [null, "", "oak", "oak"],
                      "resultTotals": {"oak": 2, "invalid": null}
                    },
                    "null-vote": null,
                    "missing-realm": {"type": "REALM_COLOR"}
                  },
                  "proposals": {"invalid": null},
                  "laws": {"invalid": null},
                  "officeTerms": {"invalid": null},
                  "authorityTitleRestores": {"invalid": null}
                }
                """.formatted(GovernmentState.CURRENT_SCHEMA_VERSION));

        GovernmentStorage storage = new GovernmentStorage(LoggerFactory.getLogger("government-test"), root);
        GovernmentState loaded = storage.load(root);

        assertTrue(Files.exists(stateFile));
        assertTrue(loaded.realms.isEmpty());
        assertTrue(loaded.proposals.isEmpty());
        assertTrue(loaded.laws.isEmpty());
        assertTrue(loaded.officeTerms.isEmpty());
        assertTrue(loaded.authorityTitleRestores.isEmpty());
        assertEquals(1, loaded.votes.size());
        GovernmentVoteState vote = loaded.votes.get("realm1:realm_name");
        assertEquals("realm1", vote.realmId);
        assertEquals(1, vote.round);
        assertEquals(1, vote.options.size());
        assertEquals("oak", vote.options.get("oak").id);
        assertEquals("", vote.options.get("oak").title);
        assertEquals(Map.of("voter", List.of("oak")), vote.ballots);
        assertEquals(List.of("oak"), vote.winnerIds);
        assertEquals(Map.of("oak", 2L), vote.resultTotals);

        vote.options.put("republic", GovernmentVoteOption.governmentForm(
                "republic", "Republic", "Elect a President."));
        vote.ballots.put("next-voter", List.of("republic"));
    }

    @Test
    void explicitNullVoteCollectionsLoadAsMutableEmptyCollections() throws Exception {
        Files.createDirectories(root);
        Files.writeString(root.resolve("state.json"), """
                {
                  "schemaVersion": %d,
                  "votes": {
                    "realm1:realm_color": {
                      "realmId": "realm1",
                      "type": "REALM_COLOR",
                      "options": null,
                      "ballots": null,
                      "winnerIds": null,
                      "resultTotals": null
                    }
                  }
                }
                """.formatted(GovernmentState.CURRENT_SCHEMA_VERSION));

        GovernmentState loaded = new GovernmentStorage(LoggerFactory.getLogger("government-test"), root).load(root);
        GovernmentVoteState vote = loaded.votes.get("realm1:realm_color");

        assertTrue(vote.options.isEmpty());
        assertTrue(vote.ballots.isEmpty());
        assertTrue(vote.winnerIds.isEmpty());
        assertTrue(vote.resultTotals.isEmpty());
        vote.options.put("gold", GovernmentVoteOption.realmColor(
                "gold", "Gold", "Use gold."));
        vote.ballots.put("voter", List.of("gold"));
        vote.winnerIds.add("gold");
        vote.resultTotals.put("gold", 1L);
    }
}
