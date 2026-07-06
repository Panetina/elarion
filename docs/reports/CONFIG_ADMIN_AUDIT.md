# Configuration And Admin Panel Audit

Date: 2026-07-05

Scope: Phase 0, Slice 2 of the project-wide revamp. This was an audit-only
slice. No production Java, resources, config schemas, packets, or persistence
formats were changed.

Implementation follow-up: Phase 2, Slice 1 added the Core read-only descriptor
registry and a `core` reference domain. Phase 2, Slice 2 added the first addon
domain, `groups`. Phase 3, Slice 1 added an OP-only Admin Panel read-only
browser using the existing row/detail model. Phase 2, Slice 3 added the
`economy` domain. Phase 2, Slice 4 added the `worlds` domain. Phase 2, Slice 5
added the `portals` domain. Phase 2, Slice 6 added the `offerings` domain.
Phase 2, Slice 7 added the `government` domain. Phase 2, Slice 8 added the
`npcs` domain. Phase 2, Slice 9 added the `quests` domain. Phase 2, Slice 10
added the `realms` domain. Phase 2, Slice 11 added the `mounts` domain.
Phase 2, Slice 12 added the `underworld` domain.
Phase 2, Slice 13 added Core-owned `optimization` settings.
Phase 2, Slice 14 expanded the `core` domain with scalar manager settings.
Phase 2, Slice 15 added Core Realm definition descriptors.
Phase 2, Slice 16 added Core title and title-progression descriptors.
Phase 2, Slice 17 added Core reward descriptors.
Config writes, reload orchestration, packet changes, and persistence changes
remain deferred.

## Objective

Map the current configuration and Admin Panel systems closely enough to define
the first safe Config/Admin implementation slice.

## Files Inspected

Core configuration and reload:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDefaultFiles.java`
- `platform/core/src/main/java/panetina/elarion/core/api/AddonConfigFiles.java`
- `platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java`
- `platform/core/src/test/java/panetina/elarion/core/config/CoreConfigManagerTest.java`

Addon configuration representatives:

- `addons/economy/src/main/java/panetina/elarion/addons/economy/ElarionEconomyAddon.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyConfig.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyServicePriceConfig.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/command/EconomyCommands.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyPricingService.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/service/EconomyTransactionService.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/config/GroupConfigLoader.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/command/GroupCommands.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/service/GroupService.java`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/config/WorldsConfigManager.java`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/service/WorldService.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentDefinitionService.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/service/OfferingDefinitionService.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalDefinitionService.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/service/PortalRouteService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcDefinitionService.java`
- `addons/quests/src/main/java/panetina/elarion/addons/quests/service/QuestDefinitionService.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/config/UnderworldConfigLoader.java`
- `addons/realms/src/main/java/panetina/elarion/addons/realms/config/RealmProtectionConfig.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/config/MountCollectionTextConfig.java`

Admin Panel contracts, packets, screen, providers, and tests:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelProvider.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelSnapshot.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelTab.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelActionPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/AdminPanelPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/service/GovernmentAdminPanelProvider.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/service/OfferingAdminPanelProvider.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/service/UnderworldAdminPanelProvider.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/service/MountAdminPanelProvider.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/service/MountAdminPanelProviderTest.java`

Docs and test inventory:

- `docs/config.md`
- `docs/systems/GUI.md`
- `docs/addons/core.md`
- `docs/ai/CURRENT_STATUS.md`
- Config/Admin tests discovered under `platform/core/src/test/java` and
  `addons/*/src/test/java`.

## Verified Configuration Facts

- Editable definitions are already under `config/elarion/`, with addon files
  normally generated under `config/elarion/addons/<addon>/` by
  `AddonConfigFiles.writeDefault`.
- Core config uses `CoreConfigManager.load()`, generated defaults, migration,
  cross-file validation, and local variables before field assignment. Failed
  Core reloads preserve the previous valid Core snapshots.
- `/e reload` reloads Core config only. It catches `ConfigValidationException`
  and reports that the previous valid configuration remains active. On success,
  it resyncs identity and UI theme state to connected players.
- Core `ui_theme.yml` already includes `defaults.font-scale-percent`, accepts
  `100-150`, defaults to `100`, rejects invalid reload, and has automated test
  coverage for rollback.
- Addon config reloads are domain-specific commands, not part of one shared
  config registry or reload lifecycle.
- `AddonConfigFiles` writes default files only. It does not expose config
  domains, entries, typed values, descriptions, bounds, restart/reload markers,
  permissions, versions, or Admin Panel metadata.

## Current Config Domain Map

| Owner | Current editable files | Current load/reload shape | Audit note |
| --- | --- | --- | --- |
| Core | `config/elarion/core/*.yml` | `CoreConfigManager.load()`, `/e reload` | Best current validation and rollback model. |
| Economy | `economy.yml`, `service_prices.yml` | Loaded on addon init; `/e economy reload` reloads transaction config then pricing | Reload is not atomic across both files if pricing reload fails after transaction config was already applied. |
| Groups | `groups.yml` | Loaded on init; `/e groups reload` passes a newly loaded config into `GroupService.reload()` | Failed load preserves old service config because assignment happens after loading. |
| Worlds | `worlds.yml` | `WorldsConfigManager.load()`, `WorldService.reload()` | Reload snapshots old values and restores them on failure. Has config tests. |
| Government | `government.yml`, `forms/<form-id>/form.yml` | `GovernmentDefinitionService.load()` | Settings assignment happens before form assignment; a form-load failure can leave new settings with old forms. |
| Offerings | `society.yml`, `ui.yml`, project definitions | `OfferingDefinitionService.load()` | Definitions assignment happens before UI assignment; a UI-load failure can leave new definitions with old UI. |
| Portals | `routes.yml`, `ui.yml` | `PortalDefinitionService.load()`, `PortalRouteService.reload()` | Loaded values are validated before assignment. It also validates Economy price keys before publishing snapshots. |
| NPCs | `npcs.yml`, `skins.yml`, `portraits.yml`, `ui.yml`, `dialogues/**` | `NpcDefinitionService.load/reload()` | Loader returns a full config before assignment. Validator tests exist for dialogue/action/prompt errors. |
| Quests | `questlines/**` | `QuestDefinitionService.load()` | Loader result is swapped through an `AtomicReference`. Loader tests exist. |
| Underworld | `underworld.yml` | `UnderworldConfigLoader.load()` | Malformed config logs an error and silently falls back to defaults instead of rejecting reload. |
| Realms addon | `protection.yml` | `RealmProtectionConfig.load()` on init | Throws on IO/runtime failure; no typed descriptor metadata. |
| Mounts | `collection.yml` | `MountCollectionTextConfig.load()` on init | Malformed config logs an error and falls back to defaults. |
| Optimization/Security | `performance.yml`, `security.yml` | Default files generated by addon entrypoints | Shell/foundation-style configs without a shared descriptor contract. |

## Verified Admin Panel Facts

- Core owns `/e panel`, the Admin Panel service, packets, screen, and provider
  registry.
- `ElarionAdminPanelProvider` currently contributes system rows, realm rows,
  player actions, runtime reset support, and action dispatch.
- `ElarionAdminPanelService` keeps providers in a sorted
  `CopyOnWriteArrayList`, replaces providers by normalized ID, and gates panel
  open/actions behind OP level 4.
- Current snapshot tabs are fixed by Core: overview, players, systems, realms,
  and danger.
- Current rows are card-like records with title, subtitle, body, state, icon,
  kind, active/danger flags, and actions.
- Current actions support normal/input/danger styles, enabled state,
  confirmation metadata, and one optional string parameter.
- Packets are bounded: rows, tabs, actions, labels, bodies, parameters, and
  confirmation strings have explicit caps.
- Client UI renders server-authored rows/actions and sends provider/action/
  target IDs plus optional parameters. Server providers own mutations.
- Government, Offerings, Underworld, and Mounts currently use providers for
  operational rows and admin actions, not config-domain discovery.

## Gaps For Config/Admin First

High priority:

- A typed read-only config descriptor registry now exists for parsed runtime
  config domains. The Admin Panel Config tab displays registered domains as
  read-only summary rows. Config mutation, typed controls, and common reload
  orchestration are still missing.
- `/e reload` covers Core only; addon reloads are fragmented. A future common
  registry needs to describe reload behavior without forcing every addon into
  one physical file format.
- Addon reload safety is inconsistent. Core and Worlds preserve previous valid
  snapshots; Groups, NPCs, Quests, and Portals mostly assign after successful
  load; Economy, Government, and Offerings have partial-apply risks; Underworld
  and Mounts fall back to defaults on bad config.
- The current Admin Panel row/action model is not expressive enough for typed
  config editing. It can display read-only summaries, but it cannot represent
  typed controls, choices, numeric bounds, per-entry validation state,
  restart-required markers, reload safety, or multi-field edits.

Medium priority:

- Admin Panel realm rows flatten addon realm-row details into Core Realm rows
  by merging actions. This is useful for operational controls, but it is not a
  general page/category navigation model.
- Existing config test coverage is uneven. Core and Worlds have strong reload
  tests; NPCs, Quests, and Offerings have targeted validation tests; several
  addon config surfaces lack rollback/malformed-config tests.
- There is no dedicated Admin Panel system doc. `docs/systems/GUI.md` and
  `docs/addons/core.md` describe the current contract, but Config/Admin work
  will need a source-backed Admin Panel page once the provider model expands.

## Architecture Decision For Phase 2

Keep existing physical config formats initially. Add a Core-owned read-only
descriptor registry above current loaders.

The first implementation should not parse files when an admin opens the panel.
Descriptors should expose the current validated runtime snapshot held by the
owning service. That keeps Admin Panel discovery bounded and avoids adding
file IO to ordinary UI open paths.

## Completed Phase 2 Slice 1

Objective:

- Add Core-owned read-only config descriptor contracts and a registry, then
  register a small Core domain as the first reference implementation.

Approved boundaries:

- Include descriptor/registry types, server-side registration, current-value
  snapshots for a narrow Core subset such as UI theme defaults and server
  identity, and focused tests.
- Exclude Admin Panel rendering, config mutations, file format changes, addon
  domain migration, reload orchestration changes, persistence changes, and
  network packet changes.

Expected production files:

- `platform/core/src/main/java/panetina/elarion/core/config/...`
- Possibly a small Core API accessor under
  `platform/core/src/main/java/panetina/elarion/core/api/...` if the registry
  needs to be visible to addons in later slices.

Expected tests/docs:

- `platform/core/src/test/java/panetina/elarion/core/config/...`
- `docs/config.md`
- `docs/systems/GUI.md` only if the UI/Admin contract text changes
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `PLAN.md`, `TODO.md`, and `INDEX.md`

Architecture impact:

- Core owns the registry and descriptor contracts.
- Addons will later register owned domains through Core, but this first slice
  should not change addon loading behavior.
- No persistence, save data, config file, command, packet, or client/server
  authority behavior changes.

Compatibility risk:

- Low if kept read-only and server-side. It adds discovery metadata without
  changing existing config files, reload commands, or runtime state.

Verification:

- Focused Core unit tests for registry registration, stable ordering,
  duplicate ID rejection/replacement policy, descriptor current/default values,
  bounds metadata, and immutable snapshots.
- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Scope classification:

- MEDIUM. It adds a new Core contract and tests, but it avoids Admin Panel UI,
  networking, config writes, addon migration, and persistence.

## Recommended Phase 2 Slice 2

Status: completed.

Objective:

- Register one low-risk addon config domain through
  `ElarionApi.system().configs()`.

Recommended addon:

- Groups, because its editable surface is a single `groups.yml`, its runtime
  service already accepts a parsed `GroupConfig`, and the descriptor can stay
  read-only without changing reload behavior.

Approved boundaries:

- Add read-only Groups descriptors and focused tests.
- Do not add Admin Panel UI, config writes, reload orchestration, packet
  changes, or persistence changes.

Scope classification:

- SMALL.

Verification:

- Passed `.\gradlew.bat :addons:groups:test --tests panetina.elarion.addons.groups.config.GroupConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:groups:test`.
- Passed `.\gradlew.bat :platform:core:test`.

## Completed Phase 3 Slice 1

Objective:

- Add an OP-only read-only Admin Panel config browser skeleton using the
  existing Admin Panel row/detail model.

Approved boundaries:

- Show registered domains, categories, entries, current/default display values,
  owner module, source files, reload command, bounds, and reload/restart
  markers.
- Do not add config writes, typed edit controls, reload orchestration, packet
  schema changes, or persistence changes.

Scope classification:

- SMALL.

Implementation:

- `ElarionAdminPanelService` initially prepended config-domain rows to the
  Systems tab from `ElarionApi.system().configs()`. This was superseded by
  Phase 3 Slice 3; config rows now live in the dedicated Config tab.
- Each row is read-only and summarizes one registered domain. The detail body
  includes owner module, source files, reload command, categories, entries,
  current/default values, bounds, choices, and reload/restart markers.
