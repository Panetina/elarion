# Current Project Status

Last audited in this documentation pass: 2026-07-06.
Last project-wide revamp audit slice: 2026-07-05.

This file is the fast recovery snapshot for a new AI session or a new machine.
It does not replace source-backed docs. Use it to orient, then follow the
authority chain in `INDEX.md`.

## Active Project-Wide Revamp Status

The project-wide revamp master plan is active. The completed current slices
are Phase 0, Slice 1: audit preparation and repository map verification,
Phase 0, Slice 2: Configuration and Admin Panel deep audit, and Phase 2,
Slice 1: Core read-only config descriptor registry, and Phase 2, Slice 2:
Groups read-only config descriptor domain, and Phase 3, Slice 1: read-only
Admin Panel config browser skeleton, and Phase 2, Slice 3: Economy read-only
config descriptor domain, and Phase 2, Slice 4: Worlds read-only config
descriptor domain, and Phase 2, Slice 5: Portals read-only config descriptor
domain, Phase 2, Slice 6: Offerings read-only config descriptor domain, and
Phase 2, Slice 7: Government read-only config descriptor domain, and Phase 2,
Slice 8: NPCs read-only config descriptor domain, and Phase 2, Slice 9: Quests
read-only config descriptor domain, and Phase 2, Slice 10: Realms protection
read-only config descriptor domain, and Phase 2, Slice 11: Mounts Collection
text read-only config descriptor domain, and Phase 2, Slice 12: Underworld
read-only config descriptor domain, Phase 2, Slice 13: Core-owned
Optimization/performance read-only config descriptor domain, Phase 2, Slice 14:
Core scalar read-only config descriptor expansion, and Phase 2, Slice 15: Core
Realm definition read-only descriptors, and Phase 2, Slice 16: Core title and
title-progression read-only descriptors, and Phase 2, Slice 17: Core reward
read-only descriptors, Phase 3, Slice 2: Admin Panel config browser contract
proposal, and Phase 3, Slice 3: dedicated read-only Admin Panel Config tab.
Phase 3, Slice 4 completed the typed config detail model proposal, and
Phase 3, Slice 5 added Config tab domain/category read-only rows. Phase 3,
Slice 6 completed the config mutation/readiness audit, Phase 3, Slice 7 added
Core config mutation contract records without write support, and Phase 3,
Slice 8 added a pure Core config mutation validator without write support.
Phase 3, Slice 9 completed the Admin Panel config validation preview action
proposal, and Phase 3, Slice 10 added Config entry rows plus a preview-only
`Validate Value` action without write support. Phase 3, Slice 11 completed
the dedicated config editing packet/model proposal. Phase 3, Slice 12 added
Core config edit model/payload records and codecs without registering
handlers. Phase 3, Slice 13 completed the config edit packet registration and
dispatch proposal. Phase 3, Slice 14 registered config edit payload types
without adding receivers. Phase 3, Slice 15 completed the validation-only
receiver/dispatch proposal. Phase 3, Slice 16 added the validation-only
config edit receiver and service dispatch. Phase 3, Slice 17 completed the
config edit client-result handling proposal. Phase 3, Slice 18 added the
client result receiver/cache. Phase 3, Slice 19 completed the config edit
open/detail UX proposal. Phase 3, Slice 20 added the config edit open-payload
client receiver/cache. Phase 3, Slice 21 completed the server open action
proposal, Phase 3, Slice 22 added the server `Open Editor` action and Config
row action, and Phase 3, Slice 23 completed the read-only detail shell
 proposal. Phase 3, Slice 24 added the read-only config edit detail shell.
Phase 3, Slice 25 completed the validation input/request proposal.
Phase 3, Slice 26 added validation input and request handling in the config
edit shell. Phase 3, Slice 27 completed the config edit Apply/readiness
proposal. Phase 3 V1 later added the first safe production Apply target for
`core:ui_theme:defaults.font-scale-percent`, including audit-backed
write/reload/rollback, strict client Apply gating, UI theme resync, and live
screenshot verification. Other config entries remain read-only or
validation-only until their owners register safe appliers.

Slice reports:

- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions confirmed for the long-term plan:

- Config/Admin comes before UI/Profile/Chronicle implementation.
- Existing config formats are preserved initially.
- Admin config starts as read-only discovery before editing.
- The civic brown/gold UI style is canonical for custom Elarion screens.
- Chronicle archive wording persists selected variant IDs.
- Profile visibility defaults conservative.
- Saves/config migrations must be live-safe.
- Shell addons stay in the build but must be labeled honestly.
- A dev-only UI gallery is part of the UI target after components stabilize.

Files changed in the 2026-07-05 slice:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigDomain.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigCategory.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigCodec.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPermission.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/config/GroupConfigDescriptors.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/ElarionGroupsAddon.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptors.java`
- `addons/economy/src/main/java/panetina/elarion/addons/economy/ElarionEconomyAddon.java`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/config/WorldsConfigDescriptors.java`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/ElarionWorldsAddon.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/config/PortalConfigDescriptors.java`
- `addons/portals/src/main/java/panetina/elarion/addons/portals/ElarionPortalsAddon.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/config/OfferingConfigDescriptors.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/ElarionOfferingsAddon.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/config/GovernmentConfigDescriptors.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/ElarionGovernmentAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptors.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/quests/src/main/java/panetina/elarion/addons/quests/config/QuestConfigDescriptors.java`
- `addons/quests/src/main/java/panetina/elarion/addons/quests/ElarionQuestsAddon.java`
- `addons/realms/src/main/java/panetina/elarion/addons/realms/config/RealmConfigDescriptors.java`
- `addons/realms/src/main/java/panetina/elarion/addons/realms/config/RealmProtectionConfig.java`
- `addons/realms/src/main/java/panetina/elarion/addons/realms/ElarionRealmsAddon.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/config/MountConfigDescriptors.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/ElarionMountsAddon.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/config/UnderworldConfigDescriptors.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/ElarionUnderworldAddon.java`
- `addons/optimization/src/main/java/panetina/elarion/addons/optimization/PerformanceConfigDescriptors.java`
- `addons/optimization/src/main/java/panetina/elarion/addons/optimization/ElarionOptimizationAddon.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigRegistryTest.java`
- `addons/groups/src/test/java/panetina/elarion/addons/groups/config/GroupConfigDescriptorsTest.java`
- `addons/economy/src/test/java/panetina/elarion/addons/economy/config/EconomyConfigDescriptorsTest.java`
- `addons/worlds/src/test/java/panetina/elarion/addons/worlds/config/WorldsConfigDescriptorsTest.java`
- `addons/portals/src/test/java/panetina/elarion/addons/portals/config/PortalConfigDescriptorsTest.java`
- `addons/offerings/src/test/java/panetina/elarion/addons/offerings/config/OfferingConfigDescriptorsTest.java`
- `addons/government/src/test/java/panetina/elarion/addons/government/config/GovernmentConfigDescriptorsTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigDescriptorsTest.java`
- `addons/quests/src/test/java/panetina/elarion/addons/quests/config/QuestConfigDescriptorsTest.java`
- `addons/realms/src/test/java/panetina/elarion/addons/realms/config/RealmConfigDescriptorsTest.java`
- `addons/mounts/src/test/java/panetina/elarion/addons/mounts/config/MountConfigDescriptorsTest.java`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/config/UnderworldConfigDescriptorsTest.java`
- `addons/optimization/src/test/java/panetina/elarion/addons/optimization/PerformanceConfigDescriptorsTest.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/addons/economy.md`
- `docs/addons/groups.md`
- `docs/addons/portals.md`
- `docs/addons/offerings.md`
- `docs/addons/government.md`
- `docs/addons/npcs.md`
- `docs/addons/quests.md`
- `docs/addons/realms.md`
- `docs/addons/mounts.md`
- `docs/addons/underworld.md`
- `docs/addons/optimization.md`
- `wiki/admin/performance.md`
- `wiki/admin/npcs.md`
- `wiki/admin/quests.md`
- `wiki/admin/README.md`
- `docs/addons/worlds.md`
- `docs/systems/GUI.md`
- `AGENTS.md`
- `CODEX.md`
- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `OPTIMIZATION_TRACKER.md`
- `docs/ai/CURRENT_STATUS.md`

Tests/builds run in this slice:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
- `.\gradlew.bat :platform:core:test`
- `.\gradlew.bat :addons:groups:test --tests panetina.elarion.addons.groups.config.GroupConfigDescriptorsTest`
- `.\gradlew.bat :addons:groups:test`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test :addons:groups:test`
- `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.config.EconomyConfigDescriptorsTest`
- `.\gradlew.bat :addons:economy:test :platform:core:test`
- `.\gradlew.bat :addons:worlds:test --tests panetina.elarion.addons.worlds.config.WorldsConfigDescriptorsTest`
- `.\gradlew.bat :addons:worlds:test :platform:core:test`
- `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.config.PortalConfigDescriptorsTest`
- `.\gradlew.bat :addons:portals:test :platform:core:test`
- `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.config.OfferingConfigDescriptorsTest`
- `.\gradlew.bat :addons:offerings:test :platform:core:test`
- `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.config.GovernmentConfigDescriptorsTest`
- `.\gradlew.bat :addons:government:test :platform:core:test`
- `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest`
- `.\gradlew.bat :addons:npcs:test :platform:core:test`
- `.\gradlew.bat :addons:quests:test --tests panetina.elarion.addons.quests.config.QuestConfigDescriptorsTest`
- `.\gradlew.bat :addons:quests:test :platform:core:test`
- `.\gradlew.bat :addons:realms:test --tests panetina.elarion.addons.realms.config.RealmConfigDescriptorsTest`
- `.\gradlew.bat :addons:realms:test :platform:core:test`
- `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.config.MountConfigDescriptorsTest`
- `.\gradlew.bat :addons:mounts:test :platform:core:test`
- `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.config.UnderworldConfigDescriptorsTest`
- `.\gradlew.bat :addons:underworld:test :platform:core:test`
- `.\gradlew.bat :addons:optimization:test --tests panetina.elarion.addons.optimization.PerformanceConfigDescriptorsTest`
- `.\gradlew.bat :addons:optimization:test :platform:core:test`
- No GameTest or broad build was run.

Key Config/Admin findings and current status:

- Core has the strongest current config validation/reload rollback behavior;
  `/e reload` is Core-only and resyncs identity/theme snapshots on success.
- Addon config reloads are domain-specific and uneven. Worlds restores prior
  config on failure; Groups, NPCs, Quests, and Portals mostly assign after
  successful loads; Economy, Government, and Offerings have partial-apply
  risks; Underworld and Mounts fall back to defaults on malformed config.
- `AddonConfigFiles` only writes defaults. Core now has a read-only
  `ElarionConfigRegistry` exposed through `ElarionApi.system().configs()`;
  `core`, `groups`, `economy`, `worlds`, `portals`, `offerings`, and
  `government`, `npcs`, `quests`, `realms`, `mounts`, `underworld`, and
  `optimization` are registered domains.
- Admin Panel rows/actions are bounded and server-authoritative, but the model
  is operational-card oriented. It now displays read-only config summaries in
  a dedicated Config tab with domain/category/entry rows and a preview-only
  entry validation action, but not typed controls or multi-field config editing
  without a richer contract.
- Slice 11 decided that true editing must use dedicated Core-owned config edit
  models/packets rather than expanding generic Admin Panel actions. Descriptor
  visibility does not imply editability; editable domains must opt in later
  with reload-safe appliers/edit providers.
- Slice 12 added config edit target/control models and open/request/result
  payload codecs only. No config edit payload receivers are registered yet.
- Slice 13 decided the next safe implementation is payload type registration
  only: config edit open/result as S2C and config edit request as C2S. Receiver
  registration and UI remain deferred.
- Slice 14 registered the three config edit payload types in `ElarionCoreMod`.
- Slice 15 decided the first receiver should be validation-only, delegated to
  Core Admin/config dispatch, with `APPLY` returning unsupported until
  editable-domain appliers exist.
- Slice 16 registered the C2S receiver for `ElarionConfigEditRequestPayload`
  and delegated validation to `ElarionAdminPanelService`. The server now sends
  `ElarionConfigEditResultPayload` and refreshes the Admin Panel Config tab
  message. `APPLY` is still unsupported, all results have `canApply=false`, and
  there is still no client edit UI.
- Slice 17 decided to use a lightweight client receiver/cache before building
  typed edit UI.
- Slice 18 registered the client receiver for `ElarionConfigEditResultPayload`
  and stores the last structured result in `ElarionConfigEditClientState`,
  clearing it on join/disconnect.
- Slice 19 decided that config edit detail should be a dedicated state opened
  from a Config entry row by a server-authored `ElarionConfigEditOpenPayload`,
  not another generic text-input modal.
- Slice 20 registered the client receiver for `ElarionConfigEditOpenPayload`
  and stores the current server-authored `ElarionConfigEditControl` in
  `ElarionConfigEditClientState`. Opening a new control clears stale
  validation result state.
- Slice 21 decided to reuse the existing OP-gated Admin Panel action path for
  opening config edit details.
- Slice 22 added `Open Editor` on Config entry rows and server-side resolution
  of `config-entry|domain|category|entry` into a disabled
  `ElarionConfigEditOpenPayload`.
- Slice 23 decided the first rendered config edit detail should be a
  read-only modal/detail shell over `ElarionAdminPanelScreen`, with Close and
  disabled Validate/Apply affordances only.
- Slice 24 rendered that read-only shell and added close-only open-control
  clearing.
- Slice 25 decided that the next validation-capable shell should use local
  proposed-value input state, send `ElarionConfigEditRequestPayload` with
  `VALIDATE`, render only target-matched results, and keep Apply disabled.
- Slice 26 implemented that validation-capable shell.
- Slice 27 established that descriptor visibility and successful validation do
  not imply editability; editable domains must explicitly register safe
  appliers with validation re-checks, persistence ownership, rollback,
  audit/history events, and post-apply sync behavior.
- The first production applier now covers only
  `core:ui_theme:defaults.font-scale-percent`. Live QA verified apply from
  `100` to `125`, UI reflow/theme sync, restore to `100`, and disabled Apply
  for a non-applier entry.

Phase 2 Slice 1 implementation:

- Added read-only descriptor contracts under `platform/core/.../config`:
  registry, domain, category, entry, codec, validator, and permission.
- Registered a `core` domain through `CoreConfigDescriptors`, covering selected
  `ui_theme.yml` and `server_identity.yml` values from the current
  `CoreConfigManager` snapshot.
- Exposed the registry through `ElarionApi.system().configs()`.
- Deferred mutation, Admin Panel rendering, addon migration, reload
  orchestration, packets, and persistence.

Phase 2 Slice 2 implementation:

- Added `GroupConfigDescriptors` under `addons/groups/.../config`.
- Registered the `groups` domain from `ElarionGroupsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `GroupService.config()` and do not
  parse `groups.yml` during discovery.
- Deferred Admin Panel rendering, config writes, reload orchestration, packets,
  and persistence.

Phase 3 Slice 1 implementation:

- Added read-only config-domain rows to the Admin Panel Systems tab through
  `ElarionAdminPanelService`.
- Superseded by Phase 3 Slice 3: config-domain rows now live in a dedicated
  Config tab.
- Rows are built from the current registered config descriptors and show domain
  owner, source files, reload command, categories, entries, current/default
  values, bounds, choices, and reload/restart markers.
- Reused the existing row/detail packet model. No config actions, typed edit
  controls, reload orchestration, packet schema changes, file format changes,
  or persistence changes were added.

Phase 2 Slice 3 implementation:

- Added `EconomyConfigDescriptors` under `addons/economy/.../config`.
- Registered the `economy` domain from `ElarionEconomyAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `EconomyTransactionService.config()`
  and `EconomyPricingService.definitions()` and do not parse config files
  during discovery.
- The domain covers persistence, query, Governor, and service-price metadata.
- Deferred Economy reload atomicity fixes, config writes, packet schema
  changes, file format changes, and persistence changes.

Phase 2 Slice 4 implementation:

- Added `WorldsConfigDescriptors` under `addons/worlds/.../config`.
- Registered the `worlds` domain from `ElarionWorldsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `WorldsConfigManager` and do not parse
  config files during discovery.
- The domain covers schema, lobby routing, current world keys/counts, and
  per-world identity/type/rule summaries.
- Deferred world storage changes, managed-world behavior changes, reload
  semantic changes, config writes, packet schema changes, file format changes,
  and persistence changes.

Phase 2 Slice 5 implementation:

- Added `PortalConfigDescriptors` under `addons/portals/.../config`.
- Registered the `portals` domain from `ElarionPortalsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `PortalDefinitionService` route and
  UI snapshots and do not parse config files during discovery.
- The domain covers route IDs, modes, source/destination dimensions, Economy
  price keys, schedule settings, visual settings, and prompt UI sizing.
- Deferred Portal travel behavior changes, schedule evaluation changes, Economy
  price integration changes, config writes, packet schema changes, file format
  changes, and persistence changes.

Phase 2 Slice 6 implementation:

- Added `OfferingConfigDescriptors` under `addons/offerings/.../config`.
- Registered the `offerings` domain from `ElarionOfferingsAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `OfferingDefinitionService` project
  and UI snapshots and do not parse config files during discovery.
- The domain covers reserved society metadata, Shrine UI settings, project IDs,
  scopes, repeatability flags, requirement/milestone/level counts, and
  presentation fields.
- Deferred Offering donation/progression behavior changes, reward or Shrine
  block changes, reload semantic changes, config writes, packet schema changes,
  file format changes, and persistence changes.

Phase 2 Slice 7 implementation:

- Added `GovernmentConfigDescriptors` under `addons/government/.../config`.
- Registered the `government` domain from `ElarionGovernmentAddon` through
  `api.system().configs()`.
- Descriptors expose current values from `GovernmentDefinitionService` settings
  and form snapshots and do not parse config files during discovery.
- The domain covers authority cleanup timing, form IDs, display metadata,
  authority offices, office counts/holder limits, action groups, and
  transitions.
- Deferred Government voting, office, authority, form-loading, reload semantic
  changes, config writes, packet schema changes, file format changes, and
  persistence changes.

Phase 2 Slice 8 implementation:

