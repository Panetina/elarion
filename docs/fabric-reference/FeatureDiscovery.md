# Fabric Feature Discovery Guide

Use this file when you know the goal but not the Fabric/Elarion entry point.

## Register A Block

Goal: Add a new block.
Recommended Fabric approach: `Registry.register(Registries.BLOCK, id, block)` plus a matching item if needed.
Important classes: `Block`, `Registry`, `Registries`, `Identifier`.
Example files: `addons/offerings/.../OfferingsBlocks.java`.
Elarion notes: Add to an addon creative tab and keep visible names in identity/config.
Avoid: registering during gameplay.

## Register An Item

Goal: Add a new item.
Recommended Fabric approach: Register an `Item` under a stable ID and add it to a creative tab.
Important classes: `Item`, `Item.Settings`, `Registries.ITEM`.
Example files: `addons/economy/.../EconomyItems.java`.
Elarion notes: Use generic IDs for configurable identity, e.g. `currency`.
Avoid: lore terms as registry IDs.

## Register A Block Entity

Goal: Store or render per-block local state.
Recommended Fabric approach: Register a `BlockEntityType` and use server-side validation.
Important classes: `BlockEntity`, `BlockEntityType`, `BlockWithEntity`.
Example files: current Shrine uses anchors instead of block entities.
Elarion notes: Use block entities only for local state; canonical project state belongs in world storage.
Avoid: storing full project/history data in block entity NBT.

## Create A Command

Goal: Add admin or player command behavior.
Recommended Fabric approach: Brigadier via command registration callback or Core command registry.
Important classes: `CommandManager`, `ServerCommandSource`, `LiteralArgumentBuilder`.
Example files: `platform/core/.../command`, `addons/npcs/.../NpcCommands.java`.
Elarion notes: Prefer `/e <system>` for OP systems.
Avoid: new root commands unless player-facing and justified.

## Send Server-To-Client Packet

Goal: Open/update UI or sync state.
Recommended Fabric approach: `CustomPayload`, `PayloadTypeRegistry.playS2C`, `ServerPlayNetworking.send`.
Important classes: `CustomPayload`, `PacketCodec`, `ServerPlayNetworking`.
Example files: `NpcDialogueOpenPayload`, `ShrineUiOpenPayload`, `UiThemeSyncPayload`.
Elarion notes: Send snapshots, not definitions.
Avoid: unbounded payloads.

## Send Client-To-Server Packet

Goal: Send a player action.
Recommended Fabric approach: C2S payload and server receiver.
Important classes: `ClientPlayNetworking`, `ServerPlayNetworking`, `PayloadTypeRegistry.playC2S`.
Example files: `NpcDialogueSelectPayload`, `ShrineContributionSubmitPayload`.
Elarion notes: Treat packet data as untrusted.
Avoid: client-authoritative mutation.

## Sync GUI Data

Goal: Keep a custom screen current.
Recommended Fabric approach: server sends authoritative snapshot after each mutation.
Important classes: custom payload records and client screen update methods.
Example files: `NpcDialogueScreen.updateDialogue`, `ShrineOfFoundationScreen.applySnapshot`.
Elarion notes: Preserve local tab/scroll state only when safe.
Avoid: client calculating progress.

## Store Custom Player Data

Goal: Persist player-owned state.
Recommended Fabric approach: use Core citizen services when it is identity/civic state; addon storage when addon-owned.
Important classes: `CitizenRecord`, `CitizenStorage`, addon storage classes.
Example files: `CitizenService`, `EconomyStorage`.
Elarion notes: Core owns identity, Realm, titles, status, and activity.
Avoid: duplicate player managers.

## Store World-Level Persistent Data

Goal: Persist server/addon state.
Recommended Fabric approach: `world/elarion/addon-state/<addon>/...` with atomic JSON.
Important classes: `JsonStateStorage`, addon storage wrappers.
Example files: `OfferingStorage`, `NpcPlacementStorage`, `EconomyStorage`.
Elarion notes: Separate editable definitions from runtime state.
Avoid: config files as runtime state.

## Create A Screen Handler

