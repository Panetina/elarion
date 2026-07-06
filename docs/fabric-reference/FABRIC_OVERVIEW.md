# Fabric 1.21.1 Development Overview

Fabric is Elarion's primary platform. Use the local checkouts in `external/` as reference code, but keep Elarion's architecture centered on Core plus addons.

## Registry Patterns

- Register vanilla registry content with `Registry.register(...)` and stable `Identifier` values.
- Register blocks, items, item groups, entities, screen handlers, and payload IDs during module initialization.
- Keep registry IDs generic when the visible name is configurable. Example: use `elarion:currency`, not `elarion:sigil`.
- Put new items in an Elarion creative tab so server owners can find them quickly.
- Avoid static initialization that depends on Core runtime state. Registry constants are fine; gameplay state belongs in services.

## Networking Patterns

- Use Fabric networking payloads:
  - `CustomPayload`
  - `PayloadTypeRegistry.playS2C()`
  - `PayloadTypeRegistry.playC2S()`
  - `ServerPlayNetworking`
  - `ClientPlayNetworking`
- Client packets should carry intent only. The server must revalidate session, range, permissions, ownership, and current state.
- Prefer authoritative server snapshots after mutations. Elarion already uses this for NPC dialogue and Shrine screens.
- Keep payloads small and typed. Do not send full definitions when an ID and snapshot fields are enough.

## ScreenHandler Patterns

- Use `ScreenHandler` for inventory-backed flows where the player moves real item stacks.
- Use custom client screens plus typed networking for read-only dashboards, dialogue, project views, and service prompts.
- For future trade/barter screens, prefer a server-owned `ScreenHandler` when two inventories or escrowed item slots are involved.
- For visual-only screens, use the shared Elarion UI primitives in `panetina.elarion.core.client.ui`.

## Components And Data Attachment Alternatives

- Fabric has Data Attachment API support in `fabric-data-attachment-api-v1`, visible in `external/fabric-api`.
- Elarion should still prefer its current storage model for canonical gameplay state:
  - editable definitions in `config/elarion/`
  - mutable runtime state in `world/elarion/`
  - compact IDs in entities/block entities
- Use attachments only for local, bounded state tied naturally to an object lifecycle. Do not use attachments to duplicate Core citizens, Realms, history, rewards, or Economy balances.

## Datagen Notes

- Fabric datagen is provided by `fabric-data-generation-api-v1`.
- Use datagen for generated assets, tags, models, language files, recipes, and loot tables when content volume grows.
- Keep generated output deterministic. Do not mix generated files with hand-authored runtime config.
- For Elarion, datagen is appropriate for item/block assets and tags; gameplay definitions should remain in validated YAML/JSON config.

## Common Fabric APIs Used By Mods

- `fabric-api-base`: core Fabric API primitives.
- `fabric-command-api-v2`: Brigadier command registration.
- `fabric-networking-api-v1`: C2S/S2C payloads.
- `fabric-lifecycle-events-v1`: server start/stop/join lifecycle hooks.
- `fabric-events-interaction-v0`: block/entity interaction events.
- `fabric-object-builder-api-v1`: entity/block/item builders.
- `fabric-item-group-api-v1`: creative tabs.
- `fabric-screen-handler-api-v1`: inventory/menu-backed UI.
- `fabric-data-generation-api-v1`: asset/data generation.
- `fabric-data-attachment-api-v1`: optional object-attached state.
- `fabric-gametest-api-v1`: in-game integration tests.

## Local References

- Fabric API: `external/fabric-api`
- Fabric Loom: `external/fabric-loom`
- Yarn mappings: `external/yarn`
- Example mod: `external/example-mods/fabric-example-mod`

The local reference branches may not exactly match Minecraft 1.21.1. Check `docs/REFERENCE_SETUP_REPORT.md` before copying patterns.
