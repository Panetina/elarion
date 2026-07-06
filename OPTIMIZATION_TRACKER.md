# OPTIMIZATION TRACKER

Active optimization health tracker for Elarion.

## Current Status

The repo has already moved through the major architecture cleanup pass. Keep
this file focused on remaining performance and operational risks.

Last broad build audit: `.\gradlew.bat build` passed.

Last project-revamp audit slice: 2026-07-05, Phase 0 Slice 2 documentation-only
Configuration/Admin Panel audit. See
`docs/reports/CONFIG_ADMIN_AUDIT.md`.

## Active Risks

- Portal and Shrine flows should remain bounded and server-authoritative.
- History-style systems should use indexes, summaries, or bounded windows before
  becoming player-facing at scale.
- UI systems should continue reusing shared Core UI primitives instead of
  growing duplicate screen stacks.
- Command and GameTest coverage should expand when a subsystem gains new
  persistence or public interaction paths.
- Government UI mutations are now session-bound to a recently opened Civic
  Forum or Seat block through a small tested session service. Add command/GameTest
  coverage before expanding authority actions.
- Portal player checks are chunk-indexed and run every 5 ticks, but
  `PortalRouteService` is now large enough that travel, scheduling, indexing,
  and admin repair should be split before adding more route behavior.
- Worlds routing/border checks now use event routing plus a one-second safety
  sweep. Profile before large public tests if lobby/world enforcement expands.
- The typed config registry and future Admin Panel config browser must describe
  existing loader snapshots; they should not parse every config file on
  ordinary client UI open.
- Addon reload safety is inconsistent. Before exposing config edits in the
  Admin Panel, each editable domain needs explicit reload/rollback behavior so
  bad config cannot partially apply runtime values.
- The future placeholder registry must expose bounded, side-effect-free
  resolution. It should not scan storage or mutate state while formatting text.
- The future Citizen Profile aggregation API must request bounded
  addon-provided sections. It should not copy all addon state into Core or
  synchronize private data and hide it client-side.

## Completed Themes

- Root documentation has been consolidated into a small authoritative set.
- The current shared UI foundation is already in use by NPC, Shrine, and
  Government screens.
- Identity, Realm presentation, and authority markers are now routed through
  Core-owned APIs.
- Shared UI primitives are reused by NPC, Offering, Portal, and Government
  screens.
- Notification HUD placeholder chat messages were removed from player-facing
  clicks.
- Economy recent transaction queries now stream monthly transaction files and
  keep only a bounded newest-match window instead of loading full files.
- Government expired vote resolution now runs on a one-second interval instead
  of scanning all vote state every server tick.
- Portal legacy route migration moved out of `PortalRouteService`.
- Government text, category, color, and ID validation moved out of
  `GovernmentStateService`.
- Dirty worktree classification is recorded in
  `docs/reports/WORKTREE_CLEANUP_AUDIT.md`.

## Keep Watching

- `/e perf` and server diagnostics surface quality.
- Bounded IO on history, reward, and state save paths.
- Any future Chronicle, newspaper, ledger, or search feature should use
  dedicated read models, not raw storage scans.
- Player-facing placeholder text should not ship in live UI/HUD paths.
- Angling reference cleanup is intentionally deferred.
- Documentation/source navigation is now indexed through `AGENTS.md`,
  `INDEX.md`, `CODEX.md`, `docs/addons/README.md`, and `wiki/addons/README.md`;
  keep those files synchronized to avoid future search/token waste.
- Planning and implementation should stay sliced by subsystem to reduce
  token/credit waste without weakening verification.

## Note

This tracker should stay concise. When optimization work is finished, archive it
instead of expanding it indefinitely.
