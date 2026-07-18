package panetina.elarion.addons.npcs.service;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.npcs.model.NpcTradeDeliveryStack;
import panetina.elarion.addons.npcs.model.NpcTradeEnchantmentDefinition;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;

import java.util.List;

public final class NpcTradeItemStacks {
    private NpcTradeItemStacks() {
    }

    public static boolean itemAvailable(NpcTradeOfferDefinition offer) {
        Identifier itemId = Identifier.tryParse(offer.itemId());
        return itemId != null && Registries.ITEM.containsId(itemId);
    }

    public static ItemStack preview(ServerPlayerEntity player, NpcTradeOfferDefinition offer) {
        return stack(player, NpcTradeDeliveryStack.from(offer, 1), offer.count());
    }

    public static ItemStack stack(ServerPlayerEntity player, NpcTradeDeliveryStack delivery, int count) {
        Identifier itemId = Identifier.tryParse(delivery.itemId());
        boolean itemAvailable = itemId != null && Registries.ITEM.containsId(itemId);
        ItemStack stack = itemAvailable
                ? new ItemStack(Registries.ITEM.get(itemId), Math.max(1, count))
                : new ItemStack(Items.BARRIER);
        applyPresentation(player, stack, delivery.customName(), delivery.customModelData(),
                delivery.lore(), delivery.enchantments());
        return stack;
    }

    public static void deliver(ServerPlayerEntity player, NpcTradeDeliveryStack delivery) {
        int remaining = delivery.count();
        int maxStack = Math.max(1, stack(player, delivery, 1).getMaxCount());
        while (remaining > 0) {
            int count = Math.min(remaining, maxStack);
            ItemStack stack = stack(player, delivery, count);
            if (!player.getInventory().insertStack(stack) && !stack.isEmpty()) {
                player.dropItem(stack, false);
            } else if (!stack.isEmpty()) {
                player.dropItem(stack, false);
            }
            remaining -= count;
        }
        player.getInventory().markDirty();
    }

    private static void applyPresentation(
            ServerPlayerEntity player,
            ItemStack stack,
            String customName,
            int customModelData,
            List<String> lore,
            List<NpcTradeEnchantmentDefinition> enchantments
    ) {
        if (customName != null && !customName.isBlank()) {
            stack.set(DataComponentTypes.CUSTOM_NAME, plainName(customName));
        }
        if (customModelData > 0) {
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData));
        }
        if (lore != null && !lore.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(
                    lore.stream().map(NpcTradeItemStacks::plainLore).toList()));
        }
        addEnchantments(player, stack, enchantments);
    }

    private static void addEnchantments(
            ServerPlayerEntity player,
            ItemStack stack,
            List<NpcTradeEnchantmentDefinition> definitions
    ) {
        if (definitions == null || definitions.isEmpty()) return;
        var registry = player.getServer().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        for (NpcTradeEnchantmentDefinition definition : definitions) {
            Identifier id = Identifier.tryParse(definition.id());
            if (id == null) continue;
            java.util.Optional<RegistryEntry.Reference<Enchantment>> enchantment =
                    registry.getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, id));
            enchantment.ifPresent(value -> stack.addEnchantment(value, definition.level()));
        }
    }

    private static Text plainName(String value) {
        return Text.literal(value).setStyle(Style.EMPTY.withItalic(false));
    }

    private static Text plainLore(String value) {
        return Text.literal(value)
                .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY));
    }
}
