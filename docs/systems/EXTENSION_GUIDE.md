# Elarion Extension And Maintenance Guide

This is the implementation checklist for extending Elarion without duplicating
canonical state or introducing rendering-time scans. Read the owning system doc
and current source before using any example below.

## Common Contract

1. Choose one canonical owner. Core owns shared truth and infrastructure;
   addons own domain definitions and runtime state.
2. Keep editable definitions under `config/elarion/` and runtime state under
   `world/elarion/`.
3. Register through `ElarionApi`, `ElarionSystemApi`, or the owning addon's
   public API. Never import another addon's storage or internal service.
4. Treat client packets as requests. The server validates permission, inputs,
   state transitions, persistence, and the authoritative response.
5. Add focused tests and update the configuration/API/system/addon docs in the
   same slice. Run the touched module test before a cross-module build.

## Add A Config Domain

- Keep the existing parser and physical format unless a separately approved
  migration requires otherwise.
- Define truthful `ElarionConfigEntry<T>` values with stable IDs, paths,
  codecs, defaults, current-value suppliers, validation, bounds or choices,
  reload/restart flags, and read/write permissions.
- Group entries in `ElarionConfigCategory`, then expose one
  `ElarionConfigDomain` through `api.system().configs().registerDomain(...)`.
- Add a runtime applier only when the owner can validate first, commit
  atomically, roll back on failure, and resynchronize affected clients.
- Test default creation, valid values, invalid values, malformed reload
  rollback, and descriptor parity. Update `docs/config.md` and the addon doc.
- Reference implementations: `CoreConfigDescriptors`,
  `EconomyConfigDescriptors`, and `NpcConfigDescriptors`.

## Add An Admin Panel Contribution

- Implement `ElarionAdminPanelProvider` in the owning module and register it
  through `api.system().adminPanel().registerProvider(...)`.
- Contribute bounded system/Realm rows and explicit actions. Do not read all
  storage while building a snapshot.
- Action IDs and target IDs are stable contracts. The provider rechecks
  permissions and validates parameters on the server; the screen never mutates
  domain state directly.
- Destructive or broad actions require confirmation and a tested failure path.
- Reference implementations: `GovernmentAdminPanelProvider`,
  `OfferingAdminPanelProvider`, and `MountAdminPanelProvider`.

## Add A Shared UI Component

- Put domain-neutral tokens/layout/primitives in Core `client/ui`; keep
  domain-specific presentation wrappers in the owning addon.
- Use `ElarionUiTheme`, `ElarionTypography`, `ElarionCivicUi`, and existing
  semantic layout records before adding coordinates or literal colors.
- Rendering, clipping, scrolling, and hitboxes must use the same calculated
  bounds. Text-bearing controls must use typography metrics and scale-aware
  internal heights.
- Add layout tests for normal, hover/selected/disabled, long text, and supported
  font scales. Add the component to the dev gallery when it is reusable.
- Perform screenshot QA for player-facing changes and record it in
  `UI_JOURNAL.md`. See `GUI.md` for visual and interaction contracts.

## Add A Placeholder

- The owner registers one namespaced `PlaceholderDescriptor` and a bounded,
  side-effect-free `PlaceholderResolver` through
  `api.system().placeholders()`.
- Declare owner, value type, permitted render contexts, required context keys,
  visibility, and missing/unauthorized behavior.
- Resolve only supplied context or owner-maintained bounded summaries. Never
  perform storage/history/world/player scans, IO, writes, network calls, or
  mutation while formatting.
- Add an alias only for a real compatibility requirement; aliases must point to
  one canonical ID and remain cycle-free.
- Test visibility, missing context, unknown tokens, aliases, request limits,
  output limits, nesting, cycles, and memoization. Update `PLACEHOLDERS.md`.

## Add A Profile Section

- Keep canonical data with its owner. Implement `CitizenProfileContributor`
  and register through `api.system().profiles().registerContributor(...)`.
- Read an owner-maintained index or summary; profile opening must not scan raw
  ledgers, history, worlds, or every player.
- Assign stable section/field IDs and conservative `ProfileVisibility`.
  Authorization filters data on the server before synchronization.
- Add tests for self, public, Realm/group/official where supported, admin, and
  denied views. Confirm read paths do not create or mutate state.
- Reference implementations: `GovernmentProfileContributor`,
  `UnderworldProfileContributor`, and `MountProfileContributor`.

## Add A Chronicle Event Or Renderer

- Persist structured event type, IDs, timestamp, scope, actors/targets,
  numeric metadata, visibility, schema version, and selected variant ID. Do not
  persist prose as canonical truth.
- Implement `ChronicleRenderer` and register through
  `api.publicHistory().registerRenderer(...)`.
- A player-facing event family requires ten authored variants, deterministic
  selection, required-metadata validation, localization-ready text, and a safe
  missing-context fallback.
- Publish only meaningful lifecycle outcomes. Ordinary polling, UI opens, and
  routine internal mutations stay silent.
- Test structured serialization, stable wording after restart, all variants,
  missing metadata, and visibility. Update `Chronicles.md`.

## Add An NPC Action Or Condition

- Register stable namespaced types through `ElarionNpcApi.registerAction(...)`
  or `registerCondition(...)`; use Core registry handlers for genuinely shared
  concepts.
- Conditions are side-effect-free and bounded. Actions validate context and
  delegate mutation to the owning server service.
- Add definition validation for required parameters and referenced IDs. Never
  put story, economy, quest, or relationship mutation in `NpcDialogueScreen`.
- Test success, rejection, missing context, invalid parameters, one-time/reentry
  behavior where relevant, and restart persistence for durable changes.
- Reference implementations: `NpcRelationshipRegistryHandlers` and
  `NpcStoryRegistryHandlers`.

## Add A Notification

- Publish through `api.notifications()` with a semantic category, source,
  event type, title/body/status, icon, expiry, metadata, and a stable
  deduplication key when repetition is possible.
- Register server-authored actions once with stable IDs. The handler rechecks
  player, target, permission, availability, and ownership before mutation.
- Choose the smallest correct audience: personal, Realm, or world-eligible.
  Missing optional addons must degrade safely.
- Test serialization, audience, deduplication, expiry, read/dismiss state,
  action rejection, successful resync, and provider absence.

## Add A New Addon Integration

- Declare required/optional Fabric dependencies truthfully and preserve the
  dependency direction `Core -> addon consumer`; Core never imports addon
  implementations.
- Prefer the owner's public API. Use a Core-neutral event/provider only when
  the concept is genuinely cross-domain. Put optional adapters in the
  consuming addon and define provider-absent behavior.
- Record owner, consumer, public contract, initialization order, threading,
  persistence owner, network boundary, failure behavior, and tests in
  `docs/architecture/DEPENDENCY_GRAPH.md` and `INDEX.md`.
- Verify the touched modules, optional-provider absence, full build when the
  contract crosses modules, and dedicated startup for initialization changes.

## Completion Checklist

- Ownership and dependency direction are explicit.
- Config descriptors, Admin discovery, UI, placeholders, profile projection,
  Chronicle/notification behavior, and public API docs are updated where
  applicable.
- Persistence changes have schema, migration, backup, rollback, and restart
  tests; otherwise state formats remain unchanged.
- `PLAN.md`, `TODO.md`, `INDEX.md`, and `docs/ai/CURRENT_STATUS.md` describe the
  completed slice and precise remaining work.

