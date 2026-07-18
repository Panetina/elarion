# TODO

Active incomplete work only. Detailed superseded lists are archived at
`docs/ai/archive/TODO_THROUGH_2026-07-11.md`.

## Current Slice

- None. The approved revamp and post-review remediation are complete.

## Remaining Revamp Sequence

- Complete. Final evidence: `docs/reports/PHASE_14_COMPLETION.md`.

Full acceptance criteria and slice boundaries:
`docs/architecture/REVAMP_REMAINING_ROADMAP.md`.
Executable Phase 14 evidence matrix:
`docs/reports/PHASE_14_VERIFICATION_MATRIX.md`.

## High Priority Backlog

- Phase 14: exercise one guarded live deployment/startup verification cycle and
  record rollback/startup-log evidence. Do not deploy merely to test the tool.
- Add future owner-specific website projection adapters only with concrete
  player-facing pages. Shrine aggregates, the persisted advancement top-10,
  and typed bounded map markers are implemented; new adapters must not scan
  canonical storage or duplicate owner state.
- Promote additional Chronicle families only for concrete player-facing use;
  each family needs ten stable variants, metadata validation, fallback text,
  and tests.
- Run only feature-specific UI QA when those flows change. Phase 14 already
  covers Character Menu, Admin Config, notifications, Portal, resource packs,
  and indexed accepted Shrine/onboarding/bank/trader/mount evidence. Grave
  Recovery edge states remain a focused future flow check.
- Add the shared server-authored player-link/profile-opening contract; do not
  parse client display text or trust client UUID selection.
- Add stable Mount/Pet reward hooks only as a separately approved owner slice.
- Manually verify reset preservation, tablist Realm headers, Underworld V1,
  Character Lifecycle, and component-safe grave recovery/reconciliation.
- Add selection and arbitrary caret navigation to `ElarionTextInput` only as a
  bounded shared-input slice; onboarding no longer depends on editing an
  invalid account-name prefill.
- Retry simultaneous Client One/Client Two screenshot observation on a QA host
  that can sustain two GLFW renderers. Slice 5 proved each client separately;
  both overlapping attempts failed in native `glfw.dll` without Elarion frames.
- After explicit owner approval, remove the isolated remote staging releases
  `20260718-005248` and `20260718-005342`; neither changed live `mods`.

## Architecture and Performance

- Split oversized services/screens only on existing ownership boundaries.
- Phase 13 structural cleanup is complete. Portal responsibilities are
  extracted behind one canonical facade, and no additional safe duplicate or
  dead deletion was proven. See `docs/reports/PHASE_13_COMPLETION.md`.
- Add GameTests for persistence mutations where focused unit coverage is not
  sufficient.
- Keep shell addons clearly marked until they own real behavior.

## Documentation

- Keep affected system/addon docs and wiki pages synchronized according to
  `RULES.md`.
- Add source line references only when the associated subsystem is next
  changed.
- Keep historical completion logs out of current status, plan, and TODO files.

## Needs Investigation

- Shrine completed-state snapshots that show zero projected requirements.
