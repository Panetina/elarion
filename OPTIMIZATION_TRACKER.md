# OPTIMIZATION TRACKER

Active optimization health tracker for Elarion.

## Current Status

The repo has already moved through the major architecture cleanup pass. Keep
this file focused on remaining performance and operational risks.

Last audit: `.\gradlew.bat build` passed.

## Active Risks

- Portal and Shrine flows should remain bounded and server-authoritative.
- History-style systems should use indexes, summaries, or bounded windows before
  becoming player-facing at scale.
- UI systems should continue reusing shared Core UI primitives instead of
  growing duplicate screen stacks.
- Command and GameTest coverage should expand when a subsystem gains new
  persistence or public interaction paths.
- Government currently scans vote state every server tick. This is acceptable at
  development scale, but should move to an interval or deadline queue before
  many Realms/elections are live.
- Government UI mutations are now session-bound to a recently opened Civic
  Forum or Seat block through a small tested session service. Add command/GameTest
  coverage before expanding authority actions.
- Portal player checks are chunk-indexed and run every 5 ticks, but
  `PortalRouteService` is now large enough that travel, scheduling, indexing,
  and admin repair should be split before adding more route behavior.

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
