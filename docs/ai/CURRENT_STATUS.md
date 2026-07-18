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

- Phase 10-13 focused suites and full cross-module builds passed; their detailed
  evidence remains in the indexed completion reports.
- Run focused module verification first. Use the full build only for
  cross-module changes or final handoff.

## Active Work

- None. The project-wide revamp and its final review remediation are complete.
  Future work begins as one bounded feature slice using
  `docs/systems/EXTENSION_GUIDE.md`.

## Known Risks

- The final post-revamp review and its completed remediation are indexed at
  `docs/reports/POST_REVAMP_PROJECT_REVIEW.md` and
  `docs/reports/POST_REVAMP_FINDINGS_COMPLETION.md`.
- Custom faction metadata beyond stable id/title-case display remains future
  work; Worldheart, Underworld, and Realm factions are available now.
- Additional Chronicle families should be promoted only when a player-facing
  consumer needs them and the ten-variant contract is satisfied.
- Additional Government player-context GameTests and Grave/Underworld edge-flow
  QA remain post-revamp hardening work.
- Phase 13 is complete; Portal responsibilities are extracted behind the stable
  facade and no further duplicate deletion was evidence-safe. See
  `docs/reports/PHASE_13_COMPLETION.md`.
- Phase 14 exports contain 42 server jars and 43 client jars with no duplicate
  names; Mod Menu is the only client-only jar. Build, GameTests, startup,
  restart, optional-addon, client/onboarding, UI/resource, and deployment-plan
  evidence is indexed in the verification matrix.
- Concurrent dual-renderer observation is still limited by native `glfw.dll`
  crashes on this host; each authority direction passed independently.
- Two isolated remote staging releases created while detecting the original
  plan-only ordering defect remain pending explicit owner-approved cleanup.

## Next Slice

- None selected. Choose one concrete feature from `TODO.md`; do not reopen the
  completed broad revamp.

## Historical Recovery

- Status history: `docs/ai/archive/CURRENT_STATUS_THROUGH_2026-07-11.md`.
- Plan history: `docs/ai/archive/PLAN_THROUGH_2026-07-11.md`.
- Previous TODO detail: `docs/ai/archive/TODO_THROUGH_2026-07-11.md`.
- Use these only for an explicit historical investigation. Current source and
  authoritative system docs override archived handoffs.
