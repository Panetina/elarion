package panetina.elarion.addons.government;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentOfficeDefinition;
import panetina.elarion.addons.government.model.RealmGovernmentState;

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
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_theocracy_faith"));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_election"));
        assertEquals("civic_forum", GovernmentBlockInteractions.expectedBlockForScreen("civic_features"));
        assertTrue(GovernmentBlockInteractions.isCivicScreen("civic_name"));
        assertTrue(GovernmentBlockInteractions.isCivicScreen("civic_module_proposals"));
        assertTrue(GovernmentBlockInteractions.isCivicScreen("civic_election"));
    }

    @Test
    void seatScreensAndModulesRequireSeatOfRuleSessions() {
        assertEquals("seat_of_rule", GovernmentBlockInteractions.expectedBlockForScreen("seat_of_rule"));
        assertEquals("seat_of_rule", GovernmentBlockInteractions.expectedBlockForScreen("seat_module_proposals"));
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
                List.of(office("president"), office("council_member"), office("officer")));

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
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");
        GovernmentFormDefinition republic = form("republic",
                List.of(office("president", "President", 1), office("council_member", "Councilor", 3)));
        GovernmentFormDefinition monarchy = form("monarchy",
                List.of(office("monarch", "Monarch", 1), office("heir", "Heir", 1)));
        GovernmentFormDefinition theocracy = form("theocracy",
                List.of(office("high_priest", "High Priest", 1), office("synod_member", "Synod Member", 3)));
        GovernmentFormDefinition confederation = form("confederation",
                List.of(office("delegate", "Delegate", 3), office("officer", "Confederation Officer", 3)));

        assertEquals("President Vacant", GovernmentBlockInteractions.authorityLabel(
                republic, RealmGovernmentState.empty("realm1").withForm("republic"), id -> "Unknown"));
        assertEquals("President Biggus", GovernmentBlockInteractions.authorityLabel(
                republic, RealmGovernmentState.empty("realm1").withForm("republic").withOfficeHolder("president", ruler),
                id -> "Biggus"));
        assertEquals("Monarch Terea", GovernmentBlockInteractions.authorityLabel(
                monarchy, RealmGovernmentState.empty("realm1").withForm("monarchy").withOfficeHolder("monarch", ruler),
                id -> "Terea"));
        assertEquals("High Priest Lux", GovernmentBlockInteractions.authorityLabel(
                theocracy, RealmGovernmentState.empty("realm1").withForm("theocracy").withOfficeHolder("high_priest", ruler),
                id -> "Lux"));
        RealmGovernmentState delegates = RealmGovernmentState.empty("realm1").withForm("confederation")
                .withOfficeHolder("delegate", ruler)
                .withOfficeHolder("delegate", second);
        assertEquals("Delegates 2/3", GovernmentBlockInteractions.authorityLabel(
                confederation, delegates, id -> id.equals(ruler) ? "One" : "Two"));
    }

    @Test
    void uiDisplayTextDoesNotExposeRawUuidsWhenIdentityIsMissing() {
        String uuid = "cf9a67f2-423f-8db2-9baa-9f248e0c28bd";

        String visible = GovernmentBlockInteractions.uiTextForDisplay(null,
                "Proposer: " + uuid + " submitted a proposal.");

        assertFalse(visible.contains(uuid));
        assertTrue(visible.contains("Unknown Citizen"));
    }

    private static GovernmentFormDefinition form(String id, List<GovernmentOfficeDefinition> offices) {
        return new GovernmentFormDefinition(id, id, "", true, "%realm%", List.of(), false,
                offices, Map.of(), Map.of());
    }

    private static GovernmentOfficeDefinition office(String id) {
        return new GovernmentOfficeDefinition(id, id, "", 1);
    }

    private static GovernmentOfficeDefinition office(String id, String displayName, int maxHolders) {
        return new GovernmentOfficeDefinition(id, displayName, "", maxHolders);
    }
}
