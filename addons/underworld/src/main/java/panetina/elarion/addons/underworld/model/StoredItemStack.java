package panetina.elarion.addons.underworld.model;

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

public final class StoredItemStack {
    public static final String SOURCE_INVENTORY = "inventory";
    public static final String SOURCE_ARMOR = "armor";
    public static final String SOURCE_OFFHAND = "offhand";
    public static final String SOURCE_ID_INVENTORY = "minecraft:inventory";
    public static final String SOURCE_ID_ARMOR = "minecraft:armor";
    public static final String SOURCE_ID_OFFHAND = "minecraft:offhand";

    public String itemId = "";
    public int count;
    public String stackNbt = "";
    public String sourceType = "";
    public String sourceId = "";
    public String sourceLabel = "";
    public int slotIndex = -1;
    public String equipmentSlot = "";

    public StoredItemStack() {
    }

    public StoredItemStack(String itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }

    public static StoredItemStack from(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        return from(stack, registries, SOURCE_INVENTORY, -1, "");
    }

    public static StoredItemStack fromInventory(ItemStack stack, RegistryWrapper.WrapperLookup registries, int slotIndex) {
        return from(stack, registries, SOURCE_INVENTORY, slotIndex, "");
    }

    public static StoredItemStack fromArmor(ItemStack stack, RegistryWrapper.WrapperLookup registries, String equipmentSlot) {
        return from(stack, registries, SOURCE_ARMOR, -1, equipmentSlot);
    }

    public static StoredItemStack fromOffhand(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        return from(stack, registries, SOURCE_OFFHAND, -1, "offhand");
    }

    public static StoredItemStack from(
            ItemStack stack,
            RegistryWrapper.WrapperLookup registries,
            String sourceType,
            int slotIndex,
            String equipmentSlot
    ) {
        Identifier id = Registries.ITEM.getId(stack.getItem());
        StoredItemStack stored = new StoredItemStack(id.toString(), stack.getCount());
        stored.sourceType = sourceType == null ? "" : sourceType;
        stored.sourceId = defaultSourceId(stored.sourceType);
        stored.sourceLabel = defaultSourceLabel(stored.sourceType, slotIndex, equipmentSlot);
        stored.slotIndex = slotIndex;
        stored.equipmentSlot = equipmentSlot == null ? "" : equipmentSlot;
        try {
            NbtCompound root = new NbtCompound();
            root.put("stack", stack.encode(registries));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, output);
            stored.stackNbt = Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize item stack " + id, exception);
        }
        return stored;
    }

    public static String defaultSourceId(String sourceType) {
        return switch (sourceType == null ? "" : sourceType) {
            case SOURCE_INVENTORY -> SOURCE_ID_INVENTORY;
            case SOURCE_ARMOR -> SOURCE_ID_ARMOR;
            case SOURCE_OFFHAND -> SOURCE_ID_OFFHAND;
            default -> "";
        };
    }

    public static String defaultSourceLabel(String sourceType, int slotIndex, String equipmentSlot) {
        return switch (sourceType == null ? "" : sourceType) {
            case SOURCE_INVENTORY -> slotIndex >= 0 && slotIndex < 9
                    ? "Hotbar slot " + (slotIndex + 1)
                    : slotIndex >= 0 ? "Inventory slot " + (slotIndex + 1) : "Inventory";
            case SOURCE_ARMOR -> {
                String slot = equipmentSlot == null || equipmentSlot.isBlank() ? "Armor" : equipmentSlot;
                yield "Armor: " + slot;
            }
            case SOURCE_OFFHAND -> "Offhand";
            default -> "Inventory fallback";
        };
    }

    public ItemStack toStack(RegistryWrapper.WrapperLookup registries) {
        if (stackNbt != null && !stackNbt.isBlank()) {
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
        if (id == null || count <= 0) return ItemStack.EMPTY;
        Item item = Registries.ITEM.get(id);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, count);
    }
}
