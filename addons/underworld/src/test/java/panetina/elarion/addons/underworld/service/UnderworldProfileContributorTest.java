package panetina.elarion.addons.underworld.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UnderworldProfileContributorTest {
    @Test
    void contributesSelfVisibleLifetimeDeathSummary() {
        UUID citizenId = UUID.randomUUID();
        UnderworldProfileContributor contributor = new UnderworldProfileContributor(id -> 7L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals(CitizenProfileSummaryFields.SOURCE_UNDERWORLD, contributor.id());
        assertEquals(1, sections.size());
        CitizenProfileSection section = sections.getFirst();
        assertEquals("underworld.summary", section.id());
        assertEquals(CitizenProfileSummaryFields.SOURCE_UNDERWORLD, section.sourceSystem());
        assertEquals(ProfileVisibility.SELF, section.visibility());
        assertEquals(CitizenProfileSummaryFields.FIELD_DEATHS, section.fields().getFirst().id());
        assertEquals("7", section.fields().getFirst().value());
        assertEquals(ProfileVisibility.SELF, section.fields().getFirst().visibility());
    }

    @Test
    void negativeReaderValuesAreClampedToZero() {
        UUID citizenId = UUID.randomUUID();
        UnderworldProfileContributor contributor = new UnderworldProfileContributor(id -> -3L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals("0", sections.getFirst().fields().getFirst().value());
    }

    @Test
    void lifetimeDeathStatKeyIsStable() {
        assertEquals("underworld_lifetime_deaths", UnderworldService.LIFETIME_DEATHS_STAT);
    }
}
