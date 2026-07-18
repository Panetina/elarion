package panetina.elarion.addons.offerings.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OfferingProfileContributorTest {
    @Test
    void contributesSelfVisibleOfferingScoreSummary() {
        UUID citizenId = UUID.randomUUID();
        OfferingProfileContributor contributor = new OfferingProfileContributor(id -> 128L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals(CitizenProfileSummaryFields.SOURCE_OFFERINGS, contributor.id());
        assertEquals(1, sections.size());
        CitizenProfileSection section = sections.getFirst();
        assertEquals("offerings.summary", section.id());
        assertEquals(CitizenProfileSummaryFields.SOURCE_OFFERINGS, section.sourceSystem());
        assertEquals(ProfileVisibility.SELF, section.visibility());
        assertEquals(CitizenProfileSummaryFields.FIELD_OFFERING_SCORE, section.fields().getFirst().id());
        assertEquals("128", section.fields().getFirst().value());
        assertEquals(ProfileVisibility.SELF, section.fields().getFirst().visibility());
    }

    @Test
    void negativeReaderValuesAreClampedToZero() {
        UUID citizenId = UUID.randomUUID();
        OfferingProfileContributor contributor = new OfferingProfileContributor(id -> -8L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals("0", sections.getFirst().fields().getFirst().value());
    }

    @Test
    void offeringScoreStatKeyIsStable() {
        assertEquals("offerings_score", OfferingService.OFFERING_SCORE_STAT);
    }
}
