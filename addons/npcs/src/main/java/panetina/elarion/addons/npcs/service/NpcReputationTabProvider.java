package panetina.elarion.addons.npcs.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.npcs.model.NpcReputationSummary;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionTab;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.service.ElarionCollectionService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class NpcReputationTabProvider implements ElarionCollectionService.TabProvider {
    public static final String TAB_ID = "reputation";

    private final ElarionApi api;
    private final NpcDefinitionService definitions;
    private final NpcRelationshipService relationships;

    public NpcReputationTabProvider(ElarionApi api, NpcDefinitionService definitions,
                                    NpcRelationshipService relationships) {
        this.api = api;
        this.definitions = definitions;
        this.relationships = relationships;
    }

    @Override
    public String id() {
        return TAB_ID;
    }

    @Override
    public ElarionCollectionTab snapshot(ServerPlayerEntity player) {
        Map<String, NpcReputationSummary> scores = relationships.factionSummaries(player.getUuid());
        List<ElarionCollectionEntry> entries = new ArrayList<>();
        for (String faction : factionIds()) {
            NpcReputationSummary summary = scores.getOrDefault(faction, NpcReputationSummary.EMPTY);
            long score = summary.totalScore();
            NpcReputationTier tier = NpcReputationTier.forScore(score);
            String contacts = summary.contactCount() == 1 ? "1 known contact"
                    : summary.contactCount() + " known contacts";
            entries.add(new ElarionCollectionEntry(
                    faction,
                    displayName(faction),
                    contacts,
                    "Standing: " + tier.label() + "\nFaction reputation: " + score,
                    tier.progress() + "/" + tier.progressMaximum(),
                    icon(faction),
                    true,
                    false,
                    List.of(),
                    tier.color(),
                    tier.label(),
                    tier.color()));
        }
        return new ElarionCollectionTab(TAB_ID, "Reputation",
                "Standing with Realms, powers, and factions.", entries);
    }

    @Override
    public ElarionCollectionService.ActionResult act(ServerPlayerEntity player, String entryId, String actionId) {
        return ElarionCollectionService.ActionResult.failure("Reputation has no direct actions.");
    }

    private Set<String> factionIds() {
        Set<String> result = new LinkedHashSet<>();
        api.realms().all().stream().map(RealmDefinition::id).sorted()
                .forEach(id -> result.add("realm:" + id));
        result.add("worldheart");
        result.add("underworld");
        definitions.npcs().stream().map(npc -> npc.faction()).sorted().forEach(result::add);
        result.remove("unaffiliated");
        return result;
    }

    private String displayName(String faction) {
        if (faction.startsWith("realm:")) {
            String realmId = faction.substring("realm:".length());
            return api.realms().find(realmId).map(api.realms()::displayName).orElse(titleCase(realmId));
        }
        if (faction.equals("worldheart")) return api.serverIdentity().capitalName();
        if (faction.equals("underworld")) return "Underworld";
        return titleCase(faction.startsWith("faction:") ? faction.substring(8) : faction);
    }

    private static String icon(String faction) {
        if (faction.startsWith("realm:")) return "realm";
        if (faction.equals("worldheart")) return "world";
        if (faction.equals("underworld")) return "underworld";
        return "reputation";
    }

    private static String titleCase(String value) {
        String clean = value == null ? "" : value.replace('_', ' ').replace('-', ' ').trim()
                .toLowerCase(Locale.ROOT);
        if (clean.isBlank()) return "Unknown Faction";
        StringBuilder result = new StringBuilder(clean.length());
        boolean upper = true;
        for (char character : clean.toCharArray()) {
            result.append(upper ? Character.toUpperCase(character) : character);
            upper = Character.isWhitespace(character);
        }
        return result.toString();
    }
}
