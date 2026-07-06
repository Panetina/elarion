# PLAN

Short project memory and current read order.

## Active Approved Work

Project-wide revamp master plan is active. Implementation is approved only one
explicit slice at a time.

Completed current slices:

- Phase 0, Slice 1: audit preparation and repository map verification.
- Report: `docs/reports/PROJECT_REVAMP_AUDIT.md`.
- Phase 0, Slice 2: Configuration and Admin Panel deep audit.
- Report: `docs/reports/CONFIG_ADMIN_AUDIT.md`.
- Phase 2, Slice 1: Core read-only config descriptor registry.
- Added `ElarionConfigRegistry`, read-only descriptor contracts, Core
  `ui_theme.yml` / `server_identity.yml` descriptors, and
  `ElarionApi.system().configs()`.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Phase 2, Slice 2: Groups read-only config descriptor domain.
- Added `GroupConfigDescriptors`, registered the `groups` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `GroupService.config()`.
- Verification passed:
  `.\gradlew.bat :addons:groups:test --tests panetina.elarion.addons.groups.config.GroupConfigDescriptorsTest`,
  `.\gradlew.bat :addons:groups:test`, and `.\gradlew.bat :platform:core:test`.
- Phase 3, Slice 1: read-only Admin Panel config browser skeleton.
- Added config-domain rows to the Admin Panel Systems tab through the existing
  row/detail model. The browser displays registered domains, source files,
  reload command, categories, entries, current/default values, bounds, choices,
  and reload/restart markers.
- Superseded by Phase 3, Slice 3: config-domain rows now live in the dedicated
  Config tab.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test :addons:groups:test`.
- Config file formats, config writes, reload behavior, packets, and
  persistence were not changed.
- Phase 2, Slice 3: Economy read-only config descriptor domain.
- Added `EconomyConfigDescriptors`, registered the `economy` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `EconomyTransactionService.config()` and `EconomyPricingService.definitions()`.
- Verification passed:
  `.\gradlew.bat :addons:economy:test --tests panetina.elarion.addons.economy.config.EconomyConfigDescriptorsTest`
  and `.\gradlew.bat :addons:economy:test :platform:core:test`.
- Economy reload behavior, config writes, packets, file formats, and
  persistence were not changed.
- Phase 2, Slice 4: Worlds read-only config descriptor domain.
- Added `WorldsConfigDescriptors`, registered the `worlds` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `WorldsConfigManager`.
- Verification passed:
  `.\gradlew.bat :addons:worlds:test --tests panetina.elarion.addons.worlds.config.WorldsConfigDescriptorsTest`
  and `.\gradlew.bat :addons:worlds:test :platform:core:test`.
- World storage, managed-world behavior, reload semantics, config writes,
  packets, file formats, and persistence were not changed.
- Phase 2, Slice 5: Portals read-only config descriptor domain.
- Added `PortalConfigDescriptors`, registered the `portals` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `PortalDefinitionService` route/UI snapshots.
- Verification passed:
  `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.config.PortalConfigDescriptorsTest`
  and `.\gradlew.bat :addons:portals:test :platform:core:test`.
- Portal travel behavior, schedule evaluation, Economy price integration,
  config writes, packets, file formats, and persistence were not changed.
- Phase 2, Slice 6: Offerings read-only config descriptor domain.
- Added `OfferingConfigDescriptors`, registered the `offerings` domain through
  `ElarionApi.system().configs()`, and exposed current values from
  `OfferingDefinitionService` project/UI snapshots.
- Verification passed:
  `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.config.OfferingConfigDescriptorsTest`
  and `.\gradlew.bat :addons:offerings:test :platform:core:test`.
- Offering donation/progression behavior, rewards, Shrine blocks, reload
  semantics, config writes, packets, file formats, and persistence were not
  changed.
- Phase 2, Slice 7: Government read-only config descriptor domain.
- Added `GovernmentConfigDescriptors`, registered the `government` domain
  through `ElarionApi.system().configs()`, and exposed current values from
  `GovernmentDefinitionService` settings/form snapshots.
- Verification passed:
  `.\gradlew.bat :addons:government:test --tests panetina.elarion.addons.government.config.GovernmentConfigDescriptorsTest`
  and `.\gradlew.bat :addons:government:test :platform:core:test`.
- Government voting, office, authority, form-loading, reload semantics,
  config writes, packets, file formats, and persistence were not changed.
- Phase 2, Slice 8: NPCs read-only config descriptor domain.
- Added `NpcConfigDescriptors`, registered the `npcs` domain after the first
  successful definition load, and exposed supplier-backed NPC definition,
  visual profile, dialogue summary, and UI snapshots.
- Verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.config.NpcConfigDescriptorsTest`
  and `.\gradlew.bat :addons:npcs:test :platform:core:test`.
- NPC placement/runtime state, dialogue sessions, condition/action evaluation,
  reload semantics, config writes, packets, file formats, and persistence were
  not changed.
- Phase 2, Slice 9: Quests read-only config descriptor domain.
- Added `QuestConfigDescriptors`, registered the `quests` domain after the
  existing validated definition load, and exposed supplier-backed quest
  package metadata and graph summaries.
- Verification passed:
  `.\gradlew.bat :addons:quests:test --tests panetina.elarion.addons.quests.config.QuestConfigDescriptorsTest`
  and `.\gradlew.bat :addons:quests:test :platform:core:test`.
- Quest runtime state, progression, actions/conditions, scheduled consequences,
  notifications, reload semantics, config writes, packets, file formats, and
  persistence were not changed.
- Phase 2, Slice 10: Realms protection read-only config descriptor domain.
- Added `RealmConfigDescriptors`, registered the `realms` domain from the
  loaded protection snapshot, and centralized shipped fallback values in
  `RealmProtectionConfig.defaults()`.
- Verification passed:
  `.\gradlew.bat :addons:realms:test --tests panetina.elarion.addons.realms.config.RealmConfigDescriptorsTest`
  and `.\gradlew.bat :addons:realms:test :platform:core:test`.
- Protection behavior, config loading semantics, packets, file formats, and
  persistence were not changed.
- Phase 2, Slice 11: Mounts collection text read-only config descriptor domain.
- Added `MountConfigDescriptors`, registered the `mounts` domain from the
  loaded Collection text snapshot, and exposed four presentation fields for
  each registered mount type.
- Verification passed:
  `.\gradlew.bat :addons:mounts:test --tests panetina.elarion.addons.mounts.config.MountConfigDescriptorsTest`
  and `.\gradlew.bat :addons:mounts:test :platform:core:test`.
- Collection rendering, mount runtime state, config loading, packets, file
  formats, and persistence were not changed.
- Phase 2, Slice 12: Underworld read-only config descriptor domain.
- Added `UnderworldConfigDescriptors`, registered the `underworld` domain from
  `UnderworldService.config()`, and exposed five bounded categories with 33
  settings.
- Verification passed:
  `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.config.UnderworldConfigDescriptorsTest`
  and `.\gradlew.bat :addons:underworld:test :platform:core:test`.
- Death/corpse/PvP loot/combat-tag/Soul Fracture behavior, config loading,
  packets, files, and persistence were not changed.
- Phase 2, Slice 13: Optimization/performance read-only config descriptor
  domain.
- Added `PerformanceConfigDescriptors`, registered the `optimization` domain
  with explicit `platform:core` ownership, and exposed 16 host, budget, and
  monitoring settings from `ElarionTaskService.snapshot()`.
- Verification passed:
  `.\gradlew.bat :addons:optimization:test --tests panetina.elarion.addons.optimization.PerformanceConfigDescriptorsTest`
  and `.\gradlew.bat :addons:optimization:test :platform:core:test`.
- Worker/queue behavior, budgets, monitoring, config loading, files, and
  runtime behavior were not changed.

- Phase 2, Slice 14: Core scalar read-only config descriptor expansion.
- Added 41 scalar entries across citizen/activity, chat, identity/nickname, and
  history categories to the existing `core` domain.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Core load/reload behavior, files, commands, persistence, and runtime behavior
  were not changed.

- Phase 2, Slice 15: Core Realm definition read-only descriptors.
- Added `realms.yml` to the existing `core` domain with Realm count/IDs and
  per-Realm presentation, visibility, spawn, and flag descriptors.
- Dynamic Realm rows are fixed to IDs present at descriptor registration;
  current values for those IDs remain supplier-backed through
  `CoreConfigManager.realms()` after successful `/e reload` operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Realm membership, governance, config loading, files, commands, persistence,
  and gameplay behavior were not changed.

- Phase 2, Slice 16: Core title and title-progression read-only descriptors.
- Added `titles.yml` and `title-progression.yml` to the existing `core` domain
  with title count/IDs, per-title presentation/acquisition/ownership summaries,
  progression region summaries, and unlock-rule summaries.
- Dynamic title/progression rows are fixed to IDs present at descriptor
  registration; current values for those IDs remain supplier-backed through
  existing `CoreConfigManager` snapshots after successful `/e reload`
  operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Title ownership/activation, ability ownership, config loading, files,
  commands, persistence, and gameplay behavior were not changed.

- Phase 2, Slice 17: Core reward read-only descriptors.
- Added `rewards.yml` to the existing `core` domain with reward count/IDs and
  per-reward action count/type/parameter descriptors.