- Added `NpcConfigDescriptors` under `addons/npcs/.../config`.
- Registered the `npcs` domain from `ElarionNpcsAddon` after the first
  successful server definition load.
- Descriptors expose supplier-backed NPC definitions, skin and portrait
  profiles, dialogue graph summaries, and dialogue UI settings without parsing
  files during Admin Panel discovery.
- Dynamic rows are fixed to IDs present at registration. Current values remain
  supplier-backed.
- Deferred NPC placement/runtime state, dialogue session, condition/action,
  reload semantic, config write, packet, file format, and persistence changes.
- Decimal NPC ranges remain read-only string descriptors until Core gains a
  decimal codec.

Phase 2 Slice 9 implementation:

- Added `QuestConfigDescriptors` under `addons/quests/.../config`.
- Registered the `quests` domain from `ElarionQuestsAddon` immediately after
  the existing validated definition load.
- Descriptors expose supplier-backed package identity, scope, root stage,
  version/tags, actor, variable, stage/edge, evidence, ending/Shrine projection,
  reusable condition/consequence, authoring, and metadata summaries.
- Dynamic questline rows are fixed to IDs present at registration. Current
  values remain backed by the atomic `QuestDefinitionService` snapshot.
- Deferred Quest runtime state, progression, action/condition, scheduled
  consequence, notification, reload semantic, config write, packet, file
  format, and persistence changes.

Phase 2 Slice 10 implementation:

- Added `RealmConfigDescriptors` under `addons/realms/.../config`.
- Registered the `realms` domain from `ElarionRealmsAddon` using the same
  loaded `RealmProtectionConfig` snapshot passed to the protection service.
- Added and reused `RealmProtectionConfig.defaults()` so loader fallback and
  descriptor defaults cannot drift.
- Descriptors expose shared worlds, OP bypass, explosion block protection,
  feedback cooldown, and extra mechanism/container block IDs.
- All entries are restart-required and non-runtime-reloadable. Protection
  behavior, config loading semantics, packets, file formats, and persistence
  were not changed.

Phase 2 Slice 11 implementation:

- Added `MountConfigDescriptors` under `addons/mounts/.../config`.
- Registered the `mounts` domain from `ElarionMountsAddon` using the same
  loaded `MountCollectionTextConfig` snapshot consumed by Collection rows.
- Descriptors expose a bounded mount count/ID summary and four presentation
  fields for every registered `ElarionMountType`.
- All entries are restart-required and non-runtime-reloadable. Collection
  rendering, mount runtime state, config loading, packets, file formats, and
  persistence were not changed.

Phase 2 Slice 12 implementation:

- Added `UnderworldConfigDescriptors` under `addons/underworld/.../config`.
- Registered the `underworld` domain from `ElarionUnderworldAddon` through
  `UnderworldService.config()`.
- Descriptors expose 33 settings across Underworld, corpse, PvP loot,
  combat-tag, and Soul Fracture categories.
- Suppliers follow `/e death reload`; Admin Panel discovery does not parse
  YAML. Decimal fields remain validated read-only strings.
- Death/corpse/PvP loot/combat-tag/Soul Fracture behavior, config loading,
  packets, files, and persistence were not changed.

Phase 2 Slice 13 implementation:

- Added `PerformanceConfigDescriptors` under `addons/optimization`.
- Registered the `optimization` domain with `platform:core` ownership because
  Core loads and owns task settings before addon initialization.
- Descriptors expose 16 host, task-budget, and monitoring settings from
  `ElarionTaskService.snapshot()` while excluding live counters and ignored
  compatibility metadata.
- All entries are restart-required and non-runtime-reloadable. Worker, queue,
  budget, monitoring, config loading, file, and runtime behavior were not
  changed.

Descriptor coverage status:

- Every addon config file currently parsed into an addon runtime model now has
  a registered read-only descriptor domain.
- The Core domain now exposes UI theme, server identity, Realm definitions,
  title definitions, title-progression definitions, reward definitions,
  citizen/activity, chat, identity/nickname, and history settings.
- Jail and Security generate YAML placeholders but do not parse runtime config;
  truthful domains require typed loaders first. Core abilities are also
  generated defaults rather than a typed runtime snapshot.

Phase 2 Slice 14 implementation:

- Expanded `CoreConfigDescriptors` with 41 scalar entries across citizens,
  chat, identity, and history categories.
- Added five Core scalar source files to the domain metadata.
- Current values remain supplier-backed through existing `CoreConfigManager`
  getters and update after successful `/e reload` operations.
- Core load/reload behavior, files, commands, persistence, and runtime behavior
  were not changed.

Phase 2 Slice 15 implementation:

- Added `realms.yml` to the existing `core` domain metadata.
- Added Core Realm count/ID summary descriptors and per-Realm presentation,
  visibility, spawn, and flag descriptors backed by
  `CoreConfigManager.realms()`.
- Dynamic Realm rows are fixed to IDs present at descriptor registration;
  current values for those rows remain supplier-backed after successful
  `/e reload` operations.
- Decimal spawn values are represented as read-only string descriptors until
  Core gains a decimal config descriptor codec.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Realm runtime membership/governance, config loading, files, commands,
  persistence, and gameplay behavior were not changed.

Phase 2 Slice 16 implementation:

- Added `titles.yml` and `title-progression.yml` to the existing `core` domain
  metadata.
- Added Core title count/ID summary descriptors and per-title presentation,
  priority, visibility, acquisition, ownership, ability, and active-effect
  summaries backed by `CoreConfigManager.titles()`.
- Added title progression region and unlock-rule summaries backed by
  `CoreConfigManager.progressionRegions()` and
  `CoreConfigManager.titleUnlockRules()`.
- Dynamic title, region, and rule rows are fixed to IDs present at descriptor
  registration; current values for those rows remain supplier-backed after
  successful `/e reload` operations.
- Decimal progression-region coordinates are represented as read-only string
  descriptors until Core gains a decimal config descriptor codec.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Title ownership/activation, ability ownership, config loading, files,
  commands, persistence, and gameplay behavior were not changed.

Phase 2 Slice 17 implementation:

- Added `rewards.yml` to the existing `core` domain metadata.
- Added Core reward count/ID summary descriptors plus per-reward action count,
  ordered action type, action type, and action parameter descriptors backed by
  `CoreConfigManager.rewards()`.
- Dynamic reward rows are fixed to IDs and action indexes present at descriptor
  registration; current values for those rows remain supplier-backed after
  successful `/e reload` operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Reward execution, claim state, config loading, files, commands, persistence,
  packets, and gameplay behavior were not changed.

Phase 3 Slice 2 implementation:

- Completed as a documentation/proposal slice.
- Inspected `ElarionAdminPanelService`, Admin Panel snapshot/tab/row records,
  `ElarionAdminPanelScreen`, and `ElarionAdminPanelServiceTest`.
- Decision: next implementation should add a dedicated read-only `configs`
  Admin Panel tab using the existing snapshot/tab/row packet model.
- Config-domain rows should move out of Systems into the new Config tab.
- The current fixed-width tab layout must be adjusted so six tabs fit inside
  the panel.
- Page/category provider contracts, config writes, typed editing, reload
  orchestration, packet schema changes, persistence, commands, and gameplay
  behavior remain deferred.

Phase 3 Slice 3 implementation:

- Added a dedicated `configs` Admin Panel tab using the existing
  snapshot/tab/row packet model.
- Moved config-domain rows out of Systems; Systems now remains provider-owned
  testing and repair rows.
- Adjusted Admin Panel tab layout/hitboxes to compute width from tab count so
  six tabs fit inside the existing panel.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

Phase 3 Slice 4 implementation:

- Completed as a documentation/proposal slice.
- Inspected current Admin Panel row/body/payload limits.
- Decision: do not add a new Admin Panel packet schema or page/category
  provider contract yet.
- Next implementation should add domain summary rows plus per-category
  read-only rows in the existing Config tab using the current snapshot/tab/row
  packet model.
- True typed controls, config writes, reload orchestration, packet schema
  changes, persistence, commands, and gameplay behavior remain deferred.

Phase 3 Slice 5 implementation:

- Expanded `ElarionAdminPanelService.configRows(...)` from one row per domain
  to one domain summary row plus one category detail row per category.
- Domain rows summarize owner, files, reload command, category count, entry
  count, reloadable/restart-required/invalid counts, and category summaries.
- Category rows use stable `config:<domain>:category:<category>` IDs and keep
  entry details scoped to one category to reduce body truncation.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

## Phase 3 Slice 6 Handoff

Objective:

- Audit Admin Panel config mutation readiness without changing production
  behavior.

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigDomain.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelActionPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- Current reload/load findings in `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions:

- Do not enable config editing yet.
- Do not add Admin Panel edit actions to Config rows yet.
- Do not use generic provider actions as the long-term config mutation API.
- Add typed Core config change request/result/error records before any write
  support.

Verified facts:

- `ElarionConfigEntry` exposes validators, current/default display values,
  bounds, reload/restart markers, read permission, and write permission
  metadata.
- `ElarionConfigRegistry` is read-only discovery and has no apply/write path.
- `AdminPanelActionPayload` carries generic string parameters; it has no
  config-entry type, validation-result shape, old/new value reporting, reload
  policy, or audit hook.
- Config tab rows remain server-authored read-only rows.
- Addon reload/apply behavior is not uniform enough for global editing.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests:

- Not run; this was a documentation/proposal slice.

Unresolved risks:

- Future mutation contracts become public Core API, so fields should stay
  intentionally small and stable.
- Addon-specific file writes and reload rollback remain unimplemented and must
  be approved separately.

Completed follow-up:

- Phase 3, Slice 7: Core config mutation contract records, no writes.

## Phase 3 Slice 7 Handoff

Objective:

- Add Core config mutation contract records without enabling config writes.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigChangeContractTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionConfigChangeRequest` normalizes domain/category/entry IDs, preserves
  raw submitted values for future typed parsing, carries optional
  expected-current value, actor UUID, and reason, and exposes a stable target
  key.
- `ElarionConfigChangeError` defines stable error codes for unknown targets,
  permission denial, stale values, parse/validation failures, unsupported
  edits, reload/restart gating, apply failures, and internal failures.
- `ElarionConfigChangeResult` models `VALIDATED`, `APPLIED`, and `REJECTED`
  states with old/new display values, reload/restart flags, audit event type,
  and immutable error lists.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeContractTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- File writes.
- Apply services.
- Reload orchestration and rollback.
- Config mutation packets.
- Admin Panel edit actions.
- Audit-history emission.
- Addon-specific editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 8: Core config mutation validation service, no writes.

## Phase 3 Slice 8 Handoff

Objective:

- Add a pure Core config mutation validation service without enabling config
  writes.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigChangeValidatorTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionConfigChangeValidator.validate(registry, request, actorPermission)`
  resolves domain/category/entry IDs against the descriptor registry.
- Null actor permission is treated as `PUBLIC`.
- Write permission metadata is checked before parsing.
- Nonblank `expectedCurrentValue` is compared to the descriptor current display
  value to detect stale requests.
- Proposed raw values are parsed through the entry codec and then checked with
  the entry validator.
- Successful checks return `ElarionConfigChangeResult.validated` with old/new
  display values and reload/restart flags.
- Unknown targets, permission denial, stale values, parse failures, validation
  failures, current-value supplier failures, and validator failures return
  structured rejected results.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeValidatorTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- File writes.
- Config apply services.
- Reload orchestration and rollback.
- Config mutation packets.
- Admin Panel edit controls.
- Audit-history emission.
- Addon-specific editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 9: Admin Panel config validation preview action proposal.

## Phase 3 Slice 9 Handoff

Objective:

- Audit whether Admin Panel config validation preview should reuse the existing
  generic action payload or require a dedicated config packet/model before UI
  work.

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelActionPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/AdminPanelPayloadTest.java`

Decisions:

- Do not add a dedicated packet/model yet for read-only validation preview.
- Do not add validation actions to current category rows.
- Do not add edit/apply buttons.
- Reuse the existing generic Admin Panel action payload only for a narrow
  preview-only `Validate Value` action after Config gains stable entry-level
  rows.
- Add a dedicated config packet/model later before true editing, typed
  controls, diff display, audit preview, or apply/reload orchestration.

Verified facts:

- The generic action payload can carry a selected tab, provider, target row,
  action id, one or more string parameters, and confirmation state.
- The current screen supports one text input per action.
- Current Config rows are domain/category rows only; a category body can
  describe many entries and cannot safely identify one target entry.
- The server reopens the panel with a short message after actions, which is
  acceptable for validation preview but not rich editing state.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests:

- Not run; this was a documentation/proposal slice.

Completed follow-up:

- Phase 3, Slice 10: Config tab entry rows and validation preview action.
- Add stable read-only entry rows under each Config category and a preview-only
  `Validate Value` action per entry row. The action should call
  `ElarionConfigChangeValidator` server-side and return a short result
  message.
- Exclude file writes, actual edit/apply controls, reload orchestration,
  persistence, commands, gameplay behavior, and dedicated packet/model changes.

## Phase 3 Slice 10 Handoff

Objective:

- Add stable Config tab entry rows and a preview-only validation action.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionAdminPanelService.configRows(...)` now emits one stable entry row
  per descriptor entry after each category row.
- Entry rows use IDs shaped as
  `config-entry|<domain>|<category>|<entry>` and expose one setting's path,
  description, current/default display values, type, bounds, choices,
  permissions, runtime marker, and current validation state. The pipe-delimited
  internal form avoids ambiguity because descriptor IDs may contain colons.
- Each entry row has a preview-only `Validate Value` input action. The action
  routes through the existing server-authoritative Admin Panel action path,
  calls `ElarionConfigChangeValidator`, and returns a short valid/invalid
  message.
- The preview intentionally does not pass `expectedCurrentValue`, does not
  write files, does not apply values, does not reload config, does not emit
  audit/history events, and does not change runtime state.
- Config actions now reopen the Config tab and preserve the selected entry
  row.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Manual in-client UI verification.
- Dedicated config edit packet/model.
- Typed config controls.
- Diff display and audit preview.
- File writes, apply services, reload orchestration, rollback, audit emission,
  persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 11: dedicated config editing packet/model proposal.
- Classification: MEDIUM.
- Specify the packet/model shape required before true config editing: typed
  controls, expected-current values, validation result payloads, diff display,
  audit preview, reload/restart policy, apply permissions, and
  server-authoritative failure states.

## Phase 3 Slice 11 Handoff

Objective:

- Specify the dedicated packet/model contract required before true Admin Panel
  config editing.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelActionPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelSnapshot.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelTab.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`

Decisions:

- Do not evolve `AdminPanelActionPayload` into the long-term config editing
  protocol.
- Keep generic Admin Panel actions for provider operations and the current
  preview-only `Validate Value` action.
- Add a dedicated Core-owned config edit packet/model family before true
  editing.
- The future protocol must carry edit targets, typed control metadata,
  expected-current display values, proposed raw values, structured result
  errors, reload/restart policy, audit preview text, and server-authored
  failure states.
- Descriptor visibility does not imply editability. Each editable domain must
  later register an explicit reload-safe applier or edit provider.
- The first implementation should add records/codecs/tests only, with no
  receiver registration and no UI behavior.

Tests:

- Not run; this was a documentation/proposal slice.
- Lightweight verification was limited to targeted source inspection and
  Markdown search checks.

Deferred:

- Core edit packet/model Java records.
- Packet codec tests.
- Server receiver registration.
- UI controls.
- Config file writes.
- Apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 12: Core config edit packet/model records, no handlers.
- Classification: MEDIUM.
- Add Core model/network records and packet codecs for config edit targets,
  edit-control snapshots, edit requests, and edit results. Add focused codec
  round-trip and validation tests.
- Exclude server receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 12 Handoff

Objective:

- Add Core config edit packet/model records and codecs without registering
  handlers or enabling editing.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditTarget.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- `ElarionConfigEditTarget` normalizes domain/category/entry IDs and exposes a
  stable target key.
- `ElarionConfigEditControl` carries a server-authored typed control snapshot:
  target, label, description, path, value type, current/default display values,
  choices, bounds, reload/restart markers, read/write permissions, editable
  flag, and disabled reason.
- `ElarionConfigEditOpenPayload` carries one edit-control snapshot and a short
  message.
- `ElarionConfigEditRequestPayload` carries target, expected-current display
  value, proposed raw value, reason, and `VALIDATE`/`APPLY` intent.
- `ElarionConfigEditResultPayload` carries target, result status, old/new
  display values, reload/restart markers, `canApply`, audit preview,
  structured errors, and a short message.
- `ElarionConfigEditPayloadCodecs` centralizes bounded target/control/error
  serialization.

Tests:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Server receiver registration.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 13: config edit packet registration audit/proposal.
- Classification: MEDIUM.
- Decide where and how the new config edit payload IDs should be registered,
  how OP/admin permission checks should be enforced for validate/apply
  requests, what server service method should own dispatch, and how results
  should reopen or update the Config tab.
- Exclude UI controls, file writes, apply services, reload orchestration,
  persistence, commands, and gameplay behavior.

## Phase 3 Slice 13 Handoff

Objective:

- Audit and specify registration/dispatch for the new config edit packet
  contracts before adding live handlers.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions:

- Config edit payload codecs should be registered in `ElarionCoreMod`, where
  Core registers its current play payload types.
- `ElarionConfigEditOpenPayload` is S2C.
- `ElarionConfigEditResultPayload` is S2C.
- `ElarionConfigEditRequestPayload` is C2S.
- The next implementation should register payload types only. Do not add
  server/client receivers in that slice.
- Future request handling should be OP-gated and delegated to a Core
  Admin/config service method, not implemented directly inside the receiver
  lambda.
- Future `APPLY` requests must return unsupported/domain-not-editable until a
  separately approved editable-domain applier registry exists.

Tests:

- Not run; this was a documentation/proposal slice.
- Lightweight verification was limited to targeted source inspection and
  Markdown search checks.

Deferred:

- Payload type registration implementation.
- Server receiver registration.
- Client receiver registration.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Completed follow-up:

