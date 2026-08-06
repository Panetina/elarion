# TODO

Active incomplete work only. Detailed superseded lists are archived at
`docs/ai/archive/TODO_THROUGH_2026-07-11.md`.

## Current Implementation Work

### `WORLD-01` Managed World Regeneration

Complete `/e reset world <world>` as a separate destructive-command slice.
It needs executor-bound preview/confirmation, a complete streamed backup,
selected-world evacuation/unload/recreation from its existing definition,
validation, and failure restoration. `ADM-01` player reset must not contain
dimension deletion or regeneration.

### Angling Fabric Release Gate

Keep public fishing disabled until all remaining parity evidence is complete:

- exact fish and bobber renderer/model mappings;
- remaining typed catch/minigame behavior, compatibility catch tags, and the
  bounded Fabric loot-hook replacement;
- live kill/restart GameTests for bait debit, title reconciliation, and action
  replay.

The owner-authorized local reference remains the direct port source. Do not
replace its roster/assets with placeholders or enable partial public gameplay.

## High Priority Backlog

- Add a character-life path choice during the three-stage creation flow (name,
  biography, or Realm placement): `Ember` uses the ordinary death/Underworld
  loop; `Ashen` is opt-in hardcore/permadeath. An Ashen character remains in
  the Underworld after death until the player voluntarily surrenders that soul
  to reset and create a new character. Surrender must retire the character,
  immediately redact its name from Chronicle/history/books/website projections,
  and remove its live identity data while retaining access-controlled recovery
  backups for verified bug, exploit, or compromise restoration. Design the
  Core-owned lifecycle, retention, redaction indexes, confirmation/reversal,
  and restart/audit tests before exposing this choice. While living, Ashen
  characters visibly use the `Ashen` identity title instead of `Ember`, and
  official civic titles gain the same prefix (for example `Ashen Monarch` and
  `Ashen President`).
- When the real Core admission queue is implemented, consume
  `PlayerRestrictionService.QUEUED_ADMISSION` before admitting queued accounts
  and immediately disconnect any already-admitted banished account when queue
  pressure begins. A banished account may join only when capacity is available
  and the queue is empty. Preserve fair ordering for ordinary and Supporter
  players; Underworld must not own queue state.

- Phase 14: exercise one guarded live deployment/startup verification cycle and
  record rollback/startup-log evidence. Do not deploy merely to test the tool.
- Design and implement the USD 5/month `Supporter` membership as a bounded
  post-release slice. The website/payment provider owns recurring billing,
  purchase history, cancellation, and the paid-through expiry; Core consumes a
  signed expiring entitlement without synchronous billing calls. Benefits are
  limited to starvation-safe queue priority that never kicks an online player,
  capacity-aware AFK grace, toggleable cosmetic title/chat/name presentation,
  and synchronized Discord role/community recognition. Add automatic expiry,
  idempotent renewal/reconciliation, restart/outage tests, audited expiring
  manual overrides, public recurring-price/cancellation disclosures, and a
  Minecraft Usage Guidelines review before launch. Do not grant gameplay,
  Economy, Realm-politics, moderation, or rule-bypass advantages.
- Add future owner-specific website projection adapters only with concrete
  player-facing pages. The current Guild lore authority projection, Shrine aggregates, the persisted
  advancement top-10, and typed bounded map markers are implemented; new
  adapters must not scan canonical storage or duplicate owner state. Religion
  authority waits for a standalone canonical religion owner contract.
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
- Manually verify reset preservation, tablist Realm headers, Underworld V2,
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
- Portal responsibilities are extracted behind one canonical facade. Split
  additional services only when an existing ownership boundary proves it.
- Add GameTests for persistence mutations where focused unit coverage is not
  sufficient.
- Keep shell addons clearly marked until they own real behavior.

## Documentation

- Keep affected system/addon docs and wiki pages synchronized according to
  `RULES.md`.
- Add source line references only when the associated subsystem is next
  changed.
- Keep historical completion logs out of current status, plan, and TODO files.