- The existing Admin Panel packet and row/action model was reused. No config
  write actions were added.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`.
- Passed `.\gradlew.bat :platform:core:test :addons:groups:test`.

Deferred:

- Dedicated config pages/categories.
- Typed edit controls.
- Config mutation requests/results.
- Common reload orchestration.
- Packet schema changes.
- Persistence or file format changes.

## Completed Phase 2 Slice 3

Objective:

- Register Economy's current validated config/pricing snapshot as a read-only
  config descriptor domain.

Approved boundaries:

- Add read-only Economy descriptors and focused tests.
- Expose current transaction config and service-price metadata without parsing
  files during ordinary discovery.
- Do not fix Economy reload atomicity, add config writes, change `/e economy
  reload`, alter packets, or change persistence.

Scope classification:

- SMALL.

Implementation:

- Added `EconomyConfigDescriptors`.
- Registered the `economy` domain from `ElarionEconomyAddon` through
  `api.system().configs()`.
- Descriptors read current values from `EconomyTransactionService.config()` and
  `EconomyPricingService.definitions()`.
- The domain covers persistence, query, Governor, and service-price metadata.

Verification:

- Passed `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.config.EconomyConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:economy:test :platform:core:test`.

Deferred:

- Economy reload atomicity fixes.
- Config writes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Completed Phase 2 Slice 4

Objective:

- Register Worlds' current validated config snapshot as a read-only config
  descriptor domain.

Approved boundaries:

- Add read-only Worlds descriptors and focused tests.
- Use the existing Worlds config manager/service snapshot.
- Do not change world storage, managed-world behavior, reload semantics,
  packets, or persistence.

Scope classification:

- SMALL.

Implementation:

- Added `WorldsConfigDescriptors`.
- Registered the `worlds` domain from `ElarionWorldsAddon` through
  `api.system().configs()`.
- Descriptors read current values from `WorldsConfigManager`.
- The domain covers schema, lobby routing, current world keys/counts, and
  per-world identity/type/rule summaries.

Verification:

- Passed `.\gradlew.bat :addons:worlds:test --tests panetina.elarion.addons.worlds.config.WorldsConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:worlds:test :platform:core:test`.

Deferred:

- World storage or managed-world behavior changes.
- Worlds reload semantic changes.
- Config writes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Completed Phase 2 Slice 5

Objective:

- Register Portal route/UI config snapshots as a read-only config descriptor
  domain.

Approved boundaries:

- Add read-only Portal descriptors and focused tests.
- Expose route IDs, route modes, dimensions, pricing keys, schedule metadata,
  and UI settings where these are already available from Portal definition
  services.
- Do not change Portal travel behavior, schedule evaluation, Economy price
  integration, config writes, packets, file formats, or persistence.

Scope classification:

- MEDIUM.

Implementation:

- Added `PortalConfigDescriptors`.
- Registered the `portals` domain from `ElarionPortalsAddon` through
  `api.system().configs()`.
- Descriptors read current values from `PortalDefinitionService` route and UI
  snapshots.
- The domain covers route IDs, modes, source/destination dimensions, Economy
  price keys, schedule settings, visual settings, and prompt UI sizing.

Verification:

- Passed `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.config.PortalConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:portals:test :platform:core:test`.

Deferred:

- Portal travel behavior changes.
- Schedule evaluation changes.
- Economy price integration changes.
- Config writes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Recommended Phase 2 Slice 6

Objective:

- Register Offering/Shrine society, UI, and project definition snapshots as a
  read-only config descriptor domain.

Approved boundaries:

- Add read-only Offerings descriptors and focused tests.
- Expose society settings, UI settings, and project/milestone summary metadata
  where already available from Offering definition services.
- Do not change donation/progression behavior, rewards, Shrine blocks, reload
  semantics, packets, config writes, file formats, or persistence.

Scope classification:

- MEDIUM.

Status: completed.

Implementation:

- Added `OfferingConfigDescriptors`.
- Registered the `offerings` domain from `ElarionOfferingsAddon` through
  `api.system().configs()`.
- Descriptors read current values from `OfferingDefinitionService` project and
  UI snapshots.
- The domain covers reserved society metadata, Shrine UI settings, project IDs,
  scopes, repeatability flags, requirement/milestone/level counts, and
  presentation fields.

Verification:

- Passed `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.config.OfferingConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:offerings:test :platform:core:test`.

Deferred:

- Offering donation/progression behavior changes.
- Reward or Shrine block changes.
- Reload semantic changes.
- Config writes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Phase 2 Slice 7 - Government Read-Only Config Descriptor Domain

Objective:

- Register Government settings/forms snapshots as a read-only config descriptor
  domain.

Approved boundaries:

- Add read-only Government descriptors and focused tests.
- Expose Government settings and form/office/action summary metadata where
  already available from Government definition services.
- Do not change voting, office, authority, form-loading, reload semantics,
  packets, config writes, file formats, or persistence.

Scope classification:

- MEDIUM.

Status: completed.

Implementation:

- Added `GovernmentConfigDescriptors`.
- Registered the `government` domain from `ElarionGovernmentAddon` through
  `api.system().configs()`.
- Descriptors read current values from `GovernmentDefinitionService` settings
  and form snapshots.
- The domain covers authority cleanup timing, form IDs, display metadata,
  authority offices, office counts/holder limits, action groups, and
  transitions.

Verification:

- Passed `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.config.GovernmentConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:government:test :platform:core:test`.

Deferred:

- Government voting, office, and authority behavior changes.
- Form-loading or reload semantic changes.
- Config writes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Phase 2 Slice 8

Objective:

- Register NPC definition/dialogue snapshots as a read-only config descriptor
  domain.

Approved boundaries:

- Add read-only NPC descriptors and focused tests.
- Expose NPC definition, visual profile, and dialogue summary metadata where
  already available from NPC definition services.
- Do not change NPC placement/runtime state, dialogue session behavior,
  condition/action evaluation, reload semantics, packets, config writes, file
  formats, or persistence.

Scope classification:

- MEDIUM.

Status: completed.

Implementation:

- Added `NpcConfigDescriptors`.
- Registered the `npcs` domain from `ElarionNpcsAddon` after the first
  successful server definition load.
- Descriptors read current values from `NpcDefinitionService` suppliers.
- The domain covers NPC definitions, skin and portrait profiles, dialogue graph
  summaries, and dialogue UI settings.
- Dynamic definition rows are fixed to IDs present at registration while
  current values remain supplier-backed.

Verification:

- Passed `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:npcs:test :platform:core:test`.

Deferred:

- NPC placement/runtime state and dialogue session changes.
- Condition/action evaluation or reload semantic changes.
- Config writes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.
- A shared decimal descriptor codec; decimal NPC ranges remain read-only string
  descriptors.

## Phase 2 Slice 9

Objective:

- Register Quest package definition snapshots as a read-only config descriptor
  domain.

Approved boundaries:

- Add read-only Quest descriptors and focused tests.
- Expose validated Quest package and graph summary metadata already available
  from Quest definition services.
- Do not change Quest runtime state, progression, actions/conditions, scheduled
  consequences, notifications, reload semantics, packets, config writes, file
  formats, or persistence.

Scope classification:

- MEDIUM.

Status: completed.

Implementation:

- Added `QuestConfigDescriptors`.
- Registered the `quests` domain from `ElarionQuestsAddon` after the existing
  validated definition load.
- Descriptors read current values from the atomic `QuestDefinitionService`
  snapshot through suppliers.
- The domain covers package identity, scope, root stage, version/tags, actors,
  variables, stages/edges, evidence, endings/Shrine projections, reusable
  conditions/consequences, authoring keys, and metadata keys.
- Dynamic questline rows are fixed to IDs present at registration while current
  values remain supplier-backed.

Verification:

- Passed `.\gradlew.bat :addons:quests:test --tests panetina.elarion.addons.quests.config.QuestConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:quests:test :platform:core:test`.

Deferred:

- Quest runtime state, progression, actions/conditions, scheduled consequences,
  and notifications.
- Reload semantic changes or config writes/Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Phase 2 Slice 10

Objective:

- Register the loaded Realms protection config as a read-only config descriptor
  domain.

Approved boundaries:

- Add read-only Realms protection descriptors and focused tests.
- Expose only the existing `RealmProtectionConfig` snapshot.
- Do not change protection behavior, config loading, packets, file formats, or
  persistence.

Scope classification:

- SMALL.

Status: completed.

Implementation:

- Added `RealmConfigDescriptors`.
- Registered the `realms` domain from `ElarionRealmsAddon` using the same loaded
  `RealmProtectionConfig` snapshot passed to `RealmProtectionService`.
- Added `RealmProtectionConfig.defaults()` and reused it in config loading so
  descriptor defaults and loader fallbacks share one source.
- The domain covers shared world IDs, OP bypass, explosion block protection,
  feedback cooldown, and extra mechanism/container block IDs.
- All entries are non-runtime-reloadable and restart-required because Realms
  currently loads `protection.yml` only during addon initialization.

Verification:

- Passed `.\gradlew.bat :addons:realms:test --tests panetina.elarion.addons.realms.config.RealmConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:realms:test :platform:core:test`.

Deferred:

- Protection behavior and config loading semantic changes.
- Config writes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Phase 2 Slice 11

Objective:

- Register Mounts Collection text config as a read-only config descriptor
  domain.

Approved boundaries:

- Add read-only Mounts collection text descriptors and focused tests.
- Expose only loaded `MountCollectionTextConfig` entries.
- Do not change Collection rendering, mount runtime state, config loading,
  packets, file formats, or persistence.

Scope classification:

- SMALL.

Status: completed.

Implementation:

- Added `MountConfigDescriptors`.
- Registered the `mounts` domain from `ElarionMountsAddon` using the same
  loaded `MountCollectionTextConfig` snapshot consumed by Collection rows.
- The domain covers a bounded mount count/ID summary plus locked/unlocked row
  and detail text for every `ElarionMountType`.
- All entries are non-runtime-reloadable and restart-required because Mounts
  currently loads `collection.yml` only during addon initialization.

Verification:

- Passed `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.config.MountConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:mounts:test :platform:core:test`.

Deferred:

- Collection rendering and mount runtime state changes.
- Config loading semantic changes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.

## Phase 2 Slice 12

Objective:

- Register the loaded Underworld config as a read-only config descriptor
  domain.

Approved boundaries:

- Add bounded Underworld, corpse, PvP loot, combat-tag, and soul categories with
  focused tests.
- Expose only the existing `UnderworldConfig` snapshot.
- Represent decimal values as read-only strings until Core gains a decimal
  descriptor codec.
- Do not change death/corpse/soul behavior, config loading, packets, files, or
  persistence.

Scope classification:

- MEDIUM.

Status: completed.

Implementation:

- Added `UnderworldConfigDescriptors`.
- Registered the `underworld` domain from `ElarionUnderworldAddon` through
  `UnderworldService.config()`.
- The domain exposes 33 settings across Underworld, corpse, PvP loot,
  combat-tag, and Soul Fracture categories.
- Descriptor suppliers follow the service snapshot replaced by
  `/e death reload`; discovery does not parse YAML.
- Decimal settings use validated read-only string descriptors until Core gains
  a decimal codec.

Verification:

- Passed `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.config.UnderworldConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:underworld:test :platform:core:test`.

Deferred:

- Death, corpse, PvP loot, combat-tag, Soul Fracture, and True Death behavior.
- Config loading/fallback semantic changes or Admin Panel edit actions.
- Packet schema changes.
- Persistence or file format changes.
- A shared decimal descriptor codec.

## Phase 2 Slice 13

Objective:

- Register Optimization/performance task settings as a read-only config
  descriptor domain.

Approved boundaries:

- Expose the active Core task settings/snapshot backing
  `config/elarion/addons/optimization/performance.yml`.
- Add focused descriptor tests.
- Represent decimal values as read-only strings until Core gains a decimal
  descriptor codec.
- Do not change worker queues, budgets, monitoring, config loading, files, or
  runtime behavior.

Scope classification:

- MEDIUM.

Approval:

- Pre-approved by the user as part of all remaining config descriptor work.

Status: completed.

Implementation:

- Added `PerformanceConfigDescriptors` in the Optimization addon.
- Registered the `optimization` domain with `platform:core` ownership because
  Core loads and owns `ElarionTaskService` settings before addons initialize.
- Exposed 16 parsed host, task-budget, and monitoring settings through
  `ElarionTaskService.snapshot()`.
- Excluded live queue counters, fallback status, validation warnings, and
  compatibility notes because they are runtime diagnostics or unparsed
  metadata rather than config values.
- All entries are restart-required and non-runtime-reloadable.

Verification:

- Passed `.\gradlew.bat :addons:optimization:test --tests panetina.elarion.addons.optimization.PerformanceConfigDescriptorsTest`.
- Passed `.\gradlew.bat :addons:optimization:test :platform:core:test`.

Deferred:

- Worker, queue, budget, sampling, and monitoring behavior changes.
- Config loading, file format, and runtime reload changes.
- Config writes or Admin Panel edit actions.
- A shared decimal descriptor codec.

Descriptor coverage status:

- Every addon config file currently parsed into an addon runtime model has a
  registered read-only domain.
- The Core domain covers selected UI theme, server identity, scalar manager,
  Realm definition, title definition, title-progression definition, and reward
  definition values.
- Jail and Security generate placeholder YAML but do not load it into typed
  runtime snapshots. Registering current-value descriptors now would be
  misleading.

## Phase 2 Slice 14

Objective:

- Expand the existing Core domain with scalar runtime config snapshots.

Expected scope:

- Citizen default title and activity settings.
- Chat channels, radii, cooldown, formats, and notice settings.
- Identity/nickname policy and protection settings.
- History recording/query/archive/public-query settings.
- Core descriptor tests.
- No config writes, file-format changes, reload changes, commands, persistence,
  or gameplay behavior.

Scope classification:

- MEDIUM.

Approval: pre-approved by the user as part of all remaining config descriptor
work.

Status: completed.

Implementation:

- Expanded `CoreConfigDescriptors` with 41 scalar entries across citizens,
  chat, identity, and history categories.
- Added `citizens-defaults.yml`, `activity.yml`, `chat.yml`, `identity.yml`, and
  `history.yml` to the Core domain source-file list.
- Current values use existing `CoreConfigManager` getters and remain
  supplier-backed after successful `/e reload` operations.
- Shipped defaults, type metadata, bounds, and optional-list behavior are
  represented without adding parsers or runtime state.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Core reward definition map descriptors.
- Ability ownership/snapshot audit.
- Config writes, reload behavior changes, files, commands, persistence, and
  gameplay behavior.

## Phase 2 Slice 15

Objective:

- Add Core Realm definition descriptors to the existing `core` domain.

Approved boundaries:

- Add `realms.yml` count/ID and per-Realm presentation, visibility, spawn, and
  flag summaries.
- Read current values only from `CoreConfigManager.realms()`.
- Dynamic rows are fixed to IDs present at registration and supplier-backed
  afterward.
- Exclude runtime Realm membership/governance, load/reload behavior changes,
  files, commands, persistence, and gameplay behavior.

Scope classification:

- MEDIUM.

Approval:

- Pre-approved by the user as part of all remaining config descriptor work.

Status: completed.

Implementation:

- Added `realms.yml` to the `core` domain source-file list.
- Added a `realms` category with count/ID summary descriptors.
- Added per-Realm descriptors for display name, short name, prefix, color,
  visibility scope, spawn world/coordinates/rotation, and flags.
- Dynamic rows are fixed to Realm IDs present when `CoreConfigDescriptors`
  builds the domain. Current values for those rows read
  `CoreConfigManager.realms()` and update after successful `/e reload`
  operations.
- Decimal spawn coordinates/rotation are exposed as read-only strings because
  the shared descriptor registry still has no decimal codec.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Title and title-progression descriptors were completed by Slice 16.
- Reward definition descriptors were completed by Slice 17.
- Ability ownership/snapshot audit.
- Config writes, reload behavior changes, files, commands, persistence, and
  gameplay behavior.

## Phase 2 Slice 16

Objective:

- Add Core title and title-progression descriptors to the existing `core`
  domain.

Approved boundaries:

- Add `titles.yml` and `title-progression.yml` count/ID and summary
  descriptors using existing `CoreConfigManager` definition snapshots.
- Include title presentation, acquisition/ownership metadata, visibility,
  priority, ability/effect summaries, progression region summaries, and unlock
  rule summary fields where they are already exposed by Core config models.
- Exclude runtime title ownership/activation, ability ownership changes,
  config load/reload behavior changes, files, commands, persistence, and
  gameplay behavior.

Scope classification:

- MEDIUM.

Approval:

- Pre-approved by the user as part of all remaining config descriptor work.

Status: completed.

Implementation:

- Added `titles.yml` and `title-progression.yml` to the `core` domain
  source-file list.
- Added a `titles` category with count/ID summary descriptors and per-title
  presentation, priority, visibility, acquisition, ownership, ability, and
  active-effect summaries.
- Added a `title_progression` category with progression region count/ID and
  bounds descriptors plus unlock-rule count/ID, target-filter, threshold,
  metadata, and continuous-rule summaries.
- Dynamic title, region, and rule rows are fixed to IDs present when
  `CoreConfigDescriptors` builds the domain. Current values for those rows read
  existing `CoreConfigManager` snapshots and update after successful
  `/e reload` operations.
- Decimal progression-region coordinates are exposed as read-only strings
  because the shared descriptor registry still has no decimal codec.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Core reward definition descriptors were completed by Slice 17.
- Ability ownership/snapshot audit.
- Config writes, reload behavior changes, files, commands, persistence, and
  gameplay behavior.

## Phase 2 Slice 17

Objective:

- Add Core reward descriptors to the existing `core` domain.

Approved boundaries:

- Add `rewards.yml` count/ID and action summary descriptors using
  `CoreConfigManager.rewards()`.
- Exclude reward execution, claim state, config load/reload behavior changes,
  files, commands, persistence, packets, and gameplay behavior.

Scope classification:

- MEDIUM.

Approval:

- Pre-approved by the user as part of all remaining config descriptor work.

Status: completed.

Implementation:

- Added `rewards.yml` to the `core` domain source-file list.
- Added a `rewards` category with count/ID summary descriptors.
- Added per-reward descriptors for action count, ordered action types, and
  per-action type/parameter summaries.
- Dynamic reward rows are fixed to reward IDs and action indexes present when
  `CoreConfigDescriptors` builds the domain. Current values for those rows read
  `CoreConfigManager.rewards()` and update after successful `/e reload`
  operations.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Reward execution, claim state, config load/reload behavior changes, files,
  commands, persistence, packets, and gameplay behavior.
- Core abilities, Jail, and Security descriptors until typed runtime loaders
  exist.

## Phase 3 Slice 2

Objective:

- Phase 3, Slice 2: Admin Panel config browser contract proposal.

Approved boundaries:

- Decide whether to keep the current Systems-row display, add a dedicated
  config tab, or introduce page/category navigation providers before mutation
  support.
- Exclude config writes, typed editing, reload orchestration, packet schema
  changes, persistence, and gameplay behavior unless separately approved.

Scope classification:

- MEDIUM.

Approval:

- Approved by the user when requesting to move to the next recommended slice.

Status: completed as a documentation/proposal slice.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelSnapshot.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelTab.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Verified facts:

- The current Admin Panel packet shape is already a generic snapshot of tabs,
  rows, and actions. A read-only Config tab can be added without packet schema
  changes.
- The current config browser is injected into the Systems tab by
  `ElarionAdminPanelService.systemRows()` via `configRows(...)`.
- `ElarionAdminPanelScreen` currently lays out fixed-width top tabs. Adding a
  sixth tab requires dynamic tab width or a narrower fixed width, otherwise the
  tab row can overflow the 660px panel.
- Existing `configRows(...)` rows already include owner, files, reload command,
  category entries, current/default values, bounds, choices, and
  reload/restart markers. They are read-only and have no actions.

Decision:

- Next implementation should add a dedicated read-only `configs` Admin Panel
  tab using the existing `ElarionAdminPanelSnapshot`/`Tab`/`Row` packet model.
- Move config-domain rows out of the Systems tab into the new Config tab.
- Keep the current row/detail content structure for the first implementation
  slice.
- Adjust the tab layout to derive tab width from the number of tabs or use a
  bounded narrower tab width so all tabs fit.
- Do not add page/category provider contracts yet. That is useful for typed
  editing, but it is unnecessary for the next read-only improvement and would
  expand the slice.
- Do not add config writes, reload orchestration, audit logging, or mutation
  packets in the next implementation slice.

Recommended next implementation slice:

- Phase 3, Slice 3: Add a dedicated read-only Admin Panel Config tab.

Expected files:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `PLAN.md`
- `TODO.md`

Architecture impact:

- Ownership remains Core-owned because Admin Panel shell, config registry, and
  config discovery are Core infrastructure.
- Addon domains remain addon-owned descriptors registered into Core; the UI
  reads only server-authored snapshots.
- No persistence, config files, commands, packets, or server-authoritative
  mutation contracts change.

Compatibility risks:

- Client UI tab layout can overflow if not adjusted for six tabs.
- Moving config rows out of Systems changes admin navigation but keeps the same
  row/detail data and server snapshot contract.
- Existing Admin Panel tests should be updated to expect config rows on the
  new Config tab and no config rows in Systems.

Verification:

- Focused `ElarionAdminPanelServiceTest`.
- `.\gradlew.bat :platform:core:test`.
- Manual UI check later when running a client, because this slice should not
  start a dev client by default.

## Phase 3 Slice 3

Objective:

- Add a dedicated read-only Admin Panel Config tab.

Approved boundaries:

- Move config-domain rows from Systems into a dedicated Config tab.
- Adjust Admin Panel tab layout so six tabs fit.
- Update focused Admin Panel tests and docs.
- Exclude config writes, typed editing, reload orchestration, page/category
  provider contracts, packet schema changes, persistence, commands, and
  gameplay behavior.

Scope classification:

- MEDIUM.

Approval:

- Approved by the user when asking to continue from the recommended slice.

Status: completed.

Implementation:

- Added `configs` to `ElarionAdminPanelService` tab order and snapshot output.
- Moved `configRows(...)` out of `systemRows(...)`; Systems now shows
  provider-owned testing/repair rows only.
- Kept the existing snapshot/tab/row packet model and read-only config-domain
  row body format.
- Adjusted `ElarionAdminPanelScreen` tab hitboxes/rendering to compute tab
  width from the current tab count.
- Extended layout metrics/tests to assert six tabs fit inside the panel.
- Added focused service coverage for the six-tab order.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Manual in-client screenshot/UI verification.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior.

Next recommended slice requiring approval:

- Phase 3, Slice 4: Admin Panel typed config detail model proposal.
- Decide whether the next read-only improvement should introduce typed config
  detail rows/sections within the existing packet model or wait for a larger
  page/category provider contract.

## Phase 3 Slice 4

Objective:

- Decide the next safe Admin Panel config-detail step after the dedicated
  read-only Config tab.

Approved boundaries:

- Inspect current Admin Panel row/body/payload limits.
- Produce a proposal only.
- Exclude production code changes, config writes, typed editing, reload
  orchestration, packet schema changes, persistence, commands, and gameplay
  behavior.

Scope classification:

- MEDIUM.

Approval:

- Approved by the user when asking to continue from the recommended slice.

Status: completed as a documentation/proposal slice.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelTab.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/test/java/panetina/elarion/core/network/AdminPanelPayloadTest.java`

