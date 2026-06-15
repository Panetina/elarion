# Elarion Current Work

This file tracks active implementation work only. Stable future phases belong
in `PLANS.md`.

## Done

- [x] Establish permanent engineering and architecture rules.
- [x] Preserve the upstream MIT code notice in `LICENSE`.
- [x] Add provenance and asset exclusion notice.
- [x] Clone branch `2.4-neoforge-1.21.1` at commit
  `06b2bd98c8db30f9eacfebfab04aa070e28a4e8b`.
- [x] Keep the full upstream checkout local and ignored to prevent accidental
  redistribution of All Rights Reserved assets.
- [x] Inventory upstream asset, text, model, sound, data, and content surfaces.
- [x] Define placeholder naming and replacement tracking.
- [x] Add an executable policy guard.
- [x] Create the Fabric addon shell in the monorepo.
- [x] Register the Core `elarion:addon` entrypoint with no gameplay behavior.
- [x] Keep the pinned NeoForge reference ignored and outside Gradle source
  roots.
- [x] Document that future catch history, rarity summaries, and title-facing
  progression state are Core-owned.
- [x] Add restart-safe `PORTING_LOG.md` for Angling slices.
- [x] Add reference subsystem audit with adapt/redesign/defer/reject decisions.
- [x] Add file-level reference index for future port slices.
- [x] Add decision-complete data model spec for the next Java slice.
- [x] Implement the data model skeleton: placeholder fish definition, rarity,
  condition identifiers, immutable index, validation, and bounded lookup tests.
- [x] Add the model skeleton rows to `INDEX.md` in the same slice as the Java
  files and tests.
- [x] Write the decision-complete definition loader spec and choose a pure
  JSON-to-index loader before Fabric reload/resource integration.
- [x] Implement the pure JSON-to-index loader with document and field-scoped
  parse diagnostics.
- [x] Add loader rows to `INDEX.md` in the same slice as the loader Java files
  and tests.
- [x] Write the Fabric resource reload and atomic snapshot spec using the
  Fabric-native server-data reload foundation.
- [x] Implement the Fabric server-data reload listener and atomic snapshot
  repository.
- [x] Add reload rows to `INDEX.md` in the same slice as the reload Java files
  and tests.
- [x] Write the decision-complete placeholder fish data resource and reload
  smoke-validation spec.
- [x] Add exactly one placeholder fish data resource and reload smoke test.
- [x] Record the placeholder translation and data resource in `REPLACE.md`.
- [x] Add the Core-owned immutable catch telemetry event transport and Angling
  rarity ID mapping.
- [x] Add Core event-bus and Angling contract tests for catch telemetry.
- [x] Define the Core catch telemetry processing, persistence, replay, and
  summary architecture before wiring gameplay emission.
- [x] Add stable telemetry event identity/time, the versioned Core accepted
  record, and explicit pure JSONL codec/path tests.
- [x] Add player-partitioned forced journal append and bounded replay with
  checkpoints, exact deduplication, conflict/corruption diagnostics, and
  restart tests.
- [x] Add immutable per-player catch summaries, overflow-safe checkpointed
  projection, atomic snapshot storage, dirty tracking, and restart replay
  tests.
- [x] Add the Core catch telemetry processing service, lifecycle/event-bus
  binding, bounded queued replay, dirty-save lifecycle, and read-only summary
  API.
- [x] Add an immutable placeholder catch result and server-owned resolution
  service that validates the current definition snapshot and emits Core
  telemetry with retry-stable identity.
- [x] Add bounded condition evaluation and deterministic weighted candidate
  selection with fail-closed unknown IDs and hard definition/condition/registry
  limits.
- [x] Add direct-player ephemeral fishing sessions with one active session per
  player, bounded deadline-queue expiry, reload-stable selected IDs, and
  retry-stable completion into Core telemetry.
- [x] Audit Fabric 1.21.1 fishing hooks and connect server bobber fishing
  ticks/unload plus successful retrieval to Angling sessions with Core
  telemetry accepted before vanilla loot generation.
- [x] Add one-shot per-cast candidate selection and placeholder-only,
  direct-player rate-limited action-bar feedback for accepted and unavailable
  catches.
- [x] Add Angling to the aggregate `dev` runtime and verify dedicated-server
  classloading, datapack discovery, addon initialization, and mixin application.

## Next

- [ ] Run the remaining in-world fishing smoke scenarios with a connected
  player: cast, unavailable selection, accepted catch, disconnect, and
  telemetry-failure retry.
- [ ] Specify custom placeholder reward identity, delivery ordering,
  inventory-full handling, duplicate prevention, and recovery before adding
  any item or inventory mutation.
- [ ] Keep selecting Fabric-native variants when translating NeoForge systems;
  do not preserve upstream architecture where Fabric has a cleaner boundary.
- [ ] Add CI for build, tests, datagen validation, dedicated-server startup,
  and `scripts/check-project-policy.ps1`.
- [ ] Audit every upstream dependency for Fabric availability, purpose,
  license, optionality, and replacement strategy.
- [ ] Continue the first playable vertical slice after selection is proven:
  one server-authoritative session, one placeholder reward, and minimal client
  feedback.

## Blocked on Manual Content

- [ ] Elarion visual design brief.
- [ ] Original mod icon.
- [ ] Original texture/model set.
- [ ] Original sound set or decision to remain silent.
- [ ] Original fish roster and naming.
- [ ] Original item names, lore, UI copy, advancements, guide, catalogue, and
  narrative.

These items do not block mechanics development while placeholders follow
`REPLACE.md`.

## Recently Deferred

- Full catalogue, tournament, aquarium, selling, compatibility, and archive
  features remain deferred until the first vertical slice and scalable state
  contracts are proven.
