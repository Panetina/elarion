# PLAN

Short current-focus memory. Keep this file below 12 KB; completed phase logs
belong under `docs/ai/archive/`.

## Current Direction

The project-wide revamp was completed through small, explicitly approved
slices. Core remains the canonical owner of shared truth and infrastructure.
Addons extend behavior through stable APIs, registries, domain events, and
bounded owner-maintained summaries.

The project-wide revamp is complete through Phase 14. Its final evidence is in
`docs/reports/PHASE_14_COMPLETION.md`; future work begins as a new bounded
feature or hardening slice using `docs/systems/EXTENSION_GUIDE.md`.

## Active Approved Work

### F-01 Afterlife Inventory And Protection

- Complete this boundary before `ADM-01`. Persist separate Living and Afterlife
  inventory snapshots for every player regardless of rank or game mode; never
  let Underworld entry, death, return, logout, restart, or admin mode merge the
  two inventories.
- Living-world death items remain in the existing corpse/grave flow. Obols and
  approved Underworld/Limbo resources remain in the Afterlife inventory across
  visits and cannot enter living worlds without a future explicit allowlisted
  transfer contract.
- Disable PvP in Underworld/Limbo and close the reported banishment pickup gap.
  Movement-only banished accounts cannot acquire items, XP, rewards, or
  progression through operator/game-mode bypasses.
- Add round-trip, repeated-death, return, restart, disconnect, game-mode,
  banishment, and corpse-recovery tests before destructive reset tooling uses
  this ownership boundary.

### ADM-01 Complete Player-Data Reset

- Implement only `/e reset players` in this slice. The first invocation shows
  affected counts and clickable confirm/cancel controls; confirmation is tied
  to the executor and expires after 60 seconds.
- Core owns one reset coordinator, backup manifest, audit summary, and handler
  registry. Each addon resets and backs up only its own state through a
  registered handler.
- Reset UUID-keyed vanilla and Elarion player progression, Shrine progression,
  tombstones, and Underworld/Afterlife state. Back up and clear operator,
  whitelist, and persisted/in-memory profile-cache state together with vanilla
  playerdata/stats/advancements; recovery requires console access or a fresh
  signed bridge access operation. Preserve all worlds, terrain,
  buildings, placed Shrine blocks, configured NPCs, portals, Seats of Rule,
  definitions, configuration, and map infrastructure.
- `WORLD-01` managed-world regeneration follows in a separate run and must not
  share destructive execution logic with this slice.

### WORLD-01 Managed World Reset

`/e reset world <world>` previews and confirms a managed-world regeneration,
backs up world-scoped addon state, removes placed NPCs, Shrine/offering records,
and portal endpoints tied to that world, then recreates the world from its
existing definition. Definitions and config remain intact.

### Underworld Moderation Banishment

- `/banish` and `/unbanish` form a persisted timed/permanent moderation path
  separate from ordinary death sessions and corpses.
- Banished players are confined to the Underworld, see the Soul Sight grade and
  their reason, retain movement only, and appear dark emissive-red to others.
- Core owns the global interaction gates and UUID-only `queued_admission`
  restriction. The actual queue consumer remains a later Core admission slice.

### Elarion Angling Fabric Port

- The frozen local reference is owner-authorized as the full Elarion Angling
  port source, including code, content, and edited creative resources. Ported
  files may ship from proper Fabric module paths without a later replacement-
  asset phase.
- The completed foundation established the two Fabric module boundaries,
  frozen-source and parity contracts, performance/authority rules, release
  exclusion, and cross-machine handoff. The first content slice now registers
  134 exact vanilla-behavior items; custom fish, equipment, blocks, and fishing
  gameplay remain gated on their real Fabric behavior classes.
- Later work proceeds through bounded vertical slices and keeps accepted catch,
  reward, title, metric, ranking, and event truth Core-owned.
- When parity and technical release gates pass, both modules become canonical
  exportable Elarion mods without another general asset-approval step.

## Planned Follow-ups

1. Port the custom item and bucketable-fish behavior classes, then complete the
   item and entity registries without placeholder registrations.
2. Port blocks, block entities, screen handlers, and entity renderers before
   enabling the server-authoritative fishing pipeline. The three particle
   factories are complete and isolated behind the client entrypoint.
3. `CHAT-01`: remove vanilla `/say` and prove that Local, Underworld,
   banishment, and future Jail restrictions cannot be bypassed through vanilla
   command routing.
4. Promote the detailed SMP features in `PLANS.md` one bounded slice at a time.
   `GUILD-01` must migrate the current Group domain and persisted data to the
   single Guild/Guilds vocabulary before Guild UI, roles, announcements,
   secrecy, heraldry, or NPC creation expands the system.

## Required Work Style

1. Start with the task route in `docs/ai/routes.json`.
2. Read current source and only the authority docs selected by that route.
3. Search existing services, registries, networking, UI, and persistence
   patterns before adding infrastructure.
4. Make the smallest coherent change and run focused verification first.
5. Apply the documentation maintenance matrix in `RULES.md`.
6. Preserve unrelated dirty-worktree changes.
7. Keep ordinary context at 6,000 tokens and below 12,000; expand to at most
   24,000 only for a justified cross-module Core contract.

## Current Invariants

- Fabric 1.21.1 is the source of truth.
- Runtime state stays under `world/elarion/`; editable definitions stay under
  `config/elarion/`.
- Client packets are requests; the server validates and mutates.
- Player-facing large-history features use bounded indexes or summaries, not
  raw JSONL scans.
- Config-backed definitions require matching read-only descriptors and tests.
- Meaningful lifecycle events use reusable Core domain events; only selected
  events become notifications.
- Placeholder resolution may not perform storage/history/world/player scans,
  IO, writes, mutation, network calls, or unbounded parsing during rendering.
- Addons retain canonical values; Core placeholder infrastructure must not copy
  addon runtime state.

## Archive

The previous append-only plan is preserved at
`docs/ai/archive/PLAN_THROUGH_2026-07-11.md` and is not part of the normal read
path.
