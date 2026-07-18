package panetina.elarion.addons.government;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentOfficeDefinition;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.core.model.ChronicleProjection;
import panetina.elarion.core.model.PublicHistoryEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

class GovernmentBlockInteractionsTest {
    @Test
    void civicScreensRequireCivicForumSessions() {
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen(null));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen(""));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_forum"));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_name"));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_color"));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_form"));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_election"));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_features"));
        assertTrue(GovernmentBlockInteractions.isCivicScreen("civic_name"));
        assertTrue(GovernmentBlockInteractions.isCivicScreen("civic_module_audience"));
        assertTrue(GovernmentBlockInteractions.isCivicScreen("civic_election"));
    }

    @Test
    void seatScreensAndModulesRequireSeatOfRuleSessions() {
        assertEquals("seat_of_rule", GovernmentBlockInteractions.expectedBlockForScreen("seat_of_rule"));
        assertEquals("seat_of_rule", GovernmentBlockInteractions.expectedBlockForScreen("seat_module_review"));
        assertEquals("seat_of_rule", GovernmentBlockInteractions.expectedBlockForScreen("seat_module_laws"));
        assertEquals("seat_of_rule", GovernmentBlockInteractions.expectedBlockForScreen("seat_module_archive"));
        assertFalse(GovernmentBlockInteractions.isCivicScreen("seat_of_rule"));
        assertFalse(GovernmentBlockInteractions.isCivicScreen("seat_module_proposals"));
    }

    @Test
    void seatModulesDoNotExposeFutureOfficePlaceholders() {
        GovernmentFormDefinition monarchy = form("monarchy",
                List.of(office("monarch"), office("heir"), office("officer")));
        GovernmentFormDefinition republic = form("republic",
                List.of(office("president"), office("officer")));

        assertTrue(GovernmentBlockInteractions.seatModuleRows(monarchy).stream()
                .anyMatch(row -> row.id().equals("offices") && row.unlocked()));
        assertTrue(GovernmentBlockInteractions.seatModuleRows(monarchy).stream()
                .anyMatch(row -> row.id().equals("review")));
        assertTrue(GovernmentBlockInteractions.seatModuleRows(monarchy).stream()
                .anyMatch(row -> row.id().equals("projects")));
        assertFalse(GovernmentBlockInteractions.seatModuleRows(monarchy).stream()
                .anyMatch(row -> row.id().equals("heir")));
        assertFalse(GovernmentBlockInteractions.seatModuleRows(republic).stream()
                .anyMatch(row -> row.id().equals("heir")));
        assertFalse(GovernmentBlockInteractions.seatModuleRows(republic).stream()
                .anyMatch(row -> row.id().equals("notices") || row.id().equals("rules")));
    }

    @Test
    void authorityLabelsUsePrimaryOfficeAndNicknames() {
        UUID ruler = UUID.fromString("00000000-0000-0000-0000-000000000001");
        GovernmentFormDefinition republic = form("republic",
                List.of(office("president", "President", 1), office("officer", "Officer", 3)));
        GovernmentFormDefinition monarchy = form("monarchy",
                List.of(office("monarch", "Monarch", 1), office("heir", "Heir", 1)));

        assertEquals("President Vacant", GovernmentBlockInteractions.authorityLabel(
                republic, RealmGovernmentState.empty("realm1").withForm("republic"), id -> "Unknown"));
        assertEquals("President Biggus", GovernmentBlockInteractions.authorityLabel(
                republic, RealmGovernmentState.empty("realm1").withForm("republic").withOfficeHolder("president", ruler),
                id -> "Biggus"));
        assertEquals("Monarch Terea", GovernmentBlockInteractions.authorityLabel(
                monarchy, RealmGovernmentState.empty("realm1").withForm("monarchy").withOfficeHolder("monarch", ruler),
                id -> "Terea"));
        RealmGovernmentState republicOfficer = RealmGovernmentState.empty("realm1").withForm("republic")
                .withOfficeHolder("officer", ruler);
        assertEquals("President Vacant", GovernmentBlockInteractions.authorityLabel(
                republic, republicOfficer, id -> "Officer"));
    }

    @Test
    void uiDisplayTextDoesNotExposeRawUuidsWhenIdentityIsMissing() {
        String uuid = "cf9a67f2-423f-8db2-9baa-9f248e0c28bd";

        String visible = GovernmentBlockInteractions.uiTextForDisplay(null,
                "Proposer: " + uuid + " submitted a proposal.");

        assertFalse(visible.contains(uuid));
        assertTrue(visible.contains("Unknown Ember"));
    }

    @Test
    void governmentChronicleProjectionUsesStructuredMetadata() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                1L,
                "live-index",
                "government",
                "proposal-approved",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "proposal",
                "proposal-1",
                "realm1",
                Map.of(
                        "title", "Harbor Tax Reform",
                        "category", "law",
                        "chronicle.variant", "government.proposal-approved.01"),
                "A civic proposal was approved.");

        ChronicleProjection projection =
                GovernmentChronicleText.project(entry, "Mara");

        assertEquals("Proposal Approved", projection.title());
        assertEquals("The law \"Harbor Tax Reform\" cleared Seat review and awaits final wording.", projection.body());
        assertEquals("Law", projection.category());
        assertEquals("Approved by authority", projection.detailLabel());
        assertEquals("government.proposal-approved.01", projection.variantId());
    }

    @Test
    void governmentChronicleProposalApprovedFamilyIsLibraryReady() {
        assertTrue(GovernmentChronicleText.proposalApprovedFamily().isLibraryReady());
    }

    @Test
    void governmentChronicleProposalApprovedVariantSelectionIsStable() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                1L,
                "live-index",
                "government",
                "proposal-approved",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "proposal",
                "proposal-1",
                "realm1",
                Map.of("title", "Market Stall Rules", "category", "law"),
                "A civic proposal was approved.");

        ChronicleProjection first = GovernmentChronicleText.project(entry, "Mara");
        ChronicleProjection second = GovernmentChronicleText.project(entry, "Mara");

        assertEquals(first.variantId(), second.variantId());
        assertTrue(first.variantId().startsWith("government.proposal-approved."));
        assertFalse(first.variantId().endsWith(".default"));
        assertEquals(first.body(), second.body());
    }

    @Test
    void governmentChronicleProposalApprovedUsesFallbackWhenTitleIsMissing() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "government",
                "proposal-approved",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "proposal",
                "proposal-1",
                "realm1",
                Map.of("category", "law"),
                "A civic proposal was approved.");

        ChronicleProjection projection = GovernmentChronicleText.project(entry, "Mara");

        assertEquals("A civic proposal was approved and awaits official wording.", projection.body());
    }

    @Test
    void governmentChronicleProposalRejectedUsesTemplateFamily() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "government",
                "proposal-rejected",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "proposal",
                "proposal-1",
                "realm1",
                Map.of(
                        "title", "Closed Harbor",
                        "category", "law",
                        "chronicle.variant", "government.proposal-rejected.02"),
                "A civic proposal was rejected.");

        ChronicleProjection projection = GovernmentChronicleText.project(entry, "Mara");

        assertTrue(GovernmentChronicleText.proposalRejectedFamily().isLibraryReady());
        assertEquals("Proposal Rejected", projection.title());
        assertEquals("\"Closed Harbor\" failed authority review as a law.", projection.body());
        assertEquals("Law", projection.category());
        assertEquals("Rejected by authority", projection.detailLabel());
        assertEquals("government.proposal-rejected.02", projection.variantId());
    }

    @Test
    void governmentChronicleProposalRejectedFallsBackWithoutTitle() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "government",
                "proposal-rejected",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "proposal",
                "proposal-1",
                "realm1",
                Map.of("category", "law"),
                "A civic proposal was rejected.");

        ChronicleProjection projection = GovernmentChronicleText.project(entry, "Mara");

        assertEquals("A civic proposal was rejected by the Seat of Rule.", projection.body());
    }

    @Test
    void governmentChronicleCivicRecordCreatedUsesTemplateFamily() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "government",
                "civic-record-created",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "civic_record",
                "law-1",
                "realm1",
                Map.of(
                        "title", "Market Stall Rules",
                        "category", "law",
                        "chronicle.variant", "government.civic-record-created.04"),
                "A Government civic record was created.");

        ChronicleProjection projection = GovernmentChronicleText.project(entry, "Mara");

        assertTrue(GovernmentChronicleText.civicRecordCreatedFamily().isLibraryReady());
        assertEquals("Law Created", projection.title());
        assertEquals("A new law, \"Market Stall Rules\", took effect.", projection.body());
        assertEquals("Law", projection.category());
        assertEquals("Official record created", projection.detailLabel());
        assertEquals("government.civic-record-created.04", projection.variantId());
    }

    @Test
    void governmentChronicleCivicRecordCreatedVariantSelectionIsStable() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                1L,
                "live-index",
                "government",
                "civic-record-created",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "civic_record",
                "law-1",
                "realm1",
                Map.of("title", "Granary Fund", "category", "project"),
                "A Government civic record was created.");

        ChronicleProjection first = GovernmentChronicleText.project(entry, "Mara");
        ChronicleProjection second = GovernmentChronicleText.project(entry, "Mara");

        assertEquals(first.variantId(), second.variantId());
        assertTrue(first.variantId().startsWith("government.civic-record-created."));
        assertEquals(first.body(), second.body());
    }

    @Test
    void privateVoteCastEventsAreNotShownInCivicArchive() {
        PublicHistoryEntry entry = new PublicHistoryEntry(
                UUID.randomUUID(),
                1L,
                "live-index",
                "government",
                "vote-cast",
                null,
                "realm",
                "realm1",
                "realm1",
                Map.of("type", "realm_name"),
                "A private Government ballot was cast.");

        assertFalse(GovernmentChronicleText.visibleInArchive(entry));
    }

    private static GovernmentFormDefinition form(String id, List<GovernmentOfficeDefinition> offices) {
        return new GovernmentFormDefinition(id, id, "", true, "%realm%", List.of(),
                offices, Map.of(), Map.of());
    }

    private static GovernmentOfficeDefinition office(String id) {
        return new GovernmentOfficeDefinition(id, id, "", 1);
    }

    private static GovernmentOfficeDefinition office(String id, String displayName, int maxHolders) {
        return new GovernmentOfficeDefinition(id, displayName, "", maxHolders);
    }
}