- Phase 3, Slice 14: register config edit payload types only.
- Classification: SMALL.
- Add `PayloadTypeRegistry.playS2C()` registrations for
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload`, plus
  `PayloadTypeRegistry.playC2S()` registration for
  `ElarionConfigEditRequestPayload`.
- Exclude server/client receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 14 Handoff

Objective:

- Register config edit payload types only.

Files changed:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation:

- Registered `ElarionConfigEditOpenPayload` through
  `PayloadTypeRegistry.playS2C()`.
- Registered `ElarionConfigEditResultPayload` through
  `PayloadTypeRegistry.playS2C()`.
- Registered `ElarionConfigEditRequestPayload` through
  `PayloadTypeRegistry.playC2S()`.
- No `ServerPlayNetworking.registerGlobalReceiver(...)` or
  `ClientPlayNetworking.registerGlobalReceiver(...)` calls were added for
  these payloads.

Tests:

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

Completed follow-up:

- Phase 3, Slice 15: config edit receiver/dispatch proposal.
- Classification: MEDIUM.
- Specify the minimal server receiver and service dispatch shape for
  validation-only requests: OP gating, target resolution, conversion to
  `ElarionConfigChangeRequest`, result payload conversion, and Config tab
  refresh behavior.
- Exclude implementation, UI controls, file writes, apply services, reload
  orchestration, persistence, commands, and gameplay behavior.

## Phase 3 Slice 15 Handoff

Objective:

- Specify the minimal receiver and service dispatch shape for validation-only
  config edit requests.

Files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/config.md`
- `docs/addons/core.md`

Decisions:

- The first live config edit receiver should support validation only.
- The receiver should delegate to `ElarionAdminPanelService` or a narrow Core
  config edit coordinator.
- Receiver lambdas must not parse config, mutate state, or construct detailed
  validation behavior directly.
- `VALIDATE` should convert `ElarionConfigEditRequestPayload` into
  `ElarionConfigChangeRequest`, run `ElarionConfigChangeValidator`, and return
  `ElarionConfigEditResultPayload`.
- `APPLY` must return a rejected `UNSUPPORTED` result until an editable-domain
  applier registry is approved and implemented.
- Non-OP players must receive `PERMISSION_DENIED` without target-existence
  details.
- `canApply` remains false for every result until apply support exists.
- Validation preview must not emit audit/history events.

Tests:

- Not run; this was a documentation/proposal slice.
- Lightweight verification was limited to targeted source inspection and
  Markdown search checks.

Deferred:

- Validation-only receiver implementation.
- Result conversion helpers.
- Service tests for valid, invalid, stale, permission-denied, and
  apply-unsupported cases.
- UI edit controls.
- Config file writes.
- Config apply services.
- Reload orchestration and rollback.
- Audit-history emission.
- Addon editable-domain opt-in.
- Persistence, commands, and gameplay behavior.

Next recommended slice:

- Phase 3, Slice 16: validation-only config edit receiver and dispatch.
- Classification: MEDIUM.
- Add the server receiver for `ElarionConfigEditRequestPayload`, a
  validation-only dispatch method on `ElarionAdminPanelService`, result payload
  conversion helpers, and focused service tests for valid, invalid,
  permission-denied, stale, and apply-unsupported cases.
- Exclude UI controls, file writes, apply services, reload orchestration,
  persistence, commands, and gameplay behavior.

## Phase 3 Slice 16 Handoff

Slice name:

- Validation-only config edit receiver and dispatch.

Objective:

- Add the first live server handling path for dedicated config edit requests,
  limited to validation preview only.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditTarget.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- Keep dispatch inside `ElarionAdminPanelService` instead of placing validation
  logic in the networking receiver.
- Use the existing `ElarionConfigChangeValidator` for packet-backed validation
  so generic row preview and future edit UI share one validation path.
- `APPLY` remains closed and returns `UNSUPPORTED` until editable-domain
  appliers, audit logging, and reload orchestration exist.
- All result payloads report `canApply=false` in this slice.
- The receiver refreshes the Admin Panel Config tab with the result message
  because no dedicated edit screen exists yet.

Exact files changed:

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

APIs and packets affected:

- Added `ElarionAdminPanelService.validateConfigEdit`.
- Added package-visible `ElarionAdminPanelService.configEditResult` for focused
  service testing.
- Registered the live C2S receiver for `ElarionConfigEditRequestPayload`.
- Existing payload record shapes and codecs were not changed.

Config and persistence impact:

- No config keys were added or changed.
- No descriptor domains changed.
- No config files are written.
- No runtime config snapshots are mutated.
- No persistent formats changed.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- `ElarionConfigEditResultPayload` is sent by the server, but no dedicated
  client receiver or edit UI consumes it yet. The Admin Panel refresh message
  is the current visible feedback.
- True edit/apply support still needs explicit domain appliers, reload-safety
  rules, rollback behavior, and admin audit logging.

Questions requiring user input:

- None for the completed validation-only receiver.

Work deliberately deferred:

- Client result receiver.
- Typed edit controls.
- Dedicated config edit detail state.
- Config writes/apply services.
- Reload orchestration and rollback.
- Audit/history entries for applied admin changes.
- Editable-domain provider registration.

Precise recommended next slice:

- Phase 3, Slice 17: config edit client result handling proposal.
- Classification: SMALL.
- Decide whether the client should use a no-op receiver, lightweight screen
  cache, Admin Panel message-only flow, or dedicated edit detail state for
  `ElarionConfigEditResultPayload` before implementing real edit controls.
- Search terms: `ElarionConfigEditResultPayload`,
  `ClientPlayNetworking.registerGlobalReceiver`, `AdminPanelScreen`,
  `AdminPanelOpenPayload`, `ElarionAdminPanelService.validateConfigEdit`.

## Phase 3 Slice 17 Handoff

Slice name:

- Config edit client result handling proposal.

Objective:

- Decide the minimum client-side handling required for structured config edit
  results before adding real edit controls.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Add a lightweight client receiver/state cache as the next implementation
  slice.
- Keep current visible feedback in the server-refreshed Admin Panel message.
- Defer dedicated edit/detail UI until the open/detail UX is specified.

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- Later Slice 18 documentation updates also touched `PLAN.md`, `TODO.md`,
  `docs/config.md`, `docs/addons/core.md`, and this handoff.

Tests run:

- None for the proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 18: config edit client result receiver/cache.

## Phase 3 Slice 18 Handoff

Slice name:

- Config edit client result receiver/cache.

Objective:

- Consume `ElarionConfigEditResultPayload` on the client without adding edit UI
  or any config write path.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Store the last structured result in passive client state for future UI use.
- Clear the cached result on join and disconnect.
- Do not render the result yet; the existing Admin Panel message remains the
  visible feedback.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- Added client-local `ElarionConfigEditClientState`.
- Registered client receiver for `ElarionConfigEditResultPayload`.
- No payload record shapes or codecs changed.

Config and persistence impact:

- No config keys were added or changed.
- No descriptors changed.
- No config files are written.
- No persistent formats changed.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- There is still no typed config edit UI.
- The client cache is passive and currently only prepares for future UI state.
- `APPLY` remains unsupported server-side and must stay disabled until
  editable-domain appliers, reload safety, rollback, and audit logging exist.

Work deliberately deferred:

- Config edit open/detail UI.
- Typed controls.
- Result rendering.
- Apply/write services.
- Reload orchestration.
- Admin audit/history entries.
- Editable-domain provider registration.

Precise recommended next slice:

- Phase 3, Slice 19: config edit open/detail UX proposal.
- Classification: SMALL.
- Decide how an admin opens a typed edit/detail view from a Config entry row
  and how that UI should present current value, proposed value, validation
  errors, old/new diff, reload/restart policy, and disabled Apply state.
- Search terms: `ElarionAdminPanelScreen`, `ElarionConfigEditOpenPayload`,
  `ElarionConfigEditControl`, `ElarionConfigEditClientState`,
  `config-entry|`, `VALIDATE_CONFIG_VALUE_ACTION`.

## Phase 3 Slice 19 Handoff

Slice name:

- Config edit open/detail UX proposal.

Objective:

- Define how a future typed config edit/detail view should open and what it
  must display before adding UI code.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`

Decisions made:

- Config edit detail should be a dedicated state opened from a Config entry
  row.
- The generic Admin Panel action path can request opening the selected row
  because it carries no proposed value and does not mutate state.
- The server must resolve the target and send `ElarionConfigEditOpenPayload`;
  the client must not build edit controls from row body text.
- The future detail state should show label, description, path, current/default
  values, type, choices, bounds, reload/restart policy, read/write
  permissions, proposed value input, validation errors, old/new diff,
  Validate, and disabled Apply with disabled reason.
- `Apply` remains unavailable until editable-domain appliers exist and the
  server returns `canApply=true`.

Rejected options:

- Keep using the one-field generic modal as the long-term config edit UX.
- Add config writes before the open/detail state exists.
- Let the client infer editable metadata from visible row text.
- Add a dedicated C2S open request payload before the generic Admin Panel
  action path proves insufficient.

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- None. Proposal only.

Config and persistence impact:

- No config keys changed.
- No descriptors changed.
- No config files or persistence changed.

Tests run:

- None. Proposal-only slice.

Unresolved risks:

- No rendered edit/detail UI exists yet.
- No server open action exists yet.
- No config write/apply path exists yet.

Work deliberately deferred:

- Client open payload receiver/cache.
- Server open action.
- Admin Panel row action.
- Rendered edit/detail UI.
- Validation input controls.
- Apply services, reload orchestration, rollback, and audit logging.

Precise recommended next slice:

- Phase 3, Slice 20: config edit open payload receiver and client open-state
  cache.
- Classification: SMALL.
- Extend `ElarionConfigEditClientState` to store the current open control,
  register a client receiver for `ElarionConfigEditOpenPayload`, clear open
  state on join/disconnect, and add focused cache tests.
- Exclude server open actions, Admin Panel row actions, rendered edit UI,
  validation input controls, config writes, apply services, reload
  orchestration, descriptor changes, and addon appliers.
- Search terms: `ElarionConfigEditClientState`,
  `ElarionConfigEditOpenPayload`, `ElarionCoreClient`,
  `ElarionConfigEditControl`.

## Phase 3 Slice 20 Handoff

Slice name:

- Config edit open payload receiver and client open-state cache.

Objective:

- Consume `ElarionConfigEditOpenPayload` on the client by storing the current
  server-authored edit control.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`

Decisions made:

- Store the open control alongside the last validation result in passive client
  state.
- Opening a new control clears `lastResult` so future UI does not display stale
  validation output for the wrong target.
- Reuse existing join/disconnect `clear()` behavior to clear both result and
  open-control state.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- Added client-local `open(ElarionConfigEditControl)` and `openControl()` on
  `ElarionConfigEditClientState`.
- Registered client receiver for `ElarionConfigEditOpenPayload`.
- No payload shapes or codecs changed.

Config and persistence impact:

- No config keys changed.
- No descriptors changed.
- No config files or persistence changed.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No server action sends `ElarionConfigEditOpenPayload` yet.
- No rendered edit/detail UI exists yet.
- `APPLY` remains unsupported.

Work deliberately deferred:

- Server open action.
- Admin Panel row action.
- Rendered edit/detail UI.
- Validation input controls.
- Config writes, apply services, reload orchestration, rollback, and audit
  logging.

Precise recommended next slice:

- Phase 3, Slice 21: config edit server open action proposal.
- Classification: SMALL.
- Decide the narrow server-side action that opens `ElarionConfigEditOpenPayload`
  from a selected Config entry row, including OP gating, target parsing,
  unknown-target fallback, disabled edit reason, selected-row refresh behavior,
  and tests.
- Search terms: `ElarionAdminPanelService`, `configEntryRowId`,
  `ConfigEntryTarget`, `ElarionConfigEditOpenPayload`,
  `ElarionConfigEditControl.fromEntry`, `coreAction`.

## Phase 3 Slice 21 Handoff

Slice name:

- Config edit server open action proposal.

Objective:

- Decide the server-side action path for opening a config edit detail from a
  Config entry row.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelRow.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- Add a Core-owned `Open Editor` action to config entry rows.
- Reuse `AdminPanelActionPayload`; do not add another C2S open request yet.
- Server parses row IDs, resolves descriptors from `ElarionConfigRegistry`, and
  sends `ElarionConfigEditOpenPayload` only for valid targets.
- Send controls with `editable=false` until appliers exist.

Tests run:

- None. Proposal-only slice.

## Phase 3 Slice 22 Handoff

Slice name:

- Config edit server open action and Config row action.

Objective:

- Expose `Open Editor` on Config entry rows and send server-authored disabled
  edit controls for valid entry targets.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation facts:

- Added `OPEN_CONFIG_EDITOR_ACTION`.
- Config entry rows now include `Open Editor` and `Validate Value`.
- Added `configEditOpenControl` resolver.
- Successful open sends `ElarionConfigEditOpenPayload`.
- Unknown/stale targets fail with messages and do not send open payloads.
- Controls remain `editable=false` with
  `Config editing is not enabled yet.`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`

Work deliberately deferred:

- Rendered edit UI.
- Text input.
- Validation submission from the edit detail.
- Apply/write services.
- Reload orchestration, rollback, and audit logging.

## Phase 3 Slice 23 Handoff

Slice name:

- Config edit read-only detail shell proposal.

Objective:

- Define the first rendered edit detail shell before adding UI code.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Render a presentation-only modal/detail shell over `ElarionAdminPanelScreen`.
- Source data from `ElarionConfigEditClientState.openControl()`.
- Include Close and disabled/non-interactive Validate/Apply affordances.
- Do not send validation requests or accept input in the first render slice.

Precise recommended next slice:

- Phase 3, Slice 24: config edit read-only detail shell.
- Classification: MEDIUM.
- Render the read-only shell, add Close behavior, keep Validate/Apply disabled,
  and add focused helper tests where practical.
- Search terms: `ElarionAdminPanelScreen`, `renderModal`,
  `ElarionConfigEditClientState.openControl`, `ElarionConfigEditControl`,
  `ElarionUiRenderer.compactButton`.

## Phase 3 Slice 24 Handoff

Slice name:

- Config edit read-only detail shell.

Objective:

- Render the current server-authored config edit control in the Admin Panel
  without adding input, validation submission, Apply, or writes.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`

Exact files changed:

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

- Added read-only shell rendering in `ElarionAdminPanelScreen`.
- Shell displays label, description, path, current/default values, type,
  bounds, choices, runtime policy, permissions, disabled reason, Close, and
  disabled Validate/Apply affordances.
- Shell consumes mouse/typing/scroll while open.
- Escape closes the shell before closing the Admin Panel.
- Added `ElarionConfigEditClientState.closeOpenControl()`.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Work deliberately deferred:

- Proposed-value text input.
- Validation submission from the detail shell.
- Structured result rendering.
- Apply/write services.
- Reload orchestration and rollback.
- Admin audit/history entries.
- Editable-domain appliers.

Precise recommended next slice:

- Phase 3, Slice 25: config edit validation input proposal.
- Classification: SMALL.
- Decide proposed-value input state, focus/keyboard behavior, Validate button
  enablement, C2S validation request construction, stale-result clearing, and
  result display boundaries.
- Search terms: `ElarionAdminPanelScreen`, `ElarionTextInput`,
  `ElarionConfigEditRequestPayload`, `ElarionConfigEditClientState`,
  `renderConfigEditShell`, `handleConfigEditShellClick`.

## Phase 3 Slice 25 Handoff

Slice name:

- Config edit validation input proposal.

Objective:

- Define how the read-only config edit shell should send validation requests
  and display validation results without enabling Apply.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextInput.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`

Decisions made:

- Proposed value input should be local to `ElarionAdminPanelScreen`.
- Input should seed from `ElarionConfigEditControl.currentDisplayValue()` when
  a control opens.
- Input should clear/discard when the open control closes or changes.
- Enter and Validate should send a `VALIDATE` request.
- The request should include target, expected current display value from the
  server-authored control, proposed raw value, reason
  `admin-panel-config-edit-preview`, and intent `VALIDATE`.
- Result display should only use `lastResult()` when its target matches the
  open control target.
- Any local input change after a result should clear or mark stale displayed
  result state.
- Apply remains disabled/non-interactive.

Rejected options:

- Reuse the generic `Validate Value` modal inside the edit shell.
- Enable Apply after successful validation.
- Store proposed-value input in global client state.
- Trust client-side parsing/validation for anything beyond simple blank or
  unchanged convenience states.

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

APIs and packets affected:

- None. Proposal-only slice.

Tests run:

- None. Proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 26: config edit validation input and request.
- Classification: MEDIUM.
- Add proposed-value input state to `ElarionAdminPanelScreen`, focus and
  keyboard handling while the shell is open, Validate button enablement,
  `ElarionConfigEditRequestPayload` send with `VALIDATE`, and target-matched
  result rendering from `ElarionConfigEditClientState.lastResult()`.
- Exclude Apply, config writes, reload orchestration, descriptor changes, and
  addon appliers.
- Search terms: `ElarionAdminPanelScreen`, `ElarionTextInput`,
  `ElarionConfigEditRequestPayload`, `ElarionConfigEditClientState.update`,
  `ElarionConfigEditClientState.lastResult`, `renderConfigEditShell`.

## Phase 3 Slice 26 Handoff

Slice name:

- Config edit validation input and request.

Objective:

- Let admins validate a proposed config value from the config edit shell while
  keeping Apply and all write paths disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionTextInput.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`

Exact files changed:

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

- Proposed-value input is local to `ElarionAdminPanelScreen`.
- Input initializes from the open control current display value.
- Typing/Backspace edit the proposed value and clear stale validation results.
- Enter and Validate send `ElarionConfigEditRequestPayload` with intent
  `VALIDATE`.
- Result display uses only target-matched `ElarionConfigEditResultPayload`
  values from `ElarionConfigEditClientState.lastResult()`.
- Apply remains visible but disabled.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Work deliberately deferred:

- Apply enablement.
- Config writes.
- Reload orchestration and rollback.
- Admin audit/history entries.
- Editable-domain appliers.
- Descriptor changes.

Precise recommended next slice:

- Phase 3, Slice 27: config edit apply/readiness proposal.
- Classification: SMALL.
- Audit what must exist before Apply can ever become enabled, including
  editable-domain applier contracts, reload safety, rollback, audit/history
  entries, permissions, stale expected-current handling, persistence, and
  tests.