Verified facts:

- `AdminPanelOpenPayload` already allows up to 16 tabs and 512 rows per tab.
- Each row body is capped at 2048 characters on the wire. The current config
  row builder intentionally caps generated body text below that.
- The current Config tab emits one row per config domain. Large domains can
  truncate entry details because a whole domain is flattened into one body.
- The existing `ElarionAdminPanelRow.kind` string can distinguish future row
  presentation without changing the packet schema.
- The existing row model already has title, subtitle, body, state, icon,
  active/danger flags, and actions. It is adequate for a better read-only
  drilldown but not for final typed editing controls.

Decision:

- Do not introduce a new Admin Panel packet schema yet.
- Do not add page/category provider contracts yet.
- Next implementation should expand the Config tab into domain and category
  rows using the existing snapshot/tab/row packet model.
- Domain rows should summarize owner, source files, reload command, category
  count, entry count, reload/static/restart-required totals, and validation
  status.
- Category rows should use stable IDs such as
  `config:<domain>:category:<category>` and list entries for that category only,
  reducing truncation compared with whole-domain bodies.
- Entry details should still be read-only strings in this next slice, but rows
  should preserve type, current/default values, bounds, choices,
  reload/restart markers, and validation errors in a predictable format.
- A later packet/model slice can add true typed controls and mutation requests
  once validation, permissions, audit logging, reload safety, and rollback are
  designed.

Recommended next implementation slice:

- Phase 3, Slice 5: Config tab domain/category read-only rows.

Expected files:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `PLAN.md`
- `TODO.md`

Architecture impact:

- Keeps Admin Panel and config discovery Core-owned.
- Keeps addon config ownership distributed through registered descriptor
  domains.
- Keeps the client dumb: it displays server-authored read-only rows.
- No persistence, command, packet schema, reload, or mutation contract changes.

Compatibility risks:

- The Config tab will contain more rows, so filtering and list scrolling must
  remain usable.
- Large category bodies may still truncate if one category has many entries,
  but the risk is lower than whole-domain rows.
- Row IDs should remain stable so selected-row preservation continues to work.

Verification:

- Focused `ElarionAdminPanelServiceTest` for domain/category rows,
  stable IDs, summary counts, and no config rows in Systems.
- `.\gradlew.bat :platform:core:test`.
- Manual UI check later when running a client.

## Phase 3 Slice 5

Objective:

- Expand the Config tab from one row per domain to domain summary rows plus
  per-category rows.

Approved boundaries:

- Use the existing Admin Panel snapshot/tab/row packet model.
- Preserve read-only behavior.
- Exclude config writes, typed editing, reload orchestration, page/category
  provider contracts, packet schema changes, persistence, commands, and
  gameplay behavior.

Scope classification:

- MEDIUM.

Approval:

- Approved by the user when asking to continue from the recommended slice.

Status: completed.

Implementation:

- `ElarionAdminPanelService.configRows(...)` now emits:
  - one domain summary row per registered config domain
  - one category detail row per domain category
- Domain summary rows include owner, files, reload command, category count,
  total entry count, reloadable entry count, restart-required entry count,
  invalid entry count, and category summaries.
- Category rows use stable IDs in the form
  `config:<domain>:category:<category>` and include only the entries for that
  category.
- Entry details remain read-only strings using the existing row body field.
- No packet/model schema changed.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Manual in-client UI verification.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior.

Next recommended slice requiring approval:

- Phase 3, Slice 6: Admin Panel config mutation/readiness audit.
- Decide the prerequisites for moving from read-only discovery toward safe
  config editing: write contracts, validation results, permission checks, audit
  history, reload safety, rollback behavior, and packet/request shape.

Remaining descriptor boundary:

- Jail and Security currently generate YAML placeholders but do not parse them
  into runtime config models. Do not register misleading current-value domains;
  first add real typed loaders in separately approved implementation slices.

## Phase 3 Slice 6

Objective:

- Audit the current read-only config descriptor and Admin Panel action
  contracts to decide what must exist before safe config editing.

Approved boundaries:

- Documentation and readiness analysis only.
- Inspect descriptor records, Admin Panel action payloads, server-side action
  dispatch, and reload-risk evidence.
- Exclude production mutation support, config writes, reload orchestration,
  packet schema changes, persistence, commands, gameplay behavior, and UI edit
  controls.

Scope classification:

- MEDIUM.

Status: completed.

Inspected:

- `ElarionConfigEntry`
- `ElarionConfigRegistry`
- `ElarionConfigDomain`
- `AdminPanelActionPayload`
- `ElarionAdminPanelAction`
- `ElarionAdminPanelService`
- `ElarionCoreMod` Admin Panel action receiver
- Current Core and addon reload/load findings from the Config/Admin audit

Verified facts:

- `ElarionConfigEntry` already exposes current/default display values,
  validators, bounds, reload/restart markers, read permission, and write
  permission metadata.
- The config registry is still read-only discovery. It has no typed change
  request, typed result, file-write, apply, rollback, or reload orchestration
  contract.
- The Admin Panel action payload can carry generic string parameters, but the
  action model is provider/action oriented and has no config-entry type,
  expected value type, validation-result shape, old/new value reporting, or
  reload policy contract.
- Current Config tab rows have no edit actions. The client only displays
  server-authored read-only rows.
- Addon reload behavior is not uniform enough for global editing. Some loaders
  preserve previous valid snapshots; others partially apply, fall back to
  defaults, or reload multiple services without a shared rollback boundary.
- Server-side Admin Panel actions already execute on the server thread through
  `ElarionAdminPanelService`, but current action dispatch is not a config
  mutation service.

Decision:

- Do not enable Admin Panel config editing yet.
- Do not add edit buttons to config rows.
- Do not reuse generic Admin Panel provider actions as the long-term config
  mutation API.
- Add Core-owned config mutation contract records before any write support.
  Those records should define request/result/error semantics, permissions,
  validation reporting, reload policy, old/new display values, and audit hooks,
  while remaining no-op/readiness-only until a later slice adds a real apply
  service.

Recommended next implementation slice:

- Phase 3, Slice 7: Core config mutation contract records, no writes.

Expected files:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- Focused tests under `platform/core/src/test/java/panetina/elarion/core/config/`
- `docs/config.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `PLAN.md`
- `TODO.md`

Architecture impact:

- Keeps Core as owner of shared config mutation contracts.
- Keeps addons as owners of their config files and domain-specific apply
  behavior.
- Does not change persistence, networking, Admin Panel packets, reload
  behavior, or client/server authority.

Compatibility risks:

- Low if the slice adds records/tests only.
- The record names and fields become public Core contracts, so they should be
  intentionally boring and stable before later UI or write services depend on
  them.

Verification:

- Focused Core config tests for normalization, validation-error modeling,
  success/failure result construction, and immutable parameter maps.
- `.\gradlew.bat :platform:core:test`.

## Phase 3 Slice 7

Objective:

- Add Core-owned config mutation contract records without enabling config
  writes.

Approved boundaries:

- Add request/result/error records under Core config.
- Add focused Core tests for contract behavior.
- Update docs and handoff.
- Exclude file writes, apply services, reload orchestration, packet schema
  changes, Admin Panel edit actions, persistence, commands, and gameplay
  behavior.

Scope classification:

- SMALL.

Status: completed.

Implementation:

- Added `ElarionConfigChangeRequest`.
  - Normalizes domain/category/entry IDs through the existing config ID rules.
  - Preserves submitted raw values for future typed parsing.
  - Carries optional expected-current display value, actor UUID, and reason.
  - Exposes a stable `domain:category:entry` target key.
- Added `ElarionConfigChangeError`.
  - Defines stable error codes for unknown targets, permission denial, stale
    values, parse failures, validation failures, unsupported edits,
    reload/restart gating, apply failures, and internal errors.
  - Provides default messages while preserving explicit path/message details.
- Added `ElarionConfigChangeResult`.
  - Defines `VALIDATED`, `APPLIED`, and `REJECTED` states.
  - Carries old/new display values, reload/restart flags, audit event type,
    and immutable error lists.
  - Rejects invalid result combinations: successful results cannot carry
    errors, and rejected results must carry at least one error.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeContractTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Config write/apply services.
- Admin Panel edit controls.
- Config mutation packets.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon-specific editable-domain opt-in.
- Persistence or config file format changes.

Next recommended implementation slice:

- Phase 3, Slice 8: Core config mutation validation service, no writes.
- Add a Core service/helper that accepts a change request and registry, resolves
  the domain/category/entry, checks write permission metadata, parses the raw
  value through the entry codec, runs the entry validator, detects stale
  expected-current values, and returns `ElarionConfigChangeResult.validated` or
  `rejected`.
- Exclude file writes, config application, reload orchestration, packets, Admin
  Panel edit actions, persistence, commands, and gameplay behavior.

Expected files:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- Focused tests under `platform/core/src/test/java/panetina/elarion/core/config/`
- `docs/config.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `PLAN.md`
- `TODO.md`

Architecture impact:

- Keeps config mutation readiness Core-owned and server-side.
- Still does not make any config domain editable.
- Keeps addon ownership intact because future apply behavior remains
  domain-owned and explicitly opt-in.

Compatibility risks:

- Low if validation remains pure and read-only.
- The validator should not parse files or mutate runtime snapshots.

Verification:

- Focused Core config validation tests.
- `.\gradlew.bat :platform:core:test`.

## Phase 3 Slice 8

Objective:

- Add a pure Core config mutation validation service without enabling writes.

Approved boundaries:

- Resolve `ElarionConfigChangeRequest` objects against the existing descriptor
  registry.
- Check descriptor write-permission metadata.
- Detect stale expected-current display values.
- Parse submitted raw values with the entry codec.
- Run entry validators.
- Return `ElarionConfigChangeResult.validated` or `rejected`.
- Exclude file writes, config application, reload orchestration, packets, Admin
  Panel edit actions, persistence, commands, and gameplay behavior.

Scope classification:

- SMALL.

Status: completed.

Implementation:

- Added `ElarionConfigChangeValidator`.
- `validate(registry, request, actorPermission)` now:
  - rejects missing domains, categories, and entries with stable error codes
  - treats null actor permission as `PUBLIC`
  - requires the actor permission rank to satisfy the entry write permission
  - reads the current display value from the descriptor supplier
  - rejects stale requests when `expectedCurrentValue` is nonblank and no
    longer matches the current display value
  - parses the proposed raw value through the entry codec
  - runs the entry validator on the parsed value
  - returns a read-only `VALIDATED` result with old/new display values and
    reload/restart flags
- The validator catches parse, current-value, and validator failures into
  structured `ElarionConfigChangeError` results instead of mutating state or
  throwing through normal validation paths.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeValidatorTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- File writes.
- Config apply services.
- Reload orchestration and rollback.
- Admin Panel edit controls.
- Config mutation packets.
- Audit-history emission.
- Addon-specific editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Next recommended implementation slice:

- Phase 3, Slice 9: Admin Panel config validation preview action proposal.
- Audit whether the existing `AdminPanelActionPayload` can safely carry a
  read-only "validate proposed value" request for one selected config entry, or
  whether config validation preview needs a dedicated packet/model before any
  UI work.
- Exclude actual edit buttons, writes, apply services, reload orchestration,
  persistence, commands, and gameplay behavior.

Alternative safe next slice:

- Add a dedicated packet-contract proposal for future config validation preview
  and edit controls before touching the Admin Panel screen.

## Phase 3 Slice 9

Objective:

- Audit whether Admin Panel config validation preview should reuse the existing
  generic Admin Panel action payload or require a dedicated config packet/model
  before UI work.

Approved boundaries:

- Documentation/proposal only.
- Inspect Admin Panel action/open payloads, row/action models, screen action
  flow, service dispatch, config row IDs, and current tests.
- Exclude production behavior changes, Java edits, UI edit controls, packets,
  writes, apply services, reload orchestration, persistence, commands, and
  gameplay behavior.

Scope classification:

- MEDIUM.

Status: completed.

Inspected:

- `AdminPanelActionPayload`
- `AdminPanelOpenPayload`
- `ElarionAdminPanelAction`
- `ElarionAdminPanelRow`
- `ElarionAdminPanelScreen`
- `ElarionAdminPanelService`
- `ElarionAdminPanelServiceTest`
- `AdminPanelPayloadTest`

Verified facts:

- `AdminPanelActionPayload` carries selected tab, provider id, target row id,
  action id, up to 16 string parameters, and confirmation state.
- `ElarionAdminPanelAction` exposes one optional text input through
  `parameterKey`, `parameterLabel`, and `parameterPlaceholder`.
- The current Admin Panel screen can already open a single-input modal and send
  generic action parameters back to the server.
- Server-side actions are authoritative and run through
  `ElarionAdminPanelService.act(...)`, then the server reopens the panel with a
  message string.
- Current Config tab rows are domain rows and category rows only. There is no
  stable selected row representing one config entry.
- Config row bodies are read-only text. Category bodies flatten many entries
  into one body and cannot identify which entry a proposed value belongs to.
- The snapshot message is limited and suitable for a short validation preview,
  but not for rich typed editing state.

Decision:

- Do not add a dedicated packet/model for read-only validation preview yet.
- Do not add validation actions to category rows.
- Do not add actual edit/apply buttons.
- The existing generic Admin Panel action payload is acceptable for a narrow
  validation-preview step only if the Config tab first gains stable entry rows.
  Each entry row can target exactly one descriptor entry and expose one
  `Validate Value` action with one `value` input.
- The preview action should call `ElarionConfigChangeValidator` server-side and
  return a short message such as `Valid: 100 -> 125; reload required` or
  `Invalid: must be between 100 and 150`.
- The preview action must not write files, apply values, reload config, emit
  audit events, or present itself as a save/edit control.
- A dedicated config packet/model should be introduced later, before true
  editing, multi-field controls, typed widgets, diff display, audit preview,
  or apply/reload orchestration.

Recommended next implementation slice:

- Phase 3, Slice 10: Config tab entry rows and validation preview action.

Expected files:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `PLAN.md`
- `TODO.md`

Architecture impact:

- Keeps Config/Admin discovery and validation Core-owned.
- Keeps the client dumb: it submits a proposed string value and displays a
  server-authored result message.
- Keeps descriptors read-only and addon-owned apply behavior untouched.
- No persistence, command, reload, or gameplay authority changes.

Compatibility risks:

- More Config tab rows can make browsing longer. Keep row IDs stable and list
  filtering usable.
- A validation-only action may be mistaken for editing unless labels and docs
  are explicit.
- Generic action messages are short; complex validation output will need a
  later typed result model.

Verification:

- Focused Admin Panel service tests for entry row IDs, entry row actions, and
  validation preview success/failure messages.
- Existing Admin Panel payload test is enough unless action metadata changes.
- `.\gradlew.bat :platform:core:test`.

## Phase 3 Slice 10

Objective:

- Add stable Config tab entry rows and a preview-only validation action.

Approved boundaries:

- Use the existing Admin Panel snapshot/tab/row/action packet model.
- Add one row per descriptor entry under each category row.
- Add one `Validate Value` input action per entry row.
- Validate the proposed value server-side through
  `ElarionConfigChangeValidator`.
- Exclude file writes, actual edit/apply controls, reload orchestration,
  persistence, commands, gameplay behavior, and dedicated packet/model changes.

Scope classification:

- MEDIUM.

Status: completed.

Implementation:

- `ElarionAdminPanelService.configRows(...)` now emits:
  - one domain summary row per registered config domain
  - one category detail row per domain category
  - one stable entry row per descriptor entry
- Entry row IDs use the current internal form
  `config-entry|<domain>|<category>|<entry>` so config IDs may still contain
  colons without making action target parsing ambiguous.
- Entry rows show one setting's path, description, current/default values,
  type, bounds, choices, runtime marker, read/write permissions, and current
  validation state.
- Each entry row exposes a `Validate Value` input action with a `value`
  parameter. The action calls `ElarionConfigChangeValidator` with operator
  permission and returns a short valid/invalid message.
- The preview intentionally omits `expectedCurrentValue` so it can validate a
  proposed raw value without becoming a stale-write or save operation.
- The action reopens the Config tab and preserves the selected entry row.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Manual in-client UI verification.
- File writes.
- Config apply services.
- Reload orchestration and rollback.
- Dedicated config mutation packets/models.
- Typed controls, diff display, and audit preview.
- Audit-history emission.
- Addon-specific editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Next recommended slice:

- Phase 3, Slice 11: dedicated config editing packet/model proposal.
- Classification: MEDIUM.
- Audit and specify the packet/model shape needed before true config editing:
  typed controls, expected-current values, validation result payloads, diff
  display, audit preview, reload/restart policy, apply permissions, and
  server-authoritative failure states.

## Phase 3 Slice 11

Objective:

- Specify the dedicated packet/model contract required before true Admin Panel
  config editing.

Approved boundaries:

- Documentation/proposal only.
- Inspect the current Admin Panel row/action payloads and config change
  request/result/error contracts.
- Define the next safe implementation shape for typed edit metadata,
  validation result payloads, expected-current values, diff display, audit
  preview, reload/restart policy, apply permissions, and authoritative failure
  states.
