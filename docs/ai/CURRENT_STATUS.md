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

- Guild Registrar/management, chat/UUID PM selection, typed Seat Taxes,
  tracked-NPC quest markers, and top-layer notification badges are integrated
  as bounded server-authoritative projections.

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
- Repository-context routing is implemented through `docs/ai/routes.json` and
  `dev/tools/ai-context.ps1`; `verifyAiContext` enforces budgets, exclusions,
  representative recall, and low-confidence refusal.
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
- Development launch integrity is explicit: the root run wrappers order Loom
  strictly after `syncDevRuntimeMods`, corrupt local managed-mod markers are
  rebuilt safely, and the dev server is offline-only for deterministic Loom
  identities. A real Client One launch reached rendering after all Elarion
  addons and both runtime resource packs loaded.
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

- Post-review persistence, request-budget, runtime-queue, and resource-pack
  hardening is reflected in the current source and focused tests.
- Phase 14 passed GameTests, dedicated startup/shutdown, controlled restart
  persistence, jar parity, onboarding, Realm teleport authority, `/e`
  rejection, and title resync. Detailed evidence remains in the indexed Phase
  14 reports; concurrent client observation is limited by host `glfw.dll`
  crashes.
- The canonical distribution manifest validated; the official-CDN export
  resolved and passed SHA-512 verification. Current generated roots contain 62
  client jars, 12 `config/` files, one managed Bobby cache sentinel, and two
  resource packs; the server contains 46 jars plus three managed config files.
  `syncDevRuntimeMods` produced 41-jar standard clients, a 43-jar admin Client
  One, and a 25-jar development server.
- Excalibured CIT is release-clean: `verifyExcaliburedCit` validates 267 active
  definitions, and a fresh ordinary-player resource load had zero CIT errors.
- The 2026-08-06 full Java 21 gateway rebuilt all modules and produced 332
  JUnit result files with no failures or errors. `verifyAiContext` passed all
  18 cases; `verifyExcaliburedCit` remains clean with 267 active definitions.
  Detailed historical slice evidence belongs in the indexed reports and Git,
  not in this bounded handoff.
- Bobby's fresh-install cache-root error and Sodium Extra's Reese's Sodium
  Options recommendation remain corrected in source.
- Performance validation passed the two-hour soak, managed-world travel,
  restart/reconnect, clean shutdown, and optimized-server A/B. Known remaining
  startup messages are classified pinned-upstream noise.

- Phase 10-13 focused suites and full cross-module builds passed; their detailed
  evidence remains in the indexed completion reports.
- Run focused module verification first. Use the full build only for
  cross-module changes or final handoff.

## Active Work

- Angling is active but public fishing is release-gated off pending rendering,
  behavior parity, and restart GameTests. Details: `docs/addons/angling.md`.
- The authorized local reference may be ported only into Elarion paths; its raw
  checkout remains excluded. Runtime authority and reload snapshots stay bounded.

## Known Risks

- Custom faction metadata beyond stable id/title-case display remains future
  work; Worldheart, Underworld, and Realm factions are available now.
- Grave/Underworld edge-flow QA remains post-revamp hardening work.
- Portal responsibilities are extracted behind the stable facade; add further
  splits only when an ownership boundary is evidenced.
- Phase 14's former export evidence predates the manifest-owned performance
  distribution. Its dedicated startup, restart, DH/Bobby travel, A/B, and
  two-hour soak gates are now refreshed; the former eleven legacy
  Excalibured/Polytone colormap-target errors are resolved.
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
