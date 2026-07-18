package panetina.elarion.addons.npcs.model;

import java.util.List;

public record NpcTradeCatalogDefinition(String id, List<NpcTradeOfferDefinition> offers) {
    public NpcTradeCatalogDefinition {
        id = id == null ? "" : id.trim();
        offers = offers == null ? List.of() : List.copyOf(offers);
    }
}
