# Atlas System

Status: Future design; only the `M`-key placeholder shell is operational.

## Goal

Build a first-party Elarion-styled Atlas for explored terrain, political Realm
presentation, Shrines, Portals, NPCs, personal NPC tracking, and a bounded
website map. Xaero's World Map and Antique Atlas are interaction inspiration
only; Elarion will use original code and artwork and no runtime dependency on
either project.

## Current Boundary

The current addon registers a client key and renders a static placeholder. It
does not classify chunks, discover terrain, persist regions, register packets,
query owner addons, publish bridge values, or expose a website map. Every
capability below is deferred until its implementation slice is explicitly
activated.

## Future Ownership And Contracts

- Core owns reusable map-feature, world-access, change-event, and fixed-palette
  pixel-asset contracts. These are projections and must not duplicate domain
  truth.
- Atlas owns terrain-region projections, discovery masks, personal tracked-NPC
  selection, viewport networking, cache policy, and Atlas rendering.
- Core supplies Realm identity and spawn presentation. Government, Offerings,
  Portals, and NPCs contribute bounded projections through their own APIs and
  events; Atlas never reads their storage.
- Fabric remains canonical. The website stores permission-filtered read models
  received through the signed Core projection bridge.

## Future Terrain And Visibility

- Use one semantic Elarion terrain tile per Minecraft chunk, stored in lazy
  32x32-chunk regions with stable style IDs and original artwork.
- Maintain an incremental `allDiscovered` mask, a Realm-member discovery mask
  for Realm worlds, and lazy per-player outsider masks. Never reconstruct these
  by scanning worlds or every player file.
- Worldheart and non-Realm managed worlds use shared discovery for everyone.
- Realm citizens share discoveries within their Realm. Outsiders see only
  their personal discoveries while that Realm is closed.
- When the canonical Offering-owned `ancient_gate_unlocked` flag opens a Realm
  globally, all viewers see the accumulated shared discovery. Closing access
  hides it from outsiders without deleting discovery state.
- Servers mask hidden cells before sending a viewport. Clients and website
  responses never receive unauthorized raw terrain values.
- Website guests see neutral and globally open terrain. Authenticated citizens
  may later see their Realm-shared terrain. Personal outsider masks remain
  game-only and are never projected per player.

## Future Features

- The full Atlas keeps `M`, drag pan, scroll zoom, world selection, coordinates,
  bounded search/filtering, marker selection, and a details panel.
- Realm presentation includes canonical name/color, Government form, 32x32
  heraldry, bounded Shrine level/progress summaries, and Portal endpoints.
- Shrine, Portal, and placed-NPC markers remain projections of their owning
  addons and update through events, including tombstones.
- Players may track one stable placed-NPC UUID. Atlas highlights it and a small
  HUD cue may show direction/distance. Tracking grants no pathfinding,
  teleportation, or access to hidden NPCs.
- Government will own a 32x32 heraldry canvas using transparent plus 16 curated
  Elarion colors. Only the active Monarch or President may publish it through a
  server-validated Seat of Rule workflow.

## Future Bridge And Website

- Planned bridge kinds are `map.region`, `map.world-access`,
  `realm.heraldry`, and existing typed `map.marker.*` state.
- Region states coalesce by world/region in Core's persisted outbox. Fabric
  publishes bounded batches and never exposes raw addon storage.
- The website needs dedicated indexed map-world/map-region read models before
  terrain becomes player-facing; generic JSON projection rows are not the
  long-term region query engine.
- A future viewport API accepts one world and no more than 16 regions, applies
  access policy and masking server-side, and caps response bytes and markers.
- `/worlds` will show a small interactive preview. `/worlds/map` will provide
  the expanded view while preserving world and viewport selection.

## Performance Invariants

- No startup world scan, all-region load, per-tick global scan, owner-storage
  polling, raw-history query, or request-time player-file scan.
- Discovery, classification, writes, bridge publications, viewport requests,
  markers, and responses must all have explicit bounds.
- Regions and personal masks load lazily through bounded caches. Writes are
  atomic and batched, with clear dirty-state and invalidation rules.
- Domain changes arrive through APIs/events. Rendering performs no storage IO,
  parsing, mutation, or network call.
- Existing worlds fill through validated future exploration in v1; historical
  backfill requires a separate bounded design.

## Deferred Delivery Order

1. Core map contracts and owner projection events.
2. Atlas region codec, persistence, discovery, and authorization.
3. Full Atlas UI and Realm/Shrine/Portal/NPC projections.
4. Persistent personal NPC tracking and HUD direction cue.
5. Government heraldry persistence, Seat editor, and shared pixel asset.
6. Bridge region batching and dedicated website map read model/UI.

No item above belongs in current implementation work until explicitly moved
from `PLANS.md` to the active plan and TODO.
