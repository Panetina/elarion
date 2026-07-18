package panetina.elarion.addons.npcs.model;

import java.util.List;

public record NpcTradeOfferDefinition(
        String id,
        String direction,
        String label,
        String subtitle,
        String itemId,
        int count,
        String customName,
        List<String> lore,
        List<NpcTradeEnchantmentDefinition> enchantments,
        int customModelData,
        String priceKey,
        long price,
        int stockLimit,
        int restockAmount,
        long restockIntervalSeconds,
        boolean enabled,
        String sellMatch,
        String componentPolicy,
        int maxQuantity,
        String stockDestination,
        String destinationOfferId
) {
    public NpcTradeOfferDefinition(
            String id,
            String direction,
            String label,
            String subtitle,
            String itemId,
            int count,
            String customName,
            List<String> lore,
            List<NpcTradeEnchantmentDefinition> enchantments,
            int customModelData,
            String priceKey,
            long price,
            int stockLimit,
            int restockAmount,
            long restockIntervalSeconds,
            boolean enabled,
            String sellMatch,
            String componentPolicy,
            int maxQuantity,
            String stockDestination
    ) {
        this(id, direction, label, subtitle, itemId, count, customName, lore, enchantments,
                customModelData, priceKey, price, stockLimit, restockAmount, restockIntervalSeconds,
                enabled, sellMatch, componentPolicy, maxQuantity, stockDestination, "");
    }

    public NpcTradeOfferDefinition(
            String id,
            String direction,
            String label,
            String subtitle,
            String itemId,
            int count,
            String customName,
            List<String> lore,
            List<NpcTradeEnchantmentDefinition> enchantments,
            int customModelData,
            String priceKey,
            long price,
            int stockLimit,
            int restockAmount,
            long restockIntervalSeconds,
            boolean enabled
    ) {
        this(id, direction, label, subtitle, itemId, count, customName, lore, enchantments,
                customModelData, priceKey, price, stockLimit, restockAmount, restockIntervalSeconds,
                enabled, "", "", 0, "", "");
    }

    public NpcTradeOfferDefinition(
            String id,
            String direction,
            String label,
            String subtitle,
            String itemId,
            int count,
            String customName,
            List<String> lore,
            List<NpcTradeEnchantmentDefinition> enchantments,
            int customModelData,
            long price,
            boolean enabled
    ) {
        this(id, direction, label, subtitle, itemId, count, customName, lore, enchantments,
                customModelData, "", price, 0, 0, 0L, enabled);
    }

    public NpcTradeOfferDefinition(
            String id,
            String direction,
            String label,
            String subtitle,
            String itemId,
            int count,
            String customName,
            List<String> lore,
            List<NpcTradeEnchantmentDefinition> enchantments,
            int customModelData,
            String priceKey,
            long price,
            boolean enabled
    ) {
        this(id, direction, label, subtitle, itemId, count, customName, lore, enchantments,
                customModelData, priceKey, price, 0, 0, 0L, enabled);
    }

    public NpcTradeOfferDefinition(
            String id,
            String direction,
            String label,
            String subtitle,
            String itemId,
            int count,
            String customName,
            List<String> lore,
            List<NpcTradeEnchantmentDefinition> enchantments,
            long price,
            boolean enabled
    ) {
        this(id, direction, label, subtitle, itemId, count, customName, lore, enchantments,
                0, "", price, 0, 0, 0L, enabled);
    }

    public NpcTradeOfferDefinition {
        id = id == null ? "" : id.trim();
        direction = direction == null ? "buy" : direction.trim().toLowerCase(java.util.Locale.ROOT);
        label = label == null ? "" : label.trim();
        subtitle = subtitle == null ? "" : subtitle.trim();
        itemId = itemId == null ? "" : itemId.trim();
        customName = customName == null ? "" : customName.trim();
        priceKey = priceKey == null ? "" : priceKey.trim();
        sellMatch = sellMatch == null || sellMatch.isBlank()
                ? "exact_item"
                : sellMatch.trim().toLowerCase(java.util.Locale.ROOT);
        componentPolicy = componentPolicy == null || componentPolicy.isBlank()
                ? "vanilla_only"
                : componentPolicy.trim().toLowerCase(java.util.Locale.ROOT);
        stockDestination = stockDestination == null || stockDestination.isBlank()
                ? "none"
                : stockDestination.trim().toLowerCase(java.util.Locale.ROOT);
        destinationOfferId = destinationOfferId == null ? "" : destinationOfferId.trim();
        lore = lore == null ? List.of() : lore.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
        enchantments = enchantments == null ? List.of() : List.copyOf(enchantments);
    }

    public NpcTradeOfferDefinition withDestinationOfferId(String destinationOfferId) {
        return new NpcTradeOfferDefinition(
                id,
                direction,
                label,
                subtitle,
                itemId,
                count,
                customName,
                lore,
                enchantments,
                customModelData,
                priceKey,
                price,
                stockLimit,
                restockAmount,
                restockIntervalSeconds,
                enabled,
                sellMatch,
                componentPolicy,
                maxQuantity,
                stockDestination,
                destinationOfferId);
    }
}