- Exclude Java behavior changes, file writes, apply services, reload
  orchestration, persistence, commands, gameplay behavior, and UI edit/save
  controls.

Scope classification:

- MEDIUM.

Status: completed.

Inspected:

- `AdminPanelActionPayload`
- `AdminPanelOpenPayload`
- `ElarionAdminPanelAction`
- `ElarionAdminPanelRow`
- `ElarionConfigChangeRequest`
- `ElarionConfigChangeResult`
- `ElarionConfigChangeError`
- `ElarionConfigChangeValidator`
- `ElarionAdminPanelService`
- Current Config tab documentation and Slice 10 implementation notes

Verified facts:

- `AdminPanelActionPayload` is provider/action oriented. It can carry a target
  row ID, action ID, confirmation flag, and up to 16 string parameters, but it
  cannot represent typed config controls or structured validation state.
- `ElarionAdminPanelAction` supports one optional text input. It has no
  numeric, boolean, enum, multiline, bounded, choice-list, restart-required, or
  apply-policy control metadata.
- `AdminPanelOpenPayload` carries one snapshot message string capped at 512
  characters. That is enough for preview messages, but not enough for a rich
  validation result, diff display, or audit preview.
- `ElarionConfigChangeRequest` already has the core target and safety fields:
  domain ID, category ID, entry ID, proposed raw value, optional
  expected-current display value, actor UUID, and reason.
- `ElarionConfigChangeResult` already models validated/applied/rejected states
  with old/new display values, reload/restart flags, audit event type, and
  structured errors.
- `ElarionConfigChangeValidator` is pure and read-only. It can be reused by a
  future edit service, but it is not an apply service.

Decision:

- Do not evolve `AdminPanelActionPayload` into the long-term config editing
  protocol. Keep it for generic provider actions and the current preview-only
  `Validate Value` action.
- Add a dedicated config edit packet/model family before true editing. The
  packet family should be Core-owned because config discovery, permissions,
  validation, audit shape, and reload policy are shared infrastructure.
- The client must remain dumb: it may render server-authored typed controls,
  submit proposed raw values, submit the expected current display value it was
  shown, and display server-authored validation/apply results. It must not
  decide editability, permissions, reload safety, or apply success.
- The first implementation should add records/codecs/tests only, with no
  handler registration and no edit UI behavior. That keeps the protocol
  reviewable before any state mutation exists.

Recommended model shape:

- `ElarionConfigEditTarget`
  - `domainId`
  - `categoryId`
  - `entryId`
  - stable `targetKey`
- `ElarionConfigEditControl`
  - target
  - label
  - description
  - path
  - value type from `ElarionConfigCodec.ValueType`
  - current display value
  - default display value
  - choices
  - minimum and maximum display bounds
  - runtime reloadable flag
  - restart-required flag
  - read permission
  - write permission
  - editable flag
  - disabled reason
- `ElarionConfigEditRequestPayload`
  - target
  - expected current display value
  - proposed raw value
  - reason
  - validate-only/apply intent enum, or separate validate/apply payloads if the
    implementation reads cleaner
- `ElarionConfigEditResultPayload`
  - target
  - result status
  - old display value
  - new display value
  - reload required
  - restart required
  - can apply
  - audit preview text
  - structured errors with code/path/message
  - server-authored short message
- Future apply payload, if separated:
  - target
  - expected current display value
  - proposed raw value
  - reason
  - confirmation token or explicit confirmation flag

Recommended packet direction:

- Server to client:
  - config edit/open snapshot for one target entry, or an enriched Config tab
    snapshot once the UI is ready
  - config validation/apply result
- Client to server:
  - validate proposed value
  - later, apply proposed value

Server-side validation/apply sequence:

1. Re-resolve domain/category/entry from the Core registry.
2. Check OP/Admin permission and descriptor write permission.
3. Compare nonblank expected current display value to the current descriptor
   value.
4. Parse the proposed raw value through the descriptor codec.
5. Run the descriptor validator.
6. For validation-only requests, return structured validated/rejected result.
7. For future apply requests, require an editable domain/applier opt-in.
8. Apply through the owning domain only, never through a generic file writer.
9. Record audit history only after an apply succeeds.
10. Resync the affected config/Admin snapshots after apply or reload.

Apply opt-in requirement:

- A descriptor being visible is not enough to make it editable.
- Each editable domain must later register an explicit applier or edit provider
  that owns file writes, runtime apply behavior, rollback behavior, and reload
  policy for that domain.
- Domains with partial-apply risk, fallback-to-default behavior, generated-only
  YAML, or no typed runtime loader must remain read-only.

Failure states that must be represented:

- unknown domain/category/entry
- permission denied
- stale expected current value
- parse failure
- validation failure
- unsupported edit
- restart required before apply
- reload required before effect
- domain not editable
- apply failure
- reload/rollback failure
- internal failure

Compatibility and safety:

- Existing `AdminPanelActionPayload` remains backward compatible and continues
  to support provider actions.
- Existing Config entry rows and preview validation remain valid but are not a
  save path.
- No existing config file format changes are implied.
- No existing addon becomes editable until its owning config loader/apply path
  is audited and explicitly opted in.
- Packet strings must remain bounded. Proposed values may need a larger but
  still bounded limit than the current 256-character generic action parameter
  because some future config text fields may be longer.

Recommended next implementation slice:

- Phase 3, Slice 12: Core config edit packet/model records, no handlers.
- Classification: MEDIUM.
- Add Core model/network records and packet codecs for config edit targets,
  edit-control snapshots, edit requests, and edit results. Add focused codec
  round-trip and validation tests.
- Exclude server receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 12

Objective:

- Add Core config edit packet/model records and codecs without registering
  handlers or enabling editing.

Approved boundaries:

- Add Core model records for config edit targets and edit-control snapshots.
- Add Core network payload records/codecs for edit open snapshots, edit
  requests, and edit results.
- Add focused codec round-trip and target validation tests.
- Exclude server receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

Scope classification:

- MEDIUM.

Status: completed.

Implementation:

- Added `ElarionConfigEditTarget`.
  - Normalizes domain/category/entry IDs through the existing config ID rules.
  - Exposes the stable `domain:category:entry` target key.
- Added `ElarionConfigEditControl`.
  - Carries one server-authored typed control snapshot: target, label,
    description, path, value type, current/default display values, choices,
    bounds, reload/restart markers, read/write permissions, editable flag, and
    disabled reason.
  - Includes `fromEntry(...)` so later UI work can build controls from
    descriptor entries without copying formatting logic.
- Added packet records/codecs:
  - `ElarionConfigEditOpenPayload`
  - `ElarionConfigEditRequestPayload`
  - `ElarionConfigEditResultPayload`
- Added package-private `ElarionConfigEditPayloadCodecs` to centralize bounded
  target/control/error/list serialization.
- Request payloads carry target, expected-current display value, proposed raw
  value, reason, and intent (`VALIDATE` or `APPLY`).
- Result payloads carry target, `ElarionConfigChangeResult.Status`, old/new
  display values, reload/restart markers, `canApply`, audit preview,
  structured `ElarionConfigChangeError` values, and a short message.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Packet receiver registration.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Next recommended slice:

- Phase 3, Slice 13: config edit packet registration audit/proposal.
- Classification: MEDIUM.
- Decide where and how the new config edit payload IDs should be registered,
  how OP/admin permission checks should be enforced for validate/apply
  requests, what server service method should own dispatch, and how results
  should reopen or update the Config tab.
- Exclude UI controls, file writes, apply services, reload orchestration,
  persistence, commands, and gameplay behavior.

## Phase 3 Slice 13

Objective:

- Audit and specify registration/dispatch for the new config edit packet
  contracts before adding live handlers.

Approved boundaries:

- Documentation/proposal only.
- Inspect Core payload type registration, server receiver registration,
  client receiver registration, Admin Panel open/action dispatch, and the new
  config edit payload classes.
- Decide the safest implementation sequence for payload type registration,
  server-side permission checks, validation dispatch ownership, result
  delivery, and Config tab refresh behavior.
- Exclude Java behavior changes, packet receiver registration, UI controls,
  file writes, apply services, reload orchestration, persistence, commands,
  and gameplay behavior.

Scope classification:

- MEDIUM.

Status: completed.

Inspected:

- `ElarionCoreMod` payload type registration and server receivers
- `ElarionCoreClient` client receivers
- `ElarionAdminPanelService.open(...)`
- `ElarionAdminPanelService.act(...)`
- `ElarionConfigEditOpenPayload`
- `ElarionConfigEditRequestPayload`
- `ElarionConfigEditResultPayload`

Verified facts:

- Core registers all current play payload codecs in `ElarionCoreMod` through
  `PayloadTypeRegistry.playS2C()` and `PayloadTypeRegistry.playC2S()`.
- Core registers current server receivers in `ElarionCoreMod` with
  `ServerPlayNetworking.registerGlobalReceiver(...)`, then switches work back
  to the server thread through `context.server().execute(...)`.
- Core client screen update receivers live in `ElarionCoreClient` through
  `ClientPlayNetworking.registerGlobalReceiver(...)`.
- The Admin Panel is already server-authoritative: `/e panel` and action
  payloads are gated through server-owned services and OP level 4 checks.
- `ElarionAdminPanelService.open(...)` is the current owner for creating and
  sending Admin Panel snapshots.
- `ElarionAdminPanelService.act(...)` owns generic provider action dispatch
  and reopens the selected tab/row with a short message.
- The new config edit payload records have codecs and IDs, but no payload type
  registration or receivers.

Decision:

- Register the config edit payload types in `ElarionCoreMod` before any live
  handler is added:
  - `ElarionConfigEditOpenPayload`: S2C
  - `ElarionConfigEditResultPayload`: S2C
  - `ElarionConfigEditRequestPayload`: C2S
- Keep receiver registration out of the first registration slice. Registering
  codecs alone is low risk and makes the packet contract visible without
  enabling config edit behavior.
- Do not send `ElarionConfigEditOpenPayload` until a client edit surface
  exists. Current Config tab entry rows and preview validation remain the only
  live config UI.
- When a C2S edit request receiver is later added, it must be in Core server
  initialization and must delegate to a Core-owned Admin/config service method
  instead of parsing or mutating config in the receiver lambda.
- `ElarionAdminPanelService` is the correct near-term dispatch owner because
  it already owns OP-gated Admin Panel context, config rows, preview
  validation, and panel refresh behavior. If the method grows beyond
  validation/result orchestration, extract a dedicated Core config edit service
  before adding apply support.
- Every future config edit request must check `player.hasPermissionLevel(4)`
  before target resolution or validation detail is returned.
- Validation requests may call `ElarionConfigChangeValidator` and return
  `ElarionConfigEditResultPayload`.
- Apply requests must return an unsupported/domain-not-editable result until a
  separately approved applier registry exists.

Recommended dispatch shape:

- Add a future method on `ElarionAdminPanelService` or a Core config edit
  coordinator:
  - input: `ServerPlayerEntity admin`, `ElarionConfigEditRequestPayload`
  - output: send `ElarionConfigEditResultPayload` and optionally reopen the
    Config tab with a short message
- Dispatch sequence:
  1. Reject non-OP admins immediately.
  2. Resolve target against `api.configs()`.
  3. Convert payload into `ElarionConfigChangeRequest` with the admin UUID.
  4. Run `ElarionConfigChangeValidator`.
  5. Convert result into `ElarionConfigEditResultPayload`.
  6. For `VALIDATE`, send the result and keep runtime state unchanged.
  7. For `APPLY`, return unsupported until editable-domain appliers exist.
  8. Reopen or refresh the Config tab only with server-authored snapshots.

Result delivery:

- Validation result payload is the structured result channel.
- Admin Panel snapshot message may also show a short summary for continuity
  with the existing panel.
- Do not rely on the 512-character Admin Panel snapshot message as the only
  validation output once typed editing exists.

Compatibility and safety:

- Existing Admin Panel action payload behavior remains unchanged.
- Existing preview-only `Validate Value` action remains valid and can be kept
  until the dedicated edit surface replaces it.
- Registering payload types without receivers does not allow clients to mutate
  config.
- No config file format changes are implied.
- No addon config domain becomes editable without an explicit applier opt-in.

Recommended next implementation slice:

- Phase 3, Slice 14: register config edit payload types only.
- Classification: SMALL.
- Add `PayloadTypeRegistry.playS2C()` registrations for
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload`, plus
  `PayloadTypeRegistry.playC2S()` registration for
  `ElarionConfigEditRequestPayload`.
- Add/adjust a focused compile or packet test if useful.
- Exclude server/client receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 14

Objective:

- Register config edit payload types only.

Approved boundaries:

- Add S2C payload type registrations for `ElarionConfigEditOpenPayload` and
  `ElarionConfigEditResultPayload`.
- Add C2S payload type registration for `ElarionConfigEditRequestPayload`.
- Exclude server/client receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

Scope classification:

- SMALL.

Status: completed.

Implementation:

- Added imports for the three config edit payload records in `ElarionCoreMod`.
- Registered `ElarionConfigEditOpenPayload` through
  `PayloadTypeRegistry.playS2C()`.
- Registered `ElarionConfigEditResultPayload` through
  `PayloadTypeRegistry.playS2C()`.
- Registered `ElarionConfigEditRequestPayload` through
  `PayloadTypeRegistry.playC2S()`.
- No `ServerPlayNetworking.registerGlobalReceiver(...)` or
  `ClientPlayNetworking.registerGlobalReceiver(...)` calls were added for
  these payloads.

Verification:

- Passed `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Server receiver registration.
- Client receiver registration.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Next recommended slice:

- Phase 3, Slice 15: config edit receiver/dispatch proposal.
- Classification: MEDIUM.
- Specify the minimal server receiver and service dispatch shape for
  validation-only requests: OP gating, target resolution, conversion to
  `ElarionConfigChangeRequest`, result payload conversion, and Config tab
  refresh behavior.
- Exclude implementation, UI controls, file writes, apply services, reload
  orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 15

Objective:

- Specify the minimal receiver and service dispatch shape for validation-only
  config edit requests.

Approved boundaries:

- Documentation/proposal only.
- Inspect the current Admin Panel preview validation helper, config edit
  request/result payload records, and Core server receiver pattern.
- Define the next safe implementation slice for a validation-only receiver and
  service method.
- Exclude Java behavior changes, UI controls, file writes, apply services,
  reload orchestration, persistence, commands, and gameplay behavior.

Scope classification:

- MEDIUM.

Status: completed.

Inspected:

- `ElarionAdminPanelService.configValidationPreview(...)`
- `ElarionConfigChangeValidator`
- `ElarionConfigChangeRequest`
- `ElarionConfigChangeResult`
- `ElarionConfigChangeError`
- `ElarionConfigEditRequestPayload`
- `ElarionConfigEditResultPayload`
- `ElarionCoreMod` server receiver block

Verified facts:

- Existing preview validation already proves the safe validation path:
  descriptor target, proposed raw value, OP-level permission metadata,
  pure validation, and no writes.
- `ElarionConfigEditRequestPayload` carries the richer fields the generic
  preview action lacks: target, expected-current display value, proposed raw
  value, reason, and intent.
- `ElarionConfigEditResultPayload` can carry structured old/new values,
  reload/restart markers, errors, `canApply`, audit preview text, and a short
  message.
- Core server receivers already run through `context.server().execute(...)`.
- Config edit payload types are registered, but no receiver exists.

Decision:

- The next implementation may add a C2S receiver for
  `ElarionConfigEditRequestPayload`, but it must support validation only.
- The receiver should do no config parsing, validation, or result construction
  directly. It should delegate to `ElarionAdminPanelService` or a narrow Core
  config edit coordinator.
- `ElarionAdminPanelService` is the correct near-term owner because it already
  has `api`, OP-gated panel context, config row construction, and preview
  validation behavior.
- Add a method shaped like:

```text
ElarionConfigEditResultPayload validateConfigEdit(
    ServerPlayerEntity admin,
    ElarionConfigEditRequestPayload payload
)
```

- That method should send or return only structured results. It should not
  modify runtime state.
- `VALIDATE` requests should be converted into `ElarionConfigChangeRequest`
  with:
  - target IDs from `payload.target()`
  - proposed value from `payload.proposedRawValue()`
  - expected-current value from `payload.expectedCurrentDisplayValue()`
  - actor UUID from the server-side admin player
  - reason from the payload, falling back to `admin-panel-config-edit-preview`
- `APPLY` requests must return a rejected `UNSUPPORTED` result until a later
  editable-domain applier registry exists.
- Non-OP players must receive a rejected `PERMISSION_DENIED` result. The
  service should not reveal whether the target exists to a non-OP player.
- Unknown targets, parse failures, stale values, and validation failures should
  flow through the existing `ElarionConfigChangeValidator` errors.
- `canApply` should be false for every result until apply support exists.
- `auditPreview` may be a server-authored string such as
  `Would change <domain>/<category>/<entry> from <old> to <new>.`
  Validation preview must not emit audit/history events.

Recommended result conversion:

- Validated result:
  - status `VALIDATED`
  - old display value from validator
  - new display value from validator
  - reload/restart flags from validator
  - `canApply=false`
  - no errors
  - short message `Valid: <old> -> <new>. <runtime marker>.`
- Rejected result:
  - status `REJECTED`
  - empty old/new values unless available from a stale/validation context
  - `canApply=false`
  - structured errors from validator or receiver guard
  - short message from the first structured error
- Apply intent:
  - status `REJECTED`
  - one `UNSUPPORTED` error
  - message `Config apply is not enabled yet.`

Recommended receiver shape:

