# TODO

Active incomplete work only. Future ideas and deferred designs belong in
`PLAN.md`; completed work belongs in Git history.

## Implementation

### P0 Verification and Integration

- Merge the independently verified integration branch only after reviewing the
  preserved Desktop WIP and divergent local history; never force-push or reset
  that WIP.

### Angling Release Gate

Keep public fishing disabled until these parity gates pass:

- exact fish and bobber renderer/model mappings;
- remaining typed catch/minigame behavior and compatibility catch tags;
- the bounded Fabric loot-hook replacement;
- kill/restart GameTests for bait debit, title reconciliation, and action
  replay.

The owner-authorized local reference remains the direct port source. Do not
enable partial public gameplay or replace supplied assets with placeholders.

## Verification And Operations

- Exercise one explicitly approved guarded live deployment/startup cycle and
  capture rollback plus startup-log evidence. Never deploy merely to test the
  tool.
- Manually verify reset preservation, tablist Realm headers, Underworld V2,
  Character Lifecycle, and component-safe grave recovery/reconciliation when
  their next implementation slice changes those paths.
- Retry simultaneous Client One/Client Two observation only on a QA host that
  can sustain two GLFW renderers; each authority direction has already passed
  independently on this host.
- After explicit owner approval, remove the isolated remote staging releases
  `20260718-005248` and `20260718-005342`; neither changed live `mods`.

## Maintenance

Update affected authoritative docs and tests in the same implementation slice.
Do not add completed checklists, speculative feature catalogs, or dated work
logs here.
