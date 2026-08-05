# OPTIMIZATION TRACKER

Active optimization health tracker for Elarion.

## Current Status

The repo has already moved through the major architecture cleanup pass. Keep
this file focused on remaining performance and operational risks.

Last broad build audit: 2026-07-18, `.\gradlew.bat build --console=plain`
passed with 189 actionable tasks after post-revamp hardening.

## Active Risks

- Phase 14 dual-client QA produced repeatable native `glfw.dll` access
  violations only while Client One and Client Two rendered concurrently on the
  current Windows QA host. Each client is stable alone, and no Elarion frame,
  packet mismatch, or server error preceded the crashes. Treat this as a host
  runtime/capture limitation until reproduced outside the development stack;
  do not add gameplay polling or synchronization workarounds.
- Phase 14 restart smoke observed one `server-queue-apply` warning at about
  1.43 seconds while managed worlds were opening. It did not block readiness or
  recur as an error. Profile the startup queue only if this grows or appears
  during ordinary gameplay; do not optimize from this single startup sample.

- Portal and Shrine flows should remain bounded and server-authoritative.
- History-style systems should use indexes, summaries, or bounded windows before
  becoming player-facing at scale.
- UI systems should continue reusing shared Core UI primitives instead of
  growing duplicate screen stacks.
- Command and GameTest coverage should expand during Phase 14 verification when
  a subsystem gains new persistence or public interaction paths.
- Portal player checks remain chunk-indexed and run every 5 ticks. Phase 13 now
  isolates admin mutation, schedule reconciliation, prompt detection, atomic
  travel execution, and world guarding while preserving one canonical
  `PortalState` owner. Rollback and successful-only accounting now have focused
  unit coverage.
- Worlds routing/border checks now use event routing plus a one-second safety
  sweep. Profile during Phase 14 broad public tests if lobby/world enforcement
  expands.
- The typed config registry and Admin Panel config browser describe existing
  loader snapshots; they should not parse every config file on ordinary client
  UI open.
- Real NPC trading must not detect duplicate payments by scanning Economy
  JSONL history or poll stock every tick. Read-only catalog snapshots are now
  bounded and non-mutating. BUY mutations now use O(1) purchase IDs, O(1)
  Economy operation receipts, and per-placed-NPC stock records with lazy
  restock. See `docs/reports/NPC_TRADE_OWNER_AUDIT.md`.
  - Receipt foundation completed: Economy schema v2 stores a bounded O(1)
    receipt map, reconstructs post-snapshot receipts during journal replay,
    evicts oldest entries at the configured cap, and expires entries in bounded
    batches.
  - Unlimited BUY foundation completed: NPC purchase IDs are O(1) journal
    lookups, Economy settlement uses O(1) operation receipts, and ordinary
    purchases do not scan transaction JSONL, placement files, or tax policy
    files.
  - Finite BUY stock completed: stock records are keyed by placed NPC plus
    offer, restock only during open/quote/purchase paths, and replayed purchase
    IDs cannot decrement stock twice.

## Completed Themes

- Repository-context startup was reduced from a 573,780-character mandatory
  path to bounded task capsules. The 12-case deterministic recall suite
  measured 96.01% aggregate character savings while retaining required source,
  authority docs, verification obligations, dirty-worktree priority, and
  low-confidence refusal.
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
- Collection snapshots now apply matching outbound/inbound limits for tabs,
  entries, and actions, filter unsafe actionable IDs, and repair selection
  before encoding.
- Government expired vote resolution now runs on a one-second interval instead
  of scanning all vote state every server tick.
- Economy tax quotes use an O(1) authority/category map and bounded quantities;
  ordinary NPC interactions never scan tax files, treasuries, or history.
- Worldheart tax destination routing is O(1) and uses a stable dedicated
  Economy treasury field. Changing Worldheart authority emits a Core event but
  does not scan or transfer treasury balances.
- Portal legacy route migration moved out of `PortalRouteService`.
- Government text, category, color, and ID validation moved out of
  `GovernmentStateService`.
- Placeholder resolution and Citizen Profile aggregation are bounded,
  side-effect-free owner projections with server-side visibility filtering.
- Economy reload is a two-stage prepare/commit operation; invalid pricing can
  no longer leave transaction settings partially reloaded.
- Underworld reload now preserves the prior valid service snapshot on malformed
  YAML and keeps first-start default fallback behavior.
- Offerings definition reload now commits project definitions and Shrine UI
  config atomically from the service boundary.
- Government definition reload now commits settings and form definitions
  atomically from the service boundary.
- Realms protection config startup now falls back to safe defaults on malformed
  YAML instead of crashing addon initialization.
- Mounts Collection text config startup fallback now has focused malformed YAML
  coverage and keeps unlock/session state untouched.
- Phase 12 persistence/performance corrections are closed for the approved
  revamp scope. See `docs/reports/PHASE_12_COMPLETION.md`.
- Citizen Profile requests are limited per player before server-thread
  aggregation. Contributor failures have one-warning-per-failure-episode
  diagnostics and recover without disabling the contributor.
- Underworld tomb refreshes and corpse expiration no longer scan every corpse
  periodically. Deduplicated display work and due-time expiration are each
  capped at 64 records per second; only startup reconciliation and explicit
  administrative resets perform complete passes.
- Shared JSON state quarantines malformed input before fallback and propagates
  atomic save failures. Government, Quest, and Underworld state now carry
  tested schema version `1` contracts.

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