- Search terms: `ElarionConfigChangeResult`, `canApply`,
  `ElarionConfigChangeRequest`, `ElarionConfigChangeValidator`,
  `ElarionConfigEditRequestPayload.Intent.APPLY`,
  `ElarionConfigRegistry`, `CoreConfigManager.reload`.

## Phase 3 Slice 27 Handoff

Slice name:

- Config edit Apply/readiness proposal.

Objective:

- Audit and document what must exist before the config edit shell can enable
  `Apply`.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/event/ElarionDomainEvent.java`
- `platform/core/src/main/java/panetina/elarion/core/event/ElarionEventBus.java`
- `platform/core/src/main/java/panetina/elarion/core/service/HistoryService.java`

Exact files changed:

- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions made:

- Do not enable `Apply` yet.
- Add a Core-owned apply/applier contract before any config entry can become
  editable.
- Keep apply/write behavior separate from descriptors and validation.
- Explicit domain appliers must own file mutation, runtime reload/update,
  rollback/failure recovery, audit metadata, and post-apply synchronization.
- `Apply` must re-run server-side validation immediately before writing and
  must reject stale expected-current values.
- Validation previews must not emit audit/history records; successful applies
  must emit an administration domain event and history record.
- Sensitive config values need a redaction/sensitivity flag before they can be
  safely edited or audited through Admin Panel.

Tests run:

- No Java tests were run; this was a documentation/proposal slice only.
- Documentation whitespace/diff checks were run after the slice.

Work deliberately deferred:

- Core apply contract records/registry.
- File writes.
- Reload orchestration and rollback.
- UI Apply enablement.
- Descriptor changes.
- Addon/domain appliers.

Unresolved risks:

- No applier registry exists yet.
- No domain can safely persist Admin Panel config edits yet.
- No rollback, temp-write, backup, or cross-domain reload orchestration exists.
- `ElarionConfigChangeResult.applied(...)` exists as a model helper but is not
  used by production code.
- Addon config reload safety remains uneven and must be solved per domain
  before writes are enabled.

Precise recommended next slice:

- Phase 3, Slice 28: Core config apply contract records/registry, no writers.
- Classification: MEDIUM.
- Add the Core apply contract types and focused tests for registration,
  duplicate/missing applier behavior, and apply-readiness lookup.
- Exclude file writes, reload orchestration, UI Apply enablement, descriptor
  changes, and addon/domain appliers.
- Search terms: `ElarionConfigChangeResult`, `ElarionConfigChangeRequest`,
  `ElarionConfigChangeValidator`, `ElarionConfigEntry`, `canApply`,
  `ElarionAdminPanelService.dispatchConfigEditRequest`.

## Phase 3 Slice 28 Handoff

Status: completed on 2026-07-06.

Objective:

- Add Core config apply contract records and a registration/readiness registry
  without implementing writers or enabling Apply.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigDomain.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigCategory.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEntry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditTarget.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeRequest.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeResult.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeError.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- Existing focused config contract and registry tests.

Exact production files added:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplier.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCapability.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyContext.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyReadiness.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`

Test file added:

- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyRegistryTest.java`

Documentation files changed:

- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions and contract behavior:

- Apply ownership remains separate from read-only descriptors and pure
  validation.
- Registrations bind one normalized `ElarionConfigEditTarget` to immutable
  capability metadata and one applier.
- Enabled capabilities must declare audit event type and affected files;
  reload/restart support is explicit. Disabled capabilities carry a reason.
- Descriptor-aware readiness returns structured failures for unknown targets,
  missing/disabled registrations, and unsupported reload/restart policies.
- Ready results must declare audit and affected-file metadata and cannot carry
  errors; blocked results must carry at least one error.
- `ElarionConfigApplyContext` verifies request IDs match the resolved domain,
  category, and entry.

APIs/config/persistence affected:

- New Core Java contract types were added under the config package.
- No existing public facade exposes the registry yet.
- No config keys, files, packet formats, or persistent formats changed.
- No applier is registered or called in production.

Tests run:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Unresolved risks and deliberately deferred work:

- Registry lifetime/API exposure and registration timing are undecided.
- Atomic validate/write/reload execution, per-domain serialization, backups,
  rollback, audit/history, and client resync are not implemented.
- Admin Panel `canApply` remains false and the client does not send `APPLY`.
- No Core/addon domain appliers exist.
- No sensitive-value descriptor/redaction contract exists.

Questions requiring user input:

- None for this completed contract-only slice.

Precise recommended next slice:

- Phase 3, Slice 29: config apply registry ownership and readiness wiring
  proposal.
- Classification: SMALL.
- Inspect `ElarionApi`, `ElarionSystemApi`, Core initialization,
  `ElarionAdminPanelService.configEditOpenControl`, config edit dispatch, and
  addon descriptor registration timing. Decide registry ownership, exposure,
  and readiness consumption while Apply remains disabled.
- Exclude implementation, writers, reload/rollback, audit emission, UI Apply
  enablement, descriptor changes, and domain appliers.
- Search terms: `ElarionSystemApi`, `configs()`, `configEditOpenControl`,
  `dispatchConfigEditRequest`, `canApply`, `registerDomain`.

## Phase 3 Slice 29 Handoff

Status: completed on 2026-07-06.

Objective:

- Produce the ownership and wiring design for the inert apply registry without
  modifying production code.

Files inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/AddonInitializationOrder.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- Core Admin/apply registry focused tests.
- Representative descriptor registration in Government, Economy, and NPCs.

Decisions:

- Core constructs and owns one canonical process-lifetime apply registry beside
  the descriptor registry before addon initialization.
- Addons receive only a registration facade through `ElarionSystemApi`. The
  facade must wrap or method-reference the registry so consumers cannot cast it
  back to the concrete registry and retrieve executable appliers.
- Admin receives readiness without executable lookup. A future Core apply
  coordinator alone may resolve and invoke an applier.
- Register each applier immediately after its matching descriptor and validated
  service/snapshot exist. Deferred domains such as NPCs register both once after
  the first successful definition load.
- Readiness may later improve server-authored disabled reasons, but `editable`
  and `canApply` stay false until full apply orchestration exists.
- A future `canApply=true` requires validation success, current readiness,
  authorization, and coordinator availability; Apply must revalidate instead of
  trusting a preview.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

APIs/config/persistence affected:

- None. This slice changed documentation only.

Tests run:

- No Java tests were run; this was a proposal-only slice.

Unresolved risks and deliberately deferred work:

- The registration-only interface/facade does not exist yet.
- Core does not yet construct a canonical apply registry.
- Admin does not consume readiness and still uses a generic disabled reason.
- No coordinator, serialization, writer, backup, rollback, audit, or resync path
  exists.
- No production appliers are registered.

Questions requiring user input:

- None for this proposal slice.

Precise recommended next slice:

- Phase 3, Slice 30: canonical apply registry ownership and registration-only
  Core API.
- Classification: MEDIUM.
- Expected production files: a new registrar contract under Core config,
  `ElarionCoreMod`, `ElarionApi`, and `ElarionSystemApi`; focused Core tests and
  affected config/Core API docs.
- Compatibility: additive API only; no config/save/packet/command behavior.
- Verification: focused registrar/registry tests followed by full Core tests.
- Exclude Admin readiness integration, production/domain registrations,
  applier execution, file writes, reload/rollback, audit emission, and UI Apply
  enablement.
- Search terms: `ElarionConfigApplyRegistry`, `ElarionSystemApi`,
  `ElarionCoreMod configRegistry`, `new ElarionApi`, `registerDomain`.

## Phase 3 Slice 30 Handoff

Status: completed on 2026-07-06.

Objective:

- Establish canonical Core apply-registry ownership and safe registration-only
  addon access without registering or executing any production applier.

Production files changed:

- Added
  `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistrar.java`.
- Updated `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`.
- Updated `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`.
- Updated
  `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`.

Test file changed:

- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyRegistryTest.java`.

Documentation files changed:

- `docs/api.md`
- `docs/config.md`
- `docs/addons/core.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation decisions:

- `ElarionConfigApplyRegistrar` declares only `register(target, capability,
  applier)`.
- Core creates one canonical concrete registry beside the descriptor registry
  before API construction and addon initialization.
- The API stores a method-reference facade (`configApplyRegistry::register`),
  not the concrete registry typed as the registrar. Consumers cannot cast the
  returned object back to `ElarionConfigApplyRegistry` to obtain executable
  registrations.
- `ElarionSystemApi.configAppliers()` is the addon registration extension point.
- No applier is registered in this slice.

API/config/persistence impact:

- Additive public Java API: `ElarionConfigApplyRegistrar` and
  `ElarionSystemApi.configAppliers()`.
- No config keys, descriptors, files, packet formats, commands, or persisted
  formats changed.
- No runtime apply behavior changed.

Tests run:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Unresolved risks and deferred work:

- The canonical registry has no readiness-only Admin view yet.
- No production registrations or apply coordinator exist.
- Admin still uses a generic disabled reason, reports `canApply=false`, and
  rejects `APPLY`.
- File mutation, reload/rollback, audit/history, and post-apply synchronization
  remain unimplemented.

Questions requiring user input:

- None for this completed slice.

Precise recommended next slice:

- Phase 3, Slice 31: non-executable Admin config readiness integration.
- Classification: MEDIUM.
- Add a readiness-only functional contract/view backed by
  `target -> configApplyRegistry.readiness(configRegistry, target)` and inject it
  into Admin config open/validation helpers.
- Use it only to report precise target-specific disabled reasons. Preserve
  `editable=false`, `canApply=false`, server-side `APPLY` rejection, and the
  disabled/non-interactive client Apply button.
- Add focused tests for missing, disabled, reload-unsafe, restart-unsafe, and
  ready-but-execution-disabled targets, then run full Core tests.
- Exclude production/domain registrations, executable lookup/invocation, file
  writes, reload/rollback, audit emission, and UI Apply enablement.
- Search terms: `configEditOpenControl`, `configEditResult`,
  `CONFIG_EDIT_DISABLED_REASON`, `ElarionConfigApplyReadiness`, `canApply`.

## Phase 3 Slice 31 Handoff

Status: completed on 2026-07-06.

Production files changed:

- Added
  `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyReadinessProvider.java`.
- Updated `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`.
- Updated
  `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`.

Test file changed:

- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`.

Behavior and decisions:

- Core injects a readiness-only method reference into Admin; executable lookup
  remains unavailable there.
- Open controls and successful validation messages report target-specific
  readiness failures.
- A ready registration still reports that execution is disabled.
- `editable=false`, `canApply=false`, server `APPLY` rejection, and the client
  button remain unchanged.

Tests run:

- Passed `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`.
- Passed `.\gradlew.bat :platform:core:test`.

Deferred:

- Coordinator/execution contract, production registrations, file writes,
  reload/rollback, audit/history, synchronization, and UI Apply enablement.

Precise next approved slice:

- Phase 3, Slice 32: Core config apply coordinator contract audit.
- Classification: SMALL.
- Inspect apply/change contracts, Admin audit emission, event/history handling,
  and config reload guarantees. Produce the bounded Slice 33 implementation
  contract without modifying production code.
- Search terms: `ElarionConfigApplier`, `ElarionConfigChangeResult.applied`,
  `ElarionDomainEvent`, `emit`, `HistoryService`, `ConfigValidationException`.

## Phase 3 Slice 32 Handoff

Status: completed on 2026-07-06.

Files inspected:

- `ElarionConfigApplier`, `ElarionConfigApplyContext`,
  `ElarionConfigApplyCapability`, `ElarionConfigApplyRegistry`.
- `ElarionConfigChangeValidator`, `ElarionConfigChangeResult`, and error codes.
- `ElarionAdminPanelService` administration event/history emission.
- `ElarionDomainEvent`, `ElarionEventBus`, and `HistoryService.record`.

Decisions:

- Do not implement coordinator execution against the immediate callback API.
- Add a prepare/commit/rollback transaction boundary first.
- Future coordinator uses one global mutation lock initially; it revalidates,
  rechecks readiness, and resolves trusted descriptors under that lock.
- Preparation is non-mutating. Commit performs owner mutation. Rollback is
  idempotent and must restore prior file/runtime state when later coordinator
  steps fail.
- Applied results must preserve validated reload/restart policy and registered
  audit type.

Files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No additional Java tests; Slice 32 was documentation/audit only. Slice 31's
  focused and full Core tests were already green immediately before this audit.

Precise next approved slice:

- Phase 3, Slice 33: transactional config applier contract.
- Classification: MEDIUM.
- Add `ElarionConfigPreparedChange` (or convention-matching equivalent) with
  `commit()` and idempotent `rollback()`. Change `ElarionConfigApplier` to
  `prepare(context)`. Add an applied-result factory preserving reload/restart
  flags and update apply registry tests.
- No coordinator, production registrations, Admin Apply, file writes, reload,
  audit, packets, persistence, or UI changes.

## Phase 3 Slice 33 Handoff

Status: completed on 2026-07-06.

Production files changed:

- Added
  `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`.
- Updated `ElarionConfigApplier.java` to prepare transactions.
- Updated `ElarionConfigChangeResult.java` with runtime-policy-preserving applied
  results and applied audit-type validation.

Tests changed:

- Added `ElarionConfigPreparedChangeTest.java`.
- Updated `ElarionConfigApplyRegistryTest.java`.
- Updated `ElarionConfigChangeContractTest.java`.
- Updated `ElarionAdminPanelServiceTest.java`.

Behavior and invariants:

- `prepare(context)` is the owner extension point and must not mutate state.
- Prepared changes commit at most once and may roll back after success/failure;
  the standard helper invokes rollback at most once.
- `APPLIED` results require an audit event type and preserve reload/restart
  flags when supplied.
- No code invokes a prepared transaction in production.

Tests run:

- Focused prepared-change, change-contract, apply-registry, and Admin tests.
- Full `.\gradlew.bat :platform:core:test` passed.
- Final focused prepared-change/change-contract rerun passed after API cleanup.

Deferred:

- Coordinator, production registrations, Admin/network/UI Apply wiring, real
  files, reload/rollback implementations, audit/history, and synchronization.

Precise recommended next slice:

- Phase 3, Slice 34: internal config apply coordinator implementation.
- Classification: MEDIUM.
- Add an unwired coordinator using the canonical descriptor/apply registries, a
  global `ReentrantLock`, and a mandatory audit sink. Revalidate/readiness-check
  under lock, resolve trusted context, prepare/commit, validate returned result,
  audit, and roll back on commit/audit failure.
- Add focused tests for rejection without preparation, stale recheck, missing
  registration, prepare/commit exceptions, invalid owner result, successful
  audit, rollback after commit/audit failure, and lock serialization.
- Exclude production registrations, Admin/network/UI wiring, real file writes,
  reload orchestration, and client synchronization.

## Phase 3 Slice 34 Handoff

Status: completed on 2026-07-06.

Production files added:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditRecord.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`

Test file added:

- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`

Behavior:

- One fair static lock serializes all coordinator instances.
- Validation, stale detection, readiness, and trusted descriptor resolution run
  under that lock.
- Owner preparation is non-mutating; commit results are accepted only when all
  trusted fields match validation and capability metadata.
- Structured audit is mandatory after trusted commit.
- Commit/result/audit failure rolls back. Rollback failure is retained in the
  returned `APPLY_FAILED` message.

Tests run:

- Passed focused `ElarionConfigApplyCoordinatorTest` before and after final lock
  and failure-coverage corrections.
- Passed full `.\gradlew.bat :platform:core:test`.

Deferred:

- Production coordinator construction/ownership.
- Core history/domain-event audit adapter.
- Production appliers, real file writers, reload orchestration, Admin/network/UI
  Apply wiring, and synchronization.

Precise recommended next slice:

- Phase 3, Slice 35: coordinator ownership and audit-adapter proposal.
- Classification: SMALL.
- Inspect Core construction order, `HistoryService.bind/record`, non-throwing
  `ElarionEventBus.emitDomainEvent`, Admin config request dispatch, and current
  audit metadata conventions. Decide ownership and safe history-first/event-last
  adapter ordering before implementation.
- Exclude implementation, production registrations, Apply enablement, files,
  reload orchestration, packets, UI, and synchronization.
- Search terms: `new ElarionConfigApplyCoordinator`, `HistoryService.record`,
  `emitDomainEvent`, `validateConfigEdit`, `APPLY`, `canApply`.

## Phase 3 Slice 35 Handoff

Status: completed on 2026-07-06.

Inspected:

- Core construction and `SERVER_STARTED` binding order.
- `HistoryService.bind`, policy-filtered `record`, and queued storage/index
  append behavior.
- Non-throwing domain-event listener dispatch.
- Admin config request dispatch and current unconditional `APPLY` rejection.
- Coordinator audit record/sink and rollback behavior.

Decisions:

- General History cannot be the mandatory config audit authority because it is
  filterable and queued.
- Add a dedicated write-ahead config audit journal under
  `world/elarion/core/audit/` before constructing the production coordinator.
- Audit uses PREPARED before commit and COMMITTED after commit; terminal failure
  phases preserve rollback/recovery evidence.
- History and domain events are post-COMMITTED projections and do not control
  rollback.
- Coordinator execution is unavailable until the audit journal is bound.
- Admin eventually receives only an execution facade.
- Secret-like entries remain non-editable until sensitivity/redaction metadata
  exists.

Files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; Slice 35 was proposal-only. Slice 34 focused and full Core tests
  were green immediately before this audit.

Precise recommended next slice:

- Phase 3, Slice 36: write-ahead config audit session contracts.
- Classification: MEDIUM.
- Evolve `ElarionConfigApplyAuditSink` into `prepare(record)` returning a session
  with committed/rolledBack/failed terminal operations. Update the unwired
  coordinator and focused tests so projections are no longer modeled as the
  mandatory audit step.
- Exclude storage/files, production coordinator ownership, appliers,
  Admin/network/UI wiring, reload, and synchronization.
- Search terms: `ElarionConfigApplyAuditSink`, `prepare(record)`,
  `ElarionConfigApplyCoordinator`, `rollback`, `ElarionConfigApplyCoordinatorTest`.

## Audit Scope

