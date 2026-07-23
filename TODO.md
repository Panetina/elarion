# TODO

Active incomplete work only. Detailed superseded lists are archived at
`docs/ai/archive/TODO_THROUGH_2026-07-11.md`.

## Current Slice

### First Execution Roadmap

1. `F-01` Afterlife inventory and protection: persist separate Living and
   Afterlife inventory state for every rank and game mode before destructive
   administration tooling relies on that ownership boundary. Living death
   items stay in corpse/grave recovery; Obols and approved Underworld/Limbo
   resources stay in the Afterlife inventory across visits and cannot enter
   living worlds. Disable Underworld/Limbo PvP, prevent banished movement-only
   players from picking up items/XP or receiving progression, and cover entry,
   return, repeated death, logout, restart, game-mode, banishment, and grave
   recovery round trips.
2. `ADM-01` Complete player-data reset: add `/e reset players` as a preview-
   first, executor-bound, expiring confirmation flow. Back up affected state,
   kick affected online players, and invoke registered domain-owned reset
   handlers for vanilla UUID state, Core citizen/character state, Realm
   membership and progression, notifications, Shrine progression, tombstones,
   and Underworld/Afterlife state. The confirmed reset also clears backed-up
   `ops.json`, `whitelist.json`, the persisted/in-memory profile cache, and
   vanilla playerdata/stats/advancements so stale offline UUIDs cannot survive.
   Preserve terrain, dimensions, buildings,
   placed Shrine blocks, configured NPCs, portals, Seats of Rule, Realm/world
   definitions, configuration, and map infrastructure. This is the active
   implementation slice.
3. `WORLD-01` Managed world regeneration: add `/e reset world <world>` in a
   separate run. Reuse managed-world validation and suggestions, require an
   executor-bound preview/confirmation, stream a complete world backup,
   evacuate and unload the selected world, recreate it from its configured seed
   and current world-generation stack, validate it, and restore on failure.
   Regeneration deliberately removes everything physically inside that world.
4. `L-01` Launcher obsolete-file cleanup.
5. `W-01` Wiki editor simplification.

`ADM-01` and `WORLD-01` must remain separate implementation slices. The player
reset may not contain dimension deletion or regeneration logic.

- Complete Angling's Fabric registries for custom items, bucketable fish,
  blocks, block entities, entities, recipes, screen handlers, and particle
  rendering consumers. The 198 registered items include all 15 component-safe
  rods, the basic/native roster, 48
  bucketable fish items, the caught-fish water bucket, generic persistent fish
  entity, fishing-bob entity shell, all sound events, all three particle
  types/factories, and all 12 durable components are done. The fish renderer's
  55 model mappings remain gated, so the bucketable entity slice is not yet
  player-ready.
- Complete the remaining typed catch/minigame modifier behavior, compatibility
  catch-tag lookup, and the bounded Fabric global-loot hook. Catch selection,
  stable inline pool entries, treasure resolution, exact multi-stack outcome
  persistence, and the implemented equipment modifiers are server-owned.
- Integrate the deterministic server minigame session into the live Fabric
  bobber entity and client screen. Typed start/state payloads, the owner/entity/
  session/replay/rate input gate, live bobber/index/selection/commit runtime,
  the native/equipment modifier behaviors implemented so far, all nine compiled sweetspot behaviors, holding,
  layers, seeded placement, terminal idempotency, and the four-state/80-tick
  bobber timing core are implemented and tested. The receiver resolves only a
  same-world, same-owner bobber host. The authoritative client screen
  foundation now renders server snapshots and emits only input edges. The
  explicit public gameplay gate remains false until exact entity/fish
  rendering and remaining modifiers pass. Clients send sequenced hit, layer-selection, and
  abandon edges but never duration, hits, perfect, treasure, or success.
- Add completed-event retention summaries, cursor-based offline title
  reconciliation, and web projections. Versioned `MetricUpdatedEvent`,
  metric-indexed title conditions, online lazy reconciliation, append-first
  persistence, atomic restart restoration, bounded workers, diagnostics, and
  the read-only/submit Core API are complete and bound.
- Continue semantic screen/rendering/config/tournament/Delight ledgers with
  their implementation slices. Do not enable catch selection before its full
  native registry and server-authority dependencies validate together.
- Add live kill/restart GameTests for the append-first bait debit projection,
  player-file cursor reconciliation, missing bait, and completed-action replay.
- Use the owner-authorized local reference as the direct port source; do not
  create a duplicate placeholder roster or schedule a replacement-art phase.

## Remaining Revamp Sequence

- Complete. Final evidence: `docs/reports/PHASE_14_COMPLETION.md`.

Full acceptance criteria and slice boundaries:
`docs/architecture/REVAMP_REMAINING_ROADMAP.md`.
Executable Phase 14 evidence matrix:
`docs/reports/PHASE_14_VERIFICATION_MATRIX.md`.

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
- Remove vanilla `/say` from registration and verify it cannot bypass Core
  proximity chat, Underworld/banishment restrictions, or future Jail gates.
  Update command help/policy tests in the same slice.
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
  player-facing pages. The current Group lore authority projection (planned to
  migrate to Guild naming in `GUILD-01`), Shrine aggregates, the persisted
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