- Dynamic reward rows are fixed to IDs and action indexes present at descriptor
  registration; current values for those rows remain supplier-backed through
  `CoreConfigManager.rewards()` after successful `/e reload` operations.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigRegistryTest`
  and `.\gradlew.bat :platform:core:test`.
- Reward execution, claim state, config loading, files, commands, packets,
  persistence, and gameplay behavior were not changed.

Addon runtime config and Core loaded-definition descriptor coverage are
complete for the currently parsed runtime config models. Jail, Security, and
Core abilities only generate placeholder/default YAML; typed loaders are
required before truthful descriptors can be registered for them.

- Phase 3, Slice 2: Admin Panel config browser contract proposal.
- Inspected the current Admin Panel service, snapshot/tab/row records, screen,
  and service tests.
- Decision: add a dedicated read-only `configs` Admin Panel tab as the next
  implementation step, using the existing snapshot/tab/row packet model.
- Defer page/category provider contracts, config writes, typed editing, reload
  orchestration, packet schema changes, persistence, and gameplay behavior.
- Risk recorded: the current fixed-width tab layout must be adjusted before
  adding a sixth tab.

- Phase 3, Slice 3: Dedicated read-only Admin Panel Config tab.
- Added a `configs` tab to the Admin Panel snapshot and moved config-domain
  rows out of Systems.
- Systems now contains provider-owned testing and repair rows only.
- Adjusted Admin Panel tab layout to compute tab width from tab count so six
  tabs fit inside the existing 660px panel.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

- Phase 3, Slice 4: Admin Panel typed config detail model proposal.
- Inspected current Admin Panel row/body/payload limits.
- Decision: do not add a new packet schema or page/category provider contract
  yet. The next implementation should improve read-only Config browsing by
  adding domain and category rows using the existing snapshot/tab/row packet
  model.
- Defer true typed controls, config writes, reload orchestration, packet schema
  changes, persistence, commands, and gameplay behavior.

- Phase 3, Slice 5: Config tab domain/category read-only rows.
- Expanded `ElarionAdminPanelService.configRows(...)` to emit one domain
  summary row plus one per-category detail row per registered config domain.
- Domain rows summarize owner, files, reload command, category count, entry
  count, reloadable/restart-required/invalid counts, and category summaries.
- Category rows use stable `config:<domain>:category:<category>` IDs and keep
  entry details scoped to that category.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.
- Config writes, typed editing, reload orchestration, page/category provider
  contracts, packet schema changes, persistence, commands, and gameplay
  behavior were not changed.

Phase 3, Slice 6: Admin Panel config mutation/readiness audit was completed as
documentation/proposal only.

- Verified `ElarionConfigEntry` exposes validation, bounds, reload/restart,
  read permission, and write permission metadata, but no apply/write contract.
- Verified the config registry remains read-only discovery.
- Verified the current Admin Panel action payload is generic string-parameter
  provider dispatch, not a typed config mutation API.
- Verified Config tab rows remain read-only and carry no edit actions.
- Decision: do not enable config editing yet and do not reuse generic provider
  actions as the long-term mutation API.

Phase 3, Slice 7: Core config mutation contract records was completed.

- Added `ElarionConfigChangeRequest`, `ElarionConfigChangeError`, and
  `ElarionConfigChangeResult`.
- The records model normalized targets, raw proposed values, optional
  expected-current values, actor/reason metadata, stable error codes,
  validation/applied/rejected statuses, old/new display values, reload/restart
  flags, audit event type, and immutable errors.
- No file writes, apply services, reload orchestration, packet schema changes,
  Admin Panel edit actions, persistence, commands, or gameplay behavior were
  added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeContractTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 8: Core config mutation validation service was completed.

- Added `ElarionConfigChangeValidator`.
- The validator resolves requests against the descriptor registry, checks write
  permission metadata, detects stale expected-current values, parses submitted
  raw values through entry codecs, runs entry validators, and returns
  `ElarionConfigChangeResult.validated` or `rejected`.
- The validator is pure/read-only. It does not write files, apply config,
  reload config, emit packets, add Admin Panel edit actions, touch persistence,
  register commands, or change gameplay behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigChangeValidatorTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 9: Admin Panel config validation preview action proposal was
completed.

- Inspected Admin Panel action/open payloads, row/action models, screen action
  flow, service dispatch, Config tab row IDs, and current tests.
- Decision: do not add a dedicated packet/model yet for read-only validation
  preview.
- Decision: do not validate category rows and do not add edit/apply buttons.
- The existing generic Admin Panel action payload can be reused narrowly for a
  preview-only `Validate Value` action only after Config gains stable
  entry-level rows.
- A dedicated config packet/model remains required before true editing, typed
  controls, diff display, audit preview, or apply/reload orchestration.

Phase 3, Slice 10: Config tab entry rows and validation preview action was
completed.

- Added stable Config entry rows under each category row in the dedicated
  Config tab.
- Entry rows expose path, description, current/default values, type, bounds,
  choices, permissions, runtime marker, and current validation state.
- Added a preview-only `Validate Value` action per entry row. The action calls
  `ElarionConfigChangeValidator` server-side and returns a short valid/invalid
  message.
- The validation preview does not write files, apply values, reload config,
  emit audit events, change runtime state, add typed controls, alter packet
  schemas, touch persistence, register commands, or change gameplay behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 11: dedicated config editing packet/model proposal was
completed.

- Inspected current Admin Panel payload/model contracts and config change
  request/result/error records.
- Decision: do not evolve `AdminPanelActionPayload` into the long-term config
  editing protocol. Keep it for generic provider actions and preview-only
  validation.
- Decision: true editing needs a Core-owned config edit packet/model family
  with explicit target, typed control metadata, expected-current value,
  proposed raw value, structured validation/apply result, reload/restart
  policy, audit preview, and bounded failure states.
- Decision: descriptor visibility does not imply editability. Each editable
  domain must later register an explicit reload-safe applier/edit provider.
- No Java behavior, packets, file writes, reloads, persistence, commands,
  gameplay behavior, or UI edit/save controls were changed.

Phase 4, Slice 15: first live server-backed UI screenshot QA pass was completed.

- Started `runServer` and `runClientOne`, connected `ElarionAdmin` to the saved
  `localhost` server, and opened each screen through its real authoritative
  interaction/session path.
- Captured Realm notification empty state, Civic Forum History, Create Civic
  Proposal modal, linked Shrine UI, and allowed Neutral Gate confirmation under
  `build/ui-qa/`.
- No custom-payload decode/disconnect error occurred. The server shut down
  cleanly after the pass.
- Findings: populated Notification rows/actions remain untested; the Neutral
  Gate icon frame was empty; the completed Shrine snapshot showed zero progress
  and zero requirements while reporting completion.
- Temporary Government/Shrine blocks, route unlock state, player position, and
  Offering anchor coordinates were restored after QA.
- No production Java, config schema, packet, command, or UI rendering code was
  changed.

Next recommended slice requiring approval:

Phase 3, Slice 12: Core config edit packet/model records was completed.

- Added `ElarionConfigEditTarget` and `ElarionConfigEditControl`.
- Added `ElarionConfigEditOpenPayload`,
  `ElarionConfigEditRequestPayload`, and
  `ElarionConfigEditResultPayload`.
- Added bounded packet codecs for edit-control snapshots, edit requests,
  edit results, targets, structured errors, and list fields.
- Added focused tests for target normalization and packet round trips.
- No server receiver registration, UI controls, file writes, apply services,
  reload orchestration, persistence, commands, or gameplay behavior were
  changed.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

Phase 3, Slice 13: config edit packet registration audit/proposal was
completed.

- Verified current payload codec registration lives in `ElarionCoreMod`
  through `PayloadTypeRegistry`.
- Verified server receivers live in `ElarionCoreMod` and client screen
  receivers live in `ElarionCoreClient`.
- Decision: register config edit payload types before adding live handlers.
- Decision: `ElarionConfigEditOpenPayload` and
  `ElarionConfigEditResultPayload` are S2C; `ElarionConfigEditRequestPayload`
  is C2S.
- Decision: receiver registration should be deferred until a Core dispatch
  method is added and reviewed. Future dispatch should stay OP-gated and
  service-owned, not implemented in the receiver lambda.
- No Java behavior, packet registration, receivers, UI controls, file writes,
  apply services, reload orchestration, persistence, commands, or gameplay
  behavior were changed.

Phase 3, Slice 14: config edit payload type registration was completed.

- Registered `ElarionConfigEditOpenPayload` as S2C.
- Registered `ElarionConfigEditResultPayload` as S2C.
- Registered `ElarionConfigEditRequestPayload` as C2S.
- No server/client receiver registration, UI controls, file writes, apply
  services, reload orchestration, persistence, commands, or gameplay behavior
  were changed.
- Verification passed:
  `.\gradlew.bat :platform:core:compileJava :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

Phase 3, Slice 15: config edit receiver/dispatch proposal was completed.

- Decision: the first live receiver should support validation only.
- Decision: the receiver should delegate to `ElarionAdminPanelService` or a
  narrow Core config edit coordinator; it must not implement validation or
  mutation directly in the receiver lambda.
- Decision: `VALIDATE` converts `ElarionConfigEditRequestPayload` into
  `ElarionConfigChangeRequest`, runs `ElarionConfigChangeValidator`, and
  returns `ElarionConfigEditResultPayload`.
- Decision: `APPLY` must return an `UNSUPPORTED` rejected result until an
  editable-domain applier registry exists.