```text
ServerPlayNetworking.registerGlobalReceiver(
    ElarionConfigEditRequestPayload.ID,
    (payload, context) -> context.server().execute(() -> {
        ElarionConfigEditResultPayload result =
            adminPanel.validateConfigEdit(context.player(), payload);
        ServerPlayNetworking.send(context.player(), result);
        adminPanel.open(context.player(), "configs", "", result.message());
    })
);
```

Implementation note:

- The exact selected row ID refresh can be improved later by deriving the
  Config entry row ID from the target. For the first receiver slice, reopening
  the Config tab with a server-authored message is enough because there is no
  dedicated edit UI yet.

Compatibility and safety:

- Existing generic `Validate Value` action remains unchanged.
- Existing Admin Panel rows and action payload remain unchanged.
- No client-side UI is required to exercise the receiver in this slice.
- No config files or runtime snapshots are mutated.
- No addon config domain becomes editable.

Recommended next implementation slice:

- Phase 3, Slice 16: validation-only config edit receiver and dispatch.
- Classification: MEDIUM.
- Add the server receiver for `ElarionConfigEditRequestPayload`, a
  validation-only dispatch method on `ElarionAdminPanelService`, result payload
  conversion helpers, and focused service tests for valid, invalid,
  permission-denied, stale, and apply-unsupported cases.
- Exclude UI controls, file writes, apply services, reload orchestration,
  persistence, commands, and gameplay behavior.

## Phase 3 Slice 16

Status: completed on 2026-07-06.

Objective:

- Add validation-only server dispatch for dedicated config edit requests.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditTarget.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- `ElarionCoreMod` now registers a server receiver for
  `ElarionConfigEditRequestPayload`.
- The receiver executes on the server thread, delegates to
  `ElarionAdminPanelService`, sends `ElarionConfigEditResultPayload`, and
  refreshes the Admin Panel Config tab with the server-authored result message.
- `ElarionAdminPanelService.validateConfigEdit` requires the bound Core API and
  OP level 4 permission.
- Validation requests are converted into `ElarionConfigChangeRequest` and run
  through `ElarionConfigChangeValidator` with operator permission.
- `APPLY` requests return `UNSUPPORTED`.
- Non-OP requests return `PERMISSION_DENIED`.
- Result payloads always report `canApply=false` until an editable-domain
  applier registry exists.

Boundaries preserved:

- No client edit UI was added.
- No client result handler was added.
- No config files are written.
- No runtime config snapshots are mutated.
- No reload orchestration, audit/history events, persistence changes, commands,
  or gameplay behavior were added.
- Existing generic Admin Panel `Validate Value` row action remains unchanged.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- Passed after Slice 23 documentation:
  `.\gradlew.bat :platform:core:test`
- Passed:
  `.\gradlew.bat :platform:core:test`

Next recommended slice:

- Phase 3, Slice 17: config edit client result handling proposal.
- Classification: SMALL.
- Decide how the client should consume `ElarionConfigEditResultPayload` before
  adding real edit controls: no-op receiver, lightweight screen cache,
  Admin Panel message-only flow, or dedicated edit detail state.
- Exclude config writes, apply services, reload orchestration, descriptor
  changes, and addon editable-domain appliers.

## Phase 3 Slice 17

Status: completed on 2026-07-06.

Objective:

- Decide the minimal client-side handling model for
  `ElarionConfigEditResultPayload` before real config edit controls exist.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decision:

- Add a lightweight client receiver/state cache as the next implementation
  slice.
- Keep visible feedback in the existing Admin Panel refresh message for now.
- Do not add a dedicated config edit screen, typed controls, or result detail
  rendering until the edit open/detail UX is specified.

Rejected options:

- No-op receiver only: safe, but wastes the structured result and leaves no
  future handoff point for edit UI.
- Admin Panel message-only forever: too lossy for future multi-error,
  old/new, audit-preview, and can-apply display.
- Dedicated edit detail state now: premature before there are real edit
  controls or editable-domain appliers.

Compatibility and safety:

- No server behavior needs to change.
- No config writes or applies are enabled.
- Client state must be cleared on join/disconnect to avoid stale result reuse.
- The cache must not authorize anything; server results remain authoritative.

Recommended next implementation slice:

- Phase 3, Slice 18: config edit client result receiver/cache.
- Classification: SMALL.
- Add a Core client state holder for the last config edit result, register a
  client receiver for `ElarionConfigEditResultPayload`, clear it on
  join/disconnect, and add a focused unit test for cache update/clear behavior.
- Exclude UI controls, dedicated edit detail rendering, config writes, apply
  services, reload orchestration, descriptor changes, and addon appliers.

## Phase 3 Slice 18

Status: completed on 2026-07-06.

Objective:

- Add minimal client-side handling for `ElarionConfigEditResultPayload` without
  adding UI controls or edit rendering.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- Added `ElarionConfigEditClientState`, a passive client cache for the last
  config edit result payload.
- Registered a client receiver for `ElarionConfigEditResultPayload` in
  `ElarionCoreClient`.
- Cleared cached config edit result state on client join and disconnect.
- Added focused unit coverage for storing, clearing, and retaining rejected
  results.

Boundaries preserved:

- No Admin Panel edit controls were added.
- No dedicated result rendering was added.
- No server behavior changed.
- No config descriptors changed.
- No config files are written.
- No apply services, reload orchestration, persistence, commands, or gameplay
  behavior were added.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- Passed:
  `.\gradlew.bat :platform:core:test`

Next recommended slice:

- Phase 3, Slice 19: config edit open/detail UX proposal.
- Classification: SMALL.
- Decide how an admin opens a typed edit/detail view from a Config entry row
  and how the UI should present current value, proposed value, validation
  errors, old/new diff, reload/restart policy, and disabled Apply state.
- Exclude config writes, apply services, reload orchestration, descriptor
  changes, and addon editable-domain appliers.

## Phase 3 Slice 19

Status: completed on 2026-07-06.

Objective:

- Define the first config edit open/detail UX contract before adding edit UI.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`

Decision:

- Add config edit detail as a dedicated state opened from a Config entry row,
  not as another generic text-input modal.
- Use the existing generic Admin Panel action path only to request opening the
  detail for a selected entry. The request carries no proposed value and does
  not mutate state.
- The server remains authoritative for opening: it resolves the row target from
  the registry and sends `ElarionConfigEditOpenPayload`.
- `ElarionConfigEditOpenPayload` carries `ElarionConfigEditControl`, including
  target, label, description, path, value type, current/default display values,
  choices, bounds, reload/restart flags, read/write permissions, editable flag,
  and disabled reason.
- The client should store the open control in `ElarionConfigEditClientState`
  and then render a dedicated detail/modal state in a later slice.
- The first rendered detail state should show:
  - label, description, and config path
  - current value and default value
  - value type, choices, and numeric bounds where present
  - reload/restart policy
  - read/write permission labels
  - proposed value input
  - validation errors and old/new diff from the last result for the same target
  - `Validate` enabled for OP-opened controls
  - `Apply` visible but disabled while `control.editable=false` or
    `result.canApply=false`
  - disabled reason whenever Apply is unavailable
- The existing `Validate Value` row action may remain as a compatibility
  preview during rollout, but long-term typed validation should happen inside
  the edit detail state through `ElarionConfigEditRequestPayload`.

Rejected options:

- Continue using the generic one-field modal as the long-term edit UX. It
  cannot display typed metadata, structured errors, old/new diff, policy, or
  disabled Apply state cleanly.
- Add config writes before the open/detail state exists. That would create a
  write path without enough administrator feedback.
- Let the client build edit controls from visible row text. Row body text is a
  display summary; server-authored `ElarionConfigEditControl` is the contract.
- Add a C2S open request payload now. The generic Admin Panel action path is
  sufficient for selecting a server-known row target and keeps the packet
  surface smaller until there is a clear need for a dedicated open request.

Compatibility and safety:

- No config files should be written by the open/detail flow.
- Opening a detail must OP-gate on the server exactly like current Admin Panel
  actions.
- If a target is unknown or no longer exists, the server should refresh the
  Config tab with a failure message rather than leaving stale client state.
- Opening a detail should set `editable=false` and a clear disabled reason
  until the owning domain has an explicit applier.
- The client must not infer editability from descriptor visibility or row
  labels.

Recommended next implementation slice:

- Phase 3, Slice 20: config edit open payload receiver and client open-state
  cache.
- Classification: SMALL.
- Extend `ElarionConfigEditClientState` to store the current open control,
  register a client receiver for `ElarionConfigEditOpenPayload`, clear open
  state on join/disconnect, and add focused cache tests.
- Exclude server open actions, Admin Panel row actions, rendered edit UI,
  validation input controls, config writes, apply services, reload
  orchestration, descriptor changes, and addon appliers.

## Phase 3 Slice 20

Status: completed on 2026-07-06.

Objective:

- Add client-side handling for `ElarionConfigEditOpenPayload` without adding
  server open actions or rendered edit UI.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- `ElarionConfigEditClientState` now stores the current open
  `ElarionConfigEditControl`.
- Opening a new control clears the cached validation result to prevent future
  UI from showing stale result data for the wrong target.
- `ElarionCoreClient` now registers a client receiver for
  `ElarionConfigEditOpenPayload`.
- Existing join/disconnect clearing now clears both open control and last
  result.
- Focused tests cover storing/clearing open control and clearing stale results
  when a new control opens.

Boundaries preserved:

- No server open action was added.
- No Admin Panel row action was added.
- No rendered edit UI was added.
- No validation input controls were added.
- No config descriptors changed.
- No config files are written.
- No apply services, reload orchestration, persistence, commands, or gameplay
  behavior were added.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- Passed:
  `.\gradlew.bat :platform:core:test`

Next recommended slice:

- Phase 3, Slice 21: config edit server open action proposal.
- Classification: SMALL.
- Decide the narrow server-side action that opens `ElarionConfigEditOpenPayload`
  from a selected Config entry row, including OP gating, target parsing,
  unknown-target fallback, disabled edit reason, selected-row refresh behavior,
  and tests.
- Exclude rendered edit UI, validation input controls, config writes, apply
  services, reload orchestration, descriptor changes, and addon appliers.

## Phase 3 Slice 21

Status: completed on 2026-07-06.

Objective:

- Specify the server-side action that opens config edit detail state from a
  Config entry row.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decision:

- Add a Core-owned Admin Panel row action for config entry rows, tentatively
  labeled `Open Editor`.
- Reuse the existing `AdminPanelActionPayload` path because it is already
  server-authoritative, OP-gated, row-targeted, and does not require trusting a
  client-built descriptor target.
- The server action should parse only row IDs of the existing
  `config-entry|domain|category|entry` form.
- The server must resolve the domain, category, and entry through
  `ElarionConfigRegistry`.
- Unknown or stale targets should return an Admin Panel failure message and
  must not send `ElarionConfigEditOpenPayload`.
- Successful open should send `ElarionConfigEditOpenPayload` with
  `ElarionConfigEditControl.fromEntry`.
- Until editable-domain appliers exist, the control must be sent with
  `editable=false` and a clear disabled reason such as
  `Config editing is not enabled yet.`
- After the action, the existing Admin Panel refresh may keep the selected
  config entry row and show a short message such as
  `Opened config editor for Creation Fee.`

Rejected options:

- Add a new C2S open request payload now. The existing Admin Panel action path
  is enough for a server-known selected row and avoids another packet surface.
- Let the client submit domain/category/entry IDs directly from local state.
  The server row target is already available and keeps this path consistent
  with existing Admin Panel actions.
- Enable Apply or editability as part of opening. Opening is presentation only.

Recommended next implementation slice:

- Phase 3, Slice 22: config edit server open action and Config row action.
- Classification: SMALL.
- Add the `Open Editor` action to config entry rows, implement the server-side
  resolver/sender in `ElarionAdminPanelService`, and add focused service tests
  for successful control construction plus invalid/stale target rejection.
- Exclude rendered edit UI, validation input controls, config writes, apply
  services, reload orchestration, descriptor changes, and addon appliers.

## Phase 3 Slice 22

Status: completed on 2026-07-06.

Objective:

- Add the server-side config edit open action and expose it on Config entry
  rows.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigDomain.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigCategory.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- Config entry rows now expose `Open Editor` before the existing
  `Validate Value` preview action.
- Added `OPEN_CONFIG_EDITOR_ACTION` in `ElarionAdminPanelService`.
- Added a server-side resolver that parses
  `config-entry|domain|category|entry`, resolves the descriptor through
  `ElarionConfigRegistry`, and builds `ElarionConfigEditControl.fromEntry`.
- Successful open sends `ElarionConfigEditOpenPayload` to the admin client.
- Controls are sent with `editable=false` and
  `Config editing is not enabled yet.`
- Unknown or stale targets return an Admin Panel failure message and do not
  construct an open payload.

Boundaries preserved:

- No rendered edit UI was added.
- No validation input controls were added.
- No config descriptors changed.
- No config files are written.
- No apply services, reload orchestration, persistence, commands, or gameplay
  behavior were added.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`

## Phase 3 Slice 23

Status: completed on 2026-07-06.

Objective:

- Specify the first rendered config edit detail shell before adding UI code.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decision:

- The first rendered detail should be a modal/detail shell layered over the
  existing Admin Panel screen when `ElarionConfigEditClientState.openControl()`
  is present.
- The shell should be presentation-only in its first implementation:
  label, description, path, current value, default value, type, choices,
  bounds, reload/restart policy, read/write permissions, disabled reason,
  and Close.
- It should include visible `Validate` and `Apply` affordances only as disabled
  or non-interactive placeholders until a later input-controls slice defines
  proposed-value editing.
- It should not send `ElarionConfigEditRequestPayload` yet.
- It should close by clearing only the open-control state; the current Admin
  Panel snapshot and selected row should remain intact.
- It should use existing Core UI primitives and avoid introducing a new screen
  class unless the existing Admin Panel layout cannot safely host the modal.

Rejected options:

- Add text input and validation submit in the first render slice. That is a
  separate interaction slice and needs clear focus/keyboard behavior.
- Add Apply as an enabled button. The server still returns `canApply=false`
  and no applier registry exists.
- Replace the Admin Panel screen with a separate config editor screen. The
  current flow is row-contextual and should preserve selected row context.

Recommended next implementation slice:

- Phase 3, Slice 24: config edit read-only detail shell.
- Classification: MEDIUM.
- Render a read-only modal/detail shell in `ElarionAdminPanelScreen` from
  `ElarionConfigEditClientState.openControl()`, add Close behavior, show
  disabled Validate/Apply affordances, and add focused client-state/screen
  helper tests where practical.
- Exclude text input, validation submission, result rendering beyond disabled
  placeholders, config writes, apply services, reload orchestration, descriptor
  changes, and addon appliers.

## Phase 3 Slice 24

Status: completed on 2026-07-06.

Objective:

- Render a read-only config edit detail shell from the server-authored open
  control.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- `ElarionAdminPanelScreen` now renders a read-only config edit shell when
  `ElarionConfigEditClientState.openControl()` is present.
- The shell displays label, description, path, current value, default value,
  type, bounds, choices, runtime policy, read/write permissions, disabled
  reason, and Close.
- Validate and Apply are visible but disabled/non-interactive.
- Escape closes the config edit shell before closing the Admin Panel.
- Mouse clicks are consumed while the shell is open; Close buttons clear only
  open-control state.
- `ElarionConfigEditClientState` now supports `closeOpenControl()`.
- Focused tests cover close-only behavior preserving the last result.

Boundaries preserved:

- No text input was added.
- No validation submission from the detail shell was added.
- No result rendering beyond disabled placeholders was added.
- No config descriptors changed.
- No config files are written.
- No apply services, reload orchestration, persistence, commands, or gameplay
  behavior were added.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- Passed:
  `.\gradlew.bat :platform:core:test`

Next recommended slice:

- Phase 3, Slice 25: config edit validation input proposal.
- Classification: SMALL.
- Decide how the read-only detail shell should become a validation-capable
  detail: proposed-value input state, focus/keyboard behavior, Validate button
  enablement, C2S validation request construction, stale-result clearing, and
  result display boundaries.
- Exclude Apply, config writes, reload orchestration, descriptor changes, and
  addon appliers.

## Phase 3 Slice 25

Status: completed on 2026-07-06.

Objective:

- Specify how the read-only config edit detail shell should become
  validation-capable without enabling Apply or writes.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextInput.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`

Decision:

- Add proposed-value input state to the config edit shell, seeded from
  `ElarionConfigEditControl.currentDisplayValue()` when a control opens.
- Keep input state client-local and discard it when the open control closes or
  when a different target opens.
- Use `ElarionTextInput` with a bounded single-line value. Start with the
  existing packet value limit and keep the visual field clipped/ellipsized.
- The input should receive focus when the edit shell opens.
- Character typing and Backspace should edit the proposed value while the shell
  is open.
- Enter should send a validation request when the proposed value is not blank;
  Escape should close the shell.
- Clicking Validate should send
  `ElarionConfigEditRequestPayload(target, currentDisplayValue, proposedValue,
  "admin-panel-config-edit-preview", VALIDATE)`.
- Clicking Apply must remain disabled/non-interactive regardless of validation
  outcome.
- Any local input change after a result is received should clear stale displayed
  result state or mark it stale. Do not show old validation results as current
  for a changed proposed value.
- Display validation results only when the result target matches the open
  control target.
- Result display should show status, message, old value, new value,
  reload/restart policy, audit preview, and structured errors.

Rejected options:

- Reuse the existing generic `Validate Value` modal inside the edit shell. It
  would duplicate context and does not fit the richer result display.
- Enable Apply after a successful validation. Server results still return
  `canApply=false`, and no editable-domain appliers exist.
- Store proposed-value input in global client state. It is screen-interaction
  state and should not outlive the open control.
- Trust client-side parsing or validation for enablement. The server remains
  authoritative; client-side checks are limited to blank/unchanged convenience
  states.

Recommended next implementation slice:

- Phase 3, Slice 26: config edit validation input and request.
- Classification: MEDIUM.
- Add proposed-value input state to `ElarionAdminPanelScreen`, focus and
  keyboard handling while the shell is open, Validate button enablement,
  `ElarionConfigEditRequestPayload` send with `VALIDATE`, and target-matched
  result rendering from `ElarionConfigEditClientState.lastResult()`.
- Exclude Apply, config writes, reload orchestration, descriptor changes, and
  addon appliers.

## Phase 3 Slice 26

Status: completed on 2026-07-06.

Objective:

- Make the config edit detail shell validation-capable without enabling Apply
  or writes.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextInput.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- `ElarionAdminPanelScreen` now keeps local proposed-value input state for the
  open config edit control.
- Input seeds from `ElarionConfigEditControl.currentDisplayValue()` when a
  target opens.
- Typing and Backspace edit the proposed value while the shell is open.
- Input changes clear stale validation result state.
- Enter and the Validate button send `ElarionConfigEditRequestPayload` with
  intent `VALIDATE`.
- Validation requests include the target, expected current display value from
  the server-authored control, proposed raw value, and
  `admin-panel-config-edit-preview` reason.
- Result display reads `ElarionConfigEditClientState.lastResult()` only when
  the result target matches the open control target.
- Apply remains visible but disabled/non-interactive.

Boundaries preserved:

- No `APPLY` requests are sent.
- No config descriptors changed.
- No config files are written.
- No apply services, reload orchestration, persistence, commands, or gameplay
  behavior were added.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- Passed:
  `.\gradlew.bat :platform:core:test`

Next recommended slice:

- Phase 3, Slice 27: config edit apply/readiness proposal.
- Classification: SMALL.
- Audit what must exist before `Apply` can ever become enabled: editable-domain
  applier contracts, reload safety, rollback, audit/history entries,
  permission checks, stale expected-current handling, config file persistence,
  and tests.
- Exclude implementing Apply, config writes, reload orchestration, descriptor
  changes, and addon appliers.

## Phase 3 Slice 27

Status: completed on 2026-07-06.

Objective:

- Audit what must exist before the config edit shell can ever enable `Apply`.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/event/ElarionDomainEvent.java`
- `platform/core/src/main/java/panetina/elarion/core/event/ElarionEventBus.java`
- `platform/core/src/main/java/panetina/elarion/core/service/HistoryService.java`

Verified facts:

- `ElarionConfigChangeValidator` is validation-only. It resolves the target
  descriptor, checks write permission metadata, rejects stale expected-current
  values, parses the proposed value, runs the descriptor validator, and returns
  `VALIDATED` or `REJECTED`.
- `ElarionConfigChangeResult` already has an `APPLIED` status and audit event
  type field, but no production service currently returns an applied result.
- `CoreConfigManager.load()` uses a strong reload pattern for Core: it writes
  defaults/migrations, validates, loads all snapshots into local variables, and
  assigns runtime fields only after successful parsing/validation.
- `/e reload` rejects `ConfigValidationException` and tells admins the
  previous valid configuration remains active, then resynchronizes identity and
  UI theme snapshots on success.
- The Admin Panel already emits administration domain events/history entries
  for existing mutating admin actions, but there is no config-specific apply
  event contract yet.
- No generic file writer, descriptor-owned writer, or addon applier exists.
  Descriptor visibility and descriptor validation do not imply safe editability.

Decision:

- Do not enable `Apply` yet.
- Add a Core-owned apply contract before any entry can become editable. The
  contract should be separate from descriptors and validation so read-only
  discovery stays truthful and low risk.
- Future names may be adjusted to local conventions, but the next contract
  should provide equivalents of:
  - `ElarionConfigApplyRegistry`
  - `ElarionConfigApplier`
  - `ElarionConfigApplyContext`
  - `ElarionConfigApplyResult` or an apply-plan/result pair
- Each editable domain must explicitly register an applier for a domain,
  category, entry, or declared entry group. Entries without an applier remain
  validate-only even when their descriptor has operator write permission.
- `Apply` must re-run server-side validation immediately before writing, using
  the same expected-current stale check as preview validation.
- `Apply` must remain server-authoritative. The client can send a target,
  expected current display value, proposed value, and reason, but the server
  resolves descriptor metadata, permissions, file ownership, and runtime reload
  behavior from trusted server state.

Apply readiness requirements:

- Permission: the request must require Admin Panel operator access and the
  descriptor's write permission. Future finer-grained permissions must be
  enforced server-side before validation and before write.
- Stale/concurrency safety: apply must reject stale expected-current values and
  should serialize mutation per target domain or file group so concurrent Admin
  Panel edits and `/e reload` do not race.
- Reload policy: restart-required entries must not be apply-enabled until their
  owner defines a safe non-runtime behavior. Runtime-reloadable entries can be
  apply-enabled only when the owning domain can update files and runtime
  snapshots safely.
- Persistence ownership: the applier, not the descriptor registry, owns file
  mutation. It must declare affected files and whether a request is a single
  scalar edit, structured edit, or unsupported generated/default-only content.
- Rollback: an applier must define backup/temp-write behavior and failure
  recovery. If write or reload fails after a file change, it must restore the
  previous file state or preserve a recoverable backup and keep the previous
  runtime snapshot active.
- Audit/history: successful apply must emit a Core domain event and history
  record with actor id, domain, category, entry, old/new display values, file
  or logical path, reason, reload/restart policy, result, and source
  `admin-panel-config-apply`. Validation-only preview must not emit audit
  history.
- Sensitive values: before secret-like config entries are editable, descriptors
  need a sensitivity/redaction flag. Do not audit or broadcast raw secret
  values.
- Synchronization: successful runtime apply must refresh affected Admin Panel
  snapshots and any affected client sync payloads. For Core UI theme edits,
  this means `UiThemeSyncPayload`-style resync; other domains need their own
  domain-safe resync.

Rejected options:

- Use descriptors as writers. Descriptors describe and validate config values;
  they do not know enough about file mutation, comments/default preservation,
  reload orchestration, or rollback.
- Enable `Apply` only because validation succeeded. Validation proves a value
  is syntactically and semantically acceptable, not that it can be persisted and
  activated safely.
- Use one generic YAML writer for all domains. Many descriptors summarize
  generated defaults, loaded maps, dynamic definitions, or addon snapshots; a
  generic writer risks corrupting comments, structure, or unsupported content.
- Emit audit history for validation previews. Admins may test invalid or
  tentative values; only successful mutations should become administrative
  history.

Tests required before Apply is enabled:

- Apply registry rejects duplicate registrations and reports missing appliers.
- Apply rejects unknown targets, no permission, stale expected-current values,
  parse failures, validation failures, unsupported restart-required entries,
  and targets with no applier.
- Apply performs no file or runtime mutation on any rejected request.
- Successful apply re-runs validation, persists through the owner applier,
  returns `APPLIED`, emits exactly one audit event/history record, refreshes
  Admin Panel state, and marks whether runtime reload or restart is required.
- Failed write/reload restores previous file/runtime state or preserves a
  documented recoverable backup.
- Validation result `canApply` remains false unless a registered applier exists
  and the entry's reload/persistence policy is safe.

Recommended next implementation slice:

- Phase 3, Slice 28: Core config apply contract records/registry, no writers.
- Classification: MEDIUM.
- Add the Core apply contract types and focused tests for registration,
  duplicate/missing applier behavior, and apply-readiness lookup. Wire nothing
  to file writes, reload orchestration, UI Apply enablement, or addon domains
  yet.

## Phase 3 Slice 28

Status: completed on 2026-07-06.

Implemented:

- Added `ElarionConfigApplyRegistry` with explicit target registration,
  duplicate rejection, lookup, and descriptor-aware readiness checks.
- Added `ElarionConfigApplier`, `ElarionConfigApplyCapability`,
  `ElarionConfigApplyContext`, and `ElarionConfigApplyReadiness`.
- Enabled capability metadata requires an audit event type and at least one
  affected file. It also declares runtime-reload and restart-required support.
  Disabled registrations carry a server-authored reason.
- Readiness blocks unknown domains/categories/entries, missing or disabled
  appliers, and reload/restart policy mismatches with structured config change
  errors.
- Apply context rejects requests whose target IDs do not match the resolved
  descriptor records.

Boundaries preserved:

- The apply registry is not exposed through `ElarionApi` or another singleton.
- No production appliers are registered or invoked.
- Admin Panel open/validate dispatch still reports `canApply=false`, and the
  client Apply button remains disabled.
- No config files, descriptors, reload behavior, rollback behavior, packets,
  persistence, commands, or gameplay behavior changed.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Unresolved risks:

- Registry ownership and initialization lifetime are not yet defined.
- No service re-runs validation and invokes an applier atomically.
- No write, backup, rollback, reload, audit/history, or post-apply sync path
  exists.
- No addon or Core domain has proven its persistence/reload policy through an
  applier registration.

Recommended next slice:

- Phase 3, Slice 29: config apply registry ownership and readiness wiring
  proposal.
- Classification: SMALL.
- Audit where Core should own/expose the registry, when addons may register,
  and how readiness should inform server-authored edit controls/results while
  Apply remains disabled.
- Exclude implementation, file writes, reload/rollback orchestration, UI Apply
  enablement, descriptor changes, and addon/domain appliers.

## Phase 3 Slice 29

Status: completed on 2026-07-06.

Objective:

- Decide canonical apply-registry ownership, safe addon registration exposure,
  registration timing, and future Admin readiness consumption without changing
  production behavior.

Verified facts:

- `ElarionCoreMod` constructs the canonical descriptor registry before
  `ElarionApi`, registers Core descriptors, binds the Admin service, and only
  then initializes addons in dependency order.
- `ElarionSystemApi.configs()` exposes the concrete descriptor registry because
  descriptor lookup and registration are read-only with respect to runtime
  state.
- `ElarionConfigApplyRegistry.registration()` returns a registration containing
  the executable applier. Exposing this concrete registry would let unrelated
  addon code invoke another domain's writer outside future authorization,
  serialization, rollback, audit, and synchronization paths.
- `ElarionAdminPanelService` currently receives the descriptor registry through
  its bound `ElarionApi`. Its open-control and validation helpers have no apply
  registry dependency.
- `ElarionConfigEditControl` already carries `editable` and a disabled reason;
  `ElarionConfigEditResultPayload` already carries `canApply` and audit preview.
- The client hard-disables Apply and sends only `VALIDATE`. The server rejects
  every `APPLY` intent before validation.
- Most addon descriptors register during addon initialization after their
  validated definitions/services exist. NPC descriptors register after the
  first successful `SERVER_STARTED` definition load under an atomic one-time
  guard.

Decisions:

- Core owns one canonical `ElarionConfigApplyRegistry` for the process lifetime,
  constructed beside `ElarionConfigRegistry` before `ElarionApi` and addon
  initialization.
- Addons receive a registration-only interface or facade, tentatively
  `ElarionConfigApplyRegistrar`, through `ElarionSystemApi`. The facade must be
  a wrapper/method reference rather than the concrete registry typed as an
  interface, preventing a cast back to executable registration lookup.
- Core Admin/apply services receive a non-executable readiness function or view.
  Only a future Core apply coordinator receives concrete registration lookup
  and may invoke appliers.
- Appliers register immediately after the matching descriptor registration and
  after the owning validated snapshot/service exists. Deferred domains register
  descriptor and applier under the same one-time guard. Registration must not
  repeat on each server start.
- Readiness is advisory until execution orchestration exists. Admin open controls
  may eventually show the precise missing/disabled/reload/restart readiness
  reason. A ready registration still receives a global "apply execution is not
  enabled" reason for now.
- `ElarionConfigEditControl.editable` and result `canApply` remain false until a
  Core coordinator can revalidate, serialize by affected file/domain, invoke the
  owner applier, roll back failures, emit one audit event, and resynchronize
  affected state.
- When execution exists, `canApply` is the conjunction of successful validation,
  current readiness, permission, and coordinator availability. Apply must still
  rerun validation/readiness because preview results can become stale.

Rejected options:

- Expose `ElarionConfigApplyRegistry` directly through `ElarionSystemApi`.
  Concrete registration lookup exposes executable appliers.
- Let Admin invoke `Registration.applier()` directly. Execution policy belongs
  in one Core coordinator, not UI/service presentation code.
- Mark controls editable solely because readiness reports true. The registry
  does not yet provide concurrency control, rollback, audit, or resync.
- Register appliers only at every `SERVER_STARTED`. The canonical registry is
  process-lived and rejects duplicates; service instances can bind/unbind to the
  active server while their registration remains stable.

Compatibility and architecture impact:

- This proposal changes no Java API or behavior.
- The planned registrar is additive. Descriptor APIs, files, commands, packets,
  save data, and existing addon initialization remain compatible.
- Server authority remains intact because registration is not execution.

Verification:

- No Java tests were run; this was a documentation/proposal slice only.
- Documentation whitespace and diff checks are required before handoff.

Recommended next slice:

- Phase 3, Slice 30: canonical apply registry ownership and registration-only
  Core API.
- Classification: MEDIUM.
- Add a narrow registrar contract, construct the canonical registry in Core,
  and expose only a non-castable registration facade from `ElarionSystemApi`.
- Add focused ownership/duplicate-registration tests and update Core/config API
  docs.
- Exclude Admin readiness integration, production/domain appliers, execution,
  file writes, reload/rollback, audit emission, and UI Apply enablement.

## Phase 3 Slice 30

Status: completed on 2026-07-06.

Implemented:

- Added the functional `ElarionConfigApplyRegistrar` contract with registration
  as its only declared operation.
- `ElarionCoreMod` now constructs one canonical
  `ElarionConfigApplyRegistry` beside the descriptor registry before
  `ElarionApi` and addon initialization.
- `ElarionApi` passes a `configApplyRegistry::register` method reference into
  `ElarionSystemApi`.
- `ElarionSystemApi.configAppliers()` exposes the registrar interface, not the
  concrete registry. Consumers therefore cannot use the API surface to obtain
  `Registration.applier()` or invoke another domain's executable registration.

Tests:

- Added facade delegation coverage to `ElarionConfigApplyRegistryTest`.
- The test verifies duplicate rejection through the facade, the registrar's
  single-method surface, and that `ElarionSystemApi.configAppliers()` returns
  `ElarionConfigApplyRegistrar` rather than the concrete registry.
- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Boundaries preserved:

- No Core or addon applier registrations were added.
- Admin Panel does not receive readiness or executable lookup.
- `editable=false`, `canApply=false`, server `APPLY` rejection, and the disabled
  client Apply button are unchanged.
- No config files, descriptors, packets, persistence, commands, reload/rollback,
  audit/history emission, or gameplay behavior changed.

Compatibility and architecture impact:

- The public API addition is additive.
- Existing descriptor registration and addon initialization remain unchanged.
- The canonical registry is process-lived through the method-reference facade
  retained by `ElarionSystemApi`.

Unresolved risks:

- Admin still shows one generic disabled reason and cannot query readiness.
- No Core apply coordinator owns validation recheck, serialization, execution,
  rollback, audit, or synchronization.
- No production config owner has registered an applier.

Recommended next slice:

- Phase 3, Slice 31: non-executable Admin config readiness integration.
- Classification: MEDIUM.
- Add a readiness-only view over the canonical registry and inject it into the
  Admin config open/validation flow. Use readiness only for precise disabled
  reasons while `editable=false` and `canApply=false` remain invariant.
- Exclude production/domain registrations, executable lookup/invocation, file
  writes, reload/rollback, audit emission, and UI Apply enablement.

## Phase 3 Slice 31

Status: completed on 2026-07-06.

Implemented:

- Added `ElarionConfigApplyReadinessProvider` as a non-executable functional
  contract.
- Core binds
  `target -> configApplyRegistry.readiness(configRegistry, target)` into the
  Admin service after Core descriptors are registered.
- Admin config open controls use readiness for target-specific disabled reasons.
- Successful validation result messages include the same readiness reason.
- A ready registration receives the separate global reason
  `Config apply execution is not enabled yet.`

Invariants preserved:

- The provider cannot retrieve `Registration` or `ElarionConfigApplier`.
- Controls remain `editable=false`; results remain `canApply=false`.
- Server `APPLY` intent rejection and the disabled client Apply button are
  unchanged.
- No production appliers, file writes, reload/rollback, audit emission, packet
  changes, persistence, or gameplay behavior were added.

Verification:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Recommended next approved slice:

- Phase 3, Slice 32: Core config apply coordinator contract audit.
- Classification: SMALL.
- Specify the internal coordinator's lock scope, validation/readiness order,
  trusted context resolution, applier result checks, rollback responsibility,
  audit event timing, and rejection behavior.
- Exclude production code, Admin Apply wiring, production/domain appliers, file
  writes, reload behavior, and UI enablement.

## Phase 3 Slice 32

Status: completed on 2026-07-06.

Verified coordinator requirements:

- Validation and readiness must be rerun inside the same mutation lock used for
  execution; preview validation cannot authorize Apply.
- Descriptor domain/category/entry records must be resolved from the canonical
  server registry immediately before owner code runs.
- A conservative global config-mutation lock is acceptable initially. Admin
  config writes are rare, and a global lock avoids incorrect overlapping-file
  lock groups. Narrower keyed locks require demonstrated need and overlap tests.
- The coordinator must reject null, thrown, `VALIDATED`, mismatched-request,
  mismatched-value, or mismatched-audit results from an owner applier.
- Successful applied results must preserve the validator's reload/restart flags
  and the registered capability's audit event type.
- Audit occurs only after successful commit and must include actor, target,
  old/new values, affected files, reason, and runtime policy.

Blocking contract findings:

- `ElarionConfigApplier.apply(context)` permits immediate mutation and returns a
  final result. There is no coordinator-owned rollback operation if commit,
  audit/history, or post-apply synchronization fails.
- Admin audit currently emits a domain event and separately records history.
  Either post-mutation operation can fail independently.
