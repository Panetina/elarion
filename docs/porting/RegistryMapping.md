# Registry Mapping

NeoForge feature: deferred registers and registry events.

Fabric equivalent: direct `Registry.register(...)` calls during initialization.

Minecraft classes: `Registry`, `Registries`, `Identifier`, `Item`, `Block`, `EntityType`.

Fabric API classes: object builder APIs, item group API, screen handler API.

Porting difficulty: Low.

Notes:

- Preserve content IDs when porting an existing mod where compatibility matters.
- In Elarion, keep registry IDs generic and visible names configurable.
- Register content in the owning addon, not Core, unless it is truly shared infrastructure.

Example source locations:

- NeoForge: registry references under `external/neoforge/src/main/java/net/neoforged/neoforge/registries`
- Fabric: `external/fabric-api/fabric-content-registries-v0`
- Elarion: `addons/economy/.../EconomyItems.java`, `addons/offerings/.../OfferingsBlocks.java`
