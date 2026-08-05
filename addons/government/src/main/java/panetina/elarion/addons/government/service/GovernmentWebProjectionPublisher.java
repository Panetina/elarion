package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentVoteState;
import panetina.elarion.addons.government.model.GovernmentVoteType;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;
import panetina.elarion.core.model.ElarionDomainEvent;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Publishes public Government read models without exposing ballots or voter identities. */
public final class GovernmentWebProjectionPublisher {
    private final ElarionApi api;
    private final GovernmentStateService states;

    public GovernmentWebProjectionPublisher(ElarionApi api, GovernmentStateService states) {
        this.api = api;
        this.states = states;
    }

    public void publishAll() {
        api.realms().all().forEach(realm -> publishRealm(realm.id()));
    }

    public void onDomainEvent(ElarionDomainEvent event) {
        if (event == null || !"elarion_government".equals(event.sourceSystem()) || event.realmId().isBlank()) return;
        publishRealm(event.realmId());
        if (event.actorId() != null) publishOffice(event.actorId());
    }

    private void publishRealm(String realmId) {
        api.realms().find(realmId).ifPresent(realm -> {
            Map<String, String> identity = new LinkedHashMap<>();
            identity.put("displayName", api.realms().displayName(realm));
            identity.put("officialName", api.realms().officialName(realm));
            identity.put("shortName", api.realms().shortName(realm));
            identity.put("tag", api.realms().prefix(realm));
            identity.put("colorName", api.realms().color(realm));
            var heraldry = states.heraldry(realm.id());
            identity.put("heraldryRevision", Long.toString(heraldry.revision()));
            identity.put("heraldryPaletteBase64", java.util.Base64.getEncoder().encodeToString(heraldry.paletteIndices()));
            api.system().webProjections().publishState("realm.identity", realm.id(), realm.id(),
                    Visibility.PUBLIC, identity);
            if (realm.spawn() != null && !realm.spawn().worldId().isBlank()) {
                api.system().webProjections().publishState("world.presentation", realm.spawn().worldId(), realm.id(),
                        Visibility.PUBLIC, Map.of("displayName", api.realms().officialName(realm)));
            }
        });
        states.realm(realmId).officeHolders().values().forEach(holders -> holders.forEach(this::publishOffice));
        for (GovernmentVoteType type : GovernmentVoteType.values()) {
            publishVote(realmId, type, states.existingVote(realmId, type).orElse(null));
        }
    }

    private void publishOffice(UUID citizenId) {
        if (citizenId == null) return;
        String realmId = api.citizens().find(citizenId).map(citizen -> citizen.realmId()).orElse("");
        api.system().webProjections().publishState("government.office", citizenId.toString(), realmId,
                Visibility.WHITELISTED, Map.of("officeDisplayName", states.officeDisplayName(citizenId)));
    }

    private void publishVote(String realmId, GovernmentVoteType type, GovernmentVoteState vote) {
        long now = System.currentTimeMillis();
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("title", title(type));
        payload.put("status", status(vote, now));
        payload.put("summary", summary(vote));
        payload.put("voteType", type.name().toLowerCase(Locale.ROOT));
        payload.put("round", Integer.toString(vote == null ? 0 : vote.round));
        payload.put("optionCount", Integer.toString(vote == null ? 0 : vote.options.size()));
        payload.put("ballotCount", Integer.toString(vote == null ? 0 : vote.ballots.size()));
        payload.put("endsAt", Long.toString(vote == null ? 0 : Math.max(vote.proposalEndsAt, vote.endsAt)));
        api.system().webProjections().publishState("election", realmId + ":" + type.name().toLowerCase(Locale.ROOT),
                realmId, Visibility.PUBLIC, payload);
    }

    static String status(GovernmentVoteState vote, long now) {
        if (vote == null) return "NOT_STARTED";
        if (vote.resolved) return "RESOLVED";
        if (vote.proposalActive(now)) return "PROPOSALS_OPEN";
        if (vote.active(now)) return vote.runoff ? "RUNOFF_OPEN" : "VOTING_OPEN";
        if (vote.ended(now)) return "RESOLVING";
        return "WAITING";
    }

    private static String summary(GovernmentVoteState vote) {
        if (vote == null) return "No civic window has opened.";
        int options = vote.options.size();
        int ballots = vote.ballots.size();
        return options + (options == 1 ? " option" : " options") + " recorded; "
                + ballots + (ballots == 1 ? " ballot" : " ballots") + ".";
    }

    private static String title(GovernmentVoteType type) {
        return switch (type) {
            case REALM_NAME -> "Realm Name";
            case REALM_COLOR -> "Realm Color";
            case GOVERNMENT_FORM -> "Government Form";
            case FOUNDING_ELECTION -> "Founding Election";
        };
    }
}
