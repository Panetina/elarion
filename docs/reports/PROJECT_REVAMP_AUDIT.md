# Project Revamp Audit

Audit date: 2026-07-05

Scope: Phase 0, Slice 1 - audit preparation and repository map verification.
This slice did not modify production Java, resources, config formats, packets,
or persistence.

Follow-up slices completed: Phase 0, Slice 2 Configuration/Admin Panel audit,
Phase 2 read-only config descriptors for Core, Groups, Economy, Worlds,
Portals, Offerings, Government, NPCs, Quests, Realms, Mounts, and Underworld,
plus Core-owned Optimization settings, and Phase 3, Slice 1 read-only Admin
Panel config browser. See
`docs/reports/CONFIG_ADMIN_AUDIT.md` for current Config/Admin status.

## Objective

Verify the current repository map against source and docs, identify reusable
architecture documentation, locate the implementation areas needed for the
project-wide revamp, and recommend the next bounded audit slice.

## Files Inspected

Root authority and planning:

- `RULES.md`
- `AGENTS.md`
- `CODEX.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `OPTIMIZATION_TRACKER.md`
- `settings.gradle`

Architecture and system docs:

- `docs/architecture/PROJECT_STRUCTURE.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/systems/GUI.md`
- `docs/systems/Government.md`
- `docs/systems/Chronicles.md`
- `docs/systems/NPCs.md`
- `docs/systems/Persistence.md`
- `docs/config.md`
- `docs/ai/CURRENT_STATUS.md`
- `docs/ai/AI_SEARCH_HINTS.md`
- `docs/reports/REPOSITORY_AUDIT_REPORT.md`
- `docs/reports/WORKTREE_CLEANUP_AUDIT.md`

Targeted source discovery:

- Core config, storage, UI, Admin Panel, Collection, notifications, history,
  public-history, registries, and API packages under `platform/core/src/main/java`.
- Addon config, storage, API, Admin Panel provider, notification, history,
  command, and UI integration points under `addons/**/src/main/java`.
- Unit and GameTest source locations under `platform/core/src/test/java`,
  `addons/**/src/test/java`, and `tests/gametest/src/main/java`.

Searches explicitly excluded `addons/angling/reference/**`, build outputs, and
Gradle/Loom cache directories where practical.

## Verified Repository Map

- `settings.gradle` currently includes `dev`, `platform:core`, 20 addon
  modules, and `tests:gametest`.
- Core remains the shared infrastructure module. Verified docs and source point
  to Core ownership of config defaults/validation patterns, citizens, Realms,
  identity, titles, rewards, history/public history, notifications, character
  lifecycle, permissions/abilities, task queues, networking examples, shared UI,
  Collection, and Admin Panel.
- Addons are still domain owners. Verified docs and source show separate addon
  services/config/storage for Economy, Offerings, Government, Groups, NPCs,
  Quests, Portals, Worlds, Realms, Security, Optimization, Angling, Underworld,
  Mounts, Names, Titles, and shell modules.
- The existing documentation map is usable. `INDEX.md`,
  `docs/architecture/PROJECT_STRUCTURE.md`, `docs/architecture/DEPENDENCY_GRAPH.md`,
  and `docs/ai/CURRENT_STATUS.md` already provide the right starting points for
  future slices.

## Verified Implementation Locations

### Configuration

- Core config entry point: `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`.
- Core default files: `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDefaultFiles.java`.
- Shared addon default helper: `platform/core/src/main/java/panetina/elarion/core/api/AddonConfigFiles.java`.
- Existing addon config loaders are module-owned. Examples found:
  `GovernmentConfigLoader`, `NpcConfigLoader`, `QuestConfigLoader`,
  `PortalConfigLoader`, `WorldsConfigManager`, `GroupConfigLoader`,
  `OfferingConfigLoader`, `UnderworldConfigLoader`, and Economy/Mount config
  records/helpers.
- Current strategy already matches the confirmed plan decision: keep physical
  formats initially and add typed discovery above existing loaders.
- Missing target capability: no Core-owned typed config descriptor registry was
  found for Admin Panel discovery.

### Admin Panel

- Core shell/service: `ElarionAdminPanelService`.
- Provider contract: `ElarionAdminPanelProvider`.
- Snapshot models: `ElarionAdminPanelSnapshot`, `ElarionAdminPanelTab`,
  `ElarionAdminPanelRow`, `ElarionAdminPanelAction`.
- Packets: `AdminPanelOpenPayload`, `AdminPanelOpenRequestPayload`,
  `AdminPanelActionPayload`.
- Client screen: `ElarionAdminPanelScreen`.
- Existing provider examples found in Government, Offerings, Mounts, and
  Underworld.
- Current capability is provider rows/actions. The planned config platform
  needs registered page/category/navigation/config-domain descriptors without
  breaking the current provider actions.

### Shared UI

- Core UI primitives and typography are documented in `docs/systems/GUI.md`.
- Source locations include `ElarionScreen`, `ElarionUiRenderer`,
  `ElarionUiTypography`, `ElarionScaledLayout`, `ElarionVirtualList`,
  `ElarionTextInput`, Collection, Admin Panel, and notification HUD classes.
- Government and notification docs identify the civic brown/gold style as the
  current canonical visual reference.
- Missing target capability: no dev-only UI gallery/test screen was found.

### Government UI

- Citizen-facing screen: `CivicForumScreen`.
- Authority-facing screen: `SeatOfRuleScreen`.
- Shared Government client helpers: `GovernmentScreenChrome`,
  `GovernmentUiComponents`, `GovernmentUiGlyphs`, `GovernmentUiIcons`.
- Authoritative packet contract: `GovernmentUiOpenPayload` and
  `GovernmentUiActionPayload`.
- Government docs already record important invariants: row kind/action metadata
  is the contract, clients must not infer behavior from labels, and mutating UI
  requests use server-issued sessions.

### Notifications

- Core service: `ElarionNotificationService`.
- Storage: `NotificationStorage`.
- Client HUD/drawer: `ElarionNotificationHud`.
- Snapshot/action packets: `NotificationSnapshotPayload`,
  `NotificationActionPayload`, compatibility claim/dismiss payloads.
- Addon integrations publish through `api.notifications()` and register actions
  in owning addons.
- Current docs already forbid addon-specific inboxes and HUD rails.

### History And Chronicles

- Core service: `HistoryService`.
- Public API: `ElarionPublicHistoryApi`.
- Storage/index/archive classes: `HistoryStorage`, `HistoryIndexStorage`,
  `ChronicleArchiveStorage`.
- Docs confirm raw JSONL is backing storage and player-facing views should use
  bounded public-history APIs, indexes, or archives.
- Missing target capability: no audited Chronicle renderer registry with
  persisted selected variant IDs was found in this slice.

### Placeholders

- Existing substitutions are scattered. Examples found include
  `ServerIdentityConfig.replace(...)`, reward action string replacement,
  portal Realm text placeholders, mount Collection `{realm}` text, and authored
  config placeholder notes.
- Missing target capability: no Core-owned placeholder registry with namespace,
  owner, type, context, formatter, visibility, and documentation was found.

### Citizen Profile And Unlocks

- Core citizen/title state lives in `CitizenRecord`, `CitizenStorage`, and
  `TitleService`.
- Collection already provides a modular unlockable surface through
  `ElarionCollectionService` and tab providers.
- Mounts owns mount unlock state and contributes to Collection.
- Slice 17E added the first dedicated Core profile boundary:
  `CitizenProfileService`, `CitizenProfileContributor`,
  `CitizenProfileRequestContext`, `CitizenProfileSnapshot`, section/field/card
  records, and `ProfileVisibility`. It exposes Core-owned identity, Realm, and
  active-title projections with server-side visibility filtering and bounded
  section/field/card caps.
- Remaining target capability: no addon profile contributors, profile network
  packets, Character Menu Profile UI, or owner-maintained summaries for quests,
  Offerings, NPC reputation, deaths, economy, groups, or other addon data.

### NPC Dialogue

- NPC definitions/config/session/UI are owned by `addons/npcs`.
- Important classes include `NpcConfigLoader`, `NpcConfigValidator`,
  `NpcDefinitionService`, `NpcInteractionService`, `NpcPlacementService`,
  `NpcDialogueScreen`, and NPC dialogue packets.
- NPC actions/conditions are registry-driven through Core/NPC APIs.
- Missing target capability: deeper graph validation and relationship/profile
  contribution design still need a dedicated audit slice.

### Persistence And Performance

- Core has reusable atomic JSON state helpers in `JsonStateStorage`.
- Many Core and addon stores use owner-specific storage classes and tests.
- History/index writes are queued/batched.
- Existing optimization tracker already flags large/future-facing risks:
  history-style player-facing views need bounded read models, UI should reuse
  shared primitives, and `PortalRouteService` is large enough to split before
  adding more route behavior.
- Any persistence migration remains a separately approved implementation slice.

### Tests

Verified test locations include:

- Core config, storage, network, UI layout, typography, Admin Panel, Collection,
  notifications, events, registries, character lifecycle, and history tests.
- Addon tests for Economy, Government, Groups, Mounts, NPCs, Offerings,
  Portals, Quests, Realms, Underworld, Worlds, Optimization, and Angling.
- Fabric GameTest support under `tests/gametest`.

No broad build or expensive GameTest run was performed in this audit slice.

## Findings By Risk

### High

- There is no typed config descriptor registry yet. Config is validated by
  module-owned loaders, but Admin Panel cannot discover config domains,
  categories, entries, defaults, bounds, reload/restart markers, or permission
  labels through a stable Core contract.
- There is no centralized placeholder registry yet. Existing placeholder-like
  substitutions are source- and config-local, which will become hard to audit
  for visibility, performance, and compatibility.
- There is no citizen profile aggregation API yet. Collection covers
  unlockables, but profile visibility and addon-owned profile sections are not
  modeled as a server-authoritative snapshot.

### Medium

- Admin Panel provider rows/actions are useful, but the current contract is
  too row/action-shaped for long-term config discovery and nested management
  pages.
- Chronicle storage and public-history APIs exist, but the natural-language
  renderer/variant strategy from the master plan is not yet represented as a
  reusable contract.
- Government has the strongest current UI language and semantic row contract;
  other screens still need future migration to shared components rather than
  screen-local layout code.

### Low

- Root architecture docs already match most master-plan principles. The main
  documentation risk is stale active-focus files pointing to older feature
  verification work instead of the project-wide revamp sequence.
- Shell addons remain in the build and are documented as shell/foundation
  modules. No action is needed in this first audit slice beyond preserving that
  label.

## Recommended Dependency-Aware Roadmap

1. Phase 0 Slice 2: Configuration and Admin Panel deep audit. Completed in
   `docs/reports/CONFIG_ADMIN_AUDIT.md`.
2. Phase 0 Slice 3: Shared UI and Government UI component audit.
3. Phase 0 Slice 4: History, Chronicle, notification, and placeholder audit.
4. Phase 0 Slice 5: Citizen/profile/unlock and Collection audit.
5. Phase 0 Slice 6: NPC, Quest, and narrative integration audit.
6. Phase 0 Slice 7: Addon dependency and public API contract audit.
7. Phase 0 Slice 8: Persistence and performance audit.
8. Phase 0 Slice 9: Test/build health audit.
9. Phase 0 Slice 10: Documentation accuracy and maintenance matrix audit.

After Phase 0, implement in this dependency order:

1. Phase 1 architecture contracts for any verified high-risk coupling.
2. Phase 2 typed config descriptor registry is started; Core, Groups,
   Economy, Worlds, Portals, Offerings, Government, NPCs, Quests, and Realms
   are registered, along with Mounts Collection text, Underworld settings, and
   Core-owned Optimization settings. Addon runtime config coverage is complete;
   Core scalar settings, Core Realm definitions, Core title/progression
   definitions, and Core reward definitions are covered. Generated-only Core
   abilities, Jail, and Security YAML require typed runtime models before
   truthful descriptors can be registered.
3. Phase 3 read-only Admin Panel config browser is started through the current
   row/detail model.
4. Phase 4 shared UI tokens/primitives and dev-only gallery.
5. Later phases for semantic UI components, Government archive/notifications,
   player-facing UI migration, Chronicle variants, NPC readiness, placeholders,
   profiles, persistence/performance fixes, duplicate removal, and final
   verification.

## Superseded Next Slice

This recommendation has been completed. Current Config/Admin status and the
next recommended slice now live in `docs/reports/CONFIG_ADMIN_AUDIT.md`,
`PLAN.md`, and `docs/ai/CURRENT_STATUS.md`.

Original recommendation:

Phase 0, Slice 2 - Configuration and Admin Panel deep audit.

Classification: MEDIUM.

Objective:

- Map every current config loader/file/domain and every current Admin Panel
  provider/action.
- Define the exact read-only descriptor fields needed for Phase 2 and Phase 3
  without changing code.

Expected inspection scope:

- `platform/core/src/main/java/panetina/elarion/core/config`
- `platform/core/src/main/java/panetina/elarion/core/api/AddonConfigFiles.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelProvider.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanel*.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanel*.java`
- addon `config` packages and `*AdminPanelProvider` classes
- config-related tests and Admin Panel tests
- `docs/config.md`, `docs/systems/Persistence.md`, `docs/systems/GUI.md`

Allowed outputs:

- Update this report or create a focused config/Admin audit report.
- Update `PLAN.md`, `TODO.md`, `INDEX.md`, `OPTIMIZATION_TRACKER.md` only if
  new verified facts affect them.
- Update `docs/ai/CURRENT_STATUS.md` with inspected files, decisions, risks,
  and the next recommended slice.

Stop condition:

- Present config/Admin findings and a bounded Phase 2 implementation proposal.
- Wait for explicit approval before adding typed config registry code.