- Decision: non-OP players must receive `PERMISSION_DENIED` without target
  existence details.
- No Java behavior, receivers, UI controls, file writes, apply services, reload
  orchestration, persistence, commands, or gameplay behavior were changed.

Phase 3, Slice 16: validation-only config edit receiver and dispatch was
completed.

- Added the server receiver for `ElarionConfigEditRequestPayload`.
- Added validation-only config edit dispatch to `ElarionAdminPanelService`.
- Validation requests now convert to `ElarionConfigChangeRequest`, run through
  `ElarionConfigChangeValidator`, send `ElarionConfigEditResultPayload`, and
  refresh the Admin Panel Config tab message.
- `APPLY` returns `UNSUPPORTED`; non-OP requests return `PERMISSION_DENIED`;
  `canApply` remains false for all results.
- No client edit UI, client result handler, config writes, apply services,
  reload orchestration, persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 17: config edit client result handling proposal was completed.

- Decision: add a lightweight client receiver/state cache as the next
  implementation slice.
- Decision: keep visible feedback in the existing Admin Panel refresh message
  until a dedicated edit/detail UI is specified.
- Rejected a no-op-only receiver because it would discard structured results.
- Rejected a dedicated edit detail state in the proposal slice because typed
  controls and editable-domain appliers do not exist yet.

Phase 3, Slice 18: config edit client result receiver/cache was completed.

- Added `ElarionConfigEditClientState` to store the last config edit result.
- Registered the client receiver for `ElarionConfigEditResultPayload` in
  `ElarionCoreClient`.
- Cleared cached config edit result state on client join and disconnect.
- Added focused tests for cache update, clear, and rejected-result retention.
- No Admin Panel edit controls, dedicated result rendering, server behavior,
  descriptor changes, config writes, apply services, reload orchestration,
  persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 19: config edit open/detail UX proposal was completed.

- Decision: config edit detail should be a dedicated state opened from a Config
  entry row, not another generic one-field modal.
- Decision: use the existing generic Admin Panel action path only to ask the
  server to open a selected entry; the server resolves the target and sends
  `ElarionConfigEditOpenPayload`.
- Decision: the client should store the open `ElarionConfigEditControl` and
  later render a dedicated detail/modal state with current/default values,
  proposed value, choices/bounds, validation errors, old/new diff,
  reload/restart policy, permission labels, Validate, and a visible disabled
  Apply state.
- `Apply` remains disabled until domain appliers exist and validation returns
  `canApply=true`.
- No Java behavior, UI rendering, config writes, apply services, reload
  orchestration, persistence, commands, or gameplay behavior were changed.

Phase 3, Slice 20: config edit open payload receiver and client open-state
cache was completed.

- Extended `ElarionConfigEditClientState` to store the current open
  `ElarionConfigEditControl`.
- Registered a client receiver for `ElarionConfigEditOpenPayload`.
- Opening a new control clears stale validation result state.
- Existing join/disconnect clearing now clears both open control and result.
- Added focused tests for open control update, clear, and stale result reset.
- No server open action, Admin Panel row action, rendered edit UI, validation
  input controls, config writes, apply services, reload orchestration,
  descriptor changes, persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 21: config edit server open action proposal was completed.

- Decision: add a Core-owned `Open Editor` Admin Panel row action for config
  entry rows.
- Decision: reuse the existing OP-gated `AdminPanelActionPayload` path rather
  than adding a new C2S open request payload.
- Decision: server resolves `config-entry|domain|category|entry` through
  `ElarionConfigRegistry` and sends `ElarionConfigEditOpenPayload` only for
  valid targets.

Phase 3, Slice 22: config edit server open action and Config row action was
completed.

- Added the `Open Editor` action to Config entry rows.
- Added the server-side resolver/sender in `ElarionAdminPanelService`.
- Controls are sent with `editable=false` and
  `Config editing is not enabled yet.`
- Unknown/stale targets return an Admin Panel failure message.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 23: config edit detail shell proposal was completed.

- Decision: first rendered detail should be a presentation-only modal/detail
  shell over `ElarionAdminPanelScreen` using
  `ElarionConfigEditClientState.openControl()`.
- Decision: show label, description, path, current/default values, type,
  choices/bounds, reload/restart policy, permissions, disabled reason, Close,
  and disabled/non-interactive Validate/Apply affordances.
- Text input, validation submission, Apply, result rendering, writes, reload
  orchestration, and appliers remain deferred.

Phase 3, Slice 24: config edit read-only detail shell was completed.

- `ElarionAdminPanelScreen` now renders a read-only config edit shell from
  `ElarionConfigEditClientState.openControl()`.
- The shell shows descriptor metadata, disabled reason, Close, and disabled
  Validate/Apply affordances.
- Escape and Close clear only the open-control state.
- `ElarionConfigEditClientState.closeOpenControl()` was added and tested.
- No text input, validation submission, result rendering beyond disabled
  placeholders, config writes, apply services, reload orchestration,
  descriptor changes, persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 25: config edit validation input proposal was completed.

- Decision: add proposed-value input state to the config edit shell, seeded
  from the current display value when a control opens.
- Decision: input is local to `ElarionAdminPanelScreen` and is discarded when
  the open control closes or changes.
- Decision: Validate sends `ElarionConfigEditRequestPayload` with `VALIDATE`,
  expected current display value from the server-authored control, and reason
  `admin-panel-config-edit-preview`.
- Decision: result display must be target-matched and must not show stale
  results after the proposed value changes.
- `Apply` remains disabled/non-interactive even after successful validation.
- No Java behavior, packets, UI input, config writes, apply services, reload
  orchestration, descriptor changes, persistence, commands, or gameplay
  behavior were changed.

Phase 3, Slice 26: config edit validation input and request was completed.

- Added local proposed-value input state to `ElarionAdminPanelScreen`.
- Input seeds from the server-authored current display value when a control
  opens.
- Typing and Backspace edit the proposed value while the shell is open.
- Input changes clear stale validation result state.
- Enter and Validate send `ElarionConfigEditRequestPayload` with `VALIDATE`.
- Result rendering is target-matched against the open control.
- Apply remains visible but disabled/non-interactive.
- No `APPLY` requests, config writes, reload orchestration, descriptor changes,
  persistence, commands, or gameplay behavior were added.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`
  and `.\gradlew.bat :platform:core:test`.

Phase 3, Slice 27: config edit apply/readiness proposal was completed.

- Verified current config change handling is validation-only. It can resolve
  descriptors, check permissions, reject stale expected-current values, parse
  proposed values, and run descriptor validators, but it has no writer,
  reload orchestrator, rollback behavior, or apply audit contract.
- Decision: `Apply` must stay disabled until Core has a separate
  apply/applier contract. Descriptor visibility and successful validation do
  not imply editability.
- Future apply support must require explicit domain appliers, re-run
  server-side validation before write, serialize concurrent mutation per
  domain/file group, preserve previous runtime snapshots on failure, emit
  administrative domain events/history only after successful mutation, and
  refresh affected Admin Panel/client snapshots.
- No production code, config files, descriptors, packets, reload behavior, or
  UI behavior changed in this proposal-only slice.

Phase 3, Slice 28: Core config apply contract records/registry was completed.

- Added `ElarionConfigApplyRegistry`, `ElarionConfigApplier`,
  `ElarionConfigApplyCapability`, `ElarionConfigApplyContext`, and
  `ElarionConfigApplyReadiness`.
- Registrations are explicit per `ElarionConfigEditTarget`, reject duplicates,
  declare audit/file/reload/restart capability metadata, and expose
  descriptor-aware readiness failures for unknown, missing, disabled, or
  policy-incompatible targets.
- The contract is inert: it is not exposed through `ElarionApi`, has no
  production registrations, and is not wired to Admin Panel dispatch.
- No file writes, reload/rollback orchestration, UI Apply enablement,
  descriptors, or addon/domain appliers changed.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

- Phase 3, Slice 29: config apply registry ownership and readiness wiring
  proposal.
- Classification: SMALL.
- Audit registry lifetime/API ownership and how readiness should feed
  server-authored edit controls and validation results while Apply remains
  disabled.
- Exclude implementation, file writes, reload/rollback orchestration, UI Apply
  enablement, descriptor changes, and addon/domain appliers.

Phase 3, Slice 29: config apply registry ownership and readiness wiring
proposal was completed.

- Decision: Core owns one process-lifetime canonical apply registry beside the
  descriptor registry.
- Decision: addons must receive a registration-only facade. Do not expose the
  concrete registry because its registration lookup contains executable
  appliers that could bypass Admin authorization and audit orchestration.
- Decision: Admin consumes a non-executable readiness view. Open controls may
  show target-specific readiness failures, but `editable` and result
  `canApply` remain false until a Core apply coordinator exists.
- Decision: an addon registers an applier immediately after its matching
  descriptor and validated runtime snapshot exist. Deferred descriptor domains
  such as NPCs register both under the same one-time startup guard.
- No production code, packets, config, persistence, reload behavior, or UI
  behavior changed.

Next recommended slice requiring approval:

- Phase 3, Slice 30: canonical apply registry ownership and registration-only
  Core API.
- Classification: MEDIUM.
- Add a narrow registrar interface/facade, construct one canonical registry in
  Core, and expose only registration through `ElarionSystemApi`.
- Exclude Admin readiness integration, production/domain registrations,
  applier execution, file writes, reload/rollback, audit emission, and UI Apply
  enablement.

Phase 3, Slice 30: canonical apply registry ownership and registration-only
Core API was completed.

- Added `ElarionConfigApplyRegistrar` as the public registration-only contract.
- Core constructs one canonical `ElarionConfigApplyRegistry` beside the
  descriptor registry before API/addon initialization.
- `ElarionSystemApi.configAppliers()` exposes a method-reference facade, not the
  concrete registry, so addons cannot retrieve executable registrations.
- Added focused coverage for facade delegation, duplicate rejection through the
  facade, its single-method surface, and the public API return type.
- No production appliers, Admin readiness integration, execution, file writes,
  reload/rollback, audit emission, packets, persistence, or UI behavior changed.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`
  and `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

- Phase 3, Slice 31: non-executable Admin config readiness integration.
- Classification: MEDIUM.
- Add a readiness-only Core view and use it to provide target-specific disabled
  reasons in server-authored edit controls/results while `editable=false`,
  `canApply=false`, and the client Apply button remain unchanged.
- Exclude applier execution, production/domain registrations, file writes,
  reload/rollback, audit emission, and UI Apply enablement.

Phase 3, Slice 31: non-executable Admin config readiness integration was
completed.

- Added `ElarionConfigApplyReadinessProvider`, exposing only
  `readiness(target)`.
- Core binds a method-reference view over the canonical apply and descriptor
  registries into `ElarionAdminPanelService`.
- Config edit controls and successful validation results now show precise
  missing/policy readiness reasons. A ready registration reports that execution
  is not enabled.
- `editable=false`, `canApply=false`, server `APPLY` rejection, and the disabled
  client button remain unchanged.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.config.ElarionConfigApplyRegistryTest`
  and `.\gradlew.bat :platform:core:test`.

