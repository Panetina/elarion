package panetina.elarion.core.model.profile;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CitizenProfileSummaryFieldsTest {
    @Test
    void reservedProfileSummaryFieldsExposeExpectedLedgerContracts() {
        assertTrue(CitizenProfileSummaryFields.RESERVED_BY_SOURCE
                .get(CitizenProfileSummaryFields.SOURCE_PROGRESSION)
                .contains(CitizenProfileSummaryFields.FIELD_ADVANCEMENTS_COMPLETED));
        assertTrue(CitizenProfileSummaryFields.RESERVED_BY_SOURCE
                .get(CitizenProfileSummaryFields.SOURCE_OFFERINGS)
                .contains(CitizenProfileSummaryFields.FIELD_OFFERING_SCORE));
        assertTrue(CitizenProfileSummaryFields.RESERVED_BY_SOURCE
                .get(CitizenProfileSummaryFields.SOURCE_QUESTS)
                .contains(CitizenProfileSummaryFields.FIELD_QUESTS_COMPLETED));
        assertTrue(CitizenProfileSummaryFields.RESERVED_BY_SOURCE
                .get(CitizenProfileSummaryFields.SOURCE_NPCS)
                .contains(CitizenProfileSummaryFields.FIELD_REPUTATION));
        assertTrue(CitizenProfileSummaryFields.RESERVED_BY_SOURCE
                .get(CitizenProfileSummaryFields.SOURCE_UNDERWORLD)
                .contains(CitizenProfileSummaryFields.FIELD_DEATHS));
    }

    @Test
    void reservedSourceAndFieldIdsAreNormalizedAndNonBlank() {
        Set<String> pairs = new HashSet<>();
        CitizenProfileSummaryFields.RESERVED_BY_SOURCE.forEach((source, fields) -> {
            assertStableId(source);
            assertFalse(fields.isEmpty());
            fields.forEach(field -> {
                assertStableId(field);
                assertTrue(pairs.add(source + ":" + field));
            });
        });
    }

    private static void assertStableId(String value) {
        assertFalse(value.isBlank());
        assertEquals(value, value.trim());
        assertEquals(value, value.toLowerCase(Locale.ROOT));
        assertFalse(value.contains(" "));
        assertFalse(value.contains("_"));
    }
}