- Read all Markdown files currently tracked or present in the repository.
- Scanned project source outside `addons/angling/**`.
- Kept `addons/angling/reference/**` as upstream reference material only.
- Treated the current dirty worktree as current project reality; do not revert
  uncommitted files unless explicitly asked.

Current non-Angling source footprint from this pass:

- `platform/core`: 244 code/build/resource files, including 240 Java files and
  44 Java tests.
- Active addon/source modules: Economy, Government, Groups, Mounts, Names,
  NPCs, Offerings, Optimization, Portals, Quests, Realms, Security, Titles,
  Underworld, Worlds.
- Shell or early integration modules: Jail, Newspapers, Tablist,
  Voice Chat Hooks.
- `tests/gametest`: command and integration GameTest support.
- `dev`: aggregate Fabric development runtime.

## Current Build Shape

Fabric 1.21.1 multi-module Gradle project:

- Root: `build.gradle`, `settings.gradle`, `gradle.properties`.
- Core: `platform/core`.
- Addons: `addons/*`, loaded through the custom `elarion:addon` entrypoint.
- Dev runtime: `dev`.
- GameTests: `tests/gametest`.

Primary commands:

```text
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runGameTests
.\gradlew.bat exportMods
.\gradlew.bat rebuildExportMods
.\gradlew.bat :platform:core:test
.\gradlew.bat :addons:<addon>:test
```

Use focused compile/test tasks first, then full `build` when a change crosses
modules or before handoff.
Use `exportMods` to copy deployable Core/addon remapped jars into
`build/export/mods`; use `rebuildExportMods` when the export folder should be
cleared first. The export task automatically includes `:platform:core` and
all current/future `:addons:*` projects, excluding `dev` and `tests`.

## Current Source Truth

Core owns canonical truth and shared infrastructure:

- citizens, identity, Realms, titles, abilities, rewards, history, public
  history, permissions, server identity, shared UI primitives, task queues,
  character lifecycle, True Death reset orchestration, notifications, and
  domain events
- config defaults and validation patterns under `platform/core/.../config`
- runtime world storage helpers under `platform/core/.../storage`
- networking examples under `platform/core/.../network`
- command registration under `platform/core/.../command`

Addons own only feature-specific behavior and runtime state:

- Economy: currency, wallets, treasuries, transactions, pricing, Economy pulse,
  reward/NPC action registrations.
- Offerings: Shrine of Foundation, Offering projects, instances, anchors,
  donations, milestones, Shrine UI.
- Government: Civic Forum, Seat of Rule, forms, founding votes, offices,
  authority chat, proposals, laws/civic records, gate/status UI.
- Groups: public groups, tags, invites, membership, group chat,
  Confederation delegate hooks.
- NPCs: static NPC entity, placement storage, dialogue definitions/sessions,
  prompt handling, skins, portraits, NPC UI.
- Quests: data-driven questline definitions, scoped shared/player quest state,
  registered quest actions/conditions, scheduled consequences, Quest
  notifications, and Shrine display-name projections through Offerings.
- Portals: linked A/B routes, portal fields, tickets, schedules, route windows,
  return entitlements, travel confirmation UI.
- Worlds: managed worlds, per-world borders, spawn protection, abundance rules.
- Realms: Realm protection behavior that consumes Core Realm truth.
- Names/Titles: presentation hooks for identity, titles, tablist/nameplates.
- Optimization: diagnostics and queue/world trend foundations.
- Security: evidence/status foundation.
- Underworld: death capture, component-safe graves/corpses, recovery vaults,
  nickname-labeled tombs, modular item source metadata, Underworld sessions,
  Soul Fractures, True Death handoff to Core.
- Mounts: native Fabric flying mount entity, Core Collection menu tab with
  top-tab Mounts/Pets/Titles layout, persistent unlock/active-mount state,
  whistle placeholder collection icons, config-driven collection text,
  client-only Collection model previews through Core's generic preview
  registry and direct entity-dispatch preview utility, bounds-aware preview
  scale/offset from converted geo bounds with Collection-only art calibration
  for long/tiny mounts, seven legacy whistle icon ids, GeckoLib geo rendering,
  mount input, camera/rider presentation, session recovery.
- Angling: active fishing foundation, but exclude `addons/angling/reference/**`
  unless explicitly resuming Angling porting.

## Current Runtime And Config Rules

- Editable definitions live under `config/elarion/`.
- Mutable runtime state lives under `world/elarion/`.
- Growing history-like features must expose bounded APIs, indexes, or archive
  summaries before becoming player-facing.
- Do not make players, commands, GUIs, web bridges, Chronicle, newspapers,
  ledgers, or rumors scan raw JSONL or broad world state.

Important state locations:

- Core citizens/realms/rewards/history/notifications/characters:
  `world/elarion/core/` and `world/elarion/history*`.
- Economy: `world/elarion/addon-state/economy/`.
- Offerings: `world/elarion/addon-state/offerings/`.
- Government: `world/elarion/addon-state/government/`.
- Groups: `world/elarion/addon-state/groups/`.
- NPC placements: `world/elarion/addon-state/npcs/placed-npcs.json`.
- Quests: `world/elarion/addon-state/quests/state.json`.
- Portals: `world/elarion/addon-state/portals/`.
- Underworld: `world/elarion/addon-state/underworld/`.
- Mount sessions: `world/elarion/addon-state/mounts/sessions.json`.
- Mount collection state: `world/elarion/addon-state/mounts/collection.json`.

## Current Verification Status

High-priority manual verification still lives in `TODO.md`. Current major
pending areas:

- Government civic-record V1 in-game flows and broader GameTest coverage.
- Shrine/Government reset command manual verification before the next
  progression pass.
- Tablist Realm header rows at varied GUI scales/player counts.
- Underworld V1 death, corpse, vault, restart, Soul Fracture, and True Death
  flows.
- Character Lifecycle migration, new-character creation, cooldown, and
  restart-safe cleanup.
- Collection UI manual check after the paused 2026-06-29 pass: verify selected
  unlocked mounts render in the right preview frame after the bounds-aware
  preview pass, row icon frames have no black corner pixels, top tabs show only
  Mounts, Pets, and Titles, and Titles can set active titles through the
  existing Core title state.

Known focused automated coverage exists for Core storage/services/network
payloads, Economy, Government models/services/storage, Groups, NPCs, Offerings,
Portals, Realms, Worlds, Optimization, Mounts, Angling, and GameTest command
basics. Full in-game coverage is intentionally still incomplete.

## Documentation Map

Start here:

1. `RULES.md`
2. `AGENTS.md`
3. `CODEX.md`
4. `INDEX.md`
5. `docs/ai/CURRENT_STATUS.md`
6. Relevant `docs/systems/*.md`
7. Relevant `docs/addons/*.md`
8. Relevant `wiki/admin/*.md`

Use:

- `TODO.md` for current implementation work.
- `PLAN.md` for short project memory/read order.
- `PLANS.md` for future design directions.
- `OPTIMIZATION_TRACKER.md` for performance/health risks.
- `docs/architecture/PROJECT_STRUCTURE.md` for module ownership.
- `docs/architecture/DEPENDENCY_GRAPH.md` for separation rules.
- `docs/architecture/KNOWLEDGE_MAP.md` for navigation.
- `docs/ai/AI_SEARCH_HINTS.md` for targeted source lookup.

## Hard Rules For Future Agents

- Search before adding infrastructure.
- Extend Core APIs/events/registries instead of direct cross-addon state reads.
- Keep runtime state server-authoritative.
- Keep config reload-safe and validated.
- Add or update tests when persistence, commands, networking, permissions, or
  behavior changes.
- Update docs in the same pass when ownership, command, config, API, packet,
  UI, permission, event, notification, or addon status changes.
- If docs and source disagree, inspect source and update docs before building
  on stale text.

## Latest Slice Handoff - Phase 3, Slice 36

Slice name:

- Write-ahead config audit session contracts.

Objective:

- Replace the coordinator's one-shot audit sink with a pre-commit audit session
  contract that can record committed, rolled-back, or failed terminal outcomes.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions made:

- Mandatory config apply audit is modeled as
  `ElarionConfigApplyAuditSink.prepare(record)`.
- `prepare(record)` returns `ElarionConfigApplyAuditSession`.
- The session terminal methods are `committed()`, `rolledBack(failure)`, and
  `failed(failure)`.
- The coordinator prepares audit after non-mutating owner preparation but
  before commit.
- Audit preparation failure prevents commit and rolls back prepared owner
  resources.
- Commit, trusted-result validation, or committed-terminal failure triggers
  rollback and a rolled-back audit terminal outcome.
- Rollback failure triggers the failed audit terminal outcome.
- The path remains internal and unwired; no production apply execution exists.

Exact files changed:

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

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks at the end of Slice 36:

- No durable audit journal existed yet. This was resolved by Slice 37's
  unbound journal; production binding is still unresolved.
- No production coordinator/executor owns a server/world path.
- No domain appliers are registered.
- Admin/network/UI still cannot apply config changes.
- Secret/sensitive config entries still require sensitivity and redaction
  metadata before editability.

Precise recommended next slice:

- Phase 3, Slice 37: durable config audit journal storage.
- Classification: MEDIUM persistence slice.
- Implement an unbound journal audit sink with versioned JSONL records for
  PREPARED, COMMITTED, ROLLED_BACK, and FAILED, synchronous append plus force,
  bounded unresolved-tail recovery, and temporary-directory tests.
- Exclude production binding, coordinator ownership, appliers, Admin/network/UI
  execution, config file writes, reload, and synchronization.

Search terms:

- `ElarionConfigApplyAuditSession`
- `ElarionConfigApplyAuditSink`
- `ElarionConfigApplyCoordinator`
- `committed()`
- `rolledBack`
- `failed`
- `CONFIG_ADMIN_AUDIT`

## Latest Slice Handoff - Phase 3, Slice 37

Slice name:

- Durable config audit journal storage.

Objective:

- Add the unbound JSONL write-ahead audit journal for future config apply
  execution.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalStorage.java`
- `platform/core/src/main/java/panetina/elarion/core/storage/CatchTelemetryJournalCodec.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditRecord.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSink.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditSession.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`

Decisions made:

- Config apply audit durability is owned by a dedicated Core journal, not
  general History.
- The journal is unbound: tests pass it an explicit path, and no production
  server/world owner constructs it yet.
- `ElarionConfigApplyAuditPhase` defines PREPARED, COMMITTED, ROLLED_BACK, and
  FAILED.
- `ElarionConfigApplyAuditJournal` implements `ElarionConfigApplyAuditSink`.
- Each journal line is versioned JSONL with one audit ID, phase, timestamp,
  full normalized audit record, and optional failure text.
- Append calls are synchronized and force the file before returning.
- Recovery is tail-bounded and reports `tailTruncated` so production binding
  can reject unsafe states later.

Exact files changed:

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

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No production service binds the journal to the active server/world path.
- No production coordinator/executor facade exists.
- No domain appliers are registered.
- Admin/network/UI still cannot apply config changes.
- Config file writes, reload, rollback around real files, synchronization, and
  sensitivity/redaction remain future work.

Precise recommended next slice:

- Phase 3, Slice 38: production config apply ownership proposal.
- Classification: SMALL proposal slice.
- Decide the Core-owned service/facade that binds descriptor registry, apply
  registry, coordinator, and durable audit journal to the active server/world
  path.
- Exclude domain appliers, Admin Apply wiring, config file writes, reload, and
  synchronization until the ownership boundary is approved.

Search terms:

- `ElarionConfigApplyAuditJournal`
- `ElarionConfigApplyAuditPhase`
- `recoverUnresolvedTail`
- `journalPath`
- `ElarionConfigApplyCoordinator`
- `config-changes.jsonl`

## Latest Slice Handoff - Phase 3, Slice 38

Slice name:

- Production config apply ownership proposal.

Objective:

- Decide the production owner/facade for config apply execution before wiring
  mutable Admin behavior.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionApi.java`
- `platform/core/src/main/java/panetina/elarion/core/api/ElarionSystemApi.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/storage/JsonStateStorage.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyAuditJournal.java`

Decisions made:

- Add a future Core-owned `ElarionConfigApplyService`.
- The service owns production construction and lifecycle for
  `ElarionConfigApplyCoordinator`.
- The service receives the canonical descriptor registry and concrete
  `ElarionConfigApplyRegistry` during Core initialization.
- The service binds on `SERVER_STARTED` using
  `JsonStateStorage.elarionRoot(server)` and
  `ElarionConfigApplyAuditJournal.journalPath(root)`.
- Apply execution stays unavailable while unbound, when journal recovery is
  unsafe, or when bounded recovery finds unresolved PREPARED audit records.
- `ElarionApi.system().configAppliers()` remains registration-only.
- Admin should receive only a narrow readiness/executor facade, not coordinator,
  concrete registry lookup, or journal mutation access.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Unresolved risks at the end of Slice 38:

- No `ElarionConfigApplyService` implementation existed yet. This was resolved
  by Slice 39's skeleton; backend execution remains unresolved.
- Admin/network/UI still cannot apply config changes.
- No domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 39: implement `ElarionConfigApplyService` skeleton.
- Classification: SMALL.
- Add the service class, bind it in `ElarionCoreMod`, route Admin readiness
  through the service, and add focused tests for unbound, bound, unresolved
  audit recovery, and readiness delegation.
- Exclude execute/apply wiring, domain appliers, Admin Apply enablement, config
  file writes, reload, and synchronization.

Search terms:

- `ElarionConfigApplyService`
- `ElarionConfigApplyCoordinator`
- `ElarionConfigApplyAuditJournal`
- `JsonStateStorage.elarionRoot`
- `bindConfigApplyReadiness`
- `SERVER_STARTED`

## Latest Slice Handoff - Phase 3, Slice 39

Slice name:

- Config apply service skeleton.

Objective:

- Add the Core-owned service skeleton that binds the durable audit journal and
  gates future config apply readiness.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyReadiness.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyRegistryTest.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`

Decisions made:

- `ElarionConfigApplyService` owns production config apply lifecycle.
- Core constructs it with the descriptor registry and concrete apply registry.
- Core binds it on `SERVER_STARTED` with `JsonStateStorage.elarionRoot(server)`.
- Core unbinds it on `SERVER_STOPPING`.
- Admin readiness now goes through the service.
- The service blocks otherwise-ready targets while unbound, when audit recovery
  is truncated, or when unresolved PREPARED records exist.
- No execution method or Admin Apply path was added.

Exact files changed:

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

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No backend execution method exists yet.
- No domain appliers are registered.
- Admin/network/UI still cannot apply config changes.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 40: backend config apply execution method.
- Classification: SMALL.
- Add `ElarionConfigApplyService.apply(request, actorPermission)` so it rejects
  while unbound/unsafe and delegates to the coordinator only when ready.
- Use fake test-only appliers; exclude production appliers, Admin/network/UI
  Apply wiring, config file writes, reload, and synchronization.

Search terms:

- `ElarionConfigApplyService`
- `executionReady`
- `recoverUnresolvedTail`
- `bindConfigApplyReadiness`
- `ElarionConfigApplyServiceTest`

## Latest Slice Handoff - Phase 3, Slice 40

Slice name:

- Backend config apply execution method.

Objective:

- Add backend-only apply delegation to `ElarionConfigApplyService` without
  enabling Admin Apply or production config writers.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigChangeValidator.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`

Decisions made:

- `ElarionConfigApplyService.apply(request, actorPermission)` is the backend
  execution method for future Admin Apply.
- It returns `UNSUPPORTED` while unbound or audit recovery is unsafe.
- It delegates to the coordinator only when the service is bound and safe.
- Tests use fake in-test appliers only.
- Admin/network/UI Apply remains disabled.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Admin/network/UI still cannot apply config changes.
- No production domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 41: Admin config apply executor boundary proposal.
- Classification: SMALL proposal slice.
- Decide the narrow interface Admin should use for apply execution, OP-level
  checks, packet response behavior, and the UI-disabled rollout boundary.
- Exclude implementation, production domain appliers, config file writes,
  reload, and synchronization.

Search terms:

- `ElarionConfigApplyService.apply`
- `ElarionConfigEditRequestPayload.Intent.APPLY`
- `validateConfigEdit`
- `configEditResult`
- `canApply`

## Latest Slice Handoff - Phase 3, Slice 41

Slice name:

- Admin config apply executor boundary proposal.

Objective:

- Decide the narrow boundary between Admin config edit handling and backend
  config apply execution.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`

Decisions made:

- Add a future `ElarionConfigApplyExecutor` contract.
- The executor should expose only readiness lookup and backend apply execution.
- `ElarionConfigApplyService` should implement it.
- `ElarionAdminPanelService` should bind the executor instead of only a
  readiness provider.
- The first executor-facade implementation should keep `Intent.APPLY` rejected
  and keep the client Apply button disabled.
- Server-side APPLY dispatch must be a separate slice with OP, stale-value,
  result-payload, and Admin refresh tests.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 42: add Admin config apply executor facade.
- Classification: SMALL.
- Add `ElarionConfigApplyExecutor`, have `ElarionConfigApplyService` implement
  it, and bind Admin to the executor while keeping APPLY rejected.
- Exclude server-side APPLY dispatch, production appliers, config file writes,
  reload, synchronization, and UI Apply enablement.

Search terms:

- `ElarionConfigApplyExecutor`
- `bindConfigApplyReadiness`
- `ElarionConfigApplyService`
- `ElarionAdminPanelService`
- `Intent.APPLY`

## Latest Slice Handoff - Phase 3, Slice 42

Slice name:

- Admin config apply executor facade.

Objective:

- Add the narrow Admin-facing executor facade while keeping server-side APPLY
  dispatch disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- `ElarionConfigApplyExecutor` is the Admin-facing facade.
- The facade exposes readiness and backend apply only.
- `ElarionConfigApplyService` implements the facade.
- `ElarionAdminPanelService` binds an executor through
  `bindConfigApplyExecutor(...)`.
- The readiness-only binding helper remains for focused tests and wraps
  readiness in an executor that rejects apply.
- `Intent.APPLY` still rejects before executor dispatch.
- The client Apply button remains disabled.

Exact files changed:

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

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Server-side APPLY dispatch is still disabled.
- No production domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 43: server-side Admin APPLY dispatch proposal.
- Classification: SMALL proposal slice.
- Decide exact request/result conversion from `ElarionConfigEditRequestPayload`
  APPLY to `ElarionConfigApplyExecutor.apply(...)`, OP checks, stale-value
  behavior, Admin refresh, and whether the client Apply button stays disabled
  until a production applier exists.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Search terms:

- `ElarionConfigApplyExecutor`
- `bindConfigApplyExecutor`
- `Intent.APPLY`
- `configEditResult`
- `validateConfigEdit`

## Latest Slice Handoff - Phase 3, Slice 43

Slice name:

- Server-side Admin APPLY dispatch proposal.

Objective:

- Decide how Admin config edit APPLY requests should route to the executor in a
  future implementation slice.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`

