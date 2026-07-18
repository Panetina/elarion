package panetina.elarion.addons.quests.service;

import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileContributor;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;
import panetina.elarion.core.service.PlayerStatsService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.ToLongFunction;

public final class QuestProfileContributor implements CitizenProfileContributor {
    private final ToLongFunction<UUID> completedQuests;

    public QuestProfileContributor(PlayerStatsService playerStats) {
        this(completionReader(Objects.requireNonNull(playerStats, "playerStats")));
    }

    QuestProfileContributor(ToLongFunction<UUID> completedQuests) {
        this.completedQuests = Objects.requireNonNull(completedQuests, "completedQuests");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_QUESTS;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        long value = Math.max(0L, completedQuests.applyAsLong(target.uuid()));
        return List.of(new CitizenProfileSection(
                "quests.summary",
                "Quests",
                CitizenProfileSummaryFields.SOURCE_QUESTS,
                ProfileVisibility.SELF,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_QUESTS_COMPLETED,
                        "Completed Quests",
                        Long.toString(value),
                        ProfileVisibility.SELF))));
    }

    private static ToLongFunction<UUID> completionReader(PlayerStatsService playerStats) {
        return playerId -> playerStats.value(playerId, QuestStateService.COMPLETED_QUESTS_STAT);
    }
}
