# PLAN

Short current-focus memory. Keep this file below 12 KB; completed phase logs
belong under `docs/ai/archive/`.

## Current Direction

The project-wide revamp was completed through small, explicitly approved
slices. Core remains the canonical owner of shared truth and infrastructure.
Addons extend behavior through stable APIs, registries, domain events, and
bounded owner-maintained summaries.

The project-wide revamp is complete through Phase 14. Its final evidence is in
`docs/reports/PHASE_14_COMPLETION.md`; future work begins as a new bounded
feature or hardening slice using `docs/systems/EXTENSION_GUIDE.md`.

## Active Approved Work

### Post-Revamp Finding Remediation Complete

- All review findings are fixed and verified.
- The final evidence is in
  `docs/reports/POST_REVAMP_FINDINGS_COMPLETION.md`.
- Future work starts as a new bounded feature slice.

## Planned Follow-ups

1. Select one concrete feature from `TODO.md` when development resumes.

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