- `ElarionConfigChangeResult.applied(...)` currently hardcodes both
  reload/restart flags to false instead of preserving validated descriptor
  policy.

Decision:

- Do not implement a coordinator on the immediate callback contract.
- Evolve the currently unused production applier API to a prepare phase that
  returns an explicit prepared transaction with `commit()` and `rollback()`.
- Preparation must not mutate authoritative files/runtime state. The future
  coordinator will prepare under lock, commit, perform mandatory audit/history,
  and invoke rollback if commit or audit completion fails.
- Transaction rollback must be idempotent. Domain registration remains forbidden
  until its implementation proves backup/temp-write, runtime snapshot, and
  rollback behavior with focused tests.

Recommended next approved slice:

- Phase 3, Slice 33: transactional config applier contract.
- Classification: MEDIUM.
- Add the prepared transaction interface, change applier registration to prepare
  rather than immediately apply, add an applied-result overload preserving
  reload/restart flags, and update focused contract tests.
- Exclude coordinator implementation, production registrations, Admin Apply
  wiring, file writes, reload behavior, audit emission, and UI enablement.

## Phase 3 Slice 33

Status: completed on 2026-07-06.

Implemented:

- Added `ElarionConfigPreparedChange` with explicit `commit()` and `rollback()`.
- Added a synchronized `of(commit, rollback)` implementation that allows one
  successful commit, prevents commit after rollback, and invokes rollback at
  most once. A failed commit remains rollback-capable.
- Changed `ElarionConfigApplier` to `prepare(context)`, returning the prepared
  change instead of immediately returning an applied result.
- Added an `ElarionConfigChangeResult.applied(...)` overload preserving
  reload/restart flags. Applied results now require a nonblank audit event type.

Verification:

- Added `ElarionConfigPreparedChangeTest` for commit-once, rollback idempotency,
  rollback-before-commit, and rollback-after-failed-commit behavior.
- Updated apply registry, Admin readiness, and change contract tests.
- Passed focused prepared-change/change-contract/registry/Admin tests.
- Passed full `.\gradlew.bat :platform:core:test`.
- Passed focused prepared-change/change-contract tests after removing an
  unintentionally public internal state enum.

Boundaries preserved:

- No coordinator invokes prepared changes.
- No production registrations, Admin Apply handling, file writes, reload,
  audit/history emission, packet changes, persistence, or UI behavior changed.

Recommended next slice:

- Phase 3, Slice 34: internal config apply coordinator implementation.
- Classification: MEDIUM.
- Implement an unwired coordinator with conservative global locking,
  validation/readiness recheck, trusted descriptor context, transactional
  execution, mandatory audit sink, strict result checks, and rollback on
  commit/audit failure.
- Exclude production registrations, Admin/network/UI wiring, real file writes,
  reload orchestration, and client synchronization.

## Phase 3 Slice 34

Status: completed on 2026-07-06.

Implemented:

- Added `ElarionConfigApplyAuditRecord` with target, actor, reason, old/new
  display values, runtime policy, audit event type, and immutable affected files.
- Added mandatory `ElarionConfigApplyAuditSink`.
- Added an unwired `ElarionConfigApplyCoordinator` with one fair static
  `ReentrantLock`, serializing all coordinator instances in the process.

Execution contract:

1. Acquire the global mutation lock.
2. Rerun descriptor resolution, permission, stale-value, parse, and semantic
   validation.
3. Recheck apply readiness and reject missing/disabled/policy-unsafe targets.
4. Resolve canonical domain/category/entry and executable registration.
5. Ask the owner applier to prepare without mutation.
6. Commit the prepared change.
7. Require an `APPLIED` result whose request, old/new values, reload/restart
   flags, and audit type exactly match trusted validation/capability data.
8. Submit structured audit data to the mandatory sink.
9. Return a normalized trusted `APPLIED` result.

Failure behavior:

- Validation/readiness failures return without preparing owner code.
- Preparation exceptions/nulls return `APPLY_FAILED` without rollback because
  preparation is contractually non-mutating.
- Commit, invalid owner result, or audit failure invokes rollback and returns
  `APPLY_FAILED`.
- Rollback failure is appended to the original apply failure message.

Verification:

- Added `ElarionConfigApplyCoordinatorTest` covering trusted success/audit,
  parse/stale/missing rejection, preparation failure, commit rollback, invalid
  owner-result rollback, audit rollback, combined commit/rollback failure, and
  concurrent serialization.
- Passed focused coordinator tests twice, including after converting the lock
  from instance scope to fair process-global scope.
- Passed full `.\gradlew.bat :platform:core:test`.

Boundaries preserved:

- Core does not construct or retain a production coordinator instance.
- No production applier registrations exist.
- Admin still rejects `APPLY`, returns `canApply=false`, and renders a disabled
  client Apply button.
- No real config files, runtime snapshots, reload behavior, packets,
  persistence, or synchronization changed.

Recommended next slice:

- Phase 3, Slice 35: coordinator ownership and audit-adapter proposal.
- Classification: SMALL.
- Decide canonical lifecycle/ownership, audit metadata and ordering between
  history and non-throwing domain-event dispatch, Admin execution exposure, and
  unbound-history behavior.
- Exclude implementation, production appliers, Admin Apply enablement, files,
  reload orchestration, packets, UI, and synchronization.

## Phase 3 Slice 35

Status: completed on 2026-07-06.

Verified facts:

- Core can construct a coordinator after descriptor/apply registries and before
  addon initialization, but `HistoryService` is not bound until
  `SERVER_STARTED`.
- `HistoryService.record(...)` throws while unbound.
- General History recording is controlled by `HistoryRecordingPolicy`; admins
  can disable all history or the administration category/type.
- History and index appends enter bounded in-memory queues and flush later.
  Successful `record(...)` does not mean bytes are durably forced to disk.
- `ElarionEventBus.emitDomainEvent(...)` catches listener failures and returns;
  it is suitable for non-authoritative integration projection after commit.
- Admin currently rejects every `APPLY` intent before validation and receives no
  coordinator reference.

Decision:

- Do not use general History as the mandatory coordinator audit sink.
- Add a dedicated Core-owned write-ahead config audit journal before production
  coordinator construction or Admin execution.
- Target journal location: `world/elarion/core/audit/config-changes.jsonl`, with
  a stable schema version and append ordering protected by the coordinator's
  global mutation lock.
- Target audit phases: `PREPARED`, `COMMITTED`, `ROLLED_BACK`, and `FAILED`, all
  tied by one stable audit ID.
- PREPARED records actor, target, reason, old/new display values, affected files,
  runtime policy, audit event type, and timestamp before owner commit.
- PREPARED and COMMITTED must be synchronously appended and forced before the
  coordinator advances/returns success. Administrative config writes are rare;
  this bounded synchronous durability cost is justified.
- If journal preparation fails, do not commit. If owner commit fails, roll back
  and record FAILED/ROLLED_BACK. If COMMITTED journal append fails after owner
  commit, roll back and record the failure when possible.
- After durable COMMITTED, general History and domain events are projections.
  Projection failure must be logged/retried but must not roll back a durably
  committed config change.
- Execution is unavailable until the journal is bound to an active server/world
  path. This cleanly handles unbound History/server startup.
- Admin eventually receives an execution-only facade, tentatively
  `ElarionConfigApplyExecutor`, rather than concrete coordinator/registry access.
- Do not register secret-like entries until descriptors support sensitivity and
  audit redaction. Initial appliers must be explicitly non-sensitive.

Compatibility/persistence proposal:

- Current schema: no config apply audit journal exists.
- Target schema: append-only JSONL records with schema version, audit ID, phase,
  timestamp, actor, normalized target, reason, old/new values or redacted
  markers, runtime flags, event type, affected files, and optional failure.
- Migration: none; create the file lazily on first prepared mutation.
- Backup/rollback: append-only journal is never rewritten. Config owner
  transactions still own file/runtime rollback.
- Corruption behavior: reject new Apply if journal initialization/append/force
  fails; retain existing bytes and surface an operator-visible failure.
- Restart behavior: scan only the bounded unresolved tail/index, not full player
  history. Recovery of PREPARED without terminal phase must be designed before
  the first production applier.

Recommended next slice:

- Phase 3, Slice 36: write-ahead config audit session contracts.
- Classification: MEDIUM.
- Replace `ElarionConfigApplyAuditSink.record(...)` with a preparation operation
  returning a session that can record committed, rolled-back, or failed outcome.
- Adjust only the unwired coordinator and focused tests. Do not add storage or
  production wiring yet.

## Phase 3, Slice 36 Completion - Write-Ahead Audit Session Contracts

Status: completed.

Objective:

- Convert the unwired coordinator's mandatory audit dependency from a one-shot
  post-commit record call into a pre-commit write-ahead session contract.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `INDEX.md`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSession.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- `ElarionConfigApplyAuditSink.prepare(record)` is the mandatory pre-commit
  audit boundary.
- `ElarionConfigApplyAuditSession` owns terminal outcomes:
  `committed()`, `rolledBack(failure)`, and `failed(failure)`.
- Audit preparation failure prevents owner commit and rolls back the prepared
  owner resource.
- Commit, owner-result validation, or committed-terminal failure rolls back and
  records `rolledBack(...)`.
- Rollback failure records `failed(...)`.
- Validation and readiness failures still return before owner preparation or
  audit preparation.
- The coordinator remains internal and unwired; no storage, production
  executor, Admin execution, appliers, reload, or synchronization was added.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 37: durable config audit journal storage.
- Classification: MEDIUM persistence slice.
- Implement an unbound journal-backed audit sink with versioned JSONL
  PREPARED/COMMITTED/ROLLED_BACK/FAILED records, synchronous append and force,
  bounded unresolved-tail recovery, and temporary-directory tests.
- Exclude production binding, coordinator ownership, appliers, Admin/network/UI
  execution, config file writes, reload, and synchronization.

## Phase 3, Slice 37 Completion - Durable Config Audit Journal Storage

Status: completed.

Objective:

- Add the unbound durable write-ahead audit journal needed by future config
  apply execution.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalStorage.java`
- `platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalCodec.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditRecord.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSession.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditPhase.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditJournal.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyAuditJournalTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- The durable journal is a config-owned Core utility, not general History.
- It is constructed with an explicit path and is not bound to a server/world
  service yet.
- `journalPath(root)` resolves to `core/audit/config-changes.jsonl` below the
  supplied Elarion world root.
- Every phase writes a versioned JSONL object with one audit ID, phase,
  timestamp, full normalized audit record, and optional failure text.
- PREPARED, COMMITTED, ROLLED_BACK, and FAILED are explicit enum phases.
- Appends are synchronous and call `FileChannel.force(false)` before returning.
- Recovery inspects only a requested tail window and reports whether the tail
  was truncated, so production binding can refuse unsafe startup states later.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 38: production config apply ownership proposal.
- Classification: SMALL proposal slice.
- Decide the Core service/facade that binds the descriptor registry, apply
  registry, coordinator, and durable audit journal to the active server/world
  path.
- Exclude domain appliers, Admin Apply wiring, config file writes, reload, and
  synchronization until that ownership boundary is approved.

## Phase 3, Slice 38 Proposal - Production Config Apply Ownership

Status: completed proposal.

Objective:

- Decide where production config apply execution should live before wiring any
  mutable Admin behavior.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/storage/JsonStateStorage.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditJournal.java`

Decision:

- Add a Core-owned `ElarionConfigApplyService` in a future implementation
  slice.
- The service owns production construction and lifecycle for
  `ElarionConfigApplyCoordinator`.
- It receives the canonical `ElarionConfigRegistry` and concrete
  `ElarionConfigApplyRegistry` during Core initialization.
- It binds on `SERVER_STARTED` using `JsonStateStorage.elarionRoot(server)` and
  `ElarionConfigApplyAuditJournal.journalPath(root)`.
- It refuses apply execution until bound, until journal recovery is safe, and
  when any unresolved PREPARED record appears in the bounded recovery window.
- It exposes:
  - readiness lookup for Admin disabled reasons.
  - a narrow execute method for future Admin Apply requests.
- It does not expose coordinator, concrete apply registry lookup, or journal
  mutation through `ElarionApi`.
- `ElarionApi.system().configAppliers()` remains registration-only for addons.
- Admin should bind to the service through a narrow executor/readiness facade,
  not the concrete coordinator or registry.

Lifecycle rationale:

- `ElarionCoreMod` currently creates the descriptor registry and applier
  registry before constructing `ElarionApi`.
- Addons initialize after API construction and before `SERVER_STARTED`, so addon
  applier registrations can be present before the service binds to a server
  world path.
- Core services already bind durable world-owned state on `SERVER_STARTED`.
  Config apply execution should follow that pattern rather than using static
  global server lookups.

Compatibility and safety:

- No public addon API changes are needed for ownership binding.
- No save/config schema changes are needed for the service skeleton beyond the
  already-defined journal file when execution later occurs.
- Admin Apply must remain disabled until the service, at least one safe owner
  applier, and apply-result packet behavior are wired together.
- Recovery policy must be conservative: unresolved PREPARED audit records block
  Apply and surface an operator-visible reason instead of guessing whether the
  owner file mutation occurred.

Recommended next slice:

- Phase 3, Slice 39: implement `ElarionConfigApplyService` skeleton.
- Classification: SMALL.
- Add the service class, bind it in `ElarionCoreMod`, route Admin readiness
  through the service, and add focused tests for unbound, bound, unresolved
  audit recovery, and readiness delegation.
- Exclude execute/apply wiring, domain appliers, Admin Apply enablement, config
  file writes, reload, and synchronization.

## Phase 3, Slice 39 Completion - Config Apply Service Skeleton

Status: completed.

Objective:

- Add the Core-owned service skeleton that will own production config apply
  lifecycle and readiness.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyReadiness.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyRegistryTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- `ElarionConfigApplyService` is the production lifecycle owner.
- It binds to the active world root on server start and constructs the durable
  journal/coordinator only after safe bounded recovery.
- It blocks otherwise-ready targets if unbound, recovery is truncated, or
  unresolved PREPARED audit records exist.
- Admin readiness is routed through the service.
- The public addon API remains registration-only.
- No execute/apply method exists yet.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 40: backend config apply execution method.
- Classification: SMALL.
- Add a narrow `apply(request, actorPermission)` method to
  `ElarionConfigApplyService` that rejects while unbound/unsafe and delegates to
  the coordinator only when ready.
- Use fake test-only appliers; exclude production appliers, Admin/network/UI
  Apply wiring, config file writes, reload, and synchronization.

## Phase 3, Slice 40 Completion - Backend Config Apply Execution

Status: completed.

Objective:

- Add backend-only execution delegation to the Core config apply service without
  enabling Admin Apply or any production applier.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- `ElarionConfigApplyService.apply(request, actorPermission)` is the backend
  execution seam for future Admin Apply.
- The service returns `UNSUPPORTED` before coordinator delegation if unbound or
  audit recovery is unsafe.
- Bound/safe execution delegates to `ElarionConfigApplyCoordinator`.
- Tests use fake in-test appliers only.
- Admin packets and UI remain disabled for Apply.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 41: Admin config apply executor boundary proposal.
- Classification: SMALL proposal slice.
- Decide the narrow interface Admin should use for apply execution, OP-level
  checks, packet response behavior, and the UI-disabled rollout boundary.
- Exclude implementation, production domain appliers, config file writes,
  reload, and synchronization.

## Phase 3, Slice 41 Proposal - Admin Config Apply Executor Boundary

Status: completed proposal.

Objective:

- Decide how Admin should reach future config apply execution without exposing
  coordinator, concrete apply registry, or journal internals.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`

Decision:

- Add a narrow `ElarionConfigApplyExecutor` contract in a future implementation
  slice.
- The executor should expose only:
  - `readiness(ElarionConfigEditTarget target)`
  - `apply(ElarionConfigChangeRequest request, ElarionConfigPermission actorPermission)`
- `ElarionConfigApplyService` should implement the executor.
- `ElarionAdminPanelService` should bind one executor instead of only a
  readiness provider.
- The first implementation of the boundary should keep APPLY rejected exactly
  as today. It should only replace the readiness binding with the executor
  binding so Admin no longer needs a second wiring change later.
- A later separate slice may route server-side `Intent.APPLY` to the executor,
  but only after tests cover OP checks, stale expected-current behavior, result
  payload conversion, and post-apply Admin Panel refresh.
- The client Apply button remains disabled until there is at least one approved
  production applier and clear UX for reload/restart outcomes.

Compatibility and safety:

- Existing packet shape already supports `Intent.APPLY`; no packet schema
  change is needed for the boundary.
- OP level 4 remains the server-side gate.
- Forged client packets must still be safe because the server controls whether
  APPLY is dispatched.
- No public addon API should expose executable lookup or cross-domain apply.

Recommended next slice:

- Phase 3, Slice 42: add Admin config apply executor facade.
- Classification: SMALL.
- Add `ElarionConfigApplyExecutor`, have `ElarionConfigApplyService` implement
  it, and bind Admin to the executor while keeping APPLY rejected.
- Update focused Admin/service tests. Exclude server-side APPLY dispatch,
  production appliers, config file writes, reload, synchronization, and UI
  Apply enablement.

## Phase 3, Slice 42 Completion - Admin Config Apply Executor Facade

Status: completed.

Objective:

- Add the narrow executor facade between Admin config edit handling and backend
  config apply execution, without dispatching APPLY yet.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyExecutor.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- `ElarionConfigApplyExecutor` is the Admin-facing facade.
- It exposes readiness and backend apply only.
- `ElarionConfigApplyService` implements the executor.
- Admin now binds an executor through `bindConfigApplyExecutor(...)`.
- The previous readiness-only binding remains as a compatibility helper for
  focused tests and wraps readiness in an executor that rejects apply.
