# Atlas Addon

Status: Shell.

## Purpose

`addons/atlas` reserves the client-facing Elarion Atlas. The implemented shell
registers the `M` key in the Elarion category and opens a data-free placeholder
screen. It has no map, marker, persistence, networking, command, config, bridge,
or server-runtime behavior.

## Ownership

Atlas will own map rendering, exploration projections, personal NPC tracking
state, and Atlas-specific UI. Core must own reusable map-feature, access-policy,
and pixel-asset contracts before functional integration begins.

Canonical state remains with its current owner:

- Core: citizens, Realm identity/membership, Realm spawn, and shared contracts.
- Government: government form, authority, Seat of Rule, and future heraldry.
- Offerings: Shrine anchors, levels, progress, and global-access flags.
- Portals: routes, endpoints, activation, and unlock state.
- NPCs: NPC definitions, placements, display data, and lifecycle state.

Atlas may consume future Core APIs and bounded events. It must not depend on or
read another addon's storage directly, copy owner state into Atlas persistence,
or create circular addon dependencies.

## Current Source

- Common shell: `addons/atlas/src/main/java/panetina/elarion/addons/atlas/`
- Client shell: `addons/atlas/src/main/java/panetina/elarion/addons/atlas/client/`
- Public marker API: `ElarionAtlasApi`

`ElarionAtlasApi.get()` currently returns a stable marker instance and reports
an empty capability set. That is intentional; callers must not infer terrain,
marker, persistence, or bridge availability from the module being installed.

## Current Behavior

- `M` opens or closes `AtlasPlaceholderScreen` during normal gameplay.
- The screen uses Core civic UI primitives and lists disabled future sections:
  Worlds, Realms, Shrines, Portals, NPCs, and Filters.
- Opening the screen performs no server request and loads no world data.
- The common entrypoint only records shell initialization.

## Events And Notifications

The shell emits no domain events and publishes no notifications. It creates no
audience, deduplication, expiry, or action contract. Future meaningful Atlas
lifecycle events must use Core; routine exploration and viewport movement must
remain silent to avoid notification and Chronicle spam.

## Commands, Config, State, And Networking

None. Reserved packages contain documentation only and are not operational.

## Future Design

The authoritative deferred design, visibility rules, performance invariants,
bridge boundary, and staged implementation are in `../systems/Atlas.md` and
`../../PLAN.md`.

## Inspiration And Credits

Interaction ideas were informed by
[Xaero's World Map](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map)
and [Antique Atlas](https://github.com/sleepingdragoninn/antique-atlas).
Elarion Atlas uses independently authored code and artwork. Neither project is
copied, bundled, or included as a dependency.

## Verification

```powershell
.\gradlew.bat :addons:atlas:test :addons:atlas:build
```
