# Phase 12 Persistence And Performance Completion

Date: 2026-07-12

## Status

Phase 12 is complete for the currently approved revamp scope.

The phase corrected the evidence-backed persistence and performance risks that
were safe to address without opening a new persisted-schema migration or broad
storage rewrite. Remaining work is no longer classified as Phase 12 execution:
structural splits belong to Phase 13, and broad runtime/manual verification
belongs to Phase 14.

## Completed Corrections

- Economy reload is atomic across `economy.yml` and `service_prices.yml`.
- Underworld reload preserves the previous valid `underworld.yml` snapshot when
  reload YAML is malformed.
- Offering reload commits project definitions and Shrine UI config together.
- Government reload commits settings and form definitions together.
- Realms protection startup logs malformed `protection.yml` and uses safe
  defaults instead of aborting addon initialization.
- Mounts Collection text startup fallback for malformed `collection.yml` is
  covered by focused tests.
- NPC trade purchase, stock, and sale foundations use bounded idempotency,
  O(1) receipt/purchase lookups, lazy stock restock, and replay-safe journal
  state.
- Economy tax quotes and Worldheart treasury routing use bounded/O(1) maps and
  do not scan treasuries, tax files, or history during ordinary interactions.
- Government expired vote resolution uses a one-second interval instead of
  scanning every tick.
- Collection/Character Menu snapshots are capped before transport.
- Placeholder and profile aggregation paths are bounded, side-effect-free, and
  server-authorized.

## Verification

Focused verification was run during the final Phase 12 slices:

```text
.\gradlew.bat :addons:mounts:test :addons:government:test :addons:realms:test :addons:offerings:test :addons:underworld:test :addons:economy:test --console=plain
```

Result: build successful.

Earlier Phase 12 slices also passed their focused module tests when completed.

## Deferred Out Of Phase 12

These items remain important, but they are not Phase 12 blockers:

- Splitting oversized services such as `PortalRouteService`; this is Phase 13
  structural cleanup and should follow existing ownership boundaries.
- Additional Government command/GameTest coverage; this is Phase 14 verification
  unless a specific mutation bug is found.
- Live screenshot QA for UI families; this is Phase 14 representative-flow QA.
- Dedicated-server startup/restart matrix; this is Phase 14 final verification.
- Future Chronicle/newspaper/library search indexes; implement only when the
  player-facing feature is approved.
- Any persisted-schema migration not already completed; each future migration
  still needs its own proposal, backup/failure behavior, restart tests, and
  approval.

## Next Recommended Phase

Begin Phase 13 with a duplicate/structural cleanup classification slice.

Recommended first slice:

1. Audit one bounded target, preferably `PortalRouteService`, Government
   remaining large helpers, or duplicated UI formatting utilities.
2. Classify each candidate as canonical, adapter, domain-specific, deprecated,
   safe duplicate, or uncertain.
3. Do not delete or split code until classification is documented.

Recommended model: Medium for an audit/classification slice. Use High only for
an approved structural extraction or migration.
