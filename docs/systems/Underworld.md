# Underworld System

Purpose: capture Elarion deaths into corpse recovery, Underworld timers, Soul
Fractures, and Core-owned True Death character lifecycle integration.

## Main Classes

- `addons/underworld/.../ElarionUnderworldAddon.java`
- `addons/underworld/.../service/UnderworldService.java`
- `addons/underworld/.../model/CorpseRecord.java`
- `addons/underworld/.../model/UnderworldSession.java`
- `addons/underworld/.../model/SoulState.java`
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

See `docs/commands.md` and `docs/test-commands.md`.

## Storage / Persistence

- Config: `config/elarion/addons/underworld/underworld.yml`
- Runtime: `world/elarion/addon-state/underworld/state.json`
- True Death metadata: `world/elarion/addon-state/underworld/true-death-archive/`

## Dependencies

- Core: citizens, realm spawns, task queue, domain events, player restrictions.
- Economy: not directly owned; only physical currency item/tag selection is used
  for PvP loot.
- Portals: consumes Core restrictions so souls cannot travel through portals.
- Groups/Core chat/private messages: consume Core restrictions so souls cannot
  chat.

## Extension Points

- Core `PlayerRestrictionService` can be reused by Jail, cutscenes, events, or
  future status effects that temporarily block chat/travel.
- Underworld domain events are the integration point for Government succession,
  Chronicle, newspapers, NPC rumors, and website notifications.
- The compact HUD timer is visible only while a player is bound to the
  Underworld. It intentionally does not show Soul Fracture marks; those belong
  in identity/tablist presentation.
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
- Corpse items persist source metadata with a generic source id and label in
  addition to vanilla inventory, armor, and offhand coordinates. This keeps the
  grave recovery UI ready for future backpack, trinket, skin, accessory, or
  addon-owned slot families while vanilla recovery remains the current restore
  implementation.
- Grave owner text is nickname-first: Core citizen nickname, then last known
  username, then Minecraft profile name fallback at capture time.

## Risks

- Full item components are persisted as compressed NBT with legacy fallback.
- True Death cleanup is restart-safe but still needs multiplayer/GameTest coverage.
