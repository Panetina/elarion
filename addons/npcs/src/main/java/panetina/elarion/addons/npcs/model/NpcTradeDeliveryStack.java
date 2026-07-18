package panetina.elarion.addons.npcs.model;

import java.util.List;

public record NpcTradeDeliveryStack(
        String itemId,
        int count,
        String customName,
        List<String> lore,
        List<NpcTradeEnchantmentDefinition> enchantments,
        int customModelData
) {
    public NpcTradeDeliveryStack {
        itemId = itemId == null ? "" : itemId.trim();
        count = Math.max(1, Math.min(4096, count));
        customName = customName == null ? "" : customName.trim();
        lore = lore == null ? List.of() : lore.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
        enchantments = enchantments == null ? List.of() : List.copyOf(enchantments);
        customModelData = Math.max(0, customModelData);
    }

    public static NpcTradeDeliveryStack from(NpcTradeOfferDefinition offer, int quantity) {
        int safeQuantity = Math.max(1, Math.min(64, quantity));
        int count = Math.multiplyExact(Math.max(1, offer.count()), safeQuantity);
        return new NpcTradeDeliveryStack(
                offer.itemId(), count, offer.customName(), offer.lore(),
                offer.enchantments(), offer.customModelData());
    }
}
