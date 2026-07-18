package panetina.elarion.addons.quests.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class QuestProfileContributorTest {
    @Test
    void contributesSelfVisibleCompletedQuestSummary() {
        UUID citizenId = UUID.randomUUID();
        QuestProfileContributor contributor = new QuestProfileContributor(id -> 4L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals(CitizenProfileSummaryFields.SOURCE_QUESTS, contributor.id());
        assertEquals(1, sections.size());
        CitizenProfileSection section = sections.getFirst();
        assertEquals("quests.summary", section.id());
        assertEquals(CitizenProfileSummaryFields.SOURCE_QUESTS, section.sourceSystem());
        assertEquals(ProfileVisibility.SELF, section.visibility());
        assertEquals(CitizenProfileSummaryFields.FIELD_QUESTS_COMPLETED, section.fields().getFirst().id());
        assertEquals("4", section.fields().getFirst().value());
        assertEquals(ProfileVisibility.SELF, section.fields().getFirst().visibility());
    }

    @Test
    void negativeReaderValuesAreClampedToZero() {
        UUID citizenId = UUID.randomUUID();
        QuestProfileContributor contributor = new QuestProfileContributor(id -> -2L);

        List<CitizenProfileSection> sections = contributor.sections(
                CitizenProfileRequestContext.self(citizenId),
                new CitizenRecord(citizenId, "Mara"));

        assertEquals("0", sections.getFirst().fields().getFirst().value());
    }

    @Test
    void completedQuestStatKeyIsStable() {
        assertEquals("quests_completed", QuestStateService.COMPLETED_QUESTS_STAT);
    }
}
