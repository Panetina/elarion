package panetina.elarion.addons.npcs.service;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.npcs.model.NpcTradeDeliveryStack;
import panetina.elarion.addons.npcs.model.NpcTradeEscrowResult;
import panetina.elarion.addons.npcs.model.NpcTradeEscrowStack;
import panetina.elarion.addons.npcs.model.NpcTradeOfferDefinition;

import java.util.ArrayList;
import java.util.List;

public final class NpcTradeInventoryEscrow {
    private NpcTradeInventoryEscrow() {
    }

    public static int maxSellable(ServerPlayerEntity player, NpcTradeOfferDefinition offer) {
        if (player == null || offer == null || !"sell".equals(offer.direction()) || !offer.enabled()) return 0;
        int total = 0;
        int cap = Math.max(1, Math.min(64, offer.maxQuantity()));
        for (int slot = 0; slot < player.getInventory().main.size() && total < cap; slot++) {
            ItemStack stack = player.getInventory().main.get(slot);
            if (!matches(player, stack, offer)) continue;
            total = Math.min(cap, total + stack.getCount());
        }
        return total;
    }

    public static synchronized NpcTradeEscrowResult escrow(
            ServerPlayerEntity player,
            NpcTradeOfferDefinition offer,
            int quantity
    ) {
        if (player == null || offer == null || !"sell".equals(offer.direction())) {
            return NpcTradeEscrowResult.failure("Invalid sell request.");
        }
        int requested = Math.max(1, Math.min(Math.max(1, offer.maxQuantity()), quantity));
        List<Removal> removals = new ArrayList<>();
        int remaining = requested;
        for (int slot = 0; slot < player.getInventory().main.size() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().main.get(slot);
            if (!matches(player, stack, offer)) continue;
            int take = Math.min(remaining, stack.getCount());
            removals.add(new Removal(slot, take));
            remaining -= take;
        }
        if (remaining > 0) {
            return NpcTradeEscrowResult.failure("You do not have enough matching items.");
        }

        List<NpcTradeEscrowStack> escrow = new ArrayList<>();
        var registries = player.getServer().getRegistryManager();
        String fingerprint = fingerprint(player, offer);
        for (Removal removal : removals) {
            ItemStack stack = player.getInventory().main.get(removal.slot());
            ItemStack removed = stack.copyWithCount(removal.count());
            escrow.add(NpcTradeEscrowStack.from(removed, registries, removal.slot(), fingerprint));
        }
        for (Removal removal : removals) {
            player.getInventory().main.get(removal.slot()).decrement(removal.count());
        }
        player.getInventory().markDirty();
        return NpcTradeEscrowResult.success(escrow, requested);
    }

    public static synchronized boolean restore(ServerPlayerEntity player, List<NpcTradeEscrowStack> escrow) {
        if (player == null || escrow == null || escrow.isEmpty()) return true;
        var registries = player.getServer().getRegistryManager();
        List<ItemStack> stacks = new ArrayList<>();
        for (NpcTradeEscrowStack stored : escrow) {
            ItemStack stack = stored.toStack(registries);
            if (!stack.isEmpty()) stacks.add(stack);
        }
        if (!canRestore(player, stacks)) return false;
        for (int index = 0; index < stacks.size(); index++) {
            ItemStack incoming = stacks.get(index).copy();
            NpcTradeEscrowStack stored = escrow.get(index);
            tryOriginalSlot(player, stored.sourceSlot(), incoming);
            if (!incoming.isEmpty()) player.getInventory().insertStack(incoming);
            if (!incoming.isEmpty()) return false;
        }
        player.getInventory().markDirty();
        return true;
    }

    static boolean matches(ServerPlayerEntity player, ItemStack stack, NpcTradeOfferDefinition offer) {
        if (stack == null || stack.isEmpty() || offer == null) return false;
        Identifier id = Identifier.tryParse(offer.itemId());
        if (id == null || !Registries.ITEM.containsId(id) || !stack.isOf(Registries.ITEM.get(id))) return false;
        boolean configuredComponents = "exact_components".equals(offer.componentPolicy())
                || "exact_stack".equals(offer.sellMatch());
        ItemStack expected = configuredComponents
                ? NpcTradeItemStacks.stack(player, NpcTradeDeliveryStack.from(offer, 1), 1)
                : new ItemStack(Registries.ITEM.get(id));
        if ("vanilla_only".equals(offer.componentPolicy())
                && !ItemStack.areItemsAndComponentsEqual(stack.copyWithCount(1), new ItemStack(Registries.ITEM.get(id)))) {
            return false;
        }
        if ("exact_components".equals(offer.componentPolicy())
                || "exact_stack".equals(offer.sellMatch())) {
            return ItemStack.areItemsAndComponentsEqual(stack.copyWithCount(1), expected);
        }
        return true;
    }

    private static boolean canRestore(ServerPlayerEntity player, List<ItemStack> stacks) {
        List<ItemStack> simulated = new ArrayList<>();
        player.getInventory().main.forEach(stack -> simulated.add(stack.copy()));
        for (ItemStack stack : stacks) {
            int remaining = stack.getCount();
            for (ItemStack existing : simulated) {
                if (remaining <= 0) break;
                if (existing.isEmpty()) continue;
                if (!ItemStack.areItemsAndComponentsEqual(existing, stack)) continue;
                int move = Math.min(remaining, existing.getMaxCount() - existing.getCount());
                if (move > 0) {
                    existing.increment(move);
                    remaining -= move;
                }
            }
            for (int index = 0; index < simulated.size() && remaining > 0; index++) {
                ItemStack existing = simulated.get(index);
                if (remaining <= 0) break;
                if (!existing.isEmpty()) continue;
                int move = Math.min(remaining, stack.getMaxCount());
                ItemStack copy = stack.copyWithCount(move);
                simulated.set(index, copy);
                remaining -= move;
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private static void tryOriginalSlot(ServerPlayerEntity player, int slot, ItemStack incoming) {
        if (slot < 0 || slot >= player.getInventory().main.size() || incoming.isEmpty()) return;
        ItemStack existing = player.getInventory().main.get(slot);
        if (existing.isEmpty()) {
            int move = Math.min(incoming.getCount(), incoming.getMaxCount());
            player.getInventory().main.set(slot, incoming.copyWithCount(move));
            incoming.decrement(move);
        } else if (ItemStack.areItemsAndComponentsEqual(existing, incoming)) {
            int move = Math.min(incoming.getCount(), existing.getMaxCount() - existing.getCount());
            if (move > 0) {
                existing.increment(move);
                incoming.decrement(move);
            }
        }
    }

    private static String fingerprint(ServerPlayerEntity player, NpcTradeOfferDefinition offer) {
        ItemStack stack = NpcTradeItemStacks.stack(player, NpcTradeDeliveryStack.from(offer, 1), 1);
        return offer.itemId() + ":" + stack.getComponents().hashCode();
    }

    private record Removal(int slot, int count) {
    }
}
