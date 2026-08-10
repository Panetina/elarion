# Elarion Revamp Remaining Roadmap

Date: 2026-07-18

Status: complete. Phases 10-14 finished on 2026-07-18. Completed implementation
history remains in the indexed system/addon docs and `docs/ai/archive/`.

## Direction

The foundational architecture is complete. Remaining work consolidates shared
contracts, hardens correctness and performance, closes verification gaps, and
removes confirmed duplication without another broad rewrite.

Core owns shared infrastructure and canonical shared truth. Addons retain
canonical domain values and state. New shared systems must discover addon-owned
data through typed registration and bounded owner-maintained projections, not by
copying state into Core or reading addon storage.

## Phase 10 - Placeholder Consolidation

**Status: complete (2026-07-11).** The audit, Core contracts, bounded engine,
public system API, compatibility aliases, pilot, and active proven migrations
are implemented. See `docs/reports/PLACEHOLDER_CONSOLIDATION_AUDIT.md` and
`docs/systems/PLACEHOLDERS.md`.

### Purpose

Consolidate independently implemented placeholders and text substitutions,
including server identity and terminology tokens, NPC dialogue substitutions,
Chronicle metadata, and Quest, Government, Offering, Portal, notification,
command, and UI formatting tokens.

Core owns placeholder contracts, registration, bounded resolution, visibility
enforcement, diagnostics, and compatibility aliases. Addons own and resolve
their domain values through Core APIs.

Every placeholder must define:

- stable namespaced id and canonical owner
- typed value and permitted rendering contexts
- required input/context fields
- missing and unauthorized behavior
- visibility/permission policy
- bounded side-effect-free resolver
- compatibility aliases and deprecation policy where needed
- documentation and focused tests

Resolution must use already-available context or bounded owner-maintained
summaries. It must never scan storage, history, worlds, players, or ledgers;
perform network calls or writes; mutate state; or broadly parse files while
formatting. The engine must bound placeholder count, output length, nesting
depth, cycles, diagnostics, and request-local memoization. Unknown tokens remain
safe and visible enough for diagnosis without exposing data or inventing values.

### Delivery Slices

1. **Placeholder Consolidation Audit** - audit only. Inventory syntax, keys,
   owner/source, consumers, contexts, privacy, missing behavior, runtime cost,
   compatibility, conflicts, migration order, and risk. Deliver a source-backed
   report and bounded registry proposal. Do not implement or migrate behavior.
2. **Core Contracts** - descriptor, typed value/result, immutable context,
   resolver registration, visibility decision, missing/unauthorized states,
   aliases, limits, and diagnostics. Do not migrate every subsystem.
3. **Core Resolution Engine** - deterministic lookup, aliases, permissions,
   limits, cycle detection, request memoization, safe fallbacks, and bounded
   conflict diagnostics, with failure-mode and boundedness tests.
4. **Low-Risk Pilot** - migrate Core server identity and Realm terminology.
   Prove identical output, no rendering-time storage access, compatibility
   aliases, server authority, and incremental caller migration.
5. **Owner Migrations** - one domain per approved slice: Economy; NPC dialogue;
   Government and Offerings; Portals and Quests; Chronicle metadata; then shared
   UI/notifications where appropriate. Delete legacy logic only after all
   consumers and compatibility tests migrate.

### Acceptance Criteria

- All active placeholder families are inventoried.
- Every migrated token has one owner and stable namespaced id.
- Resolution has no storage scans, writes, mutation, network calls, or
  unbounded work.
- Privacy, permission, aliases, cycles, limits, missing data, and conflicts are
  tested.
- Addons register values without exposing storage or copying state into Core.
- Legacy resolvers are removed only after complete consumer migration.
- Future-addon registration and consumption are documented.
- Focused, cross-addon, and final affected builds pass.

## Phase 11 - Profile Aggregation Completion

**Status: complete (2026-07-11).** Core filtering and linked-profile transport,
NPC faction reputation, Government indexed office history, Groups membership,
and Mount unlock projections are implemented. See
`docs/reports/PROFILE_AGGREGATION_COMPLETION.md`.

