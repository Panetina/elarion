# Current Project Status

Bounded handoff snapshot. Keep this file below 12 KB. Completed slice logs are
archived under `docs/ai/archive/` and are not part of the normal AI read path.

## Current State

- Target: Fabric 1.21.1. NeoForge is reference-only.
- Core owns canonical citizens, Realms, titles, identity, relationships,
  rewards, history, permissions, shared UI, task queues, and infrastructure.
- Addons own only their feature-specific definitions and runtime state and
  integrate through Core APIs, registries, events, and notifications.
- Editable definitions live under `config/elarion/`; runtime state lives under
  `world/elarion/`.
- The completed revamp and post-review hardening are captured in a reproducible
  Git checkpoint. Runtime worlds, generated output, caches, backups, and local
  deployment secrets remain ignored.

## Latest Completed Architecture Work

- Atlas now exists as a compiling shell addon: `M` opens a data-free Elarion
  placeholder, its public marker API exposes no capabilities, and it registers
  no server, storage, network, config, command, bridge, or domain integration.
  The functional design remains future work in `docs/systems/Atlas.md`.
- The final post-revamp findings are resolved. Shared JSON state quarantines
  malformed input and propagates save failure; Government, Quest, and
  Underworld state use schema v1; profile requests and contributor diagnostics
  are bounded; and Underworld runtime corpse maintenance uses capped queues.
- Canonical Excalibured art/font runtime packs live under tracked
  `dev/resourcepacks/` and synchronize idempotently to all development clients.
- Core now owns a restart-safe signed website projection outbox. Realm and
  citizen summaries plus filtered Chronicle events publish without raw storage
  scans; Government publishes voted Realm presentation and aggregate election
  lifecycle through `api.system().webProjections()`. Website projections are
  read models only and do not change canonical ownership.
- Core now validates typed bounded website map markers and maintains a
  persisted advancement leaderboard index updated on join/advancement events.
  Offerings publishes per-instance Shrine aggregates and marker tombstones
  without contributor identities or hot-path scans.
- The external platform is now an explicit architecture boundary. The website
  owns web identity, whitelist applications/reviews, permissions, audit state,
  and permission-filtered read models; Fabric remains canonical for game and
  whitelist state. The .NET launcher consumes signed immutable releases, and
  Discord remains an identity/notification adapter through the website.
- `deployLiveServerMods` is the guarded live promotion path. It verifies all
  modules/context, rebuilds the canonical two-folder export, stages hashed jars,
  backs up remote `mods`, and requires explicit stopped-server/owner approval.
  It never starts PebbleHost or deploys the website/launcher.
- Repository-context reduction is implemented. Root always-loaded context is
  below 16 KB; former status/plan/TODO histories are preserved under
  `docs/ai/archive/` and excluded from ordinary retrieval.
- `dev/tools/ai-context.ps1` emits bounded Markdown/JSON capsules from
  `docs/ai/routes.json`, prioritizes dirty files, and uses `rg` plus a
  deterministic PowerShell Java-signature extractor. No IDE or external
  context engine is required.
- `verifyAiContext` covers document budgets, route integrity, archive/reference
  exclusions, twelve representative source/doc recall cases, token budgets,
  and low-confidence refusal. The local aggregate character gate measured
  96.01% less repository context than the pre-compaction mandatory path.
- Phase 8 completed the Core Chronicle template-family and deterministic
  variant framework. Existing `chronicle.variant` metadata remains
  authoritative; library-ready event families require ten authored variants,
  metadata validation, fallbacks, and tests.
- Phase 9 completed NPC narrative foundations. NPCs owns bounded per-player,
  per-placed-NPC relationship scores and durable story state with flags,
  one-time choices, endings, and opt-in re-entry.
- Explicit NPC `history-worthy` outcomes publish structured
  `npc/story-outcome` history with stable Chronicle variants. Ordinary
  dialogue, relationship changes, banking, and trades remain silent.
- Recent development launch repair restored Government classes, stable
  `citizen` compatibility IDs, valid client resource rotations, and clean
  dedicated-server/client startup.
