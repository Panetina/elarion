package panetina.elarion.addons.portals.service;

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

public final class PortalProfileContributor implements CitizenProfileContributor {
    private final ToLongFunction<UUID> journeys;

    public PortalProfileContributor(PlayerStatsService playerStats) {
        this(journeyReader(Objects.requireNonNull(playerStats, "playerStats")));
    }

    PortalProfileContributor(ToLongFunction<UUID> journeys) {
        this.journeys = Objects.requireNonNull(journeys, "journeys");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_PORTALS;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        long value = Math.max(0L, journeys.applyAsLong(target.uuid()));
        return List.of(new CitizenProfileSection(
                "portals.summary",
                "Portals",
                CitizenProfileSummaryFields.SOURCE_PORTALS,
                ProfileVisibility.SELF,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_PORTAL_JOURNEYS,
                        "Portal Journeys",
                        Long.toString(value),
                        ProfileVisibility.SELF))));
    }

    private static ToLongFunction<UUID> journeyReader(PlayerStatsService playerStats) {
        return playerId -> playerStats.value(playerId, PortalRouteService.PORTAL_JOURNEYS_STAT);
    }
}