Decisions made:

- Keep the existing config edit packet schema; `Intent.APPLY` already exists.
- Keep the Core network receiver shape unchanged.
- Evolve Admin service request/result conversion internally.
- OP level 4 remains checked before validation or apply dispatch.
- VALIDATE keeps using `ElarionConfigChangeValidator.validate(...)`.
- APPLY should call `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Empty reason fallback should be `admin-panel-config-edit-preview` for
  VALIDATE and `admin-panel-config-edit-apply` for APPLY.
- APPLIED results should convert to `ElarionConfigEditResultPayload` with
  `status=APPLIED`, old/new values, runtime flags, no errors, `canApply=false`,
  an audit preview, and an `Applied:` message.
- Client Apply remains disabled until a later UI slice.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 44: implement server-side Admin APPLY dispatch.
- Classification: SMALL.
- Change only Admin service request/result conversion and focused tests.
- Keep client Apply disabled and use fake test executors only.
- Exclude production appliers, config file writes, reload, synchronization, and
  UI Apply enablement.

Search terms:

- `configEditResult`
- `validateConfigEdit`
- `ElarionConfigApplyExecutor.apply`
- `ElarionConfigEditRequestPayload.Intent.APPLY`
- `ElarionAdminPanelServiceTest`

## Latest Slice Handoff - Phase 3, Slice 44

Slice name:

- Server-side Admin APPLY dispatch.

Objective:

- Route server-side Admin config APPLY requests to `ElarionConfigApplyExecutor`
  while keeping the client Apply button disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- `configEditResult(...)` now handles both VALIDATE and APPLY.
- VALIDATE behavior remains unchanged.
- APPLY builds an `ElarionConfigChangeRequest` and dispatches to
  `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Empty APPLY reasons become `admin-panel-config-edit-apply`.
- APPLIED results convert to `ElarionConfigEditResultPayload` with status
  APPLIED, old/new values, runtime flags, no errors, `canApply=false`, audit
  preview, and an `Applied:` message.
- Rejected executor results preserve errors and return `Apply failed: ...`.
- Client Apply remains disabled and no production applier exists.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `docs/config.md`
- `docs/api.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Client Apply button is still disabled.
- No production domain appliers are registered.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 45: Admin Apply UI enablement proposal.
- Classification: SMALL proposal slice.
- Decide how server-authored edit controls should expose Apply availability,
  whether the client button remains disabled until a production applier exists,
  and what verification is required before making Apply clickable.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Search terms:

- `configEditApplyResult`
- `admin-panel-config-edit-apply`
- `ElarionConfigApplyExecutor.apply`
- `canApply`
- `ElarionAdminPanelScreen`

## Latest Slice Handoff - Phase 3, Slice 45

Slice name:

- Admin Apply UI enablement proposal.

Objective:

- Decide when and how the Admin config editor may make Apply clickable.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- Keep the client Apply button disabled until at least one production applier is
  approved and tested.
- Apply availability must be server-authored on the edit control.
- The current `editable` flag mixes input editability and apply execution, so it
  is too broad for the rollout.
- Future control metadata should separate input editability from apply
  availability.
- A future client Apply predicate must require an open control, nonblank input,
  latest matching VALIDATED result, matching current/proposed values,
  server-authored apply availability, and `result.canApply()`.
- The server must continue rejecting forged APPLY packets when execution is not
  safe.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only slice.

Precise recommended next slice:

- Phase 3, Slice 46: split config edit control input/apply state.
- Classification: SMALL.
- Add explicit apply availability metadata to the control/payload model and
  focused packet/client-state tests, but keep the Admin screen Apply button
  disabled.
- Exclude production appliers, config file writes, reload, synchronization, and
  clickable Apply UI.

Search terms:

- `ElarionConfigEditControl`
- `editable`
- `canApply`
- `sendConfigEditValidation`
- `ElarionConfigEditPayloadTest`
- `ElarionConfigEditClientStateTest`

## Latest Slice Handoff - Phase 3, Slice 46

Slice name:

- Split config edit control input/apply state.

Objective:

- Separate input editability from Apply availability in the server-authored
  config edit control while keeping Apply disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigEditControl.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/network/ElarionConfigEditPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`

Decisions made:

- `ElarionConfigEditControl` now carries `inputEditable`,
  `applyAvailable`, `disabledReason`, and `applyDisabledReason`.
- The old constructor remains as a compatibility path and maps `editable` to
  `inputEditable`, with `applyAvailable=false`.
- `editable()` remains as a compatibility alias for `inputEditable()`.
- The open-payload codec round-trips explicit apply availability state.
- The Admin screen can display `applyDisabledReason`, but Apply still renders
  and handles as disabled.
- No production applier, file write, reload, sync, or clickable Apply UI was
  added.

Exact files changed:

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

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- No production domain appliers are registered.
- Apply remains disabled in the Admin screen.
- Config file writes, reload, synchronization, and sensitivity/redaction remain
  future work.

Precise recommended next slice:

- Phase 3, Slice 47: production config applier selection proposal.
- Classification: SMALL proposal slice.
- Choose the first low-risk production config entry/domain to make writable and
  define file write, reload, rollback, audit, UI, and test requirements.
- Exclude implementation.

Search terms:

- `ElarionConfigEditControl`
- `inputEditable`
- `applyAvailable`
- `applyDisabledReason`
- `ElarionConfigEditPayloadCodecs`
- `ElarionAdminPanelScreen`

## Latest Slice Handoff - Phase 3, Slice 47

Slice name:

- Production config applier selection proposal.

Objective:

- Choose the first low-risk production config entry/domain to make writable and
  define file write, reload, rollback, audit, UI, and test requirements before
  implementation.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/command/ElarionCommands.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionUiThemeService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistry.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`

Decisions made:

- The first production writable target should be
  `core:ui_theme:defaults.font-scale-percent`.
- The target maps to `config/elarion/core/ui_theme.yml` at
  `defaults.font-scale-percent`.
- The value is a bounded integer from `100` to `150`, runtime-reloadable, not
  restart-required, and OPERATOR-gated.
- Future audit event type should be
  `core.config.ui_theme.font_scale_changed`.
- This target was chosen because it is one scalar value with existing descriptor
  coverage, validation, Core reload behavior, and UI theme sync.
- Realm spawn editing is deferred because it is a multi-field
  gameplay-affecting location transaction.
- Server identity, dynamic Realm/title/reward definition rows, addon domains,
  and restart-required settings remain deferred.
- No production applier, file write, reload, synchronization, UI click
  enablement, config behavior, or gameplay behavior was changed.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- No Java tests; this was a proposal-only documentation slice.

Unresolved risks:

- `ui_theme.yml` writer strategy must avoid partial writes on Windows.
- Generic YAML dumping may normalize formatting/comments; prefer a narrow
  scalar update helper or document normalization before implementation.
- Rollback must restore the exact previous file contents and active theme if
  post-write reload fails.
- The Admin Apply button is still disabled; click enablement remains a later
  slice unless separately approved.

Precise recommended next slice:

- Phase 3, Slice 48: Core UI theme font-scale applier backend.
- Classification: MEDIUM.
- Implement only backend applier registration, safe file mutation, Core reload,
  UI theme resync, rollback, audit-backed apply execution, and focused tests for
  `core:ui_theme:defaults.font-scale-percent`.
- Keep the Admin screen Apply button disabled unless separately approved for UI
  click enablement.

Search terms:

- `core:ui_theme:defaults.font-scale-percent`
- `CoreConfigDescriptors`
- `CoreConfigManager.load`
- `ElarionUiThemeService.syncAll`
- `ElarionConfigApplyRegistry`
- `ElarionConfigPreparedChange`

## Latest Slice Handoff - Phase 3, Slice 48

Slice name:

- Core UI theme font-scale applier backend.

Objective:

- Register the first production backend config applier for
  `core:ui_theme:defaults.font-scale-percent` without enabling the visible
  Admin Apply button.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDefaultFiles.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigDescriptors.java`
- `platform/core/src/main/java/panetina/elarion/core/config/CoreConfigManager.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCapability.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyCoordinator.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigApplyRegistrar.java`
- `platform/core/src/main/java/panetina/elarion/core/config/ElarionConfigPreparedChange.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionConfigApplyService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionUiThemeService.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionConfigApplyServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigApplyCoordinatorTest.java`
- `platform/core/src/test/java/panetina/elarion/core/config/ElarionConfigRegistryTest.java`

Decisions made:

- The only production backend target registered in this slice is
  `core:ui_theme:defaults.font-scale-percent`.
- The applier preserves the current file format by replacing exactly one
  `font-scale-percent:` scalar line instead of dumping the whole YAML map.
- Commit writes through a temp file plus replace, reloads `CoreConfigManager`,
  and invokes a supplied UI theme sync hook.
- Rollback restores exact prior file contents, reloads the previous valid Core
  config, and invokes the same sync hook.
- Core initialization registers the target with the existing canonical apply
  registry and tracks the active server only for post-apply UI theme sync.
- The visible Admin Apply button remains disabled. No client click path was
  enabled.

Exact files changed:

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

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- The Admin screen still cannot click Apply.
- Only the font-scale target has a production backend applier.
- Manual in-game verification is still needed for live UI reflow after theme
  sync at different Minecraft GUI scales.
- Realm spawn, server identity, addon config, dynamic definition rows, and
  restart-required config writes remain deferred.

Precise recommended next slice:

- Phase 3, Slice 49: Admin Apply click enablement for the single font-scale
  target.
- Classification: MEDIUM.
- Enable the client Apply button only when server-authored apply availability
  and a matching latest validation result allow it, and keep all other config
  targets disabled.
- Exclude adding any new production config appliers.

Search terms:

- `CoreUiThemeFontScaleConfigApplier`
- `core:ui_theme:defaults.font-scale-percent`
- `core.config.ui_theme.font_scale_changed`
- `ElarionConfigApplyService.apply`
- `sendConfigEditValidation`
- `canApply`

## Latest Slice Handoff - Phase 3, Slice 49

Slice name:

- Single-target Admin Apply click enablement.

Objective:

- Enable Admin Apply click behavior only for the server-authored ready
  `core:ui_theme:defaults.font-scale-percent` target and keep every other
  config target disabled.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditRequestPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditResultPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/network/ElarionConfigEditPayloadCodecs.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreMod.java`
- `platform/core/src/main/java/panetina/elarion/core/ElarionCoreClient.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionConfigEditClientState.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`

Decisions made:

- Config edit packet directions are valid in source:
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload` are S2C;
  `ElarionConfigEditRequestPayload` is C2S.
- Server-opened controls now derive `inputEditable` and `applyAvailable` from
  backend apply readiness.
- Validation results now return `canApply=true` only for a target with a ready
  apply backend.
- The Admin client sends `Intent.APPLY` only when the control is
  apply-available and the latest validation result exactly matches target,
  old/current value, and new/proposed value.
- Applied results close the edit shell to avoid stale current values after
  reload/sync.

Exact files changed:

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

Tests run:

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

Unresolved risks:

- Manual dev-client QA is still needed to confirm the panel does not disconnect
  with a custom payload error in live play.
- Only the font-scale target can be applied.
- Other config entries must remain disabled until their owner appliers are
  explicitly implemented and tested.

Precise recommended next slice:

- Phase 4, Slice 1: shared UI token/component audit and proposal.
- Classification: SMALL proposal slice.
- Inspect current Core UI primitives, Government/notification style, and
  duplicated literals to define the first UI-token consolidation target.
- Exclude implementation.

Search terms:

- `ElarionConfigEditOpenPayload`
- `ElarionConfigEditRequestPayload`
- `ElarionConfigEditResultPayload`
- `canApplyConfigEdit`
- `sendConfigEditApply`
- `CoreUiThemeFontScaleConfigApplier`

## Latest Slice Handoff - Phase 4, Slice 1

Slice name:

- Shared UI token/component audit and proposal.

Objective:

- Inspect current Core UI primitives, Government civic UI helpers, and
  Notification/Admin styling duplication to define the first safe Phase 4 UI
  consolidation target.

What was inspected:

- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiStyle.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiTypography.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiComponents.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentUiGlyphs.java`
- `addons/government/src/main/java/panetina/elarion/addons/government/client/GovernmentScreenChrome.java`

Decisions made:

- Core already owns base UI primitives and scaled typography; the next step is
  consolidation, not inventing a second UI framework.
- The civic brown/gold visual language should be represented by Core-owned
  generic helpers before migrating more screens.
- Government-specific semantic components should remain in the Government
  addon, but generic surfaces such as row boxes, action buttons, dividers,
  status chips, and attached shells belong in Core.
- The first implementation slice should be additive and should not migrate a
  complete screen.

Exact files changed:

- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `INDEX.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- Documentation sanity checks only:
  - `rg -n "UI_SYSTEM_AUDIT|Phase 4, Slice 2|ElarionCivic" PLAN.md TODO.md INDEX.md docs/systems/GUI.md docs/systems/UI_JOURNAL.md docs/reports/UI_SYSTEM_AUDIT.md docs/ai/CURRENT_STATUS.md`
  - `git diff --check -- PLAN.md TODO.md INDEX.md docs/systems/GUI.md docs/systems/UI_JOURNAL.md docs/reports/UI_SYSTEM_AUDIT.md docs/ai/CURRENT_STATUS.md`

Unresolved risks:

- Notification HUD and Admin Panel still have duplicated civic constants and
  row/action drawing logic.
- Government owns the most complete civic helpers today; those helpers should
  not become a cross-addon dependency.
- No live screenshot QA was run in this proposal slice.

Work deliberately deferred:

- Full Notification HUD migration.
- Full Admin Panel migration.
- Moving Government UI wholesale into Core.
- Dev-only UI gallery.
- NPC, Portal, Shrine, Collection, and character UI migrations.

Precise recommended next slice:

- Phase 4, Slice 2: Core civic UI token and primitive helper foundation.
- Classification: MEDIUM.
- Add additive Core helpers for civic colors, attached shell/frame, row
  surface, compact action button, divider, status chip, and font-scale-aware
  row/control metrics.
- Add focused metric tests.
- Exclude complete screen migrations and behavior changes.

Search terms:

- `ElarionUiStyle`
- `ElarionUiRenderer`
- `ElarionUiTypography`
- `GovernmentUiGlyphs`
- `GovernmentUiComponents`
- `GovernmentScreenChrome`
- `CIVIC_ROOT`
- `compactActionButton`
- `rowBox`

## Latest Slice Handoff - Phase 4, Slice 2

Slice name:

- Core civic UI token and primitive helper foundation.

Objective:

- Add additive Core helpers for the civic brown/gold visual language and
  font-scale-aware control metrics without migrating full screens or changing
  behavior.

What was inspected:

- `RULES.md`
- `CODEX.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiStyle.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiRenderer.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiTypography.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionUiTheme.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionUiTypographyTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`

Decisions made:

- Core now owns generic civic UI color tokens and primitive drawing helpers.
- `ElarionCivicColors` mirrors the current Government/Notification civic
  palette so future migrations do not visually drift.
- `ElarionCivicUi` is additive and generic: thin boxes, attached shells, row
  surfaces, compact action buttons, dividers, and status chips.
- `ElarionUiMetrics` owns font-scale-aware control/list sizing helpers that
  can be tested without a Minecraft client renderer.
- No existing screen was migrated in this slice to keep behavior stable.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiMetrics.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ui/ElarionUiMetricsTest.java`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Notification HUD still has local civic constants and duplicated row/action
  drawing until a caller migration slice adopts the new helpers.
- Admin Panel still has local danger colors, modal overlays, and action/row
  drawing logic.
- Government still owns the richest semantic civic components; generic pieces
  should move to Core gradually as screens are touched.
- No live screenshot QA was run because no existing rendered screen changed.

Work deliberately deferred:

- Full Notification HUD redesign.
- Full Admin Panel redesign.
- Civic Forum and Seat of Rule migration.
- Dev-only UI gallery.
- NPC, Portal, Shrine, Collection, and character UI migration.

Precise recommended next slice:

- Phase 4, Slice 3: migrate one small existing caller to the new Core civic UI
  helpers.
- Classification: MEDIUM.
- Recommended first target: Notification HUD row/action drawing, because it
  duplicates the same civic constants and remains the most visible rough UI.
- Keep the slice to helper adoption and small visual cleanup. Do not change
  notification packets, persistence, storage, category filtering, action
  semantics, or the whole drawer layout.

Search terms:

- `ElarionCivicColors`
- `ElarionCivicUi`
- `ElarionUiMetrics`
- `ElarionNotificationHud`

## Latest Slice Handoff - Phase 4, Slice 15

Slice name:

- First live server-backed UI screenshot pass.

What was verified:

- `runServer` plus `runClientOne` connected `ElarionAdmin` to saved
  `localhost` without a custom-payload crash.
- Realm notification empty state, Civic History, Create Civic Proposal,
  Shrine, and allowed Neutral Gate confirmation were opened through real
  server-authoritative flows and captured under `build/ui-qa/`.
- Civic's `870 x 519` fallback is bounded; the complete Civic layouts rendered
  after maximizing to `1936 x 1048`.
- Server shutdown completed successfully.

Findings:

- Populated Notification rows/actions/detail remain unverified.
- The Neutral Gate icon frame was empty.
- The Shrine reported completion while aggregate and requirement progress were
  all zero.
- Client logs contain existing model JSON warnings for unsupported rotations;
  no Elarion custom-payload decode failure was found.

State restoration:

- Neutral was relocked.
- Temporary Government and Shrine test blocks were replaced with grass.
- Player position was restored.
- Offering anchor coordinates were restored offline to their exact pre-test
  values `elarion:realm_world_1 @ 3,67,2` in both Offering runtime files.
- Client and server processes were closed.

Exact files changed:

- `PLAN.md`
- `TODO.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- Runtime restoration only: `dev/run/world/elarion/addon-state/offerings/state.json`
  and `anchors.json` were returned to their pre-test coordinates.