- Government's supported foundation is now intentionally limited to Monarchy
  and Republic. The obsolete Confederation and Theocracy paths were removed
  from config, runtime state, founding flow, UI, commands, tests, titles, and
  documentation; focused command, packet, civic-mutation, persistence, and
  GameTest coverage closes the former Government hardening item.
- Client/server distribution is now manifest-owned. `distribution/mods.json`
  pins official origins, versions, sides, licenses, filenames, sizes, and
  SHA-512 values; builds no longer read a local Modrinth profile. Exports are
  full install roots with configs, resource packs, and launcher manifests.
- The stable performance core ships Sodium, Lithium, FerriteCore, ModernFix,
  ImmediatelyFast, conservative rendering culling, EBE, BadOptimizations,
  Particle Core, Dynamic FPS, and FastQuit. Distant Horizons and Bobby are
  required clients with bounded, generation-disabled safety settings.
  LambDynamicLights 4.8.10 is client-only and uses managed fancy/culling/
  adaptive settings; Underworld registers low-luminance white dead and red
  banished silhouettes without any custom post-processing, fog, or aura pass.

## Verification Snapshot

- Post-review verification passed the full 189-task build, all required
  GameTests, all 12 AI context cases, focused persistence/schema/profile/queue
  tests, and SHA-256 resource-pack synchronization. See
  `docs/reports/POST_REVAMP_FINDINGS_COMPLETION.md`.
- Phase 14 GameTest now passes all required tests. Its runtime includes the
  required Portals dependency, derives the configured Realm fixture, preserves
  empty-result command semantics, and creates a valid Ember for Government
  office assignment.
- Phase 14 dedicated startup reached `Done (1.770s)`, initialized all Elarion
  addons, opened six managed worlds, logged zero errors, and saved every
  dimension on controlled shutdown. Twelve warnings were classified as
  third-party development, local offline-mode, Windows PerfOS, or startup-lag
  noise. See `docs/reports/PHASE_14_RUNTIME_BASELINE.md`.
- Phase 14 controlled restart passed after a 28-file runtime backup. Reversible
  Economy, Government, and Offering markers survived restart without replay
  duplication; Portal, Underworld, and Character lifecycle snapshots remained
  exact; both NPC placements remained semantically stable; owner-command
  cleanup restored progression markers. See
  `docs/reports/PHASE_14_RESTART_PERSISTENCE_SMOKE.md`.
- Phase 14 client QA passed jar parity, fresh onboarding, confirm-only Realm
  teleport, `/e` rejection, and authorized title resync. Invalid `Player###`
  prefills are filtered. Concurrent observation remains partial due native
  `glfw.dll` host crashes; see `PHASE_14_CLIENT_AUTHORITY_QA.md`.
- The canonical distribution manifest validated; the official-CDN export
  resolved and passed SHA-512 verification. Current generated roots contain 62
  client jars, 12 `config/` files, one managed Bobby cache sentinel, and two
  resource packs; the server contains 46 jars plus three managed config files.
  `syncDevRuntimeMods` produced 41-jar standard clients, a 43-jar admin Client
  One, and a 25-jar development server.
- The Excalibured CIT release blocker is resolved. Seventy-six active rules now
  use Minecraft 1.21 item-component predicates, six invalid/empty definitions
  were removed, and the stale `clarentmod` shield-model reference was corrected.
  `verifyExcaliburedCit` validates 267 active definitions and is required by
  distribution verification. A fresh ordinary-player client resource load
  completed CIT model linking with zero CIT errors and no removed-namespace
  model warning.
- Bobby's fresh-install cache-root error and Sodium Extra's Reese's Sodium
  Options recommendation remain corrected in source.
- Performance validation passed a 120-minute joined Peaceful soak, 12
  managed-world teleports, restart/reconnect, client Generational ZGC, and
  clean shutdown. The server A/B measured the optimized stack 5.01% faster in
  wall startup. See `docs/reports/PERFORMANCE_DISTRIBUTION_VALIDATION.md`.