Next approved slice:

- Phase 3, Slice 32: Core config apply coordinator contract audit.
- Classification: SMALL.
- Decide validation/readiness recheck, locking, trusted context resolution,
  applier failure handling, atomicity/rollback ownership, audit emission, and
  result invariants before implementing execution.
- Exclude production code, Admin Apply wiring, production/domain appliers, file
  writes, reload behavior, and UI enablement.

Phase 3, Slice 32: Core config apply coordinator contract audit was completed.

- Verified the current direct `ElarionConfigApplier.apply(...)` callback has no
  coordinator-controlled prepare/commit/rollback boundary.
- Verified audit currently requires two post-mutation operations: domain event
  emission and administration history recording. A failure after mutation
  cannot be safely handled by the current callback contract.
- Verified `ElarionConfigChangeResult.applied(...)` currently drops descriptor
  reload/restart flags, so coordinator results cannot yet preserve validated
  runtime policy.
- Decision: do not implement the coordinator or enable Apply on the current
  callback contract. First evolve appliers to prepare a transaction with
  explicit commit and rollback operations.
- Future coordinator uses one conservative global config-mutation lock initially,
  revalidates and rechecks readiness under that lock, resolves trusted
  descriptors, prepares the owner transaction, commits, emits audit/history,
  and rolls back if commit/audit completion fails.
- No production code changed in this audit slice.

Next approved slice:

- Phase 3, Slice 33: transactional config applier contract.
- Classification: MEDIUM.
- Add a prepared-change transaction contract, change the unused applier API from
  immediate apply to prepare, preserve reload/restart metadata in applied
  results, and update focused tests.
- Exclude coordinator implementation, production registrations, Admin Apply
  wiring, file writes, reload behavior, audit emission, and UI enablement.

Phase 3, Slice 33: transactional config applier contract was completed.

- Added `ElarionConfigPreparedChange` with `commit()`, `rollback()`, and a safe
  `of(commit, rollback)` helper.
- The helper permits one successful commit, prevents commit after rollback,
  permits rollback after a failed or successful commit, and invokes rollback at
  most once.
- Changed the unused `ElarionConfigApplier` API from immediate `apply(context)`
  to non-mutating `prepare(context)` returning a prepared transaction.
- Added an applied-result factory preserving reload/restart flags and required a
  nonblank audit event type for every `APPLIED` result.
- Updated focused registry/Admin tests and added prepared-change lifecycle
  coverage.
- Verification passed: focused prepared-change, change-contract, registry, and
  Admin tests; full `.\gradlew.bat :platform:core:test`; then focused
  prepared-change/change-contract tests after final API cleanup.
- No coordinator, production applier, Admin Apply, file write, reload, audit,
  packet, persistence, or UI behavior was added.

Next recommended slice requiring approval:

- Phase 3, Slice 34: internal config apply coordinator implementation.
- Classification: MEDIUM.
- Add an unwired Core coordinator with one global mutation lock, in-lock
  validation/readiness, trusted context resolution, transactional
  prepare/commit, mandatory injected audit sink, strict result invariants, and
  rollback on commit/audit failure.
- Exclude Core/addon production registrations, Admin/network/UI wiring, real
  file writes, reload orchestration, and client synchronization.

Phase 3, Slice 34: internal config apply coordinator implementation was
completed.

- Added structured `ElarionConfigApplyAuditRecord` and mandatory
  `ElarionConfigApplyAuditSink` contracts.
- Added an unwired `ElarionConfigApplyCoordinator` using one fair,
  process-global mutation lock.
- The coordinator revalidates and rechecks readiness under lock, resolves
  trusted descriptors, prepares/commits the owner transaction, strictly checks
  returned request/values/runtime policy/audit type, and records audit only
  after a trusted commit.
- Prepare, commit, owner-result, or audit failures return `APPLY_FAILED`;
  post-prepare failures invoke rollback and include rollback failure details.
- Added focused tests for success/audit data, invalid/stale/missing rejection,
  preparation and commit failure, invalid owner result, audit rollback,
  rollback failure reporting, and concurrent serialization.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
  and `.\gradlew.bat :platform:core:test`.
- No production coordinator instance, applier, Admin/network/UI wiring, real
  file write, reload orchestration, or client synchronization was added.

Next recommended slice requiring approval:

- Phase 3, Slice 35: coordinator ownership and audit-adapter proposal.
- Classification: SMALL.
- Decide canonical coordinator lifetime, history/domain-event audit ordering and
  metadata, Admin execution exposure, and behavior when Core history is not
  bound.
- Exclude implementation, production appliers, Admin Apply enablement, file
  writes, reload orchestration, packets, UI, and synchronization.

Phase 3, Slice 35: coordinator ownership and audit-adapter proposal was
completed.

- Verified `HistoryService.record(...)` requires a bound server, is filtered by
  configurable history policy, and queues writes. It cannot be the mandatory
  durable audit authority for config mutation.
- Verified domain-event dispatch catches listener failures and is suitable as a
  post-commit integration projection, not durable audit state.
- Decision: do not construct the canonical production coordinator until Core
  has a bound write-ahead config audit journal.
- The audit sequence must be journal PREPARED (forced), owner commit, journal
  COMMITTED (forced), then best-effort History/domain-event projections. A
  projection failure after durable COMMITTED must not roll back the config.
- Admin will eventually receive an execution-only facade, not the concrete
  coordinator or registry. Execution remains unavailable while the journal is
  unbound.
- No production code changed.

Next recommended slice requiring approval:

- Phase 3, Slice 36: write-ahead config audit session contracts.
- Classification: MEDIUM.
- Replace the one-shot audit sink with prepare/committed/failed session
  semantics and adjust the unwired coordinator/tests. Keep persistence and
  production ownership unwired.
- Exclude journal storage/files, production coordinator construction, appliers,
  Admin/network/UI wiring, reload, and synchronization.

Phase 3, Slice 36: write-ahead config audit session contracts was completed.

- Added `ElarionConfigApplyAuditSession` with committed, rolled-back, and failed
  terminal operations.
- Changed `ElarionConfigApplyAuditSink` from one-shot post-commit recording to
  `prepare(record)` returning the write-ahead session.
- The coordinator now prepares audit after non-mutating owner preparation but
  before commit. Audit preparation failure rolls back prepared resources without
  committing.
- Trusted success requires the session's committed terminal operation. Commit,
  result, or committed-terminal failure rolls back and records rolled-back;
  rollback failure records failed. Audit outcome failures remain visible in the
  returned `APPLY_FAILED` message.
