# Elarion Optimization Tracker

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

Temporary working tracker for cleanup, performance, navigation, and health work.
When items become permanent policy, move them into `RULES.md`, `TODO.md`,
`INDEX.md`, or focused docs.

## Current Health

- [x] `.\gradlew.bat build` passed before this tracker was created.
- [x] Canonical Realm and Worldheart terminology was clean in focused docs.
- [x] Project attribution uses Panyel and Panetina Team.
- [x] Re-run full build after code cleanup.
- [x] Re-run remaining health checks after this cleanup pass.
- Current largest Java files after split:
  - `WorldsConfigManager.java`: 491 lines
  - `ProgressionService.java`: 471 lines
  - `ElarionTaskService.java`: 467 lines
  - `CoreConfigValidator.java`: 461 lines
  - `CoreConfigManager.java`: 433 lines

## Large File Reduction

- [x] Split `CoreConfigManager` below roughly 500 lines through default files,
  parser, and validator helpers. Migration remains in the facade because it is
  small and tied to file writes.
- [x] Split `ElarionCommands` below roughly 500 lines through focused admin
  command registrars.
  - [x] Extract ability commands.
  - [x] Extract reward commands.
  - [x] Extract history commands.
  - [x] Extract Realm commands.
  - [x] Extract citizen commands.
  - [x] Extract title commands.
  - [x] Extract progression commands.
- [x] Split `WorldsConfigManager` below roughly 500 lines through defaults,
  YAML IO, and serializer helpers.
- [x] Keep public API, command behavior, generated configs, and runtime schema
  unchanged during cleanup-only splits.
  - Permanent rule added to `RULES.md`.

## Performance And Tick Safety

- [x] Add queue-full metrics by task family.
- [x] Add rolling server-queue apply time to `/e perf`.
- [x] Add configurable warning thresholds for slow apply ticks and queue
  pressure.
- [x] Add sampled world and Realm diagnostics.
- [x] Add sampled tick/headroom reporting.
- [x] Add `/e perf realm <realm>`, `/e perf hotzones`, and `/e perf security`.
- [x] Add sampled world entity categories and previous-sample hotzone deltas.
- [x] Add world block-rule queue-full metrics.
- [x] Slice world block-rule work into 16-block vertical server-queue tasks.
- [x] Add world block-rule slice completion/failure metrics.
- [x] Add batched history JSONL writes with periodic flush and shutdown drain.
- [x] Add per-tick identity sync coalescing for repeated sync intents.
- [x] Add continuous progression interval indexing.
- [x] Add progression-region world indexing.
- [x] Add IO/compute queue diagnostics and family counters to `/e perf`.
- [x] Add event-tracked block entity diagnostics without chunk scanning.
- [x] Add bounded in-memory per-world trend windows.
- [x] Keep no extra worker threads by default beyond configured Core budgets.
- [x] Keep world mutation on the server-thread queue.

## API And Addon Usability

- [x] Add grouped API facades for identity, Realm, messaging, progression, and
  system services.
- [x] Prefer grouped facades in addon code; current addon sources use
  `api.identity()`, `api.realm()`, `api.messaging()`, `api.progressionApi()`,
  and `api.system()` instead of direct service getters where available.
- [x] Add config-reference validation against executable registries.
  - Current Core-owned config validates built-in executable reward action
    references and other cross-file references. Future addon-owned config
    validation remains in `TODO.md` and `docs/config.md` because those config
    sections do not exist yet.
- [x] Do not create abstractions until at least two real call sites or one
  strong public-extension contract need them.
  - Permanent rule already lives in `RULES.md`.

## Config And Runtime State

- [x] Move performance task budgets to generated YAML defaults.
- [x] Remove hardware-specific local CPU baseline from performance defaults.
- [x] Add host-agnostic profile and likely shared-CPU risk marker.
- [x] Surface performance config validation warnings in `/e perf config`.
- [x] Validate every config reference to actions, conditions, requirements,
  milestones, rewards, titles, realms, abilities, and worlds.
  - Current Core references are validated. Full addon coverage is now tracked
    as future implementation work in `TODO.md` and as policy in
    `docs/config.md`.
- [x] Preserve clean separation between editable definitions and mutable world
  state.
  - Permanent rule lives in `RULES.md`, `INDEX.md`, and `docs/config.md`.
- [x] Add Core history recording filter config by category and event type.
- [x] Document config regeneration policy for development and production.

## Low Token Navigation

- [x] `INDEX.md` exists as the project dictionary and ownership map.
- [x] Focused docs exist for API, config, performance, and active addons.
- [x] Link this tracker from `INDEX.md` while active.
- [x] Add health-check command documentation to `docs/performance.md`.
- [x] Add addon docs for every current addon with real behavior.
- [x] Add command surface documentation and a basic GameTest command harness.
- [x] Add bounded live history query implementation, scaling docs, and
  filtering documentation.
- [x] Add compact monthly history index projections for public-memory views.
- [x] Add weekly immutable Chronicle archive summaries from monthly history
  indexes.
- [x] Add public-history composition API for Chronicle, newspaper, ledger, NPC
  rumor, and GUI search consumers.
- [x] Move automatic Chronicle archive generation off ordinary server-tick work
  and through Core task queues.
- [x] Add atomic Chronicle archive writes.
- [x] Add OP Chronicle list/inspect commands for server-side validation before
  GUI consumers.
- [x] Add operations workflow for TPS drops and generated-file cleanup.

## Testing And Health Checks

- [x] `.\gradlew.bat build`
- [x] Terminology and branding scan
- [x] Largest-file scan
- [x] Focused-doc existence check
- [x] `git status --short`

Latest pass:

- [x] Build passed.
- [x] Terminology and branding scan produced no matches.
- [x] Focused docs exist: API, performance, config, tracker.
- [x] Current real-behavior addon contracts exist for Core, Worlds, Realms,
  Names, Titles, Optimization, and Security.
- [x] Current addon sources scan clean for direct legacy `api.<service>()`
  getter use.
- [x] Optimization addon build passed after sampled hotzone detail update.
- [x] Generated dev `performance.yml` files were checked and already use
  conservative host-agnostic defaults.
- [x] Worktree status checked; repository remains dirty from ongoing
  development and generated logs.
- [x] Current largest-file scan refreshed on 2026-06-10.
- [x] Policy-style optimization items were moved into permanent docs instead of
  staying as temporary tracker debt.
- [x] Build passed after the performance hardening pass.
- [x] Tests added for IO/compute diagnostics, history batching, identity sync
  coalescing, progression indexing, and world block-rule slicing.
- [x] Tests added for Optimization trend diagnostics.
- [x] Tests added for history recording policy.
- [x] Tests added for bounded history monthly-file scanning.
- [x] Tests added for monthly history index summaries and bounded index
  queries.
- [x] Tests added for weekly Chronicle archive storage/generation and public
  history search composition.
- [x] GameTest command harness added for registration, removal, permission,
  and real command execution checks.
- [x] Tracked generated logs removed from Git index.

## Thinking Risks

- [x] Avoid solving future systems with premature abstractions before a real
  addon needs the extension point.
- [x] Avoid making Core know addon gameplay details.
- [x] Avoid optimization work that reduces stability, debuggability, or
  reload-safety.
- [x] Avoid assuming the server runs only Elarion; reserve CPU, memory, and IO
  headroom for the full Vanilla+ modpack.
  - These are permanent rules in `RULES.md`, `docs/config.md`, and
    `docs/performance.md`.