Complete owner-maintained, server-authorized projections for NPC reputation,
Government history, groups, unlock categories, and player-to-player profile
opening. Sensitive fields are filtered before synchronization. Profile opening
must use typed server-authored player references and bounded summaries; it must
never parse display text or trigger broad storage/history scans.

## Phase 12 - Persistence And Performance Corrections

**Status: complete (2026-07-12).** The approved reload/startup safety,
boundedness, idempotency, and no-scan corrections are complete. See
`docs/reports/PHASE_12_COMPLETION.md`.

Select evidence-backed risks from current source, focused verification, and
Git-backed completion evidence: reload inconsistencies, bounded indexes, stale
caches, blocking IO, payload sizes, save safety, and initialization hazards.
Every persistence migration is a separately approved slice with schema,
backup/failure behavior, round-trip, reload, restart, and compatibility
coverage. Do not perform speculative micro-optimization or broad storage
rewrites.

## Phase 13 - Duplicate And Structural Cleanup

**Status: complete (2026-07-17).** Classified Portal responsibilities were
extracted behind the stable facade, and the project-wide duplicate pass found
no further deletion that was both safe and evidence-backed. See
`docs/reports/PHASE_13_COMPLETION.md`.

Classify before deleting: canonical implementation, required adapter,
domain-specific implementation, deprecated compatibility layer, safe duplicate,
or uncertain. Remove only confirmed duplicate models, utilities, formatters,
obsolete adapters, dead screens/assets, and misleading shells. Split oversized
classes only where an existing ownership boundary provides a clean extraction
point.

## Phase 14 - Final Verification And Maintenance Guide

**Status: complete (2026-07-18).** Builds, GameTests, startup/restart,
optional-addon absence, client/onboarding/authority checks, representative UI
and resource QA, deployment planning, exports, and extension documentation are
recorded in `docs/reports/PHASE_14_COMPLETION.md` and the verification matrix.

Complete focused and full builds, GameTests, dedicated-server startup,
optional-addon compatibility, restart/persistence verification, representative
UI flows, final source/document consistency audit, and extension guides for
config domains, Admin pages, UI components, placeholders, profile sections,
Chronicle families, NPC actions/conditions, notifications, and addon
integrations.

## Cross-Phase Unfinished Work

- Remaining targeted UI screenshot QA.
- Chronicle families only when corresponding gameplay exists.
- Reset, Underworld, lifecycle, tablist, and grave-reconciliation verification.
- Structural cleanup candidates such as oversized services or duplicated
  formatting/helpers.

## Credit-Efficient Workflow

For every slice:

1. Start from `docs/ai/routes.json` and a 6,000-token context capsule.
2. Search only the selected domain and known patterns.
3. Read full source for edit targets; use signatures/exact sections for support.
4. Keep audit, contracts, implementation, migration, tests, and docs as separate
   reviewable slices.
5. Run focused tests first and full builds only for cross-module contracts or
   final handoff.
6. Do not load `docs/ai/archive/**` unless historical investigation is explicit.
7. Stop and deliberately expand only when required ownership or behavior cannot
   be proven.

Context targets are 6,000 tokens by default, 12,000 maximum for an ordinary
slice, and 24,000 maximum for justified cross-module Core contracts. These are
retrieval budgets, not permission to weaken source inspection, authority,
tests, documentation, or verification.

## Completed Foundation

Completed revamp foundations include architecture/config/UI/dependency audits;
bounded AI context routing; typed config descriptors; Admin config discovery,
validation, and initial safe mutations; shared civic UI primitives; Government
and notification redesign; Character Menu/profile boundaries; player-facing UI
migrations; Worldheart/Realm treasury, authority, tax, merchant, and pricing
foundations; deterministic Chronicle families; NPC narrative state; modular
Portal/world configuration; Mount preview/animation fixes; and development QA,
export, and logging helpers.

## Immediate Next Slice

None. Start the next concrete feature or hardening request as a new bounded
slice using `docs/systems/EXTENSION_GUIDE.md`. Do not reopen this broad revamp
for ordinary feature work.