- Updated focused tests for phase ordering and all terminal paths.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`
  and `.\gradlew.bat :platform:core:test`.
- No audit storage/file, production coordinator, applier, Admin/network/UI
  wiring, reload, or synchronization was added.

Next recommended slice requiring approval:

- Phase 3, Slice 37: durable config audit journal storage.
- Classification: MEDIUM persistence slice.
- Implement the unbound journal sink with versioned JSONL PREPARED/COMMITTED/
  ROLLED_BACK/FAILED records, synchronous append+force, bounded unresolved-tail
  recovery, and temp-directory tests.
- Exclude production binding, coordinator ownership, appliers,
  Admin/network/UI wiring, config file writes, reload, and synchronization.

Phase 3, Slice 37: durable config audit journal storage was completed.

- Added `ElarionConfigApplyAuditPhase`.
- Added `ElarionConfigApplyAuditJournal` as the unbound durable audit sink.
- The journal writes versioned JSONL PREPARED, COMMITTED, ROLLED_BACK, and
  FAILED records, synchronously appends and forces every record, and stores the
  full normalized audit record with one audit ID across phases.
- The journal exposes `journalPath(root)` for
  `world/elarion/core/audit/config-changes.jsonl` style binding, but no
  production service binds it yet.
- Added bounded unresolved-tail recovery for startup diagnostics and
  temporary-directory tests for phase appends, terminal outcomes, pending
  recovery, bounded tail behavior, double terminal rejection, append failure,
  and path shape.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production coordinator owner, server/world binding, domain applier,
  Admin/network/UI Apply wiring, config file write, reload, or synchronization
  was added.

Next recommended slice requiring approval:

- Phase 3, Slice 38: production config apply ownership proposal.
- Classification: SMALL proposal slice.
- Decide the Core service/facade that will bind the descriptor registry, apply
  registry, coordinator, and durable audit journal to the active server/world
  path.
- Exclude domain appliers, Admin Apply wiring, config file writes, reload, and
  synchronization until the ownership boundary is approved.

Phase 3, Slice 38: production config apply ownership proposal was completed.

- Decision: add a Core-owned `ElarionConfigApplyService` as the production
  owner for constructing/binding `ElarionConfigApplyCoordinator`.
- The service should receive the canonical descriptor registry and concrete
  apply registry during Core initialization, then bind on `SERVER_STARTED` using
  `JsonStateStorage.elarionRoot(server)` and
  `ElarionConfigApplyAuditJournal.journalPath(root)`.
- The service should refuse execution while unbound, when journal recovery is
  unsafe, or when bounded recovery sees unresolved PREPARED audit records.
- Public addon API remains registration-only:
  `ElarionApi.system().configAppliers()`.
- Admin should receive only a narrow readiness/executor facade, not coordinator,
  concrete registry lookup, or journal mutation access.
- No code, appliers, Admin Apply wiring, config writes, reload, sync, or packet
  behavior changed in this proposal slice.

Next recommended slice requiring approval:

- Phase 3, Slice 39: implement `ElarionConfigApplyService` skeleton.
- Classification: SMALL.
- Add the service class, bind it in `ElarionCoreMod`, route Admin readiness
  through the service, and add focused tests for unbound, bound, unresolved
  audit recovery, and readiness delegation.
- Exclude execute/apply wiring, domain appliers, Admin Apply enablement, config
  file writes, reload, and synchronization.

Phase 3, Slice 39: `ElarionConfigApplyService` skeleton was completed.

- Added `ElarionConfigApplyService` as the Core-owned production lifecycle
  owner for future config apply execution.
- Core constructs the service with the canonical descriptor registry and
  concrete apply registry.
- Core binds the service on `SERVER_STARTED` using
  `JsonStateStorage.elarionRoot(server)` and unbinds on `SERVER_STOPPING`.
- Admin readiness now flows through the service instead of directly through the
  concrete apply registry.
- The service blocks otherwise-ready targets while unbound, when bounded audit
  recovery is truncated, or when unresolved PREPARED audit records exist.
- Added focused tests for unbound, bound, missing applier/descriptor delegation,
  unresolved audit recovery, truncated recovery, and unbind behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`,
  and `.\gradlew.bat :platform:core:test`.
- No execute/apply method, domain applier, Admin Apply enablement, config file
  write, reload, synchronization, or packet behavior was added.

Next recommended slice requiring approval:

- Phase 3, Slice 40: backend config apply execution method.
- Classification: SMALL.
- Add a narrow `apply(request, actorPermission)` method to
  `ElarionConfigApplyService` that returns `UNSUPPORTED` while unbound/unsafe
  and delegates to the coordinator only when service execution is ready.
- Cover success/failure with fake in-test appliers only.
- Exclude production domain appliers, Admin/network/UI Apply wiring, config
  file writes, reload, and synchronization.

Phase 3, Slice 40: backend config apply execution method was completed.

- Added `ElarionConfigApplyService.apply(request, actorPermission)`.
- The method rejects with `UNSUPPORTED` while the service is unbound or audit
  recovery is unsafe.
- When the service is bound and safe, it delegates to the internal coordinator.
- Focused tests cover unbound rejection, unsafe-recovery rejection, successful
  delegation through a fake in-test applier, validation failure delegation, and
  missing-applier readiness failure delegation.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.ElarionConfigApplyCoordinatorTest --tests panetina.elarion.core.config.ElarionConfigApplyAuditJournalTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production domain applier, Admin/network/UI Apply wiring, config file
  write, reload, or synchronization was added.

Next recommended slice requiring approval:

- Phase 3, Slice 41: Admin config apply executor boundary proposal.
- Classification: SMALL proposal slice.
- Decide the narrow interface Admin should use for apply execution, how OP
  checks and packet responses should flow, and how to keep the UI disabled until
  production appliers are explicitly approved.
- Exclude implementation, production domain appliers, config file writes,
  reload, and synchronization.

Phase 3, Slice 41: Admin config apply executor boundary proposal was completed.

- Decision: add a narrow `ElarionConfigApplyExecutor` contract.
- The executor should expose only readiness lookup and backend apply execution.
- `ElarionConfigApplyService` should implement this executor.
- `ElarionAdminPanelService` should bind the executor instead of only a
  readiness provider.
- The first implementation should preserve current behavior: server-side
  `Intent.APPLY` still rejects, and the client Apply button remains disabled.
- Later server-side APPLY dispatch must be its own slice with OP checks, stale
  expected-current behavior, result payload conversion, and Admin refresh tests.
- Existing packet schema already has `Intent.APPLY`; no packet schema change is
  required for the boundary.

Next recommended slice requiring approval:

- Phase 3, Slice 42: add Admin config apply executor facade.
- Classification: SMALL.
- Add `ElarionConfigApplyExecutor`, have `ElarionConfigApplyService` implement
  it, and bind Admin to the executor while keeping APPLY rejected.
- Exclude server-side APPLY dispatch, production appliers, config file writes,
  reload, synchronization, and UI Apply enablement.

Phase 3, Slice 42: Admin config apply executor facade was completed.

- Added `ElarionConfigApplyExecutor` with readiness lookup and backend apply
  execution.
- `ElarionConfigApplyService` now implements the executor.
- `ElarionAdminPanelService` now binds an executor instead of only a readiness
  provider.
- Core initialization binds Admin to `configApplyService` through
  `bindConfigApplyExecutor(...)`.
- The legacy readiness-only binding helper remains available for focused tests
  and wraps readiness in an executor that rejects apply.
- Server-side `Intent.APPLY` still rejects before dispatch, and the client Apply
  button remains disabled.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`
  and `.\gradlew.bat :platform:core:test`.
- No server-side APPLY dispatch, production applier, config file write, reload,
  synchronization, or UI Apply enablement was added.

Next recommended slice requiring approval:

- Phase 3, Slice 43: server-side Admin APPLY dispatch proposal.
- Classification: SMALL proposal slice.
- Decide exact request/result conversion from `ElarionConfigEditRequestPayload`
  APPLY to `ElarionConfigApplyExecutor.apply(...)`, OP checks, stale-value
  behavior, Admin refresh, and whether to keep the client Apply button disabled
  until a production applier exists.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Phase 3, Slice 43: server-side Admin APPLY dispatch proposal was completed.

- Decision: keep `ElarionConfigEditRequestPayload` unchanged; existing
  `Intent.APPLY` is sufficient.
- Keep the `ElarionCoreMod` receiver shape unchanged: packet enters the server
  thread, Admin service returns `ElarionConfigEditResultPayload`, client gets
  the result, and Config tab refreshes with the result message.
- Evolve Admin service conversion internally so VALIDATE uses the validator and
  APPLY calls `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Build `ElarionConfigChangeRequest` once from target, proposed raw value,
  expected-current display value, actor UUID, and reason.
- Empty reasons should become `admin-panel-config-edit-preview` for VALIDATE and
  `admin-panel-config-edit-apply` for APPLY.
- APPLIED results convert to `ElarionConfigEditResultPayload` with status
  APPLIED, old/new values, runtime flags, no errors, `canApply=false`, audit
  preview, and an `Applied:` message.
- Client Apply remains disabled until a later UI slice enables it
  conditionally from server-authored controls.

Next recommended slice requiring approval:

- Phase 3, Slice 44: implement server-side Admin APPLY dispatch.
- Classification: SMALL.
- Change only Admin service request/result conversion and focused tests.
- Keep client Apply disabled and use fake test executors only.
- Exclude production appliers, config file writes, reload, synchronization, and
  UI Apply enablement.

Phase 3, Slice 44: server-side Admin APPLY dispatch was completed.

- `ElarionAdminPanelService.configEditResult(...)` now handles both VALIDATE and
  APPLY.
- VALIDATE behavior remains unchanged.
- APPLY now builds the same `ElarionConfigChangeRequest` shape and calls
  `ElarionConfigApplyExecutor.apply(...)` after OP checks.
- Empty APPLY reasons use `admin-panel-config-edit-apply`.
- APPLIED executor results convert to `ElarionConfigEditResultPayload` with
  old/new values, runtime flags, no errors, `canApply=false`, an audit preview,
  and an `Applied:` message.
- Rejected executor results preserve errors and return `Apply failed: ...`.
- Focused tests cover non-OP rejection before dispatch, OP fake-executor
  dispatch, APPLIED payload conversion, rejected executor result conversion,
  stale expected-current behavior through executor validation, and unchanged
  VALIDATE behavior.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production applier, config file write, reload, synchronization, or UI Apply
  enablement was added.