Recommended next slice:

- Seed Realm notifications through existing domain events and capture populated
  row/action/detail states, then verify `/e panel` config edit validation/apply
  and font-scale resync. Keep any fixes bounded to defects proven by live QA.
- Classification: MEDIUM.
- `CIVIC_ROOT`
- `drawNotificationRow`
- `compactActionButton`
- `drawActionBand`

## Latest Slice Handoff - Phase 4, Slice 3

Slice name:

- Notification HUD helper adoption.

Objective:

- Adopt the new Core civic UI helpers in Notification HUD row/action drawing
  without changing notification packets, persistence, storage, filtering, or
  action semantics.

What was inspected:

- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionUiMetrics.java`

Decisions made:

- Preserve Notification HUD behavior and layout in this slice.
- Keep the HUD's existing action glyphs and text centering, but draw the button
  surface through a new reusable `ElarionCivicUi.compactActionButtonFrame`
  helper.
- Use `ElarionCivicUi.rowSurface` for rail slots and list rows.
- Keep local Notification color names for now, but alias them to
  `ElarionCivicColors` so Core is the source of the palette.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Unresolved risks:

- Notification shell/header/ornament drawing still has local rendering logic
  and some duplicated civic literals.
- Live screenshot QA was not run in this slice.
- Admin Panel still has duplicated row/action/modal drawing.
- Government still owns richer semantic civic components.

Work deliberately deferred:

- Notification packet/storage/action/filtering changes.
- Full Notification drawer redesign.
- Admin Panel helper migration.
- Government helper migration.
- UI gallery.

Precise recommended next slice:

- Phase 4, Slice 4: continue Notification HUD visual cleanup.
- Classification: MEDIUM.
- Migrate shell/header/ornament drawing and remaining duplicated civic literals
  toward Core helpers, then perform live screenshot QA of the drawer.
- Exclude notification packets, persistence, storage, category filtering, and
  action semantics.

Search terms:

- `ElarionNotificationHud`
- `drawDrawerShell`
- `drawHeaderOrnament`
- `drawRailShell`
- `ElarionCivicUi.thinBox`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButtonFrame`

## Latest Slice Handoff - Phase 4, Slice 4

Slice name:

- Notification HUD shell helper adoption.

Objective:

- Continue Notification HUD visual cleanup by moving shell/header/ornament
  drawing and reusable civic literals into Core helpers.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `PLAN.md`
- `TODO.md`

Decisions made:

- Keep Notification HUD packet handling, storage, category filtering, and
  action semantics unchanged.
- Move reusable shell/header/ornament/message drawing into Core helpers rather
  than leaving it as Notification-local procedural code.
- Keep live screenshot QA as a separate next slice because no active Minecraft
  client/screenshot control path was available in this turn.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Added `ElarionCivicColors` tokens for rail inset/marks, message-body
  surfaces, hover gloss, divider, destructive text/border, and info button
  states.
- Added `ElarionCivicUi` helpers for header shells, rail shells, message
  bodies, header ornaments, close buttons, and reusable dimming.
- Notification HUD now delegates rail shell, drawer shell, header ornaments,
  close button, divider, message body, row surfaces, action button frames, thin
  boxes, and action height to Core civic helpers.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Notification drawer screenshot QA was not run. Verify in a dev client
  before moving on to Admin Panel helper migration.

Unresolved risks:

- The new shared close button should be visually checked for centered X at the
  actual Minecraft GUI scale.
- Notification rail/header/detail/action states need live screenshot review
  against the reference after the helper migration.
- Admin Panel still has duplicated row/action/modal drawing.

Work deliberately deferred:

- Notification packet/storage/action/filtering changes.
- Full Notification drawer redesign.
- Admin Panel helper migration.
- Government helper migration.
- UI gallery.

Precise recommended next slice:

- Phase 4, Slice 5: live Notification drawer screenshot QA and small visual
  corrections.
- Classification: SMALL.
- Verify rail icons, drawer shell, header title/ornaments, centered close X,
  selected row, unread marker, action footer, empty state, and detail state.
- Exclude notification packets, persistence, storage, category filtering, and
  action semantics.

Search terms:

- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.railShell`
- `ElarionCivicUi.closeButton`
- `ElarionCivicUi.headerOrnament`
- `ElarionCivicUi.messageBody`
- `ElarionNotificationHud`

## Latest Slice Handoff - Phase 4, Slice 5

Slice name:

- Notification HUD layout contract.

Objective:

- Continue the Notification HUD QA path. Live screenshot QA was requested by
  the previous slice, but no active Minecraft client/screenshot control path
  was available, so this slice added bounded automated layout coverage instead.

What was inspected:

- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHudLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionNotificationHudLayoutTest.java`

Decisions made:

- Do not claim live screenshot QA without an active dev-client/screenshot path.
- Add a bootstrap-free Notification HUD layout helper for automated geometry
  coverage.
- Keep Notification HUD behavior unchanged; only route layout constants and
  metric helpers through the bootstrap-free helper.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHudLayout.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionNotificationHudLayoutTest.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Added `ElarionNotificationHudLayout` with the HUD geometry constants,
  `listX`, `closeX`, `listWidth`, scaled `rowHeight`,
  `actionHeaderHeight`, `actionButtonHeight`, and a `Metrics` record.
- `ElarionNotificationHud` now aliases the tested constants and metric helper
  methods from `ElarionNotificationHudLayout`.
- Added `ElarionNotificationHudLayoutTest` for close-button/header centering,
  list bounds, and font-scale growth of row/action heights.
- A first direct test of `ElarionNotificationHud` failed because the class
  initializes `ItemStack.EMPTY`, which requires Minecraft registry bootstrap in
  plain JVM tests. The final helper avoids that class initialization.

Tests run:

- Failed first attempt:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
  before extracting the bootstrap-free helper.
- Passing after fix:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Notification drawer screenshot QA is still pending.

Unresolved risks:

- The shared close button and migrated shell/header states still need real
  Minecraft screenshot review.
- Admin Panel still has duplicated row/action/modal drawing.

Work deliberately deferred:

- Notification packet/storage/action/filtering changes.
- Full Notification drawer redesign.
- Admin Panel helper migration.
- Government helper migration.
- UI gallery.

Precise recommended next slice:

- Phase 4, Slice 6: actual dev-client Notification drawer screenshot QA and
  small visual corrections.
- Classification: SMALL.
- Verify rail icons, drawer shell, header title/ornaments, centered close X,
  selected row, unread marker, action footer, empty state, and detail state.
- Exclude notification packets, persistence, storage, category filtering, and
  action semantics.

Search terms:

- `ElarionNotificationHudLayout`
- `ElarionNotificationHudLayoutTest`
- `layoutMetrics`
- `closeButtonIsCenteredInsideHeaderAndPanelBounds`
- `rowAndActionHeightsGrowWithFontScale`

## Latest Slice Handoff - Phase 4, Slice 6

Slice name:

- Notification HUD QA follow-up.

Objective:

- Run actual dev-client Notification drawer screenshot QA and make small visual
  corrections only.

What was inspected:

- `CODEX.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`

Decisions made:

- Do not claim live screenshot QA without an attached dev client or screenshot
  control path.
- Keep Notification packet, storage, filtering, category, and action semantics
  unchanged.
- Make only one low-risk visual helper-adoption correction: route the
  Notification action-footer divider through `ElarionCivicUi.divider`.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionNotificationHud.java`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Notification drawer screenshot QA remains pending.

Unresolved risks:

- Notification drawer still needs real Minecraft screenshot review.
- Admin Panel still has duplicated row/action/modal drawing.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer live
  QA and make only small visual corrections.
- If live QA remains unavailable: begin Admin Panel helper adoption for
  row/action/modal drawing.
- Admin helper-adoption scope classification: MEDIUM.

Search terms:

- `ElarionAdminPanelScreen`
- `ElarionCivicUi`
- `compactButton`
- `danger`
- `drawConfirmModal`
- `drawConfigEditShell`

## Latest Slice Handoff - Phase 4, Slice 7

Slice name:

- Admin Panel helper adoption.

Objective:

- Adopt shared Core civic helpers in Admin Panel row/action/modal surfaces
  without changing Admin config packets, permissions, validation, apply
  behavior, storage, provider actions, or server authority.

What was inspected:

- `docs/ai/CURRENT_STATUS.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/systems/GUI.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionConfigEditClientStateTest.java`

Decisions made:

- Keep Admin Panel click targets, packet payloads, config validation/apply
  predicates, provider action dispatch, permissions, storage, and server
  behavior unchanged.
- Use Core civic helpers for repeated row/action/modal visual surfaces instead
  of adding more Admin-local drawing logic.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Non-danger Admin rows now use `ElarionCivicUi.rowSurface`.
- Danger rows use `ElarionCivicColors` destructive tokens.
- Detail action buttons now use `ElarionCivicUi.compactActionButton`.
- Confirmation modal and config edit shell overlays now use
  `ElarionCivicColors.MODAL_OVERLAY`.
- Confirmation/config modal buttons now use `ElarionCivicUi.compactActionButton`
  with normal, primary, or destructive tones.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Admin Panel shell/detail panels still have some local drawing patterns and
  need incremental helper adoption or live QA-driven correction.
- Notification drawer still needs real Minecraft screenshot review against the
  reference.

Work deliberately deferred:

- Admin config packet/edit behavior changes.
- New config appliers or config write targets.
- Admin provider/action contract redesign.
- Full Admin Panel redesign.
- Notification packet/storage/action/filtering changes.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer and
  Admin Panel live QA, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Admin
  Panel shell/detail panels or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `ElarionAdminPanelScreen`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`
- `ElarionCivicColors.MODAL_OVERLAY`
- `renderDetail`
- `renderConfigEditShell`

## Latest Slice Handoff - Phase 4, Slice 8

Slice name:

- Admin Panel shell and detail helper adoption.

Objective:

- Continue Admin Panel helper adoption by routing the remaining main shell,
  header, list/detail frame, filter/input, and config edit result surfaces
  through shared Core civic helpers.

What was inspected:

- `RULES.md`
- `CODEX.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicUi.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ui/ElarionCivicColors.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`

Decisions made:

- Keep Admin Panel click targets, list/detail geometry constants, packet
  payloads, config validation/apply predicates, provider action dispatch,
  permissions, storage, and server behavior unchanged.
- Use existing Core civic shell/frame helpers instead of adding another local
  Admin drawing abstraction.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- The Admin Panel root frame now uses `ElarionCivicUi.attachedShell`.
- The Admin header now uses shared `ElarionCivicUi.headerOrnament` accents.
- The row list outer frame and filter field now use shared civic thin boxes.
- The detail panel now uses `ElarionCivicUi.headerShell`.
- Confirmation and config edit modal shells now use `ElarionCivicUi.headerShell`.
- Modal text input, config proposed value, and config result surfaces now use
  civic thin/message body helpers.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Admin Panel visuals still need real Minecraft screenshot review at the
  configured GUI scales.
- Notification drawer still needs real Minecraft screenshot review against the
  reference.
- Collection, character creation, Realm assignment, and other Core screens
  still have older direct panel/list/detail drawing patterns.

Work deliberately deferred:

- Admin config packet/edit behavior changes.
- New config appliers or config write targets.
- Admin provider/action contract redesign.
- Full Admin Panel redesign.
- Collection packet/action/provider behavior changes.
- Notification packet/storage/action/filtering changes.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer and
  Admin Panel live QA, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Collection shell/list/detail surfaces or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `ElarionCollectionScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `renderList`
- `renderDetail`

## Latest Slice Handoff - Phase 4, Slice 9

Slice name:

- Collection shell/list/detail helper adoption.

Objective:

- Adopt shared Core civic helpers in Collection shell/list/detail visual
  surfaces without changing Collection packets, actions, provider behavior,
  unlock state, title activation, mount preview behavior, storage, or server
  authority.

What was inspected:

- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionCollectionScreenLayoutTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/CollectionPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionCollectionServiceTest.java`

Decisions made:

- Keep Collection packet/action/provider behavior, unlock state, title
  activation, mount preview behavior, storage, and server authority unchanged.
- Adopt shared Core civic helpers only for generic shell/list/detail surfaces.
- Leave Collection-specific active/selected row rendering and icon-frame
  rendering intact because they encode existing Collection visual states.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/ElarionCollectionScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- The Collection root frame now uses `ElarionCivicUi.attachedShell`.
- The Collection title header now uses shared `ElarionCivicUi.headerOrnament`
  accents.
- The content header and list frame now use shared civic thin boxes.
- The detail panel now uses `ElarionCivicUi.headerShell`.
- The preview frame and preview body now use civic thin/message body helpers.
- Collection action buttons now use `ElarionCivicUi.compactActionButton`.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest --tests panetina.elarion.core.network.CollectionPayloadTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Collection visuals still need real Minecraft screenshot review, especially
  active/selected row clarity, icon frames, preview framing, mount rendering,
  and action button tone.
- Character Creation, Realm Assignment, and other Core screens still have older
  direct panel/input/choice drawing patterns.

Work deliberately deferred:

- Collection packet/action/provider behavior changes.
- Unlock state, title activation, or mount preview behavior changes.
- Character lifecycle behavior changes.
- Realm membership behavior changes.
- Notification/Admin live visual corrections.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run Notification drawer,
  Admin Panel, and Collection live QA, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Character Creation and Realm Assignment shell/input/choice surfaces.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `CharacterCreationScreen`
- `CharacterRealmAssignmentScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 10

Slice name:

- Character Creation and Realm Assignment helper adoption.

Objective:

- Adopt shared Core civic helpers in Character Creation and Realm Assignment
  shell/input/choice/button visual surfaces without changing character
  lifecycle behavior, Realm membership behavior, packets, persistence,
  commands, or server authority.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterRealmAssignmentScreen.java`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `platform/core/src/test/java/panetina/elarion/core/network/CharacterCreationPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/CharacterLifecycleServiceTest.java`

Decisions made:

- Keep character lifecycle behavior, Realm membership behavior, packets,
  persistence, commands, and server authority unchanged.
- Use existing Core civic helpers for shell/header/input/choice/button
  surfaces instead of introducing new screen-local drawing helpers.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/client/CharacterCreationScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/client/CharacterRealmAssignmentScreen.java`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Character Creation now uses `ElarionCivicUi.attachedShell` and shared header
  ornaments.
- Character Creation name and biography input surfaces now use civic
  thin/message body helpers.
- Character Creation submit and cooldown buttons now use
  `ElarionCivicUi.compactActionButton`.
- Realm Assignment now uses `ElarionCivicUi.attachedShell` and shared header
  ornaments.
- Realm Assignment option rows now use `ElarionCivicUi.rowSurface` with the
  assigned row selected and future choices disabled.
- Realm Assignment Continue now uses `ElarionCivicUi.compactActionButton`.

Tests run:

- `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.CharacterCreationPayloadTest --tests panetina.elarion.core.service.CharacterLifecycleServiceTest`
- `.\gradlew.bat :platform:core:test`

Not verified:

- Live Character Creation screenshot QA.
- Live Realm Assignment screenshot QA.
- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Character Creation biography clipping/scroll hints and cooldown state need
  real Minecraft screenshot review.
- Realm Assignment option row clarity needs real Minecraft screenshot review.
- Portal Confirmation, Grave Recovery, NPC dialogue, Shrine, and addon screens
  still have older direct panel/list/action drawing patterns.

Work deliberately deferred:

- Character lifecycle behavior changes.
- Realm assignment behavior changes.
- Packet, command, persistence, or server-authority changes.
- Live visual corrections for Notification, Admin, Collection, Character
  Creation, and Realm Assignment.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run live QA for Notification
  drawer, Admin Panel, Collection, Character Creation, and Realm Assignment,
  then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with a small
  addon screen pair such as Portal Confirmation and Grave Recovery shell
  surfaces.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `PortalConfirmationScreen`
- `GraveRecoveryScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 11

Slice name:

- Portal Confirmation and Grave Recovery helper adoption.

Objective:

- Adopt shared Core civic helpers in Portal Confirmation and Grave Recovery
  shell/body/status/slot/action visual surfaces without changing portal travel
  behavior, grave recovery behavior, packets, persistence, commands, inventory
  mutation, or server authority.

What was inspected:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/addons/portals.md`
- `docs/addons/underworld.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`
- `addons/underworld/src/test/java/panetina/elarion/addons/underworld/network/GraveOpenPayloadTest.java`
- Portal addon focused tests under `addons/portals/src/test/java`.

Decisions made:

- Keep portal travel behavior, grave recovery behavior, packets, persistence,
  commands, inventory mutation, and server authority unchanged.
- Use existing Core civic helpers for shell/header/body/status/slot/action
  surfaces instead of adding screen-local drawing helpers.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `addons/portals/src/main/java/panetina/elarion/addons/portals/client/PortalConfirmationScreen.java`
- `addons/underworld/src/main/java/panetina/elarion/addons/underworld/client/GraveRecoveryScreen.java`
- `docs/addons/portals.md`
- `docs/addons/underworld.md`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- Portal Confirmation now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, a civic message body, a civic gate icon frame, and civic action
  buttons.
- Portal Confirmation still sends only the existing
  `PortalTravelConfirmPayload` when the server-authored prompt allows travel.
- Grave Recovery now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, `statusChip`, civic item-slot frames, and civic action buttons.
- Grave Recovery still decodes/display items client-side only for rendering and
  sends only the existing `GraveRecoverPayload` for server-side recovery.

Tests run:

- `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayloadTest :addons:underworld:test --tests panetina.elarion.addons.underworld.network.GraveOpenPayloadTest`
- `.\gradlew.bat :addons:portals:test :addons:underworld:test`

Not verified:

- Live Portal Confirmation screenshot QA.
- Live Grave Recovery screenshot QA.
- Live Character Creation screenshot QA.
- Live Realm Assignment screenshot QA.
- Live Collection screenshot QA.
- Live Admin Panel screenshot QA.
- Live Notification drawer screenshot QA.

Unresolved risks:

- Portal Confirmation needs screenshot review for allowed/blocked button state,
  gate icon frame, prompt body spacing, and dynamic route dimensions.
- Grave Recovery needs screenshot review for item-slot grid density, item
  tooltips, scroll indicator placement, status chip clarity, and footer button
  spacing.
- NPC Dialogue and Shrine UI still have older direct panel/list/action drawing
  patterns and should be inspected before migration.

Work deliberately deferred:

- Portal travel behavior changes.
- Grave recovery behavior or inventory mutation changes.
- Packet, command, persistence, or server-authority changes.
- Live visual corrections for all migrated UI screens.

Precise recommended next slice:

- If a dev-client/screenshot path is available: run live QA for the migrated
  Core/addon screens, then make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with another
  bounded addon screen target, likely NPC Dialogue or Shrine UI shell/action
  surfaces after a short proposal/audit of their local drawing complexity.
- Classification: MEDIUM for another helper-adoption slice.

Search terms:

- `NpcDialogueScreen`
- `ShrineOfFoundationScreen`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.headerShell`
- `ElarionCivicUi.rowSurface`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 12

