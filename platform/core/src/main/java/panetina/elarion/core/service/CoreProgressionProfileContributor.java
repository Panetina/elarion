package panetina.elarion.core.service;

import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileContributor;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.Objects;

public final class CoreProgressionProfileContributor implements CitizenProfileContributor {
    private final PlayerStatsService playerStats;

    public CoreProgressionProfileContributor(PlayerStatsService playerStats) {
        this.playerStats = Objects.requireNonNull(playerStats, "playerStats");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_PROGRESSION;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        long advancements = playerStats.value(target.uuid(), ProgressionService.ADVANCEMENTS_COMPLETED);
        return List.of(new CitizenProfileSection(
                "progression.summary",
                "Progression",
                "progression",
                ProfileVisibility.PUBLIC,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_ADVANCEMENTS_COMPLETED,
                        "Advancements Completed",
                        Long.toString(advancements),
                        ProfileVisibility.PUBLIC))));
    }
}
