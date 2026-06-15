# Elarion Project Rules

This file is permanent policy. `TODO.md` and `PLANS.md` may change as work
progresses; this file changes only when the project's governing contracts
change.

Normative terms `MUST`, `MUST NOT`, `SHOULD`, and `MAY` are intentional.

## 1. Project Identity

- The product name is `Elarion`.
- The mod ID and resource namespace are `elarion`.
- Published artifacts, config directories, network channels, commands,
  translation keys, data IDs, logs, and UI MUST use Elarion identity.
- The Java package root MUST be selected before Fabric source bootstrap and
  recorded in `INDEX.md`. It MUST NOT reuse `com.wdiscute` or an upstream
  package.
- The upstream project name MUST NOT appear in player-facing content,
  metadata, artifact names, package names, resource namespaces, commands, or
  marketing.
- The upstream project name MAY appear only in `LICENSE`, `NOTICE.md`,
  `RULES.md`, `REPLACE.md`, `PLANS.md`, `INDEX.md`, `TODO.md`,
  `reference/README.md`, and commit history where legally or technically
  necessary.
- Do not use branding that implies an official Fabric edition, continuation,
  endorsement, or affiliation with the upstream project.

## 2. Licensing and Provenance

- Preserve the upstream MIT copyright and permission notice in `LICENSE`.
- Any source file substantially adapted from upstream MUST be marked as
  adapted in `INDEX.md`; required copyright and permission text remains in
  `LICENSE` and provenance remains in `NOTICE.md`.
- New Elarion code MUST be distinguishable in history from mechanically
  adapted upstream code.
- The ignored upstream checkout is reference material only. It MUST NOT be
  committed, packaged, copied into a release, or added to a source archive.
- Upstream creative assets MUST NOT enter tracked Elarion source, even
  temporarily.
- Byte-identical, recolored, traced, lightly edited, or format-converted
  upstream assets are prohibited.
- Upstream prose, lore, secrets, names, catalogue entries, roster structure,
  screenshots, and distinctive content combinations are prohibited.
- Third-party code, libraries, fonts, sounds, and art require an explicit
  compatible license and attribution record before use.
- Dependency licenses and redistribution terms MUST be audited before the
  first public build and after every dependency change.
- Legal uncertainty blocks distribution. It does not justify copying first
  and cleaning up later.

## 3. Independent Content Identity

- Elarion MAY implement similar general mechanics, but its expression,
  presentation, tuning, roster, progression, writing, and visual language
  MUST be independently designed.
- Do not port the exact fish roster.
- Do not preserve one-to-one mappings between upstream fish and Elarion fish.
- Do not preserve the same sequence of names, rarity tiers, habitats,
  conditions, rewards, secrets, trophies, or catalogue progression.
- Initial development MUST use a deliberately small placeholder roster. A
  large content roster is not required to validate mechanics.
- Placeholder content MUST NOT embed upstream names or recognizable lore.
- Original content design begins only after the mechanics and data schemas are
  stable enough to avoid repeated manual rework.

## 4. Placeholder Contract

Until final content is authored:

- Registry IDs use neutral names such as
  `elarion:placeholder_fish_001`.
- Translation values use visible tokens such as
  `[REPLACE: fish.placeholder_fish_001.name]`.
- Lore uses tokens such as
  `[REPLACE: fish.placeholder_fish_001.lore]`.
- UI copy uses tokens such as `[REPLACE: ui.catalogue.title]`.
- Temporary textures MUST be newly generated development placeholders and
  MUST be visually obvious as placeholders.
- Placeholder sounds MUST be silent, generated, or independently licensed.
- Placeholder models SHOULD use simple vanilla-parent JSON authored for
  Elarion.
- No user-facing English string may be hard-coded in Java when a translation
  key can represent it.
- Every placeholder surface MUST be listed in `REPLACE.md`.
- Replacement work MUST preserve stable technical IDs unless a migration is
  deliberately implemented.

## 5. Design Rules

- Establish an Elarion design language before final asset production:
  palette, typography constraints, icon grid, texture resolution, shape
  language, animation cadence, sound direction, and writing voice.
- UI layouts MUST prioritize readable hierarchy and Minecraft-native input
  behavior rather than reproduce upstream screen composition.
- UI MUST scale correctly across supported GUI scales and common aspect
  ratios.
