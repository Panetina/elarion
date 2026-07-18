package panetina.elarion.addons.portals.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PortalProfileContributorTest {
    @Test
    void contributesSelfVisiblePortalJourneySummary() {
        UUID citizenId = UUID.randomUUID();
        PortalProfileContributor contributor = new PortalProfileContributor(id -> 12L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals(CitizenProfileSummaryFields.SOURCE_PORTALS, contributor.id());
        assertEquals(1, sections.size());
        CitizenProfileSection section = sections.getFirst();
        assertEquals("portals.summary", section.id());
        assertEquals(CitizenProfileSummaryFields.SOURCE_PORTALS, section.sourceSystem());
        assertEquals(ProfileVisibility.SELF, section.visibility());
        assertEquals(CitizenProfileSummaryFields.FIELD_PORTAL_JOURNEYS, section.fields().getFirst().id());
        assertEquals("12", section.fields().getFirst().value());
        assertEquals(ProfileVisibility.SELF, section.fields().getFirst().visibility());
    }

    @Test
    void negativeReaderValuesAreClampedToZero() {
        UUID citizenId = UUID.randomUUID();
        PortalProfileContributor contributor = new PortalProfileContributor(id -> -5L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals("0", sections.getFirst().fields().getFirst().value());
    }

    @Test
    void portalJourneyStatKeyIsStable() {
        assertEquals("portal_journeys", PortalRouteService.PORTAL_JOURNEYS_STAT);
    }
}