- A settled dedicated-server startup/stop reached `Done`, saved all dimensions,
  and exited successfully. Its Lithium/ModernFix `SortedArraySet` and
  biome-temperature overwrite warnings are resolved by explicitly assigning
  both overlapping patches to Lithium.
- Server startup now declares Lithium's block-tracking-dependent options at
  their effective disabled values and supplies the vanilla birch/mangrove
  biome tags required by the world-generation stack. Remaining missing-refmap,
  optional-compatibility-class, data-fixer, and empty backport-registry messages
  originate in pinned upstream jars and are not hidden by modifying artifacts.

- Phase 10-13 focused suites and full cross-module builds passed; their detailed
  evidence remains in the indexed completion reports.
- Run focused module verification first. Use the full build only for
  cross-module changes or final handoff.

## Active Work

- Elarion Angling has been selected as the next bounded feature. Its foundation
  slice is complete: separate main/Delight Fabric modules, frozen-source and
  parity contracts, Core ownership boundaries, performance/authority rules,
  release exclusion, and a USB/new-machine resume procedure. The first bounded
  content and server runtime registries are active, but public fishing remains
  behind an explicit false release gate.
- The owner has authorized the local `Modding` tree as the Angling port source,
  including edited art and other creative resources. Ported files may enter
  proper Elarion module paths without a separate replacement-asset phase; the
  raw reference checkout remains excluded from jars.
- Angling now tracks all 2,603 authorized reference files in a deterministic
  SHA-256 inventory. The versioned immutable catch-definition codec decodes
  all 463 actual catch definitions, and the registered caught-fish Fabric
  component has persistent and bounded packet round-trip coverage. The audit
  corrected the compatibility count from 322 to 315; the seven previously
  included JSON files are rarity tags and a treasure data map, not catches.
  All 12 durable Fabric item-component identities are registered with bounded
  value tests. Signed guides snapshot Core projections without owning live
  counters; persisted modifiers compile once and expose immutable runtime
  values. Server selection is implemented; screen gameplay remains disabled.
- The native Angling substrate is transformed and typed: 148 catch resources,
  640 compiled restrictions, 68 compiled native modifiers, 352 resolved
  sweetspots, transactional indexed reload snapshots, 1,010 runtime assets,
  and 347 additional server-data files. Authorized PNG/OGG binaries remain
  byte-identical. All 11 sound events and three server-safe particle types now
  register under `elarion_angling`; their three client-only factories now
  preserve the reference bite and notification animations. The registry now
  contains 198 items, including 48 bucketable fish, their persistent bucket,
  and all 15 component-safe rods. The non-cascading identity transform enforces
  target IDs/names and zero visible source branding. The master ledger marks
  1,518 files ported, 1,083 pending, and two Curios files dependency-
  unavailable. Remaining custom items, blocks, renderers, screens, UI,
  tournaments, compatibility, and Delight remain incomplete.
- All five former NeoForge data maps are transformed into domain-owned Elarion
  resources with 89 values. All five now compile and publish as one atomic
  immutable snapshot: 13 aquarium interactions, eight tackle skins, 92
  modifier nodes across 57 selectors using 43 active types, and 11 treasure
  definitions. All nine dormant modifier IDs are also registered, completing
  the 52-schema reference roster. Lure/throw and a first broad equipment
  minigame behavior slice are now executed at cast/session creation; remaining
  catch/treasure effects are pending. Catch selection, stable inline pool
  entries, treasure resolution, and added loot-table outcomes now execute
  server-side with bounded weighted pools.
- Minigame authority hardening now includes typed bounded start/input/state
  payloads, server-thread receiver dispatch, same-world live-entity and owner
  validation, replay/gap/rate/transition rejection, and a deterministic
  bobber-owned simulation. The server owns pointer/progress/perfect/treasure/
  hits/layers, seeded sweetspots, the nine compiled native modifier types, the
  nine compiled behavior types, and terminal idempotency. A tested four-state
  bobber core preserves flying/bobbing/biting/fishing timing and the 80-tick
  bite window. A live nonpersistent Fabric bobber, O(1) owner index,
  restriction-context capture, weighted selection, server outcome generation,
  and rod-to-commit path are wired. The authoritative client screen foundation
  consumes only server snapshots and sends only input edges. Exact bobber/fish
  renderers and remaining behavior parity are pending, so the public gameplay
  gate is false.
