# Event Mapping

NeoForge feature: event bus events and subscriber methods.

Fabric equivalent: Fabric API callbacks, lifecycle events, command callbacks, networking callbacks, or narrow mixins.

Minecraft classes: depends on event target; common classes include `MinecraftServer`, `ServerPlayerEntity`, `World`, `Entity`, `BlockState`.

Fabric API classes: `ServerLifecycleEvents`, `UseBlockCallback`, `PlayerBlockBreakEvents`, `CommandRegistrationCallback`.

Porting difficulty: Medium.

Notes:

- Translate one event at a time into the closest Fabric callback.
- If Fabric has no callback, add a narrow mixin and delegate to an Elarion service.
- For cross-addon behavior, prefer Core registries or explicit addon APIs.

Example source locations:

- NeoForge: `external/neoforge/src/main/java/net/neoforged/neoforge/event`
- Fabric: `external/fabric-api/fabric-lifecycle-events-v1`
- Elarion: `addons/offerings/src/main/java/panetina/elarion/addons/offerings/ElarionOfferingsAddon.java`
