# PLAN

Short current-focus memory. Keep this file below 12 KB; completed phase logs
belong under `docs/ai/archive/`.

## Current Direction

The project-wide revamp was completed through small, explicitly approved
slices. Core remains the canonical owner of shared truth and infrastructure.
Addons extend behavior through stable APIs, registries, domain events, and
bounded owner-maintained summaries.

The project-wide revamp is complete through Phase 14. Future work begins as a
new bounded feature or hardening slice using
`docs/systems/EXTENSION_GUIDE.md`.

## Active Approved Work

### Repository Consolidation

Owner-approved consolidation is active as recoverable vertical slices. The
2026-07-30 baseline is HEAD `e6ed78e79b8ff59a4dee49e2f7c8abdcec53d428`
plus a protected dirty worktree: 112 tracked changes, 24 staged paths, and 72
untracked paths. The staged/untracked `groups -> guilds` migration is
user-owned and must not be restaged, reverted, or absorbed into consolidation
commits.

1. **Baseline — complete:** Java 21 discovery, changed-module compilation, and
   focused Core/addon suites pass; no tracked credential or direct cross-addon
   storage/service dependency was confirmed.
2. **Context routing — complete:** `410d0ef8` rejects incomplete broad-task
   capsules while bounded Romanian/English class/domain routing passes 18/18.
3. **Document truth — complete:** `9b581280` establishes authority precedence
   and requires investigation before reconciling source and contracts.
4. **Government characterization — active:** commits `eeb87335`, `a75a56a08`,
   `ec116deb0`, `3d1e7da31`, `ee6829b97`, and `698f8ad826` extract the
   deterministic vote-resolution policy, capped newest-first office-term
   index, active founding-election lifecycle policy, notification
   audience/action/deduplication policy, and authority-title policy, then
   remove an unreachable legacy proposal migration. Focused Government tests
   and GameTest compilation pass. Persistence uses the existing
   atomic/quarantine contract. Realm proposal/law listing remains an approval
   gate because a scalable fix requires indexed pagination across API,
   networking, and UI contracts.
5. **Government Realm record indexing — complete:** commit `dbb33e135` keeps
   persisted proposal/law maps canonical while rebuilding a runtime-only
   per-Realm index after load and maintaining it on every owned write/reset.
   Existing newest-first ordering and full-list APIs are unchanged; clean-tree
   Government tests and the protected GameTest compilation pass. Bounded
   pagination across API, network, and UI remains an approval gate.
6. **Government vote deadline indexing — complete:** commit `3aab69509`
   keeps persisted votes canonical and rebuilds a runtime-only deadline index
   after load. Vote starts, runoffs, resolutions, and resets maintain the
   projection; the existing 20-tick wake-up now examines only due votes while
   preserving canonical map order for simultaneous expirations. Clean-tree
   Government tests and protected GameTest compilation pass.
7. **Government reload hardening — complete:** commits `3e71e42475` and
   `0a28f50398` add owner-side supported-schema normalization before bind.
   Null canonical-map rows are removed and mutable vote collections are
   rebuilt while valid civic state and the shared unsupported-schema
   quarantine contract are preserved. The clean committed baseline and the
   protected schema-v2 worktree both pass Government tests.
8. **Bounded Realm population work — complete:** commits `9ba24789` and
   `000c1bc90` add a Core-owned Realm membership UUID/count index and route
   Realm/World notifications, Realm rewards, Government threshold/name lookup,
   Realm decision counts, non-global Offering rewards, and starter-Realm
   balancing through indexed target populations instead of repeated
   all-citizen file scans.
9. **Reload hardening — complete:** commits `2c88f3d18f`, `725886fe6b`,
   `030ea7753a`, `211c3823d6`, `3e88be622e`, `716777b363`, `e82b4f806a`,
   `8fe8570301`, and `c8eea3955d` normalize recoverable Portal, Offering,
   Mount, Underworld, Notification, reward-grant, character-lifecycle, and
   leaderboard snapshots before bind while preserving canonical valid state
   and future-schema quarantine. Exact clean-tree suites and module totals pass.
10. **Quest consequence deadline indexing — complete:** commit `f5efa30f0c`
   keeps the persisted list canonical while polling at most 16 due actions from
   a runtime-only deadline index. Equal-deadline order is stable; the exact
   clean-tree index suite and all 15 protected Quests tests pass.
11. **Offering anchor location indexing — complete:** commit `684ee54685`
   keeps persisted anchors canonical while rebuilding and maintaining a
   runtime-only world/block index. Collision order matches the canonical map;
   exact clean-tree tests and all 40 protected Offerings tests pass.
12. **Character lifecycle work indexing — complete:** commit `fcdd94d214`
   keeps the account map canonical while indexing only pending reset retries
   and cooldown deadlines. The one-second pass no longer scans historical
   active accounts; exact clean-tree tests and all 480 Core tests pass.
13. **Realm runtime reload hardening — complete:** commit `7bac0288a9`
   retains valid relationships, hidden Realms, decisions, and nested votes when
   adjacent rows or collections are recoverably null/malformed. Exact
   clean-tree storage tests and all 483 Core tests pass.
14. **Realm decision runtime indexing — complete:** commit `ac11d8aacf` keeps
   the persisted decision map canonical while projecting pending Realm and
   deadline views, so ordinary pending/expiry work excludes historical rows.
   Exact clean-tree tests and all 485 Core tests pass.
15. **Notification runtime indexing — complete:** commit `9e9a1dcb9c` keeps
   persisted notification rows canonical while projecting recipient, category,
   and expiry lookups; ordinary inbox reads and expiry work exclude unrelated
   history. Exact clean-tree tests and all 486 Core tests pass.
16. **Deferred reward runtime indexing — complete:** commit `9d0b3ef9abc`
   projects pending grants by recipient while retaining persisted grant rows as
   canonical, removing unrelated history scans from reward drawer reads/counts.
   Exact clean-tree tests and all 487 Core tests pass.
17. **Advancement leaderboard ranking — complete:** commit `1c57c9c6c4a`
   maintains a runtime top-10 ordering over Core's persisted leaderboard rows,
   avoiding a full re-sort for each join/advancement projection. Exact
   clean-tree tests and all 487 Core tests pass.
18. **Integration checkpoint — complete:** the protected current worktree at
   `1c57c9c6c4a` passed the 217-task Java 21 build, all 18 AI-context cases, and
   all 267 active CIT definitions on 2026-08-05.

Public gameplay, commands, permissions, save compatibility, network protocols,
major dependency versions, and canonical ownership changes remain approval
gates. Each completed slice records its focused verification and thematic
commit here; no consolidation commit may include unrelated user changes.

### WORLD-01 Managed World Reset

`/e reset world <world>` is the active separate slice. It previews and confirms
a managed-world regeneration, backs up world-scoped addon state with a recovery
manifest, removes placed NPCs, Shrine/offering records, and portal endpoints
tied to that world, then recreates the world from its existing definition.
Definitions and config remain intact. Finish complete terrain recovery,
post-regeneration validation, and failure restoration before treating this
destructive command as release-ready.

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
3. Extend the completed server-authoritative chat selector only when a new
   channel has a bounded eligibility and routing contract.
4. Promote the detailed SMP features in `PLANS.md` one bounded slice at a time;
   the Guild Registrar, six-tab management UI, invitations, roles, and emblem
   editor now operate on canonical Guild state.

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
