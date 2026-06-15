# Elarion Fabric 1.21.1 Port Plan

This is the stable implementation plan. Daily status belongs in `TODO.md`.
Phases are sequential unless a phase explicitly permits parallel work.

## Guiding Strategy

Port behavior by bounded vertical slices, not by copying the upstream tree.
Each slice establishes an owned domain contract, its Fabric adapter,
persistence/network behavior where needed, tests, docs, and placeholders.

The upstream branch contains 364 Java files and no test files were found.
That makes mechanical translation high risk. Elarion will treat upstream code
as a behavior reference and selectively adapt MIT-licensed logic into a new
Fabric architecture.

## Phase 0: Legal, Identity, and Repository Controls

Status: complete for initial setup.

Deliverables:

- permanent policy and strict restrictions;
- MIT license notice and provenance notice;
- ignored local upstream reference pinned to an exact commit;
- asset/content replacement inventory;
- policy script preventing obvious contamination;
- canonical Elarion name, mod ID, and namespace.

Exit criteria:

- reference checkout is ignored;
- legal and replacement rules are documented;
- policy check passes.

## Phase 1: Fabric Build Foundation

Status: in progress. The addon shell exists; CI and launch validation remain
pending.

Work:

- choose Java package root and mapping set;
- configure Gradle, Fabric Loom, Fabric Loader, Fabric API, Java 21, and
  reproducible repositories;
- add common and client entrypoints;
- add Elarion-only `fabric.mod.json`;
- configure source sets so client code cannot load on dedicated servers;
- add unit/GameTest structure, datagen task, formatting/static analysis, and
  CI;
- add development run configurations;
- generate only independent placeholder icon/resources.

Exit criteria:

- clean build succeeds;
- client and dedicated server start;
- datagen runs reproducibly;
- policy check runs in CI;
- `INDEX.md` contains every production path.

## Phase 2: Upstream Behavior and Dependency Audit

Status: in progress. The subsystem audit and file-level reference index exist;
the dependency matrix remains pending.

Work:

- map each upstream subsystem to `retain`, `redesign`, `defer`, or `remove`;
- identify NeoForge-specific registrations, events, attachments, networking,
  config, render hooks, data maps, capabilities, and access changes;
- identify code that is loader-independent and safe to adapt;
- audit dependencies and bundled libraries for Fabric availability and
  licensing;
- define replacement interfaces for unavailable dependencies;
- record compatibility modules separately from Core.

Required output:

- subsystem matrix with upstream paths, intended Elarion owner, Fabric
  mechanism, data/persistence impact, and test strategy;
- dependency matrix with license and optionality;
- no code copied before its destination owner is known.

Exit criteria:

- first vertical slice has no unresolved architecture or dependency owner.

## Phase 3: Angling Content Definitions

Status: next code phase.

Work:

- define codecs for placeholder fish, conditions, rarity, rewards, and
  minigame behavior references;
- implement reload listeners that build immutable validated snapshots;
- precompute indexes for biome, dimension, weather/time, bait, rarity, and
  other retained conditions;
- produce diagnostics with resource ID and field path;
- expose read-only query APIs to Angling internals first, then to Core or
  addons only after ownership and event boundaries are stable;
- implement a tiny placeholder dataset, not the upstream roster.

Performance contract:

- no gameplay-time full definition scan;
- no repeated JSON parsing;
- reload publication is atomic;
- indexes have explicit invalidation on successful reload.

Exit criteria:

- valid/invalid/default/reload tests pass;
- one placeholder definition can be queried through bounded indexes.

## Phase 4: Registration and Basic Content Shell

Status: planned.

Work:

- establish registrars for retained items, blocks, entities, components,
  sounds, particles, screen handlers, recipes, and commands;
- keep registrars declarative and free of gameplay policy;
- add neutral placeholder items/models/text;
- implement data generation for models, recipes, loot tables, tags, and
  advancements only as required by the first slice;
- confirm all IDs use `elarion`.

Exit criteria:

- generated resources are deterministic;
- missing placeholders are obvious and listed in `REPLACE.md`;
- dedicated-server classloading remains clean.

## Phase 5: Fishing Vertical Slice

Status: planned.

Scope:

- one placeholder fish;
- one bounded catch-condition query;
- one rod/cast path;
- one server-owned active fishing session;
- one minimal minigame behavior;
- one validated completion request;
- one reward/catch record;
- minimal translatable client feedback.

Architecture:

- callbacks feed an Angling domain service;
- session lookup is direct by player/entity ID;
- deadlines are scheduled or checked only for active sessions;
- client sends input, server resolves outcome;
- resolved catches emit bounded Core-facing events later for durable history,
  rarity summaries, title progress, and Chronicle-facing data;
