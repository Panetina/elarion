package panetina.elarion.addons.npcs.model;

public record NpcTradeEnchantmentDefinition(String id, int level) {
    public NpcTradeEnchantmentDefinition {
        id = id == null ? "" : id.trim();
    }
}
