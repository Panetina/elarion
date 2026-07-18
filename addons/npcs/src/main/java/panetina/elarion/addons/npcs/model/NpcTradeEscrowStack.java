package panetina.elarion.addons.npcs.model;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public record NpcTradeEscrowStack(
        String itemId,
        int count,
        String customName,
        List<String> lore,
        List<NpcTradeEnchantmentDefinition> enchantments,
        int customModelData,
        String fingerprint,
        String stackNbt,
        int sourceSlot,
        String sourceLabel
) {
    public NpcTradeEscrowStack(
            String itemId,
            int count,
            String customName,
            List<String> lore,
            List<NpcTradeEnchantmentDefinition> enchantments,
            int customModelData,
            String fingerprint
    ) {
        this(itemId, count, customName, lore, enchantments, customModelData, fingerprint, "", -1, "");
    }

    public NpcTradeEscrowStack {
        itemId = itemId == null ? "" : itemId.trim();
        count = Math.max(1, Math.min(4096, count));
        customName = customName == null ? "" : customName.trim();
        lore = lore == null ? List.of() : lore.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
        enchantments = enchantments == null ? List.of() : List.copyOf(enchantments);
        customModelData = Math.max(0, customModelData);
        fingerprint = fingerprint == null ? "" : fingerprint.trim();
        stackNbt = stackNbt == null ? "" : stackNbt.trim();
        sourceSlot = Math.max(-1, sourceSlot);
        sourceLabel = sourceLabel == null ? "" : sourceLabel.trim();
    }

    public static NpcTradeEscrowStack from(
            ItemStack stack,
            RegistryWrapper.WrapperLookup registries,
            int sourceSlot,
            String fingerprint
    ) {
        if (stack == null || stack.isEmpty()) throw new IllegalArgumentException("Escrow stack cannot be empty");
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return new NpcTradeEscrowStack(
                id.toString(),
                stack.getCount(),
                "",
                List.of(),
                List.of(),
                0,
                fingerprint == null || fingerprint.isBlank() ? id + ":" + stack.getComponents().hashCode() : fingerprint,
                encode(stack, registries),
                sourceSlot,
                sourceSlot >= 0 ? "Inventory slot " + (sourceSlot + 1) : "Inventory");
    }

    public ItemStack toStack(RegistryWrapper.WrapperLookup registries) {
        if (!stackNbt.isBlank()) {
            try {
                byte[] bytes = Base64.getDecoder().decode(stackNbt);
                NbtCompound root = NbtIo.readCompressed(
                        new ByteArrayInputStream(bytes), NbtSizeTracker.ofUnlimitedBytes());
                return ItemStack.fromNbt(registries, root.get("stack")).orElse(ItemStack.EMPTY);
            } catch (IOException | IllegalArgumentException exception) {
                return ItemStack.EMPTY;
            }
        }
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || count < 1 || !Registries.ITEM.containsId(id)) return ItemStack.EMPTY;
        Item item = Registries.ITEM.get(id);
        return new ItemStack(item, count);
    }

    private static String encode(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        try {
            NbtCompound root = new NbtCompound();
            root.put("stack", stack.encode(registries));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize NPC trade escrow stack", exception);
        }
    }
}