Goal: Inventory-backed UI.
Recommended Fabric approach: `ScreenHandler` and `HandledScreen`.
Important classes: `ScreenHandler`, `Slot`, `HandledScreen`.
Example files: not yet central in Elarion.
Elarion notes: Use for future barter/trade/escrow.
Avoid: custom packets for real item-slot authority.

## Create A Custom Screen

Goal: Non-inventory UI.
Recommended Fabric approach: extend `Screen`, render via `DrawContext`, sync through payloads.
Important classes: `Screen`, `DrawContext`.
Example files: `NpcDialogueScreen`, `ShrineOfFoundationScreen`.
Elarion notes: Use `panetina.elarion.core.client.ui` primitives.
Avoid: duplicating theme/button/list renderers.

## Add Tooltip Logic

Goal: Show item or custom hover information.
Recommended Fabric approach: use `DrawContext.drawTooltip` or item tooltip events where needed.
Important classes: `DrawContext`, `Text`, `ItemStack`.
Example files: Shrine reward hover rendering.
Elarion notes: Use native item tooltips for actual item rewards.
Avoid: fake item tooltips that disagree with reward actions.

## Add Entity Interaction

Goal: React to right-click or entity use.
Recommended Fabric approach: entity interaction callbacks or entity override methods.
Important classes: `Entity`, `ServerPlayerEntity`, interaction callbacks.
Example files: NPC interaction service and Shrine `UseBlockCallback`.
Elarion notes: server must validate range/current state.
Avoid: client-only open logic for gameplay screens.

## Add Custom Renderer

Goal: Render entity/block/HUD/custom GUI.
Recommended Fabric approach: client initializer registers renderer.
Important classes: `EntityRenderer`, `EntityRendererFactory`, `DrawContext`.
Example files: `ElarionNpcEntityRenderer`, Core UI renderer.
Elarion notes: render from synced snapshots and client caches.
Avoid: server state access in renderer.

## Add Datagen

Goal: Generate assets/data.
Recommended Fabric approach: Fabric datagen providers.
Important classes: `FabricDataGenerator`, provider classes.
Example files: Fabric API datagen reference under `external/fabric-api`.
Elarion notes: good for models/tags/lang; not gameplay definitions.
Avoid: generated runtime config.

## Add Mixin

Goal: Patch behavior not exposed by API.
Recommended Fabric approach: narrow mixin plus service delegation.
Important classes: mixin target and generated refmap.
Example files: names/titles/realms/worlds mixin packages.
Elarion notes: document why an event was insufficient.
Avoid: broad hot-path injections.

## Use Access Widener

Goal: Access otherwise inaccessible Minecraft members.
Recommended Fabric approach: access widener or accessor mixin.
Important classes: target Yarn names.
Example files: check existing `fabric.mod.json` access widener entries before adding.
Elarion notes: prefer public API first.
Avoid: widening more than needed.

## Add Config

Goal: Add editable server owner definitions/settings.
Recommended Fabric approach: Elarion config loader with validation and defaults.
Important classes: Core/addon config loaders and validators.
Example files: `CoreConfigManager`, `OfferingConfigLoader`, `NpcConfigLoader`.
Elarion notes: definitions in config; mutable state in world storage.
Avoid: parsing YAML during gameplay.

## Add Permissions-Like Logic

Goal: Gate commands/actions/features.
Recommended Fabric approach: OP levels for admin commands; Core abilities for gameplay.
Important classes: `ServerCommandSource`, `AbilityService`.
Example files: ability commands, NPC required abilities.
Elarion notes: abilities are Core-owned.
Avoid: addon-local permission systems unless clearly scoped.

## Integrate With Existing Elarion Systems

Goal: Add a feature without duplicate ownership.
Recommended Fabric approach: use `ElarionApi` facades, registries, and addon APIs.
Important classes: `ElarionApi`, `ElarionAddon`, registries.
Example files: Economy NPC actions, Offering registry actions.
Elarion notes: Core owns truth; addons own feature state.
Avoid: new global managers that duplicate citizens, Realms, history, rewards, GUI theme, or networking patterns.