- Core catch telemetry now persists schema-2 typed server outcomes and
  materializes immutable per-species count/performance summaries. Schema-1
  journals and summaries migrate during decode, so guide/leaderboard consumers
  can use bounded direct projections without scanning catch JSONL.
- Catch acceptance now has a forced Angling request journal and bounded
  coordinator. Restart replay preserves event UUID/source sequence across Core
  telemetry and metrics, then idempotently enqueues exact component-bearing,
  bounded multi-item/entity rewards before accepted-event delivery. Per-action
  completion makes partial claims restart-safe. Failures close later admission;
  full inventories remain claimable through Core.
- Bait consumption is the first durable reward action. Canonical per-player
  totals use lazy append journals and bounded per-player snapshots; Fabric
  persists the applied cursor with inventory and reconciles only that player's
  used bait types on join. No synchronous gameplay-thread save is used.
  Runtime kill/restart GameTests remain pending.
- Core now has a bound typed metric/ranking engine with bounded batches,
  explicit materialized dimensions, logarithmic update/rank indexes,
  competition ties, revisioned queries, append-first journals, atomic
  snapshots, restart replay, a bounded worker, diagnostics, and public API.
  Versioned metric events, metric-indexed title conditions, and bounded online
  lazy reconciliation are implemented. Completed-event retention, offline
  cursor reconciliation, and web projections remain.
  Angling's exact 18-metric roster is registered.
- Focused Core/Angling checks and the 215-task root build pass. Both generated
  Angling jars were scanned and contain no raw reference checkout, Git,
  NeoForge, or source-brand paths. Release exclusion remains active because
  gameplay parity is incomplete.

## Known Risks

- The final post-revamp review and its completed remediation are indexed at
  `docs/reports/POST_REVAMP_PROJECT_REVIEW.md` and
  `docs/reports/POST_REVAMP_FINDINGS_COMPLETION.md`.
- Custom faction metadata beyond stable id/title-case display remains future
  work; Worldheart, Underworld, and Realm factions are available now.
- Additional Chronicle families should be promoted only when a player-facing
  consumer needs them and the ten-variant contract is satisfied.
- Grave/Underworld edge-flow QA remains post-revamp hardening work.
- Phase 13 is complete; Portal responsibilities are extracted behind the stable
  facade and no further duplicate deletion was evidence-safe. See
  `docs/reports/PHASE_13_COMPLETION.md`.
- Phase 14's former export evidence predates the manifest-owned performance
  distribution. Its dedicated startup, restart, DH/Bobby travel, A/B, and
  two-hour soak gates are now refreshed; the remaining promotion blocker is
  eleven legacy Excalibured/Polytone colormap-target errors.
- Distant Horizons 3.2.0-b is a required beta artifact. Its generation and
  updater paths are disabled, its former 2.3.0-b OpenGL fault is absent, and
  Java 21 Generational ZGC is enforced for clients. “100% stability” remains a
  release-gate target rather than a guarantee.
- Concurrent dual-renderer observation is still limited by native `glfw.dll`
  crashes on this host; each authority direction passed independently.
- Two isolated remote staging releases created while detecting the original
  plan-only ordering defect remain pending explicit owner-approved cleanup.

## Next Slice

- Complete exact bobber/fish rendering, remaining catch/minigame behavior,
  compatibility catch-tag membership, the Fabric loot-hook replacement, and
  bait-debit kill/restart GameTests. Continue block/UI/tournament/
  economy slices only while the public gameplay gate stays false.

## Historical Recovery

- Status history: `docs/ai/archive/CURRENT_STATUS_THROUGH_2026-07-11.md`.
- Plan history: `docs/ai/archive/PLAN_THROUGH_2026-07-11.md`.
- Previous TODO detail: `docs/ai/archive/TODO_THROUGH_2026-07-11.md`.
- Use these only for an explicit historical investigation. Current source and
  authoritative system docs override archived handoffs.
