package panetina.elarion.addons.government.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentFormDefinitionTest {
    @Test
    void defaultsSafeNullCollections() {
        GovernmentFormDefinition definition = new GovernmentFormDefinition(
                "republic", "", null, true, "", null, null, null, null);

        assertEquals("republic", definition.displayName());
        assertEquals("republic of %realm%", definition.officialNameTemplate());
        assertTrue(definition.authorityOffices().isEmpty());
        assertTrue(definition.offices().isEmpty());
        assertTrue(definition.actions().isEmpty());
        assertTrue(definition.transitions().isEmpty());
    }

    @Test
    void copiesCollections() {
        GovernmentFormDefinition definition = new GovernmentFormDefinition(
                "republic", "Republic", "", true,
                "Republic of %realm%", List.of("seat"),
                List.of(new GovernmentOfficeDefinition("seat", "Seat", "", 3)),
                Map.of("citizen", List.of("vote")),
                Map.of("treasury", "keep"));

        assertEquals(1, definition.offices().size());
        assertEquals(List.of("seat"), definition.authorityOffices());
        assertEquals(List.of("vote"), definition.actions().get("citizen"));
        assertEquals("keep", definition.transitions().get("treasury"));
    }
}