Next recommended slice requiring approval:

- Phase 3, Slice 45: Admin Apply UI enablement proposal.
- Classification: SMALL proposal slice.
- Decide how server-authored edit controls should expose Apply availability,
  whether the client button stays disabled until a production applier exists,
  and what manual/automated checks are required before any user-facing Apply
  button is clickable.
- Exclude implementation, production appliers, config file writes, reload, and
  synchronization.

Phase 3, Slice 45: Admin Apply UI enablement proposal was completed.

- Decision: keep the client Apply button disabled until at least one production
  applier is approved and tested.
- Apply availability must be server-authored, not inferred from validation
  success.
- The existing `editable` control flag is too broad because it mixes input
  editability and apply execution.
- Future control metadata should distinguish input editability from apply
  availability, for example `inputEditable`, `applyAvailable`, and
  `applyDisabledReason`.
- Future client Apply enablement must require an open control, nonblank input,
  matching latest VALIDATED result, matching old/new values, server-authored
  apply availability, and result `canApply=true`.
- The server must continue to reject forged APPLY packets when no safe executor
  or applier is available.

Next recommended slice requiring approval:

- Phase 3, Slice 46: split config edit control input/apply state.
- Classification: SMALL.
- Add explicit apply availability metadata to the control/payload model and
  focused packet/client-state tests, but keep the Admin screen Apply button
  disabled.
- Exclude production appliers, config file writes, reload, synchronization, and
  clickable Apply UI.

Phase 3, Slice 46: split config edit control input/apply state was completed.

- `ElarionConfigEditControl` now carries `inputEditable`, `applyAvailable`,
  `disabledReason`, and `applyDisabledReason`.
- The old constructor remains as a compatibility path and maps `editable` to
  `inputEditable` with `applyAvailable=false`.
- The legacy `editable()` method remains as an alias for `inputEditable()`.
- `ElarionConfigEditOpenPayload` round-trip now includes explicit apply
  availability metadata.
- The Admin screen continues to render/click Apply as disabled, but can display
  `applyDisabledReason` separately when the input is editable.
- Focused tests cover packet round-trip, client-state preservation, and
  Admin-open controls exposing explicit apply-disabled state.
- Verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`,
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest`,
  and `.\gradlew.bat :platform:core:test`.
- No production applier, config file write, reload, synchronization, or
  clickable Apply UI was added.

Next recommended slice requiring approval:

- Phase 3, Slice 47: production config applier selection proposal.
- Classification: SMALL proposal slice.
- Choose the first low-risk production config entry/domain to make writable and
  define file write, reload, rollback, audit, and UI rollout requirements.
- Exclude implementation.

Phase 3, Slice 47: production config applier selection proposal was completed.

- Selected first writable target:
  `core:ui_theme:defaults.font-scale-percent`.
- Source file: `config/elarion/core/ui_theme.yml`.
- YAML path: `defaults.font-scale-percent`.
- Type/policy: bounded integer `100-150`, runtime-reloadable, not
  restart-required, OPERATOR write permission.
- Future audit event type:
  `core.config.ui_theme.font_scale_changed`.
- Reason for choosing it first: one scalar value, existing descriptor and
  validator coverage, existing Core reload path, existing client UI theme sync,
  and no mutation of gameplay authority, Realm routing, economy, title
  ownership, or runtime persistence.
- Deferred Realm spawn edits because they are multi-field, gameplay-affecting
  location transactions and should not be the first production config write.
- Deferred server identity, dynamic Realm/title/reward definition rows, addon
  domains, and restart-required settings.
- Implementation requirements for the next backend slice are documented in
  `docs/reports/CONFIG_ADMIN_AUDIT.md`.
- No production applier, file write, reload, synchronization, UI click
  enablement, config behavior, or gameplay behavior was changed.

Next recommended slice requiring approval:

- Phase 3, Slice 48: Core UI theme font-scale applier backend.
- Classification: MEDIUM.
- Implement only the backend applier registration, file mutation, reload,
  rollback, audit-backed apply execution, and focused tests for
  `core:ui_theme:defaults.font-scale-percent`.
- Keep the Admin screen Apply button disabled unless separately approved for UI
  click enablement.

Phase 3, Slice 48: Core UI theme font-scale applier backend was completed.

- Added `CoreUiThemeFontScaleConfigApplier`.
- Registered exactly one production backend target:
  `core:ui_theme:defaults.font-scale-percent`.
- The target writes only `config/elarion/core/ui_theme.yml` at
  `defaults.font-scale-percent`.
- The applier stages exact old/new file contents, replaces exactly one
  `font-scale-percent:` scalar line, writes through a temp file plus replace,
  reloads `CoreConfigManager`, and resyncs UI themes through the existing Core
  UI theme service.
- Rollback restores the prior file contents, reloads the previous valid theme,
  and resyncs UI themes.
- Core initialization registers the applier with the canonical apply registry
  and tracks the active server only for post-apply theme sync.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionConfigApplyServiceTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- The visible Admin Apply button remains disabled. No client click path, new
  addon appliers, Realm spawn editing, server identity editing, or
  restart-required config writing was added.

Next recommended slice requiring approval:

- Phase 3, Slice 49: Admin Apply click enablement for the single font-scale
  target.
- Classification: MEDIUM.
- Enable the client Apply button only when server-authored apply availability
  and a matching latest validation result allow it, and keep all other config
  targets disabled.
- Exclude adding any new production config appliers.

Phase 3, Slice 49: Admin Apply click enablement for the single font-scale
target was completed.

- Server-opened config edit controls now derive `inputEditable` and
  `applyAvailable` from backend apply readiness.
- Validation results now return `canApply=true` only when the target has a
  ready apply backend.
- The Admin client enables Apply only when:
  - the open control is input-editable and apply-available.
  - the proposed input is nonblank.
  - the latest result targets the same config entry.
  - the latest result is `VALIDATED`.
  - the latest result has `canApply=true`.
  - the result has no errors.
  - the validated old value matches the current control value.
  - the validated new value matches the current proposed input.
- Apply sends `ElarionConfigEditRequestPayload.Intent.APPLY` only from that
  predicate.
- Applied results close the edit shell so stale current values are not reused
  after reload/sync.
- Config edit packet registrations were verified in source:
  `ElarionConfigEditOpenPayload` and `ElarionConfigEditResultPayload` are S2C;
  `ElarionConfigEditRequestPayload` is C2S.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.ElarionConfigEditPayloadTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- No new production config appliers, addon config editing, Realm spawn editing,
  server identity editing, restart-required config writes, or generic
  all-config Apply behavior was added.

Phase 3 V1 status:

- Complete for the first safe Admin config edit path:
  read-only descriptor discovery, server-side validation, audit-backed backend
  apply, safe file write/reload/rollback, UI theme resync, and strict client
  Apply gating for `core:ui_theme:defaults.font-scale-percent`.
- Remaining work moves to later phases/slices: broader Admin page/provider
  contracts, additional production config appliers, addon-specific reload
  safety, manual client QA, and Phase 4 shared UI tokens/primitives.

Phase 4, Slice 1: shared UI token/component audit and proposal was completed.

- Report: `docs/reports/UI_SYSTEM_AUDIT.md`.
- Verified Core already owns base UI primitives and scaled typography, while
  the strongest civic row/action/shell helpers remain split between Government,
  Notification, and Admin classes.
- No production code was changed.
- Decision: add generic civic UI tokens and primitives to Core before any full
  screen migration.

Phase 4, Slice 2: Core civic UI token and primitive helper foundation was
completed.

- Added `ElarionCivicColors`, `ElarionCivicUi`, and `ElarionUiMetrics`.
- Added focused metric coverage in `ElarionUiMetricsTest`.
- No existing screen was migrated and no packet, persistence, config, or
  server behavior changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Phase 4, Slice 3: Notification HUD helper adoption was completed.

- Notification rail slots and notification rows now use
  `ElarionCivicUi.rowSurface`.
- Notification thin boxes now delegate to `ElarionCivicUi.thinBox`.
- Notification action button frames now use
  `ElarionCivicUi.compactActionButtonFrame` while preserving icon-plus-label
  centering.
- Notification action height now uses `ElarionUiMetrics.controlHeight`.
- Notification local civic color names now alias `ElarionCivicColors`.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Phase 4, Slice 4: Notification HUD shell helper adoption was completed for
code; live screenshot QA remains pending.

- Added Core civic tokens for rail/message/divider/destructive/info UI states.
- Added Core civic helpers for header shells, rail shells, message bodies,
  header ornaments, close buttons, and reusable dimming.
- Notification HUD now delegates rail shell, drawer shell, header ornaments,
  close button, divider, message body, row surfaces, action button frames, thin
  boxes, and action height to Core civic helpers.
- No notification packet, persistence, storage, filtering, category, or action
  semantic behavior changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live screenshot QA was not run because no active Minecraft client/screenshot
  control path was available in this turn.

Phase 4, Slice 5: Notification HUD layout contract was completed.

- No active Minecraft client/screenshot control path was available, so live
  screenshot QA was not run.
- Added bootstrap-free `ElarionNotificationHudLayout` for tested Notification
  HUD geometry and scaled metric constants.
- `ElarionNotificationHud` now aliases tested layout constants and metric
  helpers from `ElarionNotificationHudLayout`.
