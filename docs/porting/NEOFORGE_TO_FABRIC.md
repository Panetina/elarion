# NeoForge To Fabric Porting Map

Fabric is the implementation target. NeoForge code is reference material for understanding behavior, content, and data models.

## Events

NeoForge:

- Event bus classes and subscribers.
- Broad lifecycle/gameplay events.

Fabric:

- Fabric API callbacks where available.
- `ServerLifecycleEvents`, player/block interaction callbacks, command callbacks, networking callbacks.
- Mixins only for missing hooks or precise behavior changes.

Elarion rule: if multiple addons need the hook, expose a Core registry/event or addon API instead of duplicating listeners.

## Registries

NeoForge:

- Deferred registers and registry event flow.

Fabric:

- `Registry.register(...)`
- `Identifier.of(namespace, path)`
- module initializer registration.

Elarion rule: keep visible names configurable through `server_identity.yml`; registry IDs should be stable and generic.

## Networking

NeoForge:

- Channel/payload registration with NeoForge helpers.

Fabric:

- `CustomPayload`
- `PacketCodec`
- `PayloadTypeRegistry.playS2C()/playC2S()`
- `ServerPlayNetworking` / `ClientPlayNetworking`

Elarion rule: packets carry intent or snapshots; the server owns validation and mutation.

## Menus

NeoForge:

- `MenuType`, container menus, screen registration.

Fabric:

- `ScreenHandlerType`
- `ScreenHandler`
- handled client screens.

Elarion rule: use handled screens for real inventories/trade slots; use Elarion UI primitives for dashboards, dialogue, project views, and read-only/admin screens.

## Data Attachments

NeoForge:

- Built-in attachment model.

Fabric:

- `fabric-data-attachment-api-v1` where appropriate.
- Elarion JSON/world storage for canonical state.

Elarion rule: attachments must not duplicate Core ownership.

## Configs

NeoForge:

- Mod config specs and generated config files.

Fabric/Elarion:

- Explicit YAML/JSON config loaders.
- Startup/reload validation.
- Immutable definition caches.
- Runtime state stored separately under `world/elarion/`.

## Datagen

NeoForge:

- NeoForge data providers.

Fabric:

- `fabric-data-generation-api-v1`.

Elarion rule: datagen is for assets/tags/recipes/loot, not mutable gameplay state.

## Capabilities

NeoForge:

- Block/entity/item capabilities.

Fabric:

- Lookup API, Transfer API, Data Attachments, direct interfaces, or addon APIs depending on use.

Elarion rule: for shared gameplay facts, prefer Core or addon APIs. For storage/inventory integration, consider Fabric Lookup/Transfer APIs.

## Access Transformers

NeoForge:

- Access transformers widen access at build/runtime.

Fabric:

- Access wideners.
- Mixins/invokers/accessors.

Elarion rule: use access widening only when a stable public API is not available and the target is narrow.

## Mixins

NeoForge:

- Often fewer mixins because more events/hooks exist.

Fabric:

- Mixins are normal, but must be narrow and documented.

Elarion rule: prefer Fabric API event hooks first; use mixins for exact gaps only. Avoid gameplay ownership inside mixins.