- Color MUST NOT be the only carrier of state. Important status requires shape,
  icon, text, or motion reinforcement.
- Text MUST be localizable. Layouts MUST tolerate longer translations.
- Effects MUST respect Minecraft accessibility settings where applicable.
- Animation and particles MUST be bounded and configurable when they can
  affect performance or visibility.
- Final art MUST be independently authored from a fresh brief. Upstream assets
  may inform a list of required functions, not visual execution.

## 6. Architecture and Ownership

- Core owns canonical truth for definitions, catches, progression, runtime
  sessions, rewards, relationships, permissions, and persistence.
- Addons MUST use stable Core APIs, registries, callbacks, or events. They MUST
  NOT access another addon's private state or create shadow copies of Core
  state.
- Each stateful concept MUST have one named owner documented in `INDEX.md`.
- Separate these concerns:
  - immutable or reloadable definitions;
  - validated registry/index state;
  - per-world persistent runtime state;
  - per-player persistent runtime state;
  - ephemeral session state;
  - client presentation state.
- Public extension points MUST expose narrow contracts, not internal mutable
  collections.
- Cross-module calls MUST depend on interfaces or stable service contracts
  when direct ownership would otherwise become ambiguous.
- Avoid utility dumping grounds, global mutable singletons, and registries
  that also perform gameplay, persistence, networking, and rendering.
- Do not create an abstraction until it has a concrete ownership or reuse
  benefit. Do create an extension point now when a planned addon clearly
  requires it.

## 7. Fabric Port Boundary

- Target Minecraft `1.21.1`, Fabric Loader, Fabric API, and Java 21.
- Use Fabric Loom and official/intermediary mappings chosen by the project.
  Mapping choice MUST be recorded in `INDEX.md`.
- NeoForge event bus, deferred registers, capabilities, attachments, data
  maps, config APIs, networking wrappers, and loader checks MUST be translated
  to deliberate Fabric equivalents.
- NeoForge imports, metadata, access transformers, and generated resources
  MUST NOT remain in production source.
- Loader adaptation code MUST stay at the boundary. Gameplay rules MUST not be
  coupled to Fabric callbacks when a small domain service can own the rule.
- Client initialization MUST use a dedicated client entrypoint and client-only
  packages/source sets.
- Mixins are a last-mile integration tool. Every mixin MUST be minimal,
  documented in `INDEX.md`, covered by a behavior test where feasible, and
  reviewed for update fragility.
- Access wideners require the same justification as mixins.
- Do not mechanically translate all 364 upstream Java files. Port one bounded
  vertical slice at a time and delete inherited architecture that Fabric does
  not need.

## 8. Data-Driven Content

- Fish, loot behavior, rarity, conditions, modifiers, rewards, trophies, and
  catalogue metadata SHOULD be data-defined when runtime behavior permits.
- Definitions MUST use explicit codecs or structured serializers.
- Every reloadable definition MUST be validated before publication.
- Reload MUST be atomic: build and validate a new snapshot, then swap it in.
- Invalid entries MUST report resource ID, field, and actionable reason.
- Runtime code MUST consume precomputed indexes, not repeatedly scan raw
  resource collections.
- IDs are persistence contracts. Renames require aliases or migrations.
- Config schema MUST be explicit, versioned where persistent, validated, and
  reload-safe.
- Editable definitions belong in config/datapacks. Runtime state belongs in
  world/player persistence, never in editable definition files.

## 9. Runtime and Persistence

- The server is authoritative for catches, rewards, inventory changes,
  progression, tournament results, and persistent records.
- Persist only canonical state. Rebuild indexes and derived caches.
- Persistent schemas MUST include a version and migration path before release.
- Persistence changes require round-trip, reload, and restart tests.
- Writes SHOULD be queued or batched where gameplay volume can grow.
- Do not rewrite a complete global dataset for one player's ordinary action.
- History expected to grow MUST use bounded indexes, summaries, or archival
  segments before it becomes player-facing.
- JSONL MAY be used only for append-only diagnostics or bounded interim
  storage. It is not an acceptable long-term query engine for Chronicle,
  search, newspapers, ledgers, dashboards, or archives.
- Corrupt or unknown records MUST fail safely with diagnostics. Silent data
  loss is prohibited.

