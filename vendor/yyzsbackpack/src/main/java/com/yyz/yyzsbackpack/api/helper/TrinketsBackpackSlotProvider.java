package com.yyz.yyzsbackpack.api.helper;

import com.mojang.datafixers.util.Pair;
import com.yyz.yyzsbackpack.api.IBackpackSlot;
import com.yyz.yyzsbackpack.api.IBackpackSlots;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Elarion's authoritative backpack source: only a Backpack placed in a real
 * Trinkets slot is active. Reflection keeps the vendor jar independent from
 * Trinkets' bundled Cardinal Components compile-time implementation.
 */
final class TrinketsBackpackSlotProvider implements IBackpackSlots {
    @Override
    public List<IBackpackSlot> getSlots(Player player) {
        try {
            Optional<?> component = componentFor(player);
            if (component.isEmpty()) return List.of();
            Object equipped = component.get().getClass().getMethod("getAllEquipped").invoke(component.get());
            if (!(equipped instanceof List<?> pairs)) return List.of();

            List<IBackpackSlot> slots = new ArrayList<>(pairs.size());
            for (Object value : pairs) {
                if (!(value instanceof Pair<?, ?> pair)) continue;
                Object reference = pair.getFirst();
                Object inventory = reference.getClass().getMethod("inventory").invoke(reference);
                int index = (int) reference.getClass().getMethod("index").invoke(reference);
                if (inventory instanceof Container container) {
                    slots.add(new TrinketSlot(container, index));
                }
            }
            return slots;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return List.of();
        }
    }

    private static Optional<?> componentFor(Player player) throws ReflectiveOperationException {
        Class<?> api = Class.forName("dev.emi.trinkets.api.TrinketsApi");
        Object result = api.getMethod("getTrinketComponent", LivingEntity.class).invoke(null, player);
        return result instanceof Optional<?> optional ? optional : Optional.empty();
    }

    private record TrinketSlot(Container inventory, int index) implements IBackpackSlot {
        @Override
        public ItemStack getStack() {
            return inventory.getItem(index);
        }

        @Override
        public void setStack(ItemStack stack) {
            inventory.setItem(index, stack);
        }
    }
}
