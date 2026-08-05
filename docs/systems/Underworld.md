# Underworld System

Purpose: capture Elarion deaths into corpse recovery, Underworld timers, Soul
Fractures, Core-owned True Death character lifecycle integration, and persisted
Underworld moderation banishments.

## Main Classes

- `addons/underworld/.../ElarionUnderworldAddon.java`
- `addons/underworld/.../service/UnderworldService.java`
- `addons/underworld/.../service/UnderworldProfileContributor.java`
- `addons/underworld/.../model/CorpseRecord.java`
- `addons/underworld/.../model/UnderworldSession.java`
- `addons/underworld/.../model/SoulState.java`
- `addons/underworld/.../model/BanishmentRecord.java`
- `platform/core/.../service/PlayerRestrictionService.java`

## Entry Points

- Fabric `ServerLivingEntityEvents.AFTER_DAMAGE` tracks combat tags.
- Fabric `ServerLivingEntityEvents.ALLOW_DEATH` captures fatal deaths and moves
  inventory into corpse state before vanilla drops occur.
- Fabric `ServerPlayerEvents.AFTER_RESPAWN` queues the Underworld teleport after
  normal respawn routing.
- Fabric `UseBlockCallback` opens the server-authoritative tomb recovery UI.

## Commands

- `/e death ...`
- `/e test death ...`
- `/banish ...`
- `/unbanish ...`

See `docs/commands.md` and `docs/test-commands.md`.

## Storage / Persistence

- Config: `config/elarion/addons/underworld/underworld.yml`
- Runtime: `world/elarion/addon-state/underworld/state.json`
- True Death metadata: `world/elarion/addon-state/underworld/true-death-archive/`

`/e death reload` reparses `underworld.yml` into a new config snapshot. If the
reload file is malformed, the Underworld service keeps the previous valid
snapshot instead of falling back to defaults. First startup without a usable
config still falls back to defaults after writing the default file when needed.

Runtime state uses schema version `3`. Versionless, schema-1, and schema-2 legacy state
migrate forward with empty inventory-boundary maps; unsupported versions fail closed
and the shared storage layer quarantines
the unreadable snapshot before fallback state can be saved.

Supported snapshots normalize null collections, nested item lists, and nullable
record fields before bind. Structurally unusable null/blank rows are discarded
without losing valid corpse, session, soul, vault, banishment, or inventory
state from the same parsed snapshot.

## Dependencies

- Core: citizens, realm spawns, task queue, domain events, root command
  registration, global interaction gates, and player/account restrictions.
- Core progression stats: Underworld increments
  `UnderworldService.LIFETIME_DEATHS_STAT` at authoritative death-capture
  points so Character Menu can render a bounded self/admin lifetime death
  summary without scanning Underworld runtime storage.
- Economy: not directly owned; only physical currency item/tag selection is used
  for PvP loot.
- Portals: consumes Core restrictions so souls cannot travel through portals.
- Guilds/Core chat/private messages: consume Core restrictions so souls cannot
  chat.

## Extension Points

- Core `PlayerRestrictionService` can be reused by Jail, cutscenes, events, or
  future status effects that temporarily block chat/travel.
- Banishment contributes a UUID-only `queued_admission` denial. A future Core
  queue must consult it whenever anyone is queued and reject or disconnect the
  banished account until both capacity is free and the queue is empty. The
  queue remains Core-owned; Underworld does not invent queue state.
- Underworld domain events are the integration point for Government succession,
  Chronicle, newspapers, NPC rumors, and website notifications.
- `UnderworldProfileContributor` contributes the `underworld/deaths` Citizen
  Ledger summary field with `SELF` visibility. It reads the Core per-player
  stat counter only; it does not enumerate corpses, sessions, vaults, or
  history files during profile snapshot creation.
- The compact HUD timer is visible only while a player is bound to the
  Underworld. It intentionally does not show Soul Fracture marks; those belong
  in identity/tablist presentation.