- Server-side `Intent.APPLY` still rejects before dispatch.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 43: server-side Admin APPLY dispatch proposal.
- Classification: SMALL proposal slice.
- Decide exact request/result conversion, OP checks, stale-value behavior,
  Admin refresh, and client button rollout before implementation.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

## Phase 3, Slice 43 Proposal - Server-Side Admin APPLY Dispatch

Status: completed proposal.

Objective:

- Decide how server-side Admin config edit requests should dispatch APPLY to
  the executor in a future implementation slice.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decision:

- Keep `ElarionConfigEditRequestPayload` unchanged. Its existing
  `Intent.APPLY` is enough for server-side dispatch.
- Keep the global receiver in `ElarionCoreMod` unchanged structurally:
  packet -> server thread -> `adminPanel.validateConfigEdit(...)` -> send result
  -> reopen Config tab with the result message.
- Evolve `validateConfigEdit(...)` internally so it delegates to a shared
  static helper that handles both VALIDATE and APPLY.
- Rename can be deferred to avoid a broader call-site churn; behavior and tests
  matter more than the method name in the next small slice.
- OP level 4 remains checked before validation or apply dispatch.
- Build `ElarionConfigChangeRequest` exactly once from target, proposed raw
  value, expected current display value, actor UUID, and reason. Empty reasons
  become `admin-panel-config-edit-apply` for APPLY and
  `admin-panel-config-edit-preview` for VALIDATE.
- VALIDATE continues to use `ElarionConfigChangeValidator.validate(...)`.
- APPLY calls `ElarionConfigApplyExecutor.apply(request, OPERATOR)` only after
  the OP check.
- Convert `ElarionConfigChangeResult` to `ElarionConfigEditResultPayload`:
  - APPLIED -> `status=APPLIED`, old/new values from result, runtime flags,
    `canApply=false`, audit preview describing the completed change, and a
    message beginning `Applied:`.
  - VALIDATED -> current behavior.
  - REJECTED -> current invalid/rejected behavior with first useful error
    message.
- `canApply` should remain false in the immediate implementation. This avoids
  advertising UI Apply as generally available while no production appliers
  exist.
- The client Apply button should remain disabled until a later UI slice can
  enable it conditionally from server-authored controls.

Required tests for the implementation slice:

- APPLY rejects non-OP before executor dispatch.
- APPLY dispatches to a fake executor for OP requests.
- APPLY preserves stale expected-current behavior via the executor/coordinator
  result.
- APPLIED result converts into `ElarionConfigEditResultPayload` with old/new,
  runtime flags, audit preview, `canApply=false`, no errors, and an `Applied:`
  message.
- Rejected executor result converts to a rejected payload with the error
  preserved.
- VALIDATE behavior remains unchanged.

Recommended next slice:

- Phase 3, Slice 44: implement server-side Admin APPLY dispatch.
- Classification: SMALL.
- Change only Admin service request/result conversion and focused tests.
- Keep client Apply disabled and use fake test executors only. Exclude
  production appliers, config file writes, reload, synchronization, and UI
  Apply enablement.

## Phase 3, Slice 44 Completion - Server-Side Admin APPLY Dispatch

Status: completed.

Objective:

- Dispatch server-side Admin config APPLY requests to the executor while keeping
  the client Apply button disabled and using fake test executors only.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- `configEditResult(...)` now handles both VALIDATE and APPLY.
- VALIDATE continues through `ElarionConfigChangeValidator`.
- APPLY requires OP level 4 before executor dispatch.
- APPLY builds the same request shape as validation and defaults empty reasons
  to `admin-panel-config-edit-apply`.
- APPLY dispatches through `ElarionConfigApplyExecutor.apply(...)`.
- APPLIED results convert to payloads with old/new values, runtime flags,
  audit preview, `canApply=false`, no errors, and an `Applied:` message.
- Rejected executor results preserve errors and use `Apply failed: ...`.
- Client Apply remains disabled.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 45: Admin Apply UI enablement proposal.
- Classification: SMALL proposal slice.
- Decide how server-authored edit controls should expose Apply availability and
  what checks are required before making the client Apply button clickable.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

## Phase 3, Slice 45 Proposal - Admin Apply UI Enablement

Status: completed proposal.

Objective:

- Decide how and when the Admin config edit UI may make the Apply button
  clickable.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decision:

- Keep the client Apply button disabled until at least one production applier is
  approved and tested. Backend dispatch alone is not sufficient.
- Apply availability must be server-authored on the edit control, not inferred
  by the client from validation success.
- The existing `editable` boolean is too broad for the next rollout because it
  mixes "input can be edited" and "Apply can execute".
- Add explicit server-authored apply state in a future implementation slice:
  - `boolean inputEditable`
  - `boolean applyAvailable`
  - `String applyDisabledReason`
- Preserve compatibility by deriving these from existing `editable` /
  `disabledReason` initially, or by replacing `editable` only with a packet
  round-trip test update in the same slice.
- Client click handling must require:
  - open control exists.
  - input is nonblank.
  - matching latest validation result exists.
  - latest validation result has status VALIDATED.
  - latest validation result old/new values still match current input and
    control current value.
  - control apply availability is true.
  - result `canApply` is true.
- The client must send `Intent.APPLY` only from that combined predicate.
- The server remains authoritative and must still reject forged APPLY packets.
- Validation success must not automatically make Apply clickable while no
  production applier exists.

Required tests for the eventual UI enablement implementation:

- Edit-control payload round-trip includes separate input/apply state.
- Disabled control keeps Apply disabled even after VALIDATED result.
- Stale validation result does not enable Apply after the input changes.
- Matching VALIDATED result plus server-authored apply availability enables the
  button.
- Click sends `Intent.APPLY` only when enabled.
- Forged APPLY packet remains server-rejected when no executor/applier is ready.

Recommended next slice:

- Phase 3, Slice 46: split config edit control input/apply state.
- Classification: SMALL.
- Add explicit apply availability metadata to the control/payload model and
  focused packet/client-state tests, but keep the Admin screen Apply button
  disabled.
- Exclude production appliers, config file writes, reload, synchronization, and
  clickable Apply UI.

## Phase 3, Slice 46 Completion - Split Config Edit Control Input/Apply State

Status: completed.

Objective:

- Separate input editability from Apply availability in the server-authored
  config edit control model.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions:

- `ElarionConfigEditControl` now carries `inputEditable`,
  `applyAvailable`, `disabledReason`, and `applyDisabledReason`.
- The old constructor remains as a compatibility path and maps `editable` to
  `inputEditable`, with `applyAvailable=false`.
- `editable()` remains as a compatibility alias for `inputEditable()`.
- The open-payload codec round-trips explicit apply availability state.
- The Admin screen still renders Apply as disabled and does not send APPLY from
  the button.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 47: production config applier selection proposal.
- Classification: SMALL proposal slice.
- Choose the first low-risk production config entry/domain to make writable and
  define file write, reload, rollback, audit, UI, and test requirements.
- Exclude implementation.

## Phase 3, Slice 47 Proposal - First Production Config Applier Target

Status: completed proposal.

Objective:

- Select the first production config entry that should become writable through
  the Admin config apply path, and define implementation requirements before
  code changes.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionUiThemeService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`

Selected first target:

- `core:ui_theme:defaults.font-scale-percent`
- Source file: `config/elarion/core/ui_theme.yml`
- YAML path: `defaults.font-scale-percent`
- Type: bounded integer, `100-150`
- Runtime policy: runtime-reloadable, not restart-required
- Required permission: OPERATOR
- Audit event type: `core.config.ui_theme.font_scale_changed`

Why this target comes first:

- It is one scalar value, not a multi-field object.
- It already has descriptor coverage, focused validation tests, and a current
  typed runtime snapshot.
- `CoreConfigManager.load()` already rejects invalid theme values and preserves
  the previous valid in-memory theme when reload fails.
- `/e reload` already synchronizes UI theme state to connected clients through
  `ElarionUiThemeService.syncAll(...)`.
- The visible result is easy to verify without mutating gameplay authority,
  economy, Realm membership, routing, title ownership, or persistence state.

Targets explicitly deferred:

- Realm spawn coordinates: multi-field, gameplay-affecting, and should be
  edited as one grouped location transaction later.
- Server identity names/terms: broad text substitutions across many systems and
  identity sync behavior.
- Dynamic Realm/title/reward definition rows: generated from IDs present at
  registration and need object-level authoring rules before writes.
- Addon domains: each needs owner-specific reload safety before production
  appliers are registered.
- Restart-required settings: not suitable for the first live Admin edit path.

Implementation requirements for the first backend slice:

- Add a Core-owned applier registration for only
  `core:ui_theme:defaults.font-scale-percent`.
- Register it through the existing Core config applier registrar during Core
  initialization.
- Capability must declare runtime reload support, no restart-required support,
  affected file `config/elarion/core/ui_theme.yml`, and audit event type
  `core.config.ui_theme.font_scale_changed`.
- Preparation must parse and stage the requested scalar change without mutating
  the authoritative config file or active runtime snapshot.
- Commit must write `ui_theme.yml` with a temp-file plus replace strategy,
  reload the full Core config through `CoreConfigManager.load()`, and resync UI
  themes to connected players.
- Rollback must restore the exact previous file contents and reload/resync the
  previous valid theme if commit fails after touching the file.
- If the post-write reload fails, the applier must restore the previous file,
  reload the previous valid config, and return a rejected apply result.
- The applier must not write other Core config files, mutate descriptors,
  bypass validation, or trust client-submitted display values.

UI requirements:

- Backend applier readiness may make this target's edit control
  `inputEditable=true`.
- The first backend slice may still keep the client Apply button disabled.
- A later UI slice must enable Apply only when all of these are true:
  open control exists, proposed value is nonblank, latest validation result is
  for the same target, old/current value still matches, new/proposed value still
  matches, control `applyAvailable=true`, and result `canApply=true`.
- Server-side APPLY rejection must remain authoritative even after the client
  button becomes clickable.

Required tests for the first backend slice:

- Applier registration exposes ready state only for
  `core:ui_theme:defaults.font-scale-percent`.
- Valid `100`, `125`, and `150` values validate and can be prepared.
- Out-of-range and non-integer values remain rejected by the existing
  validator.
- Commit updates only `ui_theme.yml.defaults.font-scale-percent`.
- Commit reloads `CoreConfigManager` and exposes the new current descriptor
  value.
- Commit triggers the supplied UI theme sync hook.
- Simulated post-write reload failure restores the original file and active
  theme.
- Simulated write/commit failure records rejection and leaves the original file
  intact.
- `ElarionConfigApplyService` still blocks when audit recovery is unsafe.
- Admin open control reflects readiness for this target and remains blocked for
  unregistered targets.

Compatibility risks:

- `ui_theme.yml` formatting/comments may change if the writer uses generic YAML
  dumping. Prefer a narrow scalar writer or document formatting normalization
  before implementation.
- File replacement must work on Windows and should avoid partial writes.
- Client UI may reflow immediately after sync; this is intended for font scale
  but should be manually checked at Minecraft GUI scales.

Recommended next slice:

- Phase 3, Slice 48: Core UI theme font-scale applier backend.
- Classification: MEDIUM.
- Implement only the backend applier registration, file mutation, reload,
  rollback, audit-backed apply execution, and focused tests for
  `core:ui_theme:defaults.font-scale-percent`.
- Keep the Admin screen Apply button disabled unless the slice explicitly
  receives separate approval for UI click enablement.

## Phase 3, Slice 48 Completion - Core UI Theme Font-Scale Applier Backend

Status: completed.

Objective:

- Register the first production backend config applier for
  `core:ui_theme:defaults.font-scale-percent` without enabling the visible
  Admin Apply button.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplier.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplierTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Added `CoreUiThemeFontScaleConfigApplier` with one target:
  `core:ui_theme:defaults.font-scale-percent`.
- Registered capability:
  - affected file: `config/elarion/core/ui_theme.yml`
  - audit event type: `core.config.ui_theme.font_scale_changed`
  - runtime reload supported
  - restart-required apply not supported
- The applier reads the current `ui_theme.yml`, replaces exactly one
  `font-scale-percent:` scalar line, writes through a temp file plus replace,
  reloads `CoreConfigManager`, and runs the supplied UI theme sync hook.
- Rollback restores the exact prior file contents, reloads the previous valid
  Core config, and resyncs UI themes.
- Core initialization registers this applier with the existing canonical apply
  registry and captures the active server only for post-apply UI theme sync.
- The Admin screen Apply button remains disabled and no client UI click path was
  enabled.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test`

Recommended next slice:

- Phase 3, Slice 49: Admin Apply click enablement for the single font-scale
  target.
- Classification: MEDIUM.
- Enable the client Apply button only when server-authored apply availability
  and a matching latest validation result allow it, and keep all other config
  targets disabled.
- Exclude adding any new production config appliers.

## Phase 3, Slice 49 Completion - Single-Target Admin Apply Click Enablement

Status: completed.

Objective:

- Enable Admin Apply click behavior only for the server-authored ready
  `core:ui_theme:defaults.font-scale-percent` target while keeping every other
  config target disabled.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Server-opened config edit controls now derive `inputEditable` and
  `applyAvailable` from `ElarionConfigApplyReadiness`.
- Successful validation now returns `canApply=true` only when the target has a
  ready apply backend.
- The Admin client sends `Intent.APPLY` only when the open control, current
  proposed input, and latest `VALIDATED` result all match exactly.
- Applied results close the open edit shell to avoid reusing stale current
  values after reload/sync.
- Config edit packet directions were verified:
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload` are S2C;
  `ElarionConfigEditRequestPayload` is C2S.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest`
- `.\gradlew.bat :platform:core:test`

Phase 3 V1 status:

- Complete for the first safe Admin config edit path:
  read-only descriptor discovery, server-side validation, audit-backed backend
  apply, safe file write/reload/rollback, UI theme resync, and strict client
  Apply gating for `core:ui_theme:defaults.font-scale-percent`.
- Not complete for broad config editing. Addon config domains, Realm spawn,
  server identity, dynamic Core definitions, restart-required entries, and
  generic page/provider contracts remain future slices.

Recommended next slice:

- Phase 4, Slice 1: shared UI token/component audit and proposal.
- Classification: SMALL proposal slice.
- Inspect current Core UI primitives, Government/notification style, and
  duplicated literals to define the first UI-token consolidation target.
- Exclude implementation.

## Admin Panel Payload Hardening - 2026-07-06

Context:

- Live `/e panel` QA exposed custom-payload disconnects when opening the
  Admin Panel and when clicking Config. The first issue was fixed by building
  rows only for the selected tab; Config still needed scoped loading because
  the full descriptor list was too heavy for one tab payload.

Implementation notes:

- Config tab rows are now scoped. Opening Config sends domain/category
  summaries only; selecting a category sends an `AdminPanelOpenRequestPayload`
  for that row and the server responds with entry rows for that category.
- `AdminPanelOpenPayload` write logic caps tab, row, action, and input
  suggestion counts before serialization so future oversized snapshots cannot
  desynchronize the client decoder.
- `ElarionAdminPanelAction` gained bounded server-authored
  `parameterSuggestions`.
- The Admin Panel client cycles action suggestions with Tab inside the existing
  single-field modal. The client still sends only submitted values; the server
  validates every mutation.
- Core player rows gained an OP-only `Set Realm` action using
  `RealmService.assign`, identity sync, and admin event/history emission.
- Core suggestions cover Realm IDs, title IDs, and registered ability IDs.
  Mounts contributes Mount ID suggestions through its existing provider.
- Added `dev/tools/minecraft-qa.ps1` for faster live command/click/screenshot
  checks.

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
- `.\gradlew.bat :addons:mounts:compileJava`
- Live patched `runServer`/`runClientOne`: `/e panel` opened, Config clicked,
  `Core: UI Theme` category loaded, Players opened, and Set Realm
  Tab-completed a Realm ID without a custom-payload disconnect.

Evidence:

- `build/ui-qa/admin-config-overview-fixed.png`
- `build/ui-qa/admin-config-tab-fixed.png`
- `build/ui-qa/admin-config-category-fixed.png`
- `build/ui-qa/admin-players-set-realm-fixed.png`
- `build/ui-qa/admin-set-realm-tab-complete.png`

Remaining work:

- Danger-row confirmation-modal QA remains pending.
- Broad Admin Panel page/category provider contracts remain future work.

## Admin Config Edit Live Verification - 2026-07-06

Status: completed.

Implementation notes:

- `CoreUiThemeFontScaleConfigApplier` now repairs stale development
  `ui_theme.yml` files that are missing `defaults.font-scale-percent` by
  inserting the scalar into the existing `defaults` block before applying the
  new value.
- Duplicate `font-scale-percent` scalar lines still reject without mutation.
- `dev/tools/minecraft-qa.ps1` can now post mouse-wheel scroll events to the
  Minecraft client, making long Admin action lists repeatable in live QA.

Live verification:

- Config tab and scoped `Core: UI Theme` category rows opened without a
  custom-payload disconnect.
- `core:ui_theme:defaults.font-scale-percent` validated and applied from `100`
  to `125`, resynced the UI theme, and reflowed the Admin Panel text.
- The same entry was then validated/applied back from `125` to `100`; the dev
  config file ended with `font-scale-percent: 100`.
- `defaults.logical-width` reported no registered applier and Apply stayed
  non-mutating.
- The Players action list scrolled to Mount actions, and `Grant Mount`
  Tab-completed `airship` from Mount-owned suggestions.

Evidence:

- `build/ui-qa/font-apply-fixed-applied-125.png`
- `build/ui-qa/font-apply-restored-100.png`
- `build/ui-qa/config-non-applier-apply-disabled.png`
- `build/ui-qa/admin-players-mount-actions.png`
- `build/ui-qa/admin-grant-mount-tab-complete.png`

Verification:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest :addons:mounts:compileJava`
