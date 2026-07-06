# Fabric Components And Attachments

## Purpose

Attach structured data to objects or use Minecraft components for item/entity behavior where appropriate.

## Core Minecraft Classes

- `net.minecraft.component.DataComponentTypes`
- `net.minecraft.item.ItemStack`
- `net.minecraft.nbt.NbtCompound`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry`
- `net.fabricmc.fabric.api.attachment.v1.AttachmentType`

## Common Patterns

- Use item components for item-local properties.
- Use Fabric attachments for small local state tied to an object lifecycle.
- Use Elarion world storage for canonical server state.

## Anti-patterns

- Storing canonical citizens, Realms, wallets, history, or rewards in attachments.
- Using NBT blobs as an unvalidated config system.
- Attaching large lists or histories to entities/block entities.

## Example Source Locations

- Fabric API: `external/fabric-api/fabric-data-attachment-api-v1`
- Elarion storage: `platform/core/src/main/java/panetina/elarion/core/storage`
- Elarion Economy state: `addons/economy/src/main/java/panetina/elarion/addons/economy/storage`

## Elarion Use Cases

- Item-local future ticket data may use components.
- Canonical wallets, citizens, ledgers, and Offering progress should stay in world storage.
- Dedicated NPC entity should store only a compact placement link, not dialogue or wallet state.
