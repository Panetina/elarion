# Fabric Entities

## Purpose

Create and register custom entities with controlled behavior, persistence, networking, and rendering.

## Core Minecraft Classes

- `net.minecraft.entity.Entity`
- `net.minecraft.entity.EntityType`
- `net.minecraft.entity.SpawnGroup`
- `net.minecraft.entity.data.DataTracker`
- `net.minecraft.nbt.NbtCompound`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder`
- `net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry`

## Common Patterns

- Keep entity NBT compact.
- Store canonical state elsewhere when the entity is only a world anchor.
- Register a dedicated renderer for dedicated entity types.
- Reconcile world entities from canonical state on startup/repair.

## Anti-patterns

- Piggybacking on vanilla entities for long-term custom rendering.
- Storing full config or project state inside entity NBT.
- Running global entity scans every tick.
- Letting client-side render state become gameplay state.

## Example Source Locations

- Elarion NPC entity: `addons/npcs/src/main/java/panetina/elarion/addons/npcs/entity`
- Elarion NPC placement storage: `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage`
- Fabric API object builder: `external/fabric-api/fabric-object-builder-api-v1`

## Elarion Use Cases

- Static NPCs.
- Future projected hologram anchors if needed.
- Future Atlas markers should prefer synchronized data, not marker entities, unless in-world interaction is required.
