# Current Project Status

Bounded handoff snapshot. Source and tests decide implementation reality;
`RULES.md` owns policy, `INDEX.md` owns navigation, `TODO.md` owns active work,
and `PLAN.md` owns the approved roadmap. Use Git history for completed slice
detail.

## Canonical State

- Runtime target: Fabric 1.21.1 on Java 21. NeoForge code is reference-only.
- Core owns citizens, Realms, identity, titles, relationships, rewards,
  history, permissions, shared UI, task queues, and infrastructure.
- Addons own only their domain definitions/runtime state and integrate through
  public APIs, registries, events, notifications, and bounded projections.
- Editable definitions live under `config/elarion/`; mutable runtime state
  lives under `world/elarion/`.
- Fabric owns canonical game truth. Website, launcher, Discord, and bridge
  components own only their explicit external domains and consume signed,
  bounded contracts.

## Implemented Foundations

- Core provides server-authoritative identity, character lifecycle, shared UI,
  notifications, rewards, metrics, History/Chronicle indexes, commands,
  persistence helpers, task queues, and signed website projection outboxes.
- Economy, Offerings, Government, Guilds, NPCs, Quests, Portals, Worlds,
  Realms, Underworld, Mounts, Names, and Titles have implemented foundations.
  Their authoritative status and boundaries live in `docs/addons/` and
  `docs/systems/`.
- Atlas is a data-free client shell. Jail, Newspapers, Tablist, and Voice Chat
  Hooks remain shells or early integration modules.
- Angling is an internal Fabric port foundation. Public fishing remains
  release-disabled until the explicit gates in `TODO.md` pass.
- Distribution is manifest-owned through `distribution/mods.json`; exports are
  complete client/server install roots with managed configs, resource packs,
  hashes, and launcher manifests.
- Live deployment is guarded by explicit approval, stopped-server confirmation,
  remote backup, staged hashes, and post-start verification.

## Reliability And Scale Baseline

- Canonical History JSONL writes and rebuildable monthly Chronicle indexes
  retain failed targets for retry. Blocking flushes surface unresolved IO
  failure. Weekly Chronicle archives run on the compute queue so they cannot
  deadlock the single IO worker while awaiting index writes.
- Player-facing Chronicle/archive/website reads use bounded indexes and
  deliberate `recordChronicle` promotion. Routine dialogue, relationship,
  banking, trade, and diagnostic events remain excluded as spam.
- NPC story state and relationships are bounded and NPC-owned. Quest marker
  projection crosses the addon boundary only through `ElarionNpcApi`; Quests
  does not import NPC entities, payloads, or networking internals.
- Shared JSON state quarantines malformed input and propagates save failures.
  Runtime maintenance uses bounded queues/caches instead of global per-tick
  scans in ordinary gameplay paths.
- Website projections are read models only. Mojang/Microsoft credentials are
  never transferred to the website; launcher passage and website-session
  contracts are documented in `docs/systems/MinecraftBridge.md`.

## Verification

- Use Java 21 and run the narrowest applicable module test before a full
  `gradlew build`; cross-module changes also run
  `:tests:gametest:runGameTest`.
- Quest/NPC marker integration crosses only `ElarionNpcApi`; Quests must not
  import NPC entities, payloads or Fabric server networking.
- Angling parity is an explicit P2 gate that requires its ignored,
  owner-authorized reference checkout. It is not evidence that the disabled
  public Angling release gate is open.
- The isolated dedicated-server smoke test passed after the owner accepted the
  local EULA; rerun it after runtime dependency or server-startup changes.
  Never commit its runtime files.

## Active Work And Risks

- The recoverable World Reset, Guild/Chat foundation, Backpacks integration,
  Government/tax/heraldry controls and NPC quest-marker projection are
  integrated and tested on the current P0 integration branch. Their evidence
  is in focused Git commits; remaining P0 work is listed in `TODO.md`.
- Custom physical Chronicle libraries/books are future presentation consumers,
  not implemented storage. Their design remains in `PLAN.md` and
  `docs/systems/Chronicles.md`.
- Grave/Underworld edge-flow QA remains a focused future verification slice.
- Distant Horizons 3.2.0-b is a required beta artifact with generation/updater
  paths disabled. Stability is a release target, not a guarantee.
- Concurrent two-client rendering is limited by native `glfw.dll` crashes on
  this host; each authority direction passed independently.
- Two isolated remote staging releases remain pending explicit owner-approved
  cleanup; neither changed live `mods`.

## Next Slice

Select the relevant domain through `docs/ai/routes.json`, read its current
source/tests and authoritative docs, then execute one item from `TODO.md` as a
small verified commit. Do not activate release gates or destructive operations
without their stated approval.