- Added `ElarionNotificationHudLayoutTest` covering close-button/header
  centering, list bounds, and scaled row/action heights.
- The first direct-HUD test attempt failed because `ItemStack.EMPTY` requires
  Minecraft registry bootstrap in a plain JVM test; extracting the layout
  helper fixed that.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`.
- Additional focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ui.ElarionUiMetricsTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Phase 4, Slice 6: Notification HUD QA follow-up was completed in limited form.

- No attached Minecraft client, app terminal, or screenshot-control path was
  available, so live Notification drawer screenshot QA could not be performed.
- The only code correction made was routing the Notification action-footer
  divider through `ElarionCivicUi.divider`.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionNotificationHudLayoutTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Notification drawer screenshot QA remains a high-priority manual task.

Phase 4, Slice 7: Admin Panel helper adoption was completed.

- Non-danger Admin rows now use `ElarionCivicUi.rowSurface`.
- Danger rows use Core civic destructive colors.
- Detail action buttons now use `ElarionCivicUi.compactActionButton`.
- Confirmation modal and config edit shell overlays now use
  `ElarionCivicColors.MODAL_OVERLAY`.
- Confirmation/config modal buttons now use `ElarionCivicUi.compactActionButton`
  with normal, primary, or destructive tones.
- Admin click targets, config validation/apply predicates, packet sends,
  permissions, storage, and provider action semantics were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.

Next recommended slice requiring approval:

- If live QA becomes available: run Notification drawer and Admin Panel
  screenshot QA and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Admin
  Panel shell/detail panels or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Admin config packet/edit behavior, permissions, storage, and config
  apply semantics.

Phase 4, Slice 8: Admin Panel shell and detail helper adoption was completed.

- The Admin Panel root frame now uses `ElarionCivicUi.attachedShell`.
- The Admin header now uses shared `ElarionCivicUi.headerOrnament` accents.
- The row list outer frame and filter field now use shared civic thin boxes.
- The detail panel and confirmation/config modal shells now use
  `ElarionCivicUi.headerShell`.
- Modal text input, config proposed value, and config result surfaces now use
  civic thin/message body helpers.
- Admin click targets, list/detail geometry constants, config validation/apply
  predicates, packet sends, permissions, storage, and provider action
  semantics were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest --tests panetina.elarion.core.client.ElarionConfigEditClientStateTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Admin Panel and Notification drawer screenshot QA remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run Notification drawer and Admin Panel
  screenshot QA and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Collection shell/list/detail surfaces or another low-risk Core screen.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Collection packet/action/provider behavior, unlock state, title
  activation, mount preview behavior, storage, and server authority.

Phase 4, Slice 9: Collection shell/list/detail helper adoption was completed.

- The Collection root frame now uses `ElarionCivicUi.attachedShell`.
- The Collection title header now uses shared `ElarionCivicUi.headerOrnament`
  accents.
- The content header and list frame now use shared civic thin boxes.
- The detail panel now uses `ElarionCivicUi.headerShell`.
- The preview frame and preview body now use civic thin/message body helpers.
- Collection action buttons now use `ElarionCivicUi.compactActionButton`.
- Collection-specific active/selected row rendering and icon-frame rendering
  were left intact because they encode existing Collection visual states.
- Click targets, layout constants, packet sends, provider actions, unlock
  state, title activation, mount preview rendering, storage, and server
  authority were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest --tests panetina.elarion.core.network.CollectionPayloadTest --tests panetina.elarion.core.service.ElarionCollectionServiceTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Collection, Admin Panel, and Notification drawer screenshot QA remain
  pending.

Next recommended slice requiring approval:

- If live QA becomes available: run Notification drawer, Admin Panel, and
  Collection screenshot QA and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with
  Character Creation and Realm Assignment shell/input/choice surfaces.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude character lifecycle behavior, Realm membership behavior, packets,
  persistence, commands, and server authority.

Phase 4, Slice 10: Character Creation and Realm Assignment helper adoption was
completed.

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
- Click targets, input focus behavior, submit/close behavior, payload sends,
  lifecycle state, Realm assignment state, persistence, commands, and server
  authority were not changed.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.network.CharacterCreationPayloadTest --tests panetina.elarion.core.service.CharacterLifecycleServiceTest`.
- Full Core verification passed: `.\gradlew.bat :platform:core:test`.
- Live Character Creation, Realm Assignment, Collection, Admin Panel, and
  Notification drawer screenshot QA remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run live QA for Notification drawer, Admin
  Panel, Collection, Character Creation, and Realm Assignment and apply only
  small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with a small
  addon screen pair such as Portal Confirmation and Grave Recovery shell
  surfaces.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude portal travel behavior, grave recovery behavior, packets,
  persistence, commands, inventory mutation, and server authority.

Phase 4, Slice 11: Portal Confirmation and Grave Recovery helper adoption was
completed.

- Portal Confirmation now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, a civic message body, a civic gate icon frame, and civic action
  buttons.
- Portal Confirmation still sends only the existing
  `PortalTravelConfirmPayload` when the server-authored prompt allows travel.
- Grave Recovery now uses `ElarionCivicUi.attachedShell`, shared header
  ornaments, `statusChip`, civic item-slot frames, and civic action buttons.
- Grave Recovery still decodes/display items client-side only for rendering and
  sends only the existing `GraveRecoverPayload` for server-side recovery.
- Portal and Grave click targets, packet payloads, travel/recovery validation,
  persistence, inventory mutation, commands, and server authority were not
  changed.
- Focused verification passed:
  `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.network.PortalRouteStatusSyncPayloadTest :addons:underworld:test --tests panetina.elarion.addons.underworld.network.GraveOpenPayloadTest`.
- Touched addon verification passed:
  `.\gradlew.bat :addons:portals:test :addons:underworld:test`.
- Live Portal Confirmation, Grave Recovery, Character Creation, Realm
  Assignment, Collection, Admin Panel, and Notification drawer screenshot QA
  remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run live QA for the migrated Core/addon screens
  and apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with another
  bounded addon screen target, likely NPC Dialogue or Shrine UI shell/action
  surfaces after a short proposal/audit of their local drawing complexity.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude NPC dialogue condition/action evaluation, Offering donation or
  milestone behavior, packets, persistence, commands, inventory mutation, and
  server authority.

Phase 4, Slice 12: NPC Dialogue and Shrine UI helper-adoption proposal was
completed.

- Inspected `NpcDialogueScreen` and `ShrineOfFoundationScreen`.
- NPC Dialogue already uses Core primitives for scaled screens, typography,
  dialogue boxes, portrait frames, cards, virtual lists, scrollbars, and
  numeric prompts, but still has direct shell/header/options/footer/prompt
  drawing that can safely adopt civic helpers.
- Shrine UI has broader local drawing density across project summary,
  progress, tabs, requirement rows, history rows, numeric prompt, rewards,
  reward slots, tooltips, icons, scrollbars, and contribution messages.
- Decision: migrate NPC Dialogue first. Defer Shrine helper adoption until
  after NPC or until live QA identifies a higher-priority Shrine visual issue.
- No production code was changed.

Next recommended slice requiring approval:

- Phase 4, Slice 13: NPC Dialogue shell/options/footer/prompt helper adoption.
- Classification: MEDIUM.
- Change only `NpcDialogueScreen` and relevant docs.
- Keep NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority unchanged.
- Exclude Shrine UI from this implementation slice.

Phase 4, Slice 13: NPC Dialogue helper adoption was completed.

- NPC Dialogue root now uses `ElarionCivicUi.attachedShell`.
- Dialogue option rows now use `ElarionCivicUi.compactActionButton` with muted,
  normal, or primary tone based on existing input/selection state.
- Footer Close now uses `ElarionCivicUi.compactActionButton`.
- Numeric prompt overlay now uses `ElarionCivicUi.headerShell`.
- Numeric prompt input now uses `ElarionCivicUi.thinBox`.
- NPC portrait rendering, dialogue boxes, relation hearts, currency badge,
  cards, typing phases, sounds, option selection state, virtual-list scroll
  behavior, prompt validation, packet sends, condition/action evaluation,
  persistence, commands, and server authority were not changed.
- Shrine UI was not changed.
- Focused verification passed:
  `.\gradlew.bat :addons:npcs:test --tests panetina.elarion.addons.npcs.client.ui.ElarionConversationControllerTest --tests panetina.elarion.addons.npcs.network.NpcDialoguePromptSubmitPayloadTest`.
- Full NPC addon verification passed: `.\gradlew.bat :addons:npcs:test`.
- Live NPC Dialogue and migrated-screen screenshot QA remain pending.

Next recommended slice requiring approval:

- If live QA becomes available: run live QA for migrated Core/addon screens and
  apply only small visual corrections.
- If live QA remains unavailable: continue Phase 4 helper adoption with Shrine
  UI shell/header/close/numeric prompt surfaces only.
- Classification: MEDIUM for another helper-adoption slice.
- Exclude Offering donation behavior, milestone behavior, reward rendering
  semantics, row icon semantics, packets, persistence, commands, inventory
  mutation, and server authority.

Phase 4, Slice 14: live screenshot QA capture path was completed.

- Added `dev/tools/capture-minecraft-window.ps1`, a Windows helper that captures
  the Minecraft window by title using `PrintWindow` by default.
- Verified `.\gradlew.bat runClientOne` launches the full dev client as
  `ElarionAdmin` and reaches a `Minecraft* 1.21.1` window.
