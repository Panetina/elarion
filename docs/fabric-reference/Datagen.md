# Fabric Datagen

## Purpose

Generate deterministic assets and data: models, blockstates, item tags, recipes, loot tables, translations, and advancements.

## Core Minecraft Classes

- `net.minecraft.data.DataGenerator`
- `net.minecraft.data.DataProvider`
- `net.minecraft.registry.RegistryWrapper`

## Core Fabric API Classes

- `net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator`
- `net.fabricmc.fabric.api.datagen.v1.FabricDataOutput`
- Fabric datagen provider classes under `fabric-data-generation-api-v1`

## Common Patterns

- Keep generated output deterministic.
- Use datagen for assets/tags/recipes, not mutable runtime state.
- Validate generated files in tests or build checks when they become important.

## Anti-patterns

- Generating gameplay config that server owners are expected to edit.
- Committing noisy generated changes without source changes.
- Relying on datagen to create runtime world state.

## Example Source Locations

- Fabric API: `external/fabric-api/fabric-data-generation-api-v1`
- Fabric docs datagen section: `https://docs.fabricmc.net/1.21.1/develop/`
- Elarion assets: `addons/economy/src/main/resources/assets/elarion`

## Elarion Use Cases

- Future generation of item/block models.
- Tags for Offering item requirements.
- Translations for item groups and UI labels.
- Recipes only when gameplay design explicitly allows craftable content.