- Bound players in `elarion:underworld` receive client-only Soul Sight: hidden
  nameplates and texture-independent solid-white full-bright opaque silhouettes for
  other players. Underworld has no post processor, fog override, screen grade,
  blur, or aura pass; shader packs retain the complete framebuffer pipeline.
- Banished players receive a reason/sentence HUD. Other clients render them
  from an opaque flat-red texture as solid full-bright silhouettes. UUID
  appearance deltas are sent only on join, sentence mutation, or expiry;
  rendering does not scan persisted banishment state.
- Client-only LambDynamicLights 4.8.10 provides bounded luminance 6/15 for the
  visible white/red spectral states using its entity-light registration API,
  adaptive ticking, background sleep, and culled chunk rebuild scheduler. It
  is neither installed nor required on the server.
- Core's interaction gates enforce movement-only banishment before NPC,
  Shrine, block, item, combat, Portal, and future skyblock callbacks execute.
  Timed expiry uses a deadline queue capped at 64 records per second.
- Underworld owns a persisted vanilla-inventory boundary: a player's Living
  inventory is held apart from their Afterlife inventory. Normal death keeps
  Living items in the corpse; administrative transfers and banishments preserve
  their Living snapshot until return. Afterlife items and experience are saved
  on logout/shutdown, restored only on Afterlife entry, and never merged into
  corpse recovery or the Living inventory.
- Underworld PvP is denied server-side. Banished players additionally cannot
  collect item entities or experience orbs, so movement-only punishment cannot
  be bypassed through passive pickup.
- Once a corpse's protected and PvP loot lists are empty, the corpse record is
  removed, saved, and its tomb blocks are removed so interaction cannot duplicate
  already recovered items.
- Tomb protection lasts while the victim's Underworld session exists. Returning
  to the living world starts public looting and the post-protection decay timer.
  When the decay timer finishes, the physical tomb is destroyed. Decayed
  leftovers move to the owner recovery vault instead of dropping in the world.

## Tomb Blocks

- Underworld registers `elarion_underworld:tomb` as a protected 1x1x2 tomb
  structure with a lower block entity holding the corpse id.
- The lower block entity also syncs display-only owner/access/timer/item-count
  data to the client floating tomb label; persisted corpse records remain
  canonical.
- The lower block renders one of three Nexo tombstone models imported from
  `C:\Users\Panyel\Desktop\Tombstones\Nexo\pack`; the upper block is invisible
  and reserves vertical clearance.
- All three variants occupy `1 x 2 x 1` blocks. Their visual heights are about
  1.06, 1.13, and 1.88 blocks respectively.
- Variant selection is deterministic per victim Realm, with corpse id fallback
  when no Realm can be resolved.
- Startup reconciliation restores missing tombs from corpse state.
- Startup reconciliation is the only ordinary full-corpse pass. Runtime tomb
  refreshes use a deduplicated queue capped at 64 updates per second, and
  corpse decay uses a due-time priority queue capped at 64 expirations per
  second. Administrative reset scans remain explicit one-time operations.
- Corpse items persist source metadata with a generic source id and label in
  addition to vanilla inventory, armor, and offhand coordinates. This keeps the
  grave recovery UI ready for future backpack, trinket, skin, accessory, or
  addon-owned slot families while vanilla recovery remains the current restore
  implementation.
- Grave owner text is nickname-first: Core citizen nickname, then last known
  username, then Minecraft profile name fallback at capture time.
- Grave Recovery uses the shared civic popup shell with a framed status/body
  panel and framed contents grid. Render, scroll, and click positions are
  derived from one client layout snapshot; server recovery remains authoritative.
  Grave item icons use the shared Core item-slot layout so native item rendering
  and tooltip hitboxes match the actual icon area.

## Risks

- Full item components are persisted as compressed NBT with legacy fallback.
- True Death cleanup is restart-safe but still needs multiplayer/GameTest coverage.