Slice name:

- NPC Dialogue and Shrine UI helper-adoption proposal.

Objective:

- Inspect NPC Dialogue and Shrine UI local drawing complexity and choose the
  next bounded helper-adoption target without changing production code.

What was inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/offerings/src/main/java/panetina/elarion/addons/offerings/client/ShrineOfFoundationScreen.java`
- `docs/addons/npcs.md`
- `docs/addons/offerings.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Decisions made:

- NPC Dialogue is the safer next implementation target because it already uses
  Core UI primitives and has a narrow visual-only path for shell/header/options
  footer and numeric prompt surfaces.
- Shrine UI is deferred because it has denser local drawing across project
  summary, progress, tabs, requirements, history, numeric prompt, rewards,
  reward slots, tooltips, icons, scrollbars, and contribution messages.
- The next implementation slice must not change NPC dialogue semantics,
  packets, portrait rendering, typing phases, saved scroll/selection,
  prompt validation, server-side condition/action evaluation, persistence,
  commands, or server authority.

Exact files changed:

- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Tests run:

- Documentation sanity checks only:
  - `rg -n "Phase 4, Slice 12|NPC Dialogue|Shrine UI|Phase 4, Slice 13" docs/reports/UI_SYSTEM_AUDIT.md PLAN.md TODO.md docs/ai/CURRENT_STATUS.md`
  - `git diff --check -- docs/reports/UI_SYSTEM_AUDIT.md PLAN.md TODO.md docs/ai/CURRENT_STATUS.md`

Not verified:

- Live screenshot QA was not run.
- No production code was changed.

Unresolved risks:

- NPC Dialogue has behavior-sensitive presentation state: typing phases, saved
  scroll/selection, numeric prompts, currency badge, cards, relationship hearts,
  portrait rendering, sounds, and server-authoritative option validation.
- Shrine UI remains a future helper-adoption target and will need a narrower
  implementation slice because it packs many row/prompt/reward variants into
  one screen.

Work deliberately deferred:

- NPC Dialogue implementation.
- Shrine UI implementation.
- Live visual corrections for all migrated screens.

Precise recommended next slice:

- Phase 4, Slice 13: NPC Dialogue shell/options/footer/prompt helper adoption.
- Classification: MEDIUM.
- Change only `NpcDialogueScreen` and relevant docs.
- Keep NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority unchanged.
- Exclude Shrine UI from this implementation slice.

Search terms:

- `NpcDialogueScreen`
- `renderOptions`
- `renderFooter`
- `renderPromptOverlay`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 13

Slice name:

- NPC Dialogue helper adoption.

Objective:

- Adopt shared Core civic helpers in NPC Dialogue shell/options/footer/prompt
  visual surfaces without changing dialogue packets, server validation,
  condition/action evaluation, portrait rendering, typing phases, sounds, saved
  scroll state, prompt semantics, persistence, commands, or server authority.

What was inspected:

- `docs/systems/GUI.md`
- `docs/addons/npcs.md`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/client/ui/ElarionConversationControllerTest.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/network/NpcDialoguePromptSubmitPayloadTest.java`

Decisions made:

- Keep NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority unchanged.
- Use Core civic helpers only for generic shell/options/footer/prompt surfaces.
- Leave Shrine UI untouched in this implementation slice.
- Keep live screenshot QA as a separate manual task because no active
  Minecraft client/screenshot control path was available.

Exact files changed:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/client/NpcDialogueScreen.java`
- `docs/addons/npcs.md`
- `docs/systems/GUI.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `PLAN.md`
- `TODO.md`
- `docs/ai/CURRENT_STATUS.md`

Implementation notes:

- NPC Dialogue root now uses `ElarionCivicUi.attachedShell`.
- Dialogue option rows now use `ElarionCivicUi.compactActionButton` with muted,
  normal, or primary tone based on existing input/selection state.
- Footer Close now uses `ElarionCivicUi.compactActionButton`.
- Numeric prompt overlay now uses `ElarionCivicUi.headerShell`.
- Numeric prompt input now uses `ElarionCivicUi.thinBox`.

Tests run:

- `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest --tests panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayloadTest`
- `.\gradlew.bat :addons:npcs:test`

Not verified:

- Live NPC Dialogue screenshot QA.
- Live migrated-screen screenshot QA for previous Phase 4 screens remains
  pending.

Unresolved risks:

- NPC Dialogue needs screenshot review for dynamic configured sizes, option
  row tone, prompt overlay fit, relation hearts, currency badge, cards, and
  scrollbars.
- Shrine UI remains a future helper-adoption target and should start with a
  narrow shell/header/close/numeric prompt slice.

Work deliberately deferred:

- Shrine UI migration.
- NPC dialogue behavior changes.
- Packet, command, persistence, or server-authority changes.
- Live visual corrections for migrated screens.

Precise recommended next slice:

- If live QA becomes available: run live QA for migrated Core/addon screens and
  make only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Shrine
  UI shell/header/close/numeric prompt surfaces only.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Offering donation behavior, milestone behavior, reward rendering
  semantics, row icon semantics, packets, persistence, commands, inventory
  mutation, and server authority.

Search terms:

- `ShrineOfFoundationScreen`
- `renderNumericPrompt`
- `renderClose`
- `renderListBackground`
- `renderSummary`
- `ElarionCivicUi.attachedShell`
- `ElarionCivicUi.compactActionButton`

## Latest Slice Handoff - Phase 4, Slice 14

Slice name:

- Live screenshot QA capture path.

Objective:

- Establish a repeatable Windows dev-client screenshot capture path before
  continuing visual helper adoption.

What was inspected:

- `CODEX.md`
- `dev/build.gradle`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/test-commands.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- Running Windows process/window state for the dev client.

Decisions made:

- Use `.\gradlew.bat runClientOne` as the default full-mod dev-client launch
  path for UI QA. It starts `ElarionAdmin` through the `dev` module.
- For real UI-state QA, also start `.\gradlew.bat runServer` and connect from
  Multiplayer using the saved `localhost` server entry. The main-menu capture
  only proves the screenshot mechanism.
- Use a local PowerShell helper under `dev/tools/` for window capture instead
  of keeping ad hoc snippets in handoff notes.
- Use `PrintWindow` by default because raw screen capture can include unrelated
  overlapping desktop windows.
- Keep `build/ui-qa/` as disposable local evidence unless a screenshot is
  intentionally promoted to `docs/ui/` as a canonical reference.

Exact files changed:

- `dev/tools/capture-minecraft-window.ps1`
- `CODEX.md`
- `docs/systems/GUI.md`
- `docs/systems/UI_JOURNAL.md`
- `docs/test-commands.md`
- `docs/reports/UI_SYSTEM_AUDIT.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Started the full dev client with `.\gradlew.bat runClientOne --console=plain`
  through a detached wrapper.
- Confirmed the dev client reached a `Minecraft* 1.21.1` window with the full
  Elarion mod set loaded.
- Confirmed raw `CopyFromScreen` capture can be polluted by another overlapping
  desktop app.
- Confirmed direct script execution is blocked by local PowerShell policy.
- Confirmed the documented bypass invocation works:
  `powershell -NoProfile -ExecutionPolicy Bypass -File .\dev\tools\capture-minecraft-window.ps1 -Output build\ui-qa\script-verified-main-menu.png`.
- Confirmed `PrintWindow` capture produced a clean Minecraft main-menu image:
  `build/ui-qa/script-verified-main-menu.png`.

Not verified:

- Notification drawer, Admin Panel, Collection, Character Creation, Realm
  Assignment, Portal Confirmation, Grave Recovery, NPC Dialogue, and Shrine UI
  screenshots were not yet captured in their target states.

Unresolved risks:

- The helper captures windows; it does not automate opening specific Elarion UI
  states.
- Driver-specific setups may return black images from `PrintWindow`; the script
  documents `-ScreenCapture` as the fallback when the Minecraft window is
  visible.
- Future dev-only UI gallery/test hooks are still needed for fully repeatable
  no-gameplay UI states.

Work deliberately deferred:

- Any production UI rendering changes.
- Any packet, config, command, persistence, or server-authority changes.
- Visual corrections for migrated screens.

Live screen access notes:

- Admin Panel: `/e panel`.
- Collection: `/collection` or the Collection keybind.
- Notification drawer: HUD notification rail.
- Shrine UI: right-click a linked Shrine of Foundation block.
- Civic Forum / Seat of Rule: right-click their Government blocks.
- NPC Dialogue: interact with a placed NPC; setup helpers include
  `/e npc place <npcDefinition> here`, `/e npc list`, and `/e npc tp <npcId>`.
- Portal Confirmation: interact with a configured/unlocked portal route while
  the server route/window state allows prompting.
- Grave Recovery: interact with an Underworld grave/tomb after creating or
  locating a corpse with Underworld test/admin commands.
- These flows are server-authoritative. Do not bypass them with client-only
  screen construction when testing action behavior.

Precise recommended next slice:

- Run the first live migrated-screen QA pass using
  the dev-server path plus `dev/tools/capture-minecraft-window.ps1`.
- Start with Notification drawer and the Admin Panel config edit path because
  they have recent shared-helper changes and the Admin path also needs custom
  payload crash verification.
- Classification: MEDIUM.
- Keep fixes limited to small visual corrections if the screenshots reveal
  clear defects.

Search terms:

- `capture-minecraft-window.ps1`
- `runClientOne`
- `Notification drawer`
- `ElarionAdminPanelScreen`
- `ElarionNotificationHud`

## Latest Slice Handoff - Admin Panel Payload Hardening

Slice name:

- Admin Panel Config crash fix, scoped descriptor rows, action suggestions,
  and faster live QA driver.

Objective:

- Stop `/e panel` and Config-tab custom payload disconnects, add an OP player
  Realm assignment action, add Tab completion for server-authored Admin Panel
  action inputs, and speed up live command/screenshot QA.

What was inspected:

- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/RealmService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/AbilityService.java`
- `platform/core/src/main/java/panetina/elarion/core/service/TitleService.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/service/MountAdminPanelProvider.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/entity/ElarionMountType.java`
- Focused Admin Panel/Core/Mount tests and docs listed below.

Decisions made:

- Keep Config discovery inside the existing Admin Panel snapshot model but make
  it scoped: Config opens with domain/category summaries only; selecting a
  category requests that category's entry rows.
- Add packet write-side caps for Admin Panel tabs, rows, row actions, and
  action suggestions so future oversized snapshots cannot desynchronize the
  client custom payload stream.
- Extend `ElarionAdminPanelAction` with server-authored input suggestions
  instead of hard-coding client-side lists.
- Add Tab completion only inside the existing single-field modal. The client
  cycles supplied suggestions and still sends only the submitted value; the
  server remains authoritative.
- Add Core `Set Realm` as an OP-only player action routed through
  `RealmService.assign`, followed by identity sync and admin history/event
  emission.
- Add mount ID suggestions from the Mounts provider instead of Core importing
  Mounts implementation details.
- Add `dev/tools/minecraft-qa.ps1` as a wrapper around focus/maximize, command
  sending, client-area clicks, and screenshot capture.

Exact files changed:

- `platform/core/src/main/java/panetina/elarion/core/model/ElarionAdminPanelAction.java`
- `platform/core/src/main/java/panetina/elarion/core/network/AdminPanelOpenPayload.java`
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionAdminPanelService.java`
- `platform/core/src/main/java/panetina/elarion/core/client/ElarionAdminPanelScreen.java`
- `addons/mounts/src/main/java/panetina/elarion/addons/mounts/service/MountAdminPanelProvider.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionAdminPanelServiceTest.java`
- `platform/core/src/test/java/panetina/elarion/core/network/AdminPanelPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/client/ElarionAdminPanelScreenLayoutTest.java`
- `dev/tools/minecraft-qa.ps1`
- `CODEX.md`
- `docs/test-commands.md`
- `docs/systems/GUI.md`
- `docs/addons/core.md`
- `docs/config.md`
- `docs/systems/UI_JOURNAL.md`
- `INDEX.md`
- `TODO.md`
- `PLAN.md`
- `docs/ai/CURRENT_STATUS.md`

APIs/packets changed:

- `ElarionAdminPanelAction` now includes bounded
  `parameterSuggestions`.
- `AdminPanelOpenPayload` now serializes those suggestions and caps tab, row,
  action, and suggestion lists before writing.

Config/persistence affected:

- No config files, parsed config formats, runtime state files, or persistence
  schemas changed.
- Config descriptor display behavior changed only in Admin Panel browsing:
  entry rows load by selected category instead of all at once.

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
- Passed:
  `.\gradlew.bat :addons:mounts:compileJava`
- Live patched `runServer`/`runClientOne` QA passed:
  `/e panel` opened, Config tab clicked, `Core: UI Theme` category loaded,
  Players tab opened, and Set Realm modal Tab-completed a Realm ID.
- No `custom_payload`, `DecoderException`, or `Internal Exception` entries
  appeared in the checked client log after the Config/category clicks.

Live evidence:

- `build/ui-qa/admin-config-overview-fixed.png`
- `build/ui-qa/admin-config-tab-fixed.png`
- `build/ui-qa/admin-config-category-fixed.png`
- `build/ui-qa/admin-players-set-realm-fixed.png`
- `build/ui-qa/admin-set-realm-tab-complete.png`

Unresolved risks:

- Font-scale config Apply still needs live validation/apply QA.
- Provider-contributed suggestions were compiled for Mounts but not manually
  opened in the live action modal yet.
- The Admin Panel still has many actions in a single player detail list; future
  provider page/category work should improve discoverability.

Work deliberately deferred:

- New config appliers.
- Broad Admin Panel redesign.
- Config persistence migrations.
- Offline player Realm reassignment.
- Additional autocomplete widgets beyond the current modal Tab cycling.

Precise recommended next slice:

- Continue Phase 3 Admin config edit verification: open
  `core:ui_theme:defaults.font-scale-percent`, validate and apply a safe value,
  confirm UI theme sync/reflow, and confirm non-applier entries keep Apply
  disabled.
- Also scroll the Players action list and open one Mount action to verify mount
  ID suggestions in the modal.
- Classification: SMALL to MEDIUM.

Search terms:

- `configRows(`
- `shouldRequestScopedRows`
- `parameterSuggestions`
- `set_realm`
- `minecraft-qa.ps1`

## Admin Config Apply Live Verification Handoff

Slice name:

- Admin Config apply, non-applier gating, and provider suggestion live QA.

What was inspected:

- Core UI theme font-scale applier and tests.
- Admin Panel scoped Config rows, edit shell, player action list, payload
  bounds, and server-authored suggestions.
- Mount Admin Panel provider suggestions.
- Live `runServer`/`runClientOne` client/server logs and screenshots.

Decisions and implementation:

- Keep the only production Apply target limited to
  `core:ui_theme:defaults.font-scale-percent`.
- Repair stale development `ui_theme.yml` files missing that scalar by
  inserting it into the existing `defaults` block.
- Continue rejecting duplicate scalar lines without file mutation.
- Add mouse-wheel posting to `dev/tools/minecraft-qa.ps1` so long list QA is
  repeatable.

Exact files changed in this continuation:

- `platform/core/src/main/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplier.java`
- `platform/core/src/test/java/panetina/elarion/core/config/CoreUiThemeFontScaleConfigApplierTest.java`
- `dev/tools/minecraft-qa.ps1`
- `CODEX.md`
- `docs/test-commands.md`
- `docs/systems/GUI.md`
- `docs/config.md`
- `docs/addons/core.md`
- `PLAN.md`
- `TODO.md`
- `docs/reports/CONFIG_ADMIN_AUDIT.md`
- `docs/ai/CURRENT_STATUS.md`

Verification:

- Passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest :addons:mounts:compileJava`
- Live Apply passed from `100` to `125`, reflowed/resynced the Admin Panel,
  and passed again restoring `125` to `100`.
- Live non-applier Apply remained disabled/non-mutating.
- Live Mount action Tab completion filled `airship` from Mount-owned
  suggestions.
- No custom-payload decoder/encoder/disconnect error appeared in checked logs.
  Unrelated invalid item model rotation errors remain outside this slice.

Live evidence:

- `build/ui-qa/font-apply-fixed-applied-125.png`
- `build/ui-qa/font-apply-restored-100.png`
- `build/ui-qa/config-non-applier-apply-disabled.png`
- `build/ui-qa/admin-players-mount-actions.png`
- `build/ui-qa/admin-grant-mount-tab-complete.png`

Unresolved risks and deferred work:

- Admin player actions still share one long list; provider page/category
  contracts remain future Phase 3/4 work.
- Danger-row confirmation-modal QA remains pending.
- No new config appliers, persistence migrations, offline-player assignment,
  or broad Admin Panel redesign were added.

Precise recommended next slice:

- Phase 4, Slice 16: populated Notification drawer live QA and bounded visual
  corrections.
- Seed notifications through existing authoritative events and capture rows,
  unread/selected states, action footer, detail view, reward tooltips, and
  empty-category behavior.
- Classification: MEDIUM.
- Avoid changing notification storage, filtering, packet contracts, or action
  semantics unless a live defect requires it.