- Verified raw screen capture can be polluted by overlapping desktop windows,
  while `PrintWindow` produced a clean Minecraft main-menu PNG at
  `build/ui-qa/script-verified-main-menu.png`.
- Direct script execution is blocked by local PowerShell policy on this
  machine; use `powershell -NoProfile -ExecutionPolicy Bypass -File
  .\dev\tools\capture-minecraft-window.ps1`.
- Updated `CODEX.md`, `docs/systems/GUI.md`, `docs/systems/UI_JOURNAL.md`,
  `docs/test-commands.md`, `docs/reports/UI_SYSTEM_AUDIT.md`, `INDEX.md`,
  `TODO.md`, and `docs/ai/CURRENT_STATUS.md` with the live capture workflow.
- No production Java, UI rendering code, packets, config, persistence,
  commands, or server-authoritative behavior were changed.
- This slice establishes capture only. Opening specific Elarion UI states is
  still manual/dev-world setup until a future dev-only UI gallery or test hooks
  exist.

Phase 3/4 live Admin Panel payload hardening was completed on 2026-07-06.

- Fixed `/e panel` custom payload disconnects by serializing rows only for the
  selected tab and by scoping Config rows: Config opens with domain/category
  summaries, and category clicks request only that category's entry rows.
- Added Admin Panel payload caps for tabs, rows, row actions, and action input
  suggestions before packet write.
- Added server-authored action suggestions and client Tab completion for
  single-field Admin Panel actions.
- Added Core `Set Realm` player action through server-authoritative
  `RealmService.assign`; Core player actions now suggest Realm IDs, title IDs,
  and registered abilities.
- Updated Mount admin player actions to suggest registered mount IDs.
- Added `dev/tools/minecraft-qa.ps1` for faster live QA command sending,
  client-area clicks, focus/maximize, and screenshots.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest`
  and `.\gradlew.bat :addons:mounts:compileJava`.
- Live QA passed with patched `runServer`/`runClientOne`: `/e panel` opened,
  Config tab clicked, `Core: UI Theme` category loaded, Players tab opened, and
  Set Realm modal Tab-completed a Realm id with no custom-payload disconnect.
  Evidence: `build/ui-qa/admin-config-*-fixed.png`,
  `build/ui-qa/admin-players-set-realm-fixed.png`, and
  `build/ui-qa/admin-set-realm-tab-complete.png`.

Completed follow-up recommendation:

- Continue Phase 3 Admin config edit verification: open
  `core:ui_theme:defaults.font-scale-percent`, validate/apply a safe value,
  confirm UI theme sync/reflow, and confirm non-applier config entries keep
  Apply disabled.
- Also verify provider suggestions in a player action such as Mount grant after
  scrolling the player action list.
- Classification: SMALL to MEDIUM.
- Exclude new config appliers, broad Admin Panel redesign, persistence
  migrations, and unrelated UI polish.

Live server QA workflow addendum:

- For real UI state checks, start `.\gradlew.bat runServer`, then
  `.\gradlew.bat runClientOne`, then join Multiplayer using the saved
  `localhost` server entry.
- Use server-authoritative commands/interactions to open target screens:
  `/e panel`, `/collection`, HUD notification rail, linked Shrine block,
  Civic Forum/Seat of Rule blocks, placed NPC interaction, configured portal
  route interaction, and grave/tomb interaction.
- Some screens intentionally do not have direct open commands because their
  action packets require server-issued block/entity/session context.

Deferred combined QA recommendation:

- Run a focused populated-Notification QA pass. Seed Realm notifications
  through existing server-owned events and verify rows/actions/detail.
- The Admin config edit portion of this earlier combined recommendation is now
  completed and verified below.
- Classification: MEDIUM.
- Expected outputs: local PNGs under `build/ui-qa/`, documented findings, and
  only small visual corrections if clearly necessary.
- Exclude packet schemas, notification storage/filtering/action semantics,
  config mutation semantics, persistence, commands, and server authority.

Admin config edit live verification was completed on 2026-07-06.

- Fixed the first live Apply failure for stale development `ui_theme.yml`
  files that predated `defaults.font-scale-percent`; the applier now inserts
  the missing scalar under the existing `defaults` block, while duplicate
  scalar lines still reject without mutation.
- Added mouse-wheel scroll support to `dev/tools/minecraft-qa.ps1` so long
  Admin Panel action lists can be tested repeatably.
- Live patched `runServer`/`runClientOne` QA passed:
  - Config tab opens without custom-payload disconnect.
  - `Core: UI Theme` category loads scoped entry rows.
  - `core:ui_theme:defaults.font-scale-percent` validates and applies `125`,
    reflows the Admin Panel with larger text, then applies back to `100`.
  - Non-applier row `defaults.logical-width` reports no registered applier and
    Apply remains non-mutating.
  - Players action list scrolls to Mount actions; `Grant Mount` modal
    Tab-completes `airship` from server-authored Mount suggestions.
- Live evidence:
  `build/ui-qa/font-apply-fixed-applied-125.png`,
  `build/ui-qa/font-apply-restored-100.png`,
  `build/ui-qa/config-non-applier-apply-disabled.png`,
  `build/ui-qa/admin-players-mount-actions.png`, and
  `build/ui-qa/admin-grant-mount-tab-complete.png`.
- Focused verification passed:
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.config.CoreUiThemeFontScaleConfigApplierTest --tests panetina.elarion.core.service.ElarionAdminPanelServiceTest --tests panetina.elarion.core.network.AdminPanelPayloadTest --tests panetina.elarion.core.client.ElarionAdminPanelScreenLayoutTest :addons:mounts:compileJava`.
- Checked live logs: no custom-payload decoder/encoder/disconnect errors were
  found; remaining logged errors were unrelated invalid item model rotations.

Next recommended slice:

- Phase 4, Slice 16: populated Notification drawer live QA and bounded visual
  corrections.
- Seed Realm/Personal notifications through existing server-owned events,
  capture compact rows, unread state, selected state, action footer, detail
  view, reward tooltips, and empty-category behavior.
- Classification: MEDIUM.
- Exclude notification packet/storage/filter/action semantic changes unless a
  live defect proves one is required.

## Current Focus

1. Read `AGENTS.md`.
2. Read `CODEX.md`.
3. Use `INDEX.md` to reach the source-of-truth docs.
4. Read `docs/ai/CURRENT_STATUS.md` for current handoff status.
5. Read `docs/reports/PROJECT_REVAMP_AUDIT.md`.
6. Read `docs/reports/CONFIG_ADMIN_AUDIT.md`.
7. Continue only with the next explicitly approved slice.
8. Work in the existing subsystem first instead of inventing a new one.
9. Before adding new systems, clear or deliberately defer the current audit
   findings in `TODO.md`.

## Current Reality

- Fabric 1.21.1 is the target platform.
- Core owns canonical truth: citizens, Realms, titles, identity, history,
  rewards, permissions, and shared infrastructure.
- Addons extend behavior, but they do not duplicate Core state.
- `addons/offerings` owns Shrine / Offering gameplay.
- `addons/government` owns the civic flow and authority backend.
- `addons/groups` owns public groups and Confederation eligibility hooks.
- `addons/npcs` owns static NPCs and dialogue.
- `addons/portals` owns linked scheduled portals and tickets.
- Current build status: `.\gradlew.bat build` passes when run with a larger
  one-off Gradle heap on this machine (`GRADLE_OPTS=-Xmx4g --no-daemon`);
  focused Core/Mounts compile/tests pass normally.
- Current audit status: no duplicate canonical systems found. Government UI
  actions are now tied to tested server-issued block sessions; remaining
  Government work is command/GameTest coverage and vote lifecycle polish.
- Current handoff snapshot: `docs/ai/CURRENT_STATUS.md`.
- Project-wide revamp audit snapshot:
  `docs/reports/PROJECT_REVAMP_AUDIT.md`.
- Config/Admin deep audit snapshot:
  `docs/reports/CONFIG_ADMIN_AUDIT.md`.
- Current active manual verification areas: Collection UI mount previews/icon
  frames/Titles tab, Government civic records, Shrine/Government reset
  preservation, tablist Realm headers, Underworld V1, and Character Lifecycle.

## Paused Work Snapshot

- Resumed on 2026-06-29 for Collection preview hardening.
- Implemented but not yet manually verified in-game: Core direct
  `ElarionMenuEntityPreviewRenderer` path for selected unlocked mount previews
  with pre-scissor UI flushing, bounds-aware Mounts preview scale/offset from
  converted geo model bounds, clean row icon frames without black corner pixels,
  pinned Mounts/Pets/Titles tab order, and Core-owned Titles Collection tab
  backed by `TitleService`.
- Last focused verification:
  `.\gradlew.bat :platform:core:compileJava :platform:core:test :addons:mounts:compileJava :addons:mounts:test`
  passed.
- Next resume step: run client, open Collection with `C`, unlock/select a mount,
  confirm the model appears in the right preview frame, confirm icon-frame
  corners are clean, and confirm Titles tab can set an active title.

## Next Work Style

- Make small, focused edits.
- Prefer bounded event-driven work over global scans.
- Update docs and tests when behavior changes.
- Keep runtime state in `world/elarion/` and editable definitions in
  `config/elarion/`.
- Treat client packets as requests only; server-side context must prove the
  player is allowed to mutate state.