- no global tick scan;
- no final upstream art or names.

Exit criteria:

- complete client/server gameplay loop works;
- spoofed/invalid packets fail safely;
- session cleanup handles logout, death, dimension change, rod loss, and
  server stop;
- tests prove bounded lookup and deterministic resolution.

## Phase 6: Persistence, Progression, and Archive Foundations

Status: planned.

Work:

- define versioned player progression and world state;
- add round-trip, migration, reload, and restart tests;
- separate canonical records from rebuildable indexes;
- define bounded recent-catch index and archive summaries before catalogue or
  Chronicle-style views;
- batch/queue writes where activity volume warrants it;
- add diagnostics for record counts, queue depth, and migration failures.

Exit criteria:

- ordinary player actions never load or rewrite all history;
- restart preserves canonical state;
- schema version and migration ownership are documented.

## Phase 7: Client Presentation Framework

Status: planned.

Work:

- define Elarion visual tokens and UI component rules;
- create screen architecture for responsive GUI scale and localization;
- implement placeholders for HUD/minigame feedback;
- isolate renderers, model layers, particles, shaders, and screen registration
  in client code;
- respect accessibility and effect settings;
- avoid reproducing upstream layouts.

Exit criteria:

- screens work at supported GUI scales and long-text test locale;
- dedicated server does not load client classes;
- every visible placeholder is registered in `REPLACE.md`.

## Phase 8: Retained Gameplay Systems

Status: later.

Implement retained systems one at a time after the vertical slice:

1. tackle and rod modifiers;
2. fish entities and display behavior;
3. storage/tackle boxes;
4. aquarium/multiblock behavior;
5. trophies and progression rewards;
6. guide/catalogue backed by bounded indexes;
7. tournaments backed by active state and archive summaries;
8. messages, selling, radar/tracker, cosmetics, and secrets only after an
   explicit retain/redesign decision.

Each system requires:

- one canonical owner;
- data/runtime/persistence separation;
- Fabric boundary mapping;
- bounded performance proof;
- tests and docs;
- independent content and assets.

## Phase 9: Public API and Addon Boundaries

Status: later.

Work:

- stabilize definition, condition, minigame, reward, and event contracts;
- expose immutable contexts and explicit request/result objects;
- publish lifecycle and reload semantics;
- add API compatibility tests;
- prohibit addons from mutating Core collections or duplicating state.

Angling-owned fishing state remains local to Angling until it becomes durable
player/world history. Durable catch history, rarity summaries, title progress,
History, and Chronicle-facing data are Core-owned.

Exit criteria:

- a sample addon can register one original placeholder extension without
  internal package access;
- API ownership and versioning are documented.

## Phase 10: Optional Mod Compatibility

Status: later.

Work:

- create one isolated adapter per supported Fabric mod;
- use runtime loader checks and avoid hard classloading;
- translate external entities/items/seasons into Core contracts;
- define behavior when the external mod is absent or changes;
- cap compatibility-generated definitions and validate them at reload.

Exit criteria:

- Core runs with no optional mods;
- one failed adapter cannot disable unrelated systems;
- compatibility does not create alternate canonical state.

## Phase 11: Original Content Production

Status: blocked on manual creative work.

Work:

- approve an Elarion visual and writing brief;
- author an original roster without one-to-one upstream mapping;
- replace every placeholder name, lore line, UI string, advancement, guide
  entry, catalogue entry, model, texture, sound, icon, and animation;
- record author/source/license in `REPLACE.md`;
- run content-identity and asset-hash review.

Exit criteria:

- no release-target placeholder remains;
- no upstream creative expression is present;
- localization source text is final enough for translation.

## Phase 12: Release Hardening

Status: later.

Work:

- complete dependency and attribution audit;
- test clean install, upgrade, reload, restart, multiplayer, dedicated server,
  and optional-mod matrices;
- profile high-volume catches, large player counts, long-lived archives, and
  GUI queries;
- verify packet bounds and permissions;
- verify reproducible datagen and build;
- inspect the built JAR for forbidden namespaces, reference files, NeoForge
  residue, and placeholders;
- update all documentation to exact release behavior.

Exit criteria:

- every `RULES.md` release gate passes;
- release JAR contains Elarion code and independently licensed Elarion
  resources only.

## Deferred Ideas

Rich Chronicle, newspaper, search, ledger, admin dashboard, and archive views
remain design ideas until dedicated indexes and archive summaries exist. They
must not query raw append-only history as a player-facing backend.
