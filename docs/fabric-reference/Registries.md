# Fabric Registries

## Purpose

Register Minecraft content with stable IDs: blocks, items, item groups, entities, screen handlers, sounds, particles, and other registry-backed types.

## Core Minecraft Classes

- `net.minecraft.registry.Registry`
- `net.minecraft.registry.Registries`
- `net.minecraft.util.Identifier`
- `net.minecraft.item.Item`
- `net.minecraft.block.Block`
- `net.minecraft.entity.EntityType`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup`
- `net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder`
- `net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings`

## Common Patterns

- Declare stable `Identifier` constants.
- Register content in the owning module initializer.
- Keep registry IDs generic and stable; make visible names configurable.
- Put addon-owned items/blocks in addon-owned creative tabs.

## Anti-patterns

- Registering content lazily during gameplay.
- Using lore/display names as registry IDs.
- Duplicating a registry object in multiple addons.
- Hard-coding Realm/currency/server identity into IDs.

## Example Source Locations

- Elarion: `addons/economy/src/main/java/panetina/elarion/addons/economy/EconomyItems.java`
- Elarion: `addons/offerings/src/main/java/panetina/elarion/addons/offerings/OfferingsBlocks.java`
- Elarion: `addons/npcs/src/main/java/panetina/elarion/addons/npcs/entity/ElarionNpcEntities.java`
- Fabric API: `external/fabric-api/fabric-item-group-api-v1`
- Fabric docs: `https://docs.fabricmc.net/1.21.1/develop/`

## Elarion Use Cases

- Currency item: `elarion:currency`.
- Shrine of Foundation block/item.
- Dedicated static NPC entity.
- Future Atlas items, quest tokens, portal tickets, and service items.
