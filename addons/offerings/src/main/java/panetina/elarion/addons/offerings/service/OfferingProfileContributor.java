package panetina.elarion.addons.offerings.service;

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

public final class OfferingProfileContributor implements CitizenProfileContributor {
    private final ToLongFunction<UUID> score;

    public OfferingProfileContributor(PlayerStatsService playerStats) {
        this(scoreReader(Objects.requireNonNull(playerStats, "playerStats")));
    }

    OfferingProfileContributor(ToLongFunction<UUID> score) {
        this.score = Objects.requireNonNull(score, "score");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_OFFERINGS;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        long value = Math.max(0L, score.applyAsLong(target.uuid()));
        return List.of(new CitizenProfileSection(
                "offerings.summary",
                "Offerings",
                CitizenProfileSummaryFields.SOURCE_OFFERINGS,
                ProfileVisibility.SELF,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_OFFERING_SCORE,
                        "Offering Score",
                        Long.toString(value),
                        ProfileVisibility.SELF))));
    }

    private static ToLongFunction<UUID> scoreReader(PlayerStatsService playerStats) {
        return playerId -> playerStats.value(playerId, OfferingService.OFFERING_SCORE_STAT);
    }
}
