# Datagen Mapping

NeoForge feature: NeoForge data providers.

Fabric equivalent: Fabric datagen providers.

Minecraft classes: data generator/provider classes, registry wrappers.

Fabric API classes: `FabricDataGenerator`, `FabricDataOutput`.

Porting difficulty: Medium.

Notes:

- Convert assets/tags/recipes/loot providers to Fabric providers.
- Keep Elarion gameplay config hand-editable and validated.
- Use generated outputs only where deterministic.

Example source locations:

- NeoForge: datagen references in `external/neoforge`.
- Fabric: `external/fabric-api/fabric-data-generation-api-v1`
- Elarion: current resources under addon `src/main/resources`.
