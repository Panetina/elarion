# Underworld Addon

Last reviewed: 2026-07-06

Status: Implemented foundation.

## Purpose

`addons/underworld` owns the first Elarion death loop:

- captures normal-world player deaths
- moves captured inventory into a persisted corpse record
- creates a protected persisted Elarion tomb block at the death site
- sends the player to the configured Underworld world after respawn
- blocks chat, private messages, group chat, and portal travel while the soul is
  in the Underworld
- returns the player after a persisted timer
- tracks Soul Fractures and hands True Death to Core Character Lifecycle
- persists timed or permanent moderation banishments without creating a corpse
- limits banished players to movement while rendering them as solid emissive-red
  spectral silhouettes to other clients
- keeps Living and Afterlife inventories (including experience) in separate
  persisted boundaries; normal death inventory remains in the corpse

## Main Source

- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/ElarionUnderworldAddon.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/service/UnderworldService.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/service/UnderworldProfileContributor.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/block/UnderworldTombBlock.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/command/DeathCommands.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/storage/UnderworldStorage.java`

## Config

- `config/elarion/addons/underworld/underworld.yml`
- `config/elarion/addons/worlds/worlds.yml`

Config controls timers, Underworld spawn, corpse expiry, PvP loot rules, combat
tag duration, physical currency tags, excluded loot tags, and Soul Fracture
limits.

`UnderworldConfigDescriptors` registers the read-only `underworld` config
domain through `ElarionApi.system().configs()`. It exposes the active service
snapshot in Underworld, corpse, PvP loot, combat-tag, and Soul Fracture
categories. `/e death reload` replaces the service config only after the YAML
parses successfully; malformed reloads preserve the previous valid in-memory
snapshot and log the failure. Descriptor suppliers immediately reflect the
active snapshot without reparsing YAML during Admin Panel discovery. Decimal
values are currently displayed as read-only strings because Core has no decimal
descriptor codec yet.

The actual Underworld dimension must exist as a managed world in `worlds.yml`.
The default development setup includes `elarion:underworld` as a small VOID
world with a deepslate-tile platform. `underworld.yml` points at that world with
`underworld.world-id: "elarion:underworld"` and controls where souls spawn
inside it.

The client suppresses rain and thunder gradients only while rendering
`elarion:underworld`. Fantasy runtime worlds can otherwise retain Overworld
precipitation visuals after their authoritative weather has been cleared.

## Runtime State

- `world/elarion/addon-state/underworld/state.json`
- `world/elarion/addon-state/underworld/true-death-archive/`

The archive folder retains minimal Underworld-side backup metadata. Canonical
character archives and reset progress live in Core Character Lifecycle state.

Supported snapshots normalize null collections, nested item lists, and nullable
record fields before bind. Structurally unusable null/blank rows are discarded
without losing valid corpse, session, soul, vault, banishment, or inventory
state from the same parsed snapshot.

Runtime state schema v3 adds separate bounded `afterlifeInventories` and
`livingInventories` maps keyed by account UUID. Schema v2's bounded
`banishments` map remains keyed by account UUID.
Each record stores the last known player name, issuing administrator, required
reason, issue time, and absolute expiry; expiry zero means permanent. Earlier
schemas migrate with empty inventory-boundary maps. Timed expiry uses a bounded deadline queue,
not a per-tick scan, and punishment state deliberately survives Core character
resets.

Corpse and recovery-vault stacks preserve the complete 1.21.1 `ItemStack`
component payload through compressed NBT, with legacy item-ID/count fallback.
New corpse stacks also store their original source slot so victim recovery can
restore armor, offhand, selected hotbar, hotbar, and inventory slots when those
slots are still empty or mergeable.

Corpse records also store the tomb block position, victim Realm id, and selected
tombstone variant. Tomb block state is derived from that corpse record during
death capture and startup reconciliation.

## Tomb Blocks

Underworld registers `elarion_underworld:tomb`, a protected 1x1x2 block
structure. The lower half renders the tomb model and stores the corpse id in a
block entity; the upper half is an invisible structural reservation block.

The tomb art comes from `C:\Users\Panyel\Desktop\Tombstones\Nexo\pack` and is
namespaced under `assets/elarion_underworld/`. Imported model footprints:

- `tombstone_1`: visual `0.938 x 1.062 x 0.312` blocks, occupies `1 x 2 x 1`.
- `tombstone_2`: visual `0.938 x 1.125 x 0.312` blocks, occupies `1 x 2 x 1`.
- `tombstone_3`: visual `0.938 x 1.875 x 0.438` blocks, occupies `1 x 2 x 1`.

Tombstone selection is deterministic per Realm: a Realm id hashes to one of the
three variants, so deaths from that Realm consistently leave the same style. If
a player has no Realm and the world has no Realm owner, the corpse id is used as
the deterministic fallback seed.

Tombs are owner-protected while the victim has an active Underworld session.
When the soul returns, the tomb becomes public-lootable and starts its decay
timer using `corpse-expires-minutes`. When the decay timer finishes, the
physical tomb structure is destroyed. Decayed leftovers move into the owner's
recovery vault and the tomb blocks are removed. The tomb block entity mirrors
only display data for the client floating label: owner display name, access
state, timer fields, and item count. Corpse state remains canonical in
persisted Underworld state.

Corpse records store the victim UUID and nickname-first display name captured
at death so the tomb label and grave recovery UI can show who the grave belongs
to without doing a lookup on interaction. The display name prefers Core citizen
nickname, then last known username, then the Minecraft profile name fallback.
Stored corpse items carry modular source metadata: legacy `sourceType`, vanilla
slot/equipment coordinates, plus `sourceId` and `sourceLabel` for future
backpack, trinket, skin, accessory, or other addon-owned slot families. Unknown
future sources fall back to normal inventory recovery unless their addon later
contributes a dedicated restore path.

## Commands

Admin:

```text
/banish <player> <minutes> <reason...>
/banish <player> permanent <reason...>
/banish list
/unbanish <player>
/e death reload
/e death inspect <player>
/e death corpse list
/e death corpse inspect <corpseId>
/e death corpse recover <corpseId> <player>
/e death vault recover <player>
/e death underworld send <player> [minutes]
/e death underworld return <player>
/e death soul inspect <player>
/e death soul add-fracture <player>
/e death soul remove-fracture <player>
/e death soul clear-fractures <player>
```

Development test:

```text
/e test death send <player> <minutes>
/e test death return <player>
/e test death fracture <player>
/e test death fracture add <player>
/e test death fracture remove <player>
/e test death clear <player>
/e test death reset-state
```

The client receives a compact status sync payload while a session or Soul
Fracture state exists. The HUD renders only while the player is bound to the
Underworld and shows the remaining return timer. Soul Fracture marks belong in
identity/tablist presentation, not in the death timer overlay.

That authoritative bound-state payload also gates Soul Sight. Other players
render to a bound viewer in `elarion:underworld` through a texture-independent
solid white full-bright opaque pass with no nameplate, skin pixels, or wearable feature
layers. Living viewers and every other dimension keep normal rendering. There
is no Underworld framebuffer effect, fog override, screen overlay, model aura,
or shader-pack hook.

Banishment reuses Soul Sight for the punished viewer and adds one UUID delta
payload for other online clients. A banished player takes precedence over the
ordinary dead appearance and renders from an opaque flat-red source texture as
a solid full-bright red silhouette. Their HUD shows whether the sentence is
permanent or timed plus the bounded reason.

The required client-only LambDynamicLights 4.8.10 integration registers only
the already-rendered player entity type and returns luminance 6/15 for local or
visible dead/banished states. Lamb owns adaptive ticking, camera/background
sleep, chunk-rebuild culling, and Iris-compatible dynamic-light composition.
Underworld performs no scan or lighting work for ordinary players. The server
does not install or require LambDynamicLights.

## Ownership

Underworld owns death capture, corpse state, recovery vaults, Underworld
sessions, Soul Fractures, and the trigger into Core Character Lifecycle.

Core owns the shared player restriction API used to block chat and travel.
Core also owns the root-command extension list and the global interaction gates
that run before addon callbacks. Underworld contributes the banishment policy
for chat, travel, block/entity attacks and use, item use, and the UUID-only
`queued_admission` restriction. A future Core admission queue must consult that
UUID restriction whenever a queue exists; it must reject or disconnect the
banished account until capacity is free and the queue is empty. No queue exists
yet, so this slice provides the enforceable integration contract without
inventing queue state in Underworld.
Portals still own route state and teleport validation. Economy still owns bank
balances; Underworld only reads physical currency items/tags when selecting PvP
loot.

Underworld owns the meaning of the lifetime death summary shown in Citizen
Ledger. At living-world death capture and repeat Underworld death capture,
`UnderworldService` increments the Core player-stat key
`underworld_lifetime_deaths`. `UnderworldProfileContributor` contributes that
single cached counter as `underworld/deaths` with `SELF` visibility. It must
not scan corpses, sessions, recovery vaults, or history records while building
profile snapshots.

## Events And Notifications

Underworld emits Core domain events:

- `corpse-created`
- `player-sent-to-underworld`
- `player-returned-from-underworld`
- `underworld-death`
- `soul-fractured`
- `true-death`
- `pvp-loot-claimed`
- `corpse-recovered`
- `player-banished`
- `player-unbanished`
- `banishment-expired`

Current notifications are intentionally minimal. True Death broadcasts in chat;
ordinary death, recovery, and loot actions use direct player feedback to avoid
notification spam. Future Chronicle, newspaper, Government succession, Jail, or
website bridge consumers should listen to the domain events.

`UnderworldChronicleText` registers with Core public history and renders
player-facing death records through the shared template-family contract.
Promoted families include PVE death, PVP death, self-inflicted death, void
death, and True Death. Each family has 10 authored stable variants, honors
persisted `chronicle.variant` values, and falls back safely for older records.
Living-world death capture now records a durable Underworld Chronicle entry
after authoritative corpse/session state is created. True Death records a
durable Chronicle entry after the character lifecycle handoff is started.

## Recovery Guarantees

- Tomb interaction opens a server-authored Elarion recovery screen.
- The client grave recovery screen extends Core `ElarionScreen` and uses the
  shared Core civic shell, framed status/body panel, framed contents grid,
  status chip, item-slot, and action-button helpers. Its render, scroll, and
  click math are derived from one internal layout snapshot so hitboxes stay
  aligned with the visible frame. It remains presentation-only; corpse access,
  item restoration, and inventory mutation are revalidated and executed
  server-side.
- Claims are revalidated by corpse ID, player ownership, world, and range.
- Victim recovery tries original empty/mergeable slots first, then normal
  inventory. Full inventories leave unmoved stacks in persisted storage; items
  are never dropped as a fallback.
- Empty corpses are removed immediately, preventing repeated recovery.
- After protection ends, other players may loot remaining tomb contents until
  decay. Decayed corpse contents move to the owner's persisted recovery vault.
- Startup reconciliation restores missing tomb blocks from active corpse records.

Government succession and addon cleanup run through Core's journaled character
reset handlers. Rich character genealogy and a player-facing recovery-vault
screen remain future work.