## 10. Networking and Security

- Use typed Fabric networking payloads with explicit codecs.
- Validate packet direction, sender state, permissions, identifiers, ranges,
  collection sizes, and text lengths on the server.
- Clients request actions; they do not declare authoritative outcomes.
- Never send complete global history or registries for an ordinary screen.
- Synchronization MUST be scoped, bounded, and incremental where practical.
- Protocol changes that affect compatibility MUST be versioned.
- Commands MUST define permission behavior, help behavior, error behavior, and
  tests for registration and execution.
- Admin/debug commands MUST not expose secrets or unbounded data dumps.

## 11. Performance

- No per-tick global player, entity, world, chunk, history, ledger, or
  definition scans.
- Tick work MUST be bounded by the active session or local object that owns it.
- Prefer events, scheduled deadlines, dirty flags, indexes, and cache
  invalidation over polling.
- Parse data at load/reload time, not repeatedly during gameplay.
- Cache expensive derived lookups with explicit ownership and invalidation.
- Avoid synchronous disk IO on server or render hot paths.
- Avoid loading all records for player-facing queries.
- Features that can grow with server lifetime MUST define limits,
  pagination/windowing, and diagnostics before release.
- Performance-sensitive systems require tests, counters, timings, or another
  proof that work remains bounded.

## 12. Dependencies and Compatibility

- Every dependency needs a documented purpose, version policy, side,
  optionality, license, and failure mode.
- Prefer Fabric-native libraries with active 1.21.1 support.
- Do not copy upstream bundled JARs or assume NeoForge dependency parity.
- Optional mod integrations MUST be isolated behind compatibility modules and
  loader checks. Core classes MUST load without optional mods.
- Integrations MUST translate external data into Core contracts. They MUST NOT
  become alternate owners of fish, progression, rewards, or player state.
- Compatibility failures should disable only the affected integration and
  produce actionable diagnostics.

## 13. Testing and Verification

- Every feature requires tests proportional to its ownership and failure risk.
- Data codecs require valid, invalid, defaulting, and reload tests.
- Persistence requires round-trip, reload, migration, and restart coverage.
- Commands require registration, permission, help, invalid-input, and
  execution coverage.
- Networking requires encode/decode and server-validation coverage.
- Client code requires dedicated-server classloading verification.
- Datagen output MUST be reproducible and validated.
- Bug fixes SHOULD include a regression test.
- A clean build, tests, datagen validation, and
  `scripts/check-project-policy.ps1` are release gates.

## 14. Documentation and Change Control

- `INDEX.md` is updated in the same change as every new, moved, renamed, or
  deleted production code unit.
- `REPLACE.md` is updated in the same change as every new, replaced, or removed
  asset or user-facing content surface.
- `TODO.md` contains only active and recently completed implementation work.
- `PLANS.md` contains stable phases and future design, not daily status.
- Ownership, persistence, command, config, API, behavior, or extension-point
  changes require documentation updates.
- Do not duplicate plans across documents. Link to the canonical document.

## 15. Strictly Prohibited Shortcuts

- Copying the upstream resource tree into Elarion.
- Search-and-replace rebranding of upstream content.
- Shipping upstream placeholders while waiting for final art.
- Reusing upstream fish names, lore, secret text, catalogue text, screenshots,
  sounds, icons, models, textures, animations, or roster identity.
- Treating the ignored reference checkout as a source dependency.
- Direct mutable cross-addon state access.
- Unversioned persistent formats.
- Unbounded player-facing queries.
- Global tick scans used as a substitute for ownership or events.
- Client-authoritative rewards or progression.
- Optional integration classes loaded by Core without guards.
- Mixins used where a Fabric callback or owned service is sufficient.
- Exposing a feature before its storage/query path can scale safely.

## 16. Release Gate

A build may be called releasable only when:

- identity is exclusively Elarion outside legal/reference exceptions;
- the upstream checkout is not tracked or packaged;
- no upstream asset is present or byte-identical;
- all visible placeholders intended for the release are replaced;
- content identity is independently reviewed;
- dependency licenses are recorded;
- dedicated server startup succeeds;
- tests, policy checks, and datagen validation pass;
- persistence and protocol versions are documented;
- `INDEX.md`, `TODO.md`, `PLANS.md`, and `REPLACE.md` match reality.
