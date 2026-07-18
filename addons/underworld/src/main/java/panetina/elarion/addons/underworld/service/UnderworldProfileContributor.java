package panetina.elarion.addons.underworld.service;

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

public final class UnderworldProfileContributor implements CitizenProfileContributor {
    private final ToLongFunction<UUID> deaths;

    public UnderworldProfileContributor(PlayerStatsService playerStats) {
        this(deathReader(Objects.requireNonNull(playerStats, "playerStats")));
    }

    UnderworldProfileContributor(ToLongFunction<UUID> deaths) {
        this.deaths = Objects.requireNonNull(deaths, "deaths");
    }

    private static ToLongFunction<UUID> deathReader(PlayerStatsService playerStats) {
        return playerId -> playerStats.value(playerId, UnderworldService.LIFETIME_DEATHS_STAT);
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_UNDERWORLD;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        long count = Math.max(0L, deaths.applyAsLong(target.uuid()));
        return List.of(new CitizenProfileSection(
                "underworld.summary",
                "Underworld",
                CitizenProfileSummaryFields.SOURCE_UNDERWORLD,
                ProfileVisibility.SELF,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_DEATHS,
                        "Deaths",
                        Long.toString(count),
                        ProfileVisibility.SELF))));
    }
}
