# PLAN

Short current-focus memory. Keep this file below 12 KB. `RULES.md` owns
permanent policy, `TODO.md` owns unfinished implementation work, and
`PLANS.md` owns future design. Superseded completion detail belongs under
`docs/ai/archive/`.

## Current Direction

Fabric 1.21.1 is the implementation target. Core owns canonical shared truth;
addons extend it through explicit APIs, events, registries, and bounded
read-models. The Phase 14 revamp is complete. New work proceeds only as small,
verified slices that preserve current gameplay and public contracts unless the
owner approves a change.

## Active Approved Work

### Repository Consolidation

The protected dirty worktree remains user-owned. Never restage, revert, or
absorb unrelated work into a consolidation commit. GitHub is the recoverable
backup: each verified thematic slice is committed and pushed before moving on.

- The Java 21 multi-module build and context-routing gateway are established
  baselines; rerun the narrowest relevant suite during a slice and the full
  build before a cross-module handoff.
- Continue evidence-led P0/P1 review of persistence, ownership boundaries,
  bounded player-facing queries, and direct cross-domain state access.
- History JSONL and its monthly Chronicle indexes now retain failed writes for
  retry and make blocking persistence failures explicit. Continue treating
  JSONL as canonical and indexes/archives as rebuildable read models.
- Keep active documentation concise and authoritative. Do not recreate
  append-only completion logs in root plans or status snapshots.

### WORLD-01 Managed World Reset

`/e reset world <world>` remains a separate destructive-command slice. It must
use an executor-bound preview/confirmation, stream a complete backup, evacuate
and unload only the selected managed world, recreate it from its existing
definition, validate it, and restore on failure. It must not be combined with
player reset logic or marked release-ready before terrain recovery and failure
restoration are verified.

### Elarion Angling Fabric Port

The owner-authorized local reference is the port source. The public fishing
gate remains false until exact fish/bobber rendering, remaining catch/minigame
behavior, compatibility catch tags, the Fabric loot-hook replacement, and
kill/restart persistence evidence are complete. Keep accepted catches,
rewards, titles, metrics, rankings, and events Core-owned. Do not package the
raw reference checkout or enable public gameplay as a partial parity measure.

## Approval Gates

The following require an explicit owner decision before implementation or
release: changes to public gameplay, commands, permissions, save compatibility,
network protocols, canonical ownership, major dependency versions, live-server
promotion, destructive reset execution, and browser authentication semantics.
The launcher/website session design remains documentation-only until its
account-linking and consent requirements are approved.

## Working Contract

1. Select the route in `docs/ai/routes.json`, then read the relevant source,
   tests, and authoritative system docs.
2. Preserve unrelated worktree changes; search existing ownership and storage
   paths before adding a new one.
3. Make the smallest coherent vertical change; add proportionate tests and
   documentation in the same slice.
4. Verify focused behavior first. Use a full build for cross-module changes or
   final handoff.
5. Push each verified thematic commit. Never commit local worlds, generated
   outputs, credentials, or ignored material merely to create a backup.

## Archive

The previous append-only plan is preserved at
`docs/ai/archive/PLAN_THROUGH_2026-07-11.md` and is not part of the normal read
path.
